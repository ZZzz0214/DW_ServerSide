package cn.iocoder.yudao.module.erp.controller.admin.product.vo.price;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * ERP 销售价格 保存 Request VO
 */
@Data
public class ErpSalesPriceSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "产品类型：0-单品，1-组品", required = true, example = "0")
    @NotNull(message = "产品类型不能为空")
    private Integer type;

    @Schema(description = "单品编号", example = "1")
    private Long productId;

    @Schema(description = "组品编号", example = "2")
    private Long comboProductId;

    @Schema(description = "客户名称", example = "测试客户")
    private String customerName;

    @Schema(description = "代发单价（单位：元）", example = "10.00")
    private BigDecimal agentPrice;

    @Schema(description = "批发单价（单位：元）", example = "8.00")
    private BigDecimal wholesalePrice;

    @Schema(description = "备注信息", example = "测试备注")
    private String remark;

    @Schema(description = "运费类型：0-固定运费，1-按件计费，2-按重计费", example = "0")
    private Integer shippingFeeType;

    @Schema(description = "固定运费（单位：元）", example = "5.00")
    private BigDecimal fixedShippingFee;

    @Schema(description = "首件数量", example = "1")
    private Integer firstItemQuantity;

    @Schema(description = "首件价格（单位：元）", example = "10.00")
    private BigDecimal firstItemPrice;

    @Schema(description = "续件数量", example = "2")
    private Integer additionalItemQuantity;

    @Schema(description = "续件价格（单位：元）", example = "8.00")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：kg）", example = "1.00")
    private BigDecimal firstWeight;

    @Schema(description = "首重价格（单位：元）", example = "10.00")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：kg）", example = "0.50")
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格（单位：元）", example = "5.00")
    private BigDecimal additionalWeightPrice;

}