package cn.iocoder.yudao.module.erp.controller.admin.inventory.vo;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 库存 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpInventoryRespVO {

    @Schema(description = "库存ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("库存ID")
    private Long id;

    @Schema(description = "库存编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "KC001")
    @ExcelProperty("库存编号")
    private String no;

    @Schema(description = "单品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("单品ID")
    private Long productId;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SP001")
    @ExcelProperty("产品编号")
    private String productNo;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品A")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "产品简称", example = "产品A简称")
    @ExcelProperty("产品简称")
    private String productShortName;

    @Schema(description = "品牌名称", example = "品牌A")
    @ExcelProperty("品牌名称")
    private String brand;

    @Schema(description = "产品品类", example = "品类A")
    @ExcelProperty("产品品类")
    private String category;

    @Schema(description = "现货库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("现货库存")
    private Integer spotInventory;

    @Schema(description = "剩余库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    @ExcelProperty("剩余库存")
    private Integer remainingInventory;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "创建人员", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


}
