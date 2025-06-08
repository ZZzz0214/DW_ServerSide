package cn.iocoder.yudao.module.erp.controller.admin.dropship.vo;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpDropshipAssistImportExcelVO {

    @ExcelProperty("编号")
    private String no;

    @ExcelProperty("原表商品")
    private String originalProduct;

    @ExcelProperty("原表规格")
    private String originalSpec;

    @ExcelProperty("原表数量")
    private Integer originalQuantity;

    @ExcelProperty("组品编号")
    private String comboProductId;

    @ExcelProperty("发货编码")
    private String shippingCode;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("产品规格")
    private String productSpec;

    @ExcelProperty("产品数量")
    private Integer productQuantity;

    @ExcelProperty("创建人员")
    private String creator;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("产品简称")
    private String productShortName;
}
