package cn.iocoder.yudao.module.erp.controller.admin.notebook.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 记事本 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpNotebookRespVO {

    @Schema(description = "记事本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("记事本编号")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "NB001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "轮播图片", example = "https://example.com/image.jpg")
    @ExcelProperty("轮播图片")
    private String carouselImage;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "开发任务")
    @ExcelProperty("任务名称")
    private String taskName;

    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("任务状态")
    private Integer taskStatus;

    @Schema(description = "任务人员", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("任务人员")
    private String taskPerson;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}