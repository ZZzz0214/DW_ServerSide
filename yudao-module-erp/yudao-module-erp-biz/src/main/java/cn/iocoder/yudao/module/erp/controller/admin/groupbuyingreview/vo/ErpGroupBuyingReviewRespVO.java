package cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 团购复盘 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpGroupBuyingReviewRespVO {

    @Schema(description = "团购复盘编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("团购复盘编号")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "GBR001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("客户编号")
    private Long customerId;

    @Schema(description = "团购货盘表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("团购货盘表ID")
    private Long groupBuyingId;

    @Schema(description = "供团价格", example = "100.00")
    @ExcelProperty("供团价格")
    private BigDecimal supplyGroupPrice;

    @Schema(description = "寄样日期", example = "2023-01-01")
    @ExcelProperty("寄样日期")
    private LocalDate sampleSendDate;

    @Schema(description = "开团日期", example = "2023-01-10")
    @ExcelProperty("开团日期")
    private LocalDate groupStartDate;

    @Schema(description = "开团销量", example = "100")
    @ExcelProperty("开团销量")
    private Integer groupSales;

    @Schema(description = "复团日期", example = "2023-02-01")
    @ExcelProperty("复团日期")
    private LocalDate repeatGroupDate;

    @Schema(description = "复团销量", example = "50")
    @ExcelProperty("复团销量")
    private Integer repeatGroupSales;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
