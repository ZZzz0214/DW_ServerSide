package cn.iocoder.yudao.module.erp.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleStatisticsRespVO;
import cn.iocoder.yudao.module.erp.service.statistics.ErpDistributionWholesaleStatisticsService;
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
 * 管理后台 - ERP 代发批发统计
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - ERP 代发批发统计")
@RestController
@RequestMapping("/erp/statistics/distribution-wholesale")
@Validated
public class ErpDistributionWholesaleStatisticsController {

    @Resource
    private ErpDistributionWholesaleStatisticsService statisticsService;

    @GetMapping("/get")
    @Operation(summary = "获得代发批发统计")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<ErpDistributionWholesaleStatisticsRespVO> getDistributionWholesaleStatistics(@Valid ErpDistributionWholesaleStatisticsReqVO reqVO) {
        return success(statisticsService.getDistributionWholesaleStatistics(reqVO));
    }

    @GetMapping("/get-category-list")
    @Operation(summary = "获得统计分类列表")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<List<String>> getCategoryList(@RequestParam("statisticsType") String statisticsType,
                                                      @RequestParam(value = "keyword", required = false) String keyword) {
        return success(statisticsService.getCategoryList(statisticsType, keyword));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得人员详细统计")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<ErpDistributionWholesaleStatisticsRespVO.DetailStatistics> getDetailStatistics(
            @Valid ErpDistributionWholesaleStatisticsReqVO reqVO,
            @RequestParam("categoryName") String categoryName) {
        return success(statisticsService.getDetailStatistics(reqVO, categoryName));
    }

    @GetMapping("/get-audit-statistics")
    @Operation(summary = "获得代发批发审核数量统计")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<ErpDistributionWholesaleStatisticsRespVO.AuditStatistics> getAuditStatistics(@Valid ErpDistributionWholesaleStatisticsReqVO reqVO) {
        return success(statisticsService.getAuditStatistics(reqVO));
    }

}
