package cn.iocoder.yudao.module.erp.controller.admin.groupbuying;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.listener.RowIndexListener;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingExportVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingSaveReqVO;
import cn.iocoder.yudao.module.erp.service.groupbuying.ErpGroupBuyingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 团购货盘")
@RestController
@RequestMapping("/erp/group-buying")
@Validated
@Slf4j
public class ErpGroupBuyingController {

    @Resource
    private ErpGroupBuyingService groupBuyingService;

    @PostMapping("/create")
    @Operation(summary = "创建团购货盘")
    @PreAuthorize("@ss.hasPermission('erp:group-buying:create')")
    public CommonResult<Long> createGroupBuying(@Valid @RequestBody ErpGroupBuyingSaveReqVO createReqVO) {
        return success(groupBuyingService.createGroupBuying(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新团购货盘")
    @PreAuthorize("@ss.hasPermission('erp:group-buying:update')")
    public CommonResult<Boolean> updateGroupBuying(@Valid @RequestBody ErpGroupBuyingSaveReqVO updateReqVO) {
        groupBuyingService.updateGroupBuying(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除团购货盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying:delete')")
    public CommonResult<Boolean> deleteGroupBuying(@RequestParam("ids") List<Long> ids) {
        groupBuyingService.deleteGroupBuying(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得团购货盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:group-buying:query')")
    public CommonResult<ErpGroupBuyingRespVO> getGroupBuying(@RequestParam("id") Long id) {
        return success(groupBuyingService.getGroupBuyingVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得团购货盘分页")
    @PreAuthorize("@ss.hasPermission('erp:group-buying:query')")
    public CommonResult<PageResult<ErpGroupBuyingRespVO>> getGroupBuyingPage(@Valid ErpGroupBuyingPageReqVO pageReqVO) {
        PageResult<ErpGroupBuyingRespVO> pageResult = groupBuyingService.getGroupBuyingVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得团购货盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying:query')")
    public CommonResult<List<ErpGroupBuyingRespVO>> getGroupBuyingListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpGroupBuyingRespVO> list = groupBuyingService.getGroupBuyingVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出团购货盘 Excel")
    @PreAuthorize("@ss.hasPermission('erp:group-buying:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportGroupBuyingExcel(@Valid ErpGroupBuyingPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpGroupBuyingRespVO> pageResult = groupBuyingService.getGroupBuyingVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpGroupBuyingExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpGroupBuyingExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "团购货盘.xlsx", "数据", ErpGroupBuyingExportVO.class,
                exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入团购货盘")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:group-buying:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpGroupBuyingImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpGroupBuyingImportExcelVO> list = ExcelUtils.read(inputStream, ErpGroupBuyingImportExcelVO.class, new RowIndexListener<>());
            return success(groupBuyingService.importGroupBuyingList(list, updateSupport));
        } catch (Exception e) {
            // 记录详细错误信息
            log.error("导入团购货盘失败", e);
            throw new RuntimeException("导入失败: " + e.getMessage(), e);
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入团购货盘模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpGroupBuyingExportVO> list = Arrays.asList(
                ErpGroupBuyingExportVO.builder()
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "团购货盘导入模板.xls", "团购货盘列表", ErpGroupBuyingExportVO.class, list);
    }
}
