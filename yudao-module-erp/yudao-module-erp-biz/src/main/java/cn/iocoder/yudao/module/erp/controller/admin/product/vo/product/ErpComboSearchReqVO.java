package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 组合产品搜索请求 VO")
@Data
public class ErpComboSearchReqVO {

    @Schema(description = "组品编号", example = "15672")
    private Long id;

    @Schema(description = "组合产品名称", example = "示例组合产品")
    private String name;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}