package cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO;

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
@Accessors(chain = false)
public class ErpWholesaleImportExcelVO {

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

    @ExcelProperty("中转人员")
    private String transferPerson;

    @ExcelProperty("创建人员")
    private String creator;

    @ExcelProperty("创建日期")
    private LocalDateTime createTime;
}
