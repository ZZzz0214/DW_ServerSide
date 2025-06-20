package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 代发缺失价格 Response VO")
@Data
public class ErpDistributionMissingPriceVO {

    @Schema(description = "组品编号", example = "1")
    private Long comboProductId;

    @Schema(description = "组品编号", example = "CP20240001")
    private String comboProductNo;

    @Schema(description = "产品名称", example = "商品A")
    private String productName;

    @Schema(description = "客户名称", example = "客户A")
    private String customerName;

    @Schema(description = "当前代发单价", example = "0.00")
    private BigDecimal distributionPrice;

    @Schema(description = "订单数量", example = "5")
    private Integer orderCount;

    @Schema(description = "总产品数量", example = "100")
    private Integer totalProductQuantity;

    @Schema(description = "相关订单编号列表")
    private List<String> orderNumbers;

    @Schema(description = "相关订单ID列表")
    private List<Long> orderIds;

    @Schema(description = "最早创建时间")
    private LocalDateTime earliestCreateTime;

    @Schema(description = "最晚创建时间")
    private LocalDateTime latestCreateTime;
} 