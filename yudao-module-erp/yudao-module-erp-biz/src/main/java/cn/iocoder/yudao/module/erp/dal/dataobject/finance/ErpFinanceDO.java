package cn.iocoder.yudao.module.erp.dal.dataobject.finance;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ERP 财务表
 */
@TableName("erp_finance")
@KeySequence("erp_finance_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceDO extends BaseDO {

    /**
     * 财务记录ID（主键，自增）
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
     * 账单名称
     */
    private String billName;

    /**
     * 收付金额
     */
    private BigDecimal amount;

    /**
     * 收入支出（1：收入，2：支出）
     */
    private Integer incomeExpense;

    /**
     * 收付类目
     */
    private String category;

    /**
     * 收付账号
     */
    private String account;

    /**
     * 账单状态（1：待处理，2：已完成，3：已取消等）
     */
    private Integer status;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 下单日期
     */
    private LocalDate orderDate;

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
} 