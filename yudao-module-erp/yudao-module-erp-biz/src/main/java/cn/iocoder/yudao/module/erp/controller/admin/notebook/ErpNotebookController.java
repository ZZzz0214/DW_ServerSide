package cn.iocoder.yudao.module.erp.controller.admin.notebook;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.notebook.vo.ErpNotebookSaveReqVO;
import cn.iocoder.yudao.module.erp.service.notebook.ErpNotebookService;
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

@Tag(name = "管理后台 - ERP 记事本")
@RestController
@RequestMapping("/erp/notebook")
@Validated
public class ErpNotebookController {

    @Resource
    private ErpNotebookService notebookService;

    @PostMapping("/create")
    @Operation(summary = "创建记事本")
    @PreAuthorize("@ss.hasPermission('erp:notebook:create')")
    public CommonResult<Long> createNotebook(@Valid @RequestBody ErpNotebookSaveReqVO createReqVO) {
        return success(notebookService.createNotebook(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新记事本")
    @PreAuthorize("@ss.hasPermission('erp:notebook:update')")
    public CommonResult<Boolean> updateNotebook(@Valid @RequestBody ErpNotebookSaveReqVO updateReqVO) {
        notebookService.updateNotebook(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除记事本")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:notebook:delete')")
    public CommonResult<Boolean> deleteNotebook(@RequestParam("ids") List<Long> ids) {
        notebookService.deleteNotebook(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得记事本")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:notebook:query')")
    public CommonResult<ErpNotebookRespVO> getNotebook(@RequestParam("id") Long id) {
        return success(notebookService.getNotebookVOList(Collections.singleton(id)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得记事本分页")
    @PreAuthorize("@ss.hasPermission('erp:notebook:query')")
    public CommonResult<PageResult<ErpNotebookRespVO>> getNotebookPage(@Valid ErpNotebookPageReqVO pageReqVO) {
        PageResult<ErpNotebookRespVO> pageResult = notebookService.getNotebookVOPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得记事本列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:notebook:query')")
    public CommonResult<List<ErpNotebookRespVO>> getNotebookListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpNotebookRespVO> list = notebookService.getNotebookVOList(ids);
        return success(list);
    }
}
