package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 财务金额分页 Request VO")
@Data
public class ErpFinanceAmountPageReqVO extends PageParam {

    @Schema(description = "编号", example = "CWJE202403250001")
    private String no;

    @Schema(description = "渠道类型", example = "微信")
    private String channelType;

    @Schema(description = "操作类型", example = "1")
    private Integer operationType;

    @Schema(description = "审核状态", example = "1")
    private Integer auditStatus;

    @Schema(description = "创建者", example = "admin")
    private String creator;

    @Schema(description = "审核人", example = "auditor")
    private String auditor;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "审核时间")
    private LocalDateTime[] auditTime;

    @Schema(description = "订单日期")
    private LocalDate[] orderDate;

    // 兼容字段（已废弃，用于向后兼容）
    @Schema(description = "微信充值金额", example = "1000.00")
    private BigDecimal wechatRecharge;

    @Schema(description = "支付宝充值金额", example = "1000.00")
    private BigDecimal alipayRecharge;

    @Schema(description = "银行卡充值金额", example = "1000.00")
    private BigDecimal bankCardRecharge;

    @Schema(description = "微信当前余额", example = "500.00")
    private BigDecimal wechatBalance;

    @Schema(description = "支付宝当前余额", example = "500.00")
    private BigDecimal alipayBalance;

    @Schema(description = "银行卡当前余额", example = "500.00")
    private BigDecimal bankCardBalance;
} 