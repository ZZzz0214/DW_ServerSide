package cn.iocoder.yudao.module.erp.service.privatebroadcastinginfo;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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

        // 批量处理数据
        List<ErpPrivateBroadcastingInfoDO> createList = new ArrayList<>();
        List<ErpPrivateBroadcastingInfoDO> updateList = new ArrayList<>();

        try {
            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpPrivateBroadcastingInfoImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpPrivateBroadcastingInfoDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(privateBroadcastingInfoMapper.selectListByNoIn(noSet), ErpPrivateBroadcastingInfoDO::getNo);

            // 批量查询客户信息
            Set<String> customerNames = importList.stream()
                    .map(ErpPrivateBroadcastingInfoImportExcelVO::getCustomerName)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpCustomerDO> customerMap = customerNames.isEmpty() ? Collections.emptyMap() :
                    convertMap(customerMapper.selectListByNameIn(customerNames), ErpCustomerDO::getName);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();
            
            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpPrivateBroadcastingInfoImportExcelVO importVO = importList.get(i);
                try {
                    // 校验数据有效性
                    validateImportData(importVO, i + 1, customerMap);

                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(PRIVATE_BROADCASTING_INFO_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 判断是否支持更新
                    ErpPrivateBroadcastingInfoDO existPrivateBroadcastingInfo = existMap.get(importVO.getNo());
                    if (existPrivateBroadcastingInfo == null) {
                        // 创建 - 自动生成新的no编号
                        ErpPrivateBroadcastingInfoDO privateBroadcastingInfo = BeanUtils.toBean(importVO, ErpPrivateBroadcastingInfoDO.class);
                        privateBroadcastingInfo.setNo(noRedisDAO.generate(ErpNoRedisDAO.PRIVATE_BROADCASTING_INFO_NO_PREFIX));
                        createList.add(privateBroadcastingInfo);
                        respVO.getCreateNames().add(privateBroadcastingInfo.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpPrivateBroadcastingInfoDO updatePrivateBroadcastingInfo = BeanUtils.toBean(importVO, ErpPrivateBroadcastingInfoDO.class);
                        updatePrivateBroadcastingInfo.setId(existPrivateBroadcastingInfo.getId());
                        updateList.add(updatePrivateBroadcastingInfo);
                        respVO.getUpdateNames().add(updatePrivateBroadcastingInfo.getNo());
                    } else {
                        throw exception(PRIVATE_BROADCASTING_INFO_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
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
                privateBroadcastingInfoMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(privateBroadcastingInfoMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }

    /**
     * 校验导入数据的有效性
     */
    private void validateImportData(ErpPrivateBroadcastingInfoImportExcelVO importVO, int rowNum, Map<String, ErpCustomerDO> customerMap) {
        // 1. 校验客户名称是否存在
        if (StrUtil.isNotBlank(importVO.getCustomerName())) {
            ErpCustomerDO customer = customerMap.get(importVO.getCustomerName());
            if (customer == null) {
                throw exception(PRIVATE_BROADCASTING_INFO_CUSTOMER_NOT_EXISTS, rowNum, importVO.getCustomerName());
            }
        }

        // 2. 校验字典数据有效性
        validateDictData(importVO.getCustomerPosition(), DictTypeConstants.ERP_PRIVATE_CUSTOMER_POSITION, "客户职位", rowNum);
        validateDictData(importVO.getPlatformName(), DictTypeConstants.ERP_PRIVATE_PLATFORM_NAME, "平台名称", rowNum);
        validateDictData(importVO.getCustomerAttribute(), DictTypeConstants.ERP_PRIVATE_CUSTOMER_ATTRIBUTE, "客户属性", rowNum);
        validateDictData(importVO.getCustomerCity(), DictTypeConstants.ERP_PRIVATE_CUSTOMER_CITY, "客户城市", rowNum);
        validateDictData(importVO.getCustomerDistrict(), DictTypeConstants.ERP_PRIVATE_CUSTOMER_DISTRICT, "客户区县", rowNum);
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