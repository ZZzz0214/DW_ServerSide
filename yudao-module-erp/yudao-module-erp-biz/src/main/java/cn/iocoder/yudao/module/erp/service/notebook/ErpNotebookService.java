package cn.iocoder.yudao.module.erp.service.notebook;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.notebook.ErpNotebookDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpNotebookService {

    Long createNotebook(@Valid ErpNotebookSaveReqVO createReqVO);

    void updateNotebook(@Valid ErpNotebookSaveReqVO updateReqVO);

    void deleteNotebook(List<Long> ids);

    ErpNotebookDO getNotebook(Long id);

    ErpNotebookDO validateNotebook(Long id);

    List<ErpNotebookDO> getNotebookList(Collection<Long> ids);

    Map<Long, ErpNotebookDO> getNotebookMap(Collection<Long> ids);

    List<ErpNotebookRespVO> getNotebookVOList(Collection<Long> ids);

    Map<Long, ErpNotebookRespVO> getNotebookVOMap(Collection<Long> ids);

    PageResult<ErpNotebookRespVO> getNotebookVOPage(ErpNotebookPageReqVO pageReqVO);
}
