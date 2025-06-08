package cn.iocoder.yudao.module.erp.controller.admin.dropship.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 代发辅助导入 Response VO")
@Data
@Builder
public class ErpDropshipAssistImportRespVO {

    @Schema(description = "创建成功的代发辅助名称数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> createNames;

    @Schema(description = "更新成功的代发辅助名称数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> updateNames;

    @Schema(description = "导入失败的代发辅助集合，key 为代发辅助名称，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureNames;
}
