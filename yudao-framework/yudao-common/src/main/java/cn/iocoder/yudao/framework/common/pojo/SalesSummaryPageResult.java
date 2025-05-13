package cn.iocoder.yudao.framework.common.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
@Schema(description = "带统计信息的分页结果")
@Data
public class SalesSummaryPageResult<T> {

    @Schema(description = "分页数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private PageResult<T> pageResult;

    @Schema(description = "出货单价合计", example = "1200.00")
    private BigDecimal totalSalePrice;

    @Schema(description = "出货运费合计", example = "600.00")
    private BigDecimal totalSaleShippingFee;

    @Schema(description = "出货杂费合计", example = "300.00")
    private BigDecimal totalSaleOtherFees;

    @Schema(description = "出货总额合计", example = "120000.00")
    private BigDecimal totalSaleAmount;
}