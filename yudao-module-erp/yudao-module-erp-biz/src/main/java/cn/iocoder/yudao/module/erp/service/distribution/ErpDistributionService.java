package cn.iocoder.yudao.module.erp.service.distribution;


import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO.ErpDistributionImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO.ErpDistributionImportRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionBaseDO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.*;
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
    ErpDistributionBaseDO getDistribution(Long id);

    // 根据id列表查询
    List<ErpDistributionBaseDO> getDistributionList(Collection<Long> ids);

    // 校验有效性
    ErpDistributionBaseDO validateDistribution(Long id);

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
     */
    void updatePurchaseAuditStatus(Long id, Integer purchaseAuditStatus, BigDecimal otherFees);

    /**
     * 更新销售售后信息
     *
     * @param reqVO 更新信息
     */
    void updateSaleAfterSales(@Valid ErpDistributionSaleAfterSalesUpdateReqVO reqVO);

    /**
     * 更新销售审核状态
     *
     * @param id 编号
     * @param saleAuditStatus 销售审核状态
     * @param otherFees 其他费用
     */
    void updateSaleAuditStatus(Long id, Integer saleAuditStatus, BigDecimal otherFees);

   /**
     * 导入代发订单
     *
     * @param importList 导入列表
     * @param updateSupport 是否支持更新
     * @return 导入结果
     */
    ErpDistributionImportRespVO importDistributionList(List<ErpDistributionImportExcelVO> importList, boolean updateSupport);
  }
