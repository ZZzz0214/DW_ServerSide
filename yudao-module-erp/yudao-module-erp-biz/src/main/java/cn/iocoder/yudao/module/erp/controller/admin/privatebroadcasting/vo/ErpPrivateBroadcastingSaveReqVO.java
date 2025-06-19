package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 私播货盘新增/修改 Request VO")
@Data
public class ErpPrivateBroadcastingSaveReqVO {

    @Schema(description = "私播货盘编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "PB001")
    private String no;

    @Schema(description = "产品图片", example = "https://example.com/image.jpg")
    private String productImage;

    @Schema(description = "品牌名称", example = "品牌A")
    private String brandName;

    @Schema(description = "产品名称", example = "产品A")
    @NotNull(message = "产品名称不能为空")
    private String productName;

    @Schema(description = "产品规格", example = "500g/盒")
    private String productSpec;

    @Schema(description = "产品SKU", example = "SKU123")
    private String productSku;

    @Schema(description = "市场价格", example = "100.00")
    private BigDecimal marketPrice;

    @Schema(description = "保质日期", example = "2023-12-31")
    private String shelfLife;

    @Schema(description = "产品库存", example = "100")
    @NotNull(message = "产品库存不能为空")
    private Integer productStock;

    @Schema(description = "直播价格", example = "80.00")
    @NotNull(message = "直播价格不能为空")
    private BigDecimal livePrice;

    @Schema(description = "产品裸价", example = "50.00")
    private BigDecimal productNakedPrice;

    @Schema(description = "快递费用", example = "10.00")
    private BigDecimal expressFee;

    @Schema(description = "代发价格", example = "60.00")
    private BigDecimal dropshipPrice;

    @Schema(description = "公域链接", example = "https://example.com/link")
    private String publicLink;

    @Schema(description = "核心卖点", example = "高性价比")
    private String coreSellingPoint;

    @Schema(description = "快递公司", example = "顺丰")
    private String expressCompany;

    @Schema(description = "发货时效", example = "24小时内")
    private String shippingTime;

    @Schema(description = "发货地区", example = "浙江省杭州市")
    private String shippingArea;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;

    @Schema(description = "私播货盘状态", example = "未设置")
    private String privateStatus;
}