package cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcasting;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcasting.ErpPrivateBroadcastingDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpPrivateBroadcastingMapper extends BaseMapperX<ErpPrivateBroadcastingDO> {

    default PageResult<ErpPrivateBroadcastingRespVO> selectPage(ErpPrivateBroadcastingPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpPrivateBroadcastingDO> query = new MPJLambdaWrapperX<ErpPrivateBroadcastingDO>()
                .likeIfPresent(ErpPrivateBroadcastingDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpPrivateBroadcastingDO::getProductName, reqVO.getProductName())
                .eqIfPresent(ErpPrivateBroadcastingDO::getBrandId, reqVO.getBrandId())
                .betweenIfPresent(ErpPrivateBroadcastingDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpPrivateBroadcastingDO::getId)
                // 私播货盘表字段映射
                .selectAs(ErpPrivateBroadcastingDO::getId, ErpPrivateBroadcastingRespVO::getId)
                .selectAs(ErpPrivateBroadcastingDO::getNo, ErpPrivateBroadcastingRespVO::getNo)
                .selectAs(ErpPrivateBroadcastingDO::getProductImage, ErpPrivateBroadcastingRespVO::getProductImage)
                .selectAs(ErpPrivateBroadcastingDO::getBrandId, ErpPrivateBroadcastingRespVO::getBrandId)
                .selectAs(ErpPrivateBroadcastingDO::getProductName, ErpPrivateBroadcastingRespVO::getProductName)
                .selectAs(ErpPrivateBroadcastingDO::getProductSpec, ErpPrivateBroadcastingRespVO::getProductSpec)
                .selectAs(ErpPrivateBroadcastingDO::getProductSku, ErpPrivateBroadcastingRespVO::getProductSku)
                .selectAs(ErpPrivateBroadcastingDO::getMarketPrice, ErpPrivateBroadcastingRespVO::getMarketPrice)
                .selectAs(ErpPrivateBroadcastingDO::getShelfLife, ErpPrivateBroadcastingRespVO::getShelfLife)
                .selectAs(ErpPrivateBroadcastingDO::getProductStock, ErpPrivateBroadcastingRespVO::getProductStock)
                .selectAs(ErpPrivateBroadcastingDO::getLivePrice, ErpPrivateBroadcastingRespVO::getLivePrice)
                .selectAs(ErpPrivateBroadcastingDO::getProductNakedPrice, ErpPrivateBroadcastingRespVO::getProductNakedPrice)
                .selectAs(ErpPrivateBroadcastingDO::getExpressFee, ErpPrivateBroadcastingRespVO::getExpressFee)
                .selectAs(ErpPrivateBroadcastingDO::getDropshipPrice, ErpPrivateBroadcastingRespVO::getDropshipPrice)
                .selectAs(ErpPrivateBroadcastingDO::getPublicLink, ErpPrivateBroadcastingRespVO::getPublicLink)
                .selectAs(ErpPrivateBroadcastingDO::getCoreSellingPoint, ErpPrivateBroadcastingRespVO::getCoreSellingPoint)
                .selectAs(ErpPrivateBroadcastingDO::getExpressCompany, ErpPrivateBroadcastingRespVO::getExpressCompany)
                .selectAs(ErpPrivateBroadcastingDO::getShippingTime, ErpPrivateBroadcastingRespVO::getShippingTime)
                .selectAs(ErpPrivateBroadcastingDO::getShippingArea, ErpPrivateBroadcastingRespVO::getShippingArea)
                .selectAs(ErpPrivateBroadcastingDO::getRemark, ErpPrivateBroadcastingRespVO::getRemark)
                .selectAs(ErpPrivateBroadcastingDO::getCreateTime, ErpPrivateBroadcastingRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpPrivateBroadcastingRespVO.class, query);
    }

    default ErpPrivateBroadcastingDO selectByNo(String no) {
        return selectOne(ErpPrivateBroadcastingDO::getNo, no);
    }
}
