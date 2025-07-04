package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice;

import cn.iocoder.yudao.framework.excel.core.convert.BigDecimalToIntegerConvert;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.BooleanEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 销售价格 Response VO")
//@Builder
@Data
@ExcelIgnoreUnannotated
public class ErpSalePriceRespVO {

    @Schema(description = "销售价格表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17386")
    private Long id;

    @Schema(description = "销售价格表业务编号", example = "XSJGBH20230001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "组品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    private Long groupProductId;

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    @ExcelProperty("组品编号")
    private String groupProductNo;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品A")
    @ExcelProperty("产品名称")
    @HeadStyle(fillForegroundColor = 10) // 10是红色背景
    @HeadFontStyle(fontHeightInPoints = 11, bold = BooleanEnum.TRUE, color = 8) // 8是黑色字体
    private String productName;

    @Schema(description = "产品简称", example = "产品A简称")
    @ExcelProperty("产品简称")
    @HeadStyle(fillForegroundColor = 10) // 10是红色背景
    @HeadFontStyle(fontHeightInPoints = 11, bold = BooleanEnum.TRUE, color = 8) // 8是黑色字体
    private String productShortName;

    @Schema(description = "客户名称", example = "客户A")
    @ExcelProperty("客户名称")
    @HeadFontStyle(fontHeightInPoints = 11, bold = BooleanEnum.TRUE, color = 8) // 8是黑色，阻止红色继承
    private String customerName;

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
    @ExcelProperty("运费类型(0-固定运费,1-按件运费,2-按重运费)")
    private Integer shippingFeeType;

    @Schema(description = "固定运费（单位：元）", example = "10.00")
    @ExcelProperty("固定运费")
    private BigDecimal fixedShippingFee;

    @Schema(description = "续件数量", example = "10")
    @ExcelProperty("按件数量")
    private Integer additionalItemQuantity;

    @Schema(description = "续件价格（单位：元）", example = "80.00")
    @ExcelProperty("按件价格")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：kg）", example = "1.00")
    @ExcelProperty(value = "首重重量", converter = BigDecimalToIntegerConvert.class)
    private BigDecimal firstWeight;

    @Schema(description = "首重价格（单位：元）", example = "10.00")
    @ExcelProperty("首重价格")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：kg）", example = "0.50")
    @ExcelProperty(value = "续重重量", converter = BigDecimalToIntegerConvert.class)
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格（单位：元）", example = "5.00")
    @ExcelProperty("续重价格")
    private BigDecimal additionalWeightPrice;

    @Schema(description = "创建人员", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建人员")
    @HeadStyle(fillForegroundColor = 10) // 10是红色背景
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @HeadStyle(fillForegroundColor = 10) // 10是红色背景
    private LocalDateTime createTime;

    @Schema(description = "关联的组品列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ErpComboRespVO> comboList;

    @Schema(description = "产品图片", example = "https://example.com/image.jpg")
    private String productImage;

    @Schema(description = "租户编号", example = "1")
    private Long tenantId;

    @Schema(description = "原表数量", example = "100")
    private Integer originalQuantity;

    @Schema(description = "发货编码", example = "SH202503250001")
    private String shippingCode;

    @Schema(description = "产品重量（单位：kg）")
    private BigDecimal weight;
}
