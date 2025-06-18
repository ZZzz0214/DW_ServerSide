package cn.iocoder.yudao.module.erp.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Date;

/**
 * ERP 采购人员 DO
 *
 * @author 芋道源码
 */
@TableName("erp_purchaser")
@KeySequence("erp_purchaser_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaserDO extends BaseDO {

    /**
     * 采购人员编号
     */
    @TableId
    private Long id;
    /**
     * 采购人员编号（业务编号）
     */
    private String no;
    /**
     * 采购人员姓名
     */
    private String purchaserName;
    /**
     * 收件姓名
     */
    private String receiverName;
    /**
     * 联系电话
     */
    private String contactPhone;
    /**
     * 详细地址
     */
    private String address;
    /**
     * 微信账号
     */
    private String wechatAccount;
    /**
     * 支付宝账号
     */
    private String alipayAccount;
    /**
     * 银行账号
     */
    private String bankAccount;
    /**
     * 备注信息
     */
    private String remark;

}
