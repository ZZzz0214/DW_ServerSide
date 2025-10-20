package cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastingreview;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ERP 私播复盘 DO
 */
@TableName("erp_private_broadcasting_review")
@KeySequence("erp_private_broadcasting_review_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPrivateBroadcastingReviewDO extends BaseDO {

    /**
     * 私播复盘编号
     */
    @TableId
    private Long id;

    /**
     * 编号
     */
    private String no;

    /**
     * 私播货盘表ID
     */
    private Long privateBroadcastingId;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 产品裸价
     */
    private BigDecimal productNakedPrice;

    /**
     * 快递费用
     */
    private BigDecimal expressFee;

    /**
     * 代发价格
     */
    private BigDecimal dropshipPrice;

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
     * 复盘状态
     */
    private String reviewStatus;

    /**
     * 直播价格
     */
    private BigDecimal livePrice;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 租户编号
     */
    private Long tenantId;
}
