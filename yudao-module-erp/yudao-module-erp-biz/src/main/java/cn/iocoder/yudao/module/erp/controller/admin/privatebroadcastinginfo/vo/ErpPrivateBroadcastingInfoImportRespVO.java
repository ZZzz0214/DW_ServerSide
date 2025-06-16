package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 私播信息导入 Response VO")
@Data
@Builder
public class ErpPrivateBroadcastingInfoImportRespVO {

    @Schema(description = "创建成功的私播信息名称数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> createNames;

    @Schema(description = "更新成功的私播信息名称数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> updateNames;

    @Schema(description = "导入失败的私播信息集合，key 为私播信息名称，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureNames;
} 