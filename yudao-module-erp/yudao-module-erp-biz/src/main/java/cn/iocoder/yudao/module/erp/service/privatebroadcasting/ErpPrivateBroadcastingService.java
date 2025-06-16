package cn.iocoder.yudao.module.erp.service.privatebroadcasting;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcasting.ErpPrivateBroadcastingDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpPrivateBroadcastingService {

    Long createPrivateBroadcasting(@Valid ErpPrivateBroadcastingSaveReqVO createReqVO);

    void updatePrivateBroadcasting(@Valid ErpPrivateBroadcastingSaveReqVO updateReqVO);

    void deletePrivateBroadcasting(List<Long> ids);

    ErpPrivateBroadcastingDO getPrivateBroadcasting(Long id);

    ErpPrivateBroadcastingDO validatePrivateBroadcasting(Long id);

    List<ErpPrivateBroadcastingDO> getPrivateBroadcastingList(Collection<Long> ids);

    Map<Long, ErpPrivateBroadcastingDO> getPrivateBroadcastingMap(Collection<Long> ids);

    List<ErpPrivateBroadcastingRespVO> getPrivateBroadcastingVOList(Collection<Long> ids);

    Map<Long, ErpPrivateBroadcastingRespVO> getPrivateBroadcastingVOMap(Collection<Long> ids);

    PageResult<ErpPrivateBroadcastingRespVO> getPrivateBroadcastingVOPage(ErpPrivateBroadcastingPageReqVO pageReqVO);

    /**
     * 导入私播货盘
     *
     * @param importList 导入数据列表
     * @param isUpdateSupport 是否支持更新
     * @return 导入结果
     */
    ErpPrivateBroadcastingImportRespVO importPrivateBroadcastingList(List<ErpPrivateBroadcastingImportExcelVO> importList, boolean isUpdateSupport);
}