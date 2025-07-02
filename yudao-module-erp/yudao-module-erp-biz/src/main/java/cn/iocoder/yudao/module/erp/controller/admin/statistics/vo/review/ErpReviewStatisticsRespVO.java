package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - ERP 复盘统计 Response VO")
@Data
public class ErpReviewStatisticsRespVO {

    @Schema(description = "团购复盘统计", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<GroupBuyingReviewStat> groupBuyingStats;

    @Schema(description = "私播复盘统计", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PrivateBroadcastingReviewStat> privateBroadcastingStats;

    @Schema(description = "直播复盘统计", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<LiveBroadcastingReviewStat> liveBroadcastingStats;

    @Schema(description = "客户选项", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CustomerOption> customerOptions;

    @Schema(description = "管理后台 - 团购复盘统计")
    @Data
    public static class GroupBuyingReviewStat {
        @Schema(description = "产品名称", example = "产品A")
        private String productName;

        @Schema(description = "寄样数量", example = "10")
        private Integer sampleSendCount;

        @Schema(description = "未寄样数量", example = "5")
        private Integer notSampleSendCount;

        @Schema(description = "寄样日期", example = "2024-01-15")
        private LocalDate sampleSendDate;

        @Schema(description = "开团数量", example = "8")
        private Integer groupStartCount;

        @Schema(description = "未开团数量", example = "7")
        private Integer notGroupStartCount;

        @Schema(description = "开团日期", example = "2024-01-20")
        private LocalDate groupStartDate;

        @Schema(description = "复团数量", example = "3")
        private Integer repeatGroupCount;

        @Schema(description = "未复团数量", example = "12")
        private Integer notRepeatGroupCount;

        @Schema(description = "复团日期", example = "2024-02-01")
        private LocalDate repeatGroupDate;
    }

    @Schema(description = "管理后台 - 私播复盘统计")
    @Data
    public static class PrivateBroadcastingReviewStat {
        @Schema(description = "产品名称", example = "产品B")
        private String productName;

        @Schema(description = "寄样数量", example = "15")
        private Integer sampleSendCount;

        @Schema(description = "未寄样数量", example = "3")
        private Integer notSampleSendCount;

        @Schema(description = "寄样日期", example = "2024-01-10")
        private LocalDate sampleSendDate;

        @Schema(description = "开团数量", example = "12")
        private Integer groupStartCount;

        @Schema(description = "未开团数量", example = "6")
        private Integer notGroupStartCount;

        @Schema(description = "开团日期", example = "2024-01-18")
        private LocalDate groupStartDate;

        @Schema(description = "复团数量", example = "5")
        private Integer repeatGroupCount;

        @Schema(description = "未复团数量", example = "13")
        private Integer notRepeatGroupCount;

        @Schema(description = "复团日期", example = "2024-02-05")
        private LocalDate repeatGroupDate;
    }

    @Schema(description = "管理后台 - 直播复盘统计")
    @Data
    public static class LiveBroadcastingReviewStat {
        @Schema(description = "产品名称", example = "产品C")
        private String productName;

        @Schema(description = "寄样数量", example = "20")
        private Integer sampleSendCount;

        @Schema(description = "未寄样数量", example = "2")
        private Integer notSampleSendCount;

        @Schema(description = "寄样日期", example = "2024-01-12")
        private LocalDate sampleSendDate;

        @Schema(description = "开团数量", example = "18")
        private Integer groupStartCount;

        @Schema(description = "未开团数量", example = "4")
        private Integer notGroupStartCount;

        @Schema(description = "开团日期", example = "2024-01-22")
        private LocalDate groupStartDate;

        @Schema(description = "复团数量", example = "7")
        private Integer repeatGroupCount;

        @Schema(description = "未复团数量", example = "15")
        private Integer notRepeatGroupCount;

        @Schema(description = "复团日期", example = "2024-02-08")
        private LocalDate repeatGroupDate;
    }

    @Schema(description = "管理后台 - 客户选项")
    @Data
    public static class CustomerOption {
        @Schema(description = "客户名称", example = "张三")
        private String customerName;

        @Schema(description = "复盘数量", example = "25")
        private Integer reviewCount;
    }
} 