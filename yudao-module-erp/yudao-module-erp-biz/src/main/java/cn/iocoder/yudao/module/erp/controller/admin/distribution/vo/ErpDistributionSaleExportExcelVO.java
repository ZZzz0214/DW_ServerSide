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
public class ErpDistributionSaleExportExcelVO {

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

    @ExcelProperty("销售人员")
    private String salesperson;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("出货单价")
    private BigDecimal salePrice;

    @ExcelProperty("出货运费")
    private BigDecimal saleShippingFee;

    @ExcelProperty("出货杂费")
    private BigDecimal saleOtherFees;

    @ExcelProperty("出货总额")
    private BigDecimal totalSaleAmount;

    @ExcelProperty("出货备注")
    private String saleRemark;

    @ExcelProperty("中转人员")
    private String transferPerson;

    @ExcelProperty("创建人员")
    private String creator;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
} 