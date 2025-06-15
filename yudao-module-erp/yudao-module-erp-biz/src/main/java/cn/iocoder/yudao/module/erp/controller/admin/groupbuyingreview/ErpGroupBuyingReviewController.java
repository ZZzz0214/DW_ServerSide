package cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview;
import java.io.IOException;
import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.*;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewSaveReqVO;
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
        return success(groupBuyingReviewService.createGroupBuyingReview(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新团购复盘")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:update')")
    public CommonResult<Boolean> updateGroupBuyingReview(@Valid @RequestBody ErpGroupBuyingReviewSaveReqVO updateReqVO) {
        groupBuyingReviewService.updateGroupBuyingReview(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除团购复盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:delete')")
    public CommonResult<Boolean> deleteGroupBuyingReview(@RequestParam("ids") List<Long> ids) {
        groupBuyingReviewService.deleteGroupBuyingReview(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得团购复盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:query')")
    public CommonResult<ErpGroupBuyingReviewRespVO> getGroupBuyingReview(@RequestParam("id") Long id) {
        return success(groupBuyingReviewService.getGroupBuyingReviewVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得团购复盘分页")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:query')")
    public CommonResult<PageResult<ErpGroupBuyingReviewRespVO>> getGroupBuyingReviewPage(@Valid ErpGroupBuyingReviewPageReqVO pageReqVO) {
        PageResult<ErpGroupBuyingReviewRespVO> pageResult = groupBuyingReviewService.getGroupBuyingReviewVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得团购复盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:query')")
    public CommonResult<List<ErpGroupBuyingReviewRespVO>> getGroupBuyingReviewListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpGroupBuyingReviewRespVO> list = groupBuyingReviewService.getGroupBuyingReviewVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出团购复盘 Excel")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportGroupBuyingReviewExcel(@Valid ErpGroupBuyingReviewPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpGroupBuyingReviewRespVO> pageResult = groupBuyingReviewService.getGroupBuyingReviewVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpGroupBuyingReviewExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpGroupBuyingReviewExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "团购复盘.xlsx", "数据", ErpGroupBuyingReviewExportVO.class,
                exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入团购复盘")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:group-buying-review:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpGroupBuyingReviewImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpGroupBuyingReviewImportExcelVO> list = ExcelUtils.read(inputStream, ErpGroupBuyingReviewImportExcelVO.class);
            return success(groupBuyingReviewService.importGroupBuyingReviewList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
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
