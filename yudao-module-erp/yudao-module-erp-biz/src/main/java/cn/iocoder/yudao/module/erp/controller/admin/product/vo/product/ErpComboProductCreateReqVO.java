package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - ERP 组品创建请求 VO")
@Data
public class ErpComboProductCreateReqVO {

    @Schema(description = "组品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "组合产品名称")
    @NotNull(message = "组品名称不能为空")
    private String name;

    @Schema(description = "关联的单品列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "至少需要关联一个单品")
    private List<ComboItem> items;

    @Data
    public static class ComboItem {
        @Schema(description = "单品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "单品编号不能为空")
        private Long productId;

        @Schema(description = "单品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        @NotNull(message = "单品数量不能为空")
        private Integer quantity;
    }
}