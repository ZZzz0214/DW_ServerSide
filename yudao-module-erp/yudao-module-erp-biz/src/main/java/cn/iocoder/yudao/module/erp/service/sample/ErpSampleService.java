package cn.iocoder.yudao.module.erp.service.sample;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSamplePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sample.ErpSampleDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpSampleService {

    Long createSample(@Valid ErpSampleSaveReqVO createReqVO);

    void updateSample(@Valid ErpSampleSaveReqVO updateReqVO);

    void deleteSample(List<Long> ids);

    ErpSampleDO getSample(Long id);

    ErpSampleDO validateSample(Long id);

    List<ErpSampleDO> getSampleList(Collection<Long> ids);

    Map<Long, ErpSampleDO> getSampleMap(Collection<Long> ids);

    List<ErpSampleRespVO> getSampleVOList(Collection<Long> ids);

    Map<Long, ErpSampleRespVO> getSampleVOMap(Collection<Long> ids);

    PageResult<ErpSampleRespVO> getSampleVOPage(ErpSamplePageReqVO pageReqVO);

    ErpSampleImportRespVO importSampleList(List<ErpSampleImportExcelVO> importList, boolean isUpdateSupport);
}
