package cn.iocoder.yudao.module.erp.controller.admin.notebook.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 记事本导出 Excel VO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ExcelIgnoreUnannotated
public class ErpNotebookExportVO {


    @ExcelProperty("编号")
    private String no;

    @ExcelProperty("图片列表")
    private String images;

    @ExcelProperty("任务名称")
    private String taskName;

    @ExcelProperty(value ="任务状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_NOTEBOOK_STATUS)
    private Integer taskStatus;

    @ExcelProperty(value = "任务人员", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SYSTEM_USER_LIST)
    private String taskPerson;

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty("创建人员")
    private String creator;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
