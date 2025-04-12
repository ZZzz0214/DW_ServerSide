package cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_sale;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 批发销售订单项 DO
 */
@TableName("erp_wholesale_sale_order_items")
@KeySequence("erp_wholesale_sale_order_items_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpWholesaleSaleOrderItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 批发销售订单编号
     */
    private Long orderId;
    /**
     * 组品编号
     */
    private Long groupProductId;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 客户名称
     */
    private String customerName;
    /**
     * 销售人员
     */
    private String salesPerson;
    /**
     * 原表商品
     */
    private String originalProduct;
    /**
     * 原表规格
     */
    private String originalSpecification;
    /**
     * 原表数量
     */
    private Integer originalQuantity;
    /**
     * 产品数量
     */
    private Integer productQuantity;
    /**
     * 物流费用
     */
    private BigDecimal logisticsFee;
    /**
     * 其他费用
     */
    private BigDecimal otherFees;
    /**
     * 出货总额
     */
    private BigDecimal shippingTotal;
    /**
     * 产品编号
     */
    private Long productId;
    /**
     * 产品单位编号
     */
    private Long productUnitId;
    /**
     * 批发出货单价
     */
    private BigDecimal wholesaleProductPrice;
    /**
     * 数量
     */
    private BigDecimal count;
    /**
     * 总价
     */
    private BigDecimal totalPrice;
    /**
     * 税率，百分比
     */
    private BigDecimal taxPercent;
    /**
     * 税额，单位：元
     */
    private BigDecimal taxPrice;
    /**
     * 备注
     */
    private String remark;
    /**
     * 销售出库数量
     */
    private BigDecimal outCount;
    /**
     * 销售退货数量
     */
    private BigDecimal returnCount;
    /**
     * 货拉拉费用
     */
    private BigDecimal hulalaFee;
}
