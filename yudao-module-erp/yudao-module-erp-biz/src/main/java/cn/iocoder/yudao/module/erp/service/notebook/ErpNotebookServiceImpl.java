package cn.iocoder.yudao.module.erp.service.notebook;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.notebook.ErpNotebookDO;
import cn.iocoder.yudao.module.erp.dal.mysql.notebook.ErpNotebookMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpNotebookServiceImpl implements ErpNotebookService {

    @Resource
    private ErpNotebookMapper notebookMapper;
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

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

        // 批量处理
        List<ErpNotebookDO> createList = new ArrayList<>();
        List<ErpNotebookDO> updateList = new ArrayList<>();

        try {
            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpNotebookImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpNotebookDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(notebookMapper.selectListByNoIn(noSet), ErpNotebookDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();
            
            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpNotebookImportExcelVO importVO = importList.get(i);
                try {
                    // 校验必填字段
                    if (StrUtil.isBlank(importVO.getTaskName())) {
                        throw exception(NOTEBOOK_IMPORT_TASK_NAME_EMPTY, i + 1);
                    }
                    if (StrUtil.isBlank(importVO.getTaskPerson())) {
                        throw exception(NOTEBOOK_IMPORT_TASK_PERSON_EMPTY, i + 1);
                    }
                    if (importVO.getTaskStatus() == null) {
                        throw exception(NOTEBOOK_IMPORT_TASK_STATUS_INVALID, i + 1);
                    }

                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(NOTEBOOK_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 判断是否支持更新
                    ErpNotebookDO existNotebook = existMap.get(importVO.getNo());
                    if (existNotebook == null) {
                        // 创建 - 自动生成新的no编号
                        ErpNotebookDO notebook = BeanUtils.toBean(importVO, ErpNotebookDO.class);
                        notebook.setNo(noRedisDAO.generate(ErpNoRedisDAO.NOTEBOOK_NO_PREFIX));
                        createList.add(notebook);
                        respVO.getCreateNames().add(notebook.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpNotebookDO updateNotebook = BeanUtils.toBean(importVO, ErpNotebookDO.class);
                        updateNotebook.setId(existNotebook.getId());
                        updateList.add(updateNotebook);
                        respVO.getUpdateNames().add(updateNotebook.getNo());
                    } else {
                        throw exception(NOTEBOOK_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
                    }
                } catch (ServiceException ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");
                    respVO.getFailureNames().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");
                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                notebookMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(notebookMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }

    private void validateNotebookForCreateOrUpdate(Long id, ErpNotebookSaveReqVO reqVO) {
        // 1. 校验记事本编号唯一
        ErpNotebookDO notebook = notebookMapper.selectByNo(reqVO.getNo());
        if (notebook != null && !ObjectUtil.equal(notebook.getId(), id)) {
            throw exception(NOTEBOOK_NO_EXISTS);
        }
    }
}