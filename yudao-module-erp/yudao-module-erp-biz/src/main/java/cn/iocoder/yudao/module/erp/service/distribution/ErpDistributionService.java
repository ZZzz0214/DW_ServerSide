package cn.iocoder.yudao.module.erp.service.distribution;


import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO.ErpDistributionImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO.ErpDistributionImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO.ErpDistributionPurchaseAuditImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO.ErpDistributionSaleAuditImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO.ErpDistributionLogisticsImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpDistributionMissingPriceVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePricePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionBaseDO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionCombinedDO;

import java.math.BigDecimal;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpDistributionService {

    // 新增
    Long createDistribution(@Valid ErpDistributionSaveReqVO createReqVO);

    // 修改
    void updateDistribution(@Valid ErpDistributionSaveReqVO updateReqVO);

    // 删除
    void deleteDistribution(List<Long> ids);

    // 根据id查询
    //ErpDistributionBaseDO getDistribution(Long id);

    // 根据id列表查询
    List<ErpDistributionBaseDO> getDistributionList(Collection<Long> ids);

    // 校验有效性
    ErpDistributionCombinedDO validateDistribution(Long id);

    // 获取 VO 列表
    List<ErpDistributionRespVO> getDistributionVOList(Collection<Long> ids);

    // 获取 VO Map
    Map<Long, ErpDistributionRespVO> getDistributionVOMap(Collection<Long> ids);

    // 获取 VO 分页
    PageResult<ErpDistributionRespVO> getDistributionVOPage(ErpDistributionPageReqVO pageReqVO);

    /**
     * 更新代发单的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateDistributionStatus(Long id, Integer status, BigDecimal otherFees);
      /**
     * 更新采购售后信息
     *
     * @param reqVO 更新信息
     */
    void updatePurchaseAfterSales(@Valid ErpDistributionPurchaseAfterSalesUpdateReqVO reqVO);

    /**
     * 更新采购审核状态
     *
     * @param id 编号
     * @param purchaseAuditStatus 采购审核状态
     * @param otherFees 其他费用
     * @param purchaseAuditTotalAmount 代发采购审核总额
     */
    void updatePurchaseAuditStatus(Long id, Integer purchaseAuditStatus, BigDecimal otherFees, BigDecimal purchaseAuditTotalAmount);

    /**
     * 批量更新采购审核状态
     *
     * @param ids 编号列表
     * @param purchaseAuditStatus 采购审核状态
     */
    void batchUpdatePurchaseAuditStatus(List<Long> ids, Integer purchaseAuditStatus);

    /**
     * 更新销售售后信息
     *
     * @param reqVO 更新信息
     */
    void updateSaleAfterSales(@Valid ErpDistributionSaleAfterSalesUpdateReqVO reqVO);

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

    /**
     * 更新销售审核状态
     *
     * @param id 编号
     * @param saleAuditStatus 销售审核状态
     * @param otherFees 其他费用
     * @param saleAuditTotalAmount 代发销售审核总额
     */
    void updateSaleAuditStatus(Long id, Integer saleAuditStatus, BigDecimal otherFees, BigDecimal saleAuditTotalAmount);

    /**
     * 批量更新销售审核状态
     *
     * @param ids 编号列表
     * @param saleAuditStatus 销售审核状态
     */
    void batchUpdateSaleAuditStatus(List<Long> ids, Integer saleAuditStatus);

   /**
     * 导入代发订单
     *
     * @param importList 导入列表
     * @param updateSupport 是否支持更新
     * @return 导入结果
     */
    ErpDistributionImportRespVO importDistributionList(List<ErpDistributionImportExcelVO> importList, boolean updateSupport);

    /**
     * 导入代发采购审核
     *
     * @param importList 导入列表
     * @return 导入结果
     */
    ErpDistributionImportRespVO importPurchaseAuditList(List<ErpDistributionPurchaseAuditImportExcelVO> importList);

    /**
     * 导入代发销售审核
     *
     * @param importList 导入列表
     * @return 导入结果
     */
    ErpDistributionImportRespVO importSaleAuditList(List<ErpDistributionSaleAuditImportExcelVO> importList);

    /**
     * 导入代发物流信息
     *
     * @param importList 导入列表
     * @return 导入结果
     */
    ErpDistributionImportRespVO importLogisticsList(List<ErpDistributionLogisticsImportExcelVO> importList);

    ErpDistributionRespVO getDistribution(Long id);

    /**
     * 手动全量同步代发数据到ES
     */
    void manualFullSyncToES();

    /**
     * 获取代发缺失价格记录
     *
     * @param pageReqVO 分页查询参数
     * @return 代发缺失价格记录分页
     */
    PageResult<ErpDistributionMissingPriceVO> getDistributionMissingPrices(ErpSalePricePageReqVO pageReqVO);

    /**
     * Scroll API全量查，返回VO列表（用于采购导出和出货导出）
     */
    List<ErpDistributionRespVO> exportAllDistributions(ErpDistributionPageReqVO pageReqVO);
}
