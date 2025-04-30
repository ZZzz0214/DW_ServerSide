package cn.iocoder.yudao.module.erp.dal.mysql.distribution;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionPurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionSaleDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpDistributionMapper extends BaseMapperX<ErpDistributionBaseDO> {

    default PageResult<ErpDistributionRespVO> selectPage(ErpDistributionPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpDistributionBaseDO> query = new MPJLambdaWrapperX<ErpDistributionBaseDO>()
                .likeIfPresent(ErpDistributionBaseDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpDistributionBaseDO::getLogisticsCompany, reqVO.getLogisticsCompany())
                .likeIfPresent(ErpDistributionBaseDO::getTrackingNumber, reqVO.getTrackingNumber())
                .likeIfPresent(ErpDistributionBaseDO::getReceiverName, reqVO.getReceiverName())
                .betweenIfPresent(ErpDistributionBaseDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpDistributionBaseDO::getId)
                // 添加基础表字段映射
                .selectAs(ErpDistributionBaseDO::getId, ErpDistributionRespVO::getId)
                .selectAs(ErpDistributionBaseDO::getNo, ErpDistributionRespVO::getNo)
                .selectAs(ErpDistributionBaseDO::getLogisticsCompany, ErpDistributionRespVO::getLogisticsCompany)
                .selectAs(ErpDistributionBaseDO::getTrackingNumber, ErpDistributionRespVO::getTrackingNumber)
                .selectAs(ErpDistributionBaseDO::getReceiverName, ErpDistributionRespVO::getReceiverName)
                .selectAs(ErpDistributionBaseDO::getReceiverPhone, ErpDistributionRespVO::getReceiverPhone)
                .selectAs(ErpDistributionBaseDO::getCreateTime, ErpDistributionRespVO::getCreateTime)
                .selectAs(ErpDistributionBaseDO::getProductQuantity, ErpDistributionRespVO::getProductQuantity)
                .selectAs(ErpDistributionBaseDO::getProductName, ErpDistributionRespVO::getProductName);

        // 联表查询采购信息
        query.leftJoin(ErpDistributionPurchaseDO.class, ErpDistributionPurchaseDO::getBaseId, ErpDistributionBaseDO::getId)
                .selectAs(ErpDistributionPurchaseDO::getPurchaser, ErpDistributionRespVO::getPurchaser)
                .selectAs(ErpDistributionPurchaseDO::getSupplier, ErpDistributionRespVO::getSupplier)
                .selectAs(ErpDistributionPurchaseDO::getPurchasePrice, ErpDistributionRespVO::getPurchasePrice)
                .selectAs(ErpDistributionPurchaseDO::getShippingFee, ErpDistributionRespVO::getShippingFee)
                .selectAs(ErpDistributionPurchaseDO::getOtherFees, ErpDistributionRespVO::getOtherFees)
                .selectAs(ErpDistributionPurchaseDO::getTotalPurchaseAmount, ErpDistributionRespVO::getTotalPurchaseAmount);

        // 联表查询销售信息
        query.leftJoin(ErpDistributionSaleDO.class, ErpDistributionSaleDO::getBaseId, ErpDistributionBaseDO::getId)
                .selectAs(ErpDistributionSaleDO::getSalesperson, ErpDistributionRespVO::getSalesperson)
                .selectAs(ErpDistributionSaleDO::getCustomerName, ErpDistributionRespVO::getCustomerName)
                .selectAs(ErpDistributionSaleDO::getSalePrice, ErpDistributionRespVO::getSalePrice)
                .selectAs(ErpDistributionSaleDO::getShippingFee, ErpDistributionRespVO::getSaleShippingFee)
                .selectAs(ErpDistributionSaleDO::getOtherFees, ErpDistributionRespVO::getSaleOtherFees)
                .selectAs(ErpDistributionSaleDO::getTotalSaleAmount, ErpDistributionRespVO::getTotalSaleAmount);

                PageResult<ErpDistributionRespVO> result = selectJoinPage(reqVO, ErpDistributionRespVO.class, query);
                System.out.println("Query result: " + result);
        return selectJoinPage(reqVO, ErpDistributionRespVO.class, query);
    }

    default ErpDistributionBaseDO selectByNo(String no) {
        return selectOne(ErpDistributionBaseDO::getNo, no);
    }
}
