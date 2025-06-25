package cn.iocoder.yudao.module.erp.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpNotebookStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpNotebookStatisticsRespVO;
import cn.iocoder.yudao.module.erp.service.statistics.ErpNotebookStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - ERP 记事本统计
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - ERP 记事本统计")
@RestController
@RequestMapping("/erp/statistics/notebook")
@Validated
public class ErpNotebookStatisticsController {

    @Resource
    private ErpNotebookStatisticsService notebookStatisticsService;

    @GetMapping("/get")
    @Operation(summary = "获得记事本统计")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<ErpNotebookStatisticsRespVO> getNotebookStatistics(@Valid ErpNotebookStatisticsReqVO reqVO) {
        return success(notebookStatisticsService.getNotebookStatistics(reqVO));
    }

    @GetMapping("/get-task-person-list")
    @Operation(summary = "获得任务人员列表")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<List<String>> getTaskPersonList(@RequestParam(value = "keyword", required = false) String keyword) {
        return success(notebookStatisticsService.getTaskPersonList(keyword));
    }

    @GetMapping("/get-task-person-options")
    @Operation(summary = "获得任务人员选项列表")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<List<ErpNotebookStatisticsRespVO.TaskPersonOption>> getTaskPersonOptions(@RequestParam(value = "keyword", required = false) String keyword) {
        return success(notebookStatisticsService.getTaskPersonOptions(keyword));
    }
} 