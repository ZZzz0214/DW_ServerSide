package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpSupplierImportExcelVO {

    @ExcelProperty("供应商名")
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