package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpFinanceServiceImpl implements ErpFinanceService {

    @Resource
    private ErpFinanceMapper financeMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpFinanceAmountService financeAmountService;

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
        if (finance != null && !ObjectUtil.equal(finance.getCreator(), currentUsername)) {
            return null; // 不是当前用户的数据，返回null
        }
        return finance;
    }

    @Override
    public ErpFinanceDO validateFinance(Long id, String currentUsername) {
        ErpFinanceDO finance = financeMapper.selectById(id);
        if (finance == null) {
            throw exception(FINANCE_NOT_EXISTS);
        }
        if (!ObjectUtil.equal(finance.getCreator(), currentUsername)) {
            throw exception(FINANCE_NOT_EXISTS); // 不是当前用户的数据
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
        // 过滤出当前用户的数据
        list = list.stream()
                .filter(item -> ObjectUtil.equal(item.getCreator(), currentUsername))
                .collect(ArrayList::new, (l, item) -> l.add(item), ArrayList::addAll);
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
        // 过滤出当前用户的数据
        return list.stream()
                .filter(item -> ObjectUtil.equal(item.getCreator(), currentUsername))
                .collect(ArrayList::new, (l, item) -> l.add(item), ArrayList::addAll);
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

        // 批量处理
        List<ErpFinanceDO> createList = new ArrayList<>();
        List<ErpFinanceDO> updateList = new ArrayList<>();

        try {
            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpFinanceImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpFinanceDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(financeMapper.selectListByNoIn(noSet), ErpFinanceDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();
            
            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpFinanceImportExcelVO importVO = importList.get(i);
                try {
                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(FINANCE_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 判断是否支持更新
                    ErpFinanceDO existFinance = existMap.get(importVO.getNo());
                    if (existFinance == null) {
                       // 创建 - 自动生成新的no编号
                       ErpFinanceDO finance = BeanUtils.toBean(importVO, ErpFinanceDO.class);
                       finance.setNo(noRedisDAO.generate(ErpNoRedisDAO.FINANCE_NO_PREFIX));
                        createList.add(finance);
                        respVO.getCreateNames().add(finance.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpFinanceDO updateFinance = BeanUtils.toBean(importVO, ErpFinanceDO.class);
                        updateFinance.setId(existFinance.getId());
                        updateList.add(updateFinance);
                        respVO.getUpdateNames().add(updateFinance.getNo());
                    } else {
                        throw exception(FINANCE_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
                    }
                } catch (ServiceException ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");
                    respVO.getFailureNames().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");
                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                financeMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(financeMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
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