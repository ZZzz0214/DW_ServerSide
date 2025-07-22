package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 采购人员 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPurchaserRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "采购人员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "P001")
    @ExcelProperty("采购人员编号")
    private String no;

    @Schema(description = "采购人员姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("采购人员姓名")
    private String purchaserName;

    @Schema(description = "收件姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("收件姓名")
    private String receiverName;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @ExcelProperty("联系电话")
    private String contactPhone;

    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "北京市朝阳区")
    @ExcelProperty("详细地址")
    private String address;

    @Schema(description = "微信账号", example = "wx123456")
    @ExcelProperty("微信账号")
    private String wechatAccount;

    @Schema(description = "支付宝账号", example = "alipay123456")
    @ExcelProperty("支付宝账号")
    private String alipayAccount;

    @Schema(description = "银行账号", example = "6225880137700000")
    @ExcelProperty("银行账号")
    private String bankAccount;

    @Schema(description = "创建人员", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建人员")
    private String creator;


    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "备注信息", example = "备注信息")
    @ExcelProperty("备注信息")
    private String remark;

}
