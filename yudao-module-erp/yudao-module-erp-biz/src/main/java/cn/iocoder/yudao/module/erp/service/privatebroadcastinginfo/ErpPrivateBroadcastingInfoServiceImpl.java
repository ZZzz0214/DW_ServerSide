package cn.iocoder.yudao.module.erp.service.privatebroadcastinginfo;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastinginfo.ErpPrivateBroadcastingInfoDO;
import cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcastinginfo.ErpPrivateBroadcastingInfoMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
}