package cn.iocoder.yudao.module.erp.controller.admin.distribution.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false)
public class ErpDistributionPurchaseExportExcelVO {

    @ExcelProperty("订单编号")
    private String no;

    @ExcelProperty("订单号")
    private String orderNumber;

    @ExcelProperty("物流公司")
    private String logisticsCompany;

    @ExcelProperty("物流单号")
    private String trackingNumber;

    @ExcelProperty("收件姓名")
    private String receiverName;

    @ExcelProperty("联系电话")
    private String receiverPhone;

    @ExcelProperty("详细地址")
    private String receiverAddress;

    @ExcelProperty("组品编号")
    private String comboProductNo;

    @ExcelProperty("发货编码")
    private String shippingCode;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("产品规格")
    private String productSpecification;

    @ExcelProperty("产品数量")
    private Integer productQuantity;

    @ExcelProperty("采购人员")
    private String purchaser;

    @ExcelProperty("供应商名")
    private String supplier;

    @ExcelProperty("采购单价")
    private BigDecimal purchasePrice;

    @ExcelProperty("采购运费")
    private BigDecimal shippingFee;

    @ExcelProperty("采购杂费")
    private BigDecimal otherFees;

    @ExcelProperty("采购总额")
    private BigDecimal totalPurchaseAmount;

    @ExcelProperty("采购备注")
    private String purchaseRemark;

    @ExcelProperty("创建人员")
    private String creator;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
} 