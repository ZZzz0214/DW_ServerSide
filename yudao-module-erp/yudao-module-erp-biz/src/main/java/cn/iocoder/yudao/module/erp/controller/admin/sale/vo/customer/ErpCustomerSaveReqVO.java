package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.*;

@Schema(description = "管理后台 - ERP 客户新增/修改 Request VO")
@Data
public class ErpCustomerSaveReqVO {

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27520")
    private Long id;

    @Schema(description = "客户业务编号", example = "KH20241201000001")
    private String no;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "客户名称不能为空")
    private String name;

    @Schema(description = "收件姓名", example = "李四")
    private String receiverName;

    @Schema(description = "联系电话", example = "15601691300")
    private String telephone;

    @Schema(description = "详细地址", example = "北京市朝阳区xxx街道xxx号")
    private String address;

    @Schema(description = "微信账号", example = "wechat123")
    private String wechatAccount;

    @Schema(description = "支付宝号", example = "alipay@example.com")
    private String alipayAccount;

    @Schema(description = "银行账号", example = "622908212277228617")
    private String bankAccount;

    @Schema(description = "备注信息", example = "重要客户")
    private String remark;

}