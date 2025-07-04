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
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "GBR001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "品牌名称", example = "XX品牌")
    @ExcelProperty("品牌名称")
    private String brandName;

    @Schema(description = "产品名称", example = "XX产品")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "产品规格", example = "500g/袋")
    @ExcelProperty("产品规格")
    private String productSpec;

    @Schema(description = "产品SKU", example = "SKU12345")
    @ExcelProperty("产品SKU")
    private String productSku;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "客户名称", example = "客户A")
    @ExcelProperty("客户名称")
    private String customerName;

    @Schema(description = "客户ID", example = "1")
    private String customerId;

    @Schema(description = "供团价格", example = "100.00")
    @ExcelProperty("供团价格")
    private BigDecimal supplyGroupPrice;

    @Schema(description = "快递费用", example = "100.00")
    @ExcelProperty("快递费用")
    private BigDecimal expressFee;

    @Schema(description = "开团机制", example = "满100人成团")
    @ExcelProperty("开团机制")
    private String groupMechanism;

    @Schema(description = "货盘状态", example = "上架")
    @ExcelProperty("货盘状态")
    private String status;

    @Schema(description = "寄样日期", example = "2023-01-01")
    @ExcelProperty("寄样日期")
    private LocalDateTime sampleSendDate;

    @Schema(description = "开团日期", example = "2023-01-10")
    @ExcelProperty("开团日期")
    private LocalDateTime groupStartDate;

    @Schema(description = "开团销量", example = "100")
    @ExcelProperty("开团销量")
    private Integer groupSales;

    @Schema(description = "复团日期", example = "2023-02-01")
    @ExcelProperty("复团日期")
    private LocalDateTime repeatGroupDate;

    @Schema(description = "复团销量", example = "50")
    @ExcelProperty("复团销量")
    private Integer repeatGroupSales;

    @Schema(description = "创建者")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "团购货盘表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "GB001")
    private String groupBuyingId;

    @Schema(description = "团购货盘表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String groupBuyingNo;


}
