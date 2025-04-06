package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 采购订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpPurchaseOrderPageReqVO extends PageParam {

    /**
     * 入库状态 - 无
     */
    public static final Integer IN_STATUS_NONE = 0;
    /**
     * 入库状态 - 部分
     */
    public static final Integer IN_STATUS_PART = 1;
    /**
     * 入库状态 - 全部
     */
    public static final Integer IN_STATUS_ALL = 2;

    /**
     * 退货状态 - 无
     */
    public static final Integer RETURN_STATUS_NONE = 0;
    /**
     * 退货状态 - 部分
     */
    public static final Integer RETURN_STATUS_PART = 1;
    /**
     * 退货状态 - 全部
     */
    public static final Integer RETURN_STATUS_ALL = 2;

    @Schema(description = "采购单编号", example = "XS001")
    private String no;

    @Schema(description = "供应商编号", example = "1724")
    private Long supplierId;

    @Schema(description = "采购时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] orderTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "采购状态", example = "2")
    private Integer status;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "产品编号", example = "1")
    private Long productId;

    @Schema(description = "入库状态", example = "2")
    private Integer inStatus;

    @Schema(description = "退货状态", example = "2")
    private Integer returnStatus;

    @Schema(description = "是否可入库", example = "true")
    private Boolean inEnable;

    @Schema(description = "是否可退货", example = "true")
    private Boolean returnEnable;

    //新增的字段
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

}
