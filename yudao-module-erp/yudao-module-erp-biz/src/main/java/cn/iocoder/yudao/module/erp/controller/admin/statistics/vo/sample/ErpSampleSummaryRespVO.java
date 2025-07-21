package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.sample;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - ERP 样品统计 Response VO")
@Data
public class ErpSampleSummaryRespVO {

    @Schema(description = "样品状态统计", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Integer> statusCount;

    @Schema(description = "客户样品统计", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CustomerSampleStat> customerStats;

    @Schema(description = "总样品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer totalCount;

    @Schema(description = "客户选项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CustomerOption> customerOptions;
    
    @Schema(description = "总客户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer total;

    @Schema(description = "当前页码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer pageNo;

    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer pageSize;

    @Schema(description = "样品状态统计详情", requiredMode = Schema.RequiredMode.REQUIRED)
    @Data
    public static class CustomerSampleStat {

        @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "客户A")
        private String customerName;

        @Schema(description = "样品状态统计", requiredMode = Schema.RequiredMode.REQUIRED)
        private Map<String, Integer> statusCount;

        @Schema(description = "该客户总样品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
        private Integer totalCount;
    }

    @Schema(description = "客户选项", requiredMode = Schema.RequiredMode.REQUIRED)
    @Data
    public static class CustomerOption {

        @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "客户A")
        private String customerName;

        @Schema(description = "客户样品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        private Integer sampleCount;
    }
} 