package cn.iocoder.yudao.module.erp.controller.admin.product.vo.ErpComboImport;


import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false)
public class ErpComboImportExcelVO {

    @ExcelProperty("组品编号")
    private String no;

    @ExcelProperty("单品组品(产品编号,产品数量;产品编号,产品数量;)")
    private String itemsString; // 直接使用字符串存储

    @ExcelProperty("产品简称")
    private String shortName;

    @ExcelProperty("发货编码")
    private String shippingCode;

    @ExcelProperty("采购人员")
    private String purchaser;

    @ExcelProperty("供应商名")
    private String supplier;

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty("运费类型(0-固定运费,1-按件运费,2-按重运费)")
    private Integer shippingFeeType;

    @ExcelProperty("固定运费")
    private BigDecimal fixedShippingFee;

    @ExcelProperty("按件数量")
    private Integer additionalItemQuantity;

    @ExcelProperty("按件价格")
    private BigDecimal additionalItemPrice;

    @ExcelProperty("首重重量")
    private BigDecimal firstWeight;

    @ExcelProperty("首重价格")
    private BigDecimal firstWeightPrice;

    @ExcelProperty("续重重量")
    private BigDecimal additionalWeight;

    @ExcelProperty("续重价格")
    private BigDecimal additionalWeightPrice;

}
