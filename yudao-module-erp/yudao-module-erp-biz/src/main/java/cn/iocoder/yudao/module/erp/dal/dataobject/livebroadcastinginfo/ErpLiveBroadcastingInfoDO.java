package cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastinginfo;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP 直播信息 DO
 */
@TableName("erp_live_broadcasting_info")
@KeySequence("erp_live_broadcasting_info_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpLiveBroadcastingInfoDO extends BaseDO {

    /**
     * 直播信息编号
     */
    @TableId
    private Long id;

    /**
     * 编号
     */
    private String no;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 客户职位
     */
    private String customerPosition;

    /**
     * 客户微信
     */
    private String customerWechat;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 客户属性
     */
    private String customerAttribute;

    /**
     * 客户城市
     */
    private String customerCity;

    /**
     * 客户区县
     */
    private String customerDistrict;

    /**
     * 用户画像
     */
    private String userPortrait;

    /**
     * 招商类目
     */
    private String recruitmentCategory;

    /**
     * 选品标准
     */
    private String selectionCriteria;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 租户编号
     */
    private Long tenantId;
}