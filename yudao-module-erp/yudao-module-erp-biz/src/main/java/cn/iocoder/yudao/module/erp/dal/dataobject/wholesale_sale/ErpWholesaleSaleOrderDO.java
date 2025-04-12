package cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_sale;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 批发销售订单 DO
 */
@TableName(value = "erp_wholesale_sale_order")
@KeySequence("erp_wholesale_sale_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpWholesaleSaleOrderDO extends BaseDO {

    /**
     * 订单编号
     */
    @TableId
    private Long id;
    /**
     * 批发销售单编号
     */
    private String no;
    /**
     * 订单号
     */
    private String orderNumber;
    /**
     * 销售状态
     */
    private Integer status;
    /**
     * 客户编号
     */
    private Long customerId;
    /**
     * 结算账户编号
     */
    private Long accountId;
    /**
     * 销售用户编号
     */
    private Long saleUserId;
    /**
     * 下单时间
     */
    private LocalDateTime orderTime;
    /**
     * 物流公司
     */
    private String logisticsCompany;
    /**
     * 物流单号
     */
    private String logisticsNumber;
    /**
     * 收件姓名
     */
    private String consigneeName;
    /**
     * 联系电话
     */
    private String contactNumber;
    /**
     * 详细地址
     */
    private String detailedAddress;
    /**
     * 售后状况
     */
    private String afterSaleStatus;
    /**
     * 备注信息
     */
    private String remark;
    /**
     * 合计数量
     */
    private BigDecimal totalCount;
    /**
     * 合计价格，单位：元
     */
    private BigDecimal totalPrice;
    /**
     * 合计产品价格，单位：元
     */
    private BigDecimal totalProductPrice;
    /**
     * 合计税额，单位：元
     */
    private BigDecimal totalTaxPrice;
    /**
     * 优惠率，百分比
     */
    private BigDecimal discountPercent;
    /**
     * 优惠金额，单位：元
     */
    private BigDecimal discountPrice;
    /**
     * 定金金额，单位：元
     */
    private BigDecimal depositPrice;
    /**
     * 物流费用合计
     */
    private BigDecimal totalLogisticsFee;
    /**
     * 其他费用合计
     */
    private BigDecimal totalOtherFees;
    /**
     * 销售总额（合计）
     */
    private BigDecimal totalSaleAmount;
    /**
     * 附件地址
     */
    private String fileUrl;
    /**
     * 销售出库数量
     */
    private BigDecimal outCount;
    /**
     * 销售退货数量
     */
    private BigDecimal returnCount;
    /**
     * 货拉拉费用合计
     */
    private BigDecimal totalHulalaFee;
}
