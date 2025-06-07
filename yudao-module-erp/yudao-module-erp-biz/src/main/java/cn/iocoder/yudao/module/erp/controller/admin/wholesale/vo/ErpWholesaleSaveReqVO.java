package cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 批发新增/修改 Request VO")
@Data
public class ErpWholesaleSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "WS202403250001")
    private String no;
    @Schema(description = "物流单号", example = "LOG202403250001")
    private String logisticsNumber;

    @Schema(description = "收件人姓名", example = "张三")
    private String receiverName;

    @Schema(description = "收件人电话", example = "13800138000")
    private String receiverPhone;

    @Schema(description = "收件地址", example = "北京市朝阳区XX街道XX号")
    private String receiverAddress;

    @Schema(description = "售后状况", example = "0")
    private String afterSalesStatus;

    @Schema(description = "备注信息", example = "备注")
    private String remark;


    @Schema(description = "产品名称", example = "商品A")
    private String productName;

    @Schema(description = "发货编码", example = "SH202403250001")
    private String shippingCode;

    @Schema(description = "产品数量", example = "100")
    private Integer productQuantity;

    // 采购信息
    @Schema(description = "组品编号", example = "1")
    private Long comboProductId;

    @Schema(description = "采购人员", example = "采购员A")
    private String purchaser;

    @Schema(description = "供应商名", example = "供应商A")
    private String supplier;

    @Schema(description = "采购单价", example = "100.00")
    private BigDecimal purchasePrice;

    @Schema(description = "货拉拉费", example = "50.00")
    private BigDecimal truckFee;

    @Schema(description = "物流费用", example = "30.00")
    private BigDecimal logisticsFee;

    @Schema(description = "其他费用", example = "20.00")
    private BigDecimal otherFees;

    @Schema(description = "采购总额", example = "10000.00")
    private BigDecimal totalPurchaseAmount;

    @Schema(description = "售后时间", example = "2025-05-21T12:34:56")
    private String afterSalesTime;

    @Schema(description = "采购备注信息", example = "采购备注示例")
    private String purchaseRemark;

    @Schema(description = "出货备注信息", example = "出货备注示例")
    private String saleRemark;

    // 销售信息
//    @Schema(description = "关联销售价格表", example = "1")
//    private Long salePriceId;

    @Schema(description = "销售人员", example = "销售员A")
    private String salesperson;

    @Schema(description = "客户名称", example = "客户A")
    private String customerName;

    @Schema(description = "出货单价", example = "120.00")
    private BigDecimal salePrice;

    @Schema(description = "货拉拉费", example = "60.00")
    private BigDecimal saleTruckFee;

    @Schema(description = "物流费用", example = "40.00")
    private BigDecimal saleLogisticsFee;

    @Schema(description = "其他费用", example = "30.00")
    private BigDecimal saleOtherFees;

    @Schema(description = "出货总额", example = "12000.00")
    private BigDecimal totalSaleAmount;

    @Schema(description = "租户编号", example = "1")
    private Long tenantId;

    @Schema(description = "产品规格", example = "规格B")
    private String productSpecification;

    @Schema(description = "订单号", example = "ORD202403250001")
    private String orderNumber;

    @Schema(description = "中转人员", example = "中转员A")
    private String transferPerson;

}
