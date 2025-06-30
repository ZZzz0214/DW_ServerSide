package cn.iocoder.yudao.module.erp.service.groupbuyinginfo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuyinginfo.ErpGroupBuyingInfoDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.groupbuyinginfo.ErpGroupBuyingInfoMapper;
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
public class ErpGroupBuyingInfoServiceImpl implements ErpGroupBuyingInfoService {

    @Resource
    private ErpGroupBuyingInfoMapper groupBuyingInfoMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpCustomerMapper customerMapper;

    @Resource
    private DictDataApi dictDataApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroupBuyingInfo(ErpGroupBuyingInfoSaveReqVO createReqVO) {
        // 1. 校验数据
        validateGroupBuyingInfoForCreateOrUpdate(null, createReqVO);

        // 2. 生成团购信息编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.GROUP_BUYING_INFO_NO_PREFIX);
        if (groupBuyingInfoMapper.selectByNo(no) != null) {
            throw exception(GROUP_BUYING_INFO_NO_EXISTS);
        }

        // 3. 插入团购信息记录
        ErpGroupBuyingInfoDO groupBuyingInfo = BeanUtils.toBean(createReqVO, ErpGroupBuyingInfoDO.class)
                .setNo(no);
        groupBuyingInfoMapper.insert(groupBuyingInfo);

        return groupBuyingInfo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupBuyingInfo(ErpGroupBuyingInfoSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateGroupBuyingInfo(updateReqVO.getId());
        // 1.2 校验数据
        validateGroupBuyingInfoForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新团购信息记录
        ErpGroupBuyingInfoDO updateObj = BeanUtils.toBean(updateReqVO, ErpGroupBuyingInfoDO.class);
        groupBuyingInfoMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroupBuyingInfo(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpGroupBuyingInfoDO> groupBuyingInfos = groupBuyingInfoMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(groupBuyingInfos)) {
            throw exception(GROUP_BUYING_INFO_NOT_EXISTS);
        }
        // 2. 删除团购信息记录
        groupBuyingInfoMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpGroupBuyingInfoDO getGroupBuyingInfo(Long id) {
        return groupBuyingInfoMapper.selectById(id);
    }

    @Override
    public ErpGroupBuyingInfoDO validateGroupBuyingInfo(Long id) {
        ErpGroupBuyingInfoDO groupBuyingInfo = groupBuyingInfoMapper.selectById(id);
        if (groupBuyingInfo == null) {
            throw exception(GROUP_BUYING_INFO_NOT_EXISTS);
        }
        return groupBuyingInfo;
    }

    @Override
    public PageResult<ErpGroupBuyingInfoRespVO> getGroupBuyingInfoVOPage(ErpGroupBuyingInfoPageReqVO pageReqVO) {
        return groupBuyingInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpGroupBuyingInfoRespVO> getGroupBuyingInfoVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpGroupBuyingInfoDO> list = groupBuyingInfoMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpGroupBuyingInfoRespVO.class);
    }

    @Override
    public Map<Long, ErpGroupBuyingInfoRespVO> getGroupBuyingInfoVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingInfoVOList(ids), ErpGroupBuyingInfoRespVO::getId);
    }

    @Override
    public List<ErpGroupBuyingInfoDO> getGroupBuyingInfoList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return groupBuyingInfoMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpGroupBuyingInfoDO> getGroupBuyingInfoMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingInfoList(ids), ErpGroupBuyingInfoDO::getId);
    }

    private void validateGroupBuyingInfoForCreateOrUpdate(Long id, ErpGroupBuyingInfoSaveReqVO reqVO) {
        // 1. 校验团购信息编号唯一
        ErpGroupBuyingInfoDO groupBuyingInfo = groupBuyingInfoMapper.selectByNo(reqVO.getNo());
        if (groupBuyingInfo != null && !groupBuyingInfo.getId().equals(id)) {
            throw exception(GROUP_BUYING_INFO_NO_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpGroupBuyingInfoImportRespVO importGroupBuyingInfoList(List<ErpGroupBuyingInfoImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(GROUP_BUYING_INFO_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpGroupBuyingInfoImportRespVO respVO = ErpGroupBuyingInfoImportRespVO.builder()
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
            List<ErpGroupBuyingInfoDO> createList = new ArrayList<>();
            List<ErpGroupBuyingInfoDO> updateList = new ArrayList<>();

            // 3. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpGroupBuyingInfoImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpGroupBuyingInfoDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(groupBuyingInfoMapper.selectListByNoIn(noSet), ErpGroupBuyingInfoDO::getNo);

            // 4. 批量转换和保存数据
            for (int i = 0; i < importList.size(); i++) {
                ErpGroupBuyingInfoImportExcelVO importVO = importList.get(i);

                // 数据转换
                ErpGroupBuyingInfoDO groupBuyingInfo = BeanUtils.toBean(importVO, ErpGroupBuyingInfoDO.class);

                // 判断是新增还是更新
                ErpGroupBuyingInfoDO existGroupBuyingInfo = existMap.get(importVO.getNo());
                if (existGroupBuyingInfo == null) {
                    // 创建 - 自动生成新的no编号
                    groupBuyingInfo.setNo(noRedisDAO.generate(ErpNoRedisDAO.GROUP_BUYING_INFO_NO_PREFIX));
                    createList.add(groupBuyingInfo);
                    respVO.getCreateNames().add(groupBuyingInfo.getNo());
                } else if (isUpdateSupport) {
                    // 更新
                    groupBuyingInfo.setId(existGroupBuyingInfo.getId());
                    updateList.add(groupBuyingInfo);
                    respVO.getUpdateNames().add(groupBuyingInfo.getNo());
                }
            }

            // 5. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                groupBuyingInfoMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(groupBuyingInfoMapper::updateById);
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
    private Map<String, String> validateAllImportData(List<ErpGroupBuyingInfoImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpGroupBuyingInfoImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpGroupBuyingInfoDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(groupBuyingInfoMapper.selectListByNoIn(noSet), ErpGroupBuyingInfoDO::getNo);

        // 3. 批量查询客户信息
        Set<String> customerNames = importList.stream()
                .map(ErpGroupBuyingInfoImportExcelVO::getCustomerName)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpCustomerDO> customerMap = customerNames.isEmpty() ? Collections.emptyMap() :
                convertMap(customerMapper.selectListByNameIn(customerNames), ErpCustomerDO::getName);

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();

        // 4. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpGroupBuyingInfoImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");

            try {

                // 4.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "团购信息编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 4.3 校验客户是否存在
                if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                    ErpCustomerDO customer = customerMap.get(importVO.getCustomerName());
                    if (customer == null) {
                        allErrors.put(errorKey, "客户不存在: " + importVO.getCustomerName());
                        continue;
                    }
                }

                // 4.4 校验字典数据有效性
                try {
                    validateDictData(importVO.getCustomerPosition(), DictTypeConstants.ERP_CUSTOMER_POSITION, "客户职位", i + 1);
                    validateDictData(importVO.getPlatformName(), DictTypeConstants.ERP_PLATFORM_NAME, "平台名称", i + 1);
                    validateDictData(importVO.getCustomerAttribute(), DictTypeConstants.ERP_CUSTOMER_ATTRIBUTE, "客户属性", i + 1);
                    validateDictData(importVO.getCustomerCity(), DictTypeConstants.ERP_CUSTOMER_CITY, "客户城市", i + 1);
                    validateDictData(importVO.getCustomerDistrict(), DictTypeConstants.ERP_CUSTOMER_DISTRICT, "客户区县", i + 1);
                } catch (ServiceException ex) {
                    allErrors.put(errorKey, ex.getMessage());
                    continue;
                }

                // 4.5 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpGroupBuyingInfoDO groupBuyingInfo = BeanUtils.toBean(importVO, ErpGroupBuyingInfoDO.class);
                    if (groupBuyingInfo == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 4.6 判断是新增还是更新，并进行相应校验
                ErpGroupBuyingInfoDO existGroupBuyingInfo = existMap.get(importVO.getNo());
                if (existGroupBuyingInfo == null) {
                    // 新增校验：可以添加其他业务校验逻辑
                } else if (!isUpdateSupport) {
                    // 更新校验：如果不支持更新，记录错误
                    allErrors.put(errorKey, "团购信息编号已存在且不支持更新: " + importVO.getNo());
                    continue;
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
    private Map<String, String> validateDataTypeErrors(List<ErpGroupBuyingInfoImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取团购信息编号 - 修复行号索引问题
                String groupBuyingInfoNo = "未知编号";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpGroupBuyingInfoImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        groupBuyingInfoNo = importVO.getNo();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + groupBuyingInfoNo + ")";
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
            throw exception(GROUP_BUYING_INFO_DICT_DATA_INVALID, rowNum, fieldName, value);
        }
    }
}
