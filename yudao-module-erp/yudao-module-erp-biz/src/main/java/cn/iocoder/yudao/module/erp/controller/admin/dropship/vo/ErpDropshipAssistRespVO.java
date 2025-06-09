package cn.iocoder.yudao.module.erp.controller.admin.dropship.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 代发辅助 Response VO")
@Data
public class ErpDropshipAssistRespVO {

    @Schema(description = "代发辅助表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "DF202403250001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "原表商品", example = "商品A")
    @ExcelProperty("原表商品")
    private String originalProduct;

    @Schema(description = "原表规格", example = "规格A")
    @ExcelProperty("原表规格")
    private String originalSpec;

    @Schema(description = "原表数量", example = "100")
    @ExcelProperty("原表数量")
    private Integer originalQuantity;

    @Schema(description = "组品编号", example = "1")
    private String comboProductId;

    @Schema(description = "组品编号", example = "1")
    @ExcelProperty("组品编号")
    private String comboProductNo;

    @Schema(description = "发货编码", example = "SH123456")
    @ExcelProperty("发货编码")
    private String shippingCode;

    @Schema(description = "产品名称", example = "组合产品A")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "产品规格", example = "规格B")
    @ExcelProperty("产品规格")
    private String productSpec;

    @Schema(description = "产品数量", example = "100")
    @ExcelProperty("产品数量")
    private Integer productQuantity;

    @Schema(description = "创建者")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "产品简称", example = "组合A")
    private String productShortName;

    @Schema(description = "产品名称", example = "组合A")
    private String name;


}
