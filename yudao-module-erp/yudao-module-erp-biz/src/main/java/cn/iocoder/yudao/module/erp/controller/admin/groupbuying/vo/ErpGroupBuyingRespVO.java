package cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 团购货盘 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpGroupBuyingRespVO {

    @Schema(description = "团购货盘编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "GB001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "产品图片（多张，逗号分隔）", example = "https://example.com/image1.jpg,https://example.com/image2.jpg")
    @ExcelProperty("产品图片")
    private String productImage;

    @Schema(description = "品牌名称", example = "品牌A")
    @ExcelProperty(value = "品牌名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_BRAND)
    private String brandName;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品A")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "产品规格", example = "标准规格")
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

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "核心价格", example = "80.00")
    @ExcelProperty("核心价格")
    private BigDecimal corePrice;

    @Schema(description = "分发价格", example = "85.00")
    @ExcelProperty("分发价格")
    private BigDecimal distributionPrice;

    @Schema(description = "供团价格", example = "90.00")
    @ExcelProperty("供团价格")
    private BigDecimal supplyGroupPrice;

    @Schema(description = "帮卖佣金", example = "10.00")
    @ExcelProperty("帮卖佣金")
    private BigDecimal sellingCommission;

    @Schema(description = "开团价格", example = "95.00")
    @ExcelProperty("开团价格")
    private BigDecimal groupPrice;

    @Schema(description = "渠道毛利", example = "15.00")
    @ExcelProperty("渠道毛利")
    private BigDecimal channelProfit;

    @Schema(description = "开团机制", example = "满100人开团")
    @ExcelProperty("开团机制")
    private String groupMechanism;

    @Schema(description = "快递费用", example = "10.00")
    @ExcelProperty("快递费用")
    private BigDecimal expressFee;

    @Schema(description = "天猫京东", example = "天猫旗舰店")
    @ExcelProperty("天猫京东")
    private String tmallJd;

    @Schema(description = "公域数据", example = "公域数据内容")
    @ExcelProperty("公域数据")
    private String publicData;

    @Schema(description = "私域数据", example = "私域数据内容")
    @ExcelProperty("私域数据")
    private String privateData;

    @Schema(description = "品牌背书", example = "品牌背书内容")
    @ExcelProperty("品牌背书")
    private String brandEndorsement;

    @Schema(description = "竞品分析", example = "竞品分析内容")
    @ExcelProperty("竞品分析")
    private String competitiveAnalysis;

    @Schema(description = "快递公司", example = "顺丰快递")
    @ExcelProperty("快递公司")
    private String expressCompany;

    @Schema(description = "发货时效", example = "48小时内")
    @ExcelProperty("发货时效")
    private String shippingTime;

    @Schema(description = "发货地区", example = "广东省深圳市")
    @ExcelProperty("发货地区")
    private String shippingArea;

    @Schema(description = "货盘状态", example = "上架")
    @ExcelProperty(value = "货盘状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_GROUP_BUYING_STATUS)
    private String status;

    @Schema(description = "创建人员", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
