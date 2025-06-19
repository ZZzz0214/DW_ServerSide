package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 组合产品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpComboPageReqVO extends PageParam {

    @Schema(description = "组品编码", example = "CB001")
    private String no;

    @Schema(description = "组合产品名称", example = "李四")
    private String name;

    @Schema(description = "产品简称", example = "简称")
    private String shortName;

    @Schema(description = "发货编码", example = "11161")
    private String shippingCode;

    @Schema(description = "采购人员", example = "张三")
    private String purchaser;

    @Schema(description = "供应商名", example = "供应商A")
    private String supplier;

    @Schema(description = "创建人员", example = "admin")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "全文搜索关键词", example = "产品")
    private String keyword;

    @Schema(description = "深度分页最后一条记录ID", example = "1024")
    private Long lastId;
}