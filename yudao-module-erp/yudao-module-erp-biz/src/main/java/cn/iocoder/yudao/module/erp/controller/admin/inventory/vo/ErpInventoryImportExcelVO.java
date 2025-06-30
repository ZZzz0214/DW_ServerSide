package cn.iocoder.yudao.module.erp.controller.admin.inventory.vo;

import cn.iocoder.yudao.framework.excel.core.convert.IntegerConvert;
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
public class ErpInventoryImportExcelVO {

    @ExcelProperty("库存编号")
    private String no;

    @ExcelProperty("产品编号")
    private String productNo;

    @ExcelProperty(value = "现货库存", converter = IntegerConvert.class)
    private Integer spotInventory;

    @ExcelProperty("备注信息")
    private String remark;
}
