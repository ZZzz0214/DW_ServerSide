package cn.iocoder.yudao.module.erp.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP 供应商 DO
 *
 * @author 芋道源码
 */
@TableName("erp_supplier")
@KeySequence("erp_supplier_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpSupplierDO extends BaseDO {

    /**
     * 供应商编号
     */
    @TableId
    private Long id;
    /**
     * 供应商编号（业务编号）
     */
    private String no;
    /**
     * 供应商名称
     */
    private String name;
    /**
     * 收件姓名
     */
    private String receiverName;
    /**
     * 联系电话
     */
    private String telephone;
    /**
     * 详细地址
     */
    private String address;
    /**
     * 微信账号
     */
    private String wechatAccount;
    /**
     * 支付宝号
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