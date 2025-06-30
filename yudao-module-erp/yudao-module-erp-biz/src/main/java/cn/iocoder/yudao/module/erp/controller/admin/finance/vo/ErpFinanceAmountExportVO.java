package cn.iocoder.yudao.module.erp.controller.admin.finance.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.module.erp.enums.DictTypeConstants.AUDIT_STATUS;

@Schema(description = "管理后台 - ERP 财务金额导出 VO")
@Data
@Builder
public class ErpFinanceAmountExportVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "CWJE202403250001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "轮播图片", example = "http://example.com/image1.jpg,http://example.com/image2.jpg")
    @ExcelProperty("图片")
    private String carouselImages;

    @Schema(description = "渠道类型", example = "微信")
    @ExcelProperty("渠道类型")
    private String channelType;

    @Schema(description = "金额", example = "100.00")
    @ExcelProperty("金额")
    private BigDecimal amount;


    @Schema(description = "备注信息", example = "充值记录")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "审核状态", example = "10")
    @ExcelProperty(value = "审核状态", converter = DictConvert.class)
    @DictFormat(AUDIT_STATUS)
    private Integer auditStatus;

    @Schema(description = "审核人")
    @ExcelProperty("审核人")
    private String auditor;

    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核备注")
    @ExcelProperty("审核备注")
    private String auditRemark;

    @Schema(description = "创建者")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
