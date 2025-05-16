package cn.iocoder.yudao.module.erp.service.notebook;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    private void validateNotebookForCreateOrUpdate(Long id, ErpNotebookSaveReqVO reqVO) {
        // 1. 校验记事本编号唯一
        ErpNotebookDO notebook = notebookMapper.selectByNo(reqVO.getNo());
        if (notebook != null && !notebook.getId().equals(id)) {
            throw exception(NOTEBOOK_NO_EXISTS);
        }
    }
}