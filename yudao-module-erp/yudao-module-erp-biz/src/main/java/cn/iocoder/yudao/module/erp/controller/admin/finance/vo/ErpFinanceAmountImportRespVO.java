package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - ERP 财务金额导入 Response VO")
@Data
@Builder
public class ErpFinanceAmountImportRespVO {

    @Schema(description = "创建成功的财务金额编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> createNames;

    @Schema(description = "更新成功的财务金额编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> updateNames;

    @Schema(description = "导入失败的财务金额数组，key 为财务金额编号，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureNames;
} 