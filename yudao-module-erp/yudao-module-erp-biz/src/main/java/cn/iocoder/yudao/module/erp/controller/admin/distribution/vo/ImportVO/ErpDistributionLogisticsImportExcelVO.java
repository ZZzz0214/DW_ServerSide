package cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false)
public class ErpDistributionLogisticsImportExcelVO {

    @ExcelProperty("订单编号")
    private String no;

    @ExcelProperty("物流公司")
    private String logisticsCompany;

    @ExcelProperty("物流单号")
    private String trackingNumber;

    @ExcelProperty("采购备注")
    private String purchaseRemark;
} 