//package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;
//
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.Data;
//
//import javax.validation.constraints.NotNull;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Schema(description = "管理后台 - ERP 采购订单新增/修改 Request VO")
//@Data
//public class ErpPurchaseOrderSaveReqVO {
//
//    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17386")
//    private Long id;
//
//    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1724")
//    @NotNull(message = "供应商编号不能为空")
//    private Long supplierId;
//
//    @Schema(description = "结算账户编号", example = "31189")
//    private Long accountId;
//
//    @Schema(description = "采购时间", requiredMode = Schema.RequiredMode.REQUIRED)
//    @NotNull(message = "采购时间不能为空")
//    private LocalDateTime orderTime;
//
//    @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.88")
//    private BigDecimal discountPercent;
//
//    @Schema(description = "定金金额，单位：元", example = "7127")
//    private BigDecimal depositPrice;
//
//    @Schema(description = "附件地址", example = "https://www.iocoder.cn")
//    private String fileUrl;
//
//    @Schema(description = "备注", example = "你猜")
//    private String remark;
//
//    @Schema(description = "订单清单列表")
//    private List<Item> items;
//
//    @Data
//    public static class Item {
//
//        @Schema(description = "订单项编号", example = "11756")
//        private Long id;
//
//        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
//        @NotNull(message = "产品编号不能为空")
//        private Long productId;
//
//        @Schema(description = "产品单位单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
//        @NotNull(message = "产品单位单位不能为空")
//        private Long productUnitId;
//
//        @Schema(description = "产品单价", example = "100.00")
//        private BigDecimal productPrice;
//
//        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
//        @NotNull(message = "产品数量不能为空")
//        private BigDecimal count;
//
//        @Schema(description = "税率，百分比", example = "99.88")
//        private BigDecimal taxPercent;
//
//        @Schema(description = "备注", example = "随便")
//        private String remark;
//
//    }
//
//}
package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购订单新增/修改 Request VO")
@Data
public class ErpPurchaseOrderSaveReqVO {

    @Schema(description = "编号", example = "17386")
    private Long id;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "PO202503250001")
    private String no;

    @Schema(description = "采购状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1724")
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "结算账户编号", example = "31189")
    private Long accountId;

    @Schema(description = "采购时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "采购时间不能为空")
    private LocalDateTime orderTime;

    @Schema(description = "物流公司", example = "顺丰速运")
    private String logisticsCompany;

    @Schema(description = "物流单号", example = "SF1234567890")
    private String trackingNumber;

    @Schema(description = "收件姓名", example = "张三")
    private String receiverName;

    @Schema(description = "联系电话", example = "13800138000")
    private String receiverPhone;

    @Schema(description = "详细地址", example = "北京市朝阳区XX街道XX号")
    private String receiverAddress;

    @Schema(description = "合计数量", example = "100.00")
    private BigDecimal totalCount;

    @Schema(description = "合计价格，单位：元", example = "10000.00")
    private BigDecimal totalPrice;

    @Schema(description = "合计产品价格，单位：元", example = "9000.00")
    private BigDecimal totalProductPrice;

    @Schema(description = "合计税额，单位：元", example = "1000.00")
    private BigDecimal totalTaxPrice;

    @Schema(description = "优惠率，百分比", example = "99.88")
    private BigDecimal discountPercent;

    @Schema(description = "优惠金额，单位：元", example = "200.00")
    private BigDecimal discountPrice;

    @Schema(description = "定金金额，单位：元", example = "7127")
    private BigDecimal depositPrice;

    @Schema(description = "采购运费（合计）", example = "100.00")
    private BigDecimal shippingFee;

    @Schema(description = "其他费用（合计）", example = "200.00")
    private BigDecimal otherFees;

    @Schema(description = "采购总额（合计）", example = "10000.00")
    private BigDecimal totalPurchaseAmount;

    @Schema(description = "附件地址", example = "https://www.iocoder.cn")
    private String fileUrl;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "采购入库数量", example = "100.00")
    private BigDecimal inCount;

    @Schema(description = "采购退货数量", example = "0.00")
    private BigDecimal returnCount;

    @Schema(description = "创建者", example = "admin")
    private String creator;

    @Schema(description = "更新者", example = "admin")
    private String updater;

    @Schema(description = "是否删除", example = "false")
    private Boolean deleted;

    @Schema(description = "租户编号", example = "1")
    private Long tenantId;

    @Schema(description = "订单清单列表")
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "订单项编号", example = "11756")
        private Long id;

        @Schema(description = "采购订单编号", example = "17386")
        private Long orderId;

        @Schema(description = "产品类型：0-单品，1-组合产品", example = "0")
        private Integer type;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "产品编号不能为空")
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

        @Schema(description = "采购运费", example = "100.00")
        private BigDecimal shippingFee;

        @Schema(description = "其他费用", example = "200.00")
        private BigDecimal otherFees;

        @Schema(description = "采购总额", example = "10000.00")
        private BigDecimal totalPurchaseAmount;

        @Schema(description = "总价", example = "10000.00")
        private BigDecimal totalPrice;

        @Schema(description = "产品单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "产品单位编号不能为空")
        private Long productUnitId;

        @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "数量不能为空")
        private BigDecimal count;

        @Schema(description = "税率，百分比", example = "99.88")
        private BigDecimal taxPercent;

        @Schema(description = "税额，单位：元", example = "1000.00")
        private BigDecimal taxPrice;

        @Schema(description = "备注", example = "随便")
        private String remark;

        @Schema(description = "采购入库数量", example = "100.00")
        private BigDecimal inCount;

        @Schema(description = "采购退货数量", example = "0.00")
        private BigDecimal returnCount;

        @Schema(description = "是否删除", example = "false")
        private Boolean deleted;

        @Schema(description = "租户编号", example = "1")
        private Long tenantId;

    }

}
