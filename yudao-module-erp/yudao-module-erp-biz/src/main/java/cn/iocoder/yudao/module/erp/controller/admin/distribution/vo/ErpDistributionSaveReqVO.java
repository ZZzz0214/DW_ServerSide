package cn.iocoder.yudao.module.erp.controller.admin.distribution.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 代发新增/修改 Request VO")
@Data
public class ErpDistributionSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "DF202403250001")
    @NotNull(message = "订单号不能为空")
    private String no;

    @Schema(description = "物流公司", example = "顺丰速运")
    private String logisticsCompany;

    @Schema(description = "物流单号", example = "SF1234567890")
    private String trackingNumber;

    @Schema(description = "收件人姓名", example = "张三")
    private String receiverName;

    @Schema(description = "收件人电话", example = "13800138000")
    private String receiverPhone;

    @Schema(description = "收件地址", example = "北京市朝阳区XX街道XX号")
    private String receiverAddress;

    @Schema(description = "原表商品名称", example = "商品A")
    private String originalProductName;

    @Schema(description = "原表规格", example = "规格A")
    private String originalStandard;

    @Schema(description = "原表数量", example = "100")
    private Integer originalQuantity;

    @Schema(description = "产品名称", example = "商品B")
    private String productName;

    @Schema(description = "产品数量", example = "100")
    private Integer productQuantity;

    // 采购信息
    @Schema(description = "采购人员", example = "采购员A")
    private String purchaser;

    @Schema(description = "供应商名", example = "供应商A")
    private String supplier;

    @Schema(description = "采购单价", example = "100.00")
    private BigDecimal purchasePrice;

    @Schema(description = "采购运费", example = "10.00")
    private BigDecimal shippingFee;

    @Schema(description = "采购其他费用", example = "5.00")
    private BigDecimal otherFees;

    @Schema(description = "采购总额", example = "10000.00")
    private BigDecimal totalPurchaseAmount;

    @Schema(description = "关联组品表", example = "1")
    private Long comboProductId;

    // 销售信息
    @Schema(description = "销售人员", example = "销售员A")
    private String salesperson;

    @Schema(description = "客户名称", example = "客户A")
    private String customerName;

    @Schema(description = "销售单价", example = "120.00")
    private BigDecimal salePrice;

    @Schema(description = "出货运费", example = "15.00")
    private BigDecimal saleShippingFee;

    @Schema(description = "销售其他费用", example = "8.00")
    private BigDecimal saleOtherFees;

    @Schema(description = "销售总额", example = "12000.00")
    private BigDecimal totalSaleAmount;

    @Schema(description = "关联销售价格表", example = "1")
    private Long salePriceId;

    @Schema(description = "租户编号", example = "1")
    private Long tenantId;
}
