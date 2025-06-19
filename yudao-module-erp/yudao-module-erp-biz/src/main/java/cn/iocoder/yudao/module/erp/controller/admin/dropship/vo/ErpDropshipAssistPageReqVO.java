package cn.iocoder.yudao.module.erp.controller.admin.dropship.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 代发辅助分页 Request VO")
@Data
public class ErpDropshipAssistPageReqVO extends PageParam {

    @Schema(description = "编号", example = "DF202403250001")
    private String no;

    @Schema(description = "原表商品", example = "商品A")
    private String originalProduct;

    @Schema(description = "原表规格", example = "规格A")
    private String originalSpec;

    @Schema(description = "组品编号", example = "1")
    private String comboProductId;

    @Schema(description = "发货编码", example = "SH001")
    private String shippingCode;

    @Schema(description = "产品名称", example = "产品A")
    private String productName;

    @Schema(description = "产品规格", example = "规格B")
    private String productSpec;

    @Schema(description = "状态信息", example = "pending")
    private String status;

    @Schema(description = "创建人员", example = "admin")
    private String creator;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
