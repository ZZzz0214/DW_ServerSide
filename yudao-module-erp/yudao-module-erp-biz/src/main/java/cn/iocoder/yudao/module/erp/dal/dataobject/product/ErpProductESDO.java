package cn.iocoder.yudao.module.erp.dal.dataobject.product;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "erp_products")
public class ErpProductESDO {

    @Id
    private Long id;

    @Field(name = "no", type = FieldType.Keyword)
    private String no;

    @Field(name = "name", type = FieldType.Keyword)
    private String name;

    @Field(name = "image", type = FieldType.Keyword)
    private String image;

    @Field(name = "product_short_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String productShortName;

    @Field(name = "shipping_code", type = FieldType.Keyword)
    private String shippingCode;

    @Field(name = "unit_id", type = FieldType.Long)
    private Long unitId;

    @Field(name = "unit_name", type = FieldType.Keyword)
    private String unitName;

    @Field(name = "standard", type = FieldType.Text, analyzer = "ik_max_word")
    private String standard;

    @Field(name = "weight", type = FieldType.Double)
    private BigDecimal weight;

    @Field(name = "production_date", type = FieldType.Date,
    format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime productionDate;

    @Field(name = "expiry_day", type = FieldType.Integer)
    private Integer expiryDay;

    @Field(name = "brand", type = FieldType.Keyword)
    private String brand;

    @Field(name = "category_id", type = FieldType.Long)
    private Long categoryId;

    @Field(name = "category_name", type = FieldType.Keyword)
    private String categoryName;

    @Field(name = "status", type = FieldType.Integer)
    private Integer status;

    @Field(name = "product_selling_points", type = FieldType.Text, analyzer = "ik_max_word")
    private String productSellingPoints;

    @Field(name = "bar_code", type = FieldType.Keyword)
    private String barCode;

    @Field(name = "product_record", type = FieldType.Keyword)
    private String productRecord;

    @Field(name = "execution_code", type = FieldType.Keyword)
    private String executionCode;

    @Field(name = "trademark_code", type = FieldType.Keyword)
    private String trademarkCode;

    @Field(name = "total_quantity", type = FieldType.Integer)
    private Integer totalQuantity;

    @Field(name = "packaging_material_quantity", type = FieldType.Integer)
    private Integer packagingMaterialQuantity;

    @Field(name = "production_date", type = FieldType.Text)
    private String orderReplenishmentLeadTime;

    @Field(name = "product_dimensions", type = FieldType.Keyword)
    private String productDimensions;

    @Field(name = "carton_specifications", type = FieldType.Keyword)
    private String cartonSpecifications;

    @Field(name = "carton_dimensions", type = FieldType.Keyword)
    private String cartonDimensions;

    @Field(name = "carton_weight", type = FieldType.Double)
    private Double cartonWeight;

    @Field(name = "shipping_address", type = FieldType.Keyword)
    private String shippingAddress;

    @Field(name = "return_address", type = FieldType.Keyword)
    private String returnAddress;

    @Field(name = "logistics_company", type = FieldType.Keyword)
    private String logisticsCompany;

    @Field(name = "nonshipping_area", type = FieldType.Keyword)
    private String nonshippingArea;

    @Field(name = "addon_shipping_area", type = FieldType.Keyword)
    private String addonShippingArea;

    @Field(name = "after_sales_standard", type = FieldType.Text)
    private String afterSalesStandard;

    @Field(name = "after_sales_script", type = FieldType.Text)
    private String afterSalesScript;

    @Field(name = "order_replenishment_lead_time_unit", type = FieldType.Keyword)
    private String orderReplenishmentLeadTimeUnit;

    @Field(name = "product_length", type = FieldType.Keyword)
    private String productLength;

    @Field(name = "product_width", type = FieldType.Keyword)
    private String productWidth;

    @Field(name = "product_height", type = FieldType.Keyword)
    private String productHeight;

    @Field(name = "product_dimensions_unit", type = FieldType.Keyword)
    private String productDimensionsUnit;

    @Field(name = "carton_length", type = FieldType.Keyword)
    private String cartonLength;

    @Field(name = "carton_width", type = FieldType.Keyword)
    private String cartonWidth;

    @Field(name = "carton_height", type = FieldType.Keyword)
    private String cartonHeight;

    @Field(name = "carton_dimensions_unit", type = FieldType.Keyword)
    private String cartonDimensionsUnit;

    @Field(name = "carton_weight_unit", type = FieldType.Keyword)
    private String cartonWeightUnit;

    @Field(name = "product_carton_spec", type = FieldType.Keyword)
    private String productCartonSpec;

    @Field(name = "product_carton_spec_unit", type = FieldType.Keyword)
    private String productCartonSpecUnit;

    @Field(name = "public_domain_event_minimum_price", type = FieldType.Keyword)
    private String publicDomainEventMinimumPrice;

    @Field(name = "live_streaming_event_minimun_price", type = FieldType.Keyword)
    private String liveStreamingEventMinimunPrice;

    @Field(name = "pinduoduo_event_minimum_price", type = FieldType.Keyword)
    private String pinduoduoEventMinimumPrice;

    @Field(name = "alibaba_event_minimun_price", type = FieldType.Keyword)
    private String alibabaEventMinimunPrice;

    @Field(name = "group_buy_event_minimun_price", type = FieldType.Keyword)
    private String groupBuyEventMinimunPrice;

    @Field(name = "expiry_unit", type = FieldType.Keyword)
    private String expiryUnit;

    @Field(name = "weight_unit", type = FieldType.Keyword)
    private String weightUnit;

    @Field(name = "purchaser", type = FieldType.Keyword)
    private String purchaser;

    @Field(name = "supplier", type = FieldType.Keyword)
    private String supplier;

    @Field(name = "purchase_price", type = FieldType.Double)
    private BigDecimal purchasePrice;

    @Field(name = "wholesale_price", type = FieldType.Double)
    private BigDecimal wholesalePrice;

    @Field(name = "remark", type = FieldType.Text)
    private String remark;

    @Field(name = "min_purchase_price", type = FieldType.Double)
    private BigDecimal minPurchasePrice;

    @Field(name = "min_wholesale_price", type = FieldType.Double)
    private BigDecimal minWholesalePrice;

    @Field(name = "shipping_fee_type", type = FieldType.Integer)
    private Integer shippingFeeType;

    @Field(name = "fixed_shipping_fee", type = FieldType.Double)
    private BigDecimal fixedShippingFee;

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

    @Field(name = "production_date", type = FieldType.Date,
            format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Field(name = "count", type = FieldType.Integer)
    private Integer count;
}
