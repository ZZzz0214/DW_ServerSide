package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 供应商分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpSupplierPageReqVO extends PageParam {

    @Schema(description = "供应商编号", example = "S001")
    private String no;

    @Schema(description = "供应商名称", example = "芋道源码")
    private String name;

    @Schema(description = "收件姓名", example = "李四")
    private String receiverName;

    @Schema(description = "联系电话", example = "18818288888")
    private String telephone;

    @Schema(description = "详细地址", example = "北京市朝阳区xxx街道xxx号")
    private String address;

    @Schema(description = "微信账号", example = "wechat123")
    private String wechatAccount;

    @Schema(description = "支付宝号", example = "alipay@example.com")
    private String alipayAccount;

    @Schema(description = "银行账号", example = "622908212277228617")
    private String bankAccount;

}