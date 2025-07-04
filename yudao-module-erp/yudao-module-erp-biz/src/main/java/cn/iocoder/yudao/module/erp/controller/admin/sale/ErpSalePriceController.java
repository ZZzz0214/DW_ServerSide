package cn.iocoder.yudao.module.erp.controller.admin.sale;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.listener.RowIndexListener;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboSearchReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.*;
import cn.iocoder.yudao.module.erp.controller.admin.transitsale.vo.ErpTransitSaleRespVO;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalePriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 销售价格")
@RestController
@RequestMapping("/erp/saleprice")
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
        System.out.println("要删除id"+ids);
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
        ExcelUtils.write(response, "销售价格.xlsx", "数据", ErpSalePriceRespVO.class, pageResult.getList());
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入销售价格模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpSalePriceRespVO> list = Arrays.asList(
                new ErpSalePriceRespVO()
        );

        ExcelUtils.write(response, "销售价格模板.xlsx", "销售价格模板", ErpSalePriceRespVO.class, list);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索销售价格表")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:query')")
    public CommonResult<List<ErpSalePriceRespVO>> searchErpSalePrice(@Valid ErpSalePriceSearchReqVO searchReqVO) {
        List<ErpSalePriceRespVO> erpSalePrices = salePriceService.searchProducts(searchReqVO);
        return success(erpSalePrices);
    }

    @GetMapping("/missing-prices")
    @Operation(summary = "获取缺失价格的销售记录")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:query')")
    public CommonResult<List<ErpSalePriceRespVO>> getMissingPrices() {
        return success(salePriceService.getMissingPrices());
    }

    @GetMapping("/distribution-missing-prices")
    @Operation(summary = "获取代发缺失价格记录")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:query')")
    public CommonResult<PageResult<ErpDistributionMissingPriceVO>> getDistributionMissingPrices(@Valid ErpSalePricePageReqVO pageReqVO) {
        return success(salePriceService.getDistributionMissingPrices(pageReqVO));
    }

    @GetMapping("/wholesale-missing-prices")
    @Operation(summary = "获取批发缺失价格记录")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:query')")
    public CommonResult<PageResult<ErpWholesaleMissingPriceVO>> getWholesaleMissingPrices(@Valid ErpSalePricePageReqVO pageReqVO) {
        return success(salePriceService.getWholesaleMissingPrices(pageReqVO));
    }

    @PostMapping("/batch-set-distribution-prices")
    @Operation(summary = "批量设置代发价格")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:update')")
    public CommonResult<Boolean> batchSetDistributionPrices(@Valid @RequestBody List<ErpDistributionPriceSetReqVO> reqList) {
        salePriceService.batchSetDistributionPrices(reqList);
        return success(true);
    }

    @PostMapping("/batch-set-wholesale-prices")
    @Operation(summary = "批量设置批发价格")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:update')")
    public CommonResult<Boolean> batchSetWholesalePrices(@Valid @RequestBody List<ErpWholesalePriceSetReqVO> reqList) {
        salePriceService.batchSetWholesalePrices(reqList);
        return success(true);
    }

    @GetMapping("/combined-missing-prices")
    @Operation(summary = "获取统一缺失价格记录（代发+批发）")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:query')")
    public CommonResult<PageResult<ErpCombinedMissingPriceVO>> getCombinedMissingPrices(@Valid ErpSalePricePageReqVO pageReqVO) {
        return success(salePriceService.getCombinedMissingPrices(pageReqVO));
    }

    @PostMapping("/batch-set-combined-prices")
    @Operation(summary = "批量设置统一价格（代发+批发）")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:update')")
    public CommonResult<Boolean> batchSetCombinedPrices(@Valid @RequestBody List<ErpCombinedPriceSetReqVO> reqList) {
        salePriceService.batchSetCombinedPrices(reqList);
        return success(true);
    }

    @PostMapping("/import")
    @Operation(summary = "导入销售价格")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:sale-price:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpSalePriceImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            System.out.println("调用导入");
            List<ErpSalePriceImportExcelVO> list = ExcelUtils.read(inputStream, ErpSalePriceImportExcelVO.class, new RowIndexListener<>());
            return success(salePriceService.importSalePriceList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @PostMapping("/clear-cache")
    @Operation(summary = "清理缓存")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:update')")
    public CommonResult<Boolean> clearCache() {
        salePriceService.clearCache();
        return success(true);
    }

    @PostMapping("/sync-es")
    @Operation(summary = "手动同步数据到ES")
    @PreAuthorize("@ss.hasPermission('erp:sale-price:update')")
    public CommonResult<Boolean> syncToES() {
        salePriceService.manualFullSyncToES();
        return success(true);
    }
}
