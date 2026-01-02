package cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 直播货盘导出 VO")
@Data
@Builder
public class ErpLiveBroadcastingExportVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ZBHP001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "产品图片", example = "https://example.com/image.jpg")
    @ExcelProperty("产品图片")
    private String productImage;

    @Schema(description = "产品图片数据（用于Excel导出）", hidden = true)
    @com.alibaba.excel.annotation.ExcelIgnore
    private byte[] productImageData;

    @ExcelProperty(value = "品牌名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_BRAND)
    private String brandName;

    @Schema(description = "产品分类编号", example = "1")
    @ExcelProperty(value = "产品分类", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_CATEGORY)
    private Long categoryId;

    @Schema(description = "产品名称", example = "产品A")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "产品规格", example = "500g/瓶")
    @ExcelProperty("产品规格")
    private String productSpec;

    @Schema(description = "产品SKU", example = "SKU001")
    @ExcelProperty("产品SKU")
    private String productSku;

    @Schema(description = "市场价格", example = "100.00")
    @ExcelProperty("市场价格")
    private BigDecimal marketPrice;

    @Schema(description = "保质日期", example = "2023-12-31")
    @ExcelProperty("保质日期")
    private LocalDate shelfLife;

    @Schema(description = "产品库存", example = "100")
    @ExcelProperty("产品库存")
    private Integer productStock;

    @Schema(description = "核心卖点", example = "天然无添加")
    @ExcelProperty("核心卖点")
    private String coreSellingPoint;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "直播价格", example = "80.00")
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

    @Schema(description = "快递公司", example = "顺丰")
    @ExcelProperty("快递公司")
    private String expressCompany;

    @Schema(description = "发货时效", example = "48小时内")
    @ExcelProperty("发货时效")
    private String shippingTime;

    @Schema(description = "发货地区", example = "浙江杭州")
    @ExcelProperty("发货地区")
    private String shippingArea;

    @Schema(description = "直播货盘状态", example = "未设置")
    @ExcelProperty(value = "直播货盘状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_LIVE_STATUS)
    private String liveStatus;

    @Schema(description = "创建者")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
