package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - ERP 私播复盘新增/修改 Request VO")
@Data
public class ErpPrivateBroadcastingReviewSaveReqVO {

    @Schema(description = "私播复盘编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SBF001")
    private String no;

    @Schema(description = "私播货盘表ID", example = "1")

    private Long privateBroadcastingId;

    @Schema(description = "客户ID", example = "1")
    private Long customerId;

    @Schema(description = "产品裸价", example = "50.00")
    private BigDecimal productNakedPrice;

    @Schema(description = "快递费用", example = "10.00")
    private BigDecimal expressFee;

    @Schema(description = "代发价格", example = "60.00")
    private BigDecimal dropshipPrice;

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

    @Schema(description = "直播价格", example = "100.00")
    private BigDecimal livePrice;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;
}
