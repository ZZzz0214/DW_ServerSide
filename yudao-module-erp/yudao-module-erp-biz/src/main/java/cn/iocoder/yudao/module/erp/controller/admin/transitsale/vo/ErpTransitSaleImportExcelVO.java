package cn.iocoder.yudao.module.erp.controller.admin.transitsale.vo;


import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 中转销售导入 Excel VO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false)
public class ErpTransitSaleImportExcelVO {

    @Schema(description = "销售价格表业务编号", example = "XSJGBH20230001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    @ExcelProperty("组品编号")
    private String groupProductNo;


    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品A")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "产品简称", example = "产品A简称")
    @ExcelProperty("产品简称")
    private String productShortName;


    @ExcelProperty(value = "中转人员", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_TRANSIT_PERSON)
    private String transitPerson;

    @Schema(description = "代发单价（单位：元）", example = "100.00")
    @ExcelProperty("代发单价")
    private BigDecimal distributionPrice;

    @Schema(description = "批发单价（单位：元）", example = "80.00")
    @ExcelProperty("批发单价")
    private BigDecimal wholesalePrice;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "运费类型（0：固定运费，1：按件计费，2：按重计费）", example = "0")
    @ExcelProperty("运费类型")
    private Integer shippingFeeType;

    @Schema(description = "固定运费（单位：元）", example = "10.00")
    @ExcelProperty("固定运费")
    private BigDecimal fixedShippingFee;

    @Schema(description = "按件数量", example = "10")
    @ExcelProperty("按件数量")
    private Integer additionalItemQuantity;

    @Schema(description = "按件价格（单位：元）", example = "80.00")
    @ExcelProperty("按件价格")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：g）", example = "1000")
    @ExcelProperty("首重重量（单位：g）")
    private BigDecimal firstWeight;

    @Schema(description = "首重价格（单位：元）", example = "10.00")
    @ExcelProperty("首重价格")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：g）", example = "500")
    @ExcelProperty("续重重量（单位：g）")
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格（单位：元）", example = "5.00")
    @ExcelProperty("续重价格")
    private BigDecimal additionalWeightPrice;

    @Schema(description = "发货编码", example = "SH202503250001")
    @ExcelProperty("发货编码")
    private String shippingCode;
}
