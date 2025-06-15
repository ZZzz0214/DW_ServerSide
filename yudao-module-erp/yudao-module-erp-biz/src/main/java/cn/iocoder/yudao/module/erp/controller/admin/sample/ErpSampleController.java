package cn.iocoder.yudao.module.erp.controller.admin.sample;


import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.*;
import cn.iocoder.yudao.module.erp.service.sample.ErpSampleService;
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

@Tag(name = "管理后台 - ERP 样品")
@RestController
@RequestMapping("/erp/sample")
@Validated
public class ErpSampleController {

    @Resource
    private ErpSampleService sampleService;

    @PostMapping("/create")
    @Operation(summary = "创建样品")
    @PreAuthorize("@ss.hasPermission('erp:sample:create')")
    public CommonResult<Long> createSample(@Valid @RequestBody ErpSampleSaveReqVO createReqVO) {
        return success(sampleService.createSample(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新样品")
    @PreAuthorize("@ss.hasPermission('erp:sample:update')")
    public CommonResult<Boolean> updateSample(@Valid @RequestBody ErpSampleSaveReqVO updateReqVO) {
        sampleService.updateSample(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除样品")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:sample:delete')")
    public CommonResult<Boolean> deleteSample(@RequestParam("ids") List<Long> ids) {
        sampleService.deleteSample(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得样品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:sample:query')")
    public CommonResult<ErpSampleRespVO> getSample(@RequestParam("id") Long id) {
        return success(sampleService.getSampleVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得样品分页")
    @PreAuthorize("@ss.hasPermission('erp:sample:query')")
    public CommonResult<PageResult<ErpSampleRespVO>> getSamplePage(@Valid ErpSamplePageReqVO pageReqVO) {
        PageResult<ErpSampleRespVO> pageResult = sampleService.getSampleVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得样品列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:sample:query')")
    public CommonResult<List<ErpSampleRespVO>> getSampleListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpSampleRespVO> list = sampleService.getSampleVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出样品 Excel")
    @PreAuthorize("@ss.hasPermission('erp:sample:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSampleExcel(@Valid ErpSamplePageReqVO pageReqVO,
                                 HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpSampleRespVO> pageResult = sampleService.getSampleVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpSampleExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpSampleExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "样品信息.xlsx", "数据", ErpSampleExportVO.class, exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入样品")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:sample:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpSampleImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpSampleImportExcelVO> list = ExcelUtils.read(inputStream, ErpSampleImportExcelVO.class);
            return success(sampleService.importSampleList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入样品模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpSampleExportVO> list = Arrays.asList(
                ErpSampleExportVO.builder()
                        .no("示例编号1")
                        .logisticsCompany("顺丰快递")
                        .logisticsNo("SF123456789")
                        .receiverName("张三")
                        .contactPhone("13800138000")
                        .address("北京市朝阳区XX街道XX号")
                        .comboProductId("CP001")
                        .productSpec("标准规格")
                        .productQuantity(1)
                        .customerName("示例客户")
                        .sampleStatus(1)
                        .remark("示例备注")
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "样品导入模板.xls", "样品列表", ErpSampleExportVO.class, list);
    }
}
