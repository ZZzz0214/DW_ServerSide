package cn.iocoder.yudao.module.erp.service.livebroadcastingreview;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastingreview.ErpLiveBroadcastingReviewDO;
import cn.iocoder.yudao.module.erp.dal.mysql.livebroadcastingreview.ErpLiveBroadcastingReviewMapper;
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
public class ErpLiveBroadcastingReviewServiceImpl implements ErpLiveBroadcastingReviewService {

    @Resource
    private ErpLiveBroadcastingReviewMapper liveBroadcastingReviewMapper;
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLiveBroadcastingReview(ErpLiveBroadcastingReviewSaveReqVO createReqVO) {
        // 1. 校验数据
        validateLiveBroadcastingReviewForCreateOrUpdate(null, createReqVO);

        // 2. 生成直播复盘编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.LIVE_BROADCASTING_REVIEW_NO_PREFIX);
        if (liveBroadcastingReviewMapper.selectByNo(no) != null) {
            throw exception(LIVE_BROADCASTING_REVIEW_NO_EXISTS);
        }

        // 3. 插入直播复盘记录
        ErpLiveBroadcastingReviewDO liveBroadcastingReview = BeanUtils.toBean(createReqVO, ErpLiveBroadcastingReviewDO.class)
                .setNo(no);
        liveBroadcastingReviewMapper.insert(liveBroadcastingReview);

        return liveBroadcastingReview.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLiveBroadcastingReview(ErpLiveBroadcastingReviewSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateLiveBroadcastingReview(updateReqVO.getId());
        // 1.2 校验数据
        validateLiveBroadcastingReviewForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新直播复盘记录
        ErpLiveBroadcastingReviewDO updateObj = BeanUtils.toBean(updateReqVO, ErpLiveBroadcastingReviewDO.class);
        liveBroadcastingReviewMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLiveBroadcastingReview(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpLiveBroadcastingReviewDO> liveBroadcastingReviews = liveBroadcastingReviewMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(liveBroadcastingReviews)) {
            throw exception(LIVE_BROADCASTING_REVIEW_NOT_EXISTS);
        }
        // 2. 删除直播复盘记录
        liveBroadcastingReviewMapper.deleteBatchIds(ids);
    }

    @Override
    public PageResult<ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReviewVOPage(ErpLiveBroadcastingReviewPageReqVO pageReqVO) {
        return liveBroadcastingReviewMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReviewVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpLiveBroadcastingReviewDO> list = liveBroadcastingReviewMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpLiveBroadcastingReviewRespVO.class);
    }

    @Override
    public Map<Long, ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReviewVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingReviewVOList(ids), ErpLiveBroadcastingReviewRespVO::getId);
    }

    @Override
    public ErpLiveBroadcastingReviewDO getLiveBroadcastingReview(Long id) {
        return liveBroadcastingReviewMapper.selectById(id);
    }

    @Override
    public ErpLiveBroadcastingReviewDO validateLiveBroadcastingReview(Long id) {
        ErpLiveBroadcastingReviewDO liveBroadcastingReview = liveBroadcastingReviewMapper.selectById(id);
        if (liveBroadcastingReview == null) {
            throw exception(LIVE_BROADCASTING_REVIEW_NOT_EXISTS);
        }
        return liveBroadcastingReview;
    }

    @Override
    public List<ErpLiveBroadcastingReviewDO> getLiveBroadcastingReviewList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return liveBroadcastingReviewMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpLiveBroadcastingReviewDO> getLiveBroadcastingReviewMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingReviewList(ids), ErpLiveBroadcastingReviewDO::getId);
    }

    private void validateLiveBroadcastingReviewForCreateOrUpdate(Long id, ErpLiveBroadcastingReviewSaveReqVO reqVO) {
        // 1. 校验直播复盘编号唯一
        ErpLiveBroadcastingReviewDO liveBroadcastingReview = liveBroadcastingReviewMapper.selectByNo(reqVO.getNo());
        if (liveBroadcastingReview != null && !liveBroadcastingReview.getId().equals(id)) {
            throw exception(LIVE_BROADCASTING_REVIEW_NO_EXISTS);
        }
    }
}
