package cn.iocoder.yudao.module.erp.controller.admin.groupbuying;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingSaveReqVO;
import cn.iocoder.yudao.module.erp.service.groupbuying.ErpGroupBuyingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 团购货盘")
@RestController
@RequestMapping("/erp/group-buying")
@Validated
public class ErpGroupBuyingController {

    @Resource
    private ErpGroupBuyingService groupBuyingService;

    @PostMapping("/create")
    @Operation(summary = "创建团购货盘")
    @PreAuthorize("@ss.hasPermission('erp:group-buying:create')")
    public CommonResult<Long> createGroupBuying(@Valid @RequestBody ErpGroupBuyingSaveReqVO createReqVO) {
        return success(groupBuyingService.createGroupBuying(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新团购货盘")
    @PreAuthorize("@ss.hasPermission('erp:group-buying:update')")
    public CommonResult<Boolean> updateGroupBuying(@Valid @RequestBody ErpGroupBuyingSaveReqVO updateReqVO) {
        groupBuyingService.updateGroupBuying(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除团购货盘")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying:delete')")
    public CommonResult<Boolean> deleteGroupBuying(@RequestParam("ids") List<Long> ids) {
        groupBuyingService.deleteGroupBuying(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得团购货盘")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:group-buying:query')")
    public CommonResult<ErpGroupBuyingRespVO> getGroupBuying(@RequestParam("id") Long id) {
        return success(groupBuyingService.getGroupBuyingVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得团购货盘分页")
    @PreAuthorize("@ss.hasPermission('erp:group-buying:query')")
    public CommonResult<PageResult<ErpGroupBuyingRespVO>> getGroupBuyingPage(@Valid ErpGroupBuyingPageReqVO pageReqVO) {
        PageResult<ErpGroupBuyingRespVO> pageResult = groupBuyingService.getGroupBuyingVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得团购货盘列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:group-buying:query')")
    public CommonResult<List<ErpGroupBuyingRespVO>> getGroupBuyingListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpGroupBuyingRespVO> list = groupBuyingService.getGroupBuyingVOList(ids);
        return success(list);
    }
}
