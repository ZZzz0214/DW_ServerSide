package cn.iocoder.yudao.module.erp.controller.admin.distribution.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 代发分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpDistributionPageReqVO extends PageParam {

    @Schema(description = "订单号", example = "DF202403250001")
    private String no;

    @Schema(description = "物流公司", example = "顺丰速运")
    private String logisticsCompany;

    @Schema(description = "物流单号", example = "SF1234567890")
    private String trackingNumber;

    @Schema(description = "收件人姓名", example = "张三")
    private String receiverName;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "租户编号", example = "1")
    private Long tenantId;

    @Schema(description = "销售状态", example = "2")
    private Integer status;
}
