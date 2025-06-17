package cn.iocoder.yudao.module.erp.dal.dataobject.finance;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 财务充值记录表
 */
@TableName("erp_finance_amount")
@KeySequence("erp_finance_amount_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceAmountDO extends BaseDO {

    /**
     * 充值记录ID（主键，自增）
     */
    @TableId
    private Long id;

    /**
     * 编号
     */
    private String no;

    /**
     * 轮播图片（可存储多张图片路径，用逗号分隔）
     */
    private String carouselImages;

    /**
     * 充值渠道（微信、支付宝、银行卡）
     */
    private String channelType;

    /**
     * 充值金额
     */
    private BigDecimal amount;

    /**
     * 操作类型（1：充值，2：消费）
     */
    private Integer operationType;

    /**
     * 充值前余额
     */
    private BigDecimal beforeBalance;

    /**
     * 充值后余额
     */
    private BigDecimal afterBalance;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 审核状态（0：待审核，1：已审核，2：审核拒绝）
     */
    private Integer auditStatus;

    /**
     * 审核人
     */
    private String auditor;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 租户编号
     */
    private Long tenantId;

    // 为了兼容现有代码，保留原有字段但标记为废弃
    /**
     * @deprecated 使用新的单条记录设计，该字段已废弃
     */
    @Deprecated
    private BigDecimal wechatRecharge;

    /**
     * @deprecated 使用新的单条记录设计，该字段已废弃
     */
    @Deprecated
    private BigDecimal alipayRecharge;

    /**
     * @deprecated 使用新的单条记录设计，该字段已废弃
     */
    @Deprecated
    private BigDecimal bankCardRecharge;

    /**
     * @deprecated 使用新的单条记录设计，该字段已废弃
     */
    @Deprecated
    private BigDecimal wechatBalance;

    /**
     * @deprecated 使用新的单条记录设计，该字段已废弃
     */
    @Deprecated
    private BigDecimal alipayBalance;

    /**
     * @deprecated 使用新的单条记录设计，该字段已废弃
     */
    @Deprecated
    private BigDecimal bankCardBalance;
} 