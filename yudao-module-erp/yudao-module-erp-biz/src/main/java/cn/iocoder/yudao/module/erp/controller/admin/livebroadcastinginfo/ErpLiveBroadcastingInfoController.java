package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo;


import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.service.livebroadcastinginfo.ErpLiveBroadcastingInfoService;
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

@Tag(name = "管理后台 - ERP 直播信息")
@RestController
@RequestMapping("/erp/live-broadcasting-info")
@Validated
public class ErpLiveBroadcastingInfoController {

    @Resource
    private ErpLiveBroadcastingInfoService liveBroadcastingInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建直播信息")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:create')")
    public CommonResult<Long> createLiveBroadcastingInfo(@Valid @RequestBody ErpLiveBroadcastingInfoSaveReqVO createReqVO) {
        return success(liveBroadcastingInfoService.createLiveBroadcastingInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新直播信息")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:update')")
    public CommonResult<Boolean> updateLiveBroadcastingInfo(@Valid @RequestBody ErpLiveBroadcastingInfoSaveReqVO updateReqVO) {
        liveBroadcastingInfoService.updateLiveBroadcastingInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除直播信息")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:delete')")
    public CommonResult<Boolean> deleteLiveBroadcastingInfo(@RequestParam("ids") List<Long> ids) {
        liveBroadcastingInfoService.deleteLiveBroadcastingInfo(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得直播信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:query')")
    public CommonResult<ErpLiveBroadcastingInfoRespVO> getLiveBroadcastingInfo(@RequestParam("id") Long id) {
        ErpLiveBroadcastingInfoRespVO vo = liveBroadcastingInfoService.getLiveBroadcastingInfoVOList(Collections.singleton(id)).get(0);
        return success(vo);
    }

    @GetMapping("/page")
    @Operation(summary = "获得直播信息分页")
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:query')")
    public CommonResult<PageResult<ErpLiveBroadcastingInfoRespVO>> getLiveBroadcastingInfoPage(@Valid ErpLiveBroadcastingInfoPageReqVO pageReqVO) {
        PageResult<ErpLiveBroadcastingInfoRespVO> pageResult = liveBroadcastingInfoService.getLiveBroadcastingInfoVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得直播信息列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:live-broadcasting-info:query')")
    public CommonResult<List<ErpLiveBroadcastingInfoRespVO>> getLiveBroadcastingInfoListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpLiveBroadcastingInfoRespVO> list = liveBroadcastingInfoService.getLiveBroadcastingInfoVOList(ids);
        return success(list);
    }
}
