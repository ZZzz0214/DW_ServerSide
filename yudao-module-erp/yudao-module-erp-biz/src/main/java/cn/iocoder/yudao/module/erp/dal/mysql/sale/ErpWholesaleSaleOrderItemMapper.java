package cn.iocoder.yudao.module.erp.dal.mysql.sale;


import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_sale.ErpWholesaleSaleOrderItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 批发销售订单项 Mapper
 */
@Mapper
public interface ErpWholesaleSaleOrderItemMapper extends BaseMapperX<ErpWholesaleSaleOrderItemDO> {

    default List<ErpWholesaleSaleOrderItemDO> selectListByOrderId(Long orderId) {
        return selectList(ErpWholesaleSaleOrderItemDO::getOrderId, orderId);
    }

    default List<ErpWholesaleSaleOrderItemDO> selectListByOrderIds(Collection<Long> orderIds) {
        return selectList(ErpWholesaleSaleOrderItemDO::getOrderId, orderIds);
    }

    default int deleteByOrderId(Long orderId) {
        return delete(ErpWholesaleSaleOrderItemDO::getOrderId, orderId);
    }
}
