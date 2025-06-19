package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 销售价格分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpSalePricePageReqVO extends PageParam {

    @Schema(description = "编号", example = "SP001")
    private String no;

    @Schema(description = "组品ID", example = "12345")
    private Long groupProductId;

    @Schema(description = "组品编号", example = "ZPXX20250620003814000001")
    private String groupProductNo;

    @Schema(description = "产品名称", example = "测试产品名称")
    private String productName;

    @Schema(description = "产品简称", example = "测试简称")
    private String productShortName;

    @Schema(description = "客户名称", example = "客户A")
    private String customerName;

    @Schema(description = "代发单价", example = "10.50")
    private BigDecimal distributionPrice;

    @Schema(description = "批发单价", example = "8.50")
    private BigDecimal wholesalePrice;

    @Schema(description = "创建人员", example = "admin")
    private String creator;

    @Schema(description = "创建时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    // 保留原有字段用于兼容
    @Schema(description = "代发单价范围", hidden = true)
    private BigDecimal[] distributionPriceRange;

    @Schema(description = "批发单价范围", hidden = true)
    private BigDecimal[] wholesalePriceRange;
}
