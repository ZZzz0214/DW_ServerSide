package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 财务表统计 Response VO")
@Data
public class ErpFinanceSummaryRespVO {

    @Schema(description = "未审核数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long unauditedCount;

    @Schema(description = "已审核数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Long auditedCount;

    @Schema(description = "未审核支出金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000.00")
    private BigDecimal unauditedExpense;

    @Schema(description = "未审核收入金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000.00")
    private BigDecimal unauditedIncome;

    @Schema(description = "已审核支出金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000.00")
    private BigDecimal auditedExpense;

    @Schema(description = "已审核收入金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "8000.00")
    private BigDecimal auditedIncome;

    @Schema(description = "总支出金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "6000.00")
    private BigDecimal totalExpense;

    @Schema(description = "总收入金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000.00")
    private BigDecimal totalIncome;

} 