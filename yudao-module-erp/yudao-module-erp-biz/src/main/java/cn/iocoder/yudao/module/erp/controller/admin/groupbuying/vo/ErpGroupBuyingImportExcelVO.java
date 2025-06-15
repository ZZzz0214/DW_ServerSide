package cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
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

    @ExcelProperty("产品图片")
    private String productImage;

    @ExcelProperty(value = "品牌名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_BRAND)
    private String brandName;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("产品规格")
    private String productSpec;

    @ExcelProperty("产品SKU")
    private String productSku;

    @ExcelProperty("市场价格")
    private BigDecimal marketPrice;

    @ExcelProperty("保质日期")
    private LocalDateTime shelfLife;

    @ExcelProperty("产品库存")
    private Integer productStock;

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty("核心价格")
    private BigDecimal corePrice;

    @ExcelProperty("分发价格")
    private BigDecimal distributionPrice;

    @ExcelProperty("供团价格")
    private BigDecimal supplyGroupPrice;

    @ExcelProperty("帮卖佣金")
    private BigDecimal sellingCommission;

    @ExcelProperty("开团价格")
    private BigDecimal groupPrice;

    @ExcelProperty("渠道毛利")
    private BigDecimal channelProfit;

    @ExcelProperty("开团机制")
    private String groupMechanism;

    @ExcelProperty("快递费用")
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
