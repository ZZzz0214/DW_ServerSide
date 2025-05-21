package cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcastingreview;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcasting.ErpPrivateBroadcastingDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastingreview.ErpPrivateBroadcastingReviewDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpPrivateBroadcastingReviewMapper extends BaseMapperX<ErpPrivateBroadcastingReviewDO> {

    default PageResult<ErpPrivateBroadcastingReviewRespVO> selectPage(ErpPrivateBroadcastingReviewPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpPrivateBroadcastingReviewDO> query = new MPJLambdaWrapperX<ErpPrivateBroadcastingReviewDO>()
                .likeIfPresent(ErpPrivateBroadcastingReviewDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpPrivateBroadcastingReviewDO::getPrivateBroadcastingId, reqVO.getPrivateBroadcastingId())
                .eqIfPresent(ErpPrivateBroadcastingReviewDO::getCustomerId, reqVO.getCustomerId())
                .betweenIfPresent(ErpPrivateBroadcastingReviewDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpPrivateBroadcastingReviewDO::getId)
                // 私播复盘表字段映射
                .selectAs(ErpPrivateBroadcastingReviewDO::getId, ErpPrivateBroadcastingReviewRespVO::getId)
                .selectAs(ErpPrivateBroadcastingReviewDO::getNo, ErpPrivateBroadcastingReviewRespVO::getNo)
                .selectAs(ErpPrivateBroadcastingReviewDO::getPrivateBroadcastingId, ErpPrivateBroadcastingReviewRespVO::getPrivateBroadcastingId)
                .selectAs(ErpPrivateBroadcastingReviewDO::getCustomerId, ErpPrivateBroadcastingReviewRespVO::getCustomerId)
                .selectAs(ErpPrivateBroadcastingReviewDO::getProductNakedPrice, ErpPrivateBroadcastingReviewRespVO::getProductNakedPrice)
                .selectAs(ErpPrivateBroadcastingReviewDO::getExpressFee, ErpPrivateBroadcastingReviewRespVO::getExpressFee)
                .selectAs(ErpPrivateBroadcastingReviewDO::getDropshipPrice, ErpPrivateBroadcastingReviewRespVO::getDropshipPrice)
                .selectAs(ErpPrivateBroadcastingReviewDO::getSampleSendDate, ErpPrivateBroadcastingReviewRespVO::getSampleSendDate)
                .selectAs(ErpPrivateBroadcastingReviewDO::getGroupStartDate, ErpPrivateBroadcastingReviewRespVO::getGroupStartDate)
                .selectAs(ErpPrivateBroadcastingReviewDO::getGroupSales, ErpPrivateBroadcastingReviewRespVO::getGroupSales)
                .selectAs(ErpPrivateBroadcastingReviewDO::getRepeatGroupDate, ErpPrivateBroadcastingReviewRespVO::getRepeatGroupDate)
                .selectAs(ErpPrivateBroadcastingReviewDO::getRepeatGroupSales, ErpPrivateBroadcastingReviewRespVO::getRepeatGroupSales)
                .selectAs(ErpPrivateBroadcastingReviewDO::getRemark, ErpPrivateBroadcastingReviewRespVO::getRemark)
                .selectAs(ErpPrivateBroadcastingReviewDO::getCreateTime, ErpPrivateBroadcastingReviewRespVO::getCreateTime);
                // 联表查询私播货盘信息
                query.leftJoin(ErpPrivateBroadcastingDO.class, ErpPrivateBroadcastingDO::getId, ErpPrivateBroadcastingReviewDO::getPrivateBroadcastingId)
                .selectAs(ErpPrivateBroadcastingDO::getProductName, ErpPrivateBroadcastingReviewRespVO::getProductName)
                .selectAs(ErpPrivateBroadcastingDO::getBrandId, ErpPrivateBroadcastingReviewRespVO::getBrandName)
                .selectAs(ErpPrivateBroadcastingDO::getProductSpec, ErpPrivateBroadcastingReviewRespVO::getProductSpec)
                .selectAs(ErpPrivateBroadcastingDO::getProductSku, ErpPrivateBroadcastingReviewRespVO::getProductSku)
                .selectAs(ErpPrivateBroadcastingDO::getLivePrice, ErpPrivateBroadcastingReviewRespVO::getLivePrice);
        return selectJoinPage(reqVO, ErpPrivateBroadcastingReviewRespVO.class, query);
    }

    default ErpPrivateBroadcastingReviewDO selectByNo(String no) {
        return selectOne(ErpPrivateBroadcastingReviewDO::getNo, no);
    }
}
