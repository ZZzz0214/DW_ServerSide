package cn.iocoder.yudao.module.erp.dal.mysql.purchase;


import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_purchase.ErpWholesalePurchaseOrderItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpWholesalePurchaseOrderItemMapper extends BaseMapperX<ErpWholesalePurchaseOrderItemDO> {

    default List<ErpWholesalePurchaseOrderItemDO> selectListByOrderId(Long orderId) {
        return selectList(ErpWholesalePurchaseOrderItemDO::getOrderId, orderId);
    }

    default List<ErpWholesalePurchaseOrderItemDO> selectListByOrderIds(Collection<Long> orderIds) {
        return selectList(ErpWholesalePurchaseOrderItemDO::getOrderId, orderIds);
    }

    default int deleteByOrderId(Long orderId) {
        return delete(ErpWholesalePurchaseOrderItemDO::getOrderId, orderId);
    }
}
