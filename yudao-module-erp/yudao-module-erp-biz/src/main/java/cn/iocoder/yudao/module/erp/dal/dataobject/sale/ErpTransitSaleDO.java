package cn.iocoder.yudao.module.erp.dal.dataobject.sale;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 中转销售 DO
 *
 * @author 芋道源码
 */
@TableName("erp_transit_sale")
@KeySequence("erp_transit_sale_seq") 
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpTransitSaleDO extends BaseDO {

    /**
     * 中转销售表id（主键，自增）
     */
    @TableId
    private Long id;

    /**
     * 编号
     */
    private String no;

    /**
     * 组品编号
     */
    private Long groupProductId;

    /**
     * 中转人员
     */
    private String transitPerson;

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