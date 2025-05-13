package cn.iocoder.yudao.module.erp.dal.dataobject.distribution;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 代发采购 DO
 *
 * @author 芋道源码
 */
@TableName("erp_distribution_purchase")
@KeySequence("erp_distribution_purchase_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpDistributionPurchaseDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 关联代发基础表
     */
    private Long baseId;

    /**
     * 关联组品表
     */
    private Long comboProductId;

    /**
     * 采购人员
     */
    private String purchaser;

    /**
     * 供应商名
     */
    private String supplier;

    /**
     * 采购单价
     */
    private BigDecimal purchasePrice;

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
     * 租户编号
     */
    private Long tenantId;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 采购售后状态
     */
    private Integer purchaseAfterSalesStatus;

    /**
     * 采购售后情况
     */
    private String purchaseAfterSalesSituation;

    /**
     * 采购售后金额
     */
    private BigDecimal purchaseAfterSalesAmount;

    /**
     * 采购审批时间
     */
    private LocalDateTime purchaseApprovalTime;

    /**
     * 采购售后时间
     */
    private LocalDateTime purchaseAfterSalesTime;

    /**
     * 采购审核状态
     */
    private Integer purchaseAuditStatus;
}
