package cn.iocoder.yudao.module.erp.dal.dataobject.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 产品 DO
 *
 * @author 芋道源码
 */
@TableName("erp_product")
@KeySequence("erp_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductDO extends BaseDO {

    /**
     * 产品编号
     */
    @TableId
    private Long id;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品图片
     */
    private String image;

    /**
     * 产品简称
     */
    private String productShortName;

    /**
     * 发货编码
     */
    private String shippingCode;

    /**
     * 单位编号
     */
    private Long unitId;


    /**
     * 产品规格
     */
    private String standard;

    /**
     * 产品重量（单位：kg）
     */
    private BigDecimal weight;

    /**
     * 产品日期
     */
    private LocalDateTime productionDate;

    /**
     * 保质期天数
     */
    private Integer expiryDay;

    /**
     * 品牌名称
     */
    private String brand;

    /**
     * 产品品类编号
     */
    private Long categoryId;

    /**
     * 产品状态
     */
    private Integer status;

    /**
     * 产品卖点
     */
    private String productSellingPoints;

    /**
     * 条形编号
     */
    private String barCode;

    /**
     * 备案编号
     */
    private String productRecord;

    /**
     * 执行编号
     */
    private String executionCode;

    /**
     * 商标编号
     */
    private String trademarkCode;

    /**
     * 现货数量
     */
    private Integer totalQuantity;

    /**
     * 包材数量
     */
    private Integer packagingMaterialQuantity;

    /**
     * 返单时效
     */
    private LocalDateTime orderReplenishmentLeadTime;

    /**
     * 产品长宽高
     */
    private String productDimensions;

    /**
     * 产品箱规
     */
    private String cartonSpecifications;

    /**
     * 箱长宽高
     */
    private String cartonDimensions;

    /**
     * 箱规重量
     */
    private Double cartonWeight;

    /**
     * 发货地址
     */
    private String shippingAddress;

    /**
     * 退货地址
     */
    private String returnAddress;

    /**
     * 快递公司
     */
    private String logisticsCompany;

    /**
     * 不发货区
     */
    private String nonshippingArea;

    /**
     * 加邮地区
     */
    private String addonShippingArea;

    /**
     * 售后标准
     */
    private String afterSalesStandard;

    /**
     * 售后话术
     */
    private String afterSalesScript;

    /**
     * 公域活动最低价
     */
    private BigDecimal publicDomainEventMinimumPrice;

    /**
     * 直播活动最低价
     */
    private BigDecimal liveStreamingEventMinimunPrice;

    /**
     * 拼多多活动最低价
     */
    private BigDecimal pinduoduoEventMinimumPrice;

    /**
     * 阿里巴巴活动最低价
     */
    private BigDecimal alibabaEventMinimunPrice;

    /**
     * 团购活动最低价
     */
    private BigDecimal groupBuyEventMinimunPrice;

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
     * 对外最低采购单价（单位：元）
     */
    private BigDecimal minPurchasePrice;

    /**
     * 对外最低批发单价（单位：元）
     */
    private BigDecimal minWholesalePrice;

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
}
