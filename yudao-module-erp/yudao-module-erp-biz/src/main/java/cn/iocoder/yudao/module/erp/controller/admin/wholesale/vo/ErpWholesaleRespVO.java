package cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 批发 Response VO")
@Data
public class ErpWholesaleRespVO {

    // 来自 ErpWholesaleBaseDO
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "WS202403250001")
    private String no;

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
    @Schema(description = "物流单号", example = "LOG202403250001")
    private String logisticsNumber;

    @Schema(description = "售后时间", example = "2025-05-21T12:34:56")
    private LocalDateTime afterSalesTime;

    @Schema(description = "采购备注信息", example = "采购备注示例")
    private String purchaseRemark;

    @Schema(description = "出货备注信息", example = "出货备注示例")
    private String saleRemark;

    // 来自 ErpWholesalePurchaseDO
    @Schema(description = "组品编号", example = "1")
    private Long comboProductId;
    @Schema(description = "组品编号", example = "CP20240001")
    private String comboProductNo;

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

    @Schema(description = "采购售后状态", example = "1")
    private Integer purchaseAfterSalesStatus;

    @Schema(description = "采购售后情况", example = "售后情况说明")
    private String purchaseAfterSalesSituation;

    @Schema(description = "采购售后金额", example = "100.00")
    private BigDecimal purchaseAfterSalesAmount;

    @Schema(description = "采购售后时间", example = "2023-01-01 12:00:00")
    private String purchaseAfterSalesTime;

    @Schema(description = "采购审核状态", example = "1")
    private Integer purchaseAuditStatus;

    @Schema(description = "批发采购审批时间", example = "2023-01-01 12:00:00")
    private LocalDateTime purchaseApprovalTime;

    @Schema(description = "批发采购反审批时间", example = "2023-01-01 12:00:00")
    private LocalDateTime purchaseUnapproveTime;

    // 来自 ErpWholesaleSaleDO
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

    @Schema(description = "销售售后状态", example = "1")
    private Integer saleAfterSalesStatus;

    @Schema(description = "销售售后情况", example = "售后情况说明")
    private String saleAfterSalesSituation;

    @Schema(description = "销售售后金额", example = "100.00")
    private BigDecimal saleAfterSalesAmount;

    @Schema(description = "销售售后时间", example = "2023-01-01 12:00:00")
    private String saleAfterSalesTime;

    @Schema(description = "销售审核状态", example = "1")
    private Integer saleAuditStatus;

    @Schema(description = "批发销售审批时间", example = "2023-01-01 12:00:00")
    private LocalDateTime saleApprovalTime;

    @Schema(description = "批发销售反审批时间", example = "2023-01-01 12:00:00")
    private LocalDateTime saleUnapproveTime;

    @Schema(description = "中转人员", example = "中转员A")
    private String transferPerson;


    // 公共字段
    @Schema(description = "创建人员", requiredMode = Schema.RequiredMode.REQUIRED)
    private String creator;
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "租户编号", example = "1")
    private Long tenantId;

    @Schema(description = "是否删除", example = "false")
    private Boolean deleted;

    @Schema(description = "产品规格", example = "规格B")
    private String productSpecification;

    @Schema(description = "订单号", example = "ORD202403250001")
    private String orderNumber;


    private BigDecimal purchaseAuditTotalAmount;
    private BigDecimal saleAuditTotalAmount;
}
