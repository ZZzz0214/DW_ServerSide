package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.BigDecimalConvert;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.excel.core.convert.IntegerConvert;
import cn.iocoder.yudao.framework.excel.core.convert.LocalDateTimeConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
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
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpLiveBroadcastingReviewImportExcelVO {

    @ExcelProperty("编号")
    private String no;

    @ExcelProperty("直播货盘编号")
    private String liveBroadcastingNo;

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty(value = "客户名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_LIVE_CUSTOMER_NAME)
    private String customerName;

    @ExcelProperty(value = "直播平台", converter = DictConvert.class)
    @DictFormat("erp_live_platform")
    private String livePlatform;


    @ExcelProperty(value = "直播佣金", converter = BigDecimalConvert.class)
    private BigDecimal liveCommission;


    @ExcelProperty(value = "公开佣金", converter = BigDecimalConvert.class)
    private BigDecimal publicCommission;


    @ExcelProperty(value = "返点佣金", converter = BigDecimalConvert.class)
    private BigDecimal rebateCommission;

    @ExcelProperty("直播价格")
    private String livePrice;

    @ExcelProperty(value = "寄样日期", converter = LocalDateTimeConvert.class)
    private LocalDateTime sampleSendDate;


    @ExcelProperty(value = "开播日期", converter = LocalDateTimeConvert.class)
    private LocalDateTime liveStartDate;


    @ExcelProperty(value = "开播销量", converter = IntegerConvert.class)
    private Integer liveSales;


    @ExcelProperty(value = "复播日期", converter = LocalDateTimeConvert.class)
    private LocalDateTime repeatLiveDate;


    @ExcelProperty(value = "复播销量", converter = IntegerConvert.class)
    private Integer repeatLiveSales;


}
