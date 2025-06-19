package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 财务分页 Request VO")
@Data
public class ErpFinancePageReqVO extends PageParam {

    @Schema(description = "编号", example = "CW202403250001")
    private String no;

    @Schema(description = "账单名称", example = "办公用品采购")
    private String billName;

    @Schema(description = "收入支出", example = "1")
    private Integer incomeExpense;

    @Schema(description = "收付类目", example = "办公费用")
    private String category;

    @Schema(description = "收付账号", example = "招商银行****1234")
    private String account;

    @Schema(description = "账单状态", example = "1")
    private Integer status;

    @Schema(description = "审核状态", example = "10")
    private Integer auditStatus;

    @Schema(description = "创建人员", example = "admin")
    private String creator;

    @Schema(description = "审核人员", example = "admin")
    private String auditor;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @Schema(description = "下单日期")
    private LocalDate[] orderDate;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "审核时间")
    private LocalDateTime[] auditTime;
} 