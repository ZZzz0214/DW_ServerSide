package cn.iocoder.yudao.module.erp.dal.dataobject.sample;


import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP 样品 DO
 */
@TableName("erp_sample")
@KeySequence("erp_sample_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpSampleDO extends BaseDO {

    /**
     * 样品编号
     */
    @TableId
    private Long id;

    /**
     * 编号
     */
    private String no;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 物流单号
     */
    private String logisticsNo;

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
     * 备注信息
     */
    private String remark;

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
     * 客户名称
     */
    private String customerName;

    /**
     * 样品状态
     */
    private Integer sampleStatus;

    /**
     * 参考
     */
    private String reference;

    /**
     * 租户编号
     */
    private Long tenantId;
}
