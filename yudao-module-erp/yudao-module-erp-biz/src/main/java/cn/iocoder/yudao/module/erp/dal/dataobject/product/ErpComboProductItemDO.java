package cn.iocoder.yudao.module.erp.dal.dataobject.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("erp_combo_product_item")
public class ErpComboProductItemDO extends BaseDO {
    @TableId
    private Long id;
    private Long comboProductId; // 组品编号
    private Long itemProductId; // 单品编号
    private Integer itemQuantity; // 单品数量
    private String creator; // 创建者
    private String updater; // 更新者
    private Long tenantId; // 租户编号

    // Getter 和 Setter 方法
    public Long getComboProductId() {
        return comboProductId;
    }

    public void setComboProductId(Long comboProductId) {
        this.comboProductId = comboProductId;
    }

    public Long getItemProductId() {
        return itemProductId;
    }

    public void setItemProductId(Long itemProductId) {
        this.itemProductId = itemProductId;
    }

    public Integer getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(Integer itemQuantity) {
        this.itemQuantity = itemQuantity;
    }
}