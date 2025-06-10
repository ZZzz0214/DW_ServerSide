package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 组合产品 Response VO")
@Data
@ExcelIgnoreUnannotated
@Builder
public class ErpComboRespVO {

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15672")
    private Long id;

    @Schema(description = "组合产品编号(业务编号)", example = "CPK20231101000001")
    @ExcelProperty("组品编号")
    private String no;

    @Schema(description = "产品图片")
    @ExcelProperty("产品图片")
    private String image;

    @Schema(description = "组合产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("产品名称")
    private String name;

    @Schema(description = "产品简称")
    @ExcelProperty("产品简称")
    private String shortName;


    @Schema(description = "发货编码")
    @ExcelProperty("发货编码")
    private String shippingCode;

    @Schema(description = "产品重量（单位：kg）")
    @ExcelProperty("产品重量（单位：g）")
    private BigDecimal weight;

    @Schema(description = "采购人员")
    @ExcelProperty("采购人员")
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
    @ExcelProperty("运费类型")
    private Integer shippingFeeType;

    @Schema(description = "固定运费（单位：元）")
    @ExcelProperty("固定运费（单位：元）")
    private BigDecimal fixedShippingFee;

//    @Schema(description = "首件数量")
//    @ExcelProperty("首件数量")
//    private Integer firstItemQuantity;
//
//    @Schema(description = "首件价格（单位：元）")
//    @ExcelProperty("首件价格（单位：元）")
//    private BigDecimal firstItemPrice;

    @Schema(description = "续件数量")
    @ExcelProperty("按件数量")
    private Integer additionalItemQuantity;

    @Schema(description = "续件价格（单位：元）")
    @ExcelProperty("按件价格（单位：元）")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：kg）")
    @ExcelProperty("首重重量（单位：g）")
    private BigDecimal firstWeight;

    @Schema(description = "首重价格（单位：元）")
    @ExcelProperty("首重价格（单位：元）")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：kg）")
    @ExcelProperty("续重重量（单位：g）")
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格（单位：元）")
    @ExcelProperty("续重价格（单位：元）")
    private BigDecimal additionalWeightPrice;

    @Schema(description = "产品数量（组合产品中包含的单品总数）")
    private Integer totalQuantity;

    @Schema(description = "创建者")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "更新者")
    private String updater;

    @Schema(description = "是否删除（0：未删除，1：已删除）")
    private Boolean deleted;

    @Schema(description = "租户编号")
    private Long tenantId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "产品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

    @Schema(description = "单品列表")
    private List<ErpProductRespVO> items;


}
