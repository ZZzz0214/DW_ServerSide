package cn.iocoder.yudao.module.erp.dal.dataobject.wholesale;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "erp_wholesale_combined")
public class ErpWholesaleCombinedESDO {

    @Id
    private Long id;

    @Field(name = "no", type = FieldType.Keyword)
    private String no;

    @Field(name = "receiver_name", type = FieldType.Keyword)
    private String receiverName;

    @Field(name = "receiver_phone", type = FieldType.Keyword)
    private String receiverPhone;

    @Field(name = "receiver_address", type = FieldType.Text)
    private String receiverAddress;

    @Field(name = "combo_product_id", type = FieldType.Long)
    private Long comboProductId;

    @Field(name = "product_quantity", type = FieldType.Integer)
    private Integer productQuantity;

    @Field(name = "product_specification", type = FieldType.Text)
    private String productSpecification;

    @Field(name = "logistics_number", type = FieldType.Keyword)
    private String logisticsNumber;

    @Field(name = "after_sales_status", type = FieldType.Text)
    private String afterSalesStatus;

    @Field(name = "after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime afterSalesTime;

    @Field(name = "remark", type = FieldType.Text)
    private String remark;

    // 采购相关字段
    @Field(name = "purchase_truck_fee", type = FieldType.Double)
    private BigDecimal purchaseTruckFee;

    @Field(name = "purchase_logistics_fee", type = FieldType.Double)
    private BigDecimal purchaseLogisticsFee;

    @Field(name = "purchase_other_fees", type = FieldType.Double)
    private BigDecimal purchaseOtherFees;

    @Field(name = "purchase_remark", type = FieldType.Text)
    private String purchaseRemark;

    @Field(name = "purchase_after_sales_status", type = FieldType.Integer)
    private Integer purchaseAfterSalesStatus;

    @Field(name = "purchase_after_sales_amount", type = FieldType.Double)
    private BigDecimal purchaseAfterSalesAmount;

    @Field(name = "purchase_after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseAfterSalesTime;

    @Field(name = "purchase_audit_status", type = FieldType.Integer)
    private Integer purchaseAuditStatus;

    @Field(name = "purchase_approval_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseApprovalTime;

    @Field(name = "purchase_unapprove_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseUnapproveTime;

    // 销售相关字段
    @Field(name = "salesperson", type = FieldType.Keyword)
    private String salesperson;

    @Field(name = "customer_name", type = FieldType.Text)
    private String customerName;

    @Field(name = "sale_truck_fee", type = FieldType.Double)
    private BigDecimal saleTruckFee;

    @Field(name = "sale_logistics_fee", type = FieldType.Double)
    private BigDecimal saleLogisticsFee;

    @Field(name = "sale_other_fees", type = FieldType.Double)
    private BigDecimal saleOtherFees;

    @Field(name = "sale_remark", type = FieldType.Text)
    private String saleRemark;

    @Field(name = "transfer_person", type = FieldType.Keyword)
    private String transferPerson;

    @Field(name = "sale_after_sales_status", type = FieldType.Integer)
    private Integer saleAfterSalesStatus;

    @Field(name = "sale_after_sales_amount", type = FieldType.Double)
    private BigDecimal saleAfterSalesAmount;

    @Field(name = "sale_after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime saleAfterSalesTime;

    @Field(name = "sale_audit_status", type = FieldType.Integer)
    private Integer saleAuditStatus;

    @Field(name = "sale_approval_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime saleApprovalTime;

    @Field(name = "sale_unapprove_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime saleUnapproveTime;

    // 基础字段
    @Field(name = "create_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;

    @Field(name = "update_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updateTime;

    @Field(name = "creator", type = FieldType.Keyword)
    private String creator;

    @Field(name = "updater", type = FieldType.Keyword)
    private String updater;

    // 租户字段
    @Field(name = "tenant_id", type = FieldType.Long)
    private Long tenantId;

    @Field(name = "deleted", type = FieldType.Boolean)
    private Boolean deleted;
}