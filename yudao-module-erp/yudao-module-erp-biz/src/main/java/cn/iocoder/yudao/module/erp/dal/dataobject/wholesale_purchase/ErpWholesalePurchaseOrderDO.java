package cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_purchase;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 批发采购订单 DO
 *
 * @author 芋道源码
 */
@TableName(value = "erp_wholesale_purchase_order")
@KeySequence("erp_wholesale_purchase_order_seq") 
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpWholesalePurchaseOrderDO extends BaseDO {

    /**
     * 订单编号
     */
    @TableId
    private Long id;
    /**
     * 订单号
     */
    private String no;
    /**
     * 采购状态
     */
    private Integer status;
    /**
     * 供应商编号
     */
    private Long supplierId;
    /**
     * 结算账户编号
     */
    private Long accountId;
    /**
     * 采购时间
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
     * 合计价格，单位：元
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
     */
    private BigDecimal discountPrice;
    /**
     * 定金金额，单位：元
     */
    private BigDecimal depositPrice;
    /**
     * 物流费用（合计）
     */
    private BigDecimal logisticsFee;
    /**
     * 货拉拉费用（合计）
     */
    private BigDecimal hulalaFee;
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
    /**
     * 是否删除
     */
    private Boolean deleted;
    /**
     * 租户编号
     */
    private Long tenantId;
}