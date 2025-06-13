package cn.iocoder.yudao.module.erp.dal.dataobject.wholesale;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("erp_wholesale_combined")
@KeySequence("erp_wholesale_combined_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpWholesaleCombinedDO extends BaseDO {

    @TableId
    private Long id;
    private String no;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Long comboProductId;
    private Integer productQuantity;
    private String productSpecification;
    private String logisticsNumber;
    private String afterSalesStatus;
    private LocalDateTime afterSalesTime;
    private String remark;

    // 采购相关字段
    private BigDecimal purchaseTruckFee;
    private BigDecimal purchaseLogisticsFee;
    private BigDecimal purchaseOtherFees;
    private String purchaseRemark;
    private Integer purchaseAfterSalesStatus;
    private BigDecimal purchaseAfterSalesAmount;
    private LocalDateTime purchaseAfterSalesTime;
    private Integer purchaseAuditStatus;
    private LocalDateTime purchaseApprovalTime;
    private LocalDateTime purchaseUnapproveTime;

    // 销售相关字段
    private String salesperson;
    private String customerName;
    private BigDecimal saleTruckFee;
    private BigDecimal saleLogisticsFee;
    private BigDecimal saleOtherFees;
    private String saleRemark;
    private String transferPerson;
    private Integer saleAfterSalesStatus;
    private BigDecimal saleAfterSalesAmount;
    private LocalDateTime saleAfterSalesTime;
    private Integer saleAuditStatus;
    private LocalDateTime saleApprovalTime;
    private LocalDateTime saleUnapproveTime;

    // 租户字段
    private Long tenantId;
    private Boolean deleted;
}
