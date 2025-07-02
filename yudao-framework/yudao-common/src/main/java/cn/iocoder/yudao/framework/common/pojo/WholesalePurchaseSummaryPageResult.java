package cn.iocoder.yudao.framework.common.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "批发采购统计信息的分页结果")
@Data
public class WholesalePurchaseSummaryPageResult<T> {

    @Schema(description = "分页数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private PageResult<T> pageResult;

    @Schema(description = "采购单价合计", example = "1000.00")
    private BigDecimal totalPurchasePrice;

    @Schema(description = "采购货拉拉费合计", example = "500.00")
    private BigDecimal totalTruckFee;

    @Schema(description = "采购物流费用合计", example = "300.00")
    private BigDecimal totalLogisticsFee;

    @Schema(description = "采购杂费合计", example = "200.00")
    private BigDecimal totalOtherFees;

    @Schema(description = "采购总额合计", example = "10000.00")
    private BigDecimal totalPurchaseAmount;

    @Schema(description = "采购售后费用合计", example = "100.00")
    private BigDecimal totalPurchaseAfterSalesAmount;
    private BigDecimal totalPurchaseAuditTotalAmount;
}
