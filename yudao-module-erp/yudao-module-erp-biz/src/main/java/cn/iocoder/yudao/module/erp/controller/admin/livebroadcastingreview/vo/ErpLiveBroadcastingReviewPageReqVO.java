package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 直播复盘分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpLiveBroadcastingReviewPageReqVO extends PageParam {

    @Schema(description = "编号", example = "FPDD001")
    private String no;

    @Schema(description = "直播货盘表ID", example = "1")
    private Long liveBroadcastingId;

    @Schema(description = "客户ID", example = "1")
    private Long customerId;

    @Schema(description = "直播平台", example = "抖音")
    private String livePlatform;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}