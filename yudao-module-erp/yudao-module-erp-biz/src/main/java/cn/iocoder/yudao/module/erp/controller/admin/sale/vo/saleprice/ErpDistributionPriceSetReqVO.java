package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 代发价格设置 Request VO")
@Data
public class ErpDistributionPriceSetReqVO {

    @Schema(description = "订单ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "订单ID列表不能为空")
    private List<Long> orderIds;

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "组品编号不能为空")
    private Long groupProductId;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "客户A")
    @NotNull(message = "客户名称不能为空")
    private String customerName;

    @Schema(description = "代发单价", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "代发单价不能为空")
    @Positive(message = "代发单价必须大于0")
    private BigDecimal distributionPrice;
} 