package cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 团购货盘新增/修改 Request VO")
@Data
public class ErpGroupBuyingSaveReqVO {

    @Schema(description = "团购货盘编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "GB001")
    private String no;

    @Schema(description = "产品图片", example = "https://example.com/image.jpg")
    private String productImage;

    @Schema(description = "品牌名称", example = "品牌A")
    private String brandName;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品A")
    @NotNull(message = "产品名称不能为空")
    private String productName;

    @Schema(description = "产品规格", example = "标准规格")
    private String productSpec;

    @Schema(description = "产品SKU", example = "SKU001")
    private String productSku;

    @Schema(description = "市场价格", example = "100.00")
    private BigDecimal marketPrice;

    @Schema(description = "保质日期", example = "2023-12-31")
    private LocalDateTime shelfLife;

    @Schema(description = "产品库存", example = "100")
    private Integer productStock;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;

    @Schema(description = "核心价格", example = "80.00")
    private BigDecimal corePrice;

    @Schema(description = "分发价格", example = "85.00")
    private BigDecimal distributionPrice;

    @Schema(description = "供团价格", example = "90.00")
    private BigDecimal supplyGroupPrice;

    @Schema(description = "帮卖佣金", example = "10.00")
    private BigDecimal sellingCommission;

    @Schema(description = "开团价格", example = "95.00")
    private BigDecimal groupPrice;

    @Schema(description = "渠道毛利", example = "15.00")
    private BigDecimal channelProfit;

    @Schema(description = "开团机制", example = "满100人开团")
    private String groupMechanism;

    @Schema(description = "快递费用", example = "10.00")
    private BigDecimal expressFee;

    @Schema(description = "天猫京东", example = "天猫旗舰店")
    private String tmallJd;

    @Schema(description = "公域数据", example = "公域数据内容")
    private String publicData;

    @Schema(description = "私域数据", example = "私域数据内容")
    private String privateData;

    @Schema(description = "品牌背书", example = "品牌背书内容")
    private String brandEndorsement;

    @Schema(description = "竞品分析", example = "竞品分析内容")
    private String competitiveAnalysis;

    @Schema(description = "快递公司", example = "顺丰快递")
    private String expressCompany;

    @Schema(description = "发货时效", example = "48小时内")
    private String shippingTime;

    @Schema(description = "发货地区", example = "广东省深圳市")
    private String shippingArea;
}
