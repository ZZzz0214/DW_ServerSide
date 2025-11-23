package cn.iocoder.yudao.module.infra.service.file;

import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileMigrationResultVO;

/**
 * 文件迁移 Service 接口
 * 用于将文件从一种存储方式迁移到另一种存储方式
 *
 * @author 芋道源码
 */
public interface FileMigrationService {

    /**
     * 迁移单个文件
     *
     * @param fileId 文件ID
     * @param targetConfigId 目标配置ID
     * @return 迁移结果
     */
    FileMigrationResultVO migrateFile(Long fileId, Long targetConfigId);

    /**
     * 批量迁移文件（从DB存储迁移到指定存储）
     *
     * @param sourceConfigId 源配置ID（DB存储）
     * @param targetConfigId 目标配置ID（LOCAL存储）
     * @param batchSize 每批处理数量
     * @return 迁移结果
     */
    FileMigrationResultVO migrateBatch(Long sourceConfigId, Long targetConfigId, Integer batchSize);

    /**
     * 获取迁移进度
     *
     * @return 迁移进度信息
     */
    FileMigrationResultVO getMigrationProgress();

    /**
     * 验证文件迁移
     *
     * @param fileId 文件ID
     * @return 验证结果
     */
    boolean verifyMigration(Long fileId);

}

