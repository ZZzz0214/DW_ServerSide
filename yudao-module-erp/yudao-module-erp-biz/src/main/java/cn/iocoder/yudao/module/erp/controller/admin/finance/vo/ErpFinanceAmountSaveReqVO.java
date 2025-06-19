package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 财务金额保存 Request VO")
@Data
public class ErpFinanceAmountSaveReqVO {

    @Schema(description = "财务金额记录ID", example = "1")
    private Long id;

    @Schema(description = "编号", example = "CWJE202403250001")
    private String no;

    @Schema(description = "轮播图片", example = "http://example.com/image1.jpg,http://example.com/image2.jpg")
    private String carouselImages;

    @Schema(description = "渠道类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "微信")
    @NotNull(message = "渠道类型不能为空")
    private String channelType;

    @Schema(description = "金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "操作类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "操作类型不能为空")
    private Integer operationType;

    @Schema(description = "操作前余额", example = "500.00")
    private BigDecimal beforeBalance;

    @Schema(description = "操作后余额", example = "600.00")
    private BigDecimal afterBalance;

    @Schema(description = "订单日期", example = "2024-03-25")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;

    @Schema(description = "备注信息", example = "充值记录")
    private String remark;

    @Schema(description = "审核状态", example = "10")
    private Integer auditStatus;

    @Schema(description = "审核人")
    private String auditor;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核备注")
    private String auditRemark;
} 