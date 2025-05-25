package cn.iocoder.yudao.module.erp.controller.admin.dropship.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 代发辅助 Response VO")
@Data
public class ErpDropshipAssistRespVO {

    @Schema(description = "代发辅助表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "DF202403250001")
    private String no;

    @Schema(description = "原表商品", example = "商品A")
    private String originalProduct;

    @Schema(description = "原表规格", example = "规格A")
    private String originalSpec;

    @Schema(description = "原表数量", example = "100")
    private Integer originalQuantity;

    @Schema(description = "组品编号", example = "1")
    private String comboProductId;

    @Schema(description = "发货编码", example = "SH123456")
    private String shippingCode;

    @Schema(description = "产品名称", example = "组合产品A")
    private String productName;

    @Schema(description = "产品简称", example = "组合A")
    private String productShortName;

    @Schema(description = "产品规格", example = "规格B")
    private String productSpec;

    @Schema(description = "产品数量", example = "100")
    private Integer productQuantity;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}