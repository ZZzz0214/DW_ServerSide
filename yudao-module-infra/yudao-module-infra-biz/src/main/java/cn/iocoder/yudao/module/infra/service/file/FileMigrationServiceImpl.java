package cn.iocoder.yudao.module.infra.service.file;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileMigrationResultVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileConfigMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_CONFIG_NOT_EXISTS;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_NOT_EXISTS;

/**
 * 文件迁移 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class FileMigrationServiceImpl implements FileMigrationService {

    @Resource
    private FileConfigService fileConfigService;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private FileConfigMapper fileConfigMapper;

    /**
     * 当前迁移进度（全局单例，简化实现）
     */
    private final AtomicReference<FileMigrationResultVO> currentProgress = new AtomicReference<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileMigrationResultVO migrateFile(Long fileId, Long targetConfigId) {
        log.info("[migrateFile][开始迁移文件] fileId={}, targetConfigId={}", fileId, targetConfigId);

        // 初始化结果
        FileMigrationResultVO result = FileMigrationResultVO.createInitial(1);

        try {
            // 1. 校验文件存在
            FileDO file = fileMapper.selectById(fileId);
            if (file == null) {
                throw exception(FILE_NOT_EXISTS);
            }

            // 2. 校验目标配置存在
            FileConfigDO targetConfig = fileConfigMapper.selectById(targetConfigId);
            if (targetConfig == null) {
                throw exception(FILE_CONFIG_NOT_EXISTS);
            }

            // 3. 如果已经是目标配置，跳过
            if (file.getConfigId().equals(targetConfigId)) {
                log.info("[migrateFile][文件已在目标存储] fileId={}", fileId);
                result.addSkip();
                result.complete();
                return result;
            }

            // 4. 从源存储读取文件内容
            FileClient sourceClient = fileConfigService.getFileClient(file.getConfigId());
            byte[] content = sourceClient.getContent(file.getPath());
            if (content == null || content.length == 0) {
                throw new RuntimeException("无法读取文件内容");
            }

            // 5. 上传到目标存储
            FileClient targetClient = fileConfigService.getFileClient(targetConfigId);
            String newUrl = targetClient.upload(content, file.getPath(), file.getType());

            // 6. 更新文件记录
            FileDO updateFile = new FileDO();
            updateFile.setId(fileId);
            updateFile.setConfigId(targetConfigId);
            updateFile.setUrl(newUrl);
            fileMapper.updateById(updateFile);

            // 7. 验证迁移（可选）
            boolean verified = verifyMigration(fileId);
            if (!verified) {
                log.warn("[migrateFile][迁移后验证失败] fileId={}", fileId);
            }

            result.addSuccess();
            log.info("[migrateFile][文件迁移成功] fileId={}, oldUrl={}, newUrl={}", fileId, file.getUrl(), newUrl);

        } catch (Exception e) {
            log.error("[migrateFile][文件迁移失败] fileId={}", fileId, e);
            result.addFail(fileId, fileMapper.selectById(fileId).getName(), e.getMessage());
        }

        result.complete();
        return result;
    }

    @Override
    public FileMigrationResultVO migrateBatch(Long sourceConfigId, Long targetConfigId, Integer batchSize) {
        log.info("[migrateBatch][开始批量迁移] sourceConfigId={}, targetConfigId={}, batchSize={}",
                sourceConfigId, targetConfigId, batchSize);

        // 1. 校验配置存在
        FileConfigDO sourceConfig = fileConfigMapper.selectById(sourceConfigId);
        if (sourceConfig == null) {
            throw exception(FILE_CONFIG_NOT_EXISTS);
        }
        FileConfigDO targetConfig = fileConfigMapper.selectById(targetConfigId);
        if (targetConfig == null) {
            throw exception(FILE_CONFIG_NOT_EXISTS);
        }

        // 2. 查询需要迁移的文件列表
        List<FileDO> files = fileMapper.selectListByConfigId(sourceConfigId);
        if (CollUtil.isEmpty(files)) {
            log.info("[migrateBatch][没有需要迁移的文件]");
            FileMigrationResultVO result = FileMigrationResultVO.createInitial(0);
            result.complete();
            return result;
        }

        // 3. 初始化进度
        FileMigrationResultVO result = FileMigrationResultVO.createInitial(files.size());
        currentProgress.set(result);

        // 4. 分批迁移
        int actualBatchSize = batchSize != null && batchSize > 0 ? batchSize : 100;
        int processedCount = 0;

        for (FileDO file : files) {
            try {
                // 从源存储读取
                FileClient sourceClient = fileConfigService.getFileClient(file.getConfigId());
                byte[] content = sourceClient.getContent(file.getPath());

                if (content == null || content.length == 0) {
                    log.warn("[migrateBatch][文件内容为空] fileId={}, path={}", file.getId(), file.getPath());
                    result.addFail(file.getId(), file.getName(), "文件内容为空");
                    continue;
                }

                // 上传到目标存储
                FileClient targetClient = fileConfigService.getFileClient(targetConfigId);
                String newUrl = targetClient.upload(content, file.getPath(), file.getType());

                // 更新数据库记录
                FileDO updateFile = new FileDO();
                updateFile.setId(file.getId());
                updateFile.setConfigId(targetConfigId);
                updateFile.setUrl(newUrl);
                fileMapper.updateById(updateFile);

                result.addSuccess();
                log.info("[migrateBatch][文件迁移成功] fileId={}, name={}, oldUrl={}, newUrl={}",
                        file.getId(), file.getName(), file.getUrl(), newUrl);

            } catch (Exception e) {
                log.error("[migrateBatch][文件迁移失败] fileId={}, name={}", file.getId(), file.getName(), e);
                result.addFail(file.getId(), file.getName(), e.getMessage());
            }

            processedCount++;

            // 批次控制：每处理一批后暂停一下，避免长时间占用资源
            if (processedCount % actualBatchSize == 0) {
                log.info("[migrateBatch][已处理批次] processed={}, total={}", processedCount, files.size());
                try {
                    Thread.sleep(100); // 暂停100毫秒
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("[migrateBatch][迁移被中断]", e);
                    break;
                }
            }
        }

        // 5. 完成迁移
        result.complete();
        log.info("[migrateBatch][批量迁移完成] total={}, success={}, fail={}, skip={}",
                result.getTotalCount(), result.getSuccessCount(), result.getFailCount(), result.getSkipCount());

        return result;
    }

    @Override
    public FileMigrationResultVO getMigrationProgress() {
        FileMigrationResultVO progress = currentProgress.get();
        if (progress == null) {
            // 没有正在进行的迁移
            return FileMigrationResultVO.builder()
                    .status("IDLE")
                    .totalCount(0)
                    .successCount(0)
                    .failCount(0)
                    .skipCount(0)
                    .progress(0.0)
                    .build();
        }
        return progress;
    }

    @Override
    public boolean verifyMigration(Long fileId) {
        try {
            FileDO file = fileMapper.selectById(fileId);
            if (file == null) {
                return false;
            }

            // 尝试读取文件内容
            FileClient client = fileConfigService.getFileClient(file.getConfigId());
            byte[] content = client.getContent(file.getPath());

            return content != null && content.length > 0;
        } catch (Exception e) {
            log.error("[verifyMigration][验证失败] fileId={}", fileId, e);
            return false;
        }
    }
}

