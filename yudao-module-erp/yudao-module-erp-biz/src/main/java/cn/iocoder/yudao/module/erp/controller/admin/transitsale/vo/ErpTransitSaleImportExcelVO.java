package cn.iocoder.yudao.module.erp.controller.admin.transitsale.vo;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpTransitSaleImportExcelVO {

    @ExcelProperty("中转销售编号")
    private Long id;

    @ExcelProperty("编号")
    private String no;

    @ExcelProperty("组品编号")
    private Long groupProductId;

    @ExcelProperty("中转人员")
    private String transitPerson;

    @ExcelProperty("代发单价")
    private BigDecimal distributionPrice;

    @ExcelProperty("批发单价")
    private BigDecimal wholesalePrice;

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty("运费类型")
    private Integer shippingFeeType;

    @ExcelProperty("固定运费")
    private BigDecimal fixedShippingFee;

    @ExcelProperty("首件数量")
    private Integer firstItemQuantity;

    @ExcelProperty("首件价格")
    private BigDecimal firstItemPrice;

    @ExcelProperty("续件数量")
    private Integer additionalItemQuantity;

    @ExcelProperty("续件价格")
    private BigDecimal additionalItemPrice;

    @ExcelProperty("首重重量")
    private BigDecimal firstWeight;

    @ExcelProperty("首重价格")
    private BigDecimal firstWeightPrice;

    @ExcelProperty("续重重量")
    private BigDecimal additionalWeight;

    @ExcelProperty("续重价格")
    private BigDecimal additionalWeightPrice;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
