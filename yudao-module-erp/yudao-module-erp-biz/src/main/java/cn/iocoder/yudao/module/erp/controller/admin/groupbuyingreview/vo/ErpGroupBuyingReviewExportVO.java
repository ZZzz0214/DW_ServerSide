package cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ExcelIgnoreUnannotated
public class ErpGroupBuyingReviewExportVO {

    @ExcelProperty("编号")
    private String no;

    @ExcelProperty("团购货盘表编号")
    private String groupBuyingId;

    @ExcelProperty(value = "品牌名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_BRAND)
    private String brandName;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("产品规格")
    private String productSpec;

    @ExcelProperty("产品SKU")
    private String productSku;

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty("客户名称")
    private String customerName;


    @ExcelProperty("开团价格")
    private BigDecimal groupPrice;

    @ExcelProperty("供货价格")
    private BigDecimal supplyGroupPrice;

    @ExcelProperty("快递费用")
    private BigDecimal expressFee;

    @ExcelProperty("开团机制")
    private String groupMechanism;

    @ExcelProperty("寄样日期")
    private LocalDateTime sampleSendDate;

    @ExcelProperty("开团日期")
    private LocalDateTime groupStartDate;

    @ExcelProperty("开团销量")
    private Integer groupSales;

    @ExcelProperty("复团日期")
    private LocalDateTime repeatGroupDate;

    @ExcelProperty("复盘状态")
    private String reviewStatus;

    @ExcelProperty(value = "货盘状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_GROUP_BUYING_STATUS)
    private String status;

    @ExcelProperty("创建人员")
    private String creator;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
