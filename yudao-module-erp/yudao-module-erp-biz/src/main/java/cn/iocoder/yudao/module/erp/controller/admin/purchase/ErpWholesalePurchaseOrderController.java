package cn.iocoder.yudao.module.erp.controller.admin.purchase;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.wholesale_purchase.ErpWholesalePurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.wholesale_purchase.ErpWholesalePurchaseOrderRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.wholesale_purchase.ErpWholesalePurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_purchase.ErpWholesalePurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_purchase.ErpWholesalePurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;

import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpWholesalePurchaseOrderService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 批发采购订单")
@RestController
@RequestMapping("/erp/wholesale-purchase-order")
@Validated
public class ErpWholesalePurchaseOrderController {

    @Resource
    private ErpWholesalePurchaseOrderService wholesalePurchaseOrderService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpComboProductService comboProductService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建批发采购订单")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-purchase-order:create')")
    public CommonResult<Long> createWholesalePurchaseOrder(@Valid @RequestBody ErpWholesalePurchaseOrderSaveReqVO createReqVO) {
        return success(wholesalePurchaseOrderService.createWholesalePurchaseOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新批发采购订单")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-purchase-order:update')")
    public CommonResult<Boolean> updateWholesalePurchaseOrder(@Valid @RequestBody ErpWholesalePurchaseOrderSaveReqVO updateReqVO) {
        wholesalePurchaseOrderService.updateWholesalePurchaseOrder(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新批发采购订单的状态")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-purchase-order:update-status')")
    public CommonResult<Boolean> updateWholesalePurchaseOrderStatus(@RequestParam("id") Long id,
                                                              @RequestParam("status") Integer status) {
        wholesalePurchaseOrderService.updateWholesalePurchaseOrderStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除批发采购订单")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:wholesale-purchase-order:delete')")
    public CommonResult<Boolean> deleteWholesalePurchaseOrder(@RequestParam("ids") List<Long> ids) {
        wholesalePurchaseOrderService.deleteWholesalePurchaseOrder(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得批发采购订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-purchase-order:query')")
    public CommonResult<ErpWholesalePurchaseOrderRespVO> getWholesalePurchaseOrder(@RequestParam("id") Long id) {
        ErpWholesalePurchaseOrderDO wholesalePurchaseOrder = wholesalePurchaseOrderService.getWholesalePurchaseOrder(id);
        if (wholesalePurchaseOrder == null) {
            return success(null);
        }
        List<ErpWholesalePurchaseOrderItemDO> wholesalePurchaseOrderItemList =
            wholesalePurchaseOrderService.getWholesalePurchaseOrderItemListByOrderId(id);

        // 分离单品和组品
        List<ErpWholesalePurchaseOrderItemDO> singleItemList = wholesalePurchaseOrderItemList.stream()
                .filter(item -> item.getType() == 0)
                .collect(Collectors.toList());
        List<ErpWholesalePurchaseOrderItemDO> comboItemList = wholesalePurchaseOrderItemList.stream()
                .filter(item -> item.getType() == 1)
                .collect(Collectors.toList());

        // 获取单品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(singleItemList, ErpWholesalePurchaseOrderItemDO::getProductId));
        // 获取组品信息
        Map<Long, ErpComboRespVO> comboMap = comboProductService.getComboVOMap(
                convertSet(comboItemList, ErpWholesalePurchaseOrderItemDO::getComboProductId));

        return success(BeanUtils.toBean(wholesalePurchaseOrder, ErpWholesalePurchaseOrderRespVO.class, wholesalePurchaseOrderVO -> {
            wholesalePurchaseOrderVO.setItems(BeanUtils.toBean(wholesalePurchaseOrderItemList,
                ErpWholesalePurchaseOrderRespVO.Item.class, item -> {
                    if (item.getType() == 0) {
                        // 单品
                        MapUtils.findAndThen(productMap, item.getProductId(), product ->
                                item.setOriginalProductName(product.getName()));
                    } else {
                        // 组品
                        MapUtils.findAndThen(comboMap, item.getComboProductId(), combo ->
                                item.setOriginalProductName(combo.getName()));
                    }
                }));
        }));
    }

    @GetMapping("/page")
    @Operation(summary = "获得批发采购订单分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-purchase-order:query')")
    public CommonResult<PageResult<ErpWholesalePurchaseOrderRespVO>> getWholesalePurchaseOrderPage(
            @Valid ErpWholesalePurchaseOrderPageReqVO pageReqVO) {
        PageResult<ErpWholesalePurchaseOrderDO> pageResult = wholesalePurchaseOrderService.getWholesalePurchaseOrderPage(pageReqVO);
        return success(buildWholesalePurchaseOrderVOPageResult(pageResult));
    }

    @GetMapping("/page2")
    @Operation(summary = "获得未审核批发采购订单分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-purchase-order:query')")
    public CommonResult<PageResult<ErpWholesalePurchaseOrderRespVO>> getUnreviewedWholesalePurchaseOrderPage(
            @Valid ErpWholesalePurchaseOrderPageReqVO pageReqVO) {
        pageReqVO.setStatus(100); // 设置状态为未审核
        PageResult<ErpWholesalePurchaseOrderDO> pageResult = wholesalePurchaseOrderService.getWholesalePurchaseOrderPage(pageReqVO);
        return success(buildWholesalePurchaseOrderVOPageResult(pageResult));
    }

    @GetMapping("/page3")
    @Operation(summary = "获得已审核批发采购订单分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-purchase-order:query')")
    public CommonResult<PageResult<ErpWholesalePurchaseOrderRespVO>> getReviewedWholesalePurchaseOrderPage(
            @Valid ErpWholesalePurchaseOrderPageReqVO pageReqVO) {
        pageReqVO.setStatus(200); // 设置状态为已审核
        PageResult<ErpWholesalePurchaseOrderDO> pageResult = wholesalePurchaseOrderService.getWholesalePurchaseOrderPage(pageReqVO);
        return success(buildWholesalePurchaseOrderVOPageResult(pageResult));
    }


    @GetMapping("/export-excel")
    @Operation(summary = "导出批发采购订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-purchase-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWholesalePurchaseOrderExcel(@Valid ErpWholesalePurchaseOrderPageReqVO pageReqVO,
                                            HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpWholesalePurchaseOrderRespVO> list = buildWholesalePurchaseOrderVOPageResult(
                wholesalePurchaseOrderService.getWholesalePurchaseOrderPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "批发采购订单.xls", "数据", ErpWholesalePurchaseOrderRespVO.class, list);
    }

    private PageResult<ErpWholesalePurchaseOrderRespVO> buildWholesalePurchaseOrderVOPageResult(
            PageResult<ErpWholesalePurchaseOrderDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }

        // 1.1 订单项
        List<ErpWholesalePurchaseOrderItemDO> wholesalePurchaseOrderItemList =
            wholesalePurchaseOrderService.getWholesalePurchaseOrderItemListByOrderIds(
                convertSet(pageResult.getList(), ErpWholesalePurchaseOrderDO::getId));
        Map<Long, List<ErpWholesalePurchaseOrderItemDO>> wholesalePurchaseOrderItemMap =
            convertMultiMap(wholesalePurchaseOrderItemList, ErpWholesalePurchaseOrderItemDO::getOrderId);

        // 分离单品和组品
        List<ErpWholesalePurchaseOrderItemDO> singleItemList = wholesalePurchaseOrderItemList.stream()
                .filter(item -> item.getType() == 0)
                .collect(Collectors.toList());
        List<ErpWholesalePurchaseOrderItemDO> comboItemList = wholesalePurchaseOrderItemList.stream()
                .filter(item -> item.getType() == 1)
                .collect(Collectors.toList());

        // 1.2 单品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(singleItemList, ErpWholesalePurchaseOrderItemDO::getProductId));

        // 1.3 组品信息
        Map<Long, ErpComboRespVO> comboMap = comboProductService.getComboVOMap(
                convertSet(comboItemList, ErpWholesalePurchaseOrderItemDO::getComboProductId));

        // 1.4 供应商信息
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(pageResult.getList(), ErpWholesalePurchaseOrderDO::getSupplierId));

        // 1.5 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), wholesalePurchaseOrder -> Long.parseLong(wholesalePurchaseOrder.getCreator())));

        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, ErpWholesalePurchaseOrderRespVO.class, wholesalePurchaseOrder -> {
            // 获取当前订单的订单项
            List<ErpWholesalePurchaseOrderItemDO> currentOrderItems = wholesalePurchaseOrderItemMap.get(wholesalePurchaseOrder.getId());

            wholesalePurchaseOrder.setItems(BeanUtils.toBean(currentOrderItems,
                ErpWholesalePurchaseOrderRespVO.Item.class, item -> {
                    if (item.getType() == 0) {
                        // 单品
                        MapUtils.findAndThen(productMap, item.getProductId(), product ->
                                item.setOriginalProductName(product.getName()));
                    } else {
                        // 组品
                        MapUtils.findAndThen(comboMap, item.getComboProductId(), combo ->
                                item.setOriginalProductName(combo.getName()));
                    }
                }));
            // 只拼接当前订单的订单项名称
            if (CollUtil.isNotEmpty(currentOrderItems)) {
                wholesalePurchaseOrder.setProductNames(CollUtil.join(currentOrderItems, "，",
                        ErpWholesalePurchaseOrderItemDO::getOriginalProductName));
            }
            MapUtils.findAndThen(supplierMap, wholesalePurchaseOrder.getSupplierId(),
                supplier -> wholesalePurchaseOrder.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(userMap, Long.parseLong(wholesalePurchaseOrder.getCreator()),
                user -> wholesalePurchaseOrder.setCreatorName(user.getNickname()));
        });
    }
}
