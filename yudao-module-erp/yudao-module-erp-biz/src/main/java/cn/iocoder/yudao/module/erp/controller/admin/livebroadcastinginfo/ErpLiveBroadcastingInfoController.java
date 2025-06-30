package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo;


import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.listener.RowIndexListener;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoExportVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.service.livebroadcastinginfo.ErpLiveBroadcastingInfoService;
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

@Tag(name = "管理后台 - ERP 直播信息")
@RestController
@RequestMapping("/erp/live-broadcasting-info")
@Validated
public class ErpLiveBroadcastingInfoController {

    @Resource
    private ErpLiveBroadcastingInfoService liveBroadcastingInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建直播信息")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:create')")
    public CommonResult<Long> createLiveBroadcastingInfo(@Valid @RequestBody ErpLiveBroadcastingInfoSaveReqVO createReqVO) {
        System.out.println("传递的参数"+createReqVO);
        return success(liveBroadcastingInfoService.createLiveBroadcastingInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新直播信息")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:update')")
    public CommonResult<Boolean> updateLiveBroadcastingInfo(@Valid @RequestBody ErpLiveBroadcastingInfoSaveReqVO updateReqVO) {
        liveBroadcastingInfoService.updateLiveBroadcastingInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除直播信息")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:delete')")
    public CommonResult<Boolean> deleteLiveBroadcastingInfo(@RequestParam("ids") List<Long> ids) {
        liveBroadcastingInfoService.deleteLiveBroadcastingInfo(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得直播信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:query')")
    public CommonResult<ErpLiveBroadcastingInfoRespVO> getLiveBroadcastingInfo(@RequestParam("id") Long id) {
        ErpLiveBroadcastingInfoRespVO vo = liveBroadcastingInfoService.getLiveBroadcastingInfoVOList(Collections.singleton(id)).get(0);
        return success(vo);
    }

    @GetMapping("/page")
    @Operation(summary = "获得直播信息分页")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:query')")
    public CommonResult<PageResult<ErpLiveBroadcastingInfoRespVO>> getLiveBroadcastingInfoPage(@Valid ErpLiveBroadcastingInfoPageReqVO pageReqVO) {
        PageResult<ErpLiveBroadcastingInfoRespVO> pageResult = liveBroadcastingInfoService.getLiveBroadcastingInfoVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得直播信息列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:query')")
    public CommonResult<List<ErpLiveBroadcastingInfoRespVO>> getLiveBroadcastingInfoListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpLiveBroadcastingInfoRespVO> list = liveBroadcastingInfoService.getLiveBroadcastingInfoVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出直播信息 Excel")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportLiveBroadcastingInfoExcel(@Valid ErpLiveBroadcastingInfoPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpLiveBroadcastingInfoRespVO> pageResult = liveBroadcastingInfoService.getLiveBroadcastingInfoVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpLiveBroadcastingInfoExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpLiveBroadcastingInfoExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "直播信息.xlsx", "数据", ErpLiveBroadcastingInfoExportVO.class,
        exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入直播信息")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpLiveBroadcastingInfoImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpLiveBroadcastingInfoImportExcelVO> list = ExcelUtils.read(inputStream, ErpLiveBroadcastingInfoImportExcelVO.class, new RowIndexListener<>());
            return success(liveBroadcastingInfoService.importLiveBroadcastingInfoList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入直播信息模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpLiveBroadcastingInfoExportVO> list = Arrays.asList(
                ErpLiveBroadcastingInfoExportVO.builder()
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "直播信息导入模板.xls", "直播信息列表", ErpLiveBroadcastingInfoExportVO.class, list);
    }
}
