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
     * 产品条码
     */
    private String barCode;

    /**
     * 产品图片
     */
    private String image;

    /**
     * 产品简称
     */
    private String productShortName;

    /**
     * 进货编码
     */
    private String purchaseCode;

    /**
     * 商品备案
     */
    private String productRecord;

    /**
     * 产品分类编号
     */
    private Long categoryId;

    /**
     * 单位编号
     */
    private Long unitId;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 产品状态
     */
    private Integer status;

    /**
     * 产品备注
     */
    private String remark;

    /**
     * 产品卖点
     */
    private String productSellingPoints;

    /**
     * 产品规格
     */
    private String standard;

    /**
     * 产品长宽高
     */
    private String productDimensions;

    /**
     * 箱规
     */
    private String cartonSpecifications;

    /**
     * 箱规长宽高
     */
    private String cartonDimensions;

    /**
     * 箱规重量
     */
    private Double cartonWeight;

    /**
     * 现货数量
     */
    private Integer availableStockQuantity;

    /**
     * 包材数量
     */
    private Integer packagingMaterialQuantity;

    /**
     * 返单时效
     */
    private LocalDateTime orderReplenishmentLeadTime;

    /**
     * 发货地址
     */
    private String shippingAddress;

    /**
     * 退货地址
     */
    private String returnAddress;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 不发货地
     */
    private String nonshippingArea;

    /**
     * 加邮区域
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
     * 供应商
     */
    private String supplier;

    /**
     * 代发单价
     */
    private BigDecimal dropshippingUnitPrice;

    /**
     * 批发单价
     */
    private BigDecimal wholesaleUnitPrice;

    /**
     * 基础运费
     */
    private BigDecimal baseShippingFee;

    /**
     * 采购详情
     */
    private String purchaseDetails;

    /**
     * 采购备注
     */
    private String purchaseNote;

    /**
     * 销售详情
     */
    private String salesDetails;

    /**
     * 销售备注
     */
    private String salesNote;

    /**
     * 一级代发单价
     */
    private BigDecimal levelOneDropshippingPrice;

    /**
     * 二级代发单价
     */
    private BigDecimal levelTwoDropshippingPrice;

    /**
     * 一级批发单价
     */
    private BigDecimal levelOneWholesalePrice;

    /**
     * 二级批发单价
     */
    private BigDecimal levelTwoWholesalePrice;

    /**
     * 代发运费类型
     */
    private Boolean dropshippingShippingFeeType;

    /**
     * 固定运费
     */
    private BigDecimal fixedShippingFee;

    /**
     * 首件数量
     */
    private Integer firstItemQuantity;

    /**
     * 首件价格
     */
    private BigDecimal firstItemPrice;

    /**
     * 续件数量
     */
    private Integer additonalItemQuantity;

    /**
     * 续件价格
     */
    private BigDecimal additonalItemPrice;

    /**
     * 首重重量
     */
    private BigDecimal firstWeight;

    /**
     * 首重价格
     */
    private BigDecimal firstWeightPrice;

    /**
     * 首批生产日期
     */
    private LocalDateTime firstBatchProductionDate;

    /**
     * 续重重量
     */
    private BigDecimal additionalWeight;

    /**
     * 续重价格
     */
    private BigDecimal additionalWeightPrice;

    /**
     * 保质期天数
     */
    private Integer expiryDay;

    /**
     * 基础重量（kg）
     */
    private BigDecimal weight;

    /**
     * 采购价格，单位：元
     */
    private BigDecimal purchasePrice;

    /**
     * 销售价格，单位：元
     */
    private BigDecimal salePrice;

    /**
     * 最低价格，单位：元
     */
    private BigDecimal minPrice;

}