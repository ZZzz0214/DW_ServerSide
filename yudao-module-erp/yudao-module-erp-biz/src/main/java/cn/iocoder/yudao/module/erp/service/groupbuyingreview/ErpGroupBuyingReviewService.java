package cn.iocoder.yudao.module.erp.service.groupbuyingreview;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingReviewDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpGroupBuyingReviewService {

    Long createGroupBuyingReview(@Valid ErpGroupBuyingReviewSaveReqVO createReqVO, String currentUsername);

    void updateGroupBuyingReview(@Valid ErpGroupBuyingReviewSaveReqVO updateReqVO, String currentUsername);

    void deleteGroupBuyingReview(List<Long> ids, String currentUsername);

    ErpGroupBuyingReviewDO getGroupBuyingReview(Long id, String currentUsername);

    ErpGroupBuyingReviewDO validateGroupBuyingReview(Long id, String currentUsername);

    List<ErpGroupBuyingReviewDO> getGroupBuyingReviewList(Collection<Long> ids, String currentUsername);

    Map<Long, ErpGroupBuyingReviewDO> getGroupBuyingReviewMap(Collection<Long> ids, String currentUsername);

    List<ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOList(Collection<Long> ids, String currentUsername);

    Map<Long, ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOMap(Collection<Long> ids, String currentUsername);

    PageResult<ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOPage(ErpGroupBuyingReviewPageReqVO pageReqVO, String currentUsername);

    /**
     * 导入团购复盘
     *
     * @param importList 导入数据列表
     * @param isUpdateSupport 是否支持更新
     * @param currentUsername 当前用户名
     * @return 导入结果
     */
    ErpGroupBuyingReviewImportRespVO importGroupBuyingReviewList(List<ErpGroupBuyingReviewImportExcelVO> importList, boolean isUpdateSupport, String currentUsername);
}
