package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting;


import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.listener.RowIndexListener;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingExportVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingSaveReqVO;
import cn.iocoder.yudao.module.erp.service.privatebroadcasting.ErpPrivateBroadcastingService;
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

@Tag(name = "管理后台 - ERP 私播货盘")
@RestController
@RequestMapping("/erp/private-broadcasting")
@Validated
public class ErpPrivateBroadcastingController {

    @Resource
    private ErpPrivateBroadcastingService privateBroadcastingService;

    @PostMapping("/create")
    @Operation(summary = "创建私播货盘")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:create')")
    public CommonResult<Long> createPrivateBroadcasting(@Valid @RequestBody ErpPrivateBroadcastingSaveReqVO createReqVO) {
        return success(privateBroadcastingService.createPrivateBroadcasting(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新私播货盘")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:update')")
    public CommonResult<Boolean> updatePrivateBroadcasting(@Valid @RequestBody ErpPrivateBroadcastingSaveReqVO updateReqVO) {
        privateBroadcastingService.updatePrivateBroadcasting(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除私播货盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:delete')")
    public CommonResult<Boolean> deletePrivateBroadcasting(@RequestParam("ids") List<Long> ids) {
        privateBroadcastingService.deletePrivateBroadcasting(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得私播货盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:query')")
    public CommonResult<ErpPrivateBroadcastingRespVO> getPrivateBroadcasting(@RequestParam("id") Long id) {
        return success(privateBroadcastingService.getPrivateBroadcastingVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得私播货盘分页")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:query')")
    public CommonResult<PageResult<ErpPrivateBroadcastingRespVO>> getPrivateBroadcastingPage(@Valid ErpPrivateBroadcastingPageReqVO pageReqVO) {
        PageResult<ErpPrivateBroadcastingRespVO> pageResult = privateBroadcastingService.getPrivateBroadcastingVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得私播货盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:query')")
    public CommonResult<List<ErpPrivateBroadcastingRespVO>> getPrivateBroadcastingListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpPrivateBroadcastingRespVO> list = privateBroadcastingService.getPrivateBroadcastingVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出私播货盘 Excel")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPrivateBroadcastingExcel(@Valid ErpPrivateBroadcastingPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpPrivateBroadcastingRespVO> pageResult = privateBroadcastingService.getPrivateBroadcastingVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpPrivateBroadcastingExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpPrivateBroadcastingExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "私播货盘信息.xlsx", "数据", ErpPrivateBroadcastingExportVO.class,
        exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入私播货盘")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpPrivateBroadcastingImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpPrivateBroadcastingImportExcelVO> list = ExcelUtils.read(inputStream, ErpPrivateBroadcastingImportExcelVO.class, new RowIndexListener<>());
            return success(privateBroadcastingService.importPrivateBroadcastingList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入私播货盘模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpPrivateBroadcastingExportVO> list = Arrays.asList(
                ErpPrivateBroadcastingExportVO.builder()
                        .no("示例编号1")
                        .productName("示例产品")
                        .privateStatus("未设置")
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "私播货盘导入模板.xls", "私播货盘列表", ErpPrivateBroadcastingExportVO.class, list);
    }
}
