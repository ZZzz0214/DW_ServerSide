package cn.iocoder.yudao.module.erp.service.groupbuyinginfo;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuyinginfo.ErpGroupBuyingInfoDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpGroupBuyingInfoService {

    Long createGroupBuyingInfo(@Valid ErpGroupBuyingInfoSaveReqVO createReqVO);

    void updateGroupBuyingInfo(@Valid ErpGroupBuyingInfoSaveReqVO updateReqVO);

    void deleteGroupBuyingInfo(List<Long> ids);

    ErpGroupBuyingInfoDO getGroupBuyingInfo(Long id);

    ErpGroupBuyingInfoDO validateGroupBuyingInfo(Long id);

    List<ErpGroupBuyingInfoDO> getGroupBuyingInfoList(Collection<Long> ids);

    Map<Long, ErpGroupBuyingInfoDO> getGroupBuyingInfoMap(Collection<Long> ids);

    List<ErpGroupBuyingInfoRespVO> getGroupBuyingInfoVOList(Collection<Long> ids);

    Map<Long, ErpGroupBuyingInfoRespVO> getGroupBuyingInfoVOMap(Collection<Long> ids);

    PageResult<ErpGroupBuyingInfoRespVO> getGroupBuyingInfoVOPage(ErpGroupBuyingInfoPageReqVO pageReqVO);

    /**
     * 导入团购信息列表
     *
     * @param importList 导入列表
     * @param isUpdateSupport 是否支持更新
     * @return 导入结果
     */
    ErpGroupBuyingInfoImportRespVO importGroupBuyingInfoList(List<ErpGroupBuyingInfoImportExcelVO> importList, boolean isUpdateSupport);
}
