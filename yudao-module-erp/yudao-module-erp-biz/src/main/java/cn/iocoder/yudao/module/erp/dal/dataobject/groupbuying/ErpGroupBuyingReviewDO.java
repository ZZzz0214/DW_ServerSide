package cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ERP 团购复盘 DO
 */
@TableName("erp_group_buying_review")
@KeySequence("erp_group_buying_review_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpGroupBuyingReviewDO extends BaseDO {

    /**
     * 团购复盘编号
     */
    @TableId
    private Long id;

    /**
     * 编号
     */
    private String no;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 客户编号
     */
    private Long customerId;

    /**
     * 团购货盘表ID
     */
    private Long groupBuyingId;

    /**
     * 供团价格
     */
    private BigDecimal supplyGroupPrice;

    /**
     * 寄样日期
     */
    private LocalDate sampleSendDate;

    /**
     * 开团日期
     */
    private LocalDate groupStartDate;

    /**
     * 开团销量
     */
    private Integer groupSales;

    /**
     * 复团日期
     */
    private LocalDate repeatGroupDate;

    /**
     * 复团销量
     */
    private Integer repeatGroupSales;

    /**
     * 租户编号
     */
    private Long tenantId;
}