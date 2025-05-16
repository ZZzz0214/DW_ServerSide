package cn.iocoder.yudao.module.erp.service.groupbuyingreview;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingReviewDO;
import cn.iocoder.yudao.module.erp.dal.mysql.groupbuyingreview.ErpGroupBuyingReviewMapper;
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
public class ErpGroupBuyingReviewServiceImpl implements ErpGroupBuyingReviewService {

    @Resource
    private ErpGroupBuyingReviewMapper groupBuyingReviewMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroupBuyingReview(ErpGroupBuyingReviewSaveReqVO createReqVO) {
        // 1. 校验数据
        validateGroupBuyingReviewForCreateOrUpdate(null, createReqVO);

        // 2. 生成团购复盘编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.GROUP_BUYING_REVIEW_NO_PREFIX);
        if (groupBuyingReviewMapper.selectByNo(no) != null) {
            throw exception(GROUP_BUYING_REVIEW_NO_EXISTS);
        }

        // 3. 插入团购复盘记录
        ErpGroupBuyingReviewDO groupBuyingReview = BeanUtils.toBean(createReqVO, ErpGroupBuyingReviewDO.class)
                .setNo(no);
        groupBuyingReviewMapper.insert(groupBuyingReview);

        return groupBuyingReview.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupBuyingReview(ErpGroupBuyingReviewSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateGroupBuyingReview(updateReqVO.getId());
        // 1.2 校验数据
        validateGroupBuyingReviewForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新团购复盘记录
        ErpGroupBuyingReviewDO updateObj = BeanUtils.toBean(updateReqVO, ErpGroupBuyingReviewDO.class);
        groupBuyingReviewMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroupBuyingReview(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpGroupBuyingReviewDO> groupBuyingReviews = groupBuyingReviewMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(groupBuyingReviews)) {
            throw exception(GROUP_BUYING_REVIEW_NOT_EXISTS);
        }
        // 2. 删除团购复盘记录
        groupBuyingReviewMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpGroupBuyingReviewDO getGroupBuyingReview(Long id) {
        return groupBuyingReviewMapper.selectById(id);
    }

    @Override
    public ErpGroupBuyingReviewDO validateGroupBuyingReview(Long id) {
        ErpGroupBuyingReviewDO groupBuyingReview = groupBuyingReviewMapper.selectById(id);
        if (groupBuyingReview == null) {
            throw exception(GROUP_BUYING_REVIEW_NOT_EXISTS);
        }
        return groupBuyingReview;
    }

    @Override
    public PageResult<ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOPage(ErpGroupBuyingReviewPageReqVO pageReqVO) {
        return groupBuyingReviewMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpGroupBuyingReviewDO> list = groupBuyingReviewMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpGroupBuyingReviewRespVO.class);
    }

    @Override
    public Map<Long, ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingReviewVOList(ids), ErpGroupBuyingReviewRespVO::getId);
    }

    @Override
    public List<ErpGroupBuyingReviewDO> getGroupBuyingReviewList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return groupBuyingReviewMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpGroupBuyingReviewDO> getGroupBuyingReviewMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingReviewList(ids), ErpGroupBuyingReviewDO::getId);
    }

    private void validateGroupBuyingReviewForCreateOrUpdate(Long id, ErpGroupBuyingReviewSaveReqVO reqVO) {
        // 1. 校验团购复盘编号唯一
        ErpGroupBuyingReviewDO groupBuyingReview = groupBuyingReviewMapper.selectByNo(reqVO.getNo());
        if (groupBuyingReview != null && !groupBuyingReview.getId().equals(id)) {
            throw exception(GROUP_BUYING_REVIEW_NO_EXISTS);
        }
    }
}
