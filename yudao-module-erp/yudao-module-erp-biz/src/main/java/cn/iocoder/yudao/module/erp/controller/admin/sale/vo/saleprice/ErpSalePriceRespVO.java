package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice;

import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 销售价格 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpSalePriceRespVO {

    @Schema(description = "销售价格表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17386")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "销售价格表业务编号", example = "XSJGBH20230001")
    @ExcelProperty("业务编号")
    private String no;

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    @ExcelProperty("组品编号")
    private Long groupProductId;

    @Schema(description = "关联的组品列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ErpComboRespVO> comboList;


    @Schema(description = "产品图片", example = "https://example.com/image.jpg")
    @ExcelProperty("产品图片")
    private String productImage;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品A")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "产品简称", example = "产品A简称")
    @ExcelProperty("产品简称")
    private String productShortName;

    @Schema(description = "客户名称", example = "客户A")
    @ExcelProperty("客户名称")
    private String customerName;

    @Schema(description = "代发单价（单位：元）", example = "100.00")
    @ExcelProperty("代发单价")
    private BigDecimal distributionPrice;

    @Schema(description = "批发单价（单位：元）", example = "80.00")
    @ExcelProperty("批发单价")
    private BigDecimal wholesalePrice;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "运费类型（0：固定运费，1：按件计费，2：按重计费）", example = "0")
    @ExcelProperty("运费类型")
    private Integer shippingFeeType;

    @Schema(description = "固定运费（单位：元）", example = "10.00")
    @ExcelProperty("固定运费")
    private BigDecimal fixedShippingFee;

    @Schema(description = "首件数量", example = "1")
    @ExcelProperty("首件数量")
    private Integer firstItemQuantity;

    @Schema(description = "首件价格（单位：元）", example = "100.00")
    @ExcelProperty("首件价格")
    private BigDecimal firstItemPrice;

    @Schema(description = "续件数量", example = "10")
    @ExcelProperty("续件数量")
    private Integer additionalItemQuantity;

    @Schema(description = "续件价格（单位：元）", example = "80.00")
    @ExcelProperty("续件价格")
    private BigDecimal additionalItemPrice;

    @Schema(description = "首重重量（单位：kg）", example = "1.00")
    @ExcelProperty("首重重量")
    private BigDecimal firstWeight;

    @Schema(description = "首重价格（单位：元）", example = "10.00")
    @ExcelProperty("首重价格")
    private BigDecimal firstWeightPrice;

    @Schema(description = "续重重量（单位：kg）", example = "0.50")
    @ExcelProperty("续重重量")
    private BigDecimal additionalWeight;

    @Schema(description = "续重价格（单位：元）", example = "5.00")
    @ExcelProperty("续重价格")
    private BigDecimal additionalWeightPrice;

    @Schema(description = "租户编号", example = "1")
    @ExcelProperty("租户编号")
    private Long tenantId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    //新增字段
    @Schema(description = "原表数量", example = "100")
    private Integer originalQuantity;

    @Schema(description = "发货编码", example = "SH202503250001")
    private String shippingCode;
}
