package cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.BigDecimalConvert;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.excel.core.convert.IntegerConvert;
import cn.iocoder.yudao.framework.excel.core.convert.LocalDateConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import javax.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpLiveBroadcastingImportExcelVO {

    @ExcelProperty("编号")
    private String no;


    @ExcelProperty(value = "品牌名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_BRAND)
    private String brandName;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("产品规格")
    private String productSpec;

    @ExcelProperty("产品SKU")
    private String productSku;

    @ExcelProperty(value = "市场价格", converter = BigDecimalConvert.class)
    private BigDecimal marketPrice;


    @ExcelProperty(value = "保质日期", converter = LocalDateConvert.class)
    private LocalDate shelfLife;


    @ExcelProperty(value = "产品库存", converter = IntegerConvert.class)
    private Integer productStock;

    @ExcelProperty("核心卖点")
    private String coreSellingPoint;

    @ExcelProperty("备注信息")
    private String remark;


    @ExcelProperty(value = "直播价格", converter = BigDecimalConvert.class)
    private BigDecimal livePrice;


    @ExcelProperty(value = "直播佣金", converter = BigDecimalConvert.class)
    private BigDecimal liveCommission;


    @ExcelProperty(value = "公开佣金", converter = BigDecimalConvert.class)
    private BigDecimal publicCommission;


    @ExcelProperty(value = "返点佣金", converter = BigDecimalConvert.class)
    private BigDecimal rebateCommission;

    @ExcelProperty("快递公司")
    private String expressCompany;

    @ExcelProperty("发货时效")
    private String shippingTime;

    @ExcelProperty("发货地区")
    private String shippingArea;

    @ExcelProperty(value = "直播货盘状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_LIVE_STATUS)
    private String liveStatus;
}
