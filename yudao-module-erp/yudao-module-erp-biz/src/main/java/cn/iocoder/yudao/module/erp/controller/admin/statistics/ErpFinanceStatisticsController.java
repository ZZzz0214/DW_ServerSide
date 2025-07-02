package cn.iocoder.yudao.module.erp.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.finance.ErpFinanceSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.finance.ErpFinanceAmountSummaryRespVO;
import cn.iocoder.yudao.module.erp.service.statistics.ErpFinanceStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 财务统计")
@RestController
@RequestMapping("/erp/finance-statistics")
@Validated
@Slf4j
public class ErpFinanceStatisticsController {

    @Resource
    private ErpFinanceStatisticsService financeStatisticsService;

    @GetMapping("/finance-summary")
    @Operation(summary = "获得财务表统计")
    @Parameter(name = "beginDate", description = "开始日期", example = "2024-01-01")
    @Parameter(name = "endDate", description = "结束日期", example = "2024-01-31")
    //@PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<ErpFinanceSummaryRespVO> getFinanceSummary(
            @RequestParam(value = "beginDate") String beginDate,
            @RequestParam(value = "endDate") String endDate) {

        try {
            log.info("获取财务表统计，开始日期：{}，结束日期：{}", beginDate, endDate);

            LocalDateTime beginTime = LocalDateTime.of(LocalDate.parse(beginDate), LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX);

            log.info("转换后的时间范围：{} 到 {}", beginTime, endTime);

            ErpFinanceSummaryRespVO summary = financeStatisticsService.getFinanceSummary(beginTime, endTime);
            log.info("财务表统计获取成功：{}", summary);

            return success(summary);
        } catch (Exception e) {
            log.error("获取财务表统计失败", e);
            throw e;
        }
    }

    @GetMapping("/finance-amount-summary")
    @Operation(summary = "获得财务金额表统计")
    @Parameter(name = "beginDate", description = "开始日期", example = "2024-01-01")
    @Parameter(name = "endDate", description = "结束日期", example = "2024-01-31")
    //@PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<ErpFinanceAmountSummaryRespVO> getFinanceAmountSummary(
            @RequestParam(value = "beginDate") String beginDate,
            @RequestParam(value = "endDate") String endDate) {

        try {
            log.info("获取财务金额表统计，开始日期：{}，结束日期：{}", beginDate, endDate);

            LocalDateTime beginTime = LocalDateTime.of(LocalDate.parse(beginDate), LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX);

            log.info("转换后的时间范围：{} 到 {}", beginTime, endTime);

            ErpFinanceAmountSummaryRespVO summary = financeStatisticsService.getFinanceAmountSummary(beginTime, endTime);
            log.info("财务金额表统计获取成功：{}", summary);

            return success(summary);
        } catch (Exception e) {
            log.error("获取财务金额表统计失败", e);
            throw e;
        }
    }

}
