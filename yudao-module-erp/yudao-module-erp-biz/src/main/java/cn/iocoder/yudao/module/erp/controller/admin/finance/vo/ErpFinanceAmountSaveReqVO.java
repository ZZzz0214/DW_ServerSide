package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
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

    @Schema(description = "备注信息", example = "充值记录")
    private String remark;

    @Schema(description = "审核状态", example = "0")
    private Integer auditStatus;

    @Schema(description = "审核人")
    private String auditor;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核备注")
    private String auditRemark;
} 