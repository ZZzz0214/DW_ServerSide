package cn.iocoder.yudao.module.erp.dal.dataobject.wholesale;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 批发采购 DO
 *
 * @author 芋道源码
 */
@TableName("erp_wholesale_purchase")
@KeySequence("erp_wholesale_purchase_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpWholesalePurchaseDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 关联批发基础表
     */
    private Long baseId;

    /**
     * 关联组品表
     */
    private Long comboProductId;

    /**
     * 采购人员（->组品编号）
     */
    private String purchaser;

    /**
     * 供应商名（->组品编号）
     */
    private String supplier;

    /**
     * 采购单价（->组品编号）
     */
    private BigDecimal purchasePrice;

    /**
     * 货拉拉费
     */
    private BigDecimal truckFee;

    /**
     * 物流费用
     */
    private BigDecimal logisticsFee;

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
     * 采购售后时间
     */
    private LocalDateTime purchaseAfterSalesTime;

    /**
     * 采购审核状态
     */
    private Integer purchaseAuditStatus;
}
