package cn.iocoder.yudao.module.erp.controller.admin.dropship;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionExportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.dropship.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.dropship.ErpDropshipAssistDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.service.dropship.ErpDropshipAssistService;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
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
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean;

@Tag(name = "管理后台 - ERP 代发辅助")
@RestController
@RequestMapping("/erp/dropship-assist")
@Validated
public class ErpDropshipAssistController {

    @Resource
    private ErpDropshipAssistService dropshipAssistService;
    @Resource
    private ErpComboProductService comboProductService;

    @PostMapping("/create")
    @Operation(summary = "创建代发辅助")
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:create')")
    public CommonResult<Long> createDropshipAssist(@Valid @RequestBody ErpDropshipAssistSaveReqVO createReqVO) {
        return success(dropshipAssistService.createDropshipAssist(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新代发辅助")
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:update')")
    public CommonResult<Boolean> updateDropshipAssist(@Valid @RequestBody ErpDropshipAssistSaveReqVO updateReqVO) {
        dropshipAssistService.updateDropshipAssist(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除代发辅助")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:delete')")
    public CommonResult<Boolean> deleteDropshipAssist(@RequestParam("ids") List<Long> ids) {
        dropshipAssistService.deleteDropshipAssist(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得代发辅助")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:query')")
    public CommonResult<ErpDropshipAssistRespVO> getDropshipAssist(@RequestParam("id") Long id) {
        // 查询基础信息
        ErpDropshipAssistDO dropshipAssist = dropshipAssistService.getDropshipAssist(id);
        if (dropshipAssist == null) {
            return success(null);
        }

        // 转换为VO
        ErpDropshipAssistRespVO respVO = BeanUtils.toBean(dropshipAssist, ErpDropshipAssistRespVO.class);

        // 如果有关联组品，查询组品信息
        if (dropshipAssist.getComboProductId() != null && !dropshipAssist.getComboProductId().trim().isEmpty()) {
            try {
                Long comboProductId = Long.parseLong(dropshipAssist.getComboProductId());
                ErpComboProductDO comboProduct = comboProductService.getCombo(comboProductId);
                if (comboProduct != null) {
                    respVO.setName(comboProduct.getName());
                    respVO.setProductName(comboProduct.getName());
                    respVO.setProductShortName(comboProduct.getShortName());
                    respVO.setShippingCode(comboProduct.getShippingCode());
                    respVO.setComboProductNo(comboProduct.getNo());
                }
            } catch (NumberFormatException e) {
                // 记录日志但不影响主流程
                System.err.println("组品编号格式错误: " + dropshipAssist.getComboProductId() + ", 错误: " + e.getMessage());
            }
        }
        return success(respVO);

    }

    @GetMapping("/page")
    @Operation(summary = "获得代发辅助分页")
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:query')")
    public CommonResult<PageResult<ErpDropshipAssistRespVO>> getDropshipAssistPage(@Valid ErpDropshipAssistPageReqVO pageReqVO) {
        PageResult<ErpDropshipAssistRespVO> pageResult = dropshipAssistService.getDropshipAssistVOPage(pageReqVO);
        System.out.println("查看代发"+pageResult.getList());
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得代发辅助列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:query')")
    public CommonResult<List<ErpDropshipAssistRespVO>> getDropshipAssistListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpDropshipAssistRespVO> list = dropshipAssistService.getDropshipAssistVOList(ids);
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出代发辅助 Excel")
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDropshipAssistExcel(@Valid ErpDropshipAssistPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpDropshipAssistRespVO> pageResult = dropshipAssistService.getDropshipAssistVOPage(pageReqVO);
        // 转换为导出VO
        List<ErpDropshipAssistExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpDropshipAssistExportVO.class);
        // 导出 Excel
        ExcelUtils.write(response, "代发辅助信息.xlsx", "数据", ErpDropshipAssistExportVO.class,
        exportList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入代发辅助")
    @Parameters({
        @Parameter(name = "file", description = "Excel 文件", required = true),
        @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpDropshipAssistImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpDropshipAssistImportExcelVO> list = ExcelUtils.read(inputStream, ErpDropshipAssistImportExcelVO.class);
            return success(dropshipAssistService.importDropshipAssistList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入代发辅助模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpDropshipAssistExportVO> list = Arrays.asList(
                ErpDropshipAssistExportVO.builder().build()
        );
        // 输出
        ExcelUtils.write(response, "代发辅助导入模板.xls", "代发辅助列表", ErpDropshipAssistExportVO.class, list);
    }
}
