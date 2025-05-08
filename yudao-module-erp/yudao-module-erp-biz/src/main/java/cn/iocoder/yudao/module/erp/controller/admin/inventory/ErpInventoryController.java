package cn.iocoder.yudao.module.erp.controller.admin.inventory;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventoryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventoryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventorySaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.inventory.ErpInventoryDO;
import cn.iocoder.yudao.module.erp.service.inventory.ErpInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 库存")
@RestController
@RequestMapping("/erp/inventory")
@Validated
public class ErpInventoryController {

    @Resource
    private ErpInventoryService inventoryService;

    @PostMapping("/create")
    @Operation(summary = "创建库存")
    @PreAuthorize("@ss.hasPermission('erp:inventory:create')")
    public CommonResult<Long> createInventory(@Valid @RequestBody ErpInventorySaveReqVO createReqVO) {
        return success(inventoryService.createInventory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库存")
    @PreAuthorize("@ss.hasPermission('erp:inventory:update')")
    public CommonResult<Boolean> updateInventory(@Valid @RequestBody ErpInventorySaveReqVO updateReqVO) {
        inventoryService.updateInventory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:inventory:delete')")
    public CommonResult<Boolean> deleteInventory(@RequestParam("ids") List<Long> ids) {
        inventoryService.deleteInventory(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库存")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:inventory:query')")
    public CommonResult<ErpInventoryRespVO> getInventory(@RequestParam("id") Long id) {
        ErpInventoryDO inventory = inventoryService.getInventory(id);
        return success(BeanUtils.toBean(inventory, ErpInventoryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存分页")
    @PreAuthorize("@ss.hasPermission('erp:inventory:query')")
    public CommonResult<PageResult<ErpInventoryRespVO>> getInventoryPage(@Valid ErpInventoryPageReqVO pageReqVO) {
        PageResult<ErpInventoryRespVO> pageResult = inventoryService.getInventoryVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得库存列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:inventory:query')")
    public CommonResult<List<ErpInventoryRespVO>> getInventoryListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpInventoryRespVO> list = inventoryService.getInventoryVOList(ids);
        return success(list);
    }
}
