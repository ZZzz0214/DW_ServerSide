package cn.iocoder.yudao.module.erp.dal.dataobject.distribution;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "erp_distribution_combined")
public class ErpDistributionCombinedESDO {

    @Id
    private Long id;

    @Field(name = "no", type = FieldType.Keyword)
    private String no;

    @Field(name = "no_keyword", type = FieldType.Keyword)
    private String noKeyword;

    @Field(name = "order_number", type = FieldType.Text, analyzer = "ik_max_word")
    private String orderNumber;

    @Field(name = "order_number_keyword", type = FieldType.Keyword)
    private String orderNumberKeyword;

    @Field(name = "logistics_company", type = FieldType.Text, analyzer = "ik_max_word")
    private String logisticsCompany;

    @Field(name = "logistics_company_keyword", type = FieldType.Keyword)
    private String logisticsCompanyKeyword;

    @Field(name = "tracking_number", type = FieldType.Text, analyzer = "ik_max_word")
    private String trackingNumber;

    @Field(name = "tracking_number_keyword", type = FieldType.Keyword)
    private String trackingNumberKeyword;

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

    @Field(name = "original_product_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String originalProductName;

    @Field(name = "original_product_keyword", type = FieldType.Keyword)
    private String originalProductKeyword;

    @Field(name = "original_standard", type = FieldType.Text, analyzer = "ik_max_word")
    private String originalStandard;

    @Field(name = "original_standard_keyword", type = FieldType.Keyword)
    private String originalStandardKeyword;

    @Field(name = "original_quantity", type = FieldType.Integer)
    private Integer originalQuantity;

    @Field(name = "remark", type = FieldType.Text, analyzer = "ik_max_word")
    private String remark;

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

    // 产品名称
    @Field(name = "product_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String productName;

    @Field(name = "product_name_keyword", type = FieldType.Keyword)
    private String productNameKeyword;

    @Field(name = "product_specification", type = FieldType.Text, analyzer = "ik_max_word")
    private String productSpecification;

    @Field(name = "product_specification_keyword", type = FieldType.Keyword)
    private String productSpecificationKeyword;

    // 采购人员
    @Field(name = "purchaser", type = FieldType.Text, analyzer = "ik_max_word")
    private String purchaser;

    @Field(name = "purchaser_keyword", type = FieldType.Keyword)
    private String purchaserKeyword;

    // 供应商名称
    @Field(name = "supplier", type = FieldType.Text, analyzer = "ik_max_word")
    private String supplier;

    @Field(name = "supplier_keyword", type = FieldType.Keyword)
    private String supplierKeyword;

    @Field(name = "product_quantity", type = FieldType.Integer)
    private Integer productQuantity;

    @Field(name = "after_sales_status", type = FieldType.Text, analyzer = "ik_max_word")
    private String afterSalesStatus;

    @Field(name = "after_sales_status_keyword", type = FieldType.Keyword)
    private String afterSalesStatusKeyword;

    @Field(name = "after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime afterSalesTime;

    // 采购相关字段
    @Field(name = "purchase_other_fees", type = FieldType.Double)
    private BigDecimal purchaseOtherFees;

    @Field(name = "purchase_remark", type = FieldType.Text, analyzer = "ik_max_word")
    private String purchaseRemark;

    @Field(name = "purchase_after_sales_status", type = FieldType.Integer)
    private Integer purchaseAfterSalesStatus;

    @Field(name = "purchase_after_sales_amount", type = FieldType.Double)
    private BigDecimal purchaseAfterSalesAmount;

    @Field(name = "purchase_approval_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseApprovalTime;

    @Field(name = "purchase_after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseAfterSalesTime;

    @Field(name = "purchase_audit_status", type = FieldType.Integer)
    private Integer purchaseAuditStatus;

    @Field(name = "purchase_unapprove_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseUnapproveTime;

    // 销售相关字段
    @Field(name = "salesperson", type = FieldType.Text, analyzer = "ik_max_word")
    private String salesperson;

    @Field(name = "salesperson_keyword", type = FieldType.Keyword)
    private String salespersonKeyword;

    @Field(name = "customer_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String customerName;

    @Field(name = "customer_name_keyword", type = FieldType.Keyword)
    private String customerNameKeyword;

    @Field(name = "sale_other_fees", type = FieldType.Double)
    private BigDecimal saleOtherFees;

    @Field(name = "sale_remark", type = FieldType.Text, analyzer = "ik_max_word")
    private String saleRemark;

    @Field(name = "transfer_person", type = FieldType.Text, analyzer = "ik_max_word")
    private String transferPerson;

    @Field(name = "transfer_person_keyword", type = FieldType.Keyword)
    private String transferPersonKeyword;

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

    @Field(name = "deleted", type = FieldType.Long)
    private Boolean deleted;
}
