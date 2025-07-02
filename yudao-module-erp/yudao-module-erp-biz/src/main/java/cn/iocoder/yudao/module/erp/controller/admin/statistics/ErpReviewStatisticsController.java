package cn.iocoder.yudao.module.erp.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.review.ErpReviewStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.review.ErpReviewStatisticsRespVO;
import cn.iocoder.yudao.module.erp.service.statistics.ErpReviewStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - ERP 复盘统计
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - ERP 复盘统计")
@RestController
@RequestMapping("/erp/statistics/review")
@Validated
public class ErpReviewStatisticsController {

    @Resource
    private ErpReviewStatisticsService reviewStatisticsService;

    @GetMapping("/get")
    @Operation(summary = "获得复盘统计")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<ErpReviewStatisticsRespVO> getReviewStatistics(@Valid ErpReviewStatisticsReqVO reqVO) {
        return success(reviewStatisticsService.getReviewStatistics(reqVO));
    }

} 