package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - ERP 客户 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpCustomerRespVO {

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27520")
    private Long id;

    @Schema(description = "客户业务编号", example = "KH20241201000001")
    @ExcelProperty("客户编号")
    private String no;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("客户名称")
    private String name;

    @Schema(description = "收件姓名", example = "李四")
    @ExcelProperty("收件姓名")
    private String receiverName;

    @Schema(description = "联系电话", example = "15601691300")
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

    @Schema(description = "备注信息", example = "重要客户")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "创建人员", example = "admin")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
