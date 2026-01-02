package cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 团购货盘分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpGroupBuyingPageReqVO extends PageParam {

    @Schema(description = "编号", example = "GB001")
    private String no;

    @Schema(description = "产品名称", example = "产品A")
    private String productName;

    @Schema(description = "品牌名称", example = "品牌A")
    private String brandName;

    @Schema(description = "产品分类编号", example = "1")
    private Long categoryId;

    @Schema(description = "产品分类编号（多选）", example = "[1, 2, 3]")
    private List<Long> categoryIds;

    @Schema(description = "品牌名称（多选）", example = "[\"品牌A\", \"品牌B\"]")
    private List<String> brandNames;

    @Schema(description = "品牌名称为空筛选", example = "true")
    private Boolean brandNameEmpty;

    @Schema(description = "产品规格", example = "500ml")
    private String productSpec;

    @Schema(description = "保质日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate[] shelfLife;

    @Schema(description = "供团价格", example = "100.00")
    private String supplyGroupPrice;

    @Schema(description = "帮卖佣金", example = "10.00")
    private String sellingCommission;

    @Schema(description = "开团价格", example = "120.00")
    private String groupPrice;

    @Schema(description = "货盘状态", example = "上架")
    private String status;

    @Schema(description = "货盘状态（多选）", example = "[\"上架\", \"下架\"]")
    private List<String> statuses;

    @Schema(description = "货盘状态为空筛选", example = "true")
    private Boolean statusEmpty;

    @Schema(description = "创建人员", example = "张三")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
