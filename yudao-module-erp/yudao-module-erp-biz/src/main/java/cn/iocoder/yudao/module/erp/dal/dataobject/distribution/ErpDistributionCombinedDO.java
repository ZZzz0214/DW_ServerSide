package cn.iocoder.yudao.module.erp.dal.dataobject.distribution;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("erp_distribution_combined")
@KeySequence("erp_distribution_combined_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpDistributionCombinedDO extends BaseDO {

    @TableId
    private Long id;
    private String no;
    private String orderNumber;
    private String logisticsCompany;
    private String trackingNumber;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String originalProductName;
    private String originalStandard;
    private Integer originalQuantity;
    private String remark;
    private Long comboProductId;
    private String productSpecification;
    private Integer productQuantity;
    private String afterSalesStatus;
    private LocalDateTime afterSalesTime;

    // 采购相关字段
    private BigDecimal purchaseOtherFees;
    private String purchaseRemark;
    private Integer purchaseAfterSalesStatus;
    private BigDecimal purchaseAfterSalesAmount;
    private LocalDateTime purchaseApprovalTime;
    private LocalDateTime purchaseAfterSalesTime;
    private Integer purchaseAuditStatus;
    private LocalDateTime purchaseUnapproveTime;

    // 销售相关字段
    private String salesperson;
    private String customerName;
    private BigDecimal saleOtherFees;
    private String saleRemark;
    private String transferPerson;
    private Integer saleAfterSalesStatus;
    private BigDecimal saleAfterSalesAmount;
    private LocalDateTime saleAfterSalesTime;
    private Integer saleAuditStatus;
    private LocalDateTime saleApprovalTime;
    private LocalDateTime saleUnapproveTime;

    // 审核总额字段
    private BigDecimal purchaseAuditTotalAmount;
    private BigDecimal saleAuditTotalAmount;

    // 租户字段
    private Long tenantId;

    /**
     * 是否删除
     */
    private Boolean deleted;
}
