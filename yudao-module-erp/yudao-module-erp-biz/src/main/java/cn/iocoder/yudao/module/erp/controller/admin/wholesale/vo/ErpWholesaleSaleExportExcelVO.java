package cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 批发出货导出 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class ErpWholesaleSaleExportExcelVO {

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

    @ExcelProperty("备注信息")
    private String remark;

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
    @NumberFormat("0.00")
    private BigDecimal saleOtherFees;

    @ExcelProperty("出货总额")
    @NumberFormat("0.00")
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
