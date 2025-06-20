package cn.iocoder.yudao.module.erp.controller.admin.inventory;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.inventory.ErpInventoryDO;
import cn.iocoder.yudao.module.erp.service.inventory.ErpInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
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

@Tag(name = "管理后台 - ERP 库存")
@RestController
@RequestMapping("/erp/inventory")
@Validated
public class ErpInventoryController {

    @Resource
    private ErpInventoryService inventoryService;

    @PostMapping("/create")
    @Operation(summary = "创建库存")
    @PreAuthorize("@ss.hasPermission('erp:inventory:create')")
    public CommonResult<Long> createInventory(@Valid @RequestBody ErpInventorySaveReqVO createReqVO) {
        return success(inventoryService.createInventory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库存")
    @PreAuthorize("@ss.hasPermission('erp:inventory:update')")
    public CommonResult<Boolean> updateInventory(@Valid @RequestBody ErpInventorySaveReqVO updateReqVO) {
        inventoryService.updateInventory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:inventory:delete')")
    public CommonResult<Boolean> deleteInventory(@RequestParam("ids") List<Long> ids) {

        inventoryService.deleteInventory(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库存")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:inventory:query')")
    public CommonResult<ErpInventoryRespVO> getInventory(@RequestParam("id") Long id) {
        ErpInventoryRespVO inventory = inventoryService.getInventoryVO(id);
        return success(inventory);
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存分页")
    @PreAuthorize("@ss.hasPermission('erp:inventory:query')")
    public CommonResult<PageResult<ErpInventoryRespVO>> getInventoryPage(@Valid ErpInventoryPageReqVO pageReqVO) {
       System.out.println("调用了库存分页"+pageReqVO);
        PageResult<ErpInventoryRespVO> pageResult = inventoryService.getInventoryVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得库存列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:inventory:query')")
    public CommonResult<List<ErpInventoryRespVO>> getInventoryListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpInventoryRespVO> list = inventoryService.getInventoryVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库存 Excel")
    @PreAuthorize("@ss.hasPermission('erp:inventory:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInventoryExcel(@Valid ErpInventoryPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpInventoryRespVO> pageResult = inventoryService.getInventoryVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpInventoryExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpInventoryExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "库存信息.xlsx", "数据", ErpInventoryExportVO.class,
        exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入库存")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:inventory:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpInventoryImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpInventoryImportExcelVO> list = ExcelUtils.read(inputStream, ErpInventoryImportExcelVO.class);
            return success(inventoryService.importInventoryList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入库存模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpInventoryExportVO> list = Arrays.asList(
                ErpInventoryExportVO.builder()
                        .no("示例库存1")
                        .productNo("SP001")
                        .spotInventory(100)
                        .remark("示例备注（剩余库存由系统自动计算）").build()
        );
        // 输出
        ExcelUtils.write(response, "库存导入模板.xls", "库存列表", ErpInventoryExportVO.class, list);
    }

    @GetMapping("/check-product-exists")
    @Operation(summary = "检查产品是否已有库存记录")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:inventory:query')")
    public CommonResult<Boolean> checkProductExists(@RequestParam("productId") Long productId) {
        boolean exists = inventoryService.checkProductExists(productId);
        return success(exists);
    }
}
