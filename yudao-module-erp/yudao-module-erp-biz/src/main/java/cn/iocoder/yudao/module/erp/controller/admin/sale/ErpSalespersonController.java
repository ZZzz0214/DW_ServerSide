package cn.iocoder.yudao.module.erp.controller.admin.sale;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalespersonDO;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalespersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

@Tag(name = "管理后台 - ERP 销售人员")
@RestController
@RequestMapping("/erp/salesperson")
@Validated
public class ErpSalespersonController {

    @Resource
    private ErpSalespersonService salespersonService;

    @PostMapping("/create")
    @Operation(summary = "创建销售人员")
    @PreAuthorize("@ss.hasPermission('erp:salesperson:create')")
    public CommonResult<Long> createSalesperson(@Valid @RequestBody ErpSalespersonSaveReqVO createReqVO) {
        return success(salespersonService.createSalesperson(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售人员")
    @PreAuthorize("@ss.hasPermission('erp:salesperson:update')")
    public CommonResult<Boolean> updateSalesperson(@Valid @RequestBody ErpSalespersonSaveReqVO updateReqVO) {
        salespersonService.updateSalesperson(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售人员")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:salesperson:delete')")
    public CommonResult<Boolean> deleteSalesperson(@RequestParam("ids") List<Long> ids) {
        salespersonService.deleteSalesperson(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售人员")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:salesperson:query')")
    public CommonResult<ErpSalespersonRespVO> getSalesperson(@RequestParam("id") Long id) {
        ErpSalespersonDO salesperson = salespersonService.getSalesperson(id);
        return success(BeanUtils.toBean(salesperson, ErpSalespersonRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售人员分页")
    @PreAuthorize("@ss.hasPermission('erp:salesperson:query')")
    public CommonResult<PageResult<ErpSalespersonRespVO>> getSalespersonPage(@Valid ErpSalespersonPageReqVO pageReqVO) {
        PageResult<ErpSalespersonRespVO> pageResult = salespersonService.getSalespersonVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得销售人员列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:salesperson:query')")
    public CommonResult<List<ErpSalespersonRespVO>> getSalespersonListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpSalespersonRespVO> list = salespersonService.getSalespersonVOList(ids);
        return success(list);
    }
    @GetMapping("/search")
    @Operation(summary = "搜索销售人员")
    @PreAuthorize("@ss.hasPermission('erp:salesperson:query')")
    public CommonResult<List<ErpSalespersonRespVO>> searchSalespersons(@Valid ErpSalespersonPageReqVO searchReqVO) {
        List<ErpSalespersonRespVO> list = salespersonService.searchSalespersons(searchReqVO);
        System.out.println("销售人员："+list);
        return success(list);
    }
}
