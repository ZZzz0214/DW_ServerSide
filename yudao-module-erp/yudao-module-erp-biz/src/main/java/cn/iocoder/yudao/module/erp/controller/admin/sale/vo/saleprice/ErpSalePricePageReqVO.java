package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 销售价格分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpSalePricePageReqVO extends PageParam {

    @Schema(description = "组品编号", example = "12345")
    private Long groupProductId;

    @Schema(description = "客户名称", example = "客户A")
    private String customerName;

    @Schema(description = "代发单价范围")
    private BigDecimal[] distributionPriceRange;

    @Schema(description = "批发单价范围")
    private BigDecimal[] wholesalePriceRange;

    @Schema(description = "创建时间范围")
    private LocalDateTime[] createTime;
}
