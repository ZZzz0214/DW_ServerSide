package cn.iocoder.yudao.module.erp.service.privatebroadcasting;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcasting.ErpPrivateBroadcastingDO;
import cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcasting.ErpPrivateBroadcastingMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpPrivateBroadcastingServiceImpl implements ErpPrivateBroadcastingService {

    @Resource
    private ErpPrivateBroadcastingMapper privateBroadcastingMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPrivateBroadcasting(ErpPrivateBroadcastingSaveReqVO createReqVO) {
        // 1. 校验数据
        validatePrivateBroadcastingForCreateOrUpdate(null, createReqVO);

        // 2. 生成私播货盘编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.PRIVATE_BROADCASTING_NO_PREFIX);
        if (privateBroadcastingMapper.selectByNo(no) != null) {
            throw exception(PRIVATE_BROADCASTING_NO_EXISTS);
        }

        // 3. 插入私播货盘记录
        ErpPrivateBroadcastingDO privateBroadcasting = BeanUtils.toBean(createReqVO, ErpPrivateBroadcastingDO.class)
                .setNo(no);
        privateBroadcastingMapper.insert(privateBroadcasting);

        return privateBroadcasting.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivateBroadcasting(ErpPrivateBroadcastingSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validatePrivateBroadcasting(updateReqVO.getId());
        // 1.2 校验数据
        validatePrivateBroadcastingForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新私播货盘记录
        ErpPrivateBroadcastingDO updateObj = BeanUtils.toBean(updateReqVO, ErpPrivateBroadcastingDO.class);
        privateBroadcastingMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePrivateBroadcasting(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpPrivateBroadcastingDO> privateBroadcastings = privateBroadcastingMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(privateBroadcastings)) {
            throw exception(PRIVATE_BROADCASTING_NOT_EXISTS);
        }
        // 2. 删除私播货盘记录
        privateBroadcastingMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpPrivateBroadcastingDO getPrivateBroadcasting(Long id) {
        return privateBroadcastingMapper.selectById(id);
    }

    @Override
    public ErpPrivateBroadcastingDO validatePrivateBroadcasting(Long id) {
        ErpPrivateBroadcastingDO privateBroadcasting = privateBroadcastingMapper.selectById(id);
        if (privateBroadcasting == null) {
            throw exception(PRIVATE_BROADCASTING_NOT_EXISTS);
        }
        return privateBroadcasting;
    }

    @Override
    public PageResult<ErpPrivateBroadcastingRespVO> getPrivateBroadcastingVOPage(ErpPrivateBroadcastingPageReqVO pageReqVO) {
        return privateBroadcastingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpPrivateBroadcastingRespVO> getPrivateBroadcastingVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpPrivateBroadcastingDO> list = privateBroadcastingMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpPrivateBroadcastingRespVO.class);
    }

    @Override
    public Map<Long, ErpPrivateBroadcastingRespVO> getPrivateBroadcastingVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPrivateBroadcastingVOList(ids), ErpPrivateBroadcastingRespVO::getId);
    }

    @Override
    public List<ErpPrivateBroadcastingDO> getPrivateBroadcastingList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return privateBroadcastingMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpPrivateBroadcastingDO> getPrivateBroadcastingMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPrivateBroadcastingList(ids), ErpPrivateBroadcastingDO::getId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpPrivateBroadcastingImportRespVO importPrivateBroadcastingList(List<ErpPrivateBroadcastingImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(PRIVATE_BROADCASTING_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpPrivateBroadcastingImportRespVO respVO = ErpPrivateBroadcastingImportRespVO.builder()
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
            List<ErpPrivateBroadcastingDO> createList = new ArrayList<>();
            List<ErpPrivateBroadcastingDO> updateList = new ArrayList<>();

            // 3. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpPrivateBroadcastingImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpPrivateBroadcastingDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(privateBroadcastingMapper.selectListByNoIn(noSet), ErpPrivateBroadcastingDO::getNo);

            // 4. 批量查询所有产品名称，用于校验产品名称唯一性
            Set<String> productNames = importList.stream()
                    .map(ErpPrivateBroadcastingImportExcelVO::getProductName)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpPrivateBroadcastingDO> productNameMap = productNames.isEmpty() ? Collections.emptyMap() :
                    convertMap(privateBroadcastingMapper.selectListByProductNameIn(productNames), ErpPrivateBroadcastingDO::getProductName);

            // 5. 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpPrivateBroadcastingImportExcelVO importVO = importList.get(i);

                // 判断是否支持更新
                ErpPrivateBroadcastingDO existPrivateBroadcasting = existMap.get(importVO.getNo());
                if (existPrivateBroadcasting == null) {
                    // 创建 - 自动生成新的no编号
                    ErpPrivateBroadcastingDO privateBroadcasting = BeanUtils.toBean(importVO, ErpPrivateBroadcastingDO.class);
                    privateBroadcasting.setNo(noRedisDAO.generate(ErpNoRedisDAO.PRIVATE_BROADCASTING_NO_PREFIX));
                    // 设置默认状态
                    if (StrUtil.isBlank(privateBroadcasting.getPrivateStatus())) {
                        privateBroadcasting.setPrivateStatus("未设置");
                    }
                    createList.add(privateBroadcasting);
                    respVO.getCreateNames().add(privateBroadcasting.getNo());
                } else if (isUpdateSupport) {
                    // 更新
                    ErpPrivateBroadcastingDO updatePrivateBroadcasting = BeanUtils.toBean(importVO, ErpPrivateBroadcastingDO.class);
                    updatePrivateBroadcasting.setId(existPrivateBroadcasting.getId());
                    // 如果状态为空，保持原状态
                    if (StrUtil.isBlank(updatePrivateBroadcasting.getPrivateStatus())) {
                        updatePrivateBroadcasting.setPrivateStatus(existPrivateBroadcasting.getPrivateStatus());
                    }
                    updateList.add(updatePrivateBroadcasting);
                    respVO.getUpdateNames().add(updatePrivateBroadcasting.getNo());
                }
            }

            // 6. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                createList.forEach(privateBroadcastingMapper::insert);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(privateBroadcastingMapper::updateById);
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
    private Map<String, String> validateAllImportData(List<ErpPrivateBroadcastingImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpPrivateBroadcastingImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpPrivateBroadcastingDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(privateBroadcastingMapper.selectListByNoIn(noSet), ErpPrivateBroadcastingDO::getNo);

        // 3. 批量查询所有产品名称，用于校验产品名称唯一性
        Set<String> productNames = importList.stream()
                .map(ErpPrivateBroadcastingImportExcelVO::getProductName)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpPrivateBroadcastingDO> productNameMap = productNames.isEmpty() ? Collections.emptyMap() :
                convertMap(privateBroadcastingMapper.selectListByProductNameIn(productNames), ErpPrivateBroadcastingDO::getProductName);

        // 用于跟踪Excel内部重复的编号和产品名称
        Set<String> processedNos = new HashSet<>();
        Set<String> processedProductNames = new HashSet<>();

        // 4. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpPrivateBroadcastingImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");

            try {
                // 4.1 基础数据校验
                if (StrUtil.isEmpty(importVO.getProductName())) {
                    allErrors.put(errorKey, "产品名称不能为空");
                    continue;
                }

                // 4.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "私播货盘编号重复: " + importVO.getNo());
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
                    ErpPrivateBroadcastingDO privateBroadcasting = convertImportVOToDO(importVO);
                    if (privateBroadcasting == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 4.5 判断是新增还是更新，并进行相应校验
                ErpPrivateBroadcastingDO existPrivateBroadcasting = existMap.get(importVO.getNo());
                if (existPrivateBroadcasting == null) {
                    // 新增校验：校验产品名称唯一性
                    ErpPrivateBroadcastingDO existProductName = productNameMap.get(importVO.getProductName());
                    if (existProductName != null) {
                        allErrors.put(errorKey, "产品名称已存在: " + importVO.getProductName());
                        continue;
                    }
                } else if (isUpdateSupport) {
                    // 更新校验：校验产品名称唯一性（排除自身）
                    ErpPrivateBroadcastingDO existProductName = productNameMap.get(importVO.getProductName());
                    if (existProductName != null && !ObjectUtil.equal(existProductName.getId(), existPrivateBroadcasting.getId())) {
                        allErrors.put(errorKey, "产品名称已存在: " + importVO.getProductName());
                        continue;
                    }
                } else {
                    allErrors.put(errorKey, "私播货盘编号不存在且不支持更新: " + importVO.getNo());
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
    private Map<String, String> validateDataTypeErrors(List<ErpPrivateBroadcastingImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取私播货盘编号 - 修复行号索引问题
                String privateBroadcastingNo = "未知编号";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpPrivateBroadcastingImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        privateBroadcastingNo = importVO.getNo();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + privateBroadcastingNo + ")";
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
     * 特别注意处理字段类型转换
     */
    private ErpPrivateBroadcastingDO convertImportVOToDO(ErpPrivateBroadcastingImportExcelVO importVO) {
        if (importVO == null) {
            return null;
        }

        // 使用BeanUtils进行基础转换
        ErpPrivateBroadcastingDO privateBroadcasting = BeanUtils.toBean(importVO, ErpPrivateBroadcastingDO.class);

        // 手动设置转换器处理的字段，确保数据正确传递

        return privateBroadcasting;
    }

    private void validatePrivateBroadcastingForCreateOrUpdate(Long id, ErpPrivateBroadcastingSaveReqVO reqVO) {
        // 1. 校验私播货盘编号唯一
        ErpPrivateBroadcastingDO privateBroadcasting = privateBroadcastingMapper.selectByNo(reqVO.getNo());
        if (privateBroadcasting != null && !ObjectUtil.equal(privateBroadcasting.getId(), id)) {
            throw exception(PRIVATE_BROADCASTING_NO_EXISTS);
        }

        // 2. 校验产品名称唯一
        validateProductNameUnique(reqVO.getProductName(), id);
    }

    /**
     * 校验产品名称是否唯一
     */
    private void validateProductNameUnique(String productName, Long excludeId) {
        if (StrUtil.isEmpty(productName)) {
            return;
        }

        ErpPrivateBroadcastingDO privateBroadcasting = privateBroadcastingMapper.selectByProductName(productName);
        if (privateBroadcasting != null && !ObjectUtil.equal(privateBroadcasting.getId(), excludeId)) {
            throw exception(PRIVATE_BROADCASTING_PRODUCT_NAME_DUPLICATE, productName);
        }
    }
}
