package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo;

import cn.iocoder.yudao.framework.excel.core.convert.*;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpPrivateBroadcastingReviewImportExcelVO {

    @ExcelProperty("编号")
    private String no;

    @ExcelProperty("私播货盘编号")
    private String privateBroadcastingNo;

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty(value = "产品裸价", converter = BigDecimalConvert.class)
    private BigDecimal productNakedPrice;

    @ExcelProperty(value = "快递费用", converter = BigDecimalConvert.class)
    private BigDecimal expressFee;

    @ExcelProperty(value = "代发价格", converter = BigDecimalConvert.class)
    private BigDecimal dropshipPrice;

    @ExcelProperty(value = "寄样日期", converter = LocalDateConvert.class)
    private LocalDate sampleSendDate;


    @ExcelProperty(value = "开团日期", converter = LocalDateConvert.class)
    private LocalDate groupStartDate;


    @ExcelProperty(value = "开团销量", converter = IntegerConvert.class)
    private Integer groupSales;


    @ExcelProperty(value = "复团日期", converter = LocalDateConvert.class)
    private LocalDate repeatGroupDate;


    @ExcelProperty("复盘状态")
    private String reviewStatus;

    @ExcelProperty(value = "直播价格", converter = BigDecimalConvert.class)
    private BigDecimal livePrice;

}
