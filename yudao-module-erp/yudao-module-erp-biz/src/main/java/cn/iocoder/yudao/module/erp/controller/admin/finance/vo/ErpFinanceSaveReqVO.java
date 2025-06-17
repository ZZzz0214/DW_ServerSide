package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - ERP 财务保存 Request VO")
@Data
public class ErpFinanceSaveReqVO {

    @Schema(description = "财务记录ID", example = "1")
    private Long id;

    @Schema(description = "编号", example = "CW202403250001")
    private String no;

    @Schema(description = "轮播图片", example = "http://example.com/image1.jpg,http://example.com/image2.jpg")
    private String carouselImages;

    @Schema(description = "账单名称", example = "办公用品采购")
    private String billName;

    @Schema(description = "收付金额", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "收入支出", example = "1")
    private Integer incomeExpense;

    @Schema(description = "收付类目", example = "办公费用")
    private String category;

    @Schema(description = "收付账号", example = "招商银行****1234")
    private String account;

    @Schema(description = "账单状态", example = "1")
    private Integer status;

    @Schema(description = "备注信息", example = "采购办公用品")
    private String remark;

    @Schema(description = "下单日期", example = "2024-03-25")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate orderDate;

    @Schema(description = "审核状态", example = "0")
    private Integer auditStatus;

    @Schema(description = "审核人")
    private String auditor;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核备注")
    private String auditRemark;
} 