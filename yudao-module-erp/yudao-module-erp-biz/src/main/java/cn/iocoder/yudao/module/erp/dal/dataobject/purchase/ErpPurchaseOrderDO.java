package cn.iocoder.yudao.module.erp.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 采购订单 DO
 *
 * @author 芋道源码
 */
@TableName(value = "erp_purchase_order")
@KeySequence("erp_purchase_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseOrderDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 采购订单号
     */
    private String no;
    /**
     * 采购状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.erp.enums.ErpAuditStatus}
     */
    private Integer status;
    /**
     * 供应商编号
     *
     * 关联 {@link ErpSupplierDO#getId()}
     */
    private Long supplierId;
    /**
     * 结算账户编号
     *
     * 关联 {@link ErpAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 下单时间
     */
    private LocalDateTime orderTime;
    /**
     * 物流公司
     */
    private String logisticsCompany;
    /**
     * 物流单号
     */
    private String trackingNumber;
    /**
     * 收件姓名
     */
    private String receiverName;
    /**
     * 联系电话
     */
    private String receiverPhone;
    /**
     * 详细地址
     */
    private String receiverAddress;
    /**
     * 合计数量
     */
    private BigDecimal totalCount;
    /**
     * 最终合计价格，单位：元
     *
     * totalPrice = totalProductPrice + totalTaxPrice - discountPrice
     */
    private BigDecimal totalPrice;
    /**
     * 合计产品价格，单位：元
     */
    private BigDecimal totalProductPrice;
    /**
     * 合计税额，单位：元
     */
    private BigDecimal totalTaxPrice;
    /**
     * 优惠率，百分比
     */
    private BigDecimal discountPercent;
    /**
     * 优惠金额，单位：元
     *
     * discountPrice = (totalProductPrice + totalTaxPrice) * discountPercent
     */
    private BigDecimal discountPrice;
    /**
     * 定金金额，单位：元
     */
    private BigDecimal depositPrice;
    /**
     * 采购运费（合计）
     */
    private BigDecimal shippingFee;
    /**
     * 其他费用（合计）
     */
    private BigDecimal otherFees;
    /**
     * 采购总额（合计）
     */
    private BigDecimal totalPurchaseAmount;
    /**
     * 附件地址
     */
    private String fileUrl;
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
//    /**
//     * 创建者
//     */
//    private String creator;
//    /**
//     * 创建时间
//     */
//    private LocalDateTime createTime;
//    /**
//     * 更新者
//     */
//    private String updater;
//    /**
//     * 更新时间
//     */
//    private LocalDateTime updateTime;
    /**
     * 是否删除
     */
    private Boolean deleted;
    /**
     * 租户编号
     */
    private Long tenantId;

}
