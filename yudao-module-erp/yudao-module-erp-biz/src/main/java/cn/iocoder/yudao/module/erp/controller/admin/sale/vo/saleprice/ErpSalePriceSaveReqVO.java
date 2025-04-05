package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 销售价格新增/修改 Request VO")
@Data
public class ErpSalePriceSaveReqVO {

    @Schema(description = "编号", example = "17386")
    private Long id;

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    @NotNull(message = "组品编号不能为空")
    private Long groupProductId;

    @Schema(description = "客户名称", example = "客户A")
    private String customerName;

    @Schema(description = "代发单价（单位：元）", example = "100.00")
    private BigDecimal distributionPrice;

    @Schema(description = "批发单价（单位：元）", example = "80.00")
    private BigDecimal wholesalePrice;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;

    @Schema(description = "运费类型（0：固定运费，1：按件计费，2：按重计费）", example = "0")
    private Integer shippingFeeType;

    @Schema(description = "固定运费（单位：元）", example = "10.00")
    private BigDecimal fixedShippingFee;

    @Schema(description = "首件数量", example = "1")
    private Integer firstItemQuantity;

    @Schema(description = "首件价格（单位：元）", example = "100.00")
    private BigDecimal firstItemPrice;

    @Schema(description = "续件数量", example = "10")
    private Integer additionalItemQuantity;

    @Schema(description = "续件价格（单位：元）", example = "80.00")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：kg）", example = "1.00")
    private BigDecimal firstWeight;

    @Schema(description = "首重价格（单位：元）", example = "10.00")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：kg）", example = "0.50")
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格（单位：元）", example = "5.00")
    private BigDecimal additionalWeightPrice;

    @Schema(description = "租户编号", example = "1")
    private Long tenantId;
}
