package cn.iocoder.yudao.framework.common.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "批发销售统计信息的分页结果")
@Data
public class WholesaleSalesSummaryPageResult<T> {

    @Schema(description = "分页数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private PageResult<T> pageResult;

    @Schema(description = "出货单价合计", example = "1200.00")
    private BigDecimal totalSalePrice;

    @Schema(description = "出货货拉拉费合计", example = "600.00")
    private BigDecimal totalSaleTruckFee;

    @Schema(description = "出货物流费用合计", example = "400.00")
    private BigDecimal totalSaleLogisticsFee;

    @Schema(description = "出货杂费合计", example = "300.00")
    private BigDecimal totalSaleOtherFees;

    @Schema(description = "出货总额合计", example = "12000.00")
    private BigDecimal totalSaleAmount;

    @Schema(description = "销售售后费用合计", example = "150.00")
    private BigDecimal totalSaleAfterSalesAmount;
    private BigDecimal totalSaleAuditTotalAmount;

}
