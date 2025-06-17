package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 财务金额分页 Request VO")
@Data
public class ErpFinanceAmountPageReqVO extends PageParam {

    @Schema(description = "编号", example = "CWJE202403250001")
    private String no;

    @Schema(description = "渠道类型", example = "微信")
    private String channelType;

    @Schema(description = "操作类型", example = "1")
    private Integer operationType;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;
} 