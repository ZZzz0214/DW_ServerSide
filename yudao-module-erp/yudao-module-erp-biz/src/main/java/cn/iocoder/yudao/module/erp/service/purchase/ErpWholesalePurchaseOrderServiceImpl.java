package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.wholesale_purchase.ErpWholesalePurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.wholesale_purchase.ErpWholesalePurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_purchase.ErpWholesalePurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_purchase.ErpWholesalePurchaseOrderItemDO;

import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpWholesalePurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpWholesalePurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
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
public class ErpWholesalePurchaseOrderServiceImpl implements ErpWholesalePurchaseOrderService {

    @Resource
    private ErpWholesalePurchaseOrderMapper wholesalePurchaseOrderMapper;
    @Resource
    private ErpWholesalePurchaseOrderItemMapper wholesalePurchaseOrderItemMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpComboProductService comboProductService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWholesalePurchaseOrder(ErpWholesalePurchaseOrderSaveReqVO createReqVO) {
        // 1.1 校验订单项的有效性
        List<ErpWholesalePurchaseOrderItemDO> orderItems = validateWholesalePurchaseOrderItems(createReqVO.getItems());
        // 1.2 校验供应商
        supplierService.validateSupplier(createReqVO.getSupplierId());
        // 1.3 校验结算账户
        if (createReqVO.getAccountId() != null) {
            accountService.validateAccount(createReqVO.getAccountId());
        }
        // 1.4 生成订单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.PURCHASE_ORDER_NO_PREFIX);
        if (wholesalePurchaseOrderMapper.selectByNo(no) != null) {
            throw exception(PURCHASE_ORDER_NO_EXISTS);
        }

        // 2.1 插入订单
        ErpWholesalePurchaseOrderDO order = BeanUtils.toBean(createReqVO, ErpWholesalePurchaseOrderDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.PROCESS.getStatus()));
        calculateTotalPrice(order, orderItems);
        wholesalePurchaseOrderMapper.insert(order);
        // 2.2 插入订单项
        orderItems.forEach(o -> o.setOrderId(order.getId()));
        wholesalePurchaseOrderItemMapper.insertBatch(orderItems);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWholesalePurchaseOrder(ErpWholesalePurchaseOrderSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpWholesalePurchaseOrderDO order = validateWholesalePurchaseOrderExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(order.getStatus())) {
            throw exception(PURCHASE_ORDER_UPDATE_FAIL_APPROVE, order.getNo());
        }
        // 1.2 校验供应商
        supplierService.validateSupplier(updateReqVO.getSupplierId());
        // 1.3 校验结算账户
        if (updateReqVO.getAccountId() != null) {
            accountService.validateAccount(updateReqVO.getAccountId());
        }
        // 1.4 校验订单项的有效性
        List<ErpWholesalePurchaseOrderItemDO> orderItems = validateWholesalePurchaseOrderItems(updateReqVO.getItems());

        // 2.1 更新订单
        ErpWholesalePurchaseOrderDO updateObj = BeanUtils.toBean(updateReqVO, ErpWholesalePurchaseOrderDO.class);
        calculateTotalPrice(updateObj, orderItems);
        wholesalePurchaseOrderMapper.updateById(updateObj);
        // 2.2 更新订单项
        updateWholesalePurchaseOrderItemList(updateReqVO.getId(), orderItems);
    }

    private void calculateTotalPrice(ErpWholesalePurchaseOrderDO order, List<ErpWholesalePurchaseOrderItemDO> orderItems) {
        order.setTotalCount(getSumValue(orderItems, ErpWholesalePurchaseOrderItemDO::getCount, BigDecimal::add));
        order.setTotalProductPrice(getSumValue(orderItems, ErpWholesalePurchaseOrderItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        order.setTotalTaxPrice(getSumValue(orderItems, ErpWholesalePurchaseOrderItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
        order.setTotalPrice(order.getTotalProductPrice().add(order.getTotalTaxPrice()));
        // 计算优惠价格
        if (order.getDiscountPercent() == null) {
            order.setDiscountPercent(BigDecimal.ZERO);
        }
        order.setDiscountPrice(MoneyUtils.priceMultiplyPercent(order.getTotalPrice(), order.getDiscountPercent()));
        order.setTotalPrice(order.getTotalPrice().subtract(order.getDiscountPrice()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWholesalePurchaseOrderStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpWholesalePurchaseOrderDO order = validateWholesalePurchaseOrderExists(id);
        // 1.2 校验状态
        if (order.getStatus().equals(status)) {
            throw exception(approve ? PURCHASE_ORDER_APPROVE_FAIL : PURCHASE_ORDER_PROCESS_FAIL);
        }
        // 1.3 存在采购入单，无法反审核
        if (!approve && order.getInCount().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(PURCHASE_ORDER_PROCESS_FAIL_EXISTS_IN);
        }
        // 1.4 存在采购退货单，无法反审核
        if (!approve && order.getReturnCount().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(PURCHASE_ORDER_PROCESS_FAIL_EXISTS_RETURN);
        }

        // 2. 更新状态
        int updateCount = wholesalePurchaseOrderMapper.updateByIdAndStatus(id, order.getStatus(),
                new ErpWholesalePurchaseOrderDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? PURCHASE_ORDER_APPROVE_FAIL : PURCHASE_ORDER_PROCESS_FAIL);
        }
    }

    private List<ErpWholesalePurchaseOrderItemDO> validateWholesalePurchaseOrderItems(List<ErpWholesalePurchaseOrderSaveReqVO.Item> list) {
        // 1. 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, item -> item.getType() == 0 ? item.getProductId() : null));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);

        // 2. 校验组合产品存在
        List<ErpComboProductDO> comboProductList = comboProductService.validComboList(
                convertSet(list, item -> item.getType() == 1 ? item.getComboProductId() : null));
        Map<Long, ErpComboProductDO> comboProductMap = convertMap(comboProductList, ErpComboProductDO::getId);

        // 3. 转化为 ErpWholesalePurchaseOrderItemDO 列表
        return convertList(list, o -> {
            ErpWholesalePurchaseOrderItemDO item = BeanUtils.toBean(o, ErpWholesalePurchaseOrderItemDO.class);
            if (item.getType() == 0) { // 单品
                ErpProductDO product = productMap.get(item.getProductId());
                if (product == null) {
                    throw exception(PRODUCT_NOT_EXISTS, item.getProductId());
                }
                item.setProductId(product.getId());
                item.setComboProductId(null);
            } else { // 组合产品
                ErpComboProductDO comboProduct = comboProductMap.get(item.getComboProductId());
                if (comboProduct == null) {
                    throw exception(COMBO_PRODUCT_NOT_EXISTS, item.getComboProductId());
                }
                item.setComboProductId(comboProduct.getId());
                item.setProductId(null);
            }
            return item;
        });
    }

    private void updateWholesalePurchaseOrderItemList(Long id, List<ErpWholesalePurchaseOrderItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpWholesalePurchaseOrderItemDO> oldList = wholesalePurchaseOrderItemMapper.selectListByOrderId(id);
        List<List<ErpWholesalePurchaseOrderItemDO>> diffList = diffList(oldList, newList,
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setOrderId(id));
            wholesalePurchaseOrderItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            wholesalePurchaseOrderItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            wholesalePurchaseOrderItemMapper.deleteBatchIds(convertList(diffList.get(2), ErpWholesalePurchaseOrderItemDO::getId));
        }
    }

    @Override
    public void updateWholesalePurchaseOrderInCount(Long id, Map<Long, BigDecimal> inCountMap) {
        List<ErpWholesalePurchaseOrderItemDO> orderItems = wholesalePurchaseOrderItemMapper.selectListByOrderId(id);
        // 1. 更新每个采购订单项
        orderItems.forEach(item -> {
            BigDecimal inCount = inCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getInCount().equals(inCount)) {
                return;
            }
            if (inCount.compareTo(item.getCount()) > 0) {
                throw exception(PURCHASE_ORDER_ITEM_IN_FAIL_PRODUCT_EXCEED,
                        productService.getProduct(item.getProductId()).getName(), item.getCount());
            }
            wholesalePurchaseOrderItemMapper.updateById(new ErpWholesalePurchaseOrderItemDO().setId(item.getId()).setInCount(inCount));
        });
        // 2. 更新采购订单
        BigDecimal totalInCount = getSumValue(inCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        wholesalePurchaseOrderMapper.updateById(new ErpWholesalePurchaseOrderDO().setId(id).setInCount(totalInCount));
    }

    @Override
    public void updateWholesalePurchaseOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap) {
        List<ErpWholesalePurchaseOrderItemDO> orderItems = wholesalePurchaseOrderItemMapper.selectListByOrderId(orderId);
        // 1. 更新每个采购订单项
        orderItems.forEach(item -> {
            BigDecimal returnCount = returnCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getReturnCount().equals(returnCount)) {
                return;
            }
            if (returnCount.compareTo(item.getInCount()) > 0) {
                throw exception(PURCHASE_ORDER_ITEM_RETURN_FAIL_IN_EXCEED,
                        productService.getProduct(item.getProductId()).getName(), item.getInCount());
            }
            wholesalePurchaseOrderItemMapper.updateById(new ErpWholesalePurchaseOrderItemDO().setId(item.getId()).setReturnCount(returnCount));
        });
        // 2. 更新采购订单
        BigDecimal totalReturnCount = getSumValue(returnCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        wholesalePurchaseOrderMapper.updateById(new ErpWholesalePurchaseOrderDO().setId(orderId).setReturnCount(totalReturnCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWholesalePurchaseOrder(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpWholesalePurchaseOrderDO> orders = wholesalePurchaseOrderMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(orders)) {
            return;
        }
        orders.forEach(order -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(order.getStatus())) {
                throw exception(PURCHASE_ORDER_DELETE_FAIL_APPROVE, order.getNo());
            }
        });

        // 2. 遍历删除
        orders.forEach(order -> {
            // 2.1 删除订单
            wholesalePurchaseOrderMapper.deleteById(order.getId());
            // 2.2 删除订单项
            wholesalePurchaseOrderItemMapper.deleteByOrderId(order.getId());
        });
    }

    private ErpWholesalePurchaseOrderDO validateWholesalePurchaseOrderExists(Long id) {
        ErpWholesalePurchaseOrderDO order = wholesalePurchaseOrderMapper.selectById(id);
        if (order == null) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
        return order;
    }

    @Override
    public ErpWholesalePurchaseOrderDO getWholesalePurchaseOrder(Long id) {
        return wholesalePurchaseOrderMapper.selectById(id);
    }

    @Override
    public ErpWholesalePurchaseOrderDO validateWholesalePurchaseOrder(Long id) {
        ErpWholesalePurchaseOrderDO order = validateWholesalePurchaseOrderExists(id);
        if (ObjectUtil.notEqual(order.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PURCHASE_ORDER_NOT_APPROVE);
        }
        return order;
    }

    @Override
    public PageResult<ErpWholesalePurchaseOrderDO> getWholesalePurchaseOrderPage(ErpWholesalePurchaseOrderPageReqVO pageReqVO) {
        return wholesalePurchaseOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpWholesalePurchaseOrderItemDO> getWholesalePurchaseOrderItemListByOrderId(Long orderId) {
        return wholesalePurchaseOrderItemMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<ErpWholesalePurchaseOrderItemDO> getWholesalePurchaseOrderItemListByOrderIds(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return wholesalePurchaseOrderItemMapper.selectListByOrderIds(orderIds);
    }
}
