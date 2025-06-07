package cn.iocoder.yudao.module.erp.dal.dataobject.wholesale;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.time.LocalDateTime;

@Data
@Document(indexName = "erp_wholesale_base")
public class ErpWholesaleBaseESDO {

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

    @Field(name = "after_sales_status")
    private String afterSalesStatus;

    @Field(name = "after_sales_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime afterSalesTime;

    @Field(name = "remark", type = FieldType.Text)
    private String remark;

    @Field(name = "combo_product_id", type = FieldType.Long)
    private Long comboProductId;

    @Field(name = "product_name", type = FieldType.Text)
    private String productName;

    @Field(name = "shipping_code")
    private String shippingCode;

    @Field(name = "product_quantity", type = FieldType.Integer)
    private Integer productQuantity;

    @Field(name = "tenant_id", type = FieldType.Long)
    private Long tenantId;

    @Field(name = "deleted")
    private Boolean deleted;

    @Field(name = "status")
    private Integer status;

    @Field(name = "product_specification", type = FieldType.Text)
    private String productSpecification;

    @Field(name = "order_number", type = FieldType.Keyword)
    private String orderNumber;

    @Field(name = "logistics_number", type = FieldType.Keyword)
    private String logisticsNumber;

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
