package cn.iocoder.yudao.module.erp.dal.mysql.wholesale;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesalePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesaleRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionPurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionSaleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesalePurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleSaleDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpWholesaleMapper extends BaseMapperX<ErpWholesaleBaseDO> {

    default PageResult<ErpWholesaleRespVO> selectPage(ErpWholesalePageReqVO reqVO) {
        MPJLambdaWrapperX<ErpWholesaleBaseDO> query = new MPJLambdaWrapperX<ErpWholesaleBaseDO>()
                .likeIfPresent(ErpWholesaleBaseDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpWholesaleBaseDO::getReceiverName, reqVO.getReceiverName())
                .betweenIfPresent(ErpWholesaleBaseDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpWholesaleBaseDO::getId)
                // 添加基础表字段映射
                .selectAs(ErpWholesaleBaseDO::getId, ErpWholesaleRespVO::getId)
                .selectAs(ErpWholesaleBaseDO::getNo, ErpWholesaleRespVO::getNo)
                .selectAs(ErpWholesaleBaseDO::getOrderNumber, ErpWholesaleRespVO::getOrderNumber)
                .selectAs(ErpWholesaleBaseDO::getLogisticsNumber, ErpWholesaleRespVO::getLogisticsNumber)
                .selectAs(ErpWholesaleBaseDO::getReceiverName, ErpWholesaleRespVO::getReceiverName)
                .selectAs(ErpWholesaleBaseDO::getReceiverPhone, ErpWholesaleRespVO::getReceiverPhone)
                .selectAs(ErpWholesaleBaseDO::getReceiverAddress, ErpWholesaleRespVO::getReceiverAddress)
                .selectAs(ErpWholesaleBaseDO::getProductQuantity, ErpWholesaleRespVO::getProductQuantity)
                .selectAs(ErpWholesaleBaseDO::getProductSpecification, ErpWholesaleRespVO::getProductSpecification)
                .selectAs(ErpWholesaleBaseDO::getRemark, ErpWholesaleRespVO::getRemark)
                .selectAs(ErpWholesaleBaseDO::getCreateTime, ErpWholesaleRespVO::getCreateTime);

        // 联表查询采购信息
        query.leftJoin(ErpWholesalePurchaseDO.class, ErpWholesalePurchaseDO::getBaseId, ErpWholesaleBaseDO::getId)
                .eq(reqVO.getPurchaseAuditStatus() != null, ErpWholesalePurchaseDO::getPurchaseAuditStatus, reqVO.getPurchaseAuditStatus()) // 修改为eq方法
                .selectAs(ErpWholesalePurchaseDO::getTruckFee, ErpWholesaleRespVO::getTruckFee)
                .selectAs(ErpWholesalePurchaseDO::getComboProductId, ErpWholesaleRespVO::getComboProductId)
                .selectAs(ErpWholesalePurchaseDO::getOtherFees, ErpWholesaleRespVO::getOtherFees)
                .selectAs(ErpWholesalePurchaseDO::getPurchaseAfterSalesStatus, ErpWholesaleRespVO::getPurchaseAfterSalesStatus)
                .selectAs(ErpWholesalePurchaseDO::getPurchaseAfterSalesSituation, ErpWholesaleRespVO::getPurchaseAfterSalesSituation)
                .selectAs(ErpWholesalePurchaseDO::getPurchaseAfterSalesAmount, ErpWholesaleRespVO::getPurchaseAfterSalesAmount)
                .selectAs(ErpWholesalePurchaseDO::getPurchaseAuditStatus, ErpWholesaleRespVO::getPurchaseAuditStatus);

        // 联表查询组品信息
        query.leftJoin(ErpComboProductDO.class, ErpComboProductDO::getId, ErpWholesalePurchaseDO::getComboProductId)
        .selectAs(ErpComboProductDO::getName, ErpWholesaleRespVO::getProductName)
        .selectAs(ErpComboProductDO::getShippingCode, ErpWholesaleRespVO::getShippingCode)
        .selectAs(ErpComboProductDO::getPurchaser, ErpWholesaleRespVO::getPurchaser)
        .selectAs(ErpComboProductDO::getSupplier, ErpWholesaleRespVO::getSupplier)
        .selectAs(ErpComboProductDO::getWholesalePrice, ErpWholesaleRespVO::getPurchasePrice);

        // 计算采购运费
        query.selectAs(
            "CASE " +
            "WHEN t2.shipping_fee_type = 0 THEN t2.fixed_shipping_fee " +
            "WHEN t2.shipping_fee_type = 1 THEN " +
            "  CASE WHEN t2.additional_item_quantity > 0 " +
            "    THEN t2.additional_item_price * CEIL(t.product_quantity / t2.additional_item_quantity) " +
            "    ELSE 0 END " +
            "WHEN t2.shipping_fee_type = 2 THEN " +
            "  CASE WHEN t2.weight * t.product_quantity <= t2.first_weight " +
            "    THEN t2.first_weight_price " +
            "    ELSE t2.first_weight_price + t2.additional_weight_price * " +
            "      CEIL((t2.weight * t.product_quantity - t2.first_weight) / t2.additional_weight) END " +
            "ELSE 0 END",
            ErpWholesaleRespVO::getLogisticsFee
        );

        // 计算采购总额 = 采购单价*数量 + 货拉拉费 + 物流费用 + 其他费用
        query.selectAs(
            "t2.wholesale_price * t.product_quantity + " +
            "COALESCE(t1.truck_fee, 0) + " +
            "CASE " +
            "WHEN t2.shipping_fee_type = 0 THEN t2.fixed_shipping_fee " +
            "WHEN t2.shipping_fee_type = 1 THEN " +
            "  CASE WHEN t2.additional_item_quantity > 0 " +
            "    THEN t2.additional_item_price * CEIL(t.product_quantity / t2.additional_item_quantity) " +
            "    ELSE 0 END " +
            "WHEN t2.shipping_fee_type = 2 THEN " +
            "  CASE WHEN t2.weight * t.product_quantity <= t2.first_weight " +
            "    THEN t2.first_weight_price " +
            "    ELSE t2.first_weight_price + t2.additional_weight_price * " +
            "      CEIL((t2.weight * t.product_quantity - t2.first_weight) / t2.additional_weight) END " +
            "ELSE 0 END + " +
            "COALESCE(t1.other_fees, 0)",
            ErpWholesaleRespVO::getTotalPurchaseAmount
        );

        // ... 已有代码 ...



        // 联表查询销售信息
        query.leftJoin(ErpWholesaleSaleDO.class, ErpWholesaleSaleDO::getBaseId, ErpWholesaleBaseDO::getId)
                .eq(reqVO.getSaleAuditStatus() != null, ErpWholesaleSaleDO::getSaleAuditStatus, reqVO.getSaleAuditStatus())
                .selectAs(ErpWholesaleSaleDO::getSalesperson, ErpWholesaleRespVO::getSalesperson)
                .selectAs(ErpWholesaleSaleDO::getCustomerName, ErpWholesaleRespVO::getCustomerName)
                .selectAs(ErpWholesaleSaleDO::getTruckFee, ErpWholesaleRespVO::getSaleTruckFee)
                .selectAs(ErpWholesaleSaleDO::getOtherFees, ErpWholesaleRespVO::getSaleOtherFees)
                .selectAs(ErpWholesaleSaleDO::getSaleAfterSalesStatus, ErpWholesaleRespVO::getSaleAfterSalesStatus)
                .selectAs(ErpWholesaleSaleDO::getSaleAfterSalesSituation, ErpWholesaleRespVO::getSaleAfterSalesSituation)
                .selectAs(ErpWholesaleSaleDO::getSaleAfterSalesAmount, ErpWholesaleRespVO::getSaleAfterSalesAmount)
                .selectAs(ErpWholesaleSaleDO::getSaleAfterSalesTime, ErpWholesaleRespVO::getSaleAfterSalesTime)
                .selectAs(ErpWholesaleSaleDO::getSaleAuditStatus, ErpWholesaleRespVO::getSaleAuditStatus);
        // 联表查询销售价格信息
//        query.leftJoin(ErpSalePriceDO.class,
//                ErpSalePriceDO::getGroupProductId, ErpWholesalePurchaseDO::getComboProductId)
//                .eq(ErpSalePriceDO::getCustomerName, ErpWholesaleSaleDO::getCustomerName)
//                .selectAs(ErpSalePriceDO::getWholesalePrice, ErpWholesaleRespVO::getSalePrice);
        query.leftJoin(ErpSalePriceDO.class,
                        wrapper -> wrapper
                                .eq(ErpSalePriceDO::getGroupProductId, ErpWholesalePurchaseDO::getComboProductId)
                                .eq(ErpSalePriceDO::getCustomerName, ErpWholesaleSaleDO::getCustomerName))
                .selectAs(ErpSalePriceDO::getWholesalePrice, ErpWholesaleRespVO::getSalePrice);
            // 计算销售物流费用
            query.selectAs(
                "CASE " +
                "WHEN t4.shipping_fee_type = 0 THEN t4.fixed_shipping_fee " +
                "WHEN t4.shipping_fee_type = 1 THEN " +
                "  CASE WHEN t4.additional_item_quantity > 0 " +
                "    THEN t4.additional_item_price * CEIL(t.product_quantity / t4.additional_item_quantity) " +
                "    ELSE 0 END " +
                "WHEN t4.shipping_fee_type = 2 THEN " +
                "  CASE WHEN t2.weight * t.product_quantity <= t4.first_weight " +
                "    THEN t4.first_weight_price " +
                "    ELSE t4.first_weight_price + t4.additional_weight_price * " +
                "      CEIL((t2.weight * t.product_quantity - t4.first_weight) / t4.additional_weight) END " +
                "ELSE 0 END",
                ErpWholesaleRespVO::getSaleLogisticsFee
            );

            // 计算销售总额 = 销售单价*数量 + 销售物流费用 + 销售其他费用
            query.selectAs(
                "t4.wholesale_price * t.product_quantity + " +
                "COALESCE(t3.truck_fee, 0) + " +
                "CASE " +
                "WHEN t4.shipping_fee_type = 0 THEN t4.fixed_shipping_fee " +
                "WHEN t4.shipping_fee_type = 1 THEN " +
                "  CASE WHEN t4.additional_item_quantity > 0 " +
                "    THEN t4.additional_item_price * CEIL(t.product_quantity / t4.additional_item_quantity) " +
                "    ELSE 0 END " +
                "WHEN t4.shipping_fee_type = 2 THEN " +
                "  CASE WHEN t2.weight * t.product_quantity <= t4.first_weight " +
                "    THEN t4.first_weight_price " +
                "    ELSE t4.first_weight_price + t4.additional_weight_price * " +
                "      CEIL((t2.weight * t.product_quantity - t4.first_weight) / t4.additional_weight) END " +
                "ELSE 0 END + " +
                "COALESCE(t3.other_fees, 0)",
                ErpWholesaleRespVO::getTotalSaleAmount
            );

            PageResult<ErpDistributionRespVO> result = selectJoinPage(reqVO, ErpDistributionRespVO.class, query);
            System.out.println("Query result: " + result);

        return selectJoinPage(reqVO, ErpWholesaleRespVO.class, query);
    }

    default ErpWholesaleBaseDO selectByNo(String no) {
        return selectOne(ErpWholesaleBaseDO::getNo, no);
    }
}
