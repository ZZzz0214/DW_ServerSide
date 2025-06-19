package cn.iocoder.yudao.module.erp.controller.admin.notebook.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 记事本分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpNotebookPageReqVO extends PageParam {

    @Schema(description = "编号", example = "NB001")
    private String no;

    @Schema(description = "任务名称", example = "开发任务")
    private String taskName;

    @Schema(description = "任务状态", example = "1")
    private Integer taskStatus;

    @Schema(description = "任务人员", example = "张三")
    private String taskPerson;

    @Schema(description = "创建人员", example = "李四")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
