package cn.iocoder.yudao.module.erp.controller.admin.product.vo.price;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "ERP 销售价格分页 Request VO")
public class ErpSalesPricePageReqVO extends PageParam {

    @Schema(description = "产品类型：0-单品，1-组品", example = "0")
    private Integer type;

    @Schema(description = "单品编号", example = "1")
    private Long productId;

    @Schema(description = "组品编号", example = "2")
    private Long comboProductId;

    @Schema(description = "客户名称", example = "测试客户")
    private String customerName;

}