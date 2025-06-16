package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

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

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("产品裸价")
    private BigDecimal productNakedPrice;

    @ExcelProperty("快递费用")
    private BigDecimal expressFee;

    @ExcelProperty("代发价格")
    private BigDecimal dropshipPrice;

    @ExcelProperty("寄样日期")
    private String sampleSendDate;

    @ExcelProperty("开团日期")
    private String groupStartDate;

    @ExcelProperty("开团销量")
    private Integer groupSales;

    @ExcelProperty("复团日期")
    private String repeatGroupDate;

    @ExcelProperty("复团销量")
    private Integer repeatGroupSales;

    @ExcelProperty("备注信息")
    private String remark;
} 