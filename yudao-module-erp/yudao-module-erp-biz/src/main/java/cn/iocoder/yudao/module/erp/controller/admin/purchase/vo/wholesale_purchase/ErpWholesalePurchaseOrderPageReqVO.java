package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.wholesale_purchase;



import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP批发采购订单分页请求VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpWholesalePurchaseOrderPageReqVO extends PageParam {

    @Schema(description = "订单编号", example = "PO20230001")
    private String no;

    @Schema(description = "供应商ID", example = "12345")
    private Long supplierId;

    @Schema(description = "订单状态", example = "1")
    private Integer status;
}


