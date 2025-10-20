package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 私播复盘导出 VO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ExcelIgnoreUnannotated
public class ErpPrivateBroadcastingReviewExportVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SBF001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "私播货盘编号", example = "PB001")
    @ExcelProperty("私播货盘编号")
    private String privateBroadcastingNo;

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

    @Schema(description = "客户名称", example = "张三")
    @ExcelProperty("客户名称")
    private String customerName;

    @Schema(description = "直播价格", example = "100.00")
    @ExcelProperty("直播价格")
    private BigDecimal livePrice;

    @Schema(description = "产品裸价", example = "50.00")
    @ExcelProperty("产品裸价")
    private BigDecimal productNakedPrice;

    @Schema(description = "快递费用", example = "10.00")
    @ExcelProperty("快递费用")
    private BigDecimal expressFee;

    @Schema(description = "代发价格", example = "60.00")
    @ExcelProperty("代发价格")
    private BigDecimal dropshipPrice;

    @Schema(description = "寄样日期", example = "2023-01-01")
    @ExcelProperty("寄样日期")
    private LocalDateTime sampleSendDate;

    @Schema(description = "开团日期", example = "2023-01-10")
    @ExcelProperty("开团日期")
    private LocalDateTime groupStartDate;

    @Schema(description = "开团销量", example = "100")
    @ExcelProperty("开团销量")
    private Integer groupSales;

    @Schema(description = "复团日期", example = "2023-02-01")
    @ExcelProperty("复团日期")
    private LocalDateTime repeatGroupDate;

    @Schema(description = "复盘状态", example = "已完成")
    @ExcelProperty("复盘状态")
    private String reviewStatus;

    @Schema(description = "货盘状态", example = "已审核")
    @ExcelProperty(value = "货盘状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRIVATE_STATUS)
    private String privateStatus;


    @ExcelProperty(value = "创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
