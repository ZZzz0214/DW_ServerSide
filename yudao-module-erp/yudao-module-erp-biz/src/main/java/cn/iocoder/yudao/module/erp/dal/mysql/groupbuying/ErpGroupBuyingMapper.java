package cn.iocoder.yudao.module.erp.dal.mysql.groupbuying;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpGroupBuyingMapper extends BaseMapperX<ErpGroupBuyingDO> {

    default PageResult<ErpGroupBuyingRespVO> selectPage(ErpGroupBuyingPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpGroupBuyingDO> query = new MPJLambdaWrapperX<ErpGroupBuyingDO>()
                .likeIfPresent(ErpGroupBuyingDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpGroupBuyingDO::getProductName, reqVO.getProductName())
                .likeIfPresent(ErpGroupBuyingDO::getBrandName, reqVO.getBrandName())
                .eqIfPresent(ErpGroupBuyingDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpGroupBuyingDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpGroupBuyingDO::getId)
                // 团购货盘表字段映射
                .selectAs(ErpGroupBuyingDO::getId, ErpGroupBuyingRespVO::getId)
                .selectAs(ErpGroupBuyingDO::getNo, ErpGroupBuyingRespVO::getNo)
                .selectAs(ErpGroupBuyingDO::getProductImage, ErpGroupBuyingRespVO::getProductImage)
                .selectAs(ErpGroupBuyingDO::getBrandName, ErpGroupBuyingRespVO::getBrandName)
                .selectAs(ErpGroupBuyingDO::getProductName, ErpGroupBuyingRespVO::getProductName)
                .selectAs(ErpGroupBuyingDO::getProductSpec, ErpGroupBuyingRespVO::getProductSpec)
                .selectAs(ErpGroupBuyingDO::getProductSku, ErpGroupBuyingRespVO::getProductSku)
                .selectAs(ErpGroupBuyingDO::getMarketPrice, ErpGroupBuyingRespVO::getMarketPrice)
                .selectAs(ErpGroupBuyingDO::getShelfLife, ErpGroupBuyingRespVO::getShelfLife)
                .selectAs(ErpGroupBuyingDO::getProductStock, ErpGroupBuyingRespVO::getProductStock)
                .selectAs(ErpGroupBuyingDO::getRemark, ErpGroupBuyingRespVO::getRemark)
                .selectAs(ErpGroupBuyingDO::getCorePrice, ErpGroupBuyingRespVO::getCorePrice)
                .selectAs(ErpGroupBuyingDO::getDistributionPrice, ErpGroupBuyingRespVO::getDistributionPrice)
                .selectAs(ErpGroupBuyingDO::getSupplyGroupPrice, ErpGroupBuyingRespVO::getSupplyGroupPrice)
                .selectAs(ErpGroupBuyingDO::getSellingCommission, ErpGroupBuyingRespVO::getSellingCommission)
                .selectAs(ErpGroupBuyingDO::getGroupPrice, ErpGroupBuyingRespVO::getGroupPrice)
                .selectAs(ErpGroupBuyingDO::getChannelProfit, ErpGroupBuyingRespVO::getChannelProfit)
                .selectAs(ErpGroupBuyingDO::getGroupMechanism, ErpGroupBuyingRespVO::getGroupMechanism)
                .selectAs(ErpGroupBuyingDO::getExpressFee, ErpGroupBuyingRespVO::getExpressFee)
                .selectAs(ErpGroupBuyingDO::getTmallJd, ErpGroupBuyingRespVO::getTmallJd)
                .selectAs(ErpGroupBuyingDO::getPublicData, ErpGroupBuyingRespVO::getPublicData)
                .selectAs(ErpGroupBuyingDO::getPrivateData, ErpGroupBuyingRespVO::getPrivateData)
                .selectAs(ErpGroupBuyingDO::getBrandEndorsement, ErpGroupBuyingRespVO::getBrandEndorsement)
                .selectAs(ErpGroupBuyingDO::getCompetitiveAnalysis, ErpGroupBuyingRespVO::getCompetitiveAnalysis)
                .selectAs(ErpGroupBuyingDO::getExpressCompany, ErpGroupBuyingRespVO::getExpressCompany)
                .selectAs(ErpGroupBuyingDO::getShippingTime, ErpGroupBuyingRespVO::getShippingTime)
                .selectAs(ErpGroupBuyingDO::getShippingArea, ErpGroupBuyingRespVO::getShippingArea)
                .selectAs(ErpGroupBuyingDO::getStatus, ErpGroupBuyingRespVO::getStatus)
                .selectAs(ErpGroupBuyingDO::getCreator, ErpGroupBuyingRespVO::getCreator)
                .selectAs(ErpGroupBuyingDO::getCreateTime, ErpGroupBuyingRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpGroupBuyingRespVO.class, query);
    }

    default ErpGroupBuyingDO selectByNo(String no) {
        return selectOne(ErpGroupBuyingDO::getNo, no);
    }

    default List<ErpGroupBuyingDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpGroupBuyingDO::getNo, nos);
    }

    default void insertBatch(List<ErpGroupBuyingDO> list) {
        list.forEach(this::insert);
    }
}
