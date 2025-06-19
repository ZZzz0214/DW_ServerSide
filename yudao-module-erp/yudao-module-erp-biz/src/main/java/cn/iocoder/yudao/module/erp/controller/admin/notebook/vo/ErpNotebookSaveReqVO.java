package cn.iocoder.yudao.module.erp.controller.admin.notebook.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 记事本新增/修改 Request VO")
@Data
public class ErpNotebookSaveReqVO {

    @Schema(description = "记事本编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "NB001")
    private String no;

    @Schema(description = "图片列表", example = "https://example.com/image1.jpg,https://example.com/image2.jpg")
    private String images;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "开发任务")
    @NotNull(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "任务状态不能为空")
    private Integer taskStatus;

    @Schema(description = "任务人员", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotNull(message = "任务人员不能为空")
    private String taskPerson;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;
}