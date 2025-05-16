package cn.iocoder.yudao.module.erp.service.privatebroadcastinginfo;



import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastinginfo.ErpPrivateBroadcastingInfoDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpPrivateBroadcastingInfoService {

    Long createPrivateBroadcastingInfo(@Valid ErpPrivateBroadcastingInfoSaveReqVO createReqVO);

    void updatePrivateBroadcastingInfo(@Valid ErpPrivateBroadcastingInfoSaveReqVO updateReqVO);

    void deletePrivateBroadcastingInfo(List<Long> ids);

    ErpPrivateBroadcastingInfoDO getPrivateBroadcastingInfo(Long id);

    ErpPrivateBroadcastingInfoDO validatePrivateBroadcastingInfo(Long id);

    List<ErpPrivateBroadcastingInfoDO> getPrivateBroadcastingInfoList(Collection<Long> ids);

    Map<Long, ErpPrivateBroadcastingInfoDO> getPrivateBroadcastingInfoMap(Collection<Long> ids);

    List<ErpPrivateBroadcastingInfoRespVO> getPrivateBroadcastingInfoVOList(Collection<Long> ids);

    Map<Long, ErpPrivateBroadcastingInfoRespVO> getPrivateBroadcastingInfoVOMap(Collection<Long> ids);

    PageResult<ErpPrivateBroadcastingInfoRespVO> getPrivateBroadcastingInfoVOPage(ErpPrivateBroadcastingInfoPageReqVO pageReqVO);
}