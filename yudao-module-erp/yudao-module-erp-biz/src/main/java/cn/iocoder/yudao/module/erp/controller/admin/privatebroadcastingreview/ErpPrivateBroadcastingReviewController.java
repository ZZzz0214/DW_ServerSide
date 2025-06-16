package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview;

import java.util.Arrays;
import java.util.Collections;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewExportVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.service.privatebroadcastingreview.ErpPrivateBroadcastingReviewService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 私播复盘")
@RestController
@RequestMapping("/erp/private-broadcasting-review")
@Validated
public class ErpPrivateBroadcastingReviewController {

    @Resource
    private ErpPrivateBroadcastingReviewService privateBroadcastingReviewService;

    @PostMapping("/create")
    @Operation(summary = "创建私播复盘")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:create')")
    public CommonResult<Long> createPrivateBroadcastingReview(@Valid @RequestBody ErpPrivateBroadcastingReviewSaveReqVO createReqVO) {
        return success(privateBroadcastingReviewService.createPrivateBroadcastingReview(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新私播复盘")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:update')")
    public CommonResult<Boolean> updatePrivateBroadcastingReview(@Valid @RequestBody ErpPrivateBroadcastingReviewSaveReqVO updateReqVO) {
        privateBroadcastingReviewService.updatePrivateBroadcastingReview(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除私播复盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:delete')")
    public CommonResult<Boolean> deletePrivateBroadcastingReview(@RequestParam("ids") List<Long> ids) {
        privateBroadcastingReviewService.deletePrivateBroadcastingReview(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得私播复盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:query')")
    public CommonResult<ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReview(@RequestParam("id") Long id) {
        return success(privateBroadcastingReviewService.getPrivateBroadcastingReviewVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得私播复盘分页")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:query')")
    public CommonResult<PageResult<ErpPrivateBroadcastingReviewRespVO>> getPrivateBroadcastingReviewPage(@Valid ErpPrivateBroadcastingReviewPageReqVO pageReqVO) {
        PageResult<ErpPrivateBroadcastingReviewRespVO> pageResult = privateBroadcastingReviewService.getPrivateBroadcastingReviewVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得私播复盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:query')")
    public CommonResult<List<ErpPrivateBroadcastingReviewRespVO>> getPrivateBroadcastingReviewListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpPrivateBroadcastingReviewRespVO> list = privateBroadcastingReviewService.getPrivateBroadcastingReviewVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出私播复盘 Excel")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPrivateBroadcastingReviewExcel(@Valid ErpPrivateBroadcastingReviewPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpPrivateBroadcastingReviewRespVO> pageResult = privateBroadcastingReviewService.getPrivateBroadcastingReviewVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpPrivateBroadcastingReviewExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpPrivateBroadcastingReviewExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "私播复盘信息.xlsx", "数据", ErpPrivateBroadcastingReviewExportVO.class,
        exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入私播复盘")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpPrivateBroadcastingReviewImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpPrivateBroadcastingReviewImportExcelVO> list = ExcelUtils.read(inputStream, ErpPrivateBroadcastingReviewImportExcelVO.class);
            return success(privateBroadcastingReviewService.importPrivateBroadcastingReviewList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入私播复盘模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpPrivateBroadcastingReviewExportVO> list = Arrays.asList(
                ErpPrivateBroadcastingReviewExportVO.builder()
                        .no("示例编号1").build()
        );
        // 输出
        ExcelUtils.write(response, "私播复盘导入模板.xls", "私播复盘列表", ErpPrivateBroadcastingReviewExportVO.class, list);
    }
}
