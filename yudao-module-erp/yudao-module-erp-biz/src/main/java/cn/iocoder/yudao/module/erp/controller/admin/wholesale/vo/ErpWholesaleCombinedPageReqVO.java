package cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 批发合并表分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpWholesaleCombinedPageReqVO extends PageParam {

    @Schema(description = "订单号", example = "PF20240601001")
    private String no;

    @Schema(description = "收件人姓名", example = "李四")
    private String receiverName;

    @Schema(description = "收件人电话", example = "13800138000")
    private String receiverPhone;

    @Schema(description = "物流单号", example = "SF1234567890")
    private String logisticsNumber;

    @Schema(description = "创建时间")
    private String[] createTime;

    @Schema(description = "租户编号", example = "1")
    private Long tenantId;

    @Schema(description = "采购审核状态", example = "1")
    private Integer purchaseAuditStatus;

    @Schema(description = "销售审核状态", example = "1")
    private Integer saleAuditStatus;

    @Schema(description = "售后状态", example = "1")
    private String afterSalesStatus;
}