package cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - ERP 团购复盘新增/修改 Request VO")
@Data
public class ErpGroupBuyingReviewSaveReqVO {

    @Schema(description = "团购复盘编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "GBR001")
    private String no;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "客户编号不能为空")
    private Long customerId;

    @Schema(description = "团购货盘表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "团购货盘表ID不能为空")
    private Long groupBuyingId;

    @Schema(description = "供团价格", example = "100.00")
    private BigDecimal supplyGroupPrice;

    @Schema(description = "寄样日期", example = "2023-01-01")
    private LocalDate sampleSendDate;

    @Schema(description = "开团日期", example = "2023-01-10")
    private LocalDate groupStartDate;

    @Schema(description = "开团销量", example = "100")
    private Integer groupSales;

    @Schema(description = "复团日期", example = "2023-02-01")
    private LocalDate repeatGroupDate;

    @Schema(description = "复团销量", example = "50")
    private Integer repeatGroupSales;
}