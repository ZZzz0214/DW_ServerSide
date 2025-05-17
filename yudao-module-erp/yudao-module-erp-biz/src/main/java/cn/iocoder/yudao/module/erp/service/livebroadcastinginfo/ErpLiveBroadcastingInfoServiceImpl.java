package cn.iocoder.yudao.module.erp.service.livebroadcastinginfo;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastinginfo.ErpLiveBroadcastingInfoDO;
import cn.iocoder.yudao.module.erp.dal.mysql.livebroadcastinginfo.ErpLiveBroadcastingInfoMapper;
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
public class ErpLiveBroadcastingInfoServiceImpl implements ErpLiveBroadcastingInfoService {

    @Resource
    private ErpLiveBroadcastingInfoMapper liveBroadcastingInfoMapper;
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

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
        List<ErpLiveBroadcastingInfoDO> list = liveBroadcastingInfoMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpLiveBroadcastingInfoRespVO.class);
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

    private void validateLiveBroadcastingInfoForCreateOrUpdate(Long id, ErpLiveBroadcastingInfoSaveReqVO reqVO) {
        // 1. 校验直播信息编号唯一
        ErpLiveBroadcastingInfoDO liveBroadcastingInfo = liveBroadcastingInfoMapper.selectByNo(reqVO.getNo());
        if (liveBroadcastingInfo != null && !liveBroadcastingInfo.getId().equals(id)) {
            throw exception(LIVE_BROADCASTING_INFO_NO_EXISTS);
        }
    }
}