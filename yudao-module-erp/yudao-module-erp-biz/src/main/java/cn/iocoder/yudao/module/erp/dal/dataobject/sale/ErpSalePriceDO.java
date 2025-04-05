package cn.iocoder.yudao.module.erp.dal.dataobject.sale;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * ERP 销售价格 DO
 *
 * @author 芋道源码
 */
@TableName("erp_sale_price")
@KeySequence("erp_sale_price_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpSalePriceDO extends BaseDO {

    /**
     * 销售价格表编号
     */
    @TableId
    private Long id;

    /**
     * 组品编号
     */
    private Long groupProductId;

    /**
     * 产品图片（->组品编号）
     */
    private String productImage;

    /**
     * 产品名称（->组品编号）
     */
    private String productName;

    /**
     * 产品简称（->组品编号）
     */
    private String productShortName;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 代发单价（单位：元）
     */
    private BigDecimal distributionPrice;

    /**
     * 批发单价（单位：元）
     */
    private BigDecimal wholesalePrice;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 运费类型（0：固定运费，1：按件计费，2：按重计费）
     */
    private Integer shippingFeeType;

    /**
     * 固定运费（单位：元）
     */
    private BigDecimal fixedShippingFee;

    /**
     * 首件数量
     */
    private Integer firstItemQuantity;

    /**
     * 首件价格（单位：元）
     */
    private BigDecimal firstItemPrice;

    /**
     * 续件数量
     */
    private Integer additionalItemQuantity;

    /**
     * 续件价格（单位：元）
     */
    private BigDecimal additionalItemPrice;

    /**
     * 首重重量（单位：kg）
     */
    private BigDecimal firstWeight;

    /**
     * 首重价格（单位：元）
     */
    private BigDecimal firstWeightPrice;

    /**
     * 续重重量（单位：kg）
     */
    private BigDecimal additionalWeight;

    /**
     * 续重价格（单位：元）
     */
    private BigDecimal additionalWeightPrice;

    /**
     * 租户编号
     */
    private Long tenantId;
}
