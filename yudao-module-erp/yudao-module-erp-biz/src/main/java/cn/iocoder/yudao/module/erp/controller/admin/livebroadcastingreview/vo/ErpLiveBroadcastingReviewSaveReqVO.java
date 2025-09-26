package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 直播复盘新增/修改 Request VO")
@Data
public class ErpLiveBroadcastingReviewSaveReqVO {

    @Schema(description = "直播复盘编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "FPDD001")
    private String no;

    @Schema(description = "直播货盘表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "直播货盘表ID不能为空")
    private Long liveBroadcastingId;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;

    @Schema(description = "客户名称", example = "张三")
    private String customerName;

    @Schema(description = "直播平台", example = "抖音")
    private String livePlatform;

    @Schema(description = "直播价格", example = "99.90")
    private String livePrice;

    @Schema(description = "直播佣金", example = "10.00")
    private BigDecimal liveCommission;

    @Schema(description = "公开佣金", example = "8.00")
    private BigDecimal publicCommission;

    @Schema(description = "返点佣金", example = "5.00")
    private BigDecimal rebateCommission;

    @Schema(description = "寄样日期", example = "2023-01-01")
    private LocalDate sampleSendDate;

    @Schema(description = "开播日期", example = "2023-01-05")
    private LocalDate liveStartDate;

    @Schema(description = "开播销量", example = "100")
    private Integer liveSales;

    @Schema(description = "复播日期", example = "2023-01-10")
    private LocalDate repeatLiveDate;

    @Schema(description = "复播销量", example = "50")
    private Integer repeatLiveSales;
}
