package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo;

import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.service.privatebroadcastinginfo.ErpPrivateBroadcastingInfoService;
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

@Tag(name = "管理后台 - ERP 私播信息")
@RestController
@RequestMapping("/erp/private-broadcasting-info")
@Validated
public class ErpPrivateBroadcastingInfoController {

    @Resource
    private ErpPrivateBroadcastingInfoService privateBroadcastingInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建私播信息")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:create')")
    public CommonResult<Long> createPrivateBroadcastingInfo(@Valid @RequestBody ErpPrivateBroadcastingInfoSaveReqVO createReqVO) {
        return success(privateBroadcastingInfoService.createPrivateBroadcastingInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新私播信息")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:update')")
    public CommonResult<Boolean> updatePrivateBroadcastingInfo(@Valid @RequestBody ErpPrivateBroadcastingInfoSaveReqVO updateReqVO) {
        privateBroadcastingInfoService.updatePrivateBroadcastingInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除私播信息")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:delete')")
    public CommonResult<Boolean> deletePrivateBroadcastingInfo(@RequestParam("ids") List<Long> ids) {
        privateBroadcastingInfoService.deletePrivateBroadcastingInfo(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得私播信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:query')")
    public CommonResult<ErpPrivateBroadcastingInfoRespVO> getPrivateBroadcastingInfo(@RequestParam("id") Long id) {
        return success(privateBroadcastingInfoService.getPrivateBroadcastingInfoVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得私播信息分页")
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:query')")
    public CommonResult<PageResult<ErpPrivateBroadcastingInfoRespVO>> getPrivateBroadcastingInfoPage(@Valid ErpPrivateBroadcastingInfoPageReqVO pageReqVO) {
        PageResult<ErpPrivateBroadcastingInfoRespVO> pageResult = privateBroadcastingInfoService.getPrivateBroadcastingInfoVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得私播信息列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:private-broadcasting-info:query')")
    public CommonResult<List<ErpPrivateBroadcastingInfoRespVO>> getPrivateBroadcastingInfoListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpPrivateBroadcastingInfoRespVO> list = privateBroadcastingInfoService.getPrivateBroadcastingInfoVOList(ids);
        return success(list);
    }
}