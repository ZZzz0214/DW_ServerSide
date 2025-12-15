package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo;

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

@Schema(description = "管理后台 - ERP 私播复盘分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpPrivateBroadcastingReviewPageReqVO extends PageParam {

    @Schema(description = "编号", example = "SBF001")
    private String no;

    @Schema(description = "私播货盘编号", example = "PB001")
    private String privateBroadcastingNo;

    @Schema(description = "品牌名称", example = "品牌A")
    private String brandName;
    
    @Schema(description = "品牌名称（多选）", example = "品牌A,品牌B")
    private List<String> brandNames;
    
    @Schema(description = "品牌名称是否为空", example = "true")
    private Boolean brandNameEmpty;

    @Schema(description = "产品名称", example = "产品A")
    private String productName;

    @Schema(description = "产品规格", example = "500ml")
    private String productSpec;

    @Schema(description = "客户名称", example = "张三")
    private String customerName;

    @Schema(description = "产品裸价", example = "80.00")
    private String nakedPrice;

    @Schema(description = "快递费用", example = "10.00")
    private String expressFee;

    @Schema(description = "代发价格", example = "90.00")
    private String dropshippingPrice;

    @Schema(description = "寄样日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate[] sampleSendDate;

    @Schema(description = "开团日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate[] groupStartDate;

    @Schema(description = "货盘状态", example = "未设置")
    private String status;
    
    @Schema(description = "货盘状态（多选）", example = "未设置,已设置")
    private List<String> statuses;
    
    @Schema(description = "货盘状态是否为空", example = "true")
    private Boolean statusEmpty;

    @Schema(description = "复盘状态", example = "已完成")
    private String reviewStatus;
    
    @Schema(description = "复盘状态（多选）", example = "已完成,进行中")
    private List<String> reviewStatuses;
    
    @Schema(description = "复盘状态是否为空", example = "true")
    private Boolean reviewStatusEmpty;

    @Schema(description = "创建人员", example = "张三")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}