package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - ERP 财务金额审核 Request VO")
@Data
public class ErpFinanceAmountAuditReqVO {

    @Schema(description = "记录ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "记录ID列表不能为空")
    private List<Long> ids;

    @Schema(description = "审核状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    @Schema(description = "审核备注", example = "审核通过")
    private String auditRemark;
} 