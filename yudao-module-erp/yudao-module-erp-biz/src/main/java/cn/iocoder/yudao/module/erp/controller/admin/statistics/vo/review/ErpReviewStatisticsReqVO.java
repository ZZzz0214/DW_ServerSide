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

    @Schema(description = "产品名称", example = "产品A")
    private String productName;

    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;
} 