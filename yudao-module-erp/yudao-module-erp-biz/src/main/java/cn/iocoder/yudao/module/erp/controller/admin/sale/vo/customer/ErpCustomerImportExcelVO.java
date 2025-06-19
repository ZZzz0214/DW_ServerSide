package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;

/**
 * ERP 客户 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false)
public class ErpCustomerImportExcelVO {

    @ExcelProperty("客户业务编号")
    private String no;

    @ExcelProperty("客户名称")
    private String name;

    @ExcelProperty("收件姓名")
    private String receiverName;

    @ExcelProperty("联系电话")
    private String telephone;

    @ExcelProperty("详细地址")
    private String address;

    @ExcelProperty("微信账号")
    private String wechatAccount;

    @ExcelProperty("支付宝号")
    private String alipayAccount;

    @ExcelProperty("银行账号")
    private String bankAccount;

    @ExcelProperty("备注信息")
    private String remark;

}
