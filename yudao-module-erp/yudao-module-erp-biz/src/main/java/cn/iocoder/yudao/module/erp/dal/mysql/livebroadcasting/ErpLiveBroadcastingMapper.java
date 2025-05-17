package cn.iocoder.yudao.module.erp.dal.mysql.livebroadcasting;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcasting.ErpLiveBroadcastingDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpLiveBroadcastingMapper extends BaseMapperX<ErpLiveBroadcastingDO> {

    default PageResult<ErpLiveBroadcastingRespVO> selectPage(ErpLiveBroadcastingPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpLiveBroadcastingDO> query = new MPJLambdaWrapperX<ErpLiveBroadcastingDO>()
                .likeIfPresent(ErpLiveBroadcastingDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpLiveBroadcastingDO::getBrandId, reqVO.getBrandId())
                .likeIfPresent(ErpLiveBroadcastingDO::getProductName, reqVO.getProductName())
                .betweenIfPresent(ErpLiveBroadcastingDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpLiveBroadcastingDO::getId)
                // 直播货盘表字段映射
                .selectAs(ErpLiveBroadcastingDO::getId, ErpLiveBroadcastingRespVO::getId)
                .selectAs(ErpLiveBroadcastingDO::getNo, ErpLiveBroadcastingRespVO::getNo)
                .selectAs(ErpLiveBroadcastingDO::getProductImage, ErpLiveBroadcastingRespVO::getProductImage)
                .selectAs(ErpLiveBroadcastingDO::getBrandId, ErpLiveBroadcastingRespVO::getBrandId)
                .selectAs(ErpLiveBroadcastingDO::getProductName, ErpLiveBroadcastingRespVO::getProductName)
                .selectAs(ErpLiveBroadcastingDO::getProductSpec, ErpLiveBroadcastingRespVO::getProductSpec)
                .selectAs(ErpLiveBroadcastingDO::getProductSku, ErpLiveBroadcastingRespVO::getProductSku)
                .selectAs(ErpLiveBroadcastingDO::getMarketPrice, ErpLiveBroadcastingRespVO::getMarketPrice)
                .selectAs(ErpLiveBroadcastingDO::getShelfLife, ErpLiveBroadcastingRespVO::getShelfLife)
                .selectAs(ErpLiveBroadcastingDO::getProductStock, ErpLiveBroadcastingRespVO::getProductStock)
                .selectAs(ErpLiveBroadcastingDO::getCoreSellingPoint, ErpLiveBroadcastingRespVO::getCoreSellingPoint)
                .selectAs(ErpLiveBroadcastingDO::getRemark, ErpLiveBroadcastingRespVO::getRemark)
                .selectAs(ErpLiveBroadcastingDO::getLivePrice, ErpLiveBroadcastingRespVO::getLivePrice)
                .selectAs(ErpLiveBroadcastingDO::getLiveCommission, ErpLiveBroadcastingRespVO::getLiveCommission)
                .selectAs(ErpLiveBroadcastingDO::getPublicCommission, ErpLiveBroadcastingRespVO::getPublicCommission)
                .selectAs(ErpLiveBroadcastingDO::getRebateCommission, ErpLiveBroadcastingRespVO::getRebateCommission)
                .selectAs(ErpLiveBroadcastingDO::getExpressCompany, ErpLiveBroadcastingRespVO::getExpressCompany)
                .selectAs(ErpLiveBroadcastingDO::getShippingTime, ErpLiveBroadcastingRespVO::getShippingTime)
                .selectAs(ErpLiveBroadcastingDO::getShippingArea, ErpLiveBroadcastingRespVO::getShippingArea)
                .selectAs(ErpLiveBroadcastingDO::getCreateTime, ErpLiveBroadcastingRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpLiveBroadcastingRespVO.class, query);
    }

    default ErpLiveBroadcastingDO selectByNo(String no) {
        return selectOne(ErpLiveBroadcastingDO::getNo, no);
    }
}