package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.BigDecimalToIntegerConvert;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.excel.core.convert.DoubleConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 产品 Response VO")
@Data
@ExcelIgnoreUnannotated
@Builder
public class ErpProductPurchaseRespVO {

    private Long lastId;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15672")
    private Long id;

    @Schema(description = "产品编号(业务编号)", example = "P123456")
    @ExcelProperty("产品编号")
    private String no;

    @Schema(description = "产品图片")
    @ExcelProperty("产品图片")
    private String image;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("产品名称")
    private String name;

    @Schema(description = "产品简称")
    @ExcelProperty("产品简称")
    private String productShortName;

    @Schema(description = "发货编码")
    @ExcelProperty("发货编码")
    private String shippingCode;

    @Schema(description = "产品规格")
    @ExcelProperty("产品规格")
    private String standard;


    @Schema(description = "产品日期")
    @ExcelProperty("产品日期")
    private LocalDateTime productionDate;

    @Schema(description = "保质期天数")
    @ExcelProperty("保质日期")
    private Integer expiryDay;

    @ExcelProperty(value = "品牌名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_BRAND)
    private String brand;

    @ExcelProperty(value = "产品品类", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_CATEGORY)
    private String categoryId;

    @ExcelProperty(value = "产品状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_STATUS)
    private String status;

    @Schema(description = "产品卖点")
    @ExcelProperty("产品卖点")
    private String productSellingPoints;


    @Schema(description = "采购人员")
    @ExcelProperty("采购人员")
    private String purchaser;


    @Schema(description = "供应商名")
    @ExcelProperty("供应商名")
    private String supplier;

    @Schema(description = "采购单价")
    @ExcelProperty("采购单价")
    private BigDecimal purchasePrice;

    @Schema(description = "批发单价")
    @ExcelProperty("批发单价")
    private BigDecimal wholesalePrice;

    @Schema(description = "备注信息")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "对外最低出货单价")
    @ExcelProperty("对外最低出货单价")
    private BigDecimal minPurchasePrice;


    @Schema(description = "运费类型（0：固定运费，1：按件计费，2：按重计费）")
    @ExcelProperty("运费类型(0-固定运费,1-按件运费,2-按重运费)")
    private Integer shippingFeeType;

    @Schema(description = "固定运费")
    @ExcelProperty("固定运费")
    private BigDecimal fixedShippingFee;


    @Schema(description = "按件数量")
    @ExcelProperty("按件数量")
    private Integer additionalItemQuantity;

    @Schema(description = "按件价格")
    @ExcelProperty("按件价格")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：kg）")
    @ExcelProperty(value = "首重重量", converter = BigDecimalToIntegerConvert.class)
    private BigDecimal firstWeight;

    @Schema(description = "首重价格")
    @ExcelProperty("首重价格")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：kg）")
    @ExcelProperty(value = "续重重量", converter = BigDecimalToIntegerConvert.class)
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格")
    @ExcelProperty("续重价格")
    private BigDecimal additionalWeightPrice;

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
    private String orderReplenishmentLeadTime;

    @Schema(description = "产品长宽高")
    @ExcelProperty("品长宽高")
    private String productLength;

    @Schema(description = "产品重量")
    @ExcelProperty(value = "产品重量", converter = BigDecimalToIntegerConvert.class)
    private BigDecimal weight;

    @Schema(description = "箱规数量")
    @ExcelProperty("箱规数量")
    private String productCartonSpec;

    @Schema(description = "箱长宽高")
    @ExcelProperty("箱长宽高")
    private String cartonLength;

    @Schema(description = "箱规重量")
    @ExcelProperty(value = "箱规重量", converter = DoubleConvert.class)
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
    private String publicDomainEventMinimumPrice;

    @Schema(description = "直播活动最低价")
    @ExcelProperty("直播活动最低价")
    private String liveStreamingEventMinimunPrice;

    @Schema(description = "拼多多活动最低价")
    @ExcelProperty("多多活动最低价")
    private String pinduoduoEventMinimumPrice;

    @Schema(description = "阿里巴巴活动最低价")
    @ExcelProperty("阿里活动最低价")
    private String alibabaEventMinimunPrice;

    @Schema(description = "团购活动最低价")
    @ExcelProperty("团购活动最低价")
    private String groupBuyEventMinimunPrice;

    @Schema(description = "创建人员", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
