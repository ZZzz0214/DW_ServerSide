package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.review.ErpReviewStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.review.ErpReviewStatisticsRespVO;

/**
 * ERP 复盘统计 Service 接口
 */
public interface ErpReviewStatisticsService {

    /**
     * 获取复盘统计
     *
     * @param reqVO 查询条件
     * @return 复盘统计数据
     */
    ErpReviewStatisticsRespVO getReviewStatistics(ErpReviewStatisticsReqVO reqVO);
} 