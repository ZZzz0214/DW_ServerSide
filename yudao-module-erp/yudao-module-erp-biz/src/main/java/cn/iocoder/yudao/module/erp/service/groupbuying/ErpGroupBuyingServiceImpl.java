package cn.iocoder.yudao.module.erp.service.groupbuying;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingDO;
import cn.iocoder.yudao.module.erp.dal.mysql.groupbuying.ErpGroupBuyingMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpGroupBuyingServiceImpl implements ErpGroupBuyingService {

    @Resource
    private ErpGroupBuyingMapper groupBuyingMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroupBuying(ErpGroupBuyingSaveReqVO createReqVO) {
        // 1. 校验数据
        validateGroupBuyingForCreateOrUpdate(null, createReqVO);

        // 2. 生成团购货盘编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.GROUP_BUYING_NO_PREFIX);
        if (groupBuyingMapper.selectByNo(no) != null) {
            throw exception(GROUP_BUYING_NO_EXISTS);
        }

        // 3. 插入团购货盘记录
        ErpGroupBuyingDO groupBuying = BeanUtils.toBean(createReqVO, ErpGroupBuyingDO.class)
                .setNo(no);
        groupBuyingMapper.insert(groupBuying);

        return groupBuying.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupBuying(ErpGroupBuyingSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateGroupBuying(updateReqVO.getId());
        // 1.2 校验数据
        validateGroupBuyingForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新团购货盘记录
        ErpGroupBuyingDO updateObj = BeanUtils.toBean(updateReqVO, ErpGroupBuyingDO.class);
        groupBuyingMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroupBuying(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpGroupBuyingDO> groupBuyings = groupBuyingMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(groupBuyings)) {
            throw exception(GROUP_BUYING_NOT_EXISTS);
        }
        // 2. 删除团购货盘记录
        groupBuyingMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpGroupBuyingDO getGroupBuying(Long id) {
        return groupBuyingMapper.selectById(id);
    }

    @Override
    public ErpGroupBuyingDO validateGroupBuying(Long id) {
        ErpGroupBuyingDO groupBuying = groupBuyingMapper.selectById(id);
        if (groupBuying == null) {
            throw exception(GROUP_BUYING_NOT_EXISTS);
        }
        return groupBuying;
    }

    @Override
    public PageResult<ErpGroupBuyingRespVO> getGroupBuyingVOPage(ErpGroupBuyingPageReqVO pageReqVO) {
        return groupBuyingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpGroupBuyingRespVO> getGroupBuyingVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpGroupBuyingDO> list = groupBuyingMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpGroupBuyingRespVO.class);
    }

    @Override
    public Map<Long, ErpGroupBuyingRespVO> getGroupBuyingVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingVOList(ids), ErpGroupBuyingRespVO::getId);
    }

    @Override
    public List<ErpGroupBuyingDO> getGroupBuyingList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return groupBuyingMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpGroupBuyingDO> getGroupBuyingMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingList(ids), ErpGroupBuyingDO::getId);
    }

    private void validateGroupBuyingForCreateOrUpdate(Long id, ErpGroupBuyingSaveReqVO reqVO) {
        // 1. 校验团购货盘编号唯一
        ErpGroupBuyingDO groupBuying = groupBuyingMapper.selectByNo(reqVO.getNo());
        if (groupBuying != null && !groupBuying.getId().equals(id)) {
            throw exception(GROUP_BUYING_NO_EXISTS);
        }
        
        // 2. 校验产品名称唯一
        if (StrUtil.isNotBlank(reqVO.getProductName())) {
            ErpGroupBuyingDO existingGroupBuying = groupBuyingMapper.selectByProductName(reqVO.getProductName());
            if (existingGroupBuying != null && !existingGroupBuying.getId().equals(id)) {
                throw exception(GROUP_BUYING_PRODUCT_NAME_DUPLICATE, reqVO.getProductName());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpGroupBuyingImportRespVO importGroupBuyingList(List<ErpGroupBuyingImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(GROUP_BUYING_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpGroupBuyingImportRespVO respVO = ErpGroupBuyingImportRespVO.builder()
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

            // 2. 批量处理
            List<ErpGroupBuyingDO> createList = new ArrayList<>();
            List<ErpGroupBuyingDO> updateList = new ArrayList<>();

            // 3. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpGroupBuyingImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpGroupBuyingDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(groupBuyingMapper.selectListByNoIn(noSet), ErpGroupBuyingDO::getNo);

            // 4. 批量转换和保存数据
            for (int i = 0; i < importList.size(); i++) {
                ErpGroupBuyingImportExcelVO importVO = importList.get(i);

                // 数据转换
                ErpGroupBuyingDO groupBuying = BeanUtils.toBean(importVO, ErpGroupBuyingDO.class);
                
                // 自动计算渠道毛利：渠道毛利 = ((开团价格 - 供团价格) / 开团价格) * 100
                if (groupBuying.getSupplyGroupPrice() != null && groupBuying.getGroupPrice() != null 
                    && groupBuying.getGroupPrice().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal channelProfit = groupBuying.getGroupPrice()
                            .subtract(groupBuying.getSupplyGroupPrice())
                            .divide(groupBuying.getGroupPrice(), 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(new BigDecimal("100"));
                    groupBuying.setChannelProfit(channelProfit);
                }

                // 判断是新增还是更新
                ErpGroupBuyingDO existGroupBuying = existMap.get(importVO.getNo());
                if (existGroupBuying == null) {
                    // 创建 - 自动生成新的no编号
                    groupBuying.setNo(noRedisDAO.generate(ErpNoRedisDAO.GROUP_BUYING_NO_PREFIX));
                    createList.add(groupBuying);
                    respVO.getCreateNames().add(groupBuying.getNo());
                } else if (isUpdateSupport) {
                    // 更新
                    groupBuying.setId(existGroupBuying.getId());
                    updateList.add(groupBuying);
                    respVO.getUpdateNames().add(groupBuying.getNo());
                }
            }

            // 5. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                groupBuyingMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(groupBuyingMapper::updateById);
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
    private Map<String, String> validateAllImportData(List<ErpGroupBuyingImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpGroupBuyingImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpGroupBuyingDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(groupBuyingMapper.selectListByNoIn(noSet), ErpGroupBuyingDO::getNo);

        // 3. 批量查询产品名称，用于校验重复
        Set<String> productNames = importList.stream()
                .map(ErpGroupBuyingImportExcelVO::getProductName)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpGroupBuyingDO> productNameMap = productNames.isEmpty() ? Collections.emptyMap() :
                convertMap(groupBuyingMapper.selectListByProductNameIn(productNames), ErpGroupBuyingDO::getProductName);

        // 用于跟踪Excel内部重复的编号和产品名称
        Set<String> processedNos = new HashSet<>();
        Set<String> processedProductNames = new HashSet<>();

        // 4. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpGroupBuyingImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");

            try {
                // 4.1 基础数据校验
                if (StrUtil.isBlank(importVO.getProductName())) {
                    allErrors.put(errorKey, "产品名称不能为空");
                    continue;
                }

                // 4.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "团购货盘编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 4.3 检查Excel内部产品名称重复
                if (StrUtil.isNotBlank(importVO.getProductName())) {
                    if (processedProductNames.contains(importVO.getProductName())) {
                        allErrors.put(errorKey, "产品名称重复: " + importVO.getProductName());
                        continue;
                    }
                    processedProductNames.add(importVO.getProductName());
                }

                // 4.4 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpGroupBuyingDO groupBuying = BeanUtils.toBean(importVO, ErpGroupBuyingDO.class);
                    if (groupBuying == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 4.5 判断是新增还是更新，并进行相应校验
                ErpGroupBuyingDO existGroupBuying = existMap.get(importVO.getNo());
                if (existGroupBuying == null) {
                    // 新增校验：校验产品名称唯一性
                    if (StrUtil.isNotBlank(importVO.getProductName())) {
                        ErpGroupBuyingDO existingProduct = productNameMap.get(importVO.getProductName());
                        if (existingProduct != null) {
                            allErrors.put(errorKey, "产品名称已存在: " + importVO.getProductName());
                            continue;
                        }
                    }
                } else if (!isUpdateSupport) {
                    // 更新校验：如果不支持更新，记录错误
                    allErrors.put(errorKey, "团购货盘编号已存在且不支持更新: " + importVO.getNo());
                    continue;
                } else {
                    // 更新校验：校验产品名称唯一性（排除当前记录）
                    if (StrUtil.isNotBlank(importVO.getProductName())) {
                        ErpGroupBuyingDO existingProduct = productNameMap.get(importVO.getProductName());
                        if (existingProduct != null && !existingProduct.getId().equals(existGroupBuying.getId())) {
                            allErrors.put(errorKey, "产品名称已存在: " + importVO.getProductName());
                            continue;
                        }
                    }
                }
            } catch (Exception ex) {
                allErrors.put(errorKey, "校验异常: " + ex.getMessage());
            }
        }

        return allErrors;
    }

    /**
     * 校验数据类型错误
     */
    private Map<String, String> validateDataTypeErrors(List<ErpGroupBuyingImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors =
                ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取团购货盘编号 - 修复行号索引问题
                String groupBuyingNo = "未知编号";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpGroupBuyingImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        groupBuyingNo = importVO.getNo();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + groupBuyingNo + ")";
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
}
