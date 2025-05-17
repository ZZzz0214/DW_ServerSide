package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview;

import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.service.livebroadcastingreview.ErpLiveBroadcastingReviewService;
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

@Tag(name = "管理后台 - ERP 直播复盘")
@RestController
@RequestMapping("/erp/live-broadcasting-review")
@Validated
public class ErpLiveBroadcastingReviewController {

    @Resource
    private ErpLiveBroadcastingReviewService liveBroadcastingReviewService;

    @PostMapping("/create")
    @Operation(summary = "创建直播复盘")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:create')")
    public CommonResult<Long> createLiveBroadcastingReview(@Valid @RequestBody ErpLiveBroadcastingReviewSaveReqVO createReqVO) {
        return success(liveBroadcastingReviewService.createLiveBroadcastingReview(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新直播复盘")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:update')")
    public CommonResult<Boolean> updateLiveBroadcastingReview(@Valid @RequestBody ErpLiveBroadcastingReviewSaveReqVO updateReqVO) {
        liveBroadcastingReviewService.updateLiveBroadcastingReview(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除直播复盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:delete')")
    public CommonResult<Boolean> deleteLiveBroadcastingReview(@RequestParam("ids") List<Long> ids) {
        liveBroadcastingReviewService.deleteLiveBroadcastingReview(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得直播复盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:query')")
    public CommonResult<ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReview(@RequestParam("id") Long id) {
        ErpLiveBroadcastingReviewRespVO vo = liveBroadcastingReviewService.getLiveBroadcastingReviewVOList(Collections.singleton(id)).get(0);
        return success(vo);
    }

    @GetMapping("/page")
    @Operation(summary = "获得直播复盘分页")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:query')")
    public CommonResult<PageResult<ErpLiveBroadcastingReviewRespVO>> getLiveBroadcastingReviewPage(@Valid ErpLiveBroadcastingReviewPageReqVO pageReqVO) {
        PageResult<ErpLiveBroadcastingReviewRespVO> pageResult = liveBroadcastingReviewService.getLiveBroadcastingReviewVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得直播复盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-review:query')")
    public CommonResult<List<ErpLiveBroadcastingReviewRespVO>> getLiveBroadcastingReviewListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpLiveBroadcastingReviewRespVO> list = liveBroadcastingReviewService.getLiveBroadcastingReviewVOList(ids);
        return success(list);
    }
}
