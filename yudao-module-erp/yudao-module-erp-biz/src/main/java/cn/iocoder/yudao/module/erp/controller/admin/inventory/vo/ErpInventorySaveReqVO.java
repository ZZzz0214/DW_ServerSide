package cn.iocoder.yudao.module.erp.controller.admin.inventory.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 库存新增/修改 Request VO")
@Data
public class ErpInventorySaveReqVO {

    @Schema(description = "库存ID", example = "1")
    private Long id;

    @Schema(description = "库存编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "KC001")
    private String no;

    @Schema(description = "单品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "单品ID不能为空")
    private Long productId;

    @Schema(description = "现货库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "现货库存不能为空")
    private Integer spotInventory;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;
}