package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.BigDecimalConvert;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.excel.core.convert.IntegerConvert;
import cn.iocoder.yudao.framework.excel.core.convert.LocalDateTimeConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
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
public class ErpPrivateBroadcastingImportExcelVO {

    @ExcelProperty("编号")
    private String no;

    @ExcelProperty(value = "品牌名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_BRAND)
    private String brandName;

    @ExcelProperty(value = "产品分类", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_CATEGORY)
    private Long categoryId;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("产品规格")
    private String productSpec;

    @ExcelProperty("产品SKU")
    private String productSku;


    @ExcelProperty(value = "市场价格", converter = BigDecimalConvert.class)
    private BigDecimal marketPrice;


    @ExcelProperty(value = "保质日期", converter = LocalDateTimeConvert.class)
    private LocalDateTime shelfLife;

    @ExcelProperty(value = "产品库存", converter = IntegerConvert.class)
    private Integer productStock;

    @ExcelProperty("备注信息")
    private String remark;


    @ExcelProperty(value = "直播价格", converter = BigDecimalConvert.class)
    private BigDecimal livePrice;


    @ExcelProperty(value = "产品裸价", converter = BigDecimalConvert.class)
    private BigDecimal productNakedPrice;


    @ExcelProperty(value = "快递费用", converter = BigDecimalConvert.class)
    private BigDecimal expressFee;


    @ExcelProperty(value = "代发价格", converter = BigDecimalConvert.class)
    private BigDecimal dropshipPrice;

    @ExcelProperty("公域链接")
    private String publicLink;

    @ExcelProperty("核心卖点")
    private String coreSellingPoint;

    @ExcelProperty("快递公司")
    private String expressCompany;

    @ExcelProperty("发货时效")
    private String shippingTime;

    @ExcelProperty("发货地区")
    private String shippingArea;

    @ExcelProperty(value = "私播货盘状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRIVATE_STATUS)
    private String privateStatus;
}
