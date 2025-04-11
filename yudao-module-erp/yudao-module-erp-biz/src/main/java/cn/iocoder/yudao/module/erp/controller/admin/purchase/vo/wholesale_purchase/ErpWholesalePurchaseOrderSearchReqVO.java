package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.wholesale_purchase;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP批发采购订单搜索请求VO")
@Data
public class ErpWholesalePurchaseOrderSearchReqVO {

    @Schema(description = "订单编号", example = "PO20230001")
    private String no;

    @Schema(description = "供应商ID", example = "12345")
    private Long supplierId;

    @Schema(description = "订单状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间范围")
    private LocalDateTime[] createTime;
}
