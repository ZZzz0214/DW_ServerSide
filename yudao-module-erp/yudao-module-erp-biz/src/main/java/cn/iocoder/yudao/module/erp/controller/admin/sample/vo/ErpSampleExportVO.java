package cn.iocoder.yudao.module.erp.controller.admin.sample.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 样品导出 Excel VO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ExcelIgnoreUnannotated
public class ErpSampleExportVO {

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

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty("组品编号")
    private String comboProductId;

    @ExcelProperty("发货编码")
    private String shippingCode;

    @ExcelProperty("产品名称")
    private String comboProductName;

    @ExcelProperty("产品规格")
    private String productSpec;

    @ExcelProperty("产品数量")
    private Integer productQuantity;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty(value = "样品状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_SAMPLE_STATUS)
    private Integer sampleStatus;


    @ExcelProperty("创建人员")
    private String creator;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
