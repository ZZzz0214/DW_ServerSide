package cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo;

import java.util.Arrays;
import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.listener.RowIndexListener;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoExportVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.service.groupbuyinginfo.ErpGroupBuyingInfoService;
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
import java.util.List;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 团购信息")
@RestController
@RequestMapping("/erp/group-buying-info")
@Validated
public class ErpGroupBuyingInfoController {

    @Resource
    private ErpGroupBuyingInfoService groupBuyingInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建团购信息")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:create')")
    public CommonResult<Long> createGroupBuyingInfo(@Valid @RequestBody ErpGroupBuyingInfoSaveReqVO createReqVO) {
        return success(groupBuyingInfoService.createGroupBuyingInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新团购信息")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:update')")
    public CommonResult<Boolean> updateGroupBuyingInfo(@Valid @RequestBody ErpGroupBuyingInfoSaveReqVO updateReqVO) {
        groupBuyingInfoService.updateGroupBuyingInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除团购信息")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:delete')")
    public CommonResult<Boolean> deleteGroupBuyingInfo(@RequestParam("ids") List<Long> ids) {
        groupBuyingInfoService.deleteGroupBuyingInfo(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得团购信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:query')")
    public CommonResult<ErpGroupBuyingInfoRespVO> getGroupBuyingInfo(@RequestParam("id") Long id) {
        return success(groupBuyingInfoService.getGroupBuyingInfoVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得团购信息分页")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:query')")
    public CommonResult<PageResult<ErpGroupBuyingInfoRespVO>> getGroupBuyingInfoPage(@Valid ErpGroupBuyingInfoPageReqVO pageReqVO) {
        PageResult<ErpGroupBuyingInfoRespVO> pageResult = groupBuyingInfoService.getGroupBuyingInfoVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得团购信息列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:query')")
    public CommonResult<List<ErpGroupBuyingInfoRespVO>> getGroupBuyingInfoListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpGroupBuyingInfoRespVO> list = groupBuyingInfoService.getGroupBuyingInfoVOList(ids);
        return success(list);
    }
    @GetMapping("/export-excel")
    @Operation(summary = "导出团购信息 Excel")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportGroupBuyingInfoExcel(@Valid ErpGroupBuyingInfoPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpGroupBuyingInfoRespVO> pageResult = groupBuyingInfoService.getGroupBuyingInfoVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpGroupBuyingInfoExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpGroupBuyingInfoExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "团购信息.xlsx", "数据", ErpGroupBuyingInfoExportVO.class,
                exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入团购信息")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpGroupBuyingInfoImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpGroupBuyingInfoImportExcelVO> list = ExcelUtils.read(inputStream, ErpGroupBuyingInfoImportExcelVO.class, new RowIndexListener<>());
            return success(groupBuyingInfoService.importGroupBuyingInfoList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入团购信息模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpGroupBuyingInfoExportVO> list = Arrays.asList(
                ErpGroupBuyingInfoExportVO.builder()
                        .no("示例编号1")
                        .customerName("示例客户")
                        .customerPosition("采购经理")
                        .customerWechat("wechat123")
                        .platformName("美团")
                        .customerAttribute("KA客户")
                        .customerCity("北京市")
                        .customerDistrict("朝阳区")
                        .userPortrait("高端用户")
                        .recruitmentCategory("食品饮料")
                        .selectionCriteria("高毛利")
                        .remark("示例备注")
                        .build()
        );
        // 输出
        ExcelUtils.write(response, "团购信息导入模板.xls", "团购信息列表", ErpGroupBuyingInfoExportVO.class, list);
    }
}
