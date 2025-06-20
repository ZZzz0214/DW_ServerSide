package cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 批发采购导出 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class ErpWholesalePurchaseExportExcelVO {

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

    @ExcelProperty("采购人员")
    private String purchaser;

    @ExcelProperty("供应商名")
    private String supplier;

    @ExcelProperty("采购单价")
    private BigDecimal purchasePrice;

    @ExcelProperty("采购货拉拉费")
    private BigDecimal truckFee;

    @ExcelProperty("采购物流费用")
    private BigDecimal logisticsFee;

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