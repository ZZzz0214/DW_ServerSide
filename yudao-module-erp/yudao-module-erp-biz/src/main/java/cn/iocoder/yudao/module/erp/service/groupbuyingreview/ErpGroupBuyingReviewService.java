package cn.iocoder.yudao.module.erp.service.groupbuyingreview;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingReviewDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpGroupBuyingReviewService {

    Long createGroupBuyingReview(@Valid ErpGroupBuyingReviewSaveReqVO createReqVO);

    void updateGroupBuyingReview(@Valid ErpGroupBuyingReviewSaveReqVO updateReqVO);

    void deleteGroupBuyingReview(List<Long> ids);

    ErpGroupBuyingReviewDO getGroupBuyingReview(Long id);

    ErpGroupBuyingReviewDO validateGroupBuyingReview(Long id);

    List<ErpGroupBuyingReviewDO> getGroupBuyingReviewList(Collection<Long> ids);

    Map<Long, ErpGroupBuyingReviewDO> getGroupBuyingReviewMap(Collection<Long> ids);

    List<ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOList(Collection<Long> ids);

    Map<Long, ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOMap(Collection<Long> ids);

    PageResult<ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOPage(ErpGroupBuyingReviewPageReqVO pageReqVO);
}
