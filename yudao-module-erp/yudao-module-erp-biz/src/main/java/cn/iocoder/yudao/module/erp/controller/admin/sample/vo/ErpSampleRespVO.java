package cn.iocoder.yudao.module.erp.controller.admin.sample.vo;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 样品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpSampleRespVO {

    @Schema(description = "样品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("样品编号")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SP001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "物流公司", requiredMode = Schema.RequiredMode.REQUIRED, example = "顺丰快递")
    @ExcelProperty("物流公司")
    private String logisticsCompany;

    @Schema(description = "物流单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SF123456789")
    @ExcelProperty("物流单号")
    private String logisticsNo;

    @Schema(description = "收件姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("收件姓名")
    private String receiverName;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @ExcelProperty("联系电话")
    private String contactPhone;

    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "北京市朝阳区")
    @ExcelProperty("详细地址")
    private String address;

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "CP001")
    @ExcelProperty("组品编号")
    private String comboProductId;

    @Schema(description = "产品规格", requiredMode = Schema.RequiredMode.REQUIRED, example = "标准规格")
    @ExcelProperty("产品规格")
    private String productSpec;

    @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("产品数量")
    private Integer productQuantity;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("客户名称")
    private String customerName;

    @Schema(description = "样品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("样品状态")
    private Integer sampleStatus;

    @Schema(description = "参考", example = "参考内容")
    @ExcelProperty("参考")
    private String reference;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}