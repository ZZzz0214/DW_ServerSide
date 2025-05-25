package cn.iocoder.yudao.module.erp.controller.admin.dropship;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.dropship.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.dropship.ErpDropshipAssistDO;
import cn.iocoder.yudao.module.erp.service.dropship.ErpDropshipAssistService;
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
import static cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean;

@Tag(name = "管理后台 - ERP 代发辅助")
@RestController
@RequestMapping("/erp/dropship-assist")
@Validated
public class ErpDropshipAssistController {

    @Resource
    private ErpDropshipAssistService dropshipAssistService;

    @PostMapping("/create")
    @Operation(summary = "创建代发辅助")
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:create')")
    public CommonResult<Long> createDropshipAssist(@Valid @RequestBody ErpDropshipAssistSaveReqVO createReqVO) {
        return success(dropshipAssistService.createDropshipAssist(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新代发辅助")
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:update')")
    public CommonResult<Boolean> updateDropshipAssist(@Valid @RequestBody ErpDropshipAssistSaveReqVO updateReqVO) {
        dropshipAssistService.updateDropshipAssist(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除代发辅助")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:delete')")
    public CommonResult<Boolean> deleteDropshipAssist(@RequestParam("ids") List<Long> ids) {
        dropshipAssistService.deleteDropshipAssist(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得代发辅助")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:query')")
    public CommonResult<ErpDropshipAssistRespVO> getDropshipAssist(@RequestParam("id") Long id) {
        ErpDropshipAssistDO dropshipAssist = dropshipAssistService.getDropshipAssist(id);
        return success(toBean(dropshipAssist, ErpDropshipAssistRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得代发辅助分页")
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:query')")
    public CommonResult<PageResult<ErpDropshipAssistRespVO>> getDropshipAssistPage(@Valid ErpDropshipAssistPageReqVO pageReqVO) {
        PageResult<ErpDropshipAssistRespVO> pageResult = dropshipAssistService.getDropshipAssistVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得代发辅助列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:dropship-assist:query')")
    public CommonResult<List<ErpDropshipAssistRespVO>> getDropshipAssistListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpDropshipAssistRespVO> list = dropshipAssistService.getDropshipAssistVOList(ids);
        return success(list);
    }
}