package cn.iocoder.yudao.module.erp.service.transitsale;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.transitsale.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpTransitSaleDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpTransitSaleService {

    // 新增中转销售记录
    Long createTransitSale(@Valid ErpTransitSaleSaveReqVO createReqVO);

    // 修改中转销售记录
    void updateTransitSale(@Valid ErpTransitSaleSaveReqVO updateReqVO);

    // 删除中转销售记录
    void deleteTransitSale(List<Long> ids);

    // 根据id查询中转销售记录
    ErpTransitSaleRespVO getTransitSale(Long id);

    // 根据id列表查询中转销售记录
    List<ErpTransitSaleDO> getTransitSaleList(Collection<Long> ids);

    // 校验中转销售记录有效性
    ErpTransitSaleDO validateTransitSale(Long id);

    // 获取中转销售记录VO列表
    List<ErpTransitSaleRespVO> getTransitSaleVOList(Collection<Long> ids);

    // 获取中转销售记录VO Map
    Map<Long, ErpTransitSaleRespVO> getTransitSaleVOMap(Collection<Long> ids);

    // 获取中转销售记录VO分页
    PageResult<ErpTransitSaleRespVO> getTransitSaleVOPage(ErpTransitSalePageReqVO pageReqVO);

    ErpTransitSaleImportRespVO importTransitSaleList(List<ErpTransitSaleImportExcelVO> importList, boolean isUpdateSupport);
}
