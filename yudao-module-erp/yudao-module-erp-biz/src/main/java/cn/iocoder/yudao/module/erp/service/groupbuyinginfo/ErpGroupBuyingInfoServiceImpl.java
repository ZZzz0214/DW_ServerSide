package cn.iocoder.yudao.module.erp.service.groupbuyinginfo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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

        // 批量处理
        List<ErpGroupBuyingInfoDO> createList = new ArrayList<>();
        List<ErpGroupBuyingInfoDO> updateList = new ArrayList<>();

        try {
            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpGroupBuyingInfoImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpGroupBuyingInfoDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(groupBuyingInfoMapper.selectListByNoIn(noSet), ErpGroupBuyingInfoDO::getNo);

            // 批量查询客户信息
            Set<String> customerNames = importList.stream()
                    .map(ErpGroupBuyingInfoImportExcelVO::getCustomerName)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpCustomerDO> customerMap = customerNames.isEmpty() ? Collections.emptyMap() :
                    convertMap(customerMapper.selectListByNameIn(customerNames), ErpCustomerDO::getName);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();
            
            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpGroupBuyingInfoImportExcelVO importVO = importList.get(i);
                try {
                    // 校验数据有效性
                    validateImportData(importVO, i + 1, customerMap);

                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(GROUP_BUYING_INFO_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 判断是否支持更新
                    ErpGroupBuyingInfoDO existGroupBuyingInfo = existMap.get(importVO.getNo());
                    if (existGroupBuyingInfo == null) {
                       // 创建 - 自动生成新的no编号
                       ErpGroupBuyingInfoDO groupBuyingInfo = BeanUtils.toBean(importVO, ErpGroupBuyingInfoDO.class);
                       groupBuyingInfo.setNo(noRedisDAO.generate(ErpNoRedisDAO.GROUP_BUYING_INFO_NO_PREFIX));
                        createList.add(groupBuyingInfo);
                        respVO.getCreateNames().add(groupBuyingInfo.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpGroupBuyingInfoDO updateGroupBuyingInfo = BeanUtils.toBean(importVO, ErpGroupBuyingInfoDO.class);
                        updateGroupBuyingInfo.setId(existGroupBuyingInfo.getId());
                        updateList.add(updateGroupBuyingInfo);
                        respVO.getUpdateNames().add(updateGroupBuyingInfo.getNo());
                    } else {
                        throw exception(GROUP_BUYING_INFO_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
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
                groupBuyingInfoMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(groupBuyingInfoMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }

    /**
     * 校验导入数据的有效性
     */
    private void validateImportData(ErpGroupBuyingInfoImportExcelVO importVO, int rowNum, Map<String, ErpCustomerDO> customerMap) {
        // 1. 校验客户名称是否存在
        if (StrUtil.isNotBlank(importVO.getCustomerName())) {
            ErpCustomerDO customer = customerMap.get(importVO.getCustomerName());
            if (customer == null) {
                throw exception(GROUP_BUYING_INFO_CUSTOMER_NOT_EXISTS, rowNum, importVO.getCustomerName());
            }
        }

        // 2. 校验字典数据有效性
        validateDictData(importVO.getCustomerPosition(), DictTypeConstants.ERP_CUSTOMER_POSITION, "客户职位", rowNum);
        validateDictData(importVO.getPlatformName(), DictTypeConstants.ERP_PLATFORM_NAME, "平台名称", rowNum);
        validateDictData(importVO.getCustomerAttribute(), DictTypeConstants.ERP_CUSTOMER_ATTRIBUTE, "客户属性", rowNum);
        validateDictData(importVO.getCustomerCity(), DictTypeConstants.ERP_CUSTOMER_CITY, "客户城市", rowNum);
        validateDictData(importVO.getCustomerDistrict(), DictTypeConstants.ERP_CUSTOMER_DISTRICT, "客户区县", rowNum);
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