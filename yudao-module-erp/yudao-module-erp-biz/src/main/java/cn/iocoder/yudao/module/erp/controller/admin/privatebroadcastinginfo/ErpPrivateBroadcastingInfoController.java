package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo;

import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.*;
import cn.iocoder.yudao.module.erp.service.privatebroadcastinginfo.ErpPrivateBroadcastingInfoService;
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

@Tag(name = "管理后台 - ERP 私播信息")
@RestController
@RequestMapping("/erp/private-broadcasting-info")
@Validated
public class ErpPrivateBroadcastingInfoController {

    @Resource
    private ErpPrivateBroadcastingInfoService privateBroadcastingInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建私播信息")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:create')")
    public CommonResult<Long> createPrivateBroadcastingInfo(@Valid @RequestBody ErpPrivateBroadcastingInfoSaveReqVO createReqVO) {
        return success(privateBroadcastingInfoService.createPrivateBroadcastingInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新私播信息")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:update')")
    public CommonResult<Boolean> updatePrivateBroadcastingInfo(@Valid @RequestBody ErpPrivateBroadcastingInfoSaveReqVO updateReqVO) {
        privateBroadcastingInfoService.updatePrivateBroadcastingInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除私播信息")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:delete')")
    public CommonResult<Boolean> deletePrivateBroadcastingInfo(@RequestParam("ids") List<Long> ids) {
        privateBroadcastingInfoService.deletePrivateBroadcastingInfo(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得私播信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:query')")
    public CommonResult<ErpPrivateBroadcastingInfoRespVO> getPrivateBroadcastingInfo(@RequestParam("id") Long id) {
        return success(privateBroadcastingInfoService.getPrivateBroadcastingInfoVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得私播信息分页")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:query')")
    public CommonResult<PageResult<ErpPrivateBroadcastingInfoRespVO>> getPrivateBroadcastingInfoPage(@Valid ErpPrivateBroadcastingInfoPageReqVO pageReqVO) {
        PageResult<ErpPrivateBroadcastingInfoRespVO> pageResult = privateBroadcastingInfoService.getPrivateBroadcastingInfoVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得私播信息列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:query')")
    public CommonResult<List<ErpPrivateBroadcastingInfoRespVO>> getPrivateBroadcastingInfoListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpPrivateBroadcastingInfoRespVO> list = privateBroadcastingInfoService.getPrivateBroadcastingInfoVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出私播信息 Excel")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPrivateBroadcastingInfoExcel(@Valid ErpPrivateBroadcastingInfoPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpPrivateBroadcastingInfoRespVO> pageResult = privateBroadcastingInfoService.getPrivateBroadcastingInfoVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpPrivateBroadcastingInfoExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpPrivateBroadcastingInfoExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "私播信息.xlsx", "数据", ErpPrivateBroadcastingInfoExportVO.class,
        exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入私播信息")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpPrivateBroadcastingInfoImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpPrivateBroadcastingInfoImportExcelVO> list = ExcelUtils.read(inputStream, ErpPrivateBroadcastingInfoImportExcelVO.class);
            return success(privateBroadcastingInfoService.importPrivateBroadcastingInfoList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入私播信息模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpPrivateBroadcastingInfoExportVO> list = Arrays.asList(
                ErpPrivateBroadcastingInfoExportVO.builder()
                        .no("示例编号1")
                        .customerName("张三")
                        .customerPosition("主播") // 字典值：客户职位
                        .customerWechat("zhangsan123")
                        .platformName("抖音") // 字典值：平台名称
                        .customerAttribute("KOL") // 字典值：客户属性
                        .customerCity("北京") // 字典值：客户城市
                        .customerDistrict("朝阳区") // 字典值：客户区县
                        .userPortrait("时尚达人")
                        .recruitmentCategory("美妆")
                        .selectionCriteria("国货品牌")
                        .remark("示例备注")
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "私播信息导入模板.xls", "私播信息列表", ErpPrivateBroadcastingInfoExportVO.class, list);
    }
}