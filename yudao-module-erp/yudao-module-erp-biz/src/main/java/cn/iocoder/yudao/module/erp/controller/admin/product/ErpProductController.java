package cn.iocoder.yudao.module.erp.controller.admin.product;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - ERP 产品")
@RestController
@RequestMapping("/erp/product")
@Validated
public class ErpProductController {

    @Resource
    private ErpProductService productService;

    @PostMapping("/create")
    @Operation(summary = "创建产品")
    @PreAuthorize("@ss.hasPermission('erp:product:create')")
    public CommonResult<Long> createProduct(@Valid @RequestBody ProductSaveReqVO createReqVO) {
        return success(productService.createProduct(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品")
    @PreAuthorize("@ss.hasPermission('erp:product:update')")
    public CommonResult<Boolean> updateProduct(@Valid @RequestBody ProductSaveReqVO updateReqVO) {
        productService.updateProduct(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:product:delete')")
    public CommonResult<Boolean> deleteProduct(@RequestParam("id") Long id) {
        productService.deleteProduct(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:product:query')")
    public CommonResult<ErpProductRespVO> getProduct(@RequestParam("id") Long id) {
        ErpProductDO product = productService.getProduct(id);
        return success(BeanUtils.toBean(product, ErpProductRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品分页")
    @PreAuthorize("@ss.hasPermission('erp:product:query')")
    public CommonResult<PageResult<ErpProductRespVO>> getProductPage(@Valid ErpProductPageReqVO pageReqVO) {
        System.out.println("调用产品分页"+pageReqVO);
        return success(productService.getProductVOPage(pageReqVO));
    }

//    @GetMapping("/simple-list")
//    @Operation(summary = "获得产品精简列表", description = "只包含被开启的产品，主要用于前端的下拉选项")
//    public CommonResult<List<ErpProductRespVO>> getProductSimpleList() {
//        List<ErpProductRespVO> list = productService.getProductVOListByStatus(CommonStatusEnum.ENABLE.getStatus());
//        return success(convertList(list, product -> new ErpProductRespVO()
//                .setId(product.getId())
//                .setName(product.getName())
//                .setBarCode(product.getBarCode())
//                .setCategoryId(product.getCategoryId())
//                .setUnitId(product.getUnitId())
//                .setUnitName(product.getUnitName())
//                .setPurchasePrice(product.getPurchasePrice())));
//    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出产品 Excel")
    @PreAuthorize("@ss.hasPermission('erp:product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductExcel(@Valid ErpProductPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpProductRespVO> pageResult = productService.getProductVOPage(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "产品信息.xlsx", "数据", ErpProductRespVO.class,
                pageResult.getList());
    }

    @GetMapping("/search")
    @Operation(summary = "搜索产品")
    @PreAuthorize("@ss.hasPermission('erp:product:query')")
    public CommonResult<List<ErpProductRespVO>> searchProducts(@Valid ErpProductSearchReqVO searchReqVO) {
        List<ErpProductRespVO> products = productService.searchProducts(searchReqVO);
        return success(products);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入产品模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpProductRespVO> list = Arrays.asList(
            ErpProductRespVO.builder().name("示例产品1").categoryId(1L).barCode("1234567890123")
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                        ErpProductRespVO.builder().name("示例产品2").categoryId(2L).barCode("9876543210987")
                        .status(CommonStatusEnum.DISABLE.getStatus()).build()
        );
        // 输出
        ExcelUtils.write(response, "产品导入模板.xls", "产品列表", ErpProductRespVO.class, list);
    }

//    @PostMapping("/import")
//    @Operation(summary = "导入产品")
//    @Parameters({
//            @Parameter(name = "file", description = "Excel 文件", required = true),
//            @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
//    })
//    @PreAuthorize("@ss.hasPermission('erp:product:import')")
//    public CommonResult<ErpProductImportRespVO> importExcel(@RequestParam("file") MultipartFile file,
//                                                            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
//        List<ErpProductImportExcelVO> list = ExcelUtils.read(file, ErpProductImportExcelVO.class);
//        return success(productService.importProductList(list, updateSupport));
//    }

//    @PostMapping("/import")
//    @Operation(summary = "导入产品")
//    @Parameters({
//            @Parameter(name = "file", description = "Excel 文件", required = true),
//            @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
//    })
//    @PreAuthorize("@ss.hasPermission('erp:product:import')")
//    public CommonResult<ErpProductImportRespVO> importExcel(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
//        try (InputStream inputStream = file.getInputStream()) {
//            List<ErpProductImportExcelVO> list = ExcelUtils.read(inputStream, ErpProductImportExcelVO.class);
//            return success(productService.importProductList(list, updateSupport));
//        } catch (Exception e) {
//            throw new RuntimeException("导入失败: " + e.getMessage());
//        }
//    }
//    @PostMapping("/import")
//    @Operation(summary = "导入产品")
//    @Parameters({
//            @Parameter(name = "file", description = "Excel 文件", required = true),
//            @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
//    })
//    @PreAuthorize("@ss.hasPermission('erp:product:import')")
//    public CommonResult<ErpProductImportRespVO> importExcel(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception{
//            InputStream inputStream = file.getInputStream();
//            // 修改为使用MultipartFile直接读取
//            List<ErpProductImportExcelVO> list = ExcelUtils.read(file, ErpProductImportExcelVO.class);
//            inputStream.close();
//            return success(productService.importProductList(list, updateSupport));
//
//    }

    @PostMapping("/import")
    @Operation(summary = "导入产品")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true),
            @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:product:import')")
    public CommonResult<ErpProductImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpProductImportExcelVO> list = ExcelUtils.read(inputStream, ErpProductImportExcelVO.class);
            return success(productService.importProductList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }


}
