package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 销售价格导入 Response VO")
@Data
@Builder
public class ErpSalePriceImportRespVO {

    @Schema(description = "创建成功的销售价格编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> createNames;

    @Schema(description = "更新成功的销售价格编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> updateNames;

    @Schema(description = "导入失败的销售价格集合，key 为销售价格编号，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureNames;
}