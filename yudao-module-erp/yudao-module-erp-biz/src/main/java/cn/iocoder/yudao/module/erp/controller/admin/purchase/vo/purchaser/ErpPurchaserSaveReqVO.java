package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 采购人员新增/修改 Request VO")
@Data
public class ErpPurchaserSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "采购人员姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "采购人员姓名不能为空")
    private String purchaserName;

    @Schema(description = "收件姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "收件姓名不能为空")
    private String receiverName;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotEmpty(message = "联系电话不能为空")
    private String contactPhone;

    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "北京市朝阳区")
    @NotEmpty(message = "详细地址不能为空")
    private String address;

    @Schema(description = "微信账号", example = "wx123456")
    private String wechatAccount;

    @Schema(description = "支付宝账号", example = "alipay123456")
    private String alipayAccount;

    @Schema(description = "银行账号", example = "6225880137700000")
    private String bankAccount;

}