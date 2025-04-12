package cn.iocoder.yudao.module.erp.controller.admin.sale;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.wholesaleorder.ErpWholesaleSaleOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.wholesaleorder.ErpWholesaleSaleOrderRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.wholesaleorder.ErpWholesaleSaleOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_sale.ErpWholesaleSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_sale.ErpWholesaleSaleOrderItemDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
import cn.iocoder.yudao.module.erp.service.sale.ErpWholesaleSaleOrderService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
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

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 批发销售订单")
@RestController
@RequestMapping("/erp/wholesale-sale-order")
@Validated
public class ErpWholesaleSaleOrderController {

    @Resource
    private ErpWholesaleSaleOrderService wholesaleSaleOrderService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpCustomerService customerService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建批发销售订单")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-sale-out:create')")
    public CommonResult<Long> createWholesaleSaleOrder(@Valid @RequestBody ErpWholesaleSaleOrderSaveReqVO createReqVO) {
        return success(wholesaleSaleOrderService.createWholesaleSaleOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新批发销售订单")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-sale-out:update')")
    public CommonResult<Boolean> updateWholesaleSaleOrder(@Valid @RequestBody ErpWholesaleSaleOrderSaveReqVO updateReqVO) {
        wholesaleSaleOrderService.updateWholesaleSaleOrder(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新批发销售订单的状态")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-sale-out:update-status')")
    public CommonResult<Boolean> updateWholesaleSaleOrderStatus(@RequestParam("id") Long id,
                                                              @RequestParam("status") Integer status) {
        wholesaleSaleOrderService.updateWholesaleSaleOrderStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除批发销售订单")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:wholesale-sale-out:delete')")
    public CommonResult<Boolean> deleteWholesaleSaleOrder(@RequestParam("ids") List<Long> ids) {
        wholesaleSaleOrderService.deleteWholesaleSaleOrder(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得批发销售订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-sale-out:query')")
    public CommonResult<ErpWholesaleSaleOrderRespVO> getWholesaleSaleOrder(@RequestParam("id") Long id) {
        ErpWholesaleSaleOrderDO wholesaleSaleOrder = wholesaleSaleOrderService.getWholesaleSaleOrder(id);
        if (wholesaleSaleOrder == null) {
            return success(null);
        }
        List<ErpWholesaleSaleOrderItemDO> wholesaleSaleOrderItemList = wholesaleSaleOrderService.getWholesaleSaleOrderItemListByOrderId(id);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(wholesaleSaleOrderItemList, ErpWholesaleSaleOrderItemDO::getProductId));
        return success(BeanUtils.toBean(wholesaleSaleOrder, ErpWholesaleSaleOrderRespVO.class, wholesaleSaleOrderVO ->
                wholesaleSaleOrderVO.setItems(BeanUtils.toBean(wholesaleSaleOrderItemList, ErpWholesaleSaleOrderRespVO.Item.class, item -> {
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName()));
                }))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得批发销售订单分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-sale-out:query')")
    public CommonResult<PageResult<ErpWholesaleSaleOrderRespVO>> getWholesaleSaleOrderPage(@Valid ErpWholesaleSaleOrderPageReqVO pageReqVO) {
        PageResult<ErpWholesaleSaleOrderDO> pageResult = wholesaleSaleOrderService.getWholesaleSaleOrderPage(pageReqVO);
        return success(buildWholesaleSaleOrderVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出批发销售订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:wholesale-sale-out:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWholesaleSaleOrderExcel(@Valid ErpWholesaleSaleOrderPageReqVO pageReqVO,
                                            HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpWholesaleSaleOrderRespVO> list = buildWholesaleSaleOrderVOPageResult(
                wholesaleSaleOrderService.getWholesaleSaleOrderPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "批发销售订单.xls", "数据", ErpWholesaleSaleOrderRespVO.class, list);
    }

    private PageResult<ErpWholesaleSaleOrderRespVO> buildWholesaleSaleOrderVOPageResult(PageResult<ErpWholesaleSaleOrderDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 订单项
        List<ErpWholesaleSaleOrderItemDO> wholesaleSaleOrderItemList = wholesaleSaleOrderService.getWholesaleSaleOrderItemListByOrderIds(
                convertSet(pageResult.getList(), ErpWholesaleSaleOrderDO::getId));
        Map<Long, List<ErpWholesaleSaleOrderItemDO>> wholesaleSaleOrderItemMap = convertMultiMap(
                wholesaleSaleOrderItemList, ErpWholesaleSaleOrderItemDO::getOrderId);
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(wholesaleSaleOrderItemList, ErpWholesaleSaleOrderItemDO::getProductId));
        // 1.3 客户信息
        Map<Long, ErpCustomerDO> customerMap = customerService.getCustomerMap(
                convertSet(pageResult.getList(), ErpWholesaleSaleOrderDO::getCustomerId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), wholesaleSaleOrder -> Long.parseLong(wholesaleSaleOrder.getCreator())));
        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, ErpWholesaleSaleOrderRespVO.class, wholesaleSaleOrder -> {
            wholesaleSaleOrder.setItems(BeanUtils.toBean(wholesaleSaleOrderItemMap.get(wholesaleSaleOrder.getId()),
                    ErpWholesaleSaleOrderRespVO.Item.class,
                    item -> MapUtils.findAndThen(productMap, item.getProductId(),
                            product -> item.setProductName(product.getName()))));
            MapUtils.findAndThen(customerMap, wholesaleSaleOrder.getCustomerId(),
                    customer -> wholesaleSaleOrder.setCustomerName(customer.getName()));
        });
    }
}
