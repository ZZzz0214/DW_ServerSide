package cn.iocoder.yudao.module.erp.dal.dataobject.product;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Document(indexName = "erp_combo_products")
public class ErpComboProductES {

    @Id
    private Long id;

    @Field(name = "no", type = FieldType.Keyword)
    private String no;

    @Field(name = "no_keyword", type = FieldType.Keyword)
    private String noKeyword;

    //@Field(name = "name", type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    @Field(name = "name", type = FieldType.Keyword)
    private String name;

    @Field(name = "name_keyword", type = FieldType.Keyword)
    private String nameKeyword;

    // 🔥 新增：原始名称字段，用于支持对原始名称的搜索
    @Field(name = "original_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String originalName;

    @Field(name = "original_name_keyword", type = FieldType.Keyword)
    private String originalNameKeyword;

    @Field(name = "short_name", type = FieldType.Text, analyzer = "ik_smart")
    private String shortName;

    @Field(name = "short_name_keyword", type = FieldType.Keyword)
    private String shortNameKeyword;

    @Field(name = "image", type = FieldType.Keyword, index = false)
    private String image;

    @Field(name = "shipping_code", type = FieldType.Keyword)
    private String shippingCode;

    @Field(name = "shipping_code_keyword", type = FieldType.Keyword)
    private String shippingCodeKeyword;

    @Field(name = "weight", type = FieldType.Double)
    private BigDecimal weight;

    @Field(name = "purchaser", type = FieldType.Text, analyzer = "ik_max_word")
    private String purchaser;

    @Field(name = "purchaser_keyword", type = FieldType.Keyword)
    private String purchaserKeyword;

    @Field(name = "supplier", type = FieldType.Text, analyzer = "ik_max_word")
    private String supplier;

    @Field(name = "supplier_keyword", type = FieldType.Keyword)
    private String supplierKeyword;

    @Field(name = "purchase_price", type = FieldType.Double)
    private BigDecimal purchasePrice;

    @Field(name = "wholesale_price", type = FieldType.Double)
    private BigDecimal wholesalePrice;

    @Field(name = "remark", type = FieldType.Text, analyzer = "ik_max_word")
    private String remark;

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

    @Field(name = "total_quantity", type = FieldType.Integer)
    private Integer totalQuantity;

    @Field(name = "status", type = FieldType.Integer)
    private Integer status;

    // 租户ID（用于多租户隔离）
    @Field(name = "tenant_id", type = FieldType.Long)
    private Long tenantId;


    @Field(name = "create_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;

    @Field(name = "update_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updateTime;

    @Field(name = "creator", type = FieldType.Text, analyzer = "ik_max_word")
    private String creator;

    @Field(name = "creator_keyword", type = FieldType.Keyword)
    private String creatorKeyword;

    @Field(name = "updater", type = FieldType.Keyword)
    private String updater;

    //用于查找name的唯一
    @Field(type = FieldType.Keyword)
    private String normalizedName;
}
