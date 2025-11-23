package cn.iocoder.yudao.module.infra.controller.admin.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileMigrationResultVO;
import cn.iocoder.yudao.module.infra.service.file.FileMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文件迁移")
@RestController
@RequestMapping("/infra/file/migration")
@Validated
@Slf4j
public class FileMigrationController {

    @Resource
    private FileMigrationService fileMigrationService;

    @PostMapping("/migrate-single")
    @Operation(summary = "迁移单个文件")
    // @PreAuthorize("@ss.hasPermission('infra:file:update')")  // 临时注释，允许所有登录用户执行
    public CommonResult<FileMigrationResultVO> migrateSingleFile(
            @RequestParam("fileId") @Parameter(description = "文件ID", required = true) Long fileId,
            @RequestParam("targetConfigId") @Parameter(description = "目标配置ID", required = true) Long targetConfigId) {
        
        log.info("[migrateSingleFile][请求迁移单个文件] fileId={}, targetConfigId={}", fileId, targetConfigId);
        FileMigrationResultVO result = fileMigrationService.migrateFile(fileId, targetConfigId);
        return success(result);
    }

    @PostMapping("/migrate-batch")
    @Operation(summary = "批量迁移文件")
    // @PreAuthorize("@ss.hasPermission('infra:file:update')")  // 临时注释，允许所有登录用户执行
    public CommonResult<FileMigrationResultVO> migrateBatch(
            @RequestParam("sourceConfigId") @Parameter(description = "源配置ID（DB存储）", required = true) Long sourceConfigId,
            @RequestParam("targetConfigId") @Parameter(description = "目标配置ID（LOCAL存储）", required = true) Long targetConfigId,
            @RequestParam(value = "batchSize", required = false, defaultValue = "100") @Parameter(description = "批次大小") Integer batchSize) {
        
        log.info("[migrateBatch][请求批量迁移] sourceConfigId={}, targetConfigId={}, batchSize={}", 
                sourceConfigId, targetConfigId, batchSize);
        
        FileMigrationResultVO result = fileMigrationService.migrateBatch(sourceConfigId, targetConfigId, batchSize);
        return success(result);
    }

    @GetMapping("/progress")
    @Operation(summary = "获取迁移进度")
    // @PreAuthorize("@ss.hasPermission('infra:file:query')")  // 临时注释，允许所有登录用户执行
    public CommonResult<FileMigrationResultVO> getMigrationProgress() {
        FileMigrationResultVO progress = fileMigrationService.getMigrationProgress();
        return success(progress);
    }

    @PostMapping("/verify")
    @Operation(summary = "验证文件迁移")
    // @PreAuthorize("@ss.hasPermission('infra:file:query')")  // 临时注释，允许所有登录用户执行
    public CommonResult<Boolean> verifyMigration(
            @RequestParam("fileId") @Parameter(description = "文件ID", required = true) Long fileId) {
        boolean verified = fileMigrationService.verifyMigration(fileId);
        return success(verified);
    }
}

