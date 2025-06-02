package cn.iocoder.yudao.module.erp.controller.admin.product;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboSearchReqVO;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - ERP 组合产品")
@RestController
@RequestMapping("/erp/combo")
@Validated
public class ErpComboProductController {

    @Resource
    private ErpComboProductService comboProductService;

//    @PostMapping("/create")
//    @Operation(summary = "创建组合产品")
//    @PreAuthorize("@ss.hasPermission('erp:combo-product:create')")
//    public CommonResult<Long> createComboProduct(@Valid @RequestBody ErpComboSaveReqVO createReqVO) {
//        return success(comboProductService.createCombo(createReqVO));
//    }
    @PostMapping("/create")
    @Operation(summary = "创建组合产品")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:create')")
    public CommonResult<Long> createComboProduct(@Valid @RequestBody ErpComboSaveReqVO createReqVO) {
        return success(comboProductService.createCombo(createReqVO));
    }
    @PutMapping("/update")
    @Operation(summary = "更新组合产品")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:update')")
    public CommonResult<Boolean> updateComboProduct(@Valid @RequestBody ErpComboSaveReqVO updateReqVO) {
        comboProductService.updateCombo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除组合产品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:combo-product:delete')")
    public CommonResult<Boolean> deleteComboProduct(@RequestParam("id") Long id) {
        comboProductService.deleteCombo(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得组合产品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:query')")
    public CommonResult<ErpComboRespVO> getComboProduct(@RequestParam("id") Long id) {
        ErpComboRespVO comboProduct = comboProductService.getComboWithItems(id);
        return CommonResult.success(comboProduct);
    }

    @GetMapping("/page")
    @Operation(summary = "获得组合产品分页")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:query')")
    public CommonResult<PageResult<ErpComboRespVO>> getComboProductPage(@Valid ErpComboPageReqVO pageReqVO) {
        return success(comboProductService.getComboVOPage(pageReqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得组合产品精简列表", description = "只包含被开启的组合产品，主要用于前端的下拉选项")
    public CommonResult<List<ErpComboRespVO>> getComboProductSimpleList() {
        List<ErpComboRespVO> list = comboProductService.getComboProductVOListByStatus(CommonStatusEnum.ENABLE.getStatus());
//        return success(convertList(list, product -> new ErpComboRespVO()
//                .setId(product.getId())
//                .setName(product.getName())
//                .setShortName(product.getShortName())
//                .setShippingCode(product.getShippingCode())));
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出组合产品 Excel")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportComboProductExcel(@Valid ErpComboPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpComboRespVO> pageResult = comboProductService.getComboVOPage(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "组品信息.xlsx", "数据", ErpComboRespVO.class,
                pageResult.getList());
    }

    @GetMapping("/search")
    @Operation(summary = "搜索组合产品")
    @PreAuthorize("@ss.hasPermission('erp:combo-product:query')")
    public CommonResult<List<ErpComboRespVO>> searchCombos(@Valid ErpComboSearchReqVO searchReqVO) {
        List<ErpComboRespVO> combos = comboProductService.searchCombos(searchReqVO);
        return success(combos);
    }
}
