package cn.iocoder.yudao.module.erp.service.sale;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.wholesaleorder.ErpWholesaleSaleOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.wholesaleorder.ErpWholesaleSaleOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_sale.ErpWholesaleSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_sale.ErpWholesaleSaleOrderItemDO;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ERP 批发销售订单 Service 接口
 */
public interface ErpWholesaleSaleOrderService {

    /**
     * 创建批发销售订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWholesaleSaleOrder(@Valid ErpWholesaleSaleOrderSaveReqVO createReqVO);

    /**
     * 更新批发销售订单
     *
     * @param updateReqVO 更新信息
     */
    void updateWholesaleSaleOrder(@Valid ErpWholesaleSaleOrderSaveReqVO updateReqVO);

    /**
     * 更新批发销售订单的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateWholesaleSaleOrderStatus(Long id, Integer status);

    /**
     * 更新批发销售订单的出库数量
     *
     * @param id 编号
     * @param outCountMap 出库数量 Map：key 批发销售订单项编号；value 出库数量
     */
    void updateWholesaleSaleOrderOutCount(Long id, Map<Long, BigDecimal> outCountMap);

    /**
     * 更新批发销售订单的退货数量
     *
     * @param orderId 编号
     * @param returnCountMap 退货数量 Map：key 批发销售订单项编号；value 退货数量
     */
    void updateWholesaleSaleOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap);

    /**
     * 删除批发销售订单
     *
     * @param ids 编号数组
     */
    void deleteWholesaleSaleOrder(List<Long> ids);

    /**
     * 获得批发销售订单
     *
     * @param id 编号
     * @return 批发销售订单
     */
    ErpWholesaleSaleOrderDO getWholesaleSaleOrder(Long id);

    /**
     * 校验批发销售订单，已经审核通过
     *
     * @param id 编号
     * @return 批发销售订单
     */
    ErpWholesaleSaleOrderDO validateWholesaleSaleOrder(Long id);

    /**
     * 获得批发销售订单分页
     *
     * @param pageReqVO 分页查询
     * @return 批发销售订单分页
     */
    PageResult<ErpWholesaleSaleOrderDO> getWholesaleSaleOrderPage(ErpWholesaleSaleOrderPageReqVO pageReqVO);

    // ==================== 批发销售订单项 ====================

    /**
     * 获得批发销售订单项列表
     *
     * @param orderId 批发销售订单编号
     * @return 批发销售订单项列表
     */
    List<ErpWholesaleSaleOrderItemDO> getWholesaleSaleOrderItemListByOrderId(Long orderId);

    /**
     * 获得批发销售订单项 List
     *
     * @param orderIds 批发销售订单编号数组
     * @return 批发销售订单项 List
     */
    List<ErpWholesaleSaleOrderItemDO> getWholesaleSaleOrderItemListByOrderIds(Collection<Long> orderIds);
}
