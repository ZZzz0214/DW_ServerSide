package cn.iocoder.yudao.module.erp.dal.dataobject.sale;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.InnerField;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "erp_sale_price")
public class ErpSalePriceESDO {

    @Id
    private Long id;

    @MultiField(
        mainField = @Field(name = "no", type = FieldType.Text, analyzer = "ik_max_word"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
        }
    )
    private String no;

    @Field(name = "group_product_id", type = FieldType.Long)
    private Long groupProductId;

    @Field(name = "product_image", type = FieldType.Keyword, index = false)
    private String productImage;

    @Field(name = "product_name", type = FieldType.Keyword)
    private String productName;

    @Field(name = "product_name_keyword", type = FieldType.Keyword)
    private String productNameKeyword;

    @Field(name = "product_short_name", type = FieldType.Keyword)
    private String productShortName;

    @Field(name = "product_short_name_keyword", type = FieldType.Keyword)
    private String productShortNameKeyword;

    @MultiField(
        mainField = @Field(name = "customer_name", type = FieldType.Text, analyzer = "ik_max_word"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
        }
    )
    private String customerName;

    @Field(name = "distribution_price", type = FieldType.Double)
    private BigDecimal distributionPrice;

    @Field(name = "wholesale_price", type = FieldType.Double)
    private BigDecimal wholesalePrice;

    @Field(name = "remark", type = FieldType.Text, analyzer = "ik_max_word")
    private String remark;

    @Field(name = "shipping_fee_type", type = FieldType.Integer)
    private Integer shippingFeeType;

    @Field(name = "fixed_shipping_fee", type = FieldType.Double)
    private BigDecimal fixedShippingFee;

    @Field(name = "first_item_quantity", type = FieldType.Integer)
    private Integer firstItemQuantity;

    @Field(name = "first_item_price", type = FieldType.Double)
    private BigDecimal firstItemPrice;

    @Field(name = "additional_item_quantity", type = FieldType.Integer)
    private Integer additionalItemQuantity;

    @Field(name = "additional_item_price", type = FieldType.Double)
    private BigDecimal additionalItemPrice;

    @Field(name = "first_weight", type = FieldType.Double)
    private BigDecimal firstWeight;

    @Field(name = "first_weight_price", type = FieldType.Double)
    private BigDecimal firstWeightPrice;

    @Field(name = "additional_weight", type = FieldType.Double)
    private BigDecimal additionalWeight;

    @Field(name = "additional_weight_price", type = FieldType.Double)
    private BigDecimal additionalWeightPrice;

    @Field(name = "tenant_id", type = FieldType.Long)
    private Long tenantId;
    
    @Field(name = "create_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;
    
    @Field(name = "update_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updateTime;
    
    @MultiField(
        mainField = @Field(name = "creator", type = FieldType.Text, analyzer = "ik_max_word"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
        }
    )
    private String creator;
    
    @Field(name = "updater", type = FieldType.Keyword)
    private String updater;
}