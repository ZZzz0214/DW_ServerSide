package cn.iocoder.yudao.module.erp.service.livebroadcastingreview;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewImportRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastingreview.ErpLiveBroadcastingReviewDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpLiveBroadcastingReviewService {

    Long createLiveBroadcastingReview(@Valid ErpLiveBroadcastingReviewSaveReqVO createReqVO);

    void updateLiveBroadcastingReview(@Valid ErpLiveBroadcastingReviewSaveReqVO updateReqVO);

    void deleteLiveBroadcastingReview(List<Long> ids);

    ErpLiveBroadcastingReviewDO getLiveBroadcastingReview(Long id);

    ErpLiveBroadcastingReviewDO validateLiveBroadcastingReview(Long id);

    List<ErpLiveBroadcastingReviewDO> getLiveBroadcastingReviewList(Collection<Long> ids);

    Map<Long, ErpLiveBroadcastingReviewDO> getLiveBroadcastingReviewMap(Collection<Long> ids);

    List<ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReviewVOList(Collection<Long> ids);

    Map<Long, ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReviewVOMap(Collection<Long> ids);

    PageResult<ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReviewVOPage(ErpLiveBroadcastingReviewPageReqVO pageReqVO);

    /**
     * 导入直播复盘列表
     *
     * @param importList 导入列表
     * @param isUpdateSupport 是否支持更新
     * @return 导入结果
     */
    ErpLiveBroadcastingReviewImportRespVO importLiveBroadcastingReviewList(List<ErpLiveBroadcastingReviewImportExcelVO> importList, boolean isUpdateSupport);
}