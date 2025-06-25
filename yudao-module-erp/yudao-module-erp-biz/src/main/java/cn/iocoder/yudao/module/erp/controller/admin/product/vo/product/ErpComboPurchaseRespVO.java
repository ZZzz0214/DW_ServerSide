package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import cn.iocoder.yudao.framework.excel.core.convert.BigDecimalToIntegerConvert;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 产品 Response VO")
@Data
@Builder
@ExcelIgnoreUnannotated
@NoArgsConstructor
@AllArgsConstructor
public class ErpComboPurchaseRespVO {
    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15672")
    private Long id;

    @Schema(description = "组合产品编号(业务编号)", example = "CPK20231101000001")
    @ExcelProperty("组品编号")
    private String no;

    @Schema(description = "产品图片")
    @ExcelProperty("产品图片")
    private String image;

    @Schema(description = "单品编号和数量字符串", example = "P001,2;P002,3")
    @ExcelProperty("单品组品(产品编号,产品数量;产品编号,产品数量;)")
    private String itemsString;

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
    @ExcelProperty(value = "产品重量", converter = BigDecimalToIntegerConvert.class)
    private BigDecimal weight;

    @Schema(description = "采购人员")
    @ExcelProperty("采购人员")
    private String purchaser;

    @Schema(description = "供应商名")
    @ExcelProperty("供应商名")
    private String supplier;

    @Schema(description = "采购单价")
    @ExcelProperty("采购单价")
    private BigDecimal purchasePrice;

    @Schema(description = "批发单价")
    @ExcelProperty("批发单价")
    private BigDecimal wholesalePrice;

    @Schema(description = "备注信息")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "运费类型（0：固定运费，1：按件计费，2：按重计费）")
    @ExcelProperty("运费类型(0-固定运费,1-按件运费,2-按重运费)")
    private Integer shippingFeeType;

    @Schema(description = "固定运费")
    @ExcelProperty("固定运费")
    private BigDecimal fixedShippingFee;


    @Schema(description = "续件数量")
    @ExcelProperty("按件数量")
    private Integer additionalItemQuantity;

    @Schema(description = "续件价格")
    @ExcelProperty("按件价格")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：kg）")
    @ExcelProperty(value = "首重重量", converter = BigDecimalToIntegerConvert.class)
    private BigDecimal firstWeight;

    @Schema(description = "首重价格")
    @ExcelProperty("首重价格")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：kg）")
    @ExcelProperty(value = "续重重量", converter = BigDecimalToIntegerConvert.class)
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格")
    @ExcelProperty("续重价格")
    private BigDecimal additionalWeightPrice;


    @Schema(description = "创建者")
    @ExcelProperty("创建人员")
    private String creator;


    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


}
