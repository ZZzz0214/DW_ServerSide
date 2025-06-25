package cn.iocoder.yudao.module.erp.controller.admin.dropship.vo;


import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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


    @ExcelProperty("产品规格")
    private String productSpec;

    @ExcelProperty("产品数量")
    private Integer productQuantity;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;


    @ExcelProperty(value = "状态信息", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_DROPSHIP_STATUS)
    private String status;


}
