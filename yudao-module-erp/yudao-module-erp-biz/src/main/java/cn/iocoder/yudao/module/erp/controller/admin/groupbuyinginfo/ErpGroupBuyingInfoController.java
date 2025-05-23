package cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo;

import java.util.Collections;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.service.groupbuyinginfo.ErpGroupBuyingInfoService;
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

@Tag(name = "管理后台 - ERP 团购信息")
@RestController
@RequestMapping("/erp/group-buying-info")
@Validated
public class ErpGroupBuyingInfoController {

    @Resource
    private ErpGroupBuyingInfoService groupBuyingInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建团购信息")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:create')")
    public CommonResult<Long> createGroupBuyingInfo(@Valid @RequestBody ErpGroupBuyingInfoSaveReqVO createReqVO) {
        return success(groupBuyingInfoService.createGroupBuyingInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新团购信息")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:update')")
    public CommonResult<Boolean> updateGroupBuyingInfo(@Valid @RequestBody ErpGroupBuyingInfoSaveReqVO updateReqVO) {
        groupBuyingInfoService.updateGroupBuyingInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除团购信息")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:delete')")
    public CommonResult<Boolean> deleteGroupBuyingInfo(@RequestParam("ids") List<Long> ids) {
        groupBuyingInfoService.deleteGroupBuyingInfo(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得团购信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:query')")
    public CommonResult<ErpGroupBuyingInfoRespVO> getGroupBuyingInfo(@RequestParam("id") Long id) {
        return success(groupBuyingInfoService.getGroupBuyingInfoVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得团购信息分页")
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:query')")
    public CommonResult<PageResult<ErpGroupBuyingInfoRespVO>> getGroupBuyingInfoPage(@Valid ErpGroupBuyingInfoPageReqVO pageReqVO) {
        PageResult<ErpGroupBuyingInfoRespVO> pageResult = groupBuyingInfoService.getGroupBuyingInfoVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得团购信息列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying-info:query')")
    public CommonResult<List<ErpGroupBuyingInfoRespVO>> getGroupBuyingInfoListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpGroupBuyingInfoRespVO> list = groupBuyingInfoService.getGroupBuyingInfoVOList(ids);
        return success(list);
    }
}
