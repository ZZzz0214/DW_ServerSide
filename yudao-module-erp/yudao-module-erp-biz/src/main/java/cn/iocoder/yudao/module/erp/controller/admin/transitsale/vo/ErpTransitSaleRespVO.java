package cn.iocoder.yudao.module.erp.controller.admin.transitsale.vo;


import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 中转销售 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpTransitSaleRespVO {

    @Schema(description = "中转销售编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "TS001")
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

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long groupProductId;

    @Schema(description = "中转人员", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty(value = "中转人员", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_TRANSIT_PERSON)
    private String transitPerson;

    @Schema(description = "代发单价（单位：元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("代发单价")
    private BigDecimal distributionPrice;

    @Schema(description = "批发单价（单位：元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("批发单价")
    private BigDecimal wholesalePrice;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "运费类型（0：固定运费，1：按件计费，2：按重计费）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("运费类型")
    private Integer shippingFeeType;

    @Schema(description = "固定运费（单位：元）")
    @ExcelProperty("固定运费")
    private BigDecimal fixedShippingFee;

//    @Schema(description = "首件数量")
//    @ExcelProperty("首件数量")
//    private Integer firstItemQuantity;
//
//    @Schema(description = "首件价格（单位：元）")
//    @ExcelProperty("首件价格")
//    private BigDecimal firstItemPrice;
//
//    @Schema(description = "续件数量")
//    @ExcelProperty("续件数量")
//    private Integer additionalItemQuantity;
//
//    @Schema(description = "续件价格（单位：元）")
//    @ExcelProperty("续件价格")
//    private BigDecimal additionalItemPrice;
    @Schema(description = "续件数量", example = "10")
    @ExcelProperty("按件数量")
    private Integer additionalItemQuantity;

    @Schema(description = "续件价格（单位：元）", example = "80.00")
    @ExcelProperty("按件价格")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：kg）")
    @ExcelProperty("首重重量（单位：g）")
    private BigDecimal firstWeight;

    @Schema(description = "首重价格（单位：元）")
    @ExcelProperty("首重价格")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：kg）")
    @ExcelProperty("续重重量（单位：g）")
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格（单位：元）")
    @ExcelProperty("续重价格")
    private BigDecimal additionalWeightPrice;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "关联的组品列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ErpComboRespVO> comboList;
}
