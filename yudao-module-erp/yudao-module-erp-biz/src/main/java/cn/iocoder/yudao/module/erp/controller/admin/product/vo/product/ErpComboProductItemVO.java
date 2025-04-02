package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - ERP 组品项 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpComboProductItemVO {

    @Schema(description = "组品项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("组品项编号")
    private Long id;

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("组品编号")
    private Long comboProductId;

    @Schema(description = "单品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("单品编号")
    private Long itemProductId;

    @Schema(description = "单品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("单品数量")
    private Integer itemQuantity;

    @Schema(description = "创建者")
    @ExcelProperty("创建者")
    private String creator;

    @Schema(description = "更新者")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "是否删除（0：未删除，1：已删除）")
    @ExcelProperty("是否删除")
    private Boolean deleted;

    @Schema(description = "租户编号")
    @ExcelProperty("租户编号")
    private Long tenantId;

}
