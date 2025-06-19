package cn.iocoder.yudao.module.erp.controller.admin.product;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.ErpComboImport.ErpComboImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.ErpComboImport.ErpComboImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.*;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - ERP 组合产品")
@RestController
@RequestMapping("/erp/combo")
@Validated
public class ErpComboProductController {

    @Resource
    private ErpComboProductService comboProductService;

//    @PostMapping("/create")
//    @Operation(summary = "创建组合产品")
//    @PreAuthorize("@ss.hasPermission('erp:combo-product:create')")
//    public CommonResult<Long> createComboProduct(@Valid @RequestBody ErpComboSaveReqVO createReqVO) {
//        return success(comboProductService.createCombo(createReqVO));
//    }
    @PostMapping("/create")
    @Operation(summary = "创建组合产品")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:create')")
    public CommonResult<Long> createComboProduct(@Valid @RequestBody ErpComboSaveReqVO createReqVO) {
        return success(comboProductService.createCombo(createReqVO));
    }
    @PutMapping("/update")
    @Operation(summary = "更新组合产品")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:update')")
    public CommonResult<Boolean> updateComboProduct(@Valid @RequestBody ErpComboSaveReqVO updateReqVO) {
        comboProductService.updateCombo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除组合产品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:combo-product:delete')")
    public CommonResult<Boolean> deleteComboProduct(@RequestParam("id") Long id) {
        comboProductService.deleteCombo(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得组合产品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:query')")
    public CommonResult<ErpComboRespVO> getComboProduct(@RequestParam("id") Long id) {
        ErpComboRespVO comboProduct = comboProductService.getComboWithItems(id);
        return CommonResult.success(comboProduct);
    }

    @GetMapping("/page")
    @Operation(summary = "获得组合产品分页")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:query')")
    public CommonResult<PageResult<ErpComboRespVO>> getComboProductPage(@Valid ErpComboPageReqVO pageReqVO) {
        System.out.println("组品查询条件详情: 组品编码=" + pageReqVO.getNo() + 
                          ", 产品名称=" + pageReqVO.getName() + 
                          ", 产品简称=" + pageReqVO.getShortName() + 
                          ", 发货编码=" + pageReqVO.getShippingCode() + 
                          ", 采购人员=" + pageReqVO.getPurchaser() + 
                          ", 供应商=" + pageReqVO.getSupplier() + 
                          ", 创建人员=" + pageReqVO.getCreator() + 
                          ", 关键词=" + pageReqVO.getKeyword());
        return success(comboProductService.getComboVOPage(pageReqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得组合产品精简列表", description = "只包含被开启的组合产品，主要用于前端的下拉选项")
    public CommonResult<List<ErpComboRespVO>> getComboProductSimpleList() {
        List<ErpComboRespVO> list = comboProductService.getComboProductVOListByStatus(CommonStatusEnum.ENABLE.getStatus());
//        return success(convertList(list, product -> new ErpComboRespVO()
//                .setId(product.getId())
//                .setName(product.getName())
//                .setShortName(product.getShortName())
//                .setShippingCode(product.getShippingCode())));
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出组合产品 Excel")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportComboProductExcel(@Valid ErpComboPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        //pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        pageReqVO.setPageSize(10000);

        PageResult<ErpComboRespVO> pageResult = comboProductService.getComboVOPage(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "组品信息.xlsx", "数据", ErpComboRespVO.class,
                pageResult.getList());
    }

    @GetMapping("/export-excel2")
    @Operation(summary = "导出组合产品 Excel")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportComboProductExcel2(@Valid ErpComboPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        System.out.println("调用导出2");
        //pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
            // 设置ES允许的最大分页大小10000
        pageReqVO.setPageSize(10000);
        PageResult<ErpComboRespVO> pageResult = comboProductService.getComboVOPage(pageReqVO);
        System.out.println("从es查询0"+pageResult);
        // 导出 Excel
        List<ErpComboPurchaseRespVO> purchaseList = BeanUtils.toBeanList(pageResult.getList(), ErpComboPurchaseRespVO.class);
        System.out.println("查看数据"+purchaseList);
        ExcelUtils.write(response, "组品信息.xlsx", "数据", ErpComboPurchaseRespVO.class, purchaseList);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索组合产品")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:query')")
    public CommonResult<List<ErpComboRespVO>> searchCombos(@Valid ErpComboSearchReqVO searchReqVO) {
        List<ErpComboRespVO> combos = comboProductService.searchCombos(searchReqVO);
        return success(combos);
    }


    // ... 其他代码保持不变 ...

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入组合产品模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpComboImportExcelVO> list = Arrays.asList(
            ErpComboImportExcelVO.builder()
            .build()
        );

        System.out.println("查询一下模板"+list);
        // 输出
        ExcelUtils.write(response, "组合产品导入模板.xls", "组合产品列表", ErpComboImportExcelVO.class, list);
    }



    @PostMapping("/import")
    @Operation(summary = "导入组合产品")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:combo-product:import')")
    public CommonResult<ErpComboImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpComboImportExcelVO> list = ExcelUtils.read(inputStream, ErpComboImportExcelVO.class);
            return success(comboProductService.importComboList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @PostMapping("/sync-es/{id}")
    @Operation(summary = "手动同步组合产品到ES")
    @Parameter(name = "id", description = "组合产品编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:update')")
    public CommonResult<Boolean> syncComboToES(@PathVariable("id") Long id) {
        comboProductService.manualSyncComboToES(id);
        return success(true);
    }

    @PostMapping("/sync-all-es")
    @Operation(summary = "手动全量同步组合产品到ES")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:update')")
    public CommonResult<Boolean> syncAllComboToES() {
        comboProductService.fullSyncToES();
        return success(true);
    }

}
