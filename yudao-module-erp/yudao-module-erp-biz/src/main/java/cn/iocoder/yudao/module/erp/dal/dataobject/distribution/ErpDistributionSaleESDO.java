package cn.iocoder.yudao.module.erp.dal.dataobject.distribution;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "erp_distribution_sale")
public class ErpDistributionSaleESDO {

    @Id
    private Long id;

    @Field(name = "base_id", type = FieldType.Long)
    private Long baseId;

    @Field(name = "sale_price_id", type = FieldType.Long)
    private Long salePriceId;

    @Field(name = "salesperson", type = FieldType.Keyword)
    private String salesperson;

    @Field(name = "customer_name", type = FieldType.Keyword)
    private String customerName;

    @Field(name = "sale_price", type = FieldType.Double)
    private BigDecimal salePrice;

    @Field(name = "shipping_fee", type = FieldType.Double)
    private BigDecimal shippingFee;

    @Field(name = "other_fees", type = FieldType.Double)
    private BigDecimal otherFees;

    @Field(name = "total_sale_amount", type = FieldType.Double)
    private BigDecimal totalSaleAmount;

    @Field(name = "tenant_id", type = FieldType.Long)
    private Long tenantId;

    @Field(name = "deleted", type = FieldType.Boolean)
    private Boolean deleted;

    @Field(name = "sale_remark", type = FieldType.Text)
    private String saleRemark;

    @Field(name = "sale_after_sales_status", type = FieldType.Integer)
    private Integer saleAfterSalesStatus;

    @Field(name = "sale_after_sales_situation", type = FieldType.Text)
    private String saleAfterSalesSituation;

    @Field(name = "sale_after_sales_amount", type = FieldType.Double)
    private BigDecimal saleAfterSalesAmount;

    @Field(name = "sale_after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime saleAfterSalesTime;

    @Field(name = "sale_audit_status", type = FieldType.Integer)
    private Integer saleAuditStatus;

    @Field(name = "transfer_person", type = FieldType.Keyword)
    private String transferPerson;

    @Field(name = "sale_approval_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime saleApprovalTime;

    @Field(name = "sale_unapprove_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime saleUnapproveTime;

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
