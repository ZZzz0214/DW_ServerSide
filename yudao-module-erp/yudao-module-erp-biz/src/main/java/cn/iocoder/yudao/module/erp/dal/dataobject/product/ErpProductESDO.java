package cn.iocoder.yudao.module.erp.dal.dataobject.product;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "erp_products")
public class ErpProductESDO {

    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private String no;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String name;

    @Field(type = FieldType.Keyword)
    private String image;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String productShortName;

    @Field(type = FieldType.Keyword)
    private String shippingCode;

    @Field(type = FieldType.Long)
    private Long unitId;

    @Field(type = FieldType.Keyword)
    private String unitName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String standard;

    @Field(type = FieldType.Double)
    private BigDecimal weight;

    @Field(type = FieldType.Date)
    private LocalDateTime productionDate;

    @Field(type = FieldType.Integer)
    private Integer expiryDay;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String productSellingPoints;

    @Field(type = FieldType.Keyword)
    private String barCode;

    @Field(type = FieldType.Keyword)
    private String productRecord;

    @Field(type = FieldType.Keyword)
    private String executionCode;

    @Field(type = FieldType.Keyword)
    private String trademarkCode;

    @Field(type = FieldType.Integer)
    private Integer totalQuantity;

    @Field(type = FieldType.Integer)
    private Integer packagingMaterialQuantity;

    @Field(type = FieldType.Date)
    private LocalDateTime orderReplenishmentLeadTime;

    @Field(type = FieldType.Keyword)
    private String productDimensions;

    @Field(type = FieldType.Keyword)
    private String cartonSpecifications;

    @Field(type = FieldType.Keyword)
    private String cartonDimensions;

    @Field(type = FieldType.Double)
    private Double cartonWeight;

    @Field(type = FieldType.Keyword)
    private String shippingAddress;

    @Field(type = FieldType.Keyword)
    private String returnAddress;

    @Field(type = FieldType.Keyword)
    private String logisticsCompany;

    @Field(type = FieldType.Keyword)
    private String nonshippingArea;

    @Field(type = FieldType.Keyword)
    private String addonShippingArea;

    @Field(type = FieldType.Text)
    private String afterSalesStandard;

    @Field(type = FieldType.Text)
    private String afterSalesScript;

    @Field(type = FieldType.Keyword)
    private String orderReplenishmentLeadTimeUnit;

    @Field(type = FieldType.Keyword)
    private String productLength;

    @Field(type = FieldType.Keyword)
    private String productWidth;

    @Field(type = FieldType.Keyword)
    private String productHeight;

    @Field(type = FieldType.Keyword)
    private String productDimensionsUnit;

    @Field(type = FieldType.Keyword)
    private String cartonLength;

    @Field(type = FieldType.Keyword)
    private String cartonWidth;

    @Field(type = FieldType.Keyword)
    private String cartonHeight;

    @Field(type = FieldType.Keyword)
    private String cartonDimensionsUnit;

    @Field(type = FieldType.Keyword)
    private String cartonWeightUnit;

    @Field(type = FieldType.Keyword)
    private String productCartonSpec;

    @Field(type = FieldType.Keyword)
    private String productCartonSpecUnit;

    @Field(type = FieldType.Keyword)
    private String publicDomainEventMinimumPrice;

    @Field(type = FieldType.Keyword)
    private String liveStreamingEventMinimunPrice;

    @Field(type = FieldType.Keyword)
    private String pinduoduoEventMinimumPrice;

    @Field(type = FieldType.Keyword)
    private String alibabaEventMinimunPrice;

    @Field(type = FieldType.Keyword)
    private String groupBuyEventMinimunPrice;

    @Field(type = FieldType.Keyword)
    private String expiryUnit;

    @Field(type = FieldType.Keyword)
    private String weightUnit;

    @Field(type = FieldType.Keyword)
    private String purchaser;

    @Field(type = FieldType.Keyword)
    private String supplier;

    @Field(type = FieldType.Double)
    private BigDecimal purchasePrice;

    @Field(type = FieldType.Double)
    private BigDecimal wholesalePrice;

    @Field(type = FieldType.Text)
    private String remark;

    @Field(type = FieldType.Double)
    private BigDecimal minPurchasePrice;

    @Field(type = FieldType.Double)
    private BigDecimal minWholesalePrice;

    @Field(type = FieldType.Integer)
    private Integer shippingFeeType;

    @Field(type = FieldType.Double)
    private BigDecimal fixedShippingFee;

    @Field(type = FieldType.Integer)
    private Integer additionalItemQuantity;

    @Field(type = FieldType.Double)
    private BigDecimal additionalItemPrice;

    @Field(type = FieldType.Double)
    private BigDecimal firstWeight;

    @Field(type = FieldType.Double)
    private BigDecimal firstWeightPrice;

    @Field(type = FieldType.Double)
    private BigDecimal additionalWeight;

    @Field(type = FieldType.Double)
    private BigDecimal additionalWeightPrice;

    @Field(type = FieldType.Date)
    private LocalDateTime createTime;

    @Field(type = FieldType.Integer)
    private Integer count;
}
