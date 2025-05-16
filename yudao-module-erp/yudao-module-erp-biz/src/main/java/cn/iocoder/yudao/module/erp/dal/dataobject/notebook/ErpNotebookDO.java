package cn.iocoder.yudao.module.erp.dal.dataobject.notebook;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP 记事本 DO
 */
@TableName("erp_notebook")
@KeySequence("erp_notebook_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpNotebookDO extends BaseDO {

    /**
     * 记事本编号
     */
    @TableId
    private Long id;

    /**
     * 编号
     */
    private String no;

    /**
     * 轮播图片
     */
    private String carouselImage;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务状态
     */
    private Integer taskStatus;

    /**
     * 任务人员
     */
    private String taskPerson;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 租户编号
     */
    private Long tenantId;
}