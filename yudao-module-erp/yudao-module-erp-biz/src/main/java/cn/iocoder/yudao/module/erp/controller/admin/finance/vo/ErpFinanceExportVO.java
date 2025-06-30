package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.module.erp.enums.DictTypeConstants.*;

@Schema(description = "管理后台 - ERP 财务导出 VO")
@Data
@Builder
public class ErpFinanceExportVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "CW202403250001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "轮播图片", example = "http://example.com/image1.jpg,http://example.com/image2.jpg")
    @ExcelProperty("轮播图片")
    private String carouselImages;

    @Schema(description = "账单名称", example = "办公用品采购")
    @ExcelProperty("账单名称")
    private String billName;

    @Schema(description = "收付金额", example = "1000.00")
    @ExcelProperty("收付金额")
    private BigDecimal amount;

    @Schema(description = "收入支出", example = "1")
    @ExcelProperty(value = "收入支出", converter = DictConvert.class)
    @DictFormat(FINANCE_INCOME_EXPENSE)
    private Integer incomeExpense;

    @Schema(description = "收付类目", example = "办公费用")
    @ExcelProperty(value = "收付类目", converter = DictConvert.class)
    @DictFormat(FINANCE_CATEGORY)
    private String category;

    @Schema(description = "收付账号", example = "招商银行****1234")
    @ExcelProperty("收付账号")
    private String account;

    @Schema(description = "账单状态", example = "1")
    @ExcelProperty(value = "账单状态", converter = DictConvert.class)
    @DictFormat(FINANCE_BILL_STATUS)
    private Integer status;

    @Schema(description = "备注信息", example = "采购办公用品")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "下单日期", example = "2024-03-25")
    @ExcelProperty("下单日期")
    private LocalDate orderDate;

    @Schema(description = "审核状态", example = "10")
    @ExcelProperty(value = "审核状态", converter = DictConvert.class)
    @DictFormat(AUDIT_STATUS)
    private Integer auditStatus;

    @Schema(description = "创建者")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "审核人员")
    @ExcelProperty("审核人员")
    private String auditor;

    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    private LocalDateTime auditTime;
}
