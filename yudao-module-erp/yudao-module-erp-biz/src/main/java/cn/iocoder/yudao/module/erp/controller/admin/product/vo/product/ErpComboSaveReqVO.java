package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 组合产品新增/修改 Request VO")
@Data
public class ErpComboSaveReqVO {

    @Schema(description = "组品编号", example = "15672")
    private Long id;

    @Schema(description = "组合产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String name;

    @Schema(description = "产品简称")
    private String shortName;

    @Schema(description = "产品图片")
    private String image;

    @Schema(description = "发货编码")
    private String shippingCode;

    @Schema(description = "产品重量（单位：kg）")
    private BigDecimal weight;

    @Schema(description = "采购人员")
    private String purchaser;

    @Schema(description = "供应商名")
    private String supplier;

    @Schema(description = "采购单价（单位：元）")
    private BigDecimal purchasePrice;

    @Schema(description = "批发单价（单位：元）")
    private BigDecimal wholesalePrice;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "运费类型（0：固定运费，1：按件计费，2：按重计费）")
    private Integer shippingFeeType;

    @Schema(description = "固定运费（单位：元）")
    private BigDecimal fixedShippingFee;

//    @Schema(description = "首件数量")
//    private Integer firstItemQuantity;
//
//    @Schema(description = "首件价格（单位：元）")
//    private BigDecimal firstItemPrice;

    @Schema(description = "按件数量")
    private Integer additionalItemQuantity;

    @Schema(description = "按件价格（单位：元）")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：kg）")
    private BigDecimal firstWeight;

    @Schema(description = "首重价格（单位：元）")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：kg）")
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格（单位：元）")
    private BigDecimal additionalWeightPrice;

    @Schema(description = "产品数量（组合产品中包含的单品总数）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer totalQuantity;

    @Schema(description = "产品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

//    @Schema(description = "关联的单品列表", requiredMode = Schema.RequiredMode.REQUIRED)
//    @NotEmpty(message = "至少需要关联一个单品")
//    private List<ComboItem> items;
//
//    @Data
//    public static class ComboItem {
//        @Schema(description = "单品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
//        @NotNull(message = "单品编号不能为空")
//        private Long productId;
//
//        @Schema(description = "单品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
//        @NotNull(message = "单品数量不能为空")
//        private Integer quantity;
//
//        // 确保包含 itemQuantity 字段
//        private Integer itemQuantity;
//    }

    @Schema(description = "关联的单品列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单品不为空")
    private List<ErpProductRespVO> items;

}
