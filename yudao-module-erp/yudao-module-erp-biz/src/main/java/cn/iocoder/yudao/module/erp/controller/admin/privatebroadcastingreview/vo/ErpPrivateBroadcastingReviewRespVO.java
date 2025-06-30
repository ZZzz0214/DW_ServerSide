package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 私播复盘 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPrivateBroadcastingReviewRespVO {

    @Schema(description = "私播复盘编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("私播复盘编号")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SBF001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "私播货盘表ID", example = "1")
    @ExcelProperty("私播货盘表ID")
    private Long privateBroadcastingId;

    @Schema(description = "客户ID", example = "1")
    @ExcelProperty("客户ID")
    private Long customerId;

    @Schema(description = "产品裸价", example = "50.00")
    @ExcelProperty("产品裸价")
    private BigDecimal productNakedPrice;

    @Schema(description = "快递费用", example = "10.00")
    @ExcelProperty("快递费用")
    private BigDecimal expressFee;

    @Schema(description = "代发价格", example = "60.00")
    @ExcelProperty("代发价格")
    private BigDecimal dropshipPrice;

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

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "创建人员", example = "张三")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "产品名称", example = "产品A")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "品牌名称", example = "品牌A")
    @ExcelProperty("品牌名称")
    private String brandName;

    @Schema(description = "产品规格", example = "标准规格")
    @ExcelProperty("产品规格")
    private String productSpec;

    @Schema(description = "产品SKU", example = "SKU001")
    @ExcelProperty("产品SKU")
    private String productSku;

    @Schema(description = "直播价格", example = "100.00")
    @ExcelProperty("直播价格")
    private BigDecimal livePrice;

    @Schema(description = "货盘编号", example = "PB001")
    @ExcelProperty("货盘编号")
    private String privateBroadcastingNo;

    @Schema(description = "客户名称", example = "张三")
    @ExcelProperty("客户名称")
    private String customerName;

    @Schema(description = "货盘状态", example = "已审核")
    @ExcelProperty("货盘状态")
    private String privateStatus;
}
