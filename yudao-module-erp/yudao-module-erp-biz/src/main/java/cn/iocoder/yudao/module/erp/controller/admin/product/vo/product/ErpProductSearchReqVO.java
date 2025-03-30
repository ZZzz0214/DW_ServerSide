package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 产品搜索请求 VO")
@Data
public class ErpProductSearchReqVO {

    @Schema(description = "产品编号", example = "1024")
    private Long id;

    @Schema(description = "产品名称", example = "示例产品")
    private String name;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}