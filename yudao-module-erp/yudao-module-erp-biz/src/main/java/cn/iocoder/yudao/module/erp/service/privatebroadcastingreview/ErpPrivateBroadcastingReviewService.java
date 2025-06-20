package cn.iocoder.yudao.module.erp.service.privatebroadcastingreview;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastingreview.ErpPrivateBroadcastingReviewDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpPrivateBroadcastingReviewService {

    Long createPrivateBroadcastingReview(@Valid ErpPrivateBroadcastingReviewSaveReqVO createReqVO, String currentUsername);

    void updatePrivateBroadcastingReview(@Valid ErpPrivateBroadcastingReviewSaveReqVO updateReqVO, String currentUsername);

    void deletePrivateBroadcastingReview(List<Long> ids, String currentUsername);

    ErpPrivateBroadcastingReviewDO getPrivateBroadcastingReview(Long id, String currentUsername);

    ErpPrivateBroadcastingReviewDO validatePrivateBroadcastingReview(Long id, String currentUsername);

    List<ErpPrivateBroadcastingReviewDO> getPrivateBroadcastingReviewList(Collection<Long> ids, String currentUsername);

    Map<Long, ErpPrivateBroadcastingReviewDO> getPrivateBroadcastingReviewMap(Collection<Long> ids, String currentUsername);

    List<ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOList(Collection<Long> ids, String currentUsername);

    Map<Long, ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOMap(Collection<Long> ids, String currentUsername);

    PageResult<ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOPage(ErpPrivateBroadcastingReviewPageReqVO pageReqVO, String currentUsername);

    /**
     * 导入私播复盘
     *
     * @param importList 导入数据列表
     * @param isUpdateSupport 是否支持更新
     * @param currentUsername 当前用户名
     * @return 导入结果
     */
    ErpPrivateBroadcastingReviewImportRespVO importPrivateBroadcastingReviewList(List<ErpPrivateBroadcastingReviewImportExcelVO> importList, boolean isUpdateSupport, String currentUsername);
}
