package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo;

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
public class ErpPrivateBroadcastingImportExcelVO {

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

    @ExcelProperty("直播价格")
    private BigDecimal livePrice;

    @ExcelProperty("产品裸价")
    private BigDecimal productNakedPrice;

    @ExcelProperty("快递费用")
    private BigDecimal expressFee;

    @ExcelProperty("代发价格")
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

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty(value = "私播货盘状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRIVATE_STATUS)
    private String privateStatus;
} 