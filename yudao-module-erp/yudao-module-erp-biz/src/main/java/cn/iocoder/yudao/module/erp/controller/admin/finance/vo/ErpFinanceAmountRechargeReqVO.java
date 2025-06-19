package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 财务金额充值 Request VO")
@Data
public class ErpFinanceAmountRechargeReqVO {

    @Schema(description = "充值渠道", requiredMode = Schema.RequiredMode.REQUIRED, example = "微信")
    @NotBlank(message = "充值渠道不能为空")
    private String channelType;

    @Schema(description = "充值金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "充值金额不能为空")
    @Positive(message = "充值金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "轮播图片", example = "http://example.com/image1.jpg,http://example.com/image2.jpg")
    private String carouselImages;

    @Schema(description = "备注信息", example = "充值记录")
    private String remark;

    @Schema(description = "订单日期", example = "2024-03-25")
    private String orderDate;
} 