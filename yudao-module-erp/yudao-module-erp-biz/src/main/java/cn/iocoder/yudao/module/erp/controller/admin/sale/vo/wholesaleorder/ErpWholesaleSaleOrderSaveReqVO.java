package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.wholesaleorder;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 批发销售订单新增/修改 Request VO")
@Data
public class ErpWholesaleSaleOrderSaveReqVO {

    @Schema(description = "编号", example = "17386")
    private Long id;

    @Schema(description = "批发销售单编号", example = "XS001")
    private String no;

    @Schema(description = "订单号", example = "DD001")
    private String orderNumber;

    @Schema(description = "销售状态", example = "2")
    private Integer status;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1724")
    @NotNull(message = "客户编号不能为空")
    private Long customerId;

    @Schema(description = "结算账户编号", example = "31189")
    private Long accountId;

    @Schema(description = "销售员编号", example = "1888")
    private Long saleUserId;

    @Schema(description = "下单时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "下单时间不能为空")
    private LocalDateTime orderTime;

    @Schema(description = "物流公司", example = "顺丰")
    private String logisticsCompany;

    @Schema(description = "物流单号", example = "SF123456789")
    private String logisticsNumber;

    @Schema(description = "收件姓名", example = "张三")
    private String consigneeName;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactNumber;

    @Schema(description = "详细地址", example = "北京市朝阳区")
    private String detailedAddress;

    @Schema(description = "售后状况", example = "未售后")
    private String afterSaleStatus;

    @Schema(description = "备注信息", example = "备注信息")
    private String remark;

    @Schema(description = "合计数量", example = "15663")
    private BigDecimal totalCount;

    @Schema(description = "合计价格，单位：元", example = "24906")
    private BigDecimal totalPrice;

    @Schema(description = "合计产品价格，单位：元", example = "7127")
    private BigDecimal totalProductPrice;

    @Schema(description = "合计税额，单位：元", example = "7127")
    private BigDecimal totalTaxPrice;

    @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.88")
    private BigDecimal discountPercent;

    @Schema(description = "优惠金额，单位：元", example = "7127")
    private BigDecimal discountPrice;

    @Schema(description = "定金金额，单位：元", example = "7127")
    private BigDecimal depositPrice;

    @Schema(description = "物流费用合计", example = "100.00")
    private BigDecimal totalLogisticsFee;

    @Schema(description = "其他费用合计", example = "100.00")
    private BigDecimal totalOtherFees;

    @Schema(description = "销售总额（合计）", example = "25000")
    private BigDecimal totalSaleAmount;

    @Schema(description = "附件地址", example = "https://www.iocoder.cn")
    private String fileUrl;

    @Schema(description = "销售出库数量", example = "100.00")
    private BigDecimal outCount;

    @Schema(description = "销售退货数量", example = "100.00")
    private BigDecimal returnCount;

    @Schema(description = "货拉拉费用合计", example = "100.00")
    private BigDecimal totalHulalaFee;

    @Schema(description = "订单项列表")
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "订单项编号", example = "11756")
        private Long id;

        @Schema(description = "批发销售订单编号", example = "17386")
        private Long orderId;

        @Schema(description = "组品编号", example = "12345")
        private Long groupProductId;

        @Schema(description = "产品名称", example = "产品A")
        private String productName;

        @Schema(description = "客户名称", example = "客户A")
        private String customerName;

        @Schema(description = "销售人员", example = "芋道")
        private String salesPerson;

        @Schema(description = "原表商品", example = "原表商品A")
        private String originalProduct;

        @Schema(description = "原表规格", example = "规格A")
        private String originalSpecification;

        @Schema(description = "原表数量", example = "10")
        private Integer originalQuantity;

        @Schema(description = "发货编码", example = "SH202503250001")
        private String shippingCode;

        @Schema(description = "产品数量", example = "10")
        private Integer productQuantity;

        @Schema(description = "物流费用", example = "10.00")
        private BigDecimal logisticsFee;

        @Schema(description = "其他费用", example = "10.00")
        private BigDecimal otherFees;

        @Schema(description = "出货总额", example = "200.00")
        private BigDecimal shippingTotal;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "产品编号不能为空")
        private Long productId;

        @Schema(description = "产品单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "产品单位编号不能为空")
        private Long productUnitId;

        @Schema(description = "批发出货单价", example = "100.00")
        private BigDecimal wholesaleProductPrice;

        @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "数量不能为空")
        private BigDecimal count;

        @Schema(description = "总价", example = "1000.00")
        private BigDecimal totalPrice;

        @Schema(description = "税率，百分比", example = "99.88")
        private BigDecimal taxPercent;

        @Schema(description = "税额，单位：元", example = "100.00")
        private BigDecimal taxPrice;

        @Schema(description = "备注", example = "随便")
        private String remark;

        @Schema(description = "销售出库数量", example = "100.00")
        private BigDecimal outCount;

        @Schema(description = "销售退货数量", example = "100.00")
        private BigDecimal returnCount;

        @Schema(description = "货拉拉费用", example = "10.00")
        private BigDecimal hulalaFee;
    }
}
