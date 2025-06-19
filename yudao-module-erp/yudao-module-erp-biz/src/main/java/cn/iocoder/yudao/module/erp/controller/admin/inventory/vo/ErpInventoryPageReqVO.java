package cn.iocoder.yudao.module.erp.controller.admin.inventory.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 库存分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpInventoryPageReqVO extends PageParam {

    @Schema(description = "库存编号", example = "KC001")
    private String no;

    @Schema(description = "单品ID", example = "1")
    private Long productId;

    @Schema(description = "产品编号", example = "P001")
    private String productNo;

    @Schema(description = "产品名称", example = "商品A")
    private String productName;

    @Schema(description = "产品简称", example = "商品A简称")
    private String productShortName;

    @Schema(description = "品牌名称", example = "品牌A")
    private String brand;

    @Schema(description = "产品品类", example = "品类A")
    private String category;

    @Schema(description = "现货库存", example = "100")
    private Integer spotInventory;

    @Schema(description = "剩余库存", example = "50")
    private Integer remainingInventory;

    @Schema(description = "创建人员", example = "张三")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
