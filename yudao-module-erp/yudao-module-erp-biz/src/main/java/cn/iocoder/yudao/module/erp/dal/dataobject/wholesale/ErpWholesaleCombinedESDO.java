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
    
    @Field(name = "no_keyword", type = FieldType.Keyword)
    private String noKeyword;

    // 物流单号
    @Field(name = "logistics_number", type = FieldType.Text, analyzer = "ik_max_word")
    private String logisticsNumber;
    
    @Field(name = "logistics_number_keyword", type = FieldType.Keyword)
    private String logisticsNumberKeyword;

    // 收件人信息
    @Field(name = "receiver_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String receiverName;
    
    @Field(name = "receiver_name_keyword", type = FieldType.Keyword)
    private String receiverNameKeyword;

    @Field(name = "receiver_phone", type = FieldType.Text, analyzer = "ik_max_word")
    private String receiverPhone;
    
    @Field(name = "receiver_phone_keyword", type = FieldType.Keyword)
    private String receiverPhoneKeyword;

    @Field(name = "receiver_address", type = FieldType.Text, analyzer = "ik_max_word")
    private String receiverAddress;
    
    @Field(name = "receiver_address_keyword", type = FieldType.Keyword)
    private String receiverAddressKeyword;

    // 组品信息
    @Field(name = "combo_product_id", type = FieldType.Long)
    private Long comboProductId;
    
    // 组品编号
    @Field(name = "combo_product_no", type = FieldType.Keyword)
    private String comboProductNo;
    
    @Field(name = "combo_product_no_keyword", type = FieldType.Keyword)
    private String comboProductNoKeyword;

    // 发货编码
    @Field(name = "shipping_code", type = FieldType.Text, analyzer = "ik_max_word")
    private String shippingCode;
    
    @Field(name = "shipping_code_keyword", type = FieldType.Keyword)
    private String shippingCodeKeyword;

    // 产品信息
    @Field(name = "product_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String productName;
    
    @Field(name = "product_name_keyword", type = FieldType.Keyword)
    private String productNameKeyword;

    @Field(name = "product_specification", type = FieldType.Text, analyzer = "ik_max_word")
    private String productSpecification;
    
    @Field(name = "product_specification_keyword", type = FieldType.Keyword)
    private String productSpecificationKeyword;

    @Field(name = "product_quantity", type = FieldType.Integer)
    private Integer productQuantity;

    // 售后信息
    @Field(name = "after_sales_status", type = FieldType.Text, analyzer = "ik_max_word")
    private String afterSalesStatus;
    
    @Field(name = "after_sales_status_keyword", type = FieldType.Keyword)
    private String afterSalesStatusKeyword;

    @Field(name = "after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime afterSalesTime;

    @Field(name = "remark", type = FieldType.Text, analyzer = "ik_max_word")
    private String remark;

    // 采购信息字段
    @Field(name = "purchaser", type = FieldType.Text, analyzer = "ik_max_word")
    private String purchaser;
    
    @Field(name = "purchaser_keyword", type = FieldType.Keyword)
    private String purchaserKeyword;

    @Field(name = "supplier", type = FieldType.Text, analyzer = "ik_max_word")
    private String supplier;
    
    @Field(name = "supplier_keyword", type = FieldType.Keyword)
    private String supplierKeyword;

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
    @Field(name = "salesperson", type = FieldType.Text, analyzer = "ik_max_word")
    private String salesperson;
    
    @Field(name = "salesperson_keyword", type = FieldType.Keyword)
    private String salespersonKeyword;

    @Field(name = "customer_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String customerName;
    
    @Field(name = "customer_name_keyword", type = FieldType.Keyword)
    private String customerNameKeyword;

    @Field(name = "transfer_person", type = FieldType.Text, analyzer = "ik_max_word")
    private String transferPerson;
    
    @Field(name = "transfer_person_keyword", type = FieldType.Keyword)
    private String transferPersonKeyword;

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

    // 系统字段
    @Field(name = "create_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;

    @Field(name = "update_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updateTime;

    @Field(name = "creator", type = FieldType.Text, analyzer = "ik_max_word")
    private String creator;
    
    @Field(name = "creator_keyword", type = FieldType.Keyword)
    private String creatorKeyword;

    @Field(name = "updater", type = FieldType.Text, analyzer = "ik_max_word")
    private String updater;
    
    @Field(name = "updater_keyword", type = FieldType.Keyword)
    private String updaterKeyword;

    @Field(name = "tenant_id", type = FieldType.Long)
    private Long tenantId;

    @Field(name = "deleted", type = FieldType.Boolean)
    private Boolean deleted;
}