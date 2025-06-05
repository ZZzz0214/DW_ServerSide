package cn.iocoder.yudao.module.erp.dal.dataobject.distribution;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.time.LocalDateTime;

@Data
@Document(indexName = "erp_distribution_base")
public class ErpDistributionBaseESDO {

    @Id
    private Long id;

    @Field(name = "no", type = FieldType.Keyword)
    private String no;

    @Field(name = "logistics_company", type = FieldType.Keyword)
    private String logisticsCompany;

    @Field(name = "tracking_number", type = FieldType.Keyword)
    private String trackingNumber;

    @Field(name = "receiver_name", type = FieldType.Keyword)
    private String receiverName;

    @Field(name = "receiver_phone", type = FieldType.Keyword)
    private String receiverPhone;

    @Field(name = "receiver_address", type = FieldType.Text)
    private String receiverAddress;

    @Field(name = "original_product_name", type = FieldType.Text)
    private String originalProductName;

    @Field(name = "original_standard", type = FieldType.Text)
    private String originalStandard;

    @Field(name = "original_quantity", type = FieldType.Integer)
    private Integer originalQuantity;

    @Field(name = "after_sales_status", type = FieldType.Keyword)
    private String afterSalesStatus;

    @Field(name = "after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime afterSalesTime;

    @Field(name = "remark", type = FieldType.Text)
    private String remark;

    @Field(name = "combo_product_id", type = FieldType.Long)
    private Long comboProductId;

    @Field(name = "product_name", type = FieldType.Text)
    private String productName;

    @Field(name = "shipping_code", type = FieldType.Keyword)
    private String shippingCode;

    @Field(name = "product_quantity", type = FieldType.Integer)
    private Integer productQuantity;

    @Field(name = "tenant_id", type = FieldType.Long)
    private Long tenantId;

    @Field(name = "deleted", type = FieldType.Boolean)
    private Boolean deleted;

    @Field(name = "status", type = FieldType.Integer)
    private Integer status;

    @Field(name = "product_specification", type = FieldType.Text)
    private String productSpecification;

    @Field(name = "order_number", type = FieldType.Keyword)
    private String orderNumber;

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