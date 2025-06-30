package cn.iocoder.yudao.module.erp.service.livebroadcasting;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcasting.ErpLiveBroadcastingDO;
import cn.iocoder.yudao.module.erp.dal.mysql.livebroadcasting.ErpLiveBroadcastingMapper;
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
public class ErpLiveBroadcastingServiceImpl implements ErpLiveBroadcastingService {

    @Resource
    private ErpLiveBroadcastingMapper liveBroadcastingMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLiveBroadcasting(ErpLiveBroadcastingSaveReqVO createReqVO) {
        // 1. 校验数据
        validateLiveBroadcastingForCreateOrUpdate(null, createReqVO);

        // 2. 生成直播货盘编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.LIVE_BROADCASTING_NO_PREFIX);
        if (liveBroadcastingMapper.selectByNo(no) != null) {
            throw exception(LIVE_BROADCASTING_NO_EXISTS);
        }

        // 3. 插入直播货盘记录
        ErpLiveBroadcastingDO liveBroadcasting = BeanUtils.toBean(createReqVO, ErpLiveBroadcastingDO.class)
                .setNo(no);
        liveBroadcastingMapper.insert(liveBroadcasting);

        return liveBroadcasting.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLiveBroadcasting(ErpLiveBroadcastingSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateLiveBroadcasting(updateReqVO.getId());
        // 1.2 校验数据
        validateLiveBroadcastingForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新直播货盘记录
        ErpLiveBroadcastingDO updateObj = BeanUtils.toBean(updateReqVO, ErpLiveBroadcastingDO.class);
        liveBroadcastingMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLiveBroadcasting(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpLiveBroadcastingDO> liveBroadcastings = liveBroadcastingMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(liveBroadcastings)) {
            throw exception(LIVE_BROADCASTING_NOT_EXISTS);
        }
        // 2. 删除直播货盘记录
        liveBroadcastingMapper.deleteBatchIds(ids);
    }

    @Override
    public PageResult<ErpLiveBroadcastingRespVO> getLiveBroadcastingVOPage(ErpLiveBroadcastingPageReqVO pageReqVO) {
        return liveBroadcastingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpLiveBroadcastingRespVO> getLiveBroadcastingVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpLiveBroadcastingDO> list = liveBroadcastingMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpLiveBroadcastingRespVO.class);
    }

    @Override
    public Map<Long, ErpLiveBroadcastingRespVO> getLiveBroadcastingVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingVOList(ids), ErpLiveBroadcastingRespVO::getId);
    }

    @Override
    public ErpLiveBroadcastingDO getLiveBroadcasting(Long id) {
        return liveBroadcastingMapper.selectById(id);
    }

    @Override
    public ErpLiveBroadcastingDO validateLiveBroadcasting(Long id) {
        ErpLiveBroadcastingDO liveBroadcasting = liveBroadcastingMapper.selectById(id);
        if (liveBroadcasting == null) {
            throw exception(LIVE_BROADCASTING_NOT_EXISTS);
        }
        return liveBroadcasting;
    }

    @Override
    public List<ErpLiveBroadcastingDO> getLiveBroadcastingList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return liveBroadcastingMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpLiveBroadcastingDO> getLiveBroadcastingMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingList(ids), ErpLiveBroadcastingDO::getId);
    }

    private void validateLiveBroadcastingForCreateOrUpdate(Long id, ErpLiveBroadcastingSaveReqVO reqVO) {
        // 1. 校验直播货盘编号唯一
        ErpLiveBroadcastingDO liveBroadcasting = liveBroadcastingMapper.selectByNo(reqVO.getNo());
        if (liveBroadcasting != null && !liveBroadcasting.getId().equals(id)) {
            throw exception(LIVE_BROADCASTING_NO_EXISTS);
        }
        
        // 2. 校验产品名称唯一
        if (StrUtil.isNotBlank(reqVO.getProductName())) {
            ErpLiveBroadcastingDO existProductName = liveBroadcastingMapper.selectByProductName(reqVO.getProductName());
            if (existProductName != null && !existProductName.getId().equals(id)) {
                throw exception(LIVE_BROADCASTING_PRODUCT_NAME_DUPLICATE, reqVO.getProductName());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpLiveBroadcastingImportRespVO importLiveBroadcastingList(List<ErpLiveBroadcastingImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(LIVE_BROADCASTING_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpLiveBroadcastingImportRespVO respVO = ErpLiveBroadcastingImportRespVO.builder()
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

            // 2. 批量处理数据
            List<ErpLiveBroadcastingDO> createList = new ArrayList<>();
            List<ErpLiveBroadcastingDO> updateList = new ArrayList<>();

            // 3. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpLiveBroadcastingImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpLiveBroadcastingDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(liveBroadcastingMapper.selectListByNoIn(noSet), ErpLiveBroadcastingDO::getNo);

            // 4. 批量转换和保存数据
            for (int i = 0; i < importList.size(); i++) {
                ErpLiveBroadcastingImportExcelVO importVO = importList.get(i);

                // 判断是否支持更新
                ErpLiveBroadcastingDO existLiveBroadcasting = existMap.get(importVO.getNo());
                if (existLiveBroadcasting == null) {
                    // 创建 - 自动生成新的no编号
                    ErpLiveBroadcastingDO liveBroadcasting = BeanUtils.toBean(importVO, ErpLiveBroadcastingDO.class);
                    liveBroadcasting.setNo(noRedisDAO.generate(ErpNoRedisDAO.LIVE_BROADCASTING_NO_PREFIX));
                    createList.add(liveBroadcasting);
                    respVO.getCreateNames().add(liveBroadcasting.getNo());
                } else if (isUpdateSupport) {
                    // 更新
                    ErpLiveBroadcastingDO updateLiveBroadcasting = BeanUtils.toBean(importVO, ErpLiveBroadcastingDO.class);
                    updateLiveBroadcasting.setId(existLiveBroadcasting.getId());
                    updateList.add(updateLiveBroadcasting);
                    respVO.getUpdateNames().add(updateLiveBroadcasting.getNo());
                }
            }

            // 5. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                liveBroadcastingMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(liveBroadcastingMapper::updateById);
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
    private Map<String, String> validateAllImportData(List<ErpLiveBroadcastingImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpLiveBroadcastingImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpLiveBroadcastingDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(liveBroadcastingMapper.selectListByNoIn(noSet), ErpLiveBroadcastingDO::getNo);

        // 2.1 批量查询已存在的产品名称
        Set<String> productNameSet = importList.stream()
                .map(ErpLiveBroadcastingImportExcelVO::getProductName)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpLiveBroadcastingDO> existProductNameMap = productNameSet.isEmpty() ? Collections.emptyMap() :
                convertMap(liveBroadcastingMapper.selectListByProductNameIn(productNameSet), ErpLiveBroadcastingDO::getProductName);

        // 用于跟踪Excel内部重复的编号和产品名称
        Set<String> processedNos = new HashSet<>();
        Set<String> processedProductNames = new HashSet<>();

        // 3. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpLiveBroadcastingImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getProductName()) ? "(" + importVO.getProductName() + ")" : "");

            try {
                // 3.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "直播货盘编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 3.3 检查Excel内部产品名称重复
                if (StrUtil.isNotBlank(importVO.getProductName())) {
                    if (processedProductNames.contains(importVO.getProductName())) {
                        allErrors.put(errorKey, "产品名称重复: " + importVO.getProductName());
                        continue;
                    }
                    processedProductNames.add(importVO.getProductName());
                }


                // 3.7 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpLiveBroadcastingDO liveBroadcasting = BeanUtils.toBean(importVO, ErpLiveBroadcastingDO.class);
                    if (liveBroadcasting == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 3.8 判断是新增还是更新，并进行相应校验
                ErpLiveBroadcastingDO existLiveBroadcasting = existMap.get(importVO.getNo());
                if (existLiveBroadcasting == null) {
                    // 新增校验：检查产品名称是否已存在
                    if (StrUtil.isNotBlank(importVO.getProductName())) {
                        ErpLiveBroadcastingDO existProductName = existProductNameMap.get(importVO.getProductName());
                        if (existProductName != null) {
                            allErrors.put(errorKey, "产品名称已存在: " + importVO.getProductName());
                            continue;
                        }
                    }
                } else if (isUpdateSupport) {
                    // 更新校验：检查产品名称是否与其他记录重复
                    if (StrUtil.isNotBlank(importVO.getProductName())) {
                        ErpLiveBroadcastingDO existProductName = existProductNameMap.get(importVO.getProductName());
                        if (existProductName != null && !existProductName.getId().equals(existLiveBroadcasting.getId())) {
                            allErrors.put(errorKey, "产品名称已存在: " + importVO.getProductName());
                            continue;
                        }
                    }
                } else {
                    allErrors.put(errorKey, "直播货盘编号不存在且不支持更新: " + importVO.getNo());
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
    private Map<String, String> validateDataTypeErrors(List<ErpLiveBroadcastingImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取产品名称 - 修复行号索引问题
                String productName = "未知产品";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpLiveBroadcastingImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getProductName())) {
                        productName = importVO.getProductName();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + productName + ")";
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
