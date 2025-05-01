package cn.iocoder.yudao.module.erp.dal.dataobject.distribution;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP 代发基础 DO
 *
 * @author 芋道源码
 */
@TableName("erp_distribution_base")
@KeySequence("erp_distribution_base_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpDistributionBaseDO extends BaseDO {

    /**
     * 订单编号
     */
    @TableId
    private Long id;

    /**
     * 订单号
     */
    private String no;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 物流单号
     */
    private String trackingNumber;

    /**
     * 收件姓名
     */
    private String receiverName;

    /**
     * 联系电话
     */
    private String receiverPhone;

    /**
     * 详细地址
     */
    private String receiverAddress;

    /**
     * 原表商品
     */
    private String originalProductName;

    /**
     * 原表规格
     */
    private String originalStandard;

    /**
     * 原表数量
     */
    private Integer originalQuantity;

    /**
     * 售后状况
     */
    private Integer afterSalesStatus;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 组品编号
     */
    private Long comboProductId;

    /**
     * 产品名称（->组品编号）
     */
    private String productName;

    /**
     * 发货编码（->组品编号）
     */
    private String shippingCode;

    /**
     * 产品数量
     */
    private Integer productQuantity;

    /**
     * 租户编号
     */
    private Long tenantId;

    /**
     * 是否删除
     */
    private Boolean deleted;
    /**
     * 代发状态
     *
     * 枚举
     */
    private Integer status;
    /**
     * 产品规格
     */
    private String productSpecification;

    /**
     * 订单号
     */
    private String orderNumber;
}
