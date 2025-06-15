package cn.iocoder.yudao.module.erp.controller.admin.notebook;


import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.*;
import cn.iocoder.yudao.module.erp.service.notebook.ErpNotebookService;
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
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 记事本")
@RestController
@RequestMapping("/erp/notebook")
@Validated
public class ErpNotebookController {

    @Resource
    private ErpNotebookService notebookService;

    @PostMapping("/create")
    @Operation(summary = "创建记事本")
    @PreAuthorize("@ss.hasPermission('erp:notebook:create')")
    public CommonResult<Long> createNotebook(@Valid @RequestBody ErpNotebookSaveReqVO createReqVO) {
        return success(notebookService.createNotebook(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新记事本")
    @PreAuthorize("@ss.hasPermission('erp:notebook:update')")
    public CommonResult<Boolean> updateNotebook(@Valid @RequestBody ErpNotebookSaveReqVO updateReqVO) {
        notebookService.updateNotebook(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除记事本")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:notebook:delete')")
    public CommonResult<Boolean> deleteNotebook(@RequestParam("ids") List<Long> ids) {
        notebookService.deleteNotebook(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得记事本")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:notebook:query')")
    public CommonResult<ErpNotebookRespVO> getNotebook(@RequestParam("id") Long id) {
        return success(notebookService.getNotebookVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得记事本分页")
    @PreAuthorize("@ss.hasPermission('erp:notebook:query')")
    public CommonResult<PageResult<ErpNotebookRespVO>> getNotebookPage(@Valid ErpNotebookPageReqVO pageReqVO) {
        PageResult<ErpNotebookRespVO> pageResult = notebookService.getNotebookVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得记事本列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:notebook:query')")
    public CommonResult<List<ErpNotebookRespVO>> getNotebookListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpNotebookRespVO> list = notebookService.getNotebookVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出记事本 Excel")
    @PreAuthorize("@ss.hasPermission('erp:notebook:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportNotebookExcel(@Valid ErpNotebookPageReqVO pageReqVO,
                                   HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpNotebookRespVO> pageResult = notebookService.getNotebookVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpNotebookExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpNotebookExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "记事本信息.xlsx", "数据", ErpNotebookExportVO.class, exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入记事本")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:notebook:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpNotebookImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpNotebookImportExcelVO> list = ExcelUtils.read(inputStream, ErpNotebookImportExcelVO.class);
            return success(notebookService.importNotebookList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入记事本模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpNotebookExportVO> list = Arrays.asList(
                ErpNotebookExportVO.builder()
                        .no("示例编号1")
                        .taskName("示例任务")
                        .taskStatus(1)
                        .taskPerson("张三")
                        .remark("示例备注")
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "记事本导入模板.xls", "记事本列表", ErpNotebookExportVO.class, list);
    }
}
