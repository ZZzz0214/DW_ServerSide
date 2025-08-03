package cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 批发分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpWholesalePageReqVO extends PageParam {

    @Schema(description = "订单编号", example = "WS202403250001")
    private String no;

    @Schema(description = "物流单号", example = "SF123456789")
    private String logisticsNumber;

    @Schema(description = "收件人姓名", example = "张三")
    private String receiverName;

    @Schema(description = "联系电话", example = "13800138000")
    private String receiverPhone;

    @Schema(description = "详细地址", example = "北京市朝阳区xxx街道")
    private String receiverAddress;

    @Schema(description = "组品编号", example = "ZP001")
    private String comboProductNo;

    @Schema(description = "发货编码", example = "FH001")
    private String shippingCode;

    @Schema(description = "产品名称", example = "测试产品")
    private String productName;

    @Schema(description = "产品规格", example = "规格A")
    private String productSpecification;

    @Schema(description = "售后状况", example = "正常")
    private String afterSalesStatus;

    @Schema(description = "售后时间")
    private String[] afterSalesTime;

    @Schema(description = "采购人员", example = "李四")
    private String purchaser;

    @Schema(description = "供应商名", example = "某某供应商")
    private String supplier;

    @Schema(description = "销售人员", example = "王五")
    private String salesperson;

    @Schema(description = "客户名称", example = "某某客户")
    private String customerName;

    @Schema(description = "中转人员", example = "赵六")
    private String transferPerson;

    @Schema(description = "采购备注", example = "备注信息")
    private String purchaseRemark;

    @Schema(description = "出货备注", example = "备注信息")
    private String saleRemark;

    @Schema(description = "创建人员", example = "admin")
    private String creator;

    @Schema(description = "创建时间")
    private String[] createTime;

    @Schema(description = "采购审核状态", example = "1")
    private Integer purchaseAuditStatus;

    @Schema(description = "出货审核状态（销售审核状态）", example = "1")
    private Integer saleAuditStatus;

    @Schema(description = "租户编号", example = "1")
    private Long tenantId;
}
