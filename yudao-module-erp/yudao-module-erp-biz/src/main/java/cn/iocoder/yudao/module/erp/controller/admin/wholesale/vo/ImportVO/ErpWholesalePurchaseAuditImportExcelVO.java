package cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false)
public class ErpWholesalePurchaseAuditImportExcelVO {

    @ExcelProperty("订单编号")
    private String no;

    @ExcelProperty("采购杂费")
    private BigDecimal purchaseOtherFees;

    @ExcelProperty("售后状况")
    private String afterSalesStatus;

    @ExcelProperty("采购售后金额")
    private BigDecimal purchaseAfterSalesAmount;
} 