package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Schema(description = "管理后台 - ERP 代发批发产品组品统计 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ErpDistributionWholesaleProductStatisticsReqVO extends PageParam {

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01")
    @NotNull(message = "开始日期不能为空")
    private String startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-12-31")
    @NotNull(message = "结束日期不能为空")
    private String endDate;

    @Schema(description = "供应商名称", example = "供应商A")
    private String supplier;

    @Schema(description = "客户名称", example = "客户A")
    private String customerName;

    /**
     * 获取开始日期
     */
    public LocalDate getStartDate() {
        if (startDate == null || startDate.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 获取结束日期
     */
    public LocalDate getEndDate() {
        if (endDate == null || endDate.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

} 