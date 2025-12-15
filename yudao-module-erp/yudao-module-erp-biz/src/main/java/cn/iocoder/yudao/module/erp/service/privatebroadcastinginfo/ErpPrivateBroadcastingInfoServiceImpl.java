package cn.iocoder.yudao.module.erp.service.privatebroadcastinginfo;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastinginfo.ErpPrivateBroadcastingInfoDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcastinginfo.ErpPrivateBroadcastingInfoMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpCustomerMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
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
public class ErpPrivateBroadcastingInfoServiceImpl implements ErpPrivateBroadcastingInfoService {

    @Resource
    private ErpPrivateBroadcastingInfoMapper privateBroadcastingInfoMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpCustomerMapper customerMapper;

    @Resource
    private DictDataApi dictDataApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPrivateBroadcastingInfo(ErpPrivateBroadcastingInfoSaveReqVO createReqVO) {
        // 1. 校验数据
        validatePrivateBroadcastingInfoForCreateOrUpdate(null, createReqVO);

        // 2. 生成私播信息编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.PRIVATE_BROADCASTING_INFO_NO_PREFIX);
        if (privateBroadcastingInfoMapper.selectByNo(no) != null) {
            throw exception(PRIVATE_BROADCASTING_INFO_NO_EXISTS);
        }

        // 3. 插入私播信息记录
        ErpPrivateBroadcastingInfoDO privateBroadcastingInfo = BeanUtils.toBean(createReqVO, ErpPrivateBroadcastingInfoDO.class)
                .setNo(no);
        privateBroadcastingInfoMapper.insert(privateBroadcastingInfo);

        return privateBroadcastingInfo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivateBroadcastingInfo(ErpPrivateBroadcastingInfoSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validatePrivateBroadcastingInfo(updateReqVO.getId());
        // 1.2 校验数据
        validatePrivateBroadcastingInfoForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新私播信息记录
        ErpPrivateBroadcastingInfoDO updateObj = BeanUtils.toBean(updateReqVO, ErpPrivateBroadcastingInfoDO.class);
        privateBroadcastingInfoMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePrivateBroadcastingInfo(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpPrivateBroadcastingInfoDO> privateBroadcastingInfos = privateBroadcastingInfoMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(privateBroadcastingInfos)) {
            throw exception(PRIVATE_BROADCASTING_INFO_NOT_EXISTS);
        }
        // 2. 删除私播信息记录
        privateBroadcastingInfoMapper.deleteBatchIds(ids);
    }

    @Override
    public PageResult<ErpPrivateBroadcastingInfoRespVO> getPrivateBroadcastingInfoVOPage(ErpPrivateBroadcastingInfoPageReqVO pageReqVO) {
        return privateBroadcastingInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpPrivateBroadcastingInfoRespVO> getPrivateBroadcastingInfoVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpPrivateBroadcastingInfoDO> list = privateBroadcastingInfoMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpPrivateBroadcastingInfoRespVO.class);
    }

    @Override
    public Map<Long, ErpPrivateBroadcastingInfoRespVO> getPrivateBroadcastingInfoVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPrivateBroadcastingInfoVOList(ids), ErpPrivateBroadcastingInfoRespVO::getId);
    }

    @Override
    public ErpPrivateBroadcastingInfoDO getPrivateBroadcastingInfo(Long id) {
        return privateBroadcastingInfoMapper.selectById(id);
    }

    @Override
    public ErpPrivateBroadcastingInfoDO validatePrivateBroadcastingInfo(Long id) {
        ErpPrivateBroadcastingInfoDO privateBroadcastingInfo = privateBroadcastingInfoMapper.selectById(id);
        if (privateBroadcastingInfo == null) {
            throw exception(PRIVATE_BROADCASTING_INFO_NOT_EXISTS);
        }
        return privateBroadcastingInfo;
    }

    @Override
    public List<ErpPrivateBroadcastingInfoDO> getPrivateBroadcastingInfoList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return privateBroadcastingInfoMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpPrivateBroadcastingInfoDO> getPrivateBroadcastingInfoMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPrivateBroadcastingInfoList(ids), ErpPrivateBroadcastingInfoDO::getId);
    }

    private void validatePrivateBroadcastingInfoForCreateOrUpdate(Long id, ErpPrivateBroadcastingInfoSaveReqVO reqVO) {
        // 1. 校验私播信息编号唯一
        ErpPrivateBroadcastingInfoDO privateBroadcastingInfo = privateBroadcastingInfoMapper.selectByNo(reqVO.getNo());
        if (privateBroadcastingInfo != null && !privateBroadcastingInfo.getId().equals(id)) {
            throw exception(PRIVATE_BROADCASTING_INFO_NO_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpPrivateBroadcastingInfoImportRespVO importPrivateBroadcastingInfoList(List<ErpPrivateBroadcastingInfoImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(PRIVATE_BROADCASTING_INFO_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpPrivateBroadcastingInfoImportRespVO respVO = ErpPrivateBroadcastingInfoImportRespVO.builder()
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
            List<ErpPrivateBroadcastingInfoDO> createList = new ArrayList<>();
            List<ErpPrivateBroadcastingInfoDO> updateList = new ArrayList<>();

            // 3. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpPrivateBroadcastingInfoImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpPrivateBroadcastingInfoDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(privateBroadcastingInfoMapper.selectListByNoIn(noSet), ErpPrivateBroadcastingInfoDO::getNo);

            // 4. 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpPrivateBroadcastingInfoImportExcelVO importVO = importList.get(i);

                // 判断是否支持更新
                ErpPrivateBroadcastingInfoDO existPrivateBroadcastingInfo = existMap.get(importVO.getNo());
                if (existPrivateBroadcastingInfo == null) {
                    // 创建 - 自动生成新的no编号
                    ErpPrivateBroadcastingInfoDO privateBroadcastingInfo = BeanUtils.toBean(importVO, ErpPrivateBroadcastingInfoDO.class);
                    privateBroadcastingInfo.setNo(noRedisDAO.generate(ErpNoRedisDAO.PRIVATE_BROADCASTING_INFO_NO_PREFIX));
                    createList.add(privateBroadcastingInfo);
                    respVO.getCreateNames().add(privateBroadcastingInfo.getNo());
                } else if (isUpdateSupport) {
                    // 更新 - 只更新导入文件中提供的非空字段，保留数据库中其他字段的原有值
                    // 逐字段判断并更新
                    if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                        existPrivateBroadcastingInfo.setCustomerName(importVO.getCustomerName());
                    }
                    if (StrUtil.isNotBlank(importVO.getCustomerPosition())) {
                        existPrivateBroadcastingInfo.setCustomerPosition(importVO.getCustomerPosition());
                    }
                    if (StrUtil.isNotBlank(importVO.getCustomerWechat())) {
                        existPrivateBroadcastingInfo.setCustomerWechat(importVO.getCustomerWechat());
                    }
                    if (StrUtil.isNotBlank(importVO.getPlatformName())) {
                        existPrivateBroadcastingInfo.setPlatformName(importVO.getPlatformName());
                    }
                    if (StrUtil.isNotBlank(importVO.getCustomerAttribute())) {
                        existPrivateBroadcastingInfo.setCustomerAttribute(importVO.getCustomerAttribute());
                    }
                    if (StrUtil.isNotBlank(importVO.getCustomerCity())) {
                        existPrivateBroadcastingInfo.setCustomerCity(importVO.getCustomerCity());
                    }
                    if (StrUtil.isNotBlank(importVO.getCustomerDistrict())) {
                        existPrivateBroadcastingInfo.setCustomerDistrict(importVO.getCustomerDistrict());
                    }
                    if (StrUtil.isNotBlank(importVO.getUserPortrait())) {
                        existPrivateBroadcastingInfo.setUserPortrait(importVO.getUserPortrait());
                    }
                    if (StrUtil.isNotBlank(importVO.getRecruitmentCategory())) {
                        existPrivateBroadcastingInfo.setRecruitmentCategory(importVO.getRecruitmentCategory());
                    }
                    if (StrUtil.isNotBlank(importVO.getSelectionCriteria())) {
                        existPrivateBroadcastingInfo.setSelectionCriteria(importVO.getSelectionCriteria());
                    }
                    if (StrUtil.isNotBlank(importVO.getRemark())) {
                        existPrivateBroadcastingInfo.setRemark(importVO.getRemark());
                    }
                    updateList.add(existPrivateBroadcastingInfo);
                    respVO.getUpdateNames().add(existPrivateBroadcastingInfo.getNo());
                }
            }

            // 5. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                privateBroadcastingInfoMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(privateBroadcastingInfoMapper::updateById);
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
    private Map<String, String> validateAllImportData(List<ErpPrivateBroadcastingInfoImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpPrivateBroadcastingInfoImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpPrivateBroadcastingInfoDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(privateBroadcastingInfoMapper.selectListByNoIn(noSet), ErpPrivateBroadcastingInfoDO::getNo);

        // 3. 批量查询客户信息
        Set<String> customerNames = importList.stream()
                .map(ErpPrivateBroadcastingInfoImportExcelVO::getCustomerName)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpCustomerDO> customerMap = customerNames.isEmpty() ? Collections.emptyMap() :
                convertMap(customerMapper.selectListByNameIn(customerNames), ErpCustomerDO::getName);

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();

        // 4. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpPrivateBroadcastingInfoImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");

            try {
                // 4.1 基础数据校验

                // 4.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "私播信息编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 4.3 校验客户名称是否存在
                if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                    ErpCustomerDO customer = customerMap.get(importVO.getCustomerName());
                    if (customer == null) {
                        allErrors.put(errorKey, "客户不存在: " + importVO.getCustomerName());
                        continue;
                    }
                }

                // 4.4 校验字典数据有效性
                try {
                    validateDictData(importVO.getCustomerPosition(), DictTypeConstants.ERP_PRIVATE_CUSTOMER_POSITION, "客户职位", i + 1);
                    validateDictData(importVO.getPlatformName(), DictTypeConstants.ERP_PRIVATE_PLATFORM_NAME, "平台名称", i + 1);
                    validateDictData(importVO.getCustomerAttribute(), DictTypeConstants.ERP_PRIVATE_CUSTOMER_ATTRIBUTE, "客户属性", i + 1);
                    validateDictData(importVO.getCustomerCity(), DictTypeConstants.ERP_PRIVATE_CUSTOMER_CITY, "客户城市", i + 1);
                    validateDictData(importVO.getCustomerDistrict(), DictTypeConstants.ERP_PRIVATE_CUSTOMER_DISTRICT, "客户区县", i + 1);
                } catch (ServiceException ex) {
                    allErrors.put(errorKey, ex.getMessage());
                    continue;
                }

                // 4.5 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpPrivateBroadcastingInfoDO privateBroadcastingInfo = convertImportVOToDO(importVO);
                    if (privateBroadcastingInfo == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 4.6 判断是新增还是更新，并进行相应校验
                ErpPrivateBroadcastingInfoDO existPrivateBroadcastingInfo = existMap.get(importVO.getNo());
                if (existPrivateBroadcastingInfo == null) {
                    // 新增校验：可以添加特定的校验逻辑
                } else if (isUpdateSupport) {
                    // 更新校验：可以添加特定的校验逻辑
                } else {
                    allErrors.put(errorKey, "私播信息编号不存在且不支持更新: " + importVO.getNo());
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
    private Map<String, String> validateDataTypeErrors(List<ErpPrivateBroadcastingInfoImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取私播信息编号 - 修复行号索引问题
                String privateBroadcastingInfoNo = "未知编号";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpPrivateBroadcastingInfoImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        privateBroadcastingInfoNo = importVO.getNo();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + privateBroadcastingInfoNo + ")";
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
    private ErpPrivateBroadcastingInfoDO convertImportVOToDO(ErpPrivateBroadcastingInfoImportExcelVO importVO) {
        if (importVO == null) {
            return null;
        }

        // 使用BeanUtils进行基础转换
        ErpPrivateBroadcastingInfoDO privateBroadcastingInfo = BeanUtils.toBean(importVO, ErpPrivateBroadcastingInfoDO.class);

        // 手动设置转换器处理的字段，确保数据正确传递
        // 根据实际需要添加字段转换逻辑

        return privateBroadcastingInfo;
    }

    /**
     * 校验字典数据有效性
     */
    private void validateDictData(String value, String dictType, String fieldName, int rowNum) {
        if (StrUtil.isBlank(value)) {
            return; // 空值不校验
        }

        try {
            // 使用字典API校验数据有效性
            dictDataApi.validateDictDataList(dictType, Collections.singletonList(value));
        } catch (Exception e) {
            throw exception(PRIVATE_BROADCASTING_INFO_DICT_DATA_INVALID, rowNum, fieldName, value);
        }
    }
}
