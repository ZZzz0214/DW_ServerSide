package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleStatisticsRespVO;

import java.util.List;

/**
 * ERP 代发批发统计 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpDistributionWholesaleStatisticsService {

    /**
     * 获取代发批发统计数据
     *
     * @param reqVO 统计请求参数
     * @return 统计结果
     */
    ErpDistributionWholesaleStatisticsRespVO getDistributionWholesaleStatistics(ErpDistributionWholesaleStatisticsReqVO reqVO);

    /**
     * 获得统计分类列表
     *
     * @param statisticsType 统计类型
     * @param keyword 搜索关键词
     * @return 分类列表
     */
    List<String> getCategoryList(String statisticsType, String keyword);

    /**
     * 获得人员详细统计
     *
     * @param reqVO 查询条件
     * @param categoryName 分类名称
     * @return 详细统计数据
     */
    ErpDistributionWholesaleStatisticsRespVO.DetailStatistics getDetailStatistics(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName);

    /**
     * 获取代发批发审核数量统计数据
     *
     * @param reqVO 统计请求参数
     * @return 审核数量统计结果
     */
    ErpDistributionWholesaleStatisticsRespVO.AuditStatistics getAuditStatistics(ErpDistributionWholesaleStatisticsReqVO reqVO);

} 