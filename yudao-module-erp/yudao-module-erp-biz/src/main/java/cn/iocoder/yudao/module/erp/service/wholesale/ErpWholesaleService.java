package cn.iocoder.yudao.module.erp.service.wholesale;



import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.*;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO.ErpWholesaleImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO.ErpWholesaleImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO.ErpWholesalePurchaseAuditImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO.ErpWholesaleSaleAuditImportExcelVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleCombinedDO;

import java.math.BigDecimal;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpWholesaleService {

    // 新增
    Long createWholesale(@Valid ErpWholesaleSaveReqVO createReqVO);

    // 修改
    void updateWholesale(@Valid ErpWholesaleSaveReqVO updateReqVO);

    // 删除
    void deleteWholesale(List<Long> ids);

    // 根据id查询
    ErpWholesaleRespVO getWholesale(Long id);

    // 根据id列表查询
    List<ErpWholesaleBaseDO> getWholesaleList(Collection<Long> ids);

    // 校验有效性
    ErpWholesaleCombinedDO validateWholesale(Long id);

    // 获取 VO 列表
    List<ErpWholesaleRespVO> getWholesaleVOList(Collection<Long> ids);

    // 获取 VO Map
    Map<Long, ErpWholesaleRespVO> getWholesaleVOMap(Collection<Long> ids);

    // 获取 VO 分页
    PageResult<ErpWholesaleRespVO> getWholesaleVOPage(ErpWholesalePageReqVO pageReqVO);

    /**
     * 更新采购审核状态
     *
     * @param id 编号
     * @param purchaseAuditStatus 采购审核状态
     * @param otherFees 其他费用
     */
    void updatePurchaseAuditStatus(Long id, Integer purchaseAuditStatus, BigDecimal otherFees);

    /**
     * 批量更新采购审核状态
     *
     * @param ids 编号列表
     * @param purchaseAuditStatus 采购审核状态
     */
    void batchUpdatePurchaseAuditStatus(List<Long> ids, Integer purchaseAuditStatus);

    /**
     * 更新销售审核状态
     *
     * @param id 编号
     * @param saleAuditStatus 销售审核状态
     * @param otherFees 其他费用
     */
    void updateSaleAuditStatus(Long id, Integer saleAuditStatus, BigDecimal otherFees);

    /**
     * 批量更新销售审核状态
     *
     * @param ids 编号列表
     * @param saleAuditStatus 销售审核状态
     */
    void batchUpdateSaleAuditStatus(List<Long> ids, Integer saleAuditStatus);

    /**
     * 更新采购售后信息
     *
     * @param reqVO 更新信息
     */
    void updatePurchaseAfterSales(@Valid ErpWholesalePurchaseAfterSalesUpdateReqVO reqVO);

    /**
     * 更新销售售后信息
     *
     * @param reqVO 更新信息
     */
    void updateSaleAfterSales(@Valid ErpWholesaleSaleAfterSalesUpdateReqVO reqVO);

    /**
     * 批量更新采购售后状态
     *
     * @param ids 编号列表
     * @param purchaseAfterSalesStatus 采购售后状态
     */
    void batchUpdatePurchaseAfterSales(List<Long> ids, Integer purchaseAfterSalesStatus);

    /**
     * 批量更新销售售后状态
     *
     * @param ids 编号列表
     * @param saleAfterSalesStatus 销售售后状态
     */
    void batchUpdateSaleAfterSales(List<Long> ids, Integer saleAfterSalesStatus);

    ErpWholesaleImportRespVO importWholesaleList(List<ErpWholesaleImportExcelVO> list, Boolean updateSupport);

    /**
     * 导入批发采购审核列表
     *
     * @param list 导入数据列表
     * @param updateSupport 是否支持更新
     * @return 导入结果
     */
    ErpWholesaleImportRespVO importWholesalePurchaseAuditList(List<ErpWholesalePurchaseAuditImportExcelVO> list, Boolean updateSupport);

    /**
     * 导入批发销售审核列表
     *
     * @param list 导入数据列表
     * @param updateSupport 是否支持更新
     * @return 导入结果
     */
    ErpWholesaleImportRespVO importWholesaleSaleAuditList(List<ErpWholesaleSaleAuditImportExcelVO> list, Boolean updateSupport);
}
