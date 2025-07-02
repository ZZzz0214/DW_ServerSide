package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.sample.ErpSampleSummaryRespVO;

import java.time.LocalDateTime;

/**
 * ERP 样品统计 Service 接口
 */
public interface ErpSampleStatisticsService {

    /**
     * 获得样品统计
     *
     * @param beginTime    开始时间
     * @param endTime      结束时间
     * @param customerName 客户名称（可选）
     * @return 样品统计
     */
    ErpSampleSummaryRespVO getSampleSummary(LocalDateTime beginTime, LocalDateTime endTime, String customerName);

} 