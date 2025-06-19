package cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcasting;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 私播货盘 DO
 */
@TableName("erp_private_broadcasting")
@KeySequence("erp_private_broadcasting_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPrivateBroadcastingDO extends BaseDO {

    /**
     * 私播货盘编号
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
     * 直播价格
     */
    private BigDecimal livePrice;

    /**
     * 产品裸价
     */
    private BigDecimal productNakedPrice;

    /**
     * 快递费用
     */
    private BigDecimal expressFee;

    /**
     * 代发价格
     */
    private BigDecimal dropshipPrice;

    /**
     * 公域链接
     */
    private String publicLink;

    /**
     * 核心卖点
     */
    private String coreSellingPoint;

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
     * 备注信息
     */
    private String remark;

    /**
     * 私播货盘状态
     */
    private String privateStatus;

    /**
     * 租户编号
     */
    private Long tenantId;
}