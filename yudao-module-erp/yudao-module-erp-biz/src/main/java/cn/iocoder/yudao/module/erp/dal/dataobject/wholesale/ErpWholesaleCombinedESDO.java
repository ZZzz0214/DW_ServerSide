package cn.iocoder.yudao.module.erp.dal.dataobject.wholesale;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "erp_wholesale_combined")
public class ErpWholesaleCombinedESDO {

    @Id
    private Long id;

    // 订单编号
    @Field(name = "no", type = FieldType.Keyword)
    private String no;

    // 物流单号
    @Field(name = "logistics_number", type = FieldType.Keyword)
    private String logisticsNumber;

    // 收件人信息
    @Field(name = "receiver_name", type = FieldType.Keyword)
    private String receiverName;

    @Field(name = "receiver_phone", type = FieldType.Keyword)
    private String receiverPhone;

    @Field(name = "receiver_address", type = FieldType.Keyword)
    private String receiverAddress;

    // 组品信息
    @Field(name = "combo_product_id", type = FieldType.Long)
    private Long comboProductId;

    // 移除以下从组品表获取的字段，需要实时查询
    // private String comboProductNo;
    // private String shippingCode;
    // private String productName;
    // private String purchaser;
    // private String supplier;

    @Field(name = "product_specification", type = FieldType.Keyword)
    private String productSpecification;

    @Field(name = "product_quantity", type = FieldType.Integer)
    private Integer productQuantity;

    // 售后信息
    @Field(name = "after_sales_status", type = FieldType.Keyword)
    private String afterSalesStatus;

    @Field(name = "after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime afterSalesTime;

    @Field(name = "remark", type = FieldType.Text, analyzer = "ik_max_word")
    private String remark;

    // 采购信息字段 - 移除采购人员和供应商，从组品表实时获取

    @Field(name = "purchase_truck_fee", type = FieldType.Double)
    private BigDecimal purchaseTruckFee;

    @Field(name = "purchase_logistics_fee", type = FieldType.Double)
    private BigDecimal purchaseLogisticsFee;

    @Field(name = "purchase_other_fees", type = FieldType.Double)
    private BigDecimal purchaseOtherFees;

    @Field(name = "purchase_remark", type = FieldType.Text, analyzer = "ik_max_word")
    private String purchaseRemark;

    @Field(name = "purchase_after_sales_status", type = FieldType.Integer)
    private Integer purchaseAfterSalesStatus;

    @Field(name = "purchase_after_sales_amount", type = FieldType.Double)
    private BigDecimal purchaseAfterSalesAmount;

    @Field(name = "purchase_after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseAfterSalesTime;

    @Field(name = "purchase_audit_status", type = FieldType.Integer)
    private Integer purchaseAuditStatus;

    @Field(name = "purchase_approval_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseApprovalTime;

    @Field(name = "purchase_unapprove_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseUnapproveTime;

    // 销售信息字段
    @Field(name = "salesperson", type = FieldType.Keyword)
    private String salesperson;

    @Field(name = "customer_name", type = FieldType.Keyword)
    private String customerName;

    @Field(name = "transfer_person", type = FieldType.Keyword)
    private String transferPerson;

    @Field(name = "sale_truck_fee", type = FieldType.Double)
    private BigDecimal saleTruckFee;

    @Field(name = "sale_logistics_fee", type = FieldType.Double)
    private BigDecimal saleLogisticsFee;

    @Field(name = "sale_other_fees", type = FieldType.Double)
    private BigDecimal saleOtherFees;

    @Field(name = "sale_remark", type = FieldType.Text, analyzer = "ik_max_word")
    private String saleRemark;

    @Field(name = "sale_after_sales_status", type = FieldType.Integer)
    private Integer saleAfterSalesStatus;

    @Field(name = "sale_after_sales_amount", type = FieldType.Double)
    private BigDecimal saleAfterSalesAmount;

    @Field(name = "sale_after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime saleAfterSalesTime;

    @Field(name = "sale_audit_status", type = FieldType.Integer)
    private Integer saleAuditStatus;

    @Field(name = "sale_approval_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime saleApprovalTime;

    @Field(name = "sale_unapprove_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime saleUnapproveTime;

    // 审核总额字段
    @Field(name = "purchase_audit_total_amount", type = FieldType.Double)
    private BigDecimal purchaseAuditTotalAmount;

    @Field(name = "sale_audit_total_amount", type = FieldType.Double)
    private BigDecimal saleAuditTotalAmount;

    // 系统字段
    @Field(name = "create_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;

    @Field(name = "update_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updateTime;

    @Field(name = "creator", type = FieldType.Keyword)
    private String creator;

    @Field(name = "updater", type = FieldType.Keyword)
    private String updater;

    @Field(name = "tenant_id", type = FieldType.Long)
    private Long tenantId;

    @Field(name = "deleted", type = FieldType.Boolean)
    private Boolean deleted;
}