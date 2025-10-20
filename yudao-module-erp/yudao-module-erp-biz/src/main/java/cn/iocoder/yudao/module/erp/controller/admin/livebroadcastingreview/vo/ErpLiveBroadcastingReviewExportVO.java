package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo;

import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 直播复盘导出 VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErpLiveBroadcastingReviewExportVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "FPDD001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "货盘编号", example = "LP001")
    @ExcelProperty("直播货盘编号")
    private String liveBroadcastingNo;

    @Schema(description = "品牌名称", example = "品牌A")
    @ExcelProperty(value = "品牌名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_BRAND)
    private String brandName;

    @Schema(description = "产品名称", example = "产品A")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "产品规格", example = "标准规格")
    @ExcelProperty("产品规格")
    private String productSpec;

    @Schema(description = "产品SKU", example = "SKU001")
    @ExcelProperty("产品SKU")
    private String productSku;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;




    @Schema(description = "客户名称", example = "客户A")
    @ExcelProperty(value = "客户名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_LIVE_CUSTOMER_NAME)
    private String customerName;

    @Schema(description = "直播平台", example = "抖音")
    @ExcelProperty(value = "直播平台", converter = DictConvert.class)
    @DictFormat("erp_live_platform")
    private String livePlatform;

    @Schema(description = "直播价格", example = "100.00")
    @ExcelProperty("直播价格")
    private String livePrice;

    @Schema(description = "直播佣金", example = "10.00")
    @ExcelProperty("直播佣金")
    private BigDecimal liveCommission;

    @Schema(description = "公开佣金", example = "8.00")
    @ExcelProperty("公开佣金")
    private BigDecimal publicCommission;

    @Schema(description = "返点佣金", example = "5.00")
    @ExcelProperty("服务佣金")
    private BigDecimal rebateCommission;

    @Schema(description = "寄样日期", example = "2023-01-01")
    @ExcelProperty("寄样日期")
    private LocalDate sampleSendDate;

    @Schema(description = "开播日期", example = "2023-01-05")
    @ExcelProperty("开播日期")
    private LocalDate liveStartDate;

    @Schema(description = "开播销量", example = "100")
    @ExcelProperty("开播销量")
    private Integer liveSales;

    @Schema(description = "复播日期", example = "2023-01-10")
    @ExcelProperty("复播日期")
    private LocalDate repeatLiveDate;

    @Schema(description = "复盘状态", example = "已完成")
    @ExcelProperty("复盘状态")
    private String reviewStatus;

    @Schema(description = "货盘状态", example = "上架")
    @ExcelProperty(value = "货盘状态", converter = DictConvert.class)
    @DictFormat("erp_live_status")
    private String liveStatus;


    @Schema(description = "创建者")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
