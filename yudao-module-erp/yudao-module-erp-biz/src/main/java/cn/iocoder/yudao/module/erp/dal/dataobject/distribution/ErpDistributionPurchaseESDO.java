package cn.iocoder.yudao.module.erp.dal.dataobject.distribution;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "erp_distribution_purchase")
public class ErpDistributionPurchaseESDO {

    @Id
    private Long id;

    @Field(name = "base_id", type = FieldType.Long)
    private Long baseId;

    @Field(name = "combo_product_id", type = FieldType.Long)
    private Long comboProductId;

    @Field(name = "purchaser", type = FieldType.Keyword)
    private String purchaser;

    @Field(name = "supplier", type = FieldType.Keyword)
    private String supplier;

    @Field(name = "purchase_price", type = FieldType.Double)
    private BigDecimal purchasePrice;

    @Field(name = "shipping_fee", type = FieldType.Double)
    private BigDecimal shippingFee;

    @Field(name = "other_fees", type = FieldType.Double)
    private BigDecimal otherFees;

    @Field(name = "total_purchase_amount", type = FieldType.Double)
    private BigDecimal totalPurchaseAmount;

    @Field(name = "tenant_id", type = FieldType.Long)
    private Long tenantId;

    @Field(name = "deleted", type = FieldType.Boolean)
    private Boolean deleted;

    @Field(name = "purchase_remark", type = FieldType.Text)
    private String purchaseRemark;

    @Field(name = "purchase_after_sales_status", type = FieldType.Integer)
    private Integer purchaseAfterSalesStatus;

    @Field(name = "purchase_after_sales_situation", type = FieldType.Text)
    private String purchaseAfterSalesSituation;

    @Field(name = "purchase_after_sales_amount", type = FieldType.Double)
    private BigDecimal purchaseAfterSalesAmount;

    @Field(name = "purchase_approval_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseApprovalTime;

    @Field(name = "purchase_after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseAfterSalesTime;

    @Field(name = "purchase_audit_status", type = FieldType.Integer)
    private Integer purchaseAuditStatus;

    @Field(name = "purchase_unapprove_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseUnapproveTime;

    // BaseDO中的基础字段
    @Field(name = "create_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;

    @Field(name = "update_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updateTime;

    @Field(name = "creator", type = FieldType.Keyword)
    private String creator;

    @Field(name = "updater", type = FieldType.Keyword)
    private String updater;
}