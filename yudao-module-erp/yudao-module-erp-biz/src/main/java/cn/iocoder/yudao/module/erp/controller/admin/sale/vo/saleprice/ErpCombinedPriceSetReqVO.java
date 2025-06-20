package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 统一价格设置 Request VO")
@Data
public class ErpCombinedPriceSetReqVO {

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "组品编号不能为空")
    private Long groupProductId;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "客户A")
    @NotNull(message = "客户名称不能为空")
    private String customerName;

    @Schema(description = "代发单价", example = "100.00")
    private BigDecimal distributionPrice;

    @Schema(description = "批发单价", example = "100.00")
    private BigDecimal wholesalePrice;

    @Schema(description = "运费类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "运费类型不能为空")
    private Integer shippingFeeType;

    @Schema(description = "固定运费", example = "10.00")
    private BigDecimal fixedShippingFee;

    @Schema(description = "续件数量", example = "1")
    private Integer additionalItemQuantity;

    @Schema(description = "续件价格", example = "5.00")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量", example = "1.0")
    private BigDecimal firstWeight;

    @Schema(description = "首重价格", example = "10.00")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量", example = "1.0")
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格", example = "5.00")
    private BigDecimal additionalWeightPrice;
} 