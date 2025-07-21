package cn.iocoder.yudao.module.erp.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.sample.ErpSampleSummaryRespVO;
import cn.iocoder.yudao.module.erp.service.statistics.ErpSampleStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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

@Tag(name = "管理后台 - ERP 样品统计")
@RestController
@RequestMapping("/erp/sample-statistics")
@Validated
@Slf4j
public class ErpSampleStatisticsController {

    @Resource
    private ErpSampleStatisticsService sampleStatisticsService;

    @GetMapping("/sample-summary")
    @Operation(summary = "获得样品统计")
    @Parameter(name = "beginDate", description = "开始日期", example = "2024-01-01")
    @Parameter(name = "endDate", description = "结束日期", example = "2024-01-31")
    @Parameter(name = "customerName", description = "客户名称", example = "客户A")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "每页条数", example = "10")
    public CommonResult<ErpSampleSummaryRespVO> getSampleSummary(
            @RequestParam(value = "beginDate") String beginDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {

        try {
            log.info("获取样品统计，开始日期：{}，结束日期：{}，页码：{}，每页条数：{}", 
                    beginDate, endDate, pageNo, pageSize);

            LocalDateTime beginTime = LocalDateTime.of(LocalDate.parse(beginDate), LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX);

            log.info("转换后的时间范围：{} 到 {}", beginTime, endTime);

            ErpSampleSummaryRespVO summary = sampleStatisticsService.getSampleSummary(beginTime, endTime, customerName, pageNo, pageSize);
            log.info("样品统计获取成功：{}", summary);

            return success(summary);
        } catch (Exception e) {
            log.error("获取样品统计失败", e);
            throw e;
        }
    }

} 