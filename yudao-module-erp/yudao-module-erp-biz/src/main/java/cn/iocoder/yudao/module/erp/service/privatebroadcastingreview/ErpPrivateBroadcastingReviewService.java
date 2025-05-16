package cn.iocoder.yudao.module.erp.service.privatebroadcastingreview;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastingreview.ErpPrivateBroadcastingReviewDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpPrivateBroadcastingReviewService {

    Long createPrivateBroadcastingReview(@Valid ErpPrivateBroadcastingReviewSaveReqVO createReqVO);

    void updatePrivateBroadcastingReview(@Valid ErpPrivateBroadcastingReviewSaveReqVO updateReqVO);

    void deletePrivateBroadcastingReview(List<Long> ids);

    ErpPrivateBroadcastingReviewDO getPrivateBroadcastingReview(Long id);

    ErpPrivateBroadcastingReviewDO validatePrivateBroadcastingReview(Long id);

    List<ErpPrivateBroadcastingReviewDO> getPrivateBroadcastingReviewList(Collection<Long> ids);

    Map<Long, ErpPrivateBroadcastingReviewDO> getPrivateBroadcastingReviewMap(Collection<Long> ids);

    List<ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOList(Collection<Long> ids);

    Map<Long, ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOMap(Collection<Long> ids);

    PageResult<ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOPage(ErpPrivateBroadcastingReviewPageReqVO pageReqVO);
}
