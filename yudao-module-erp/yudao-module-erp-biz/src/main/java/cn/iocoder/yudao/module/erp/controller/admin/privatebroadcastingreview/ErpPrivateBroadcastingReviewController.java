package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview;

import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.service.privatebroadcastingreview.ErpPrivateBroadcastingReviewService;
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

@Tag(name = "管理后台 - ERP 私播复盘")
@RestController
@RequestMapping("/erp/private-broadcasting-review")
@Validated
public class ErpPrivateBroadcastingReviewController {

    @Resource
    private ErpPrivateBroadcastingReviewService privateBroadcastingReviewService;

    @PostMapping("/create")
    @Operation(summary = "创建私播复盘")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:create')")
    public CommonResult<Long> createPrivateBroadcastingReview(@Valid @RequestBody ErpPrivateBroadcastingReviewSaveReqVO createReqVO) {
        return success(privateBroadcastingReviewService.createPrivateBroadcastingReview(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新私播复盘")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:update')")
    public CommonResult<Boolean> updatePrivateBroadcastingReview(@Valid @RequestBody ErpPrivateBroadcastingReviewSaveReqVO updateReqVO) {
        privateBroadcastingReviewService.updatePrivateBroadcastingReview(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除私播复盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:delete')")
    public CommonResult<Boolean> deletePrivateBroadcastingReview(@RequestParam("ids") List<Long> ids) {
        privateBroadcastingReviewService.deletePrivateBroadcastingReview(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得私播复盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:query')")
    public CommonResult<ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReview(@RequestParam("id") Long id) {
        return success(privateBroadcastingReviewService.getPrivateBroadcastingReviewVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得私播复盘分页")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:query')")
    public CommonResult<PageResult<ErpPrivateBroadcastingReviewRespVO>> getPrivateBroadcastingReviewPage(@Valid ErpPrivateBroadcastingReviewPageReqVO pageReqVO) {
        PageResult<ErpPrivateBroadcastingReviewRespVO> pageResult = privateBroadcastingReviewService.getPrivateBroadcastingReviewVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得私播复盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-review:query')")
    public CommonResult<List<ErpPrivateBroadcastingReviewRespVO>> getPrivateBroadcastingReviewListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpPrivateBroadcastingReviewRespVO> list = privateBroadcastingReviewService.getPrivateBroadcastingReviewVOList(ids);
        return success(list);
    }
}
