package cn.iocoder.yudao.module.erp.service.sale;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.wholesaleorder.ErpWholesaleSaleOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.wholesaleorder.ErpWholesaleSaleOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_sale.ErpWholesaleSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_sale.ErpWholesaleSaleOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpWholesaleSaleOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpWholesaleSaleOrderMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpWholesaleSaleOrderServiceImpl implements ErpWholesaleSaleOrderService {

    @Resource
    private ErpWholesaleSaleOrderMapper wholesaleSaleOrderMapper;
    @Resource
    private ErpWholesaleSaleOrderItemMapper wholesaleSaleOrderItemMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpCustomerService customerService;
    @Resource
    private ErpAccountService accountService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWholesaleSaleOrder(ErpWholesaleSaleOrderSaveReqVO createReqVO) {
        // 1.1 校验订单项的有效性
        List<ErpWholesaleSaleOrderItemDO> orderItems = validateWholesaleSaleOrderItems(createReqVO.getItems());
        // 1.2 校验客户
        customerService.validateCustomer(createReqVO.getCustomerId());
        // 1.3 校验结算账户
        if (createReqVO.getAccountId() != null) {
            accountService.validateAccount(createReqVO.getAccountId());
        }
        // 1.4 校验销售人员
        if (createReqVO.getSaleUserId() != null) {
            adminUserApi.validateUser(createReqVO.getSaleUserId());
        }
        // 1.5 生成订单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.SALE_ORDER_NO_PREFIX);
        if (wholesaleSaleOrderMapper.selectByNo(no) != null) {
            throw exception(SALE_ORDER_NO_EXISTS);
        }

        // 2.1 插入订单
        ErpWholesaleSaleOrderDO order = BeanUtils.toBean(createReqVO, ErpWholesaleSaleOrderDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.PROCESS.getStatus()));
        calculateTotalPrice(order, orderItems);
        wholesaleSaleOrderMapper.insert(order);
        // 2.2 插入订单项
        orderItems.forEach(o -> o.setOrderId(order.getId()));
        wholesaleSaleOrderItemMapper.insertBatch(orderItems);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWholesaleSaleOrder(ErpWholesaleSaleOrderSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpWholesaleSaleOrderDO order = validateWholesaleSaleOrderExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(order.getStatus())) {
            throw exception(SALE_ORDER_UPDATE_FAIL_APPROVE, order.getNo());
        }
        // 1.2 校验客户
        customerService.validateCustomer(updateReqVO.getCustomerId());
        // 1.3 校验结算账户
        if (updateReqVO.getAccountId() != null) {
            accountService.validateAccount(updateReqVO.getAccountId());
        }
        // 1.4 校验销售人员
        if (updateReqVO.getSaleUserId() != null) {
            adminUserApi.validateUser(updateReqVO.getSaleUserId());
        }
        // 1.5 校验订单项的有效性
        List<ErpWholesaleSaleOrderItemDO> orderItems = validateWholesaleSaleOrderItems(updateReqVO.getItems());

        // 2.1 更新订单
        ErpWholesaleSaleOrderDO updateObj = BeanUtils.toBean(updateReqVO, ErpWholesaleSaleOrderDO.class);
        calculateTotalPrice(updateObj, orderItems);
        wholesaleSaleOrderMapper.updateById(updateObj);
        // 2.2 更新订单项
        updateWholesaleSaleOrderItemList(updateReqVO.getId(), orderItems);
    }

    private void calculateTotalPrice(ErpWholesaleSaleOrderDO order, List<ErpWholesaleSaleOrderItemDO> orderItems) {
        order.setTotalCount(getSumValue(orderItems, ErpWholesaleSaleOrderItemDO::getCount, BigDecimal::add));
        order.setTotalProductPrice(getSumValue(orderItems, ErpWholesaleSaleOrderItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        order.setTotalTaxPrice(getSumValue(orderItems, ErpWholesaleSaleOrderItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
//        order.setTotalPrice(order.getTotalProductPrice().add(order.getTotalTaxPrice()));
        // 计算优惠价格
        if (order.getDiscountPercent() == null) {
            order.setDiscountPercent(BigDecimal.ZERO);
        }
        order.setDiscountPrice(MoneyUtils.priceMultiplyPercent(order.getTotalPrice(), order.getDiscountPercent()));
//        order.setTotalPrice(order.getTotalPrice().subtract(order.getDiscountPrice()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWholesaleSaleOrderStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpWholesaleSaleOrderDO order = validateWholesaleSaleOrderExists(id);
        // 1.2 校验状态
        if (order.getStatus().equals(status)) {
            throw exception(approve ? SALE_ORDER_APPROVE_FAIL : SALE_ORDER_PROCESS_FAIL);
        }
        // 1.3 存在销售出库单，无法反审核
        if (!approve && order.getOutCount().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(SALE_ORDER_PROCESS_FAIL_EXISTS_OUT);
        }
        // 1.4 存在销售退货单，无法反审核
        if (!approve && order.getReturnCount().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(SALE_ORDER_PROCESS_FAIL_EXISTS_RETURN);
        }

        // 2. 更新状态
        int updateCount = wholesaleSaleOrderMapper.updateByIdAndStatus(id, order.getStatus(),
                new ErpWholesaleSaleOrderDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? SALE_ORDER_APPROVE_FAIL : SALE_ORDER_PROCESS_FAIL);
        }
    }

    private List<ErpWholesaleSaleOrderItemDO> validateWholesaleSaleOrderItems(List<ErpWholesaleSaleOrderSaveReqVO.Item> list) {
        // 1. 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpWholesaleSaleOrderSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 2. 转化为 ErpWholesaleSaleOrderItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpWholesaleSaleOrderItemDO.class, item -> {
            item.setTotalPrice(MoneyUtils.priceMultiply(item.getWholesaleProductPrice(), item.getCount()));
            if (item.getTotalPrice() == null) {
                return;
            }
            if (item.getTaxPercent() != null) {
                item.setTaxPrice(MoneyUtils.priceMultiplyPercent(item.getTotalPrice(), item.getTaxPercent()));
            }
        }));
    }

    private void updateWholesaleSaleOrderItemList(Long id, List<ErpWholesaleSaleOrderItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpWholesaleSaleOrderItemDO> oldList = wholesaleSaleOrderItemMapper.selectListByOrderId(id);
        List<List<ErpWholesaleSaleOrderItemDO>> diffList = diffList(oldList, newList,
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setOrderId(id));
            wholesaleSaleOrderItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            wholesaleSaleOrderItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            wholesaleSaleOrderItemMapper.deleteBatchIds(convertList(diffList.get(2), ErpWholesaleSaleOrderItemDO::getId));
        }
    }

    @Override
    public void updateWholesaleSaleOrderOutCount(Long id, Map<Long, BigDecimal> outCountMap) {
        List<ErpWholesaleSaleOrderItemDO> orderItems = wholesaleSaleOrderItemMapper.selectListByOrderId(id);
        // 1. 更新每个批发销售订单项
        orderItems.forEach(item -> {
            BigDecimal outCount = outCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getOutCount().equals(outCount)) {
                return;
            }
            if (outCount.compareTo(item.getCount()) > 0) {
                throw exception(SALE_ORDER_ITEM_OUT_FAIL_PRODUCT_EXCEED,
                        productService.getProduct(item.getProductId()).getName(), item.getCount());
            }
            wholesaleSaleOrderItemMapper.updateById(new ErpWholesaleSaleOrderItemDO().setId(item.getId()).setOutCount(outCount));
        });
        // 2. 更新批发销售订单
        BigDecimal totalOutCount = getSumValue(outCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        wholesaleSaleOrderMapper.updateById(new ErpWholesaleSaleOrderDO().setId(id).setOutCount(totalOutCount));
    }

    @Override
    public void updateWholesaleSaleOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap) {
        List<ErpWholesaleSaleOrderItemDO> orderItems = wholesaleSaleOrderItemMapper.selectListByOrderId(orderId);
        // 1. 更新每个批发销售订单项
        orderItems.forEach(item -> {
            BigDecimal returnCount = returnCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getReturnCount().equals(returnCount)) {
                return;
            }
            if (returnCount.compareTo(item.getOutCount()) > 0) {
                throw exception(SALE_ORDER_ITEM_RETURN_FAIL_OUT_EXCEED,
                        productService.getProduct(item.getProductId()).getName(), item.getOutCount());
            }
            wholesaleSaleOrderItemMapper.updateById(new ErpWholesaleSaleOrderItemDO().setId(item.getId()).setReturnCount(returnCount));
        });
        // 2. 更新批发销售订单
        BigDecimal totalReturnCount = getSumValue(returnCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        wholesaleSaleOrderMapper.updateById(new ErpWholesaleSaleOrderDO().setId(orderId).setReturnCount(totalReturnCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWholesaleSaleOrder(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpWholesaleSaleOrderDO> orders = wholesaleSaleOrderMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(orders)) {
            return;
        }
        orders.forEach(order -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(order.getStatus())) {
                throw exception(SALE_ORDER_DELETE_FAIL_APPROVE, order.getNo());
            }
        });

        // 2. 遍历删除
        orders.forEach(order -> {
            // 2.1 删除订单
            wholesaleSaleOrderMapper.deleteById(order.getId());
            // 2.2 删除订单项
            wholesaleSaleOrderItemMapper.deleteByOrderId(order.getId());
        });
    }

    private ErpWholesaleSaleOrderDO validateWholesaleSaleOrderExists(Long id) {
        ErpWholesaleSaleOrderDO order = wholesaleSaleOrderMapper.selectById(id);
        if (order == null) {
            throw exception(SALE_ORDER_NOT_EXISTS);
        }
        return order;
    }

    @Override
    public ErpWholesaleSaleOrderDO getWholesaleSaleOrder(Long id) {
        return wholesaleSaleOrderMapper.selectById(id);
    }

    @Override
    public ErpWholesaleSaleOrderDO validateWholesaleSaleOrder(Long id) {
        ErpWholesaleSaleOrderDO order = validateWholesaleSaleOrderExists(id);
        if (ObjectUtil.notEqual(order.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(SALE_ORDER_NOT_APPROVE);
        }
        return order;
    }

    @Override
    public PageResult<ErpWholesaleSaleOrderDO> getWholesaleSaleOrderPage(ErpWholesaleSaleOrderPageReqVO pageReqVO) {
        return wholesaleSaleOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpWholesaleSaleOrderItemDO> getWholesaleSaleOrderItemListByOrderId(Long orderId) {
        return wholesaleSaleOrderItemMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<ErpWholesaleSaleOrderItemDO> getWholesaleSaleOrderItemListByOrderIds(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return wholesaleSaleOrderItemMapper.selectListByOrderIds(orderIds);
    }
}
