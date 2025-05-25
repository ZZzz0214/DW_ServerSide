package cn.iocoder.yudao.module.erp.dal.dataobject.dropship;



import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP 代发辅助表
 */
@TableName("erp_dropship_assist")
@KeySequence("erp_dropship_assist_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpDropshipAssistDO extends BaseDO {

    /**
     * 代发辅助表编号
     */
    @TableId
    private Long id;

    /**
     * 编号
     */
    private String no;

    /**
     * 原表商品
     */
    private String originalProduct;

    /**
     * 原表规格
     */
    private String originalSpec;

    /**
     * 原表数量
     */
    private Integer originalQuantity;

    /**
     * 组品编号
     */
    private String comboProductId;

    /**
     * 产品规格
     */
    private String productSpec;

    /**
     * 产品数量
     */
    private Integer productQuantity;

    /**
     * 租户编号
     */
    private Long tenantId;
}