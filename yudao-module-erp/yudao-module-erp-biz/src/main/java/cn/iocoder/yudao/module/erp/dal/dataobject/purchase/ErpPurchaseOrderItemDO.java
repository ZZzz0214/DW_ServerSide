package cn.iocoder.yudao.module.erp.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 采购订单项 DO
 *
 * @author 芋道源码
 */
@TableName("erp_purchase_order_items")
@KeySequence("erp_purchase_order_items_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseOrderItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 采购订单编号
     *
     * 关联 {@link ErpPurchaseOrderDO#getId()}
     */
    private Long orderId;
    /**
     * 产品类型：0-单品，1-组合产品
     */
    private Integer type;
    /**
     * 产品编号（指向单品或组合产品）
     *
     * 关联 {@link ErpProductDO#getId()}
     */
    private Long productId;
    /**
     * 组品编号
     */
    private Long comboProductId;
    /**
     * 原表商品名称
     */
    private String originalProductName;
    /**
     * 原表规格
     */
    private String originalStandard;
    /**
     * 原表数量
     */
    private Integer originalQuantity;
    /**
     * 售后状况
     */
    private Integer afterSalesStatus;
    /**
     * 发货编码
     */
    private String shippingCode;
    /**
     * 产品数量
     */
    private Integer productQuantity;
    /**
     * 采购运费
     */
    private BigDecimal shippingFee;
    /**
     * 其他费用
     */
    private BigDecimal otherFees;
    /**
     * 采购总额
     */
    private BigDecimal totalPurchaseAmount;
    /**
     * 产品单位编号
     */
    private Long productUnitId;
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
     * 备注信息
     */
    private String remark;
    /**
     * 采购入库数量
     */
    private BigDecimal inCount;
    /**
     * 采购退货数量
     */
    private BigDecimal returnCount;
    /**
     * 创建者
     */
    private String creator;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新者
     */
    private String updater;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 是否删除
     */
    private Boolean deleted;
    /**
     * 租户编号
     */
    private Long tenantId;

}
