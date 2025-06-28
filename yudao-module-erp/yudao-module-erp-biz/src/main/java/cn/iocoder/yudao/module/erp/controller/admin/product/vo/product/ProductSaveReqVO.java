package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 产品新增/修改 Request VO")
@Data
public class ProductSaveReqVO {

    @Schema(description = "产品编号", example = "15672")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "产品名称不能为空")
    private String name;

    @Schema(description = "产品图片")
    private String image;

    @Schema(description = "产品简称")
    private String productShortName;

    @Schema(description = "发货编码")
    private String shippingCode;

    @Schema(description = "单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8869")
    private Long unitId;

    @Schema(description = "产品规格")
    private String standard;

    @Schema(description = "产品重量（单位：kg）")
    private BigDecimal weight;

    @Schema(description = "产品日期")
    private LocalDateTime productionDate;

    @Schema(description = "保质日期")
    private Integer expiryDay;

    @Schema(description = "品牌名称")
    private String brand;

    @Schema(description = "产品品类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11161")
    @NotNull(message = "产品品类编号不能为空")
    private Long categoryId;

    @Schema(description = "产品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "产品状态不能为空")
    private Integer status;

    @Schema(description = "产品卖点")
    private String productSellingPoints;

    @Schema(description = "条形编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "X110")
    @NotEmpty(message = "条形编号不能为空")
    private String barCode;

    @Schema(description = "备案编号")
    private String productRecord;

    @Schema(description = "执行编号")
    private String executionCode;

    @Schema(description = "商标编号")
    private String trademarkCode;

    @Schema(description = "现货数量")
    private Integer totalQuantity;

    @Schema(description = "包材数量")
    private Integer packagingMaterialQuantity;

    @Schema(description = "返单时效")
    private String orderReplenishmentLeadTime;

    @Schema(description = "箱规数量")
    private String cartonSpecifications;


    @Schema(description = "箱规重量")
    private String cartonWeight;

    @Schema(description = "发货地址")
    private String shippingAddress;

    @Schema(description = "退货地址")
    private String returnAddress;

    @Schema(description = "快递公司")
    private String logisticsCompany;

    @Schema(description = "不发货区")
    private String nonshippingArea;

    @Schema(description = "加邮地区")
    private String addonShippingArea;

    @Schema(description = "售后标准")
    private String afterSalesStandard;

    @Schema(description = "售后话术")
    private String afterSalesScript;

    @Schema(description = "返单时效单位")
    private String orderReplenishmentLeadTimeUnit;

    @Schema(description = "品长宽高")
    private String productLength;

    @Schema(description = "箱长宽高")
    private String cartonLength;

    @Schema(description = "产品箱规")
    private String productCartonSpec;

    @Schema(description = "公域活动最低价")
    private String publicDomainEventMinimumPrice;

    @Schema(description = "直播活动最低价")
    private String liveStreamingEventMinimunPrice;

    @Schema(description = "拼多多活动最低价")
    private String pinduoduoEventMinimumPrice;

    @Schema(description = "阿里巴巴活动最低价")
    private String alibabaEventMinimunPrice;

    @Schema(description = "团购活动最低价")
    private String groupBuyEventMinimunPrice;


    @Schema(description = "采购人员")
    private String purchaser;

    @Schema(description = "供应商名")
    private String supplier;

    @Schema(description = "采购单价（单位：元）")
    private BigDecimal purchasePrice;

    @Schema(description = "批发单价（单位：元）")
    private BigDecimal wholesalePrice;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "对外最低采购单价（单位：元）")
    private String minPurchasePrice;

//    @Schema(description = "对外最低批发单价（单位：元）")
//    private BigDecimal minWholesalePrice;

    @Schema(description = "运费类型（0：固定运费，1：按件计费，2：按重计费）")
    private Integer shippingFeeType;

    @Schema(description = "固定运费（单位：元）")
    private BigDecimal fixedShippingFee;

//    @Schema(description = "首件数量")
//    private Integer firstItemQuantity;
//
//    @Schema(description = "首件价格（单位：元）")
//    private BigDecimal firstItemPrice;

    @Schema(description = "按件数量")
    private Integer additionalItemQuantity;

    @Schema(description = "按件价格（单位：元）")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：kg）")
    private BigDecimal firstWeight;

    @Schema(description = "首重价格（单位：元）")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：kg）")
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格（单位：元）")
    private BigDecimal additionalWeightPrice;
}
