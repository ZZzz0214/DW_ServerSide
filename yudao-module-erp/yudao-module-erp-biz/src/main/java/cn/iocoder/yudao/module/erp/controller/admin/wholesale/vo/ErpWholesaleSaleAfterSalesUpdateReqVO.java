package cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 批发销售售后信息更新 Request VO")
@Data
public class ErpWholesaleSaleAfterSalesUpdateReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "销售售后状态", example = "1")
    private Integer saleAfterSalesStatus;

    @Schema(description = "销售售后情况", example = "售后情况说明")
    private String saleAfterSalesSituation;

    @Schema(description = "销售售后金额", example = "100.00")
    private BigDecimal saleAfterSalesAmount;

    @Schema(description = "销售售后时间", example = "2023-01-01 12:00:00")
    private String saleAfterSalesTime;
}
