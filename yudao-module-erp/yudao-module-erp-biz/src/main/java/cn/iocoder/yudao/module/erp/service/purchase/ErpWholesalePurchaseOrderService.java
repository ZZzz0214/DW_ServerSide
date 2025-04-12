package cn.iocoder.yudao.module.erp.service.purchase;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.wholesale_purchase.ErpWholesalePurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.wholesale_purchase.ErpWholesalePurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_purchase.ErpWholesalePurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_purchase.ErpWholesalePurchaseOrderItemDO;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ERP 批发采购订单 Service 接口
 */
public interface ErpWholesalePurchaseOrderService {

    /**
     * 创建批发采购订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWholesalePurchaseOrder(@Valid ErpWholesalePurchaseOrderSaveReqVO createReqVO);

    /**
     * 更新批发采购订单
     *
     * @param updateReqVO 更新信息
     */
    void updateWholesalePurchaseOrder(@Valid ErpWholesalePurchaseOrderSaveReqVO updateReqVO);

    /**
     * 更新批发采购订单的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateWholesalePurchaseOrderStatus(Long id, Integer status);

    /**
     * 更新批发采购订单的入库数量
     *
     * @param id 编号
     * @param inCountMap 入库数量 Map：key 采购订单项编号；value 入库数量
     */
    void updateWholesalePurchaseOrderInCount(Long id, Map<Long, BigDecimal> inCountMap);

    /**
     * 更新批发采购订单的退货数量
     *
     * @param orderId 编号
     * @param returnCountMap 退货数量 Map：key 采购订单项编号；value 退货数量
     */
    void updateWholesalePurchaseOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap);

    /**
     * 删除批发采购订单
     *
     * @param ids 编号数组
     */
    void deleteWholesalePurchaseOrder(List<Long> ids);

    /**
     * 获得批发采购订单
     *
     * @param id 编号
     * @return 批发采购订单
     */
    ErpWholesalePurchaseOrderDO getWholesalePurchaseOrder(Long id);

    /**
     * 校验批发采购订单，已经审核通过
     *
     * @param id 编号
     * @return 批发采购订单
     */
    ErpWholesalePurchaseOrderDO validateWholesalePurchaseOrder(Long id);

    /**
     * 获得批发采购订单分页
     *
     * @param pageReqVO 分页查询
     * @return 批发采购订单分页
     */
    PageResult<ErpWholesalePurchaseOrderDO> getWholesalePurchaseOrderPage(ErpWholesalePurchaseOrderPageReqVO pageReqVO);

    // ==================== 批发采购订单项 ====================

    /**
     * 获得批发采购订单项列表
     *
     * @param orderId 批发采购订单编号
     * @return 批发采购订单项列表
     */
    List<ErpWholesalePurchaseOrderItemDO> getWholesalePurchaseOrderItemListByOrderId(Long orderId);

    /**
     * 获得批发采购订单项 List
     *
     * @param orderIds 批发采购订单编号数组
     * @return 批发采购订单项 List
     */
    List<ErpWholesalePurchaseOrderItemDO> getWholesalePurchaseOrderItemListByOrderIds(Collection<Long> orderIds);
}
