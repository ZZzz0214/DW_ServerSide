package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 组合产品搜索请求 VO")
@Data
public class ErpSalePriceSearchReqVO {

    @Schema(description = "组品编号", example = "15672")
    private Long groupProductId;

    @Schema(description = "组合名称", example = "示例组合产品")
    private String productName;

    @Schema(description = "组合简称", example = "示例组合产品")
    private String productShortName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "客户名称", example = "张三")
    private String name;
}
