package cn.iocoder.yudao.module.erp.controller.admin.distribution.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 代发批量更新销售审核状态 Request VO")
@Data
public class ErpDistributionBatchUpdateSaleAuditReqVO {

    @Schema(description = "订单数据列表（有选择时使用）")
    private List<OrderData> orderData;

    @Schema(description = "审核状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "审核状态不能为空")
    private Integer saleAuditStatus;

    @Schema(description = "搜索参数（全选时使用）")
    private Object searchParams;

    @Schema(description = "是否全选", example = "false")
    private Boolean isSelectAll = false;

    @Schema(description = "订单数据")
    @Data
    public static class OrderData {
        @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "订单ID不能为空")
        private Long id;

        @Schema(description = "出货总额", example = "100.00")
        private BigDecimal totalSaleAmount;
    }
} 