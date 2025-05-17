package cn.iocoder.yudao.module.erp.service.livebroadcastinginfo;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastinginfo.ErpLiveBroadcastingInfoDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpLiveBroadcastingInfoService {

    Long createLiveBroadcastingInfo(@Valid ErpLiveBroadcastingInfoSaveReqVO createReqVO);

    void updateLiveBroadcastingInfo(@Valid ErpLiveBroadcastingInfoSaveReqVO updateReqVO);

    void deleteLiveBroadcastingInfo(List<Long> ids);

    ErpLiveBroadcastingInfoDO getLiveBroadcastingInfo(Long id);

    ErpLiveBroadcastingInfoDO validateLiveBroadcastingInfo(Long id);

    List<ErpLiveBroadcastingInfoDO> getLiveBroadcastingInfoList(Collection<Long> ids);

    Map<Long, ErpLiveBroadcastingInfoDO> getLiveBroadcastingInfoMap(Collection<Long> ids);

    List<ErpLiveBroadcastingInfoRespVO> getLiveBroadcastingInfoVOList(Collection<Long> ids);

    Map<Long, ErpLiveBroadcastingInfoRespVO> getLiveBroadcastingInfoVOMap(Collection<Long> ids);

    PageResult<ErpLiveBroadcastingInfoRespVO> getLiveBroadcastingInfoVOPage(ErpLiveBroadcastingInfoPageReqVO pageReqVO);
}