package cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting;

import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingSaveReqVO;
import cn.iocoder.yudao.module.erp.service.livebroadcasting.ErpLiveBroadcastingService;
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

@Tag(name = "管理后台 - ERP 直播货盘")
@RestController
@RequestMapping("/erp/live-broadcasting")
@Validated
public class ErpLiveBroadcastingController {

    @Resource
    private ErpLiveBroadcastingService liveBroadcastingService;

    @PostMapping("/create")
    @Operation(summary = "创建直播货盘")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:create')")
    public CommonResult<Long> createLiveBroadcasting(@Valid @RequestBody ErpLiveBroadcastingSaveReqVO createReqVO) {
        return success(liveBroadcastingService.createLiveBroadcasting(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新直播货盘")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:update')")
    public CommonResult<Boolean> updateLiveBroadcasting(@Valid @RequestBody ErpLiveBroadcastingSaveReqVO updateReqVO) {
        liveBroadcastingService.updateLiveBroadcasting(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除直播货盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:delete')")
    public CommonResult<Boolean> deleteLiveBroadcasting(@RequestParam("ids") List<Long> ids) {
        liveBroadcastingService.deleteLiveBroadcasting(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得直播货盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:query')")
    public CommonResult<ErpLiveBroadcastingRespVO> getLiveBroadcasting(@RequestParam("id") Long id) {
        return success(liveBroadcastingService.getLiveBroadcastingVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得直播货盘分页")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:query')")
    public CommonResult<PageResult<ErpLiveBroadcastingRespVO>> getLiveBroadcastingPage(@Valid ErpLiveBroadcastingPageReqVO pageReqVO) {
        PageResult<ErpLiveBroadcastingRespVO> pageResult = liveBroadcastingService.getLiveBroadcastingVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得直播货盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting:query')")
    public CommonResult<List<ErpLiveBroadcastingRespVO>> getLiveBroadcastingListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpLiveBroadcastingRespVO> list = liveBroadcastingService.getLiveBroadcastingVOList(ids);
        return success(list);
    }
}