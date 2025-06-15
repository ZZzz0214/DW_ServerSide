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
     * 产品图片
     */
    private String productImage;

    /**
     * 品牌名称
     */
    private String brandName;

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
}