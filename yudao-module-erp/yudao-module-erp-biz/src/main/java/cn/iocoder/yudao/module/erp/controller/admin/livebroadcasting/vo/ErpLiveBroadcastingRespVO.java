package cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 直播货盘 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpLiveBroadcastingRespVO {

    @Schema(description = "直播货盘编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("直播货盘编号")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ZBHP001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "产品图片", example = "https://example.com/image.jpg")
    @ExcelProperty("产品图片")
    private String productImage;

    @Schema(description = "品牌名称", example = "品牌A")
    @ExcelProperty("品牌名称")
    private String brandName;

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
    private LocalDateTime shelfLife;

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
    @ExcelProperty("返点佣金")
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
    @ExcelProperty("直播货盘状态")
    private String liveStatus;

    @Schema(description = "创建者")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    // ==================== 新增字段：资料信息（富文本+文件上传，JSON格式） ====================

    @Schema(description = "主图（富文本+文件，JSON格式）")
    private String mainImage;

    @Schema(description = "详情（富文本+文件，JSON格式）")
    private String detailInfo;

    @Schema(description = "SKU图（富文本+文件，JSON格式）")
    private String skuImage;

    @Schema(description = "基础笔记（富文本+文件，JSON格式）")
    private String basicNotes;

    @Schema(description = "升级笔记（富文本+文件，JSON格式）")
    private String upgradeNotes;

    @Schema(description = "社群推广（富文本+文件，JSON格式）")
    private String communityPromotion;

    @Schema(description = "详细信息（富文本+文件，JSON格式）")
    private String detailedInfo;

    @Schema(description = "资质（富文本+文件，JSON格式）")
    private String qualification;

    @Schema(description = "卖点成分（富文本+文件，JSON格式）")
    private String sellingPointsIngredients;

    @Schema(description = "背书（富文本+文件，JSON格式）")
    private String endorsement;

    @Schema(description = "实拍（富文本+文件，JSON格式）")
    private String actualPhotos;

    @Schema(description = "六面图（富文本+文件，JSON格式）")
    private String sixSideImages;

    @Schema(description = "打包图（富文本+文件，JSON格式）")
    private String packagingImages;
}
