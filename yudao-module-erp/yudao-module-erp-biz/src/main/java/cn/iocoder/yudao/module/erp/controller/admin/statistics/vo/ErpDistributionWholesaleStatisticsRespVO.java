package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 代发批发统计响应 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 代发批发统计响应 VO")
@Data
public class ErpDistributionWholesaleStatisticsRespVO {

    @Schema(description = "统计类型", example = "purchaser")
    private String statisticsType;

    @Schema(description = "统计数据列表")
    private List<StatisticsItem> items;

    @Schema(description = "统计项")
    @Data
    public static class StatisticsItem {
        
        @Schema(description = "分类名称", example = "张三")
        private String categoryName;
        
        @Schema(description = "代发订单数", example = "10")
        private Integer distributionOrderCount;
        
        @Schema(description = "代发产品数量", example = "100")
        private Integer distributionProductQuantity;
        
        @Schema(description = "代发采购总额", example = "1000.00")
        private BigDecimal distributionPurchaseAmount;
        
        @Schema(description = "代发销售总额", example = "1200.00")
        private BigDecimal distributionSaleAmount;
        
        @Schema(description = "批发订单数", example = "5")
        private Integer wholesaleOrderCount;
        
        @Schema(description = "批发产品数量", example = "50")
        private Integer wholesaleProductQuantity;
        
        @Schema(description = "批发采购总额", example = "500.00")
        private BigDecimal wholesalePurchaseAmount;
        
        @Schema(description = "批发销售总额", example = "600.00")
        private BigDecimal wholesaleSaleAmount;
        
        @Schema(description = "总订单数", example = "15")
        private Integer totalOrderCount;
        
        @Schema(description = "总产品数量", example = "150")
        private Integer totalProductQuantity;
        
        @Schema(description = "总采购金额", example = "1500.00")
        private BigDecimal totalPurchaseAmount;
        
        @Schema(description = "总销售金额", example = "1800.00")
        private BigDecimal totalSaleAmount;
    }

    /**
     * 详细统计数据
     */
    @Data
    public static class DetailStatistics {
        @Schema(description = "分类名称", example = "张三")
        private String categoryName;

        @Schema(description = "统计类型", example = "purchaser")
        private String statisticsType;

        @Schema(description = "基础统计信息")
        private StatisticsItem basicInfo;

        @Schema(description = "月度趋势数据")
        private List<MonthlyTrend> monthlyTrends;

        @Schema(description = "产品分布数据")
        private List<ProductDistribution> productDistributions;

        @Schema(description = "利润分析")
        private ProfitAnalysis profitAnalysis;

        @Schema(description = "订单明细（最近10条）")
        private List<OrderDetail> recentOrders;
    }

    /**
     * 月度趋势
     */
    @Data
    public static class MonthlyTrend {
        @Schema(description = "月份", example = "2024-01")
        private String month;

        @Schema(description = "代发订单数", example = "10")
        private Integer distributionOrderCount = 0;

        @Schema(description = "批发订单数", example = "5")
        private Integer wholesaleOrderCount = 0;

        @Schema(description = "代发金额", example = "1000.00")
        private BigDecimal distributionAmount = BigDecimal.ZERO;

        @Schema(description = "批发金额", example = "500.00")
        private BigDecimal wholesaleAmount = BigDecimal.ZERO;
    }

    /**
     * 产品分布
     */
    @Data
    public static class ProductDistribution {
        @Schema(description = "产品名称", example = "商品A")
        private String productName;

        @Schema(description = "订单数量", example = "5")
        private Integer orderCount = 0;

        @Schema(description = "产品数量", example = "50")
        private Integer productQuantity = 0;

        @Schema(description = "销售金额", example = "1000.00")
        private BigDecimal saleAmount = BigDecimal.ZERO;
    }

    /**
     * 利润分析
     */
    @Data
    public static class ProfitAnalysis {
        @Schema(description = "总采购成本", example = "1500.00")
        private BigDecimal totalPurchaseCost = BigDecimal.ZERO;

        @Schema(description = "总销售收入", example = "1800.00")
        private BigDecimal totalSaleRevenue = BigDecimal.ZERO;

        @Schema(description = "总利润", example = "300.00")
        private BigDecimal totalProfit = BigDecimal.ZERO;

        @Schema(description = "利润率", example = "20.00")
        private BigDecimal profitRate = BigDecimal.ZERO;

        @Schema(description = "代发利润", example = "200.00")
        private BigDecimal distributionProfit = BigDecimal.ZERO;

        @Schema(description = "批发利润", example = "100.00")
        private BigDecimal wholesaleProfit = BigDecimal.ZERO;
    }

    /**
     * 订单明细
     */
    @Data
    public static class OrderDetail {
        @Schema(description = "订单号", example = "DFJL20240101001")
        private String orderNo;

        @Schema(description = "订单类型", example = "代发")
        private String orderType;

        @Schema(description = "产品名称", example = "商品A")
        private String productName;

        @Schema(description = "数量", example = "10")
        private Integer quantity;

        @Schema(description = "采购金额", example = "100.00")
        private BigDecimal purchaseAmount = BigDecimal.ZERO;

        @Schema(description = "销售金额", example = "120.00")
        private BigDecimal saleAmount = BigDecimal.ZERO;

        @Schema(description = "创建时间")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime createTime;
    }

} 