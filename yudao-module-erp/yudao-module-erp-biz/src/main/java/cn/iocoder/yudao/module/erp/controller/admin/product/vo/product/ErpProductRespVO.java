package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 产品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpProductRespVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15672")
    @ExcelProperty("产品编号")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("产品名称")
    private String name;

    @Schema(description = "产品图片")
    @ExcelProperty("产品图片")
    private String image;

    @Schema(description = "产品简称")
    @ExcelProperty("产品简称")
    private String productShortName;

    @Schema(description = "发货编码")
    @ExcelProperty("发货编码")
    private String shippingCode;

    @Schema(description = "单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8869")
    private Long unitId;

    @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "个")
    @ExcelProperty("单位")
    private String unitName;

    @Schema(description = "产品规格")
    @ExcelProperty("产品规格")
    private String standard;

    @Schema(description = "产品重量（单位：kg）")
    @ExcelProperty("产品重量（单位：kg）")
    private BigDecimal weight;

    @Schema(description = "产品日期")
    @ExcelProperty("产品日期")
    private LocalDateTime productionDate;

    @Schema(description = "保质期天数")
    @ExcelProperty("保质期天数")
    private Integer expiryDay;

    @Schema(description = "品牌名称")
    @ExcelProperty("品牌名称")
    private String brand;

    @Schema(description = "产品品类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11161")
    private Long categoryId;

    @Schema(description = "产品分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "水果")
    @ExcelProperty("产品分类")
    private String categoryName;

    @Schema(description = "产品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("产品状态")
    private Integer status;

    @Schema(description = "产品卖点")
    @ExcelProperty("产品卖点")
    private String productSellingPoints;

    @Schema(description = "条形编号")
    @ExcelProperty("条形编号")
    private String barCode;

    @Schema(description = "备案编号")
    @ExcelProperty("备案编号")
    private String productRecord;

    @Schema(description = "执行编号")
    @ExcelProperty("执行编号")
    private String executionCode;

    @Schema(description = "商标编号")
    @ExcelProperty("商标编号")
    private String trademarkCode;

    @Schema(description = "现货数量")
    @ExcelProperty("现货数量")
    private Integer totalQuantity;

    @Schema(description = "包材数量")
    @ExcelProperty("包材数量")
    private Integer packagingMaterialQuantity;

    @Schema(description = "返单时效")
    @ExcelProperty("返单时效")
    private LocalDateTime orderReplenishmentLeadTime;

    @Schema(description = "产品长宽高")
    @ExcelProperty("产品长宽高")
    private String productDimensions;

    @Schema(description = "产品箱规")
    @ExcelProperty("产品箱规")
    private String cartonSpecifications;

    @Schema(description = "箱长宽高")
    @ExcelProperty("箱长宽高")
    private String cartonDimensions;

    @Schema(description = "箱规重量")
    @ExcelProperty("箱规重量")
    private Double cartonWeight;

    @Schema(description = "发货地址")
    @ExcelProperty("发货地址")
    private String shippingAddress;

    @Schema(description = "退货地址")
    @ExcelProperty("退货地址")
    private String returnAddress;

    @Schema(description = "快递公司")
    @ExcelProperty("快递公司")
    private String logisticsCompany;

    @Schema(description = "不发货区")
    @ExcelProperty("不发货区")
    private String nonshippingArea;

    @Schema(description = "加邮地区")
    @ExcelProperty("加邮地区")
    private String addonShippingArea;

    @Schema(description = "售后标准")
    @ExcelProperty("售后标准")
    private String afterSalesStandard;

    @Schema(description = "售后话术")
    @ExcelProperty("售后话术")
    private String afterSalesScript;

    @Schema(description = "公域活动最低价")
    @ExcelProperty("公域活动最低价")
    private BigDecimal publicDomainEventMinimumPrice;

    @Schema(description = "直播活动最低价")
    @ExcelProperty("直播活动最低价")
    private BigDecimal liveStreamingEventMinimunPrice;

    @Schema(description = "拼多多活动最低价")
    @ExcelProperty("拼多多活动最低价")
    private BigDecimal pinduoduoEventMinimumPrice;

    @Schema(description = "阿里巴巴活动最低价")
    @ExcelProperty("阿里巴巴活动最低价")
    private BigDecimal alibabaEventMinimunPrice;

    @Schema(description = "团购活动最低价")
    @ExcelProperty("团购活动最低价")
    private BigDecimal groupBuyEventMinimunPrice;

    @Schema(description = "采购人员")
    @ExcelProperty("采购人员")
    private String purchaser;

    @Schema(description = "供应商名")
    @ExcelProperty("供应商名")
    private String supplier;

    @Schema(description = "采购单价（单位：元）")
    @ExcelProperty("采购单价（单位：元）")
    private BigDecimal purchasePrice;

    @Schema(description = "批发单价（单位：元）")
    @ExcelProperty("批发单价（单位：元）")
    private BigDecimal wholesalePrice;

    @Schema(description = "备注信息")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "对外最低采购单价（单位：元）")
    @ExcelProperty("对外最低采购单价（单位：元）")
    private BigDecimal minPurchasePrice;

    @Schema(description = "对外最低批发单价（单位：元）")
    @ExcelProperty("对外最低批发单价（单位：元）")
    private BigDecimal minWholesalePrice;

    @Schema(description = "运费类型（0：固定运费，1：按件计费，2：按重计费）")
    @ExcelProperty("运费类型")
    private Integer shippingFeeType;

    @Schema(description = "固定运费（单位：元）")
    @ExcelProperty("固定运费（单位：元）")
    private BigDecimal fixedShippingFee;

//    @Schema(description = "首件数量")
//    @ExcelProperty("首件数量")
//    private Integer firstItemQuantity;
//
//    @Schema(description = "首件价格（单位：元）")
//    @ExcelProperty("首件价格（单位：元）")
//    private BigDecimal firstItemPrice;

    @Schema(description = "按件数量")
    @ExcelProperty("按件数量")
    private Integer additionalItemQuantity;

    @Schema(description = "按件价格（单位：元）")
    @ExcelProperty("按件价格（单位：元）")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：kg）")
    @ExcelProperty("首重重量（单位：kg）")
    private BigDecimal firstWeight;

    @Schema(description = "首重价格（单位：元）")
    @ExcelProperty("首重价格（单位：元）")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：kg）")
    @ExcelProperty("续重重量（单位：kg）")
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格（单位：元）")
    @ExcelProperty("续重价格（单位：元）")
    private BigDecimal additionalWeightPrice;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "组_单数量关系")
    @ExcelProperty("组_单数量关系")
    private Integer count;
}
