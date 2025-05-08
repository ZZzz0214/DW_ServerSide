package cn.iocoder.yudao.module.erp.dal.dataobject.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 组合产品 DO
 *
 * @author 芋道源码
 */
@TableName("erp_combo_product")
@KeySequence("erp_combo_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpComboProductDO extends BaseDO {

    /**
     * 组品编号（主键，自增）
     */
    @TableId
    private Long id;

    /**
     * 组合产品编号(业务编号)
     */
    private String no;

    /**
     * 组合产品名称
     */
    private String name;
    /**
     * 产品简称
     */
    private String shortName;
    /**
     * 产品图片
     */
    private String image;
    /**
     * 发货编码
     */
    private String shippingCode;
    /**
     * 产品重量（单位：kg）
     */
    private BigDecimal weight;
    /**
     * 采购人员
     */
    private String purchaser;
    /**
     * 供应商名
     */
    private String supplier;
    /**
     * 采购单价（单位：元）
     */
    private BigDecimal purchasePrice;
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
//    /**
//     * 首件数量
//     */
//    private Integer firstItemQuantity;
//    /**
//     * 首件价格（单位：元）
//     */
//    private BigDecimal firstItemPrice;
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
     * 产品数量（组合产品中包含的单品总数）
     */
    private Integer totalQuantity;
    /**
     * 创建者
     */
    private String creator;
    /**
     * 更新者
     */
    private String updater;
    /**
     * 是否删除（0：未删除，1：已删除）
     */
    private Boolean deleted;
    /**
     * 租户编号
     */
    private Long tenantId;
    /**
     * 产品状态
     */
    private Integer status;
}
