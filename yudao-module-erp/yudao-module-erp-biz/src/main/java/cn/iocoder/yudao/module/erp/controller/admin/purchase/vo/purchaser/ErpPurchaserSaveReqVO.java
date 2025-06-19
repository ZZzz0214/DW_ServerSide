package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser;


import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 采购人员新增/修改 Request VO")
@Data
public class ErpPurchaserSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "采购人员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "P001")
    private String no;

    @Schema(description = "采购人员姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "采购人员不能为空")
    private String purchaserName;

    @Schema(description = "收件姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String receiverName;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    private String contactPhone;

    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "北京市朝阳区")

    private String address;

    @Schema(description = "微信账号", example = "wx123456")
    private String wechatAccount;

    @Schema(description = "支付宝账号", example = "alipay123456")
    private String alipayAccount;

    @Schema(description = "银行账号", example = "6225880137700000")
    private String bankAccount;

    @Schema(description = "备注信息", example = "备注信息")
    @ExcelProperty("备注信息")
    private String remark;

}
