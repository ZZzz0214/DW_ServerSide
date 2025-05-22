package cn.iocoder.yudao.module.erp.dal.dataobject.distribution;



import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 代发销售 DO
 *
 * @author 芋道源码
 */
@TableName("erp_distribution_sale")
@KeySequence("erp_distribution_sale_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpDistributionSaleDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 关联代发基础表
     */
    private Long baseId;

    /**
     * 关联销售价格表
     */
    private Long salePriceId;

    /**
     * 销售人员
     */
    private String salesperson;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 出货单价
     */
    private BigDecimal salePrice;

    /**
     * 出货运费
     */
    private BigDecimal shippingFee;

    /**
     * 其他费用
     */
    private BigDecimal otherFees;

    /**
     * 出货总额
     */
    private BigDecimal totalSaleAmount;

    /**
     * 租户编号
     */
    private Long tenantId;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 出货备注信息
     */
    private String saleRemark;

    /**
     * 销售售后状态
     */
    private Integer saleAfterSalesStatus;

    /**
     * 销售售后情况
     */
    private String saleAfterSalesSituation;

    /**
     * 销售售后金额
     */
    private BigDecimal saleAfterSalesAmount;

    /**
     * 销售售后时间
     */
    private LocalDateTime saleAfterSalesTime;

    /**
     * 销售审核状态
     */
    private Integer saleAuditStatus;

        /**
     * 中转人员
     */
    private String transferPerson;
}
