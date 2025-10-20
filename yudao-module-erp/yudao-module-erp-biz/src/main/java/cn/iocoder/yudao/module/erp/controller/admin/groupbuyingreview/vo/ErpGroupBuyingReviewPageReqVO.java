package cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 团购复盘分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpGroupBuyingReviewPageReqVO extends PageParam {

    @Schema(description = "编号", example = "GBR001")
    private String no;

    @Schema(description = "团购货盘编号", example = "GB001")
    private String groupBuyingNo;

    @Schema(description = "品牌名称", example = "品牌A")
    private String brandName;

    @Schema(description = "产品名称", example = "产品A")
    private String productName;

    @Schema(description = "产品规格", example = "500ml")
    private String productSpec;

    @Schema(description = "产品SKU", example = "SKU12345")
    private String productSku;

    @Schema(description = "客户名称", example = "张三")
    private String customerName;

    @Schema(description = "供团价格", example = "100.00")
    private String supplyGroupPrice;

    @Schema(description = "快递费用", example = "10.00")
    private String expressFee;

    @Schema(description = "开团价格", example = "99.00")
    private String groupPrice;

    @Schema(description = "寄样日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate[] sampleSendDate;

    @Schema(description = "开团日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate[] groupStartDate;

    @Schema(description = "货盘状态", example = "上架")
    private String status;

    @Schema(description = "复盘状态", example = "已完成")
    private String reviewStatus;

    @Schema(description = "创建人员", example = "张三")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}