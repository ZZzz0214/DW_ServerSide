package cn.iocoder.yudao.module.erp.service.groupbuying;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpGroupBuyingService {

    Long createGroupBuying(@Valid ErpGroupBuyingSaveReqVO createReqVO);

    void updateGroupBuying(@Valid ErpGroupBuyingSaveReqVO updateReqVO);

    void deleteGroupBuying(List<Long> ids);

    ErpGroupBuyingDO getGroupBuying(Long id);

    ErpGroupBuyingDO validateGroupBuying(Long id);

    List<ErpGroupBuyingDO> getGroupBuyingList(Collection<Long> ids);

    Map<Long, ErpGroupBuyingDO> getGroupBuyingMap(Collection<Long> ids);

    List<ErpGroupBuyingRespVO> getGroupBuyingVOList(Collection<Long> ids);

    Map<Long, ErpGroupBuyingRespVO> getGroupBuyingVOMap(Collection<Long> ids);

    PageResult<ErpGroupBuyingRespVO> getGroupBuyingVOPage(ErpGroupBuyingPageReqVO pageReqVO);

    /**
     * 导入团购货盘列表
     *
     * @param importList 导入列表
     * @param isUpdateSupport 是否支持更新
     * @return 导入结果
     */
    ErpGroupBuyingImportRespVO importGroupBuyingList(List<ErpGroupBuyingImportExcelVO> importList, boolean isUpdateSupport);
}