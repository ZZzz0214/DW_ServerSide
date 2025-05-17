package cn.iocoder.yudao.module.erp.dal.mysql.livebroadcastingreview;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastingreview.ErpLiveBroadcastingReviewDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpLiveBroadcastingReviewMapper extends BaseMapperX<ErpLiveBroadcastingReviewDO> {

    default PageResult<ErpLiveBroadcastingReviewRespVO> selectPage(ErpLiveBroadcastingReviewPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpLiveBroadcastingReviewDO> query = new MPJLambdaWrapperX<ErpLiveBroadcastingReviewDO>()
                .likeIfPresent(ErpLiveBroadcastingReviewDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpLiveBroadcastingReviewDO::getLiveBroadcastingId, reqVO.getLiveBroadcastingId())
                .eqIfPresent(ErpLiveBroadcastingReviewDO::getCustomerId, reqVO.getCustomerId())
                .likeIfPresent(ErpLiveBroadcastingReviewDO::getLivePlatform, reqVO.getLivePlatform())
                .betweenIfPresent(ErpLiveBroadcastingReviewDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpLiveBroadcastingReviewDO::getId)
                // 直播复盘表字段映射
                .selectAs(ErpLiveBroadcastingReviewDO::getId, ErpLiveBroadcastingReviewRespVO::getId)
                .selectAs(ErpLiveBroadcastingReviewDO::getNo, ErpLiveBroadcastingReviewRespVO::getNo)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveBroadcastingId, ErpLiveBroadcastingReviewRespVO::getLiveBroadcastingId)
                .selectAs(ErpLiveBroadcastingReviewDO::getRemark, ErpLiveBroadcastingReviewRespVO::getRemark)
                .selectAs(ErpLiveBroadcastingReviewDO::getCustomerId, ErpLiveBroadcastingReviewRespVO::getCustomerId)
                .selectAs(ErpLiveBroadcastingReviewDO::getLivePlatform, ErpLiveBroadcastingReviewRespVO::getLivePlatform)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveCommission, ErpLiveBroadcastingReviewRespVO::getLiveCommission)
                .selectAs(ErpLiveBroadcastingReviewDO::getPublicCommission, ErpLiveBroadcastingReviewRespVO::getPublicCommission)
                .selectAs(ErpLiveBroadcastingReviewDO::getRebateCommission, ErpLiveBroadcastingReviewRespVO::getRebateCommission)
                .selectAs(ErpLiveBroadcastingReviewDO::getSampleSendDate, ErpLiveBroadcastingReviewRespVO::getSampleSendDate)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveStartDate, ErpLiveBroadcastingReviewRespVO::getLiveStartDate)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveSales, ErpLiveBroadcastingReviewRespVO::getLiveSales)
                .selectAs(ErpLiveBroadcastingReviewDO::getRepeatLiveDate, ErpLiveBroadcastingReviewRespVO::getRepeatLiveDate)
                .selectAs(ErpLiveBroadcastingReviewDO::getRepeatLiveSales, ErpLiveBroadcastingReviewRespVO::getRepeatLiveSales)
                .selectAs(ErpLiveBroadcastingReviewDO::getCreateTime, ErpLiveBroadcastingReviewRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpLiveBroadcastingReviewRespVO.class, query);
    }

    default ErpLiveBroadcastingReviewDO selectByNo(String no) {
        return selectOne(ErpLiveBroadcastingReviewDO::getNo, no);
    }
}