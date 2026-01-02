package cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 直播货盘新增/修改 Request VO")
@Data
public class ErpLiveBroadcastingSaveReqVO {

    @Schema(description = "直播货盘编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ZBHP001")
    private String no;

    @Schema(description = "产品图片", example = "https://example.com/image.jpg")
    private String productImage;

    @Schema(description = "品牌名称", example = "品牌A")
    private String brandName;

    @Schema(description = "产品分类编号", example = "1")
    private Long categoryId;

    @Schema(description = "产品名称", example = "产品A")
    private String productName;

    @Schema(description = "产品规格", example = "500g/瓶")
    private String productSpec;

    @Schema(description = "产品SKU", example = "SKU001")
    private String productSku;

    @Schema(description = "市场价格", example = "100.00")
    private BigDecimal marketPrice;

    @Schema(description = "保质日期", example = "2023-12-31")
    private LocalDateTime shelfLife;

    @Schema(description = "产品库存", example = "100")
    private Integer productStock;

    @Schema(description = "核心卖点", example = "天然无添加")
    private String coreSellingPoint;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;

    @Schema(description = "直播价格", example = "80.00")
    @NotEmpty(message = "直播价格不能为空")
    private String livePrice;

    @Schema(description = "直播佣金", example = "10.00")
    @NotNull(message = "直播佣金不能为空")
    private BigDecimal liveCommission;

    @Schema(description = "公开佣金", example = "8.00")
    private BigDecimal publicCommission;

    @Schema(description = "返点佣金", example = "5.00")
    private BigDecimal rebateCommission;

    @Schema(description = "快递公司", example = "顺丰")
    private String expressCompany;

    @Schema(description = "发货时效", example = "48小时内")
    private String shippingTime;

    @Schema(description = "发货地区", example = "浙江杭州")
    private String shippingArea;

    @Schema(description = "直播货盘状态", example = "未设置")
    private String liveStatus;

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
