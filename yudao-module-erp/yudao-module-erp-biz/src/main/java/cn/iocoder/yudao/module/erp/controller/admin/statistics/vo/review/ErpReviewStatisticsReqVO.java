package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

//@import java.time.LocalDate;

@Schema(description = "管理后台 - ERP 复盘统计 Request VO")
@Data
public class ErpReviewStatisticsReqVO {

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01")
    private String beginDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-31")
    private String endDate;

    @Schema(description = "客户名称", example = "张三")
    private String customerName;
} 