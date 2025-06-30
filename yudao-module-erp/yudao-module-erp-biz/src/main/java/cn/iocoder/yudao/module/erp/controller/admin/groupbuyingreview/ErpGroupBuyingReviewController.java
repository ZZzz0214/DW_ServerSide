package cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview;
import java.io.IOException;
import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.listener.RowIndexListener;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.*;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingReviewDO;
import cn.iocoder.yudao.module.erp.service.groupbuyingreview.ErpGroupBuyingReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import javax.servlet.http.HttpServletResponse;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 团购复盘")
@RestController
@RequestMapping("/erp/group-buying-review")
@Validated
public class ErpGroupBuyingReviewController {

    @Resource
    private ErpGroupBuyingReviewService groupBuyingReviewService;

    @PostMapping("/create")
    @Operation(summary = "创建团购复盘")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:create')")
    public CommonResult<Long> createGroupBuyingReview(@Valid @RequestBody ErpGroupBuyingReviewSaveReqVO createReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        return success(groupBuyingReviewService.createGroupBuyingReview(createReqVO, currentUsername));
    }

    @PutMapping("/update")
    @Operation(summary = "更新团购复盘")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:update')")
    public CommonResult<Boolean> updateGroupBuyingReview(@Valid @RequestBody ErpGroupBuyingReviewSaveReqVO updateReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        groupBuyingReviewService.updateGroupBuyingReview(updateReqVO, currentUsername);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除团购复盘")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:delete')")
    public CommonResult<Boolean> deleteGroupBuyingReview(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        groupBuyingReviewService.deleteGroupBuyingReview(Collections.singletonList(id), currentUsername);
        return success(true);
    }

    @DeleteMapping("/batch-delete")
    @Operation(summary = "批量删除团购复盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:delete')")
    public CommonResult<Boolean> deleteGroupBuyingReviews(@RequestParam("ids") List<Long> ids) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        groupBuyingReviewService.deleteGroupBuyingReview(ids, currentUsername);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得团购复盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:query')")
    public CommonResult<ErpGroupBuyingReviewRespVO> getGroupBuyingReview(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        List<ErpGroupBuyingReviewRespVO> list = groupBuyingReviewService.getGroupBuyingReviewVOList(Collections.singletonList(id), currentUsername);
        if (list.isEmpty()) {
            return success(null);
        }
        return success(list.get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得团购复盘分页")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:query')")
    public CommonResult<PageResult<ErpGroupBuyingReviewRespVO>> getGroupBuyingReviewPage(@Valid ErpGroupBuyingReviewPageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        PageResult<ErpGroupBuyingReviewRespVO> pageResult = groupBuyingReviewService.getGroupBuyingReviewVOPage(pageReqVO, currentUsername);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得团购复盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:query')")
    public CommonResult<List<ErpGroupBuyingReviewRespVO>> getGroupBuyingReviewListByIds(@RequestParam("ids") List<Long> ids) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        List<ErpGroupBuyingReviewRespVO> list = groupBuyingReviewService.getGroupBuyingReviewVOList(ids, currentUsername);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出团购复盘 Excel")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportGroupBuyingReviewExcel(@Valid ErpGroupBuyingReviewPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpGroupBuyingReviewRespVO> list = groupBuyingReviewService.getGroupBuyingReviewVOPage(pageReqVO, currentUsername).getList();
        // 导出 Excel
        ExcelUtils.write(response, "团购复盘.xls", "数据", ErpGroupBuyingReviewExportVO.class,
                        BeanUtils.toBean(list, ErpGroupBuyingReviewExportVO.class));
    }

    @PostMapping("/import")
    @Operation(summary = "导入团购复盘")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true),
            @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpGroupBuyingReviewImportRespVO> importGroupBuyingReviewExcel(@RequestParam("file") MultipartFile file,
                                                                                       @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String currentUsername = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpGroupBuyingReviewImportExcelVO> list = ExcelUtils.read(inputStream, ErpGroupBuyingReviewImportExcelVO.class, new RowIndexListener<>());
            return success(groupBuyingReviewService.importGroupBuyingReviewList(list, updateSupport, currentUsername));
        }
        }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入团购复盘模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导入模板 demo
        List<ErpGroupBuyingReviewImportExcelVO> list = Arrays.asList(
                ErpGroupBuyingReviewImportExcelVO.builder()
                        .no("示例编号1")
                        .remark("示例备注")
                        .customerName("示例客户")
                        .groupBuyingId("示例团购货盘编号")
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "团购复盘导入模板.xls", "团购复盘列表", ErpGroupBuyingReviewImportExcelVO.class, list);
    }
}
