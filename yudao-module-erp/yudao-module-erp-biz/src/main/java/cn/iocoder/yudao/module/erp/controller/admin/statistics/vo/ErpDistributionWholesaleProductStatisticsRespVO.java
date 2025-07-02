package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 代发批发产品组品统计 Response VO")
@Data
public class ErpDistributionWholesaleProductStatisticsRespVO {

    @Schema(description = "单品统计分页结果")
    private PageResult<SingleProductStatistics> singleProductPageResult;

    @Schema(description = "组品统计分页结果")
    private PageResult<ComboProductStatistics> comboProductPageResult;

    @Schema(description = "代发表单品总数")
    private Integer totalDistributionSingleCount;

    @Schema(description = "批发表单品总数")
    private Integer totalWholesaleSingleCount;

    @Schema(description = "单品总数")
    private Integer totalSingleCount;

    @Schema(description = "代发表组品总数")
    private Integer totalDistributionComboCount;

    @Schema(description = "批发表组品总数")
    private Integer totalWholesaleComboCount;

    @Schema(description = "组品总数")
    private Integer totalComboCount;



    @Schema(description = "单品统计")
    @Data
    public static class SingleProductStatistics {

        @Schema(description = "单品名称")
        private String productName;

        @Schema(description = "单品规格")
        private String productSpecification;

        @Schema(description = "代发数量")
        private Integer distributionCount;

        @Schema(description = "批发数量")
        private Integer wholesaleCount;

        @Schema(description = "总数量")
        private Integer totalCount;

        @Schema(description = "代发占比")
        private BigDecimal distributionPercentage;

        @Schema(description = "批发占比")
        private BigDecimal wholesalePercentage;
    }

    @Schema(description = "组品统计")
    @Data
    public static class ComboProductStatistics {

        @Schema(description = "组品ID")
        private Long comboProductId;

        @Schema(description = "组品名称")
        private String comboProductName;

        @Schema(description = "组品编号")
        private String comboProductNo;

        @Schema(description = "代发组品数量")
        private Integer distributionComboCount;

        @Schema(description = "批发组品数量")
        private Integer wholesaleComboCount;

        @Schema(description = "组品总数量")
        private Integer totalComboCount;

        @Schema(description = "代发占比")
        private BigDecimal distributionPercentage;

        @Schema(description = "批发占比")
        private BigDecimal wholesalePercentage;

        @Schema(description = "组品包含的单品明细")
        private List<ComboProductItemDetail> itemDetails;
    }

    @Schema(description = "组品单品明细")
    @Data
    public static class ComboProductItemDetail {

        @Schema(description = "单品名称")
        private String productName;

        @Schema(description = "单品规格")
        private String productSpecification;

        @Schema(description = "单品数量")
        private Integer itemQuantity;
    }
} 