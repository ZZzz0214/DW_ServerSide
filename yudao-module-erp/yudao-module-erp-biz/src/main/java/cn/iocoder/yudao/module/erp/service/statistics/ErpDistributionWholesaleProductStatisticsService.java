package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleProductStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleProductStatisticsRespVO;

/**
 * ERP 代发批发产品组品统计 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpDistributionWholesaleProductStatisticsService {

    /**
     * 获得代发批发产品组品统计
     *
     * @param reqVO 查询条件
     * @return 统计结果
     */
    ErpDistributionWholesaleProductStatisticsRespVO getDistributionWholesaleProductStatistics(ErpDistributionWholesaleProductStatisticsReqVO reqVO);

    /**
     * 获得代发批发产品组品统计（分页）
     *
     * @param reqVO 查询条件
     * @return 统计结果
     */
    ErpDistributionWholesaleProductStatisticsRespVO getDistributionWholesaleProductStatisticsPage(ErpDistributionWholesaleProductStatisticsReqVO reqVO);

} 