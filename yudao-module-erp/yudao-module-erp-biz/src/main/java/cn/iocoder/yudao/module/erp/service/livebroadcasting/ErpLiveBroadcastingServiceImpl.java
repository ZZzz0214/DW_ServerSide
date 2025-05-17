package cn.iocoder.yudao.module.erp.service.livebroadcasting;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    }
}