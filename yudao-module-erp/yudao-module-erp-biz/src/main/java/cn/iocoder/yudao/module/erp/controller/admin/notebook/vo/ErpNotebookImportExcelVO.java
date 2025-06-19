package cn.iocoder.yudao.module.erp.controller.admin.notebook.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpNotebookImportExcelVO {

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
}
