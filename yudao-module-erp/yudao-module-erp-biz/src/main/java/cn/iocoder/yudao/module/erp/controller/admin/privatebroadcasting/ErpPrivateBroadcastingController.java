package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting;


import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingSaveReqVO;
import cn.iocoder.yudao.module.erp.service.privatebroadcasting.ErpPrivateBroadcastingService;
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

@Tag(name = "管理后台 - ERP 私播货盘")
@RestController
@RequestMapping("/erp/private-broadcasting")
@Validated
public class ErpPrivateBroadcastingController {

    @Resource
    private ErpPrivateBroadcastingService privateBroadcastingService;

    @PostMapping("/create")
    @Operation(summary = "创建私播货盘")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:create')")
    public CommonResult<Long> createPrivateBroadcasting(@Valid @RequestBody ErpPrivateBroadcastingSaveReqVO createReqVO) {
        return success(privateBroadcastingService.createPrivateBroadcasting(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新私播货盘")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:update')")
    public CommonResult<Boolean> updatePrivateBroadcasting(@Valid @RequestBody ErpPrivateBroadcastingSaveReqVO updateReqVO) {
        privateBroadcastingService.updatePrivateBroadcasting(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除私播货盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:delete')")
    public CommonResult<Boolean> deletePrivateBroadcasting(@RequestParam("ids") List<Long> ids) {
        privateBroadcastingService.deletePrivateBroadcasting(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得私播货盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:query')")
    public CommonResult<ErpPrivateBroadcastingRespVO> getPrivateBroadcasting(@RequestParam("id") Long id) {
        return success(privateBroadcastingService.getPrivateBroadcastingVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得私播货盘分页")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:query')")
    public CommonResult<PageResult<ErpPrivateBroadcastingRespVO>> getPrivateBroadcastingPage(@Valid ErpPrivateBroadcastingPageReqVO pageReqVO) {
        PageResult<ErpPrivateBroadcastingRespVO> pageResult = privateBroadcastingService.getPrivateBroadcastingVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得私播货盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting:query')")
    public CommonResult<List<ErpPrivateBroadcastingRespVO>> getPrivateBroadcastingListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpPrivateBroadcastingRespVO> list = privateBroadcastingService.getPrivateBroadcastingVOList(ids);
        return success(list);
    }
}
