package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - ERP 记事本统计 Response VO")
@Data
public class ErpNotebookStatisticsRespVO {

    @Schema(description = "总体统计")
    private TotalStatistics totalStatistics;

    @Schema(description = "人员统计列表")
    private List<PersonStatistics> personStatisticsList;

    @Data
    @Schema(description = "总体统计")
    public static class TotalStatistics {
        @Schema(description = "总任务数", example = "100")
        private Long totalTaskCount;

        @Schema(description = "未完成任务数", example = "30")
        private Long pendingTaskCount;

        @Schema(description = "正在做任务数", example = "40")
        private Long inProgressTaskCount;

        @Schema(description = "已完成任务数", example = "30")
        private Long completedTaskCount;

        @Schema(description = "总人员数", example = "10")
        private Long totalPersonCount;
    }

    @Data
    @Schema(description = "人员统计")
    public static class PersonStatistics {
        @Schema(description = "任务人员", example = "张三")
        private String taskPerson;

        @Schema(description = "总任务数", example = "10")
        private Long totalTaskCount;

        @Schema(description = "未完成任务数", example = "3")
        private Long pendingTaskCount;

        @Schema(description = "正在做任务数", example = "4")
        private Long inProgressTaskCount;

        @Schema(description = "已完成任务数", example = "3")
        private Long completedTaskCount;

        @Schema(description = "完成率", example = "30.00")
        private Double completionRate;

        @Schema(description = "状态分布")
        private List<StatusDistribution> statusDistributions;
    }

    @Data
    @Schema(description = "状态分布")
    public static class StatusDistribution {
        @Schema(description = "任务状态", example = "0")
        private Integer taskStatus;

        @Schema(description = "状态名称", example = "未完成")
        private String statusName;

        @Schema(description = "任务数量", example = "5")
        private Long taskCount;

        @Schema(description = "占比", example = "50.00")
        private Double percentage;
    }

    @Data
    @Schema(description = "任务人员选项")
    public static class TaskPersonOption {
        @Schema(description = "字典值", example = "1")
        private String value;

        @Schema(description = "字典标签", example = "张三")
        private String label;
    }
} 