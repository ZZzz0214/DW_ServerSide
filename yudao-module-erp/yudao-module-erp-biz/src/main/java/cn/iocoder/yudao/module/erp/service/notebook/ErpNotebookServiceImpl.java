package cn.iocoder.yudao.module.erp.service.notebook;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.notebook.ErpNotebookDO;
import cn.iocoder.yudao.module.erp.dal.mysql.notebook.ErpNotebookMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.system.enums.DictTypeConstants.*;

@Service
@Validated
public class ErpNotebookServiceImpl implements ErpNotebookService {

    @Resource
    private ErpNotebookMapper notebookMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private DictDataApi dictDataApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNotebook(ErpNotebookSaveReqVO createReqVO) {
        // 1. 校验数据
        validateNotebookForCreateOrUpdate(null, createReqVO);

        // 2. 生成记事本编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.NOTEBOOK_NO_PREFIX);
        if (notebookMapper.selectByNo(no) != null) {
            throw exception(NOTEBOOK_NO_EXISTS);
        }

        // 3. 插入记事本记录
        ErpNotebookDO notebook = BeanUtils.toBean(createReqVO, ErpNotebookDO.class)
                .setNo(no);
        notebookMapper.insert(notebook);

        return notebook.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotebook(ErpNotebookSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateNotebook(updateReqVO.getId());
        // 1.2 校验数据
        validateNotebookForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新记事本记录
        ErpNotebookDO updateObj = BeanUtils.toBean(updateReqVO, ErpNotebookDO.class);
        notebookMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotebook(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpNotebookDO> notebooks = notebookMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(notebooks)) {
            throw exception(NOTEBOOK_NOT_EXISTS);
        }
        // 2. 删除记事本记录
        notebookMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpNotebookDO getNotebook(Long id) {
        return notebookMapper.selectById(id);
    }

    @Override
    public ErpNotebookDO validateNotebook(Long id) {
        ErpNotebookDO notebook = notebookMapper.selectById(id);
        if (notebook == null) {
            throw exception(NOTEBOOK_NOT_EXISTS);
        }
        return notebook;
    }

    @Override
    public PageResult<ErpNotebookRespVO> getNotebookVOPage(ErpNotebookPageReqVO pageReqVO) {
        return notebookMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpNotebookRespVO> getNotebookVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpNotebookDO> list = notebookMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpNotebookRespVO.class);
    }

    @Override
    public Map<Long, ErpNotebookRespVO> getNotebookVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getNotebookVOList(ids), ErpNotebookRespVO::getId);
    }

    @Override
    public List<ErpNotebookDO> getNotebookList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return notebookMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpNotebookDO> getNotebookMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getNotebookList(ids), ErpNotebookDO::getId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpNotebookImportRespVO importNotebookList(List<ErpNotebookImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(NOTEBOOK_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpNotebookImportRespVO respVO = ErpNotebookImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        try {
            // 1. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateAllImportData(importList, isUpdateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 2. 批量处理列表
            List<ErpNotebookDO> createList = new ArrayList<>();
            List<ErpNotebookDO> updateList = new ArrayList<>();

            // 3. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpNotebookImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpNotebookDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(notebookMapper.selectListByNoIn(noSet), ErpNotebookDO::getNo);

            // 4. 批量转换和保存数据
            for (int i = 0; i < importList.size(); i++) {
                ErpNotebookImportExcelVO importVO = importList.get(i);

                // 数据转换
                ErpNotebookDO notebook = convertImportVOToDO(importVO);

                // 判断是新增还是更新
                ErpNotebookDO existNotebook = existMap.get(importVO.getNo());
                if (existNotebook == null) {
                    // 创建记事本
                    notebook.setNo(noRedisDAO.generate(ErpNoRedisDAO.NOTEBOOK_NO_PREFIX));
                    createList.add(notebook);
                    respVO.getCreateNames().add(notebook.getTaskName());
                } else if (isUpdateSupport) {
                    // 更新记事本
                    notebook.setId(existNotebook.getId());
                    updateList.add(notebook);
                    respVO.getUpdateNames().add(notebook.getTaskName());
                }
            }

            // 5. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                notebookMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(notebookMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        } finally {
            // 清除转换错误
            ConversionErrorHolder.clearErrors();
        }

        return respVO;
    }

    /**
     * 统一校验所有导入数据（包括数据类型校验和业务逻辑校验）
     * 如果出现任何错误信息都记录下来并返回，后续操作就不进行了
     */
    private Map<String, String> validateAllImportData(List<ErpNotebookImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpNotebookImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpNotebookDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(notebookMapper.selectListByNoIn(noSet), ErpNotebookDO::getNo);

        // 3. 批量查询任务状态字典数据
        List<DictDataRespDTO> taskStatusDictList = dictDataApi.getDictDataList(ERP_NOTEBOOK_STATUS);
        Set<String> validTaskStatusSet = taskStatusDictList.stream()
                .map(DictDataRespDTO::getValue)
                .collect(Collectors.toSet());

        // 4. 批量查询任务人员字典数据
        List<DictDataRespDTO> taskPersonDictList = dictDataApi.getDictDataList(SYSTEM_USER_LIST);
        Set<String> validTaskPersonSet = taskPersonDictList.stream()
                .map(DictDataRespDTO::getValue)
                .collect(Collectors.toSet());

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();

        // 5. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpNotebookImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getTaskName()) ? "(" + importVO.getTaskName() + ")" : "");

            try {
                // 5.1 基础数据校验

                // 5.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "记事本编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 5.3 校验任务状态是否有效
                if (importVO.getTaskStatus() != null) {
                    if (!validTaskStatusSet.contains(importVO.getTaskStatus().toString())) {
                        allErrors.put(errorKey, "任务状态不存在: " + importVO.getTaskStatus());
                        continue;
                    }
                }

                // 5.4 校验任务人员是否有效
                if (StrUtil.isNotBlank(importVO.getTaskPerson())) {
                    if (!validTaskPersonSet.contains(importVO.getTaskPerson())) {
                        allErrors.put(errorKey, "任务人员不存在: " + importVO.getTaskPerson());
                        continue;
                    }
                }

                // 5.5 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpNotebookDO notebook = convertImportVOToDO(importVO);
                    if (notebook == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 5.6 判断是新增还是更新，并进行相应校验
                ErpNotebookDO existNotebook = existMap.get(importVO.getNo());
                if (existNotebook == null) {
                    // 新增校验：无需额外校验
                } else if (isUpdateSupport) {
                    // 更新校验：无需额外校验
                } else {
                    allErrors.put(errorKey, "记事本编号不存在且不支持更新: " + importVO.getNo());
                    continue;
                }
            } catch (Exception ex) {
                allErrors.put(errorKey, "系统异常: " + ex.getMessage());
            }
        }

        return allErrors;
    }

    /**
     * 数据类型校验前置检查
     * 检查所有转换错误，如果有错误则返回错误信息，不进行后续导入
     */
    private Map<String, String> validateDataTypeErrors(List<ErpNotebookImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取记事本名称 - 修复行号索引问题
                String notebookName = "未知记事本";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpNotebookImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getTaskName())) {
                        notebookName = importVO.getTaskName();
                    } else if (StrUtil.isNotBlank(importVO.getNo())) {
                        notebookName = importVO.getNo();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + notebookName + ")";
                List<String> errorMessages = new ArrayList<>();

                for (ConversionErrorHolder.ConversionError error : errors) {
                    errorMessages.add(error.getErrorMessage());
                }

                String errorMsg = String.join("; ", errorMessages);
                dataTypeErrors.put(errorKey, "数据类型错误: " + errorMsg);
            }
        }

        return dataTypeErrors;
    }

    /**
     * 将导入VO转换为DO
     */
    private ErpNotebookDO convertImportVOToDO(ErpNotebookImportExcelVO importVO) {
        if (importVO == null) {
            return null;
        }

        try {
            // 使用BeanUtils进行基础转换
            ErpNotebookDO notebook = BeanUtils.toBean(importVO, ErpNotebookDO.class);
            return notebook;
        } catch (Exception e) {
            System.err.println("转换记事本导入VO到DO失败: " + e.getMessage());
            return null;
        }
    }

    private void validateNotebookForCreateOrUpdate(Long id, ErpNotebookSaveReqVO reqVO) {
        // 1. 校验记事本编号唯一
        ErpNotebookDO notebook = notebookMapper.selectByNo(reqVO.getNo());
        if (notebook != null && !ObjectUtil.equal(notebook.getId(), id)) {
            throw exception(NOTEBOOK_NO_EXISTS);
        }
    }
}
