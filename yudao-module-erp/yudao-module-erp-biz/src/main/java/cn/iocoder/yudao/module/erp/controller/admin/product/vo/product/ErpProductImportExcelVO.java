package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;


import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpProductImportExcelVO {

    @ExcelProperty("产品编号")
    private String no;

    @ExcelProperty("产品名称")
    private String name;

    @ExcelProperty("产品简称")
    private String productShortName;

    @ExcelProperty("发货编码")
    private String shippingCode;

    @ExcelProperty("产品规格")
    private String standard;

    @ExcelProperty("产品日期")
    @DateTimeFormat("yyyy/M/d")
    private LocalDateTime productionDate;

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

    @ExcelProperty("产品卖点")
    private String productSellingPoints;

    @ExcelProperty("采购人员")
    private String purchaser;

    @ExcelProperty("供应商名")
    private String supplier;

    @ExcelProperty("采购单价")
    private BigDecimal purchasePrice;

    @ExcelProperty("批发单价")
    private BigDecimal wholesalePrice;

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty("对外最低出货单价")
    private String minPurchasePrice;

    @ExcelProperty("运费类型(0-固定运费,1-按件运费,2-按重运费)")
    private Integer shippingFeeType;

    @ExcelProperty("固定运费")
    private BigDecimal fixedShippingFee;

    @ExcelProperty("按件数量")
    private Integer additionalItemQuantity;

    @ExcelProperty("按件价格")
    private BigDecimal additionalItemPrice;

    @ExcelProperty("首重重量")
    private BigDecimal firstWeight;

    @ExcelProperty("首重价格")
    private BigDecimal firstWeightPrice;

    @ExcelProperty("续重重量")
    private BigDecimal additionalWeight;

    @ExcelProperty("续重价格")
    private BigDecimal additionalWeightPrice;

    @ExcelProperty("条形编号")
    private String barCode;

    @ExcelProperty("备案编号")
    private String productRecord;

    @ExcelProperty("执行编号")
    private String executionCode;

    @ExcelProperty("商标编号")
    private String trademarkCode;

    @ExcelProperty("现货数量")
    private Integer totalQuantity;

    @ExcelProperty("包材数量")
    private Integer packagingMaterialQuantity;

    @ExcelProperty("返单时效")
    private String orderReplenishmentLeadTime;

    @ExcelProperty("品长宽高")
    private String productLength;

    @ExcelProperty("产品重量")
    private BigDecimal weight;

    @ExcelProperty("箱规数量")
    private String productCartonSpec;

    @ExcelProperty("箱长宽高")
    private String cartonLength;

    @ExcelProperty("箱规重量")
    private Double cartonWeight;

    @ExcelProperty("发货地址")
    private String shippingAddress;

    @ExcelProperty("退货地址")
    private String returnAddress;

    @ExcelProperty("快递公司")
    private String logisticsCompany;

    @ExcelProperty("不发货区")
    private String nonshippingArea;

    @ExcelProperty("加邮地区")
    private String addonShippingArea;

    @ExcelProperty("售后标准")
    private String afterSalesStandard;

    @ExcelProperty("售后话术")
    private String afterSalesScript;

    @ExcelProperty("公域活动最低价")
    private String publicDomainEventMinimumPrice;

    @ExcelProperty("直播活动最低价")
    private String liveStreamingEventMinimunPrice;

    @ExcelProperty("多多活动最低价")
    private String pinduoduoEventMinimumPrice;

    @ExcelProperty("阿里活动最低价")
    private String alibabaEventMinimunPrice;

    @ExcelProperty("团购活动最低价")
    private String groupBuyEventMinimunPrice;

}
