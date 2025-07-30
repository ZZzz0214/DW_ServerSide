package cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo;


import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpWholesaleSaleAuditExportVO {

    @ExcelProperty("订单编号")
    private String no;

    @ExcelProperty("物流单号")
    private String logisticsNumber;

    @ExcelProperty("收件姓名")
    private String receiverName;

    @ExcelProperty("联系电话")
    private String receiverPhone;

    @ExcelProperty("详细地址")
    private String receiverAddress;

    @ExcelProperty("备注信息")
    private String remark;

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

    @ExcelProperty("售后状况")
    private String afterSalesStatus;

    @ExcelProperty("售后时间")
    private LocalDateTime afterSalesTime;

    @ExcelProperty("销售人员")
    private String salesperson;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("出货单价")
    private BigDecimal salePrice;

    @ExcelProperty("出货货拉拉费")
    private BigDecimal saleTruckFee;

    @ExcelProperty("出货物流费用")
    private BigDecimal saleLogisticsFee;

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


    @ExcelProperty("出货审核状态")
    private Integer saleAuditStatus;

    @ExcelProperty("出货审核金额")
    private BigDecimal saleAuditTotalAmount;

    @ExcelProperty("出货审核时间")
    private LocalDateTime saleApprovalTime;

    @ExcelProperty("出货反审核时间")
    private LocalDateTime saleUnapproveTime;

    @ExcelProperty("出货售后状态")
    private Integer saleAfterSalesStatus;

    @ExcelProperty("出货售后金额")
    private BigDecimal saleAfterSalesAmount;

    @ExcelProperty("出货售后时间")
    private String saleAfterSalesTime;




}
