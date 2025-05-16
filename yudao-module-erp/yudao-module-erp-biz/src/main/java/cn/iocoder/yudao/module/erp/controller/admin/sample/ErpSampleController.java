package cn.iocoder.yudao.module.erp.controller.admin.sample;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSamplePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleSaveReqVO;
import cn.iocoder.yudao.module.erp.service.sample.ErpSampleService;
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

@Tag(name = "管理后台 - ERP 样品")
@RestController
@RequestMapping("/erp/sample")
@Validated
public class ErpSampleController {

    @Resource
    private ErpSampleService sampleService;

    @PostMapping("/create")
    @Operation(summary = "创建样品")
    @PreAuthorize("@ss.hasPermission('erp:sample:create')")
    public CommonResult<Long> createSample(@Valid @RequestBody ErpSampleSaveReqVO createReqVO) {
        return success(sampleService.createSample(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新样品")
    @PreAuthorize("@ss.hasPermission('erp:sample:update')")
    public CommonResult<Boolean> updateSample(@Valid @RequestBody ErpSampleSaveReqVO updateReqVO) {
        sampleService.updateSample(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除样品")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:sample:delete')")
    public CommonResult<Boolean> deleteSample(@RequestParam("ids") List<Long> ids) {
        sampleService.deleteSample(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得样品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:sample:query')")
    public CommonResult<ErpSampleRespVO> getSample(@RequestParam("id") Long id) {
        return success(sampleService.getSampleVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得样品分页")
    @PreAuthorize("@ss.hasPermission('erp:sample:query')")
    public CommonResult<PageResult<ErpSampleRespVO>> getSamplePage(@Valid ErpSamplePageReqVO pageReqVO) {
        PageResult<ErpSampleRespVO> pageResult = sampleService.getSampleVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得样品列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:sample:query')")
    public CommonResult<List<ErpSampleRespVO>> getSampleListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpSampleRespVO> list = sampleService.getSampleVOList(ids);
        return success(list);
    }
}
