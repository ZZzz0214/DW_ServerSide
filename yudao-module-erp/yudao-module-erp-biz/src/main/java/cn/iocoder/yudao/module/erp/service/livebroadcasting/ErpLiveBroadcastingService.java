package cn.iocoder.yudao.module.erp.service.livebroadcasting;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcasting.ErpLiveBroadcastingDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpLiveBroadcastingService {

    Long createLiveBroadcasting(@Valid ErpLiveBroadcastingSaveReqVO createReqVO);

    void updateLiveBroadcasting(@Valid ErpLiveBroadcastingSaveReqVO updateReqVO);

    void deleteLiveBroadcasting(List<Long> ids);

    ErpLiveBroadcastingDO getLiveBroadcasting(Long id);

    ErpLiveBroadcastingDO validateLiveBroadcasting(Long id);

    List<ErpLiveBroadcastingDO> getLiveBroadcastingList(Collection<Long> ids);

    Map<Long, ErpLiveBroadcastingDO> getLiveBroadcastingMap(Collection<Long> ids);

    List<ErpLiveBroadcastingRespVO> getLiveBroadcastingVOList(Collection<Long> ids);

    Map<Long, ErpLiveBroadcastingRespVO> getLiveBroadcastingVOMap(Collection<Long> ids);

    PageResult<ErpLiveBroadcastingRespVO> getLiveBroadcastingVOPage(ErpLiveBroadcastingPageReqVO pageReqVO);

    /**
     * 导入直播货盘
     *
     * @param importList 导入数据列表
     * @param isUpdateSupport 是否支持更新
     * @return 导入结果
     */
    ErpLiveBroadcastingImportRespVO importLiveBroadcastingList(List<ErpLiveBroadcastingImportExcelVO> importList, boolean isUpdateSupport);
}
