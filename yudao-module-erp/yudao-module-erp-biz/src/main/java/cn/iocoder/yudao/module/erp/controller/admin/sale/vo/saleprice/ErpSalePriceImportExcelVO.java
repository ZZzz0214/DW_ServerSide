package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice;


import cn.iocoder.yudao.framework.excel.core.convert.BigDecimalConvert;
import cn.iocoder.yudao.framework.excel.core.convert.IntegerConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 销售价格导入 Excel VO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpSalePriceImportExcelVO {

    @Schema(description = "销售价格表业务编号", example = "XSJGBH20230001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    @ExcelProperty("组品编号")
    private String groupProductNo;


    @Schema(description = "客户名称", example = "客户A")
    @ExcelProperty("客户名称")
    private String customerName;

    @Schema(description = "代发单价（单位：元）", example = "100.00")
    @ExcelProperty(value = "代发单价", converter = BigDecimalConvert.class)
    private BigDecimal distributionPrice;

    @Schema(description = "批发单价（单位：元）", example = "80.00")
    @ExcelProperty(value = "批发单价", converter = BigDecimalConvert.class)
    private BigDecimal wholesalePrice;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty(value = "运费类型(0-固定运费,1-按件运费,2-按重运费)", converter = IntegerConvert.class)
    private Integer shippingFeeType;

    @ExcelProperty(value = "固定运费", converter = BigDecimalConvert.class)
    private BigDecimal fixedShippingFee;

    @ExcelProperty(value = "按件数量", converter = IntegerConvert.class)
    private Integer additionalItemQuantity;

    @ExcelProperty(value = "按件价格", converter = BigDecimalConvert.class)
    private BigDecimal additionalItemPrice;

    @ExcelProperty(value = "首重重量", converter = BigDecimalConvert.class)
    private BigDecimal firstWeight;

    @ExcelProperty(value = "首重价格", converter = BigDecimalConvert.class)
    private BigDecimal firstWeightPrice;

    @ExcelProperty(value = "续重重量", converter = BigDecimalConvert.class)
    private BigDecimal additionalWeight;

    @ExcelProperty(value = "续重价格", converter = BigDecimalConvert.class)
    private BigDecimal additionalWeightPrice;

}
