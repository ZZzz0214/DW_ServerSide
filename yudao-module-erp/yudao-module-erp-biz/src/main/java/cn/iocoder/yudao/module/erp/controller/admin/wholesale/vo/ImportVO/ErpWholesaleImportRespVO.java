package cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 批发导入 Response VO")
@Data
@Builder
public class ErpWholesaleImportRespVO {

    @Schema(description = "创建成功的批发订单编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> createNames;

    @Schema(description = "更新成功的批发订单编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> updateNames;

    @Schema(description = "导入失败的批发订单集合，key 为订单编号，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureNames;
}