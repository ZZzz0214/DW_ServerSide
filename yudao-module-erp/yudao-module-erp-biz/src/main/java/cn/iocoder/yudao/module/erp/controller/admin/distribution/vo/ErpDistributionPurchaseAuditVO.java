package cn.iocoder.yudao.module.erp.controller.admin.distribution.vo;


import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 代发采购审核 VO")
@Data
public class ErpDistributionPurchaseAuditVO {

    @Schema(description = "代发采购审核ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "DF202403250001")
    private String no;

    @Schema(description = "订单号", example = "ORD202403250001")
    private String orderNumber;

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

    @Schema(description = "备注信息", example = "备注")
    private String remark;

    @Schema(description = "售后时间", example = "2025-05-21T12:34:56")
    private LocalDateTime afterSalesTime;

    @Schema(description = "采购备注信息", example = "采购备注示例")
    private String purchaseRemark;

    @Schema(description = "出货备注信息", example = "出货备注示例")
    private String saleRemark;

    @Schema(description = "组品编号", example = "1")
    private Long comboProductId;

    @Schema(description = "发货编码", example = "SH202403250001")
    private String shippingCode;

    @Schema(description = "产品名称", example = "商品B")
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

    @Schema(description = "采购运费", example = "50.00")
    private BigDecimal shippingFee;

    @Schema(description = "采购杂费", example = "20.00")
    private BigDecimal otherFees;

    @Schema(description = "采购总额", example = "10000.00")
    private BigDecimal totalPurchaseAmount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

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
}
