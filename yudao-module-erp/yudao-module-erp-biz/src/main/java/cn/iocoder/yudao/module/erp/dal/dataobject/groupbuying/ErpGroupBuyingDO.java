package cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 团购货盘 DO
 */
@TableName("erp_group_buying")
@KeySequence("erp_group_buying_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpGroupBuyingDO extends BaseDO {

    /**
     * 团购货盘编号
     */
    @TableId
    private Long id;

    /**
     * 编号
     */
    private String no;

    /**
     * 产品图片（多张，逗号分隔）
     */
    private String productImage;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 产品分类编号
     */
    private Long categoryId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品规格
     */
    private String productSpec;

    /**
     * 产品SKU
     */
    private String productSku;

    /**
     * 市场价格
     */
    private BigDecimal marketPrice;

    /**
     * 保质日期
     */
    private LocalDateTime shelfLife;

    /**
     * 产品库存
     */
    private Integer productStock;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 核心价格
     */
    private BigDecimal corePrice;

    /**
     * 分发价格
     */
    private BigDecimal distributionPrice;

    /**
     * 供团价格
     */
    private BigDecimal supplyGroupPrice;

    /**
     * 帮卖佣金
     */
    private BigDecimal sellingCommission;

    /**
     * 开团价格
     */
    private BigDecimal groupPrice;

    /**
     * 渠道毛利
     */
    private BigDecimal channelProfit;

    /**
     * 开团机制
     */
    private String groupMechanism;

    /**
     * 快递费用
     */
    private BigDecimal expressFee;

    /**
     * 天猫京东
     */
    private String tmallJd;

    /**
     * 公域数据
     */
    private String publicData;

    /**
     * 私域数据
     */
    private String privateData;

    /**
     * 品牌背书
     */
    private String brandEndorsement;

    /**
     * 竞品分析
     */
    private String competitiveAnalysis;

    /**
     * 快递公司
     */
    private String expressCompany;

    /**
     * 发货时效
     */
    private String shippingTime;

    /**
     * 发货地区
     */
    private String shippingArea;

    /**
     * 货盘状态
     */
    private String status;

    /**
     * 租户编号
     */
    private Long tenantId;

    // ==================== 新增字段：资料信息（富文本+文件上传，JSON格式） ====================

    /**
     * 主图（富文本+文件，JSON格式）
     */
    private String mainImage;

    /**
     * 详情（富文本+文件，JSON格式）
     */
    private String detailInfo;

    /**
     * SKU图（富文本+文件，JSON格式）
     */
    private String skuImage;

    /**
     * 基础笔记（富文本+文件，JSON格式）
     */
    private String basicNotes;

    /**
     * 升级笔记（富文本+文件，JSON格式）
     */
    private String upgradeNotes;

    /**
     * 社群推广（富文本+文件，JSON格式）
     */
    private String communityPromotion;

    /**
     * 详细信息（富文本+文件，JSON格式）
     */
    private String detailedInfo;

    /**
     * 资质（富文本+文件，JSON格式）
     */
    private String qualification;

    /**
     * 卖点成分（富文本+文件，JSON格式）
     */
    private String sellingPointsIngredients;

    /**
     * 背书（富文本+文件，JSON格式）
     */
    private String endorsement;

    /**
     * 实拍（富文本+文件，JSON格式）
     */
    private String actualPhotos;

    /**
     * 六面图（富文本+文件，JSON格式）
     */
    private String sixSideImages;

    /**
     * 打包图（富文本+文件，JSON格式）
     */
    private String packagingImages;
}