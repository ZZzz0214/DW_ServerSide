package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpFinanceAmountImportExcelVO {

    @Schema(description = "编号", example = "CWJE202403250001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "轮播图片", example = "http://example.com/image1.jpg,http://example.com/image2.jpg")
    @ExcelProperty("轮播图片")
    private String carouselImages;

    @Schema(description = "渠道类型", example = "微信")
    @ExcelProperty("渠道类型")
    private String channelType;

    @Schema(description = "金额", example = "100.00")
    @ExcelProperty("金额")
    private BigDecimal amount;

    @Schema(description = "操作类型", example = "1")
    @ExcelProperty("操作类型")
    private Integer operationType;

    @Schema(description = "操作前余额", example = "500.00")
    @ExcelProperty("操作前余额")
    private BigDecimal beforeBalance;

    @Schema(description = "操作后余额", example = "600.00")
    @ExcelProperty("操作后余额")
    private BigDecimal afterBalance;

    @Schema(description = "备注信息", example = "充值记录")
    @ExcelProperty("备注信息")
    private String remark;
}
