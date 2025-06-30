package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.AccountConvert;
import cn.iocoder.yudao.framework.excel.core.convert.BigDecimalConvert;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.excel.core.convert.LocalDateConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

import static cn.iocoder.yudao.module.erp.enums.DictTypeConstants.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpFinanceImportExcelVO {

    @ExcelProperty("编号")
    private String no;

    @ExcelProperty("凭证图片")
    private String carouselImages;

    @ExcelProperty("账单名称")
    private String billName;

    @ExcelProperty(value = "收付金额", converter = BigDecimalConvert.class)
    private BigDecimal amount;

    @ExcelProperty(value = "收入支出", converter = DictConvert.class)
    @DictFormat(FINANCE_INCOME_EXPENSE)
    private Integer incomeExpense;

    @ExcelProperty(value = "收付类目", converter = DictConvert.class)
    @DictFormat(FINANCE_CATEGORY)
    private String category;

    @ExcelProperty(value = "收付账号", converter = AccountConvert.class)
    private String account;

    @ExcelProperty(value = "账单状态", converter = DictConvert.class)
    @DictFormat(FINANCE_BILL_STATUS)
    private Integer status;

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty(value = "下单日期", converter = LocalDateConvert.class)
    private LocalDate orderDate;

}
