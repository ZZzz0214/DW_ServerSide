package cn.iocoder.yudao.module.erp.dal.dataobject.wholesale;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP 批发基础 DO
 *
 * @author 芋道源码
 */
@TableName("erp_wholesale_base")
@KeySequence("erp_wholesale_base_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpWholesaleBaseDO extends BaseDO {

    /** 订单编号 */
    @TableId
    private Long id;

    /** 订单号 */
    private String no;

    /** 收件人姓名 */
    private String receiverName;

    /** 收件人电话 */
    private String receiverPhone;

    /** 收件地址 */
    private String receiverAddress;

    /** 售后状况 */
    private Integer afterSalesStatus;

    /** 备注信息 */
    private String remark;

    /** 组品编号 */
    private Long comboProductId;

    /** 产品名称 */
    private String productName;

    /** 发货编码 */
    private String shippingCode;

    /** 产品数量 */
    private Integer productQuantity;

    /** 租户编号 */
    private Long tenantId;

    /** 是否删除 */
    private Boolean deleted;
        /**
     * 批发状态
     *
     * 枚举
     */
    private Integer status;

    /** 产品规格 */
    private String productSpecification;

    /** 订单号 */
    private String orderNumber;

    /** 物流单号 */
    private String logisticsNumber;
}