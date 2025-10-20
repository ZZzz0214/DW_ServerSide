package cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastingreview;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ERP 直播复盘 DO
 */
@TableName("erp_live_broadcasting_review")
@KeySequence("erp_live_broadcasting_review_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpLiveBroadcastingReviewDO extends BaseDO {

    /**
     * 直播复盘编号
     */
    @TableId
    private Long id;

    /**
     * 编号
     */
    private String no;

    /**
     * 直播货盘表ID
     */
    private Long liveBroadcastingId;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 直播平台
     */
    private String livePlatform;

    /**
     * 直播佣金
     */
    private BigDecimal liveCommission;

    /**
     * 公开佣金
     */
    private BigDecimal publicCommission;

    /**
     * 返点佣金
     */
    private BigDecimal rebateCommission;

    /**
     * 直播价格
     */
    private String livePrice;

    /**
     * 寄样日期
     */
    private LocalDate sampleSendDate;

    /**
     * 开播日期
     */
    private LocalDate liveStartDate;

    /**
     * 开播销量
     */
    private Integer liveSales;

    /**
     * 复播日期
     */
    private LocalDate repeatLiveDate;

    /**
     * 复盘状态
     */
    private String reviewStatus;

    /**
     * 租户编号
     */
    private Long tenantId;
}