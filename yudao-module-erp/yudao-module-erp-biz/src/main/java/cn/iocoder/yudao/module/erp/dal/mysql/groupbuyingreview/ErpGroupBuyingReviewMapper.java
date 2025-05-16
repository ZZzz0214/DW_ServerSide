package cn.iocoder.yudao.module.erp.dal.mysql.groupbuyingreview;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingReviewDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpGroupBuyingReviewMapper extends BaseMapperX<ErpGroupBuyingReviewDO> {

    default PageResult<ErpGroupBuyingReviewRespVO> selectPage(ErpGroupBuyingReviewPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpGroupBuyingReviewDO> query = new MPJLambdaWrapperX<ErpGroupBuyingReviewDO>()
                .likeIfPresent(ErpGroupBuyingReviewDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpGroupBuyingReviewDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(ErpGroupBuyingReviewDO::getGroupBuyingId, reqVO.getGroupBuyingId())
                .betweenIfPresent(ErpGroupBuyingReviewDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpGroupBuyingReviewDO::getId)
                // 团购复盘表字段映射
                .selectAs(ErpGroupBuyingReviewDO::getId, ErpGroupBuyingReviewRespVO::getId)
                .selectAs(ErpGroupBuyingReviewDO::getNo, ErpGroupBuyingReviewRespVO::getNo)
                .selectAs(ErpGroupBuyingReviewDO::getRemark, ErpGroupBuyingReviewRespVO::getRemark)
                .selectAs(ErpGroupBuyingReviewDO::getCustomerId, ErpGroupBuyingReviewRespVO::getCustomerId)
                .selectAs(ErpGroupBuyingReviewDO::getGroupBuyingId, ErpGroupBuyingReviewRespVO::getGroupBuyingId)
                .selectAs(ErpGroupBuyingReviewDO::getSupplyGroupPrice, ErpGroupBuyingReviewRespVO::getSupplyGroupPrice)
                .selectAs(ErpGroupBuyingReviewDO::getSampleSendDate, ErpGroupBuyingReviewRespVO::getSampleSendDate)
                .selectAs(ErpGroupBuyingReviewDO::getGroupStartDate, ErpGroupBuyingReviewRespVO::getGroupStartDate)
                .selectAs(ErpGroupBuyingReviewDO::getGroupSales, ErpGroupBuyingReviewRespVO::getGroupSales)
                .selectAs(ErpGroupBuyingReviewDO::getRepeatGroupDate, ErpGroupBuyingReviewRespVO::getRepeatGroupDate)
                .selectAs(ErpGroupBuyingReviewDO::getRepeatGroupSales, ErpGroupBuyingReviewRespVO::getRepeatGroupSales)
                .selectAs(ErpGroupBuyingReviewDO::getCreateTime, ErpGroupBuyingReviewRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpGroupBuyingReviewRespVO.class, query);
    }

    default ErpGroupBuyingReviewDO selectByNo(String no) {
        return selectOne(ErpGroupBuyingReviewDO::getNo, no);
    }
}
