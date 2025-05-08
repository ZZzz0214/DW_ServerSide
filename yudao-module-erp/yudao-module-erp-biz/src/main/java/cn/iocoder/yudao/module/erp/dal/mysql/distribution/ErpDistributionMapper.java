package cn.iocoder.yudao.module.erp.dal.mysql.distribution;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionPurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionSaleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
                .selectAs(ErpDistributionBaseDO::getProductQuantity, ErpDistributionRespVO::getProductQuantity);

        // 联表查询采购信息
        query.leftJoin(ErpDistributionPurchaseDO.class, ErpDistributionPurchaseDO::getBaseId, ErpDistributionBaseDO::getId)
                //.selectAs(ErpDistributionPurchaseDO::getShippingFee, ErpDistributionRespVO::getShippingFee)
                .selectAs(ErpDistributionPurchaseDO::getOtherFees, ErpDistributionRespVO::getOtherFees);
                // 移除原有的totalPurchaseAmount映射
                // .selectAs(ErpDistributionPurchaseDO::getTotalPurchaseAmount, ErpDistributionRespVO::getTotalPurchaseAmount);

        // 联表查询组品信息
        query.leftJoin(ErpComboProductDO.class, ErpComboProductDO::getId, ErpDistributionPurchaseDO::getComboProductId)
                .selectAs(ErpComboProductDO::getName, ErpDistributionRespVO::getProductName)
                .selectAs(ErpComboProductDO::getShippingCode, ErpDistributionRespVO::getShippingCode)
                .selectAs(ErpComboProductDO::getPurchaser, ErpDistributionRespVO::getPurchaser)
                .selectAs(ErpComboProductDO::getSupplier, ErpDistributionRespVO::getSupplier)
                .selectAs(ErpComboProductDO::getPurchasePrice, ErpDistributionRespVO::getPurchasePrice);

        // 在运费计算前添加调试信息

        // 计算运费
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
                ErpDistributionRespVO::getShippingFee
        );

        // 计算采购总额 = 采购单价*数量 + 运费 + 其他费用
        query.selectAs(
                "t2.purchase_price * t.product_quantity + " +
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
                ErpDistributionRespVO::getTotalPurchaseAmount
        );

//        // 联表查询销售信息
        query.leftJoin(ErpDistributionSaleDO.class, ErpDistributionSaleDO::getBaseId, ErpDistributionBaseDO::getId)
                .selectAs(ErpDistributionSaleDO::getSalesperson, ErpDistributionRespVO::getSalesperson)
                .selectAs(ErpDistributionSaleDO::getCustomerName, ErpDistributionRespVO::getCustomerName)
                //.selectAs(ErpDistributionSaleDO::getSalePrice, ErpDistributionRespVO::getSalePrice)
                //.selectAs(ErpDistributionSaleDO::getShippingFee, ErpDistributionRespVO::getSaleShippingFee)
                .selectAs(ErpDistributionSaleDO::getOtherFees, ErpDistributionRespVO::getSaleOtherFees);
                //.selectAs(ErpDistributionSaleDO::getTotalSaleAmount, ErpDistributionRespVO::getTotalSaleAmount);
//        // 联表查询销售价格信息
        query.leftJoin(ErpSalePriceDO.class,
                ErpSalePriceDO::getGroupProductId, ErpDistributionPurchaseDO::getComboProductId)
                .eq(ErpSalePriceDO::getCustomerName, ErpDistributionSaleDO::getCustomerName)
                .selectAs(ErpSalePriceDO::getDistributionPrice, ErpDistributionRespVO::getSalePrice);
//
//        // 计算销售运费
        // 计算销售运费 - 使用组品表(t2)中的weight字段
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
                ErpDistributionRespVO::getSaleShippingFee
        );
//
//        // 计算销售总额 = 销售单价*数量 + 销售运费 + 销售其他费用
        query.selectAs(
                "t4.distribution_price * t.product_quantity + " +
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
                ErpDistributionRespVO::getTotalSaleAmount
        );

                PageResult<ErpDistributionRespVO> result = selectJoinPage(reqVO, ErpDistributionRespVO.class, query);
                System.out.println("Query result: " + result);
        return selectJoinPage(reqVO, ErpDistributionRespVO.class, query);
    }

    default ErpDistributionBaseDO selectByNo(String no) {
        return selectOne(ErpDistributionBaseDO::getNo, no);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpDistributionBaseDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpDistributionBaseDO>()
                .eq(ErpDistributionBaseDO::getId, id)
                .eq(ErpDistributionBaseDO::getStatus, status));
    }
}
