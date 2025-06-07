package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 产品导入 Response VO")
@Data
@Builder
public class ErpProductImportRespVO {

    @Schema(description = "创建成功的产品名称数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> createNames;

    @Schema(description = "更新成功的产品名称数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> updateNames;

    @Schema(description = "导入失败的产品集合，key 为产品名称，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureNames;
}