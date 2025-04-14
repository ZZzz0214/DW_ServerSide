package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.wholesale_purchase;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP批发采购订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpWholesalePurchaseOrderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17386")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "WPO202503250001")
    @ExcelProperty("订单号")
    private String no;

    @Schema(description = "采购状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("采购状态")
    private Integer status;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1724")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "批发供应商A")
    @ExcelProperty("供应商名称")
    private String supplierName;

    @Schema(description = "结算账户编号", example = "31189")
    @ExcelProperty("结算账户编号")
    private Long accountId;

    @Schema(description = "采购时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("采购时间")
    private LocalDateTime orderTime;

    @Schema(description = "物流公司", example = "顺丰速运")
    @ExcelProperty("物流公司")
    private String logisticsCompany;

    @Schema(description = "物流单号", example = "SF1234567890")
    @ExcelProperty("物流单号")
    private String trackingNumber;

    @Schema(description = "收件姓名", example = "张三")
    @ExcelProperty("收件姓名")
    private String receiverName;

    @Schema(description = "联系电话", example = "13800138000")
    @ExcelProperty("联系电话")
    private String receiverPhone;

    @Schema(description = "详细地址", example = "北京市朝阳区XX街道XX号")
    @ExcelProperty("详细地址")
    private String receiverAddress;

    @Schema(description = "合计数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @ExcelProperty("合计数量")
    private BigDecimal totalCount;

    @Schema(description = "合计价格，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000.00")
    @ExcelProperty("合计价格")
    private BigDecimal totalPrice;

    @Schema(description = "合计产品价格，单位：元", example = "9000.00")
    @ExcelProperty("合计产品价格")
    private BigDecimal totalProductPrice;

    @Schema(description = "合计税额，单位：元", example = "1000.00")
    @ExcelProperty("合计税额")
    private BigDecimal totalTaxPrice;

    @Schema(description = "优惠率，百分比", example = "99.88")
    @ExcelProperty("优惠率")
    private BigDecimal discountPercent;

    @Schema(description = "优惠金额，单位：元", example = "200.00")
    @ExcelProperty("优惠金额")
    private BigDecimal discountPrice;

    @Schema(description = "定金金额，单位：元", example = "7127")
    @ExcelProperty("定金金额")
    private BigDecimal depositPrice;

    @Schema(description = "采购运费（合计）", example = "100.00")
    @ExcelProperty("采购运费")
    private BigDecimal shippingFee;

    @Schema(description = "其他费用（合计）", example = "200.00")
    @ExcelProperty("其他费用")
    private BigDecimal otherFees;

    @Schema(description = "采购总额（合计）", example = "10000.00")
    @ExcelProperty("采购总额")
    private BigDecimal totalPurchaseAmount;

    @Schema(description = "附件地址", example = "https://www.iocoder.cn")
    @ExcelProperty("附件地址")
    private String fileUrl;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "采购入库数量", example = "100.00")
    @ExcelProperty("采购入库数量")
    private BigDecimal inCount;

    @Schema(description = "采购退货数量", example = "0.00")
    @ExcelProperty("采购退货数量")
    private BigDecimal returnCount;

    @Schema(description = "创建人", example = "admin")
    @ExcelProperty("创建人")
    private String creator;

    @Schema(description = "创建人名称", example = "管理员")
    @ExcelProperty("创建人名称")
    private String creatorName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "订单明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Schema(description = "产品信息")
    @ExcelProperty("产品信息")
    private String productNames;

    @Schema(description = "管理后台 - ERP批发采购订单明细")
    @Data
    public static class Item {

        @Schema(description = "订单项编号", example = "11756")
        private Long id;

        @Schema(description = "采购订单编号", example = "17386")
        private Long orderId;

        @Schema(description = "产品类型：0-单品，1-组合产品", example = "0")
        private Integer type;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productId;

        @Schema(description = "组品编号", example = "3114")
        private Long comboProductId;

        @Schema(description = "原表商品名称", example = "商品A")
        private String originalProductName;

        @Schema(description = "原表规格", example = "规格A")
        private String originalStandard;

        @Schema(description = "原表数量", example = "100")
        private Integer originalQuantity;

        @Schema(description = "售后状况", example = "0")
        private Integer afterSalesStatus;

        @Schema(description = "发货编码", example = "SH202503250001")
        private String shippingCode;

        @Schema(description = "产品数量", example = "100")
        private Integer productQuantity;

        @Schema(description = "采购单价", example = "100.00")
        private BigDecimal purchasePrice;

        @Schema(description = "物流费用", example = "100.00")
        private BigDecimal logisticsFee;

        @Schema(description = "货拉拉费用", example = "50.00")
        private BigDecimal hulalaFee;

        @Schema(description = "其他费用", example = "200.00")
        private BigDecimal otherFees;

        @Schema(description = "采购总额", example = "10000.00")
        private BigDecimal totalPurchaseAmount;

        @Schema(description = "总价", example = "10000.00")
        private BigDecimal totalPrice;

        @Schema(description = "产品单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productUnitId;

        @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal count;

        @Schema(description = "税率，百分比", example = "99.88")
        private BigDecimal taxPercent;

        @Schema(description = "税额，单位：元", example = "1000.00")
        private BigDecimal taxPrice;

        @Schema(description = "备注信息", example = "随便")
        private String remark;

        @Schema(description = "采购人员", example = "采购员A")
        private String purchaser;

        @Schema(description = "采购入库数量", example = "100.00")
        private BigDecimal inCount;

        @Schema(description = "采购退货数量", example = "0.00")
        private BigDecimal returnCount;
            //新增的字段
        @Schema(description = "运费类型（0：固定运费，1：按件计费，2：按重计费）")
        @ExcelProperty("运费类型")
        private Integer shippingFeeType;

        @Schema(description = "固定运费（单位：元）")
        @ExcelProperty("固定运费（单位：元）")
        private BigDecimal fixedShippingFee;

        @Schema(description = "首件数量")
        @ExcelProperty("首件数量")
        private Integer firstItemQuantity;

        @Schema(description = "首件价格（单位：元）")
        @ExcelProperty("首件价格（单位：元）")
        private BigDecimal firstItemPrice;

        @Schema(description = "续件数量")
        @ExcelProperty("续件数量")
        private Integer additionalItemQuantity;

        @Schema(description = "续件价格（单位：元）")
        @ExcelProperty("续件价格（单位：元）")
        private BigDecimal additionalItemPrice;

        @Schema(description = "首重重量（单位：kg）")
        @ExcelProperty("首重重量（单位：kg）")
        private BigDecimal firstWeight;

        @Schema(description = "首重价格（单位：元）")
        @ExcelProperty("首重价格（单位：元）")
        private BigDecimal firstWeightPrice;

        @Schema(description = "续重重量（单位：kg）")
        @ExcelProperty("续重重量（单位：kg）")
        private BigDecimal additionalWeight;

        @Schema(description = "续重价格（单位：元）")
        @ExcelProperty("续重价格（单位：元）")
        private BigDecimal additionalWeightPrice;
    }
}
