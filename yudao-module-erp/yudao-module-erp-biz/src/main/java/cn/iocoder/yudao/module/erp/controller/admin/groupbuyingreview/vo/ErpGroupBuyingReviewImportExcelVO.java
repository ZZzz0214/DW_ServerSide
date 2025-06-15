package cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo;

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
public class ErpGroupBuyingReviewImportExcelVO {

    @ExcelProperty("编号")
    private String no;

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("团购货盘表编号")
    private String groupBuyingId;

    @ExcelProperty("供团价格")
    private BigDecimal supplyGroupPrice;

    @ExcelProperty("寄样日期")
    private LocalDateTime sampleSendDate;

    @ExcelProperty("开团日期")
    private LocalDateTime groupStartDate;

    @ExcelProperty("开团销量")
    private Integer groupSales;

    @ExcelProperty("复团日期")
    private LocalDateTime repeatGroupDate;

    @ExcelProperty("复团销量")
    private Integer repeatGroupSales;
} 