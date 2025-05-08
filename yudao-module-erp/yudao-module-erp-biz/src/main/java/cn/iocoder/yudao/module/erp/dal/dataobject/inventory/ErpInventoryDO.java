package cn.iocoder.yudao.module.erp.dal.dataobject.inventory;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ERP 库存 DO
 */
@TableName("erp_inventory")
@KeySequence("erp_inventory_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpInventoryDO extends BaseDO {

    /**
     * 库存ID
     */
    @TableId
    private Long id;

    /**
     * 库存编号
     */
    private String no;

    /**
     * 单品ID（关联产品库）
     */
    private Long productId;

    /**
     * 现货库存
     */
    private Integer spotInventory;

    /**
     * 剩余库存
     */
    private Integer remainingInventory;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 租户编号
     */
    private Long tenantId;
}