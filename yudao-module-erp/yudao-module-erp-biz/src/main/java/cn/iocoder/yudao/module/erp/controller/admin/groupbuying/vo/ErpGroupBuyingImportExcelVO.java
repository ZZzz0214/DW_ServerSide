package cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo;

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
public class ErpGroupBuyingImportExcelVO {

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

    @ExcelProperty(value = "核心价格", converter = BigDecimalConvert.class)
    private BigDecimal corePrice;

    @ExcelProperty(value = "分发价格", converter = BigDecimalConvert.class)
    private BigDecimal distributionPrice;

    @ExcelProperty(value = "供团价格", converter = BigDecimalConvert.class)
    private BigDecimal supplyGroupPrice;

    @ExcelProperty(value = "帮卖佣金", converter = BigDecimalConvert.class)
    private BigDecimal sellingCommission;

    @ExcelProperty(value = "开团价格", converter = BigDecimalConvert.class)
    private BigDecimal groupPrice;

    @ExcelProperty("开团机制")
    private String groupMechanism;

    @ExcelProperty(value = "快递费用", converter = BigDecimalConvert.class)
    private BigDecimal expressFee;

    @ExcelProperty("天猫京东")
    private String tmallJd;

    @ExcelProperty("公域数据")
    private String publicData;

    @ExcelProperty("私域数据")
    private String privateData;

    @ExcelProperty("品牌背书")
    private String brandEndorsement;

    @ExcelProperty("竞品分析")
    private String competitiveAnalysis;

    @ExcelProperty("快递公司")
    private String expressCompany;

    @ExcelProperty("发货时效")
    private String shippingTime;

    @ExcelProperty("发货地区")
    private String shippingArea;

    @ExcelProperty(value = "货盘状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_GROUP_BUYING_STATUS)
    private String status;
}
