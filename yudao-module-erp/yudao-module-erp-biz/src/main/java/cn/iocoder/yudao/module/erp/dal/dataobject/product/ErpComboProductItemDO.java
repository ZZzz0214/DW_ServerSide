package cn.iocoder.yudao.module.erp.dal.dataobject.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 组合产品项 DO
 *
 * @author 芋道源码
 */
@TableName("erp_combo_product_item")
@KeySequence("erp_combo_product_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpComboProductItemDO extends BaseDO {

    /**
     * 组品项编号（主键，自增）
     */
    @TableId
    private Long id;
    /**
     * 组品编号
     */
    private Long comboProductId;
    /**
     * 单品编号
     */
    private Long itemProductId;
    /**
     * 单品数量
     */
    private Integer itemQuantity;
    /**
     * 创建者
     */
    private String creator;
    /**
     * 更新者
     */
    private String updater;
    /**
     * 是否删除（0：未删除，1：已删除）
     */
    private Boolean deleted;
    /**
     * 租户编号
     */
    private Long tenantId;
}
