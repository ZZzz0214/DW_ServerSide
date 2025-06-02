package cn.iocoder.yudao.module.erp.dal.dataobject.product;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;


@Data
@Document(indexName = "erp_combo_products")
public class ErpComboProductES {

    @Id
    private Long id;

    @Field(name = "no", type = FieldType.Keyword)
    private String no;

    //@Field(name = "name", type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    @Field(name = "name", type = FieldType.Text, analyzer = "ik_max_word")
    private String name;

    @Field(name = "short_name", type = FieldType.Text, analyzer = "ik_smart")
    private String shortName;

    @Field(name = "image", type = FieldType.Keyword, index = false)
    private String image;

    @Field(name = "shipping_code", type = FieldType.Keyword)
    private String shippingCode;

    @Field(name = "weight", type = FieldType.Double)
    private BigDecimal weight;

    @Field(name = "purchaser", type = FieldType.Keyword)
    private String purchaser;

    @Field(name = "supplier", type = FieldType.Keyword)
    private String supplier;

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

    // 组合名称（用于搜索）
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String comboName;

    // 租户ID（用于多租户隔离）
    @Field(name = "tenant_id", type = FieldType.Long)
    private Long tenantId;
}
