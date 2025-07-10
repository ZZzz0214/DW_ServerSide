package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 客户分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpCustomerPageReqVO extends PageParam {

    @Schema(description = "客户业务编号", example = "KH20241201000001")
    private String no;

    @Schema(description = "客户名称", example = "张三")
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

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}