package cn.iocoder.yudao.module.erp.controller.admin.transitsale;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.listener.RowIndexListener;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.module.erp.controller.admin.dropship.vo.ErpDropshipAssistExportVO;
import cn.iocoder.yudao.module.erp.controller.admin.transitsale.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpTransitSaleDO;
import cn.iocoder.yudao.module.erp.service.transitsale.ErpTransitSaleService;
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

@Tag(name = "管理后台 - ERP 中转销售")
@RestController
@RequestMapping("/erp/transit-sale")
@Validated
public class ErpTransitSaleController {

    @Resource
    private ErpTransitSaleService transitSaleService;

    @PostMapping("/create")
    @Operation(summary = "创建中转销售")
    @PreAuthorize("@ss.hasPermission('erp:transit-sale:create')")
    public CommonResult<Long> createTransitSale(@Valid @RequestBody ErpTransitSaleSaveReqVO createReqVO) {
        System.out.println("调用新增"+createReqVO);
        return success(transitSaleService.createTransitSale(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新中转销售")
    @PreAuthorize("@ss.hasPermission('erp:transit-sale:update')")
    public CommonResult<Boolean> updateTransitSale(@Valid @RequestBody ErpTransitSaleSaveReqVO updateReqVO) {
        transitSaleService.updateTransitSale(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除中转销售")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:transit-sale:delete')")
    public CommonResult<Boolean> deleteTransitSale(@RequestParam("ids") List<Long> ids) {
        transitSaleService.deleteTransitSale(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得中转销售")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:transit-sale:query')")
    public CommonResult<ErpTransitSaleRespVO> getTransitSale(@RequestParam("id") Long id) {
       // ErpTransitSaleDO transitSale = transitSaleService.getTransitSale(id);
       // return success(BeanUtils.toBean(transitSale, ErpTransitSaleRespVO.class));
        return success(transitSaleService.getTransitSale(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得中转销售分页")
    @PreAuthorize("@ss.hasPermission('erp:transit-sale:query')")
    public CommonResult<PageResult<ErpTransitSaleRespVO>> getTransitSalePage(@Valid ErpTransitSalePageReqVO pageReqVO) {

        PageResult<ErpTransitSaleRespVO> pageResult = transitSaleService.getTransitSaleVOPage(pageReqVO);
        System.out.println("调用分页"+pageResult);
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出中转销售 Excel")
    @PreAuthorize("@ss.hasPermission('erp:transit-sale:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTransitSaleExcel(@Valid ErpTransitSalePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpTransitSaleRespVO> pageResult = transitSaleService.getTransitSaleVOPage(pageReqVO);
        // 转换为导出VO
        //List<ErpTransitSaleExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpTransitSaleExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "中转销售信息.xlsx", "数据", ErpTransitSaleRespVO.class, pageResult.getList());
    }


    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入中转销售模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpTransitSaleRespVO> list = Arrays.asList(
        );
        // 输出
        ExcelUtils.write(response, "中转销售模板.xlsx", "中转销售模板", ErpTransitSaleRespVO.class, list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入中转销售")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:transit-sale:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpTransitSaleImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpTransitSaleImportExcelVO> list = ExcelUtils.read(inputStream, ErpTransitSaleImportExcelVO.class, new RowIndexListener<>());
            return success(transitSaleService.importTransitSaleList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }
}
