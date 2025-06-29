package cn.iocoder.yudao.module.erp.service.dropship;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.dropship.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.dropship.ErpDropshipAssistDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpDropshipAssistService {

    // 新增代发辅助记录
    Long createDropshipAssist(@Valid ErpDropshipAssistSaveReqVO createReqVO);

    // 修改代发辅助记录
    void updateDropshipAssist(@Valid ErpDropshipAssistSaveReqVO updateReqVO);

    // 删除代发辅助记录
    void deleteDropshipAssist(List<Long> ids);

    // 根据id查询代发辅助记录
    ErpDropshipAssistDO getDropshipAssist(Long id);

    // 根据id查询代发辅助记录详情（包含组品信息）
    ErpDropshipAssistRespVO getDropshipAssistDetail(Long id);

    // 根据id列表查询代发辅助记录
    List<ErpDropshipAssistDO> getDropshipAssistList(Collection<Long> ids);

    // 校验代发辅助记录有效性
    ErpDropshipAssistDO validateDropshipAssist(Long id);

    // 获取代发辅助记录VO列表
    List<ErpDropshipAssistRespVO> getDropshipAssistVOList(Collection<Long> ids);

    // 获取代发辅助记录VO Map
    Map<Long, ErpDropshipAssistRespVO> getDropshipAssistVOMap(Collection<Long> ids);

    // 获取代发辅助记录VO分页
    PageResult<ErpDropshipAssistRespVO> getDropshipAssistVOPage(ErpDropshipAssistPageReqVO pageReqVO);

    ErpDropshipAssistImportRespVO importDropshipAssistList(List<ErpDropshipAssistImportExcelVO> importList, boolean isUpdateSupport);
}
