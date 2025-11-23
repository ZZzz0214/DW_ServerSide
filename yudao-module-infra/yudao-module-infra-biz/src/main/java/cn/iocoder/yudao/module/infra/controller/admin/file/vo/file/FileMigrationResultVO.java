package cn.iocoder.yudao.module.infra.controller.admin.file.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 文件迁移结果 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMigrationResultVO {

    @Schema(description = "总文件数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer totalCount;

    @Schema(description = "成功数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "95")
    private Integer successCount;

    @Schema(description = "失败数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer failCount;

    @Schema(description = "跳过数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer skipCount;

    @Schema(description = "迁移状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "RUNNING")
    private String status; // RUNNING, COMPLETED, FAILED, PAUSED

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "错误信息列表")
    @Builder.Default
    private List<MigrationError> errors = new ArrayList<>();

    @Schema(description = "迁移进度百分比", example = "85.5")
    private Double progress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MigrationError {
        @Schema(description = "文件ID", example = "1001")
        private Long fileId;

        @Schema(description = "文件名", example = "test.jpg")
        private String fileName;

        @Schema(description = "错误信息", example = "文件不存在")
        private String errorMessage;

        @Schema(description = "错误时间")
        private LocalDateTime errorTime;
    }

    /**
     * 创建初始结果
     */
    public static FileMigrationResultVO createInitial(Integer totalCount) {
        return FileMigrationResultVO.builder()
                .totalCount(totalCount)
                .successCount(0)
                .failCount(0)
                .skipCount(0)
                .status("RUNNING")
                .startTime(LocalDateTime.now())
                .progress(0.0)
                .errors(new ArrayList<>())
                .build();
    }

    /**
     * 添加成功记录
     */
    public void addSuccess() {
        this.successCount++;
        updateProgress();
    }

    /**
     * 添加失败记录
     */
    public void addFail(Long fileId, String fileName, String errorMessage) {
        this.failCount++;
        this.errors.add(MigrationError.builder()
                .fileId(fileId)
                .fileName(fileName)
                .errorMessage(errorMessage)
                .errorTime(LocalDateTime.now())
                .build());
        updateProgress();
    }

    /**
     * 添加跳过记录
     */
    public void addSkip() {
        this.skipCount++;
        updateProgress();
    }

    /**
     * 更新进度
     */
    private void updateProgress() {
        int processed = successCount + failCount + skipCount;
        if (totalCount > 0) {
            this.progress = (processed * 100.0) / totalCount;
        }
    }

    /**
     * 完成迁移
     */
    public void complete() {
        this.status = failCount > 0 ? "COMPLETED_WITH_ERRORS" : "COMPLETED";
        this.endTime = LocalDateTime.now();
        this.progress = 100.0;
    }

    /**
     * 标记失败
     */
    public void markFailed(String reason) {
        this.status = "FAILED";
        this.endTime = LocalDateTime.now();
    }
}

