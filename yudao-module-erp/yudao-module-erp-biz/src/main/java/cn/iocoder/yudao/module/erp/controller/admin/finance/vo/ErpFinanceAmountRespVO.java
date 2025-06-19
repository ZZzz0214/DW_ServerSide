package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 财务金额 Response VO")
@Data
public class ErpFinanceAmountRespVO {

    @Schema(description = "财务金额记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "CWJE202403250001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "轮播图片", example = "http://example.com/image1.jpg,http://example.com/image2.jpg")
    @ExcelProperty("轮播图片")
    private String carouselImages;

    @Schema(description = "渠道类型", example = "微信")
    @ExcelProperty("渠道类型")
    private String channelType;

    @Schema(description = "金额", example = "100.00")
    @ExcelProperty("金额")
    private BigDecimal amount;

    @Schema(description = "操作类型", example = "1")
    @ExcelProperty("操作类型")
    private Integer operationType;

    @Schema(description = "操作前余额", example = "500.00")
    @ExcelProperty("操作前余额")
    private BigDecimal beforeBalance;

    @Schema(description = "操作后余额", example = "600.00")
    @ExcelProperty("操作后余额")
    private BigDecimal afterBalance;

    @Schema(description = "订单日期")
    @ExcelProperty("订单日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;

    // 兼容字段（已废弃，用于向后兼容）
    @Schema(description = "微信充值金额", example = "1000.00")
    @ExcelProperty("微信充值")
    private BigDecimal wechatRecharge;

    @Schema(description = "支付宝充值金额", example = "1000.00")
    @ExcelProperty("支付宝充值")
    private BigDecimal alipayRecharge;

    @Schema(description = "银行卡充值金额", example = "1000.00")
    @ExcelProperty("银行卡充值")
    private BigDecimal bankCardRecharge;

    @Schema(description = "微信当前余额", example = "500.00")
    @ExcelProperty("微信余额")
    private BigDecimal wechatBalance;

    @Schema(description = "支付宝当前余额", example = "500.00")
    @ExcelProperty("支付宝余额")
    private BigDecimal alipayBalance;

    @Schema(description = "银行卡当前余额", example = "500.00")
    @ExcelProperty("银行卡余额")
    private BigDecimal bankCardBalance;

    @Schema(description = "备注信息", example = "充值记录")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "审核状态", example = "1")
    @ExcelProperty("审核状态")
    private Integer auditStatus;

    @Schema(description = "审核人")
    @ExcelProperty("审核人")
    private String auditor;

    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核备注")
    @ExcelProperty("审核备注")
    private String auditRemark;

    @Schema(description = "创建者")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
} 