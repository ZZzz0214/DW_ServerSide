package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 供应商 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpSupplierRespVO {

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17791")
    @ExcelProperty("供应商编号")
    private Long id;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "S001")
    @ExcelProperty("供应商编号")
    private String no;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    @ExcelProperty("供应商名称")
    private String name;

    @Schema(description = "收件姓名", example = "李四")
    @ExcelProperty("收件姓名")
    private String receiverName;

    @Schema(description = "联系电话", example = "18818288888")
    @ExcelProperty("联系电话")
    private String telephone;

    @Schema(description = "详细地址", example = "北京市朝阳区xxx街道xxx号")
    @ExcelProperty("详细地址")
    private String address;

    @Schema(description = "微信账号", example = "wechat123")
    @ExcelProperty("微信账号")
    private String wechatAccount;

    @Schema(description = "支付宝号", example = "alipay@example.com")
    @ExcelProperty("支付宝号")
    private String alipayAccount;

    @Schema(description = "银行账号", example = "622908212277228617")
    @ExcelProperty("银行账号")
    private String bankAccount;

    @Schema(description = "备注信息", example = "重要供应商")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}