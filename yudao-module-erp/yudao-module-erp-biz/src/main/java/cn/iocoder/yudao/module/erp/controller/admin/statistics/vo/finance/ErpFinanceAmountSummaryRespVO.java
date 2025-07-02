package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 财务金额表统计 Response VO")
@Data
public class ErpFinanceAmountSummaryRespVO {

    @Schema(description = "未审核数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long unauditedCount;

    @Schema(description = "已审核数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "25")
    private Long auditedCount;

    @Schema(description = "微信充值数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long wechatCount;

    @Schema(description = "支付宝充值数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Long alipayCount;

    @Schema(description = "银行卡充值数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long bankCardCount;

    @Schema(description = "微信充值总额", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000.00")
    private BigDecimal wechatTotal;

    @Schema(description = "支付宝充值总额", requiredMode = Schema.RequiredMode.REQUIRED, example = "4000.00")
    private BigDecimal alipayTotal;

    @Schema(description = "银行卡充值总额", requiredMode = Schema.RequiredMode.REQUIRED, example = "6000.00")
    private BigDecimal bankCardTotal;

    @Schema(description = "充值总金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "15000.00")
    private BigDecimal totalAmount;

} 