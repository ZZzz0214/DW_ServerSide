package cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 批发采购审核 VO")
@Data
public class ErpWholesalePurchaseAuditVO {

    @Schema(description = "批发采购审核ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "WS202403250001")
    private String no;

    @Schema(description = "物流单号", example = "LOG202403250001")
    private String logisticsNumber;

    @Schema(description = "收件人姓名", example = "张三")
    private String receiverName;

    @Schema(description = "收件人电话", example = "13800138000")
    private String receiverPhone;

    @Schema(description = "收件地址", example = "北京市朝阳区XX街道XX号")
    private String receiverAddress;

    @Schema(description = "备注信息", example = "备注")
    private String remark;

    @Schema(description = "组品编号", example = "1")
    private Long comboProductId;

    @Schema(description = "发货编码", example = "SH202403250001")
    private String shippingCode;

    @Schema(description = "产品名称", example = "商品A")
    private String productName;

    @Schema(description = "产品规格", example = "规格B")
    private String productSpecification;

    @Schema(description = "产品数量", example = "100")
    private Integer productQuantity;

    @Schema(description = "采购人员", example = "采购员A")
    private String purchaser;

    @Schema(description = "供应商名", example = "供应商A")
    private String supplier;

    @Schema(description = "采购单价", example = "100.00")
    private BigDecimal purchasePrice;

    @Schema(description = "采购货拉拉费", example = "50.00")
    private BigDecimal truckFee;

    @Schema(description = "采购物流费用", example = "30.00")
    private BigDecimal logisticsFee;

    @Schema(description = "采购杂费", example = "20.00")
    private BigDecimal otherFees;

    @Schema(description = "采购总额", example = "10000.00")
    private BigDecimal totalPurchaseAmount;
    @Schema(description = "采购审核状态", example = "1")
    private Integer purchaseAuditStatus;
}
