package cn.iocoder.yudao.module.erp.controller.admin.inventory.vo;

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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ExcelIgnoreUnannotated
public class ErpInventoryExportVO {

    @ExcelProperty("库存编号")
    private String no;

    @ExcelProperty("产品编号")
    private String productNo;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("产品简称")
    private String productShortName;

    @ExcelProperty(value = "品牌名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_BRAND)
    private String brand;

    @ExcelProperty(value = "产品品类", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_CATEGORY)
    private String category;

    @ExcelProperty("现货库存")
    private Integer spotInventory;

    @ExcelProperty("剩余库存")
    private Integer remainingInventory;

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty("创建人员")
    private String creator;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
