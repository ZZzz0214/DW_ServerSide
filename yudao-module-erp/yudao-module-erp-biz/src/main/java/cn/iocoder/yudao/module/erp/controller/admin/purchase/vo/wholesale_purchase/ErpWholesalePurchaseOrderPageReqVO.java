package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.wholesale_purchase;


import org.springframework.format.annotation.DateTimeFormat;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
@Schema(description = "管理后台 - ERP批发采购订单分页请求VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpWholesalePurchaseOrderPageReqVO extends PageParam {

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

    @Schema(description = "订单编号", example = "PO20230001")
    private String no;

    @Schema(description = "供应商ID", example = "12345")
    private Long supplierId;

    @Schema(description = "采购时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] orderTime;

    @Schema(description = "订单状态", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "备注内容")
    private String remark;

    @Schema(description = "创建者", example = "admin")
    private String creator;

    @Schema(description = "产品编号", example = "1")
    private Long productId;

    @Schema(description = "入库状态", example = "0")
    private Integer inStatus;

    @Schema(description = "退货状态", example = "0")
    private Integer returnStatus;

    @Schema(description = "是否可入库", example = "true")
    private Boolean inEnable;

    @Schema(description = "是否可退货", example = "true")
    private Boolean returnEnable;
}


