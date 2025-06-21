package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview;

import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewExportVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewImportRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastingreview.ErpLiveBroadcastingReviewDO;
import cn.iocoder.yudao.module.erp.service.livebroadcastingreview.ErpLiveBroadcastingReviewService;
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

@Tag(name = "管理后台 - ERP 直播复盘")
@RestController
@RequestMapping("/erp/live-broadcasting-review")
@Validated
public class ErpLiveBroadcastingReviewController {

    @Resource
    private ErpLiveBroadcastingReviewService liveBroadcastingReviewService;

    @PostMapping("/create")
    @Operation(summary = "创建直播复盘")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:create')")
    public CommonResult<Long> createLiveBroadcastingReview(@Valid @RequestBody ErpLiveBroadcastingReviewSaveReqVO createReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        return success(liveBroadcastingReviewService.createLiveBroadcastingReview(createReqVO, currentUsername));
    }

    @PutMapping("/update")
    @Operation(summary = "更新直播复盘")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:update')")
    public CommonResult<Boolean> updateLiveBroadcastingReview(@Valid @RequestBody ErpLiveBroadcastingReviewSaveReqVO updateReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        liveBroadcastingReviewService.updateLiveBroadcastingReview(updateReqVO, currentUsername);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除直播复盘")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:delete')")
    public CommonResult<Boolean> deleteLiveBroadcastingReview(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        liveBroadcastingReviewService.deleteLiveBroadcastingReview(Collections.singletonList(id), currentUsername);
        return success(true);
    }

    @DeleteMapping("/batch-delete")
    @Operation(summary = "批量删除直播复盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:delete')")
    public CommonResult<Boolean> deleteLiveBroadcastingReviews(@RequestParam("ids") List<Long> ids) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        liveBroadcastingReviewService.deleteLiveBroadcastingReview(ids, currentUsername);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得直播复盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:query')")
    public CommonResult<ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReview(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        List<ErpLiveBroadcastingReviewRespVO> list = liveBroadcastingReviewService.getLiveBroadcastingReviewVOList(Collections.singletonList(id), currentUsername);
        if (list.isEmpty()) {
            return success(null);
        }
        return success(list.get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得直播复盘分页")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:query')")
    public CommonResult<PageResult<ErpLiveBroadcastingReviewRespVO>> getLiveBroadcastingReviewPage(@Valid ErpLiveBroadcastingReviewPageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        PageResult<ErpLiveBroadcastingReviewRespVO> pageResult = liveBroadcastingReviewService.getLiveBroadcastingReviewVOPage(pageReqVO, currentUsername);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "获得直播复盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:query')")
    public CommonResult<List<ErpLiveBroadcastingReviewRespVO>> getLiveBroadcastingReviewListByIds(@RequestParam("ids") List<Long> ids) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        List<ErpLiveBroadcastingReviewRespVO> list = liveBroadcastingReviewService.getLiveBroadcastingReviewVOList(ids, currentUsername);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出直播复盘 Excel")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportLiveBroadcastingReviewExcel(@Valid ErpLiveBroadcastingReviewPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpLiveBroadcastingReviewRespVO> list = liveBroadcastingReviewService.getLiveBroadcastingReviewVOPage(pageReqVO, currentUsername).getList();
        // 导出 Excel
        ExcelUtils.write(response, "直播复盘.xls", "数据", ErpLiveBroadcastingReviewExportVO.class,
                        BeanUtils.toBean(list, ErpLiveBroadcastingReviewExportVO.class));
    }

    @PostMapping("/import")
    @Operation(summary = "导入直播复盘")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true),
            @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:import')")
    public CommonResult<ErpLiveBroadcastingReviewImportRespVO> importLiveBroadcastingReviewExcel(@RequestParam("file") MultipartFile file,
                                                                                                 @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        List<ErpLiveBroadcastingReviewImportExcelVO> list = ExcelUtils.read(file, ErpLiveBroadcastingReviewImportExcelVO.class);
        return success(liveBroadcastingReviewService.importLiveBroadcastingReviewList(list, updateSupport, currentUsername));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入直播复盘模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpLiveBroadcastingReviewExportVO> list = Arrays.asList(
                ErpLiveBroadcastingReviewExportVO.builder()
                        .no("示例编号1")
                        .liveBroadcastingNo("LP001")
                        .customerName("示例客户")
                        .livePlatform("抖音")
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "直播复盘导入模板.xls", "直播复盘列表", ErpLiveBroadcastingReviewExportVO.class, list);
    }

    @GetMapping("/copy")
    @Operation(summary = "复制直播复盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:query')")
    public CommonResult<ErpLiveBroadcastingReviewRespVO> copyLiveBroadcastingReview(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        List<ErpLiveBroadcastingReviewRespVO> list = liveBroadcastingReviewService.getLiveBroadcastingReviewVOList(Collections.singletonList(id), currentUsername);
        if (list.isEmpty()) {
            return success(null);
        }
        return success(list.get(0));
    }
}
