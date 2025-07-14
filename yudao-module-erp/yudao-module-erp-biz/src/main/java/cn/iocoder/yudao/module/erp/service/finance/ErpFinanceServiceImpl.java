package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.erp.enums.DictTypeConstants.*;

@Service
@Validated
public class ErpFinanceServiceImpl implements ErpFinanceService {

    @Resource
    private ErpFinanceMapper financeMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpFinanceAmountService financeAmountService;

    @Resource
    private DictDataApi dictDataApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFinance(ErpFinanceSaveReqVO createReqVO, String currentUsername) {
        // 1. 校验数据
        validateFinanceForCreateOrUpdate(null, createReqVO);

        // 2. 生成财务记录编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.FINANCE_NO_PREFIX);
        if (financeMapper.selectByNo(no) != null) {
            throw exception(FINANCE_NO_EXISTS);
        }

        // 3. 插入财务记录
        ErpFinanceDO finance = BeanUtils.toBean(createReqVO, ErpFinanceDO.class)
                .setNo(no)
                .setAuditStatus(10); // 默认待审核
        financeMapper.insert(finance);

        // 4. 同步余额变化（触发前端刷新）
        if (finance.getAmount() != null && finance.getAccount() != null && finance.getIncomeExpense() != null) {
            financeAmountService.updateBalance(currentUsername, finance.getAccount(), finance.getAmount(), finance.getIncomeExpense());
        }

        return finance.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinance(ErpFinanceSaveReqVO updateReqVO, String currentUsername) {
        // 1.1 校验存在
        ErpFinanceDO oldFinance = validateFinance(updateReqVO.getId(), currentUsername);

        // 1.2 校验审核状态，已审核的记录不能修改
        if (oldFinance.getAuditStatus() != null && oldFinance.getAuditStatus() == 20) {
            throw exception(FINANCE_AUDIT_STATUS_NOT_ALLOW_UPDATE);
        }

        // 1.3 校验数据
        validateFinanceForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新财务记录
        ErpFinanceDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinanceDO.class);
        financeMapper.updateById(updateObj);

        // 3. 同步余额变化（触发前端刷新）
        // 由于余额是实时计算的，只需要触发一次同步即可
        if (updateObj.getAmount() != null && updateObj.getAccount() != null && updateObj.getIncomeExpense() != null) {
            financeAmountService.updateBalance(currentUsername, updateObj.getAccount(), updateObj.getAmount(), updateObj.getIncomeExpense());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinance(List<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在且属于当前用户，并记录要删除的财务记录信息
        List<ErpFinanceDO> finances = new ArrayList<>();
        for (Long id : ids) {
            ErpFinanceDO finance = validateFinance(id, currentUsername);
            // 校验审核状态，已审核的记录不能删除
            if (finance.getAuditStatus() != null && finance.getAuditStatus() == 20) {
                throw exception(FINANCE_AUDIT_STATUS_NOT_ALLOW_DELETE);
            }
            finances.add(finance);
        }

        // 2. 删除财务记录
        financeMapper.deleteBatchIds(ids);

        // 3. 同步余额变化（触发前端刷新）
        // 由于余额是实时计算的，只需要触发一次同步即可
        for (ErpFinanceDO finance : finances) {
            if (finance.getAmount() != null && finance.getAccount() != null && finance.getIncomeExpense() != null) {
                financeAmountService.updateBalance(currentUsername, finance.getAccount(), finance.getAmount(), finance.getIncomeExpense());
                break; // 只需要触发一次即可
            }
        }
    }

    @Override
    public ErpFinanceDO getFinance(Long id, String currentUsername) {
        ErpFinanceDO finance = financeMapper.selectById(id);
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (finance != null && !"admin".equals(currentUsername) && !"ahao".equals(currentUsername) &&!"caiwu".equals(currentUsername) && !ObjectUtil.equal(finance.getCreator(), currentUsername)) {
            return null; // 不是当前用户的数据且不是admin，返回null
        }
        return finance;
    }

    @Override
    public ErpFinanceDO validateFinance(Long id, String currentUsername) {
        ErpFinanceDO finance = financeMapper.selectById(id);
        if (finance == null) {
            throw exception(FINANCE_NOT_EXISTS);
        }
        // admin用户可以操作全部数据，其他用户只能操作自己的数据
        if (!"ahao".equals(currentUsername) &&!"caiwu".equals(currentUsername) &&!"admin".equals(currentUsername) && !ObjectUtil.equal(finance.getCreator(), currentUsername)) {
            throw exception(FINANCE_NOT_EXISTS); // 不是当前用户的数据且不是admin
        }
        return finance;
    }

    @Override
    public PageResult<ErpFinanceRespVO> getFinanceVOPage(ErpFinancePageReqVO pageReqVO, String currentUsername) {
        return financeMapper.selectPage(pageReqVO, currentUsername);
    }

    @Override
    public List<ErpFinanceRespVO> getFinanceVOList(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpFinanceDO> list = financeMapper.selectBatchIds(ids);
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"ahao".equals(currentUsername) &&!"caiwu".equals(currentUsername) &&!"admin".equals(currentUsername)) {
            list = list.stream()
                    .filter(item -> ObjectUtil.equal(item.getCreator(), currentUsername))
                    .collect(ArrayList::new, (l, item) -> l.add(item), ArrayList::addAll);
        }
        return BeanUtils.toBean(list, ErpFinanceRespVO.class);
    }

    @Override
    public Map<Long, ErpFinanceRespVO> getFinanceVOMap(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<ErpFinanceRespVO> list = getFinanceVOList(ids, currentUsername);
        return convertMap(list, ErpFinanceRespVO::getId);
    }

    @Override
    public List<ErpFinanceDO> getFinanceList(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpFinanceDO> list = financeMapper.selectBatchIds(ids);
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"ahao".equals(currentUsername) &&!"caiwu".equals(currentUsername) && !"admin".equals(currentUsername)) {
            list = list.stream()
                    .filter(item -> ObjectUtil.equal(item.getCreator(), currentUsername))
                    .collect(ArrayList::new, (l, item) -> l.add(item), ArrayList::addAll);
        }
        return list;
    }

    private void validateFinanceForCreateOrUpdate(Long id, ErpFinanceSaveReqVO reqVO) {
        // 1. 校验编号唯一
        ErpFinanceDO finance = financeMapper.selectByNo(reqVO.getNo());
        if (finance != null && !ObjectUtil.equal(finance.getId(), id)) {
            throw exception(FINANCE_NO_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpFinanceImportRespVO importFinanceList(List<ErpFinanceImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(FINANCE_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpFinanceImportRespVO respVO = ErpFinanceImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        try {
            // 1. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateAllImportData(importList, isUpdateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 2. 批量处理列表
            List<ErpFinanceDO> createList = new ArrayList<>();
            List<ErpFinanceDO> updateList = new ArrayList<>();

            // 3. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpFinanceImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpFinanceDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(financeMapper.selectListByNoIn(noSet), ErpFinanceDO::getNo);

            // 4. 批量转换和保存数据
            for (int i = 0; i < importList.size(); i++) {
                ErpFinanceImportExcelVO importVO = importList.get(i);

                // 数据转换
                ErpFinanceDO finance = convertImportVOToDO(importVO);

                // 判断是新增还是更新
                ErpFinanceDO existFinance = existMap.get(importVO.getNo());
                if (existFinance == null) {
                    // 创建财务记录
                    finance.setNo(noRedisDAO.generate(ErpNoRedisDAO.FINANCE_NO_PREFIX));
                    finance.setAuditStatus(10); // 默认待审核
                    createList.add(finance);
                    respVO.getCreateNames().add(finance.getBillName());
                } else if (isUpdateSupport) {
                    // 更新财务记录
                    finance.setId(existFinance.getId());
                    updateList.add(finance);
                    respVO.getUpdateNames().add(finance.getBillName());
                }
            }

            // 5. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                financeMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(financeMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        } finally {
            // 清除转换错误
            ConversionErrorHolder.clearErrors();
        }

        return respVO;
    }

    /**
     * 统一校验所有导入数据（包括数据类型校验和业务逻辑校验）
     * 如果出现任何错误信息都记录下来并返回，后续操作就不进行了
     */
    private Map<String, String> validateAllImportData(List<ErpFinanceImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpFinanceImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpFinanceDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(financeMapper.selectListByNoIn(noSet), ErpFinanceDO::getNo);

        // 3. 批量查询收入支出字典数据
        List<DictDataRespDTO> incomeExpenseDictList = dictDataApi.getDictDataList(FINANCE_INCOME_EXPENSE);
        Set<String> validIncomeExpenseSet = incomeExpenseDictList.stream()
                .map(DictDataRespDTO::getValue)
                .collect(Collectors.toSet());

        // 4. 批量查询收付类目字典数据
        List<DictDataRespDTO> categoryDictList = dictDataApi.getDictDataList(FINANCE_CATEGORY);
        Set<String> validCategorySet = categoryDictList.stream()
                .map(DictDataRespDTO::getValue)
                .collect(Collectors.toSet());

        // 5. 批量查询账单状态字典数据
        List<DictDataRespDTO> billStatusDictList = dictDataApi.getDictDataList(FINANCE_BILL_STATUS);
        Set<String> validBillStatusSet = billStatusDictList.stream()
                .map(DictDataRespDTO::getValue)
                .collect(Collectors.toSet());

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();

        // 6. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpFinanceImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getBillName()) ? "(" + importVO.getBillName() + ")" : "");

            try {

                // 6.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "财务编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 6.6 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpFinanceDO finance = convertImportVOToDO(importVO);
                    if (finance == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 6.7 判断是新增还是更新，并进行相应校验
                ErpFinanceDO existFinance = existMap.get(importVO.getNo());
                if (existFinance == null) {
                    // 新增校验：无需额外校验
                } else if (isUpdateSupport) {
                    // 更新校验：无需额外校验
                } else {
                    allErrors.put(errorKey, "财务编号不存在且不支持更新: " + importVO.getNo());
                    continue;
                }
            } catch (Exception ex) {
                allErrors.put(errorKey, "系统异常: " + ex.getMessage());
            }
        }

        return allErrors;
    }

    /**
     * 数据类型校验前置检查
     * 检查所有转换错误，如果有错误则返回错误信息，不进行后续导入
     */
    private Map<String, String> validateDataTypeErrors(List<ErpFinanceImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取财务名称 - 修复行号索引问题
                String financeName = "未知财务记录";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpFinanceImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getBillName())) {
                        financeName = importVO.getBillName();
                    } else if (StrUtil.isNotBlank(importVO.getNo())) {
                        financeName = importVO.getNo();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + financeName + ")";
                List<String> errorMessages = new ArrayList<>();

                for (ConversionErrorHolder.ConversionError error : errors) {
                    errorMessages.add(error.getErrorMessage());
                }

                String errorMsg = String.join("; ", errorMessages);
                dataTypeErrors.put(errorKey, "数据类型错误: " + errorMsg);
            }
        }

        return dataTypeErrors;
    }

    /**
     * 将导入VO转换为DO
     */
    private ErpFinanceDO convertImportVOToDO(ErpFinanceImportExcelVO importVO) {
        if (importVO == null) {
            return null;
        }

        try {
            // 使用BeanUtils进行基础转换
            ErpFinanceDO finance = BeanUtils.toBean(importVO, ErpFinanceDO.class);
            return finance;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditFinance(ErpFinanceAuditReqVO auditReqVO, String currentUsername) {
        if (CollUtil.isEmpty(auditReqVO.getIds())) {
            return;
        }

        // 1. 校验记录存在且属于当前用户
        List<ErpFinanceDO> finances = new ArrayList<>();
        for (Long id : auditReqVO.getIds()) {
            ErpFinanceDO finance = validateFinance(id, currentUsername);
            // 校验审核状态，只有待审核的记录才能审核
            if (finance.getAuditStatus() != null && finance.getAuditStatus() != 10) {
                throw exception(FINANCE_AUDIT_STATUS_NOT_ALLOW_AUDIT);
            }
            finances.add(finance);
        }

        // 2. 批量更新审核状态
        for (ErpFinanceDO finance : finances) {
            finance.setAuditStatus(auditReqVO.getAuditStatus());
            finance.setAuditor(currentUsername);
            finance.setAuditTime(LocalDateTime.now());
            finance.setAuditRemark(auditReqVO.getAuditRemark());
            financeMapper.updateById(finance);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unauditFinance(List<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        // 1. 校验记录存在且属于当前用户
        List<ErpFinanceDO> finances = new ArrayList<>();
        for (Long id : ids) {
            ErpFinanceDO finance = validateFinance(id, currentUsername);
            // 校验审核状态，只有已审核的记录才能反审核
            if (finance.getAuditStatus() == null || finance.getAuditStatus() != 20) {
                throw exception(FINANCE_AUDIT_STATUS_NOT_ALLOW_UNAUDIT);
            }
            finances.add(finance);
        }

        // 2. 批量更新审核状态为待审核
        for (ErpFinanceDO finance : finances) {
            finance.setAuditStatus(10);
            finance.setAuditor(null);
            finance.setAuditTime(null);
            finance.setAuditRemark(null);
            financeMapper.updateById(finance);
        }
    }
}
