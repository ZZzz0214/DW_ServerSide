package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 私播货盘分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpPrivateBroadcastingPageReqVO extends PageParam {

    @Schema(description = "编号", example = "PB001")
    private String no;

    @Schema(description = "品牌名称", example = "品牌A")
    private String brandName;

    @Schema(description = "产品名称", example = "产品A")
    private String productName;

    @Schema(description = "产品规格", example = "500ml")
    private String productSpec;

    @Schema(description = "保质日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate[] shelfLife;

    @Schema(description = "直播价格", example = "100.00")
    private String livePrice;

    @Schema(description = "产品裸价", example = "80.00")
    private String nakedPrice;

    @Schema(description = "快递费用", example = "10.00")
    private String expressFee;

    @Schema(description = "代发价格", example = "90.00")
    private String dropshippingPrice;

    @Schema(description = "货盘状态", example = "未设置")
    private String privateStatus;

    @Schema(description = "创建人员", example = "张三")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}