package cn.iocoder.yudao.module.erp.service.privatebroadcastingreview;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastingreview.ErpPrivateBroadcastingReviewDO;
import cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcastingreview.ErpPrivateBroadcastingReviewMapper;
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
public class ErpPrivateBroadcastingReviewServiceImpl implements ErpPrivateBroadcastingReviewService {

    @Resource
    private ErpPrivateBroadcastingReviewMapper privateBroadcastingReviewMapper;
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPrivateBroadcastingReview(ErpPrivateBroadcastingReviewSaveReqVO createReqVO) {
        // 1. 校验数据
        validatePrivateBroadcastingReviewForCreateOrUpdate(null, createReqVO);

        // 2. 生成私播复盘编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.PRIVATE_BROADCASTING_REVIEW_NO_PREFIX);
        if (privateBroadcastingReviewMapper.selectByNo(no) != null) {
            throw exception(PRIVATE_BROADCASTING_REVIEW_NO_EXISTS);
        }

        // 3. 插入私播复盘记录
        ErpPrivateBroadcastingReviewDO privateBroadcastingReview = BeanUtils.toBean(createReqVO, ErpPrivateBroadcastingReviewDO.class)
                .setNo(no);
        privateBroadcastingReviewMapper.insert(privateBroadcastingReview);

        return privateBroadcastingReview.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivateBroadcastingReview(ErpPrivateBroadcastingReviewSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validatePrivateBroadcastingReview(updateReqVO.getId());
        // 1.2 校验数据
        validatePrivateBroadcastingReviewForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新私播复盘记录
        ErpPrivateBroadcastingReviewDO updateObj = BeanUtils.toBean(updateReqVO, ErpPrivateBroadcastingReviewDO.class);
        privateBroadcastingReviewMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePrivateBroadcastingReview(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpPrivateBroadcastingReviewDO> privateBroadcastingReviews = privateBroadcastingReviewMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(privateBroadcastingReviews)) {
            throw exception(PRIVATE_BROADCASTING_REVIEW_NOT_EXISTS);
        }
        // 2. 删除私播复盘记录
        privateBroadcastingReviewMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpPrivateBroadcastingReviewDO getPrivateBroadcastingReview(Long id) {
        return privateBroadcastingReviewMapper.selectById(id);
    }

    @Override
    public ErpPrivateBroadcastingReviewDO validatePrivateBroadcastingReview(Long id) {
        ErpPrivateBroadcastingReviewDO privateBroadcastingReview = privateBroadcastingReviewMapper.selectById(id);
        if (privateBroadcastingReview == null) {
            throw exception(PRIVATE_BROADCASTING_REVIEW_NOT_EXISTS);
        }
        return privateBroadcastingReview;
    }

    @Override
    public PageResult<ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOPage(ErpPrivateBroadcastingReviewPageReqVO pageReqVO) {
        return privateBroadcastingReviewMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpPrivateBroadcastingReviewDO> list = privateBroadcastingReviewMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpPrivateBroadcastingReviewRespVO.class);
    }

    @Override
    public Map<Long, ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPrivateBroadcastingReviewVOList(ids), ErpPrivateBroadcastingReviewRespVO::getId);
    }

    @Override
    public List<ErpPrivateBroadcastingReviewDO> getPrivateBroadcastingReviewList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return privateBroadcastingReviewMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpPrivateBroadcastingReviewDO> getPrivateBroadcastingReviewMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPrivateBroadcastingReviewList(ids), ErpPrivateBroadcastingReviewDO::getId);
    }

    private void validatePrivateBroadcastingReviewForCreateOrUpdate(Long id, ErpPrivateBroadcastingReviewSaveReqVO reqVO) {
        // 1. 校验私播复盘编号唯一
        ErpPrivateBroadcastingReviewDO privateBroadcastingReview = privateBroadcastingReviewMapper.selectByNo(reqVO.getNo());
        if (privateBroadcastingReview != null && !privateBroadcastingReview.getId().equals(id)) {
            throw exception(PRIVATE_BROADCASTING_REVIEW_NO_EXISTS);
        }
    }
}