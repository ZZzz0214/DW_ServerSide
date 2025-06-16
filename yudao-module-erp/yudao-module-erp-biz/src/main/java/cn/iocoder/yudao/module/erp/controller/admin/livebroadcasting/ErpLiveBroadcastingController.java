package cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting;

import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.*;
import cn.iocoder.yudao.module.erp.service.livebroadcasting.ErpLiveBroadcastingService;
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

@Tag(name = "管理后台 - ERP 直播货盘")
@RestController
@RequestMapping("/erp/live-broadcasting")
@Validated
public class ErpLiveBroadcastingController {

    @Resource
    private ErpLiveBroadcastingService liveBroadcastingService;

    @PostMapping("/create")
    @Operation(summary = "创建直播货盘")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:create')")
    public CommonResult<Long> createLiveBroadcasting(@Valid @RequestBody ErpLiveBroadcastingSaveReqVO createReqVO) {
        System.out.println("创建直播复盘的"+createReqVO);
        return success(liveBroadcastingService.createLiveBroadcasting(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新直播货盘")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:update')")
    public CommonResult<Boolean> updateLiveBroadcasting(@Valid @RequestBody ErpLiveBroadcastingSaveReqVO updateReqVO) {
        liveBroadcastingService.updateLiveBroadcasting(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除直播货盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:delete')")
    public CommonResult<Boolean> deleteLiveBroadcasting(@RequestParam("ids") List<Long> ids) {
        liveBroadcastingService.deleteLiveBroadcasting(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得直播货盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:query')")
    public CommonResult<ErpLiveBroadcastingRespVO> getLiveBroadcasting(@RequestParam("id") Long id) {
        return success(liveBroadcastingService.getLiveBroadcastingVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得直播货盘分页")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:query')")
    public CommonResult<PageResult<ErpLiveBroadcastingRespVO>> getLiveBroadcastingPage(@Valid ErpLiveBroadcastingPageReqVO pageReqVO) {
        PageResult<ErpLiveBroadcastingRespVO> pageResult = liveBroadcastingService.getLiveBroadcastingVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得直播货盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:query')")
    public CommonResult<List<ErpLiveBroadcastingRespVO>> getLiveBroadcastingListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpLiveBroadcastingRespVO> list = liveBroadcastingService.getLiveBroadcastingVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出直播货盘 Excel")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportLiveBroadcastingExcel(@Valid ErpLiveBroadcastingPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpLiveBroadcastingRespVO> pageResult = liveBroadcastingService.getLiveBroadcastingVOPage(pageReqVO);
        System.out.println("查看直播货盘" + pageResult.getList());
        // 转换为导出VO
        List<ErpLiveBroadcastingExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpLiveBroadcastingExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "直播货盘信息.xlsx", "数据", ErpLiveBroadcastingExportVO.class,
        exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入直播货盘")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpLiveBroadcastingImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpLiveBroadcastingImportExcelVO> list = ExcelUtils.read(inputStream, ErpLiveBroadcastingImportExcelVO.class);
            return success(liveBroadcastingService.importLiveBroadcastingList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入直播货盘模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpLiveBroadcastingExportVO> list = Arrays.asList(
                ErpLiveBroadcastingExportVO.builder()
                        .no("示例编号1")
                        .productName("示例产品")
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "直播货盘导入模板.xls", "直播货盘列表", ErpLiveBroadcastingExportVO.class, list);
    }
}
