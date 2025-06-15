package cn.iocoder.yudao.module.erp.controller.admin.sample.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpSampleImportExcelVO {

    @ExcelProperty("编号")
    private String no;

    @ExcelProperty("物流公司")
    private String logisticsCompany;

    @ExcelProperty("物流单号")
    private String logisticsNo;

    @ExcelProperty("收件姓名")
    private String receiverName;

    @ExcelProperty("联系电话")
    private String contactPhone;

    @ExcelProperty("详细地址")
    private String address;

    @ExcelProperty("组品编号")
    private String comboProductId;

    @ExcelProperty("产品规格")
    private String productSpec;

    @ExcelProperty("产品数量")
    private Integer productQuantity;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty(value = "样品状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_SAMPLE_STATUS)
    private Integer sampleStatus;

    @ExcelProperty("参考")
    private String reference;

    @ExcelProperty("备注信息")
    private String remark;
}
