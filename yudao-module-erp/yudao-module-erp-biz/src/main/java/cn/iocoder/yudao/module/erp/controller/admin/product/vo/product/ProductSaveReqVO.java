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

    @Schema(description = "产品条码", requiredMode = Schema.RequiredMode.REQUIRED, example = "X110")
    @NotEmpty(message = "产品条码不能为空")
    private String barCode;

    @Schema(description = "产品图片")
    private String image;

    @Schema(description = "产品简称")
    private String productShortName;

    @Schema(description = "发货编码")
    private String shippingCode;

    @Schema(description = "备案编号")
    private String productRecord;

    @Schema(description = "执行编号")
    private String executionCode;

    @Schema(description = "商标编号")
    private String trademarkCode;

    @Schema(description = "产品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11161")
    @NotNull(message = "产品分类编号不能为空")
    private Long categoryId;

    @Schema(description = "单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8869")
    @NotNull(message = "单位编号不能为空")
    private Long unitId;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "产品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "产品状态不能为空")
    private Integer status;

    @Schema(description = "产品备注")
    private String remark;

    @Schema(description = "产品卖点")
    private String productSellingPoints;

    @Schema(description = "产品规格")
    private String standard;

    @Schema(description = "产品重量（kg）")
    private BigDecimal weight;

    @Schema(description = "产品长宽高")
    private String productDimensions;

    @Schema(description = "箱规")
    private String cartonSpecifications;

    @Schema(description = "箱规长宽高")
    private String cartonDimensions;

    @Schema(description = "箱规重量")
    private Double cartonWeight;

    @Schema(description = "现货数量")
    private Integer availableStockQuantity;

    @Schema(description = "包材数量")
    private Integer packagingMaterialQuantity;

    @Schema(description = "返单时效")
    private LocalDateTime orderReplenishmentLeadTime;

    @Schema(description = "发货地址")
    private String shippingAddress;

    @Schema(description = "退货地址")
    private String returnAddress;

    @Schema(description = "物流公司")
    private String logisticsCompany;

    @Schema(description = "不发货地")
    private String nonshippingArea;

    @Schema(description = "加邮区域")
    private String addonShippingArea;

    @Schema(description = "售后标准")
    private String afterSalesStandard;

    @Schema(description = "售后话术")
    private String afterSalesScript;

    @Schema(description = "公域活动最低价")
    private BigDecimal publicDomainEventMinimumPrice;

    @Schema(description = "直播活动最低价")
    private BigDecimal liveStreamingEventMinimunPrice;

    @Schema(description = "拼多多活动最低价")
    private BigDecimal pinduoduoEventMinimumPrice;

    @Schema(description = "阿里巴巴活动最低价")
    private BigDecimal alibabaEventMinimunPrice;

    @Schema(description = "团购活动最低价")
    private BigDecimal groupBuyEventMinimunPrice;

    @Schema(description = "供应商")
    private String supplier;

    @Schema(description = "代发单价")
    private BigDecimal dropshippingUnitPrice;

    @Schema(description = "批发单价")
    private BigDecimal wholesaleUnitPrice;

    @Schema(description = "基础运费")
    private BigDecimal baseShippingFee;

    @Schema(description = "采购详情")
    private String purchaseDetails;

    @Schema(description = "采购备注")
    private String purchaseNote;

    @Schema(description = "销售详情")
    private String salesDetails;

    @Schema(description = "销售备注")
    private String salesNote;

    @Schema(description = "一级代发单价")
    private BigDecimal levelOneDropshippingPrice;

    @Schema(description = "二级代发单价")
    private BigDecimal levelTwoDropshippingPrice;

    @Schema(description = "一级批发单价")
    private BigDecimal levelOneWholesalePrice;

    @Schema(description = "二级批发单价")
    private BigDecimal levelTwoWholesalePrice;

    @Schema(description = "代发运费类型")
    private Integer shippingFeeType;

    @Schema(description = "固定运费")
    private BigDecimal fixedShippingFee;

    @Schema(description = "首件数量")
    private Integer firstItemQuantity;

    @Schema(description = "首件价格")
    private BigDecimal firstItemPrice;

    @Schema(description = "续件数量")
    private Integer additionalItemQuantity;

    @Schema(description = "续件价格")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量")
    private BigDecimal firstWeight;

    @Schema(description = "首重价格")
    private BigDecimal firstWeightPrice;

    @Schema(description = "首批生产日期")
    private LocalDateTime productionDate;

    @Schema(description = "续重重量")
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格")
    private BigDecimal additionalWeightPrice;

    @Schema(description = "保质期天数", example = "10")
    private Integer expiryDay;

    @Schema(description = "采购价格，单位：元", example = "10.30")
    private BigDecimal purchasePrice;

//    @Schema(description = "销售价格，单位：元", example = "74.32")
//    private BigDecimal salePrice;
//
//    @Schema(description = "最低价格，单位：元", example = "161.87")
//    private BigDecimal minPrice;
}