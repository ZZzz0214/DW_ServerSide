package cn.iocoder.yudao.module.erp.service.livebroadcastinginfo;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastinginfo.ErpLiveBroadcastingInfoDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.livebroadcastinginfo.ErpLiveBroadcastingInfoMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpCustomerMapper;
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
public class ErpLiveBroadcastingInfoServiceImpl implements ErpLiveBroadcastingInfoService {

    @Resource
    private ErpLiveBroadcastingInfoMapper liveBroadcastingInfoMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpCustomerMapper customerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLiveBroadcastingInfo(ErpLiveBroadcastingInfoSaveReqVO createReqVO) {
        // 1. 校验数据
        validateLiveBroadcastingInfoForCreateOrUpdate(null, createReqVO);

        // 2. 生成直播信息编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.LIVE_BROADCASTING_INFO_NO_PREFIX);
        if (liveBroadcastingInfoMapper.selectByNo(no) != null) {
            throw exception(LIVE_BROADCASTING_INFO_NO_EXISTS);
        }

        // 3. 插入直播信息记录
        ErpLiveBroadcastingInfoDO liveBroadcastingInfo = BeanUtils.toBean(createReqVO, ErpLiveBroadcastingInfoDO.class)
                .setNo(no);
        liveBroadcastingInfoMapper.insert(liveBroadcastingInfo);

        return liveBroadcastingInfo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLiveBroadcastingInfo(ErpLiveBroadcastingInfoSaveReqVO updateReqVO) {
        System.out.println("更新的id"+updateReqVO.getId());
        // 1.1 校验存在
        validateLiveBroadcastingInfo(updateReqVO.getId());
        // 1.2 校验数据
        validateLiveBroadcastingInfoForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新直播信息记录
        ErpLiveBroadcastingInfoDO updateObj = BeanUtils.toBean(updateReqVO, ErpLiveBroadcastingInfoDO.class);
        liveBroadcastingInfoMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLiveBroadcastingInfo(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpLiveBroadcastingInfoDO> liveBroadcastingInfos = liveBroadcastingInfoMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(liveBroadcastingInfos)) {
            throw exception(LIVE_BROADCASTING_INFO_NOT_EXISTS);
        }
        // 2. 删除直播信息记录
        liveBroadcastingInfoMapper.deleteBatchIds(ids);
    }

    @Override
    public PageResult<ErpLiveBroadcastingInfoRespVO> getLiveBroadcastingInfoVOPage(ErpLiveBroadcastingInfoPageReqVO pageReqVO) {
        return liveBroadcastingInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpLiveBroadcastingInfoRespVO> getLiveBroadcastingInfoVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return liveBroadcastingInfoMapper.selectListByIds(ids);
    }

    @Override
    public Map<Long, ErpLiveBroadcastingInfoRespVO> getLiveBroadcastingInfoVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingInfoVOList(ids), ErpLiveBroadcastingInfoRespVO::getId);
    }

    @Override
    public ErpLiveBroadcastingInfoDO getLiveBroadcastingInfo(Long id) {
        return liveBroadcastingInfoMapper.selectById(id);
    }

    @Override
    public ErpLiveBroadcastingInfoDO validateLiveBroadcastingInfo(Long id) {
        ErpLiveBroadcastingInfoDO liveBroadcastingInfo = liveBroadcastingInfoMapper.selectById(id);
        if (liveBroadcastingInfo == null) {
            throw exception(LIVE_BROADCASTING_INFO_NOT_EXISTS);
        }
        return liveBroadcastingInfo;
    }

    @Override
    public List<ErpLiveBroadcastingInfoDO> getLiveBroadcastingInfoList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return liveBroadcastingInfoMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpLiveBroadcastingInfoDO> getLiveBroadcastingInfoMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingInfoList(ids), ErpLiveBroadcastingInfoDO::getId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpLiveBroadcastingInfoImportRespVO importLiveBroadcastingInfoList(List<ErpLiveBroadcastingInfoImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(LIVE_BROADCASTING_INFO_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpLiveBroadcastingInfoImportRespVO respVO = ErpLiveBroadcastingInfoImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理数据
        List<ErpLiveBroadcastingInfoDO> createList = new ArrayList<>();
        List<ErpLiveBroadcastingInfoDO> updateList = new ArrayList<>();

        try {
            // 批量查询客户信息 - 通过客户名称查询
            Set<String> customerNames = importList.stream()
                    .map(ErpLiveBroadcastingInfoImportExcelVO::getCustomerName)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpCustomerDO> customerMap = customerNames.isEmpty() ? Collections.emptyMap() :
                    convertMap(customerMapper.selectListByNameIn(customerNames), ErpCustomerDO::getName);

            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpLiveBroadcastingInfoImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpLiveBroadcastingInfoDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(liveBroadcastingInfoMapper.selectListByNoIn(noSet), ErpLiveBroadcastingInfoDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();

            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpLiveBroadcastingInfoImportExcelVO importVO = importList.get(i);
                try {
                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(LIVE_BROADCASTING_INFO_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 校验客户名称是否存在，并获取客户ID
                    Long customerId = null;
                    if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                        ErpCustomerDO customer = customerMap.get(importVO.getCustomerName());
                        if (customer == null) {
                            throw exception(LIVE_BROADCASTING_INFO_CUSTOMER_NOT_EXISTS, i + 1, importVO.getCustomerName());
                        }
                        customerId = customer.getId();
                    }

                    // 判断是否支持更新
                    ErpLiveBroadcastingInfoDO existLiveBroadcastingInfo = existMap.get(importVO.getNo());
                    if (existLiveBroadcastingInfo == null) {
                       // 创建 - 自动生成新的no编号
                       ErpLiveBroadcastingInfoDO liveBroadcastingInfo = BeanUtils.toBean(importVO, ErpLiveBroadcastingInfoDO.class);
                       liveBroadcastingInfo.setNo(noRedisDAO.generate(ErpNoRedisDAO.LIVE_BROADCASTING_INFO_NO_PREFIX));
                       liveBroadcastingInfo.setCustomerId(customerId); // 设置客户ID
                        createList.add(liveBroadcastingInfo);
                        respVO.getCreateNames().add(liveBroadcastingInfo.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpLiveBroadcastingInfoDO updateLiveBroadcastingInfo = BeanUtils.toBean(importVO, ErpLiveBroadcastingInfoDO.class);
                        updateLiveBroadcastingInfo.setId(existLiveBroadcastingInfo.getId());
                        updateLiveBroadcastingInfo.setCustomerId(customerId); // 设置客户ID
                        updateList.add(updateLiveBroadcastingInfo);
                        respVO.getUpdateNames().add(updateLiveBroadcastingInfo.getNo());
                    } else {
                        throw exception(LIVE_BROADCASTING_INFO_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
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
                createList.forEach(liveBroadcastingInfoMapper::insert);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(liveBroadcastingInfoMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }

    private void validateLiveBroadcastingInfoForCreateOrUpdate(Long id, ErpLiveBroadcastingInfoSaveReqVO reqVO) {
        // 1. 校验直播信息编号唯一
        ErpLiveBroadcastingInfoDO liveBroadcastingInfo = liveBroadcastingInfoMapper.selectByNo(reqVO.getNo());
        if (liveBroadcastingInfo != null && !liveBroadcastingInfo.getId().equals(id)) {
            throw exception(LIVE_BROADCASTING_INFO_NO_EXISTS);
        }
    }
}
