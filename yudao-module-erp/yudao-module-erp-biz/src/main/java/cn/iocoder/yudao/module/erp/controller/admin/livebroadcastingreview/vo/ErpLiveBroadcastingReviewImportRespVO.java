package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 直播复盘导入 Response VO")
@Data
@Builder
public class ErpLiveBroadcastingReviewImportRespVO {

    @Schema(description = "创建成功的直播复盘编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> createNames;

    @Schema(description = "更新成功的直播复盘编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> updateNames;

    @Schema(description = "导入失败的直播复盘集合，key 为直播复盘编号，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureNames;
} 