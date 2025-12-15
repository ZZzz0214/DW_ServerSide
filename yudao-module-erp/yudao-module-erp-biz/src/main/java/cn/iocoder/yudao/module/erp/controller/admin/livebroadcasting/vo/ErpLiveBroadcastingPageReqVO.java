package cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo;

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

@Schema(description = "管理后台 - ERP 直播货盘分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpLiveBroadcastingPageReqVO extends PageParam {

    @Schema(description = "编号", example = "ZBHP001")
    private String no;

    @Schema(description = "品牌名称", example = "品牌A")
    private String brandName;

    @Schema(description = "品牌名称（多选）", example = "[\"品牌A\", \"品牌B\"]")
    private List<String> brandNames;

    @Schema(description = "品牌名称为空筛选", example = "true")
    private Boolean brandNameEmpty;

    @Schema(description = "产品名称", example = "产品A")
    private String productName;

    @Schema(description = "产品规格", example = "500ml")
    private String productSpec;

    @Schema(description = "保质日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate[] shelfLife;

    @Schema(description = "直播价格", example = "100.00")
    private String livePrice;

    @Schema(description = "直播佣金", example = "10.00")
    private String liveCommission;

    @Schema(description = "公开佣金", example = "5.00")
    private String publicCommission;

    @Schema(description = "直播货盘状态", example = "未设置")
    private String liveStatus;

    @Schema(description = "直播货盘状态（多选）", example = "[\"未设置\", \"已设置\"]")
    private List<String> liveStatuses;

    @Schema(description = "直播货盘状态为空筛选", example = "true")
    private Boolean liveStatusEmpty;

    @Schema(description = "创建人员", example = "张三")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}