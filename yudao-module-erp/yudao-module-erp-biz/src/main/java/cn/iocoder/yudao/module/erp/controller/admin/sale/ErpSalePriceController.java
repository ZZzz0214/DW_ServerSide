package cn.iocoder.yudao.module.erp.controller.admin.sale;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboSearchReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePricePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceSearchReqVO;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalePriceService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 销售价格")
@RestController
@RequestMapping("/erp/sale-price")
@Validated
public class ErpSalePriceController {

    @Resource
    private ErpSalePriceService salePriceService;

    @PostMapping("/create")
    @Operation(summary = "创建销售价格")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:create')")
    public CommonResult<Long> createSalePrice(@Valid @RequestBody ErpSalePriceSaveReqVO createReqVO) {
        return success(salePriceService.createSalePrice(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售价格")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:update')")
    public CommonResult<Boolean> updateSalePrice(@Valid @RequestBody ErpSalePriceSaveReqVO updateReqVO) {
        salePriceService.updateSalePrice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售价格")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:sale-price:delete')")
    public CommonResult<Boolean> deleteSalePrice(@RequestParam("ids") List<Long> ids) {
        salePriceService.deleteSalePrice(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取销售价格")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:query')")
    public CommonResult<ErpSalePriceRespVO> getSalePrice(@RequestParam("id") Long id) {
        ErpSalePriceRespVO salePrice = salePriceService.getSalePriceWithItems(id);
        return success(salePrice);
    }

    @GetMapping("/page")
    @Operation(summary = "获取销售价格分页")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:query')")
    public CommonResult<PageResult<ErpSalePriceRespVO>> getSalePricePage(@Valid ErpSalePricePageReqVO pageReqVO) {
        System.out.println("page:"+salePriceService.getSalePriceVOPage(pageReqVO));
        return success(salePriceService.getSalePriceVOPage(pageReqVO));
    }

//    @GetMapping("/list-by-group")
//    @Operation(summary = "获取销售价格列表（通过组品编号）")
//    @Parameter(name = "groupProductId", description = "组品编号", required = true)
//    @PreAuthorize("@ss.hasPermission('erp:sale-price:query')")
//    public CommonResult<List<ErpSalePriceRespVO>> getSalePriceListByGroupProductId(@RequestParam("groupProductId") Long groupProductId) {
//        return CommonResult.success(salePriceService.getSalePriceVOListByGroupProductId(groupProductId));
//    }
//
//    @GetMapping("/list-by-customer")
//    @Operation(summary = "获取销售价格列表（通过客户名称）")
//    @Parameter(name = "customerName", description = "客户名称", required = true)
//    @PreAuthorize("@ss.hasPermission('erp:sale-price:query')")
//    public CommonResult<List<ErpSalePriceRespVO>> getSalePriceListByCustomerName(@RequestParam("customerName") String customerName) {
//        return CommonResult.success(salePriceService.getSalePriceVOListByCustomerName(customerName));
//    }
    @GetMapping("/simple-list")
    @Operation(summary = "获取销售价格精简列表", description = "通过组合品状态筛选销售价格，主要用于前端的下拉选项")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:query')")
    public CommonResult<List<ErpSalePriceRespVO>> getSalePriceSimpleList() {
        List<ErpSalePriceRespVO> list = salePriceService.getSalePriceVOListByComboStatus();
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售价格 Excel")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSalePriceExcel(@Valid ErpSalePricePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpSalePriceRespVO> pageResult = salePriceService.getSalePriceVOPage(pageReqVO);
        ExcelUtils.write(response, "销售价格.xls", "数据", ErpSalePriceRespVO.class, pageResult.getList());
    }

    @GetMapping("/search")
    @Operation(summary = "搜索销售价格表")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:query')")
    public CommonResult<List<ErpSalePriceRespVO>> searchErpSalePrice(@Valid ErpSalePriceSearchReqVO searchReqVO) {
        List<ErpSalePriceRespVO> erpSalePrices = salePriceService.searchProducts(searchReqVO);
        return success(erpSalePrices);
    }
}
