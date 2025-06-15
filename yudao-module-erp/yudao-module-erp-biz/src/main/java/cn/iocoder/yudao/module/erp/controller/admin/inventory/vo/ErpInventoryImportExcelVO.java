package cn.iocoder.yudao.module.erp.controller.admin.inventory.vo;

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

    @ExcelProperty("现货库存")
    private Integer spotInventory;

    @ExcelProperty("剩余库存")
    private Integer remainingInventory;

    @ExcelProperty("备注信息")
    private String remark;
} 