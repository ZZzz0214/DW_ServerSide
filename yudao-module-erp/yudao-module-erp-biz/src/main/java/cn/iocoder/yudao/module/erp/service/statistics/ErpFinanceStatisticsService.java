package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.finance.ErpFinanceSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.finance.ErpFinanceAmountSummaryRespVO;

import java.time.LocalDateTime;

/**
 * ERP 财务统计 Service 接口
 */
public interface ErpFinanceStatisticsService {

    /**
     * 获得财务表统计
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 财务表统计
     */
    ErpFinanceSummaryRespVO getFinanceSummary(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得财务金额表统计
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 财务金额表统计
     */
    ErpFinanceAmountSummaryRespVO getFinanceAmountSummary(LocalDateTime beginTime, LocalDateTime endTime);

} 