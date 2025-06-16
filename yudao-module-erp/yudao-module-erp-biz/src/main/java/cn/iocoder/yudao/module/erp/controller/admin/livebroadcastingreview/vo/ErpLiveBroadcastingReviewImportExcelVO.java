package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo;

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

    @ExcelProperty("货盘编号")
    private String liveBroadcastingNo;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("直播平台")
    private String livePlatform;

    @ExcelProperty("直播佣金")
    private BigDecimal liveCommission;

    @ExcelProperty("公开佣金")
    private BigDecimal publicCommission;

    @ExcelProperty("返点佣金")
    private BigDecimal rebateCommission;

    @ExcelProperty("寄样日期")
    private LocalDateTime sampleSendDate;

    @ExcelProperty("开播日期")
    private LocalDateTime liveStartDate;

    @ExcelProperty("开播销量")
    private Integer liveSales;

    @ExcelProperty("复播日期")
    private LocalDateTime repeatLiveDate;

    @ExcelProperty("复播销量")
    private Integer repeatLiveSales;

    @ExcelProperty("备注信息")
    private String remark;
} 