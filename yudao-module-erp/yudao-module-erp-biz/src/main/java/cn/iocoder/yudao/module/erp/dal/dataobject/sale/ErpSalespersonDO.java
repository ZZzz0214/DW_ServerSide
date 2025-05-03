package cn.iocoder.yudao.module.erp.dal.dataobject.sale;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP 销售人员 DO
 *
 * @author 芋道源码
 */
@TableName("erp_salesperson")
@KeySequence("erp_salesperson_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpSalespersonDO extends BaseDO {

    /**
     * 销售人员编号
     */
    @TableId
    private Long id;
    /**
     * 销售人员姓名
     */
    private String salespersonName;
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
