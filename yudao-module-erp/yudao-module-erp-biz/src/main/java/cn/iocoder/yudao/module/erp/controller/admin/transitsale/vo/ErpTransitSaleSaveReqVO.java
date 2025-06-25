package cn.iocoder.yudao.module.erp.controller.admin.transitsale.vo;


import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 中转销售新增/修改 Request VO")
@Data
public class ErpTransitSaleSaveReqVO {

    @Schema(description = "中转销售编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "TS001")
    private String no;

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "组品编号不能为空")
    private Long groupProductId;

    @Schema(description = "中转人员", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty(value = "中转人员", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_TRANSIT_PERSON)
    private String transitPerson;

    @Schema(description = "代发单价（单位：元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "代发单价不能为空")
    private BigDecimal distributionPrice;

    @Schema(description = "批发单价（单位：元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "批发单价不能为空")
    private BigDecimal wholesalePrice;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;

    @Schema(description = "运费类型（0：固定运费，1：按件计费，2：按重计费）")
    private Integer shippingFeeType;

    @Schema(description = "固定运费（单位：元）")
    private BigDecimal fixedShippingFee;

    @Schema(description = "首件数量")
    private Integer firstItemQuantity;

    @Schema(description = "首件价格（单位：元）")
    private BigDecimal firstItemPrice;

    @Schema(description = "续件数量")
    private Integer additionalItemQuantity;

    @Schema(description = "续件价格（单位：元）")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：kg）")
    private BigDecimal firstWeight;

    @Schema(description = "首重价格（单位：元）")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：kg）")
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格（单位：元）")
    private BigDecimal additionalWeightPrice;
}
