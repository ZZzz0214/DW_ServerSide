package cn.iocoder.yudao.module.erp.controller.admin.sample.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 样品新增/修改 Request VO")
@Data
public class ErpSampleSaveReqVO {

    @Schema(description = "样品编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SP001")
    private String no;

    @Schema(description = "物流公司", requiredMode = Schema.RequiredMode.REQUIRED, example = "顺丰快递")
    private String logisticsCompany;

    @Schema(description = "物流单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SF123456789")
    private String logisticsNo;

    @Schema(description = "收件姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotNull(message = "收件姓名不能为空")
    private String receiverName;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotNull(message = "联系电话不能为空")
    private String contactPhone;

    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "北京市朝阳区")
    @NotNull(message = "详细地址不能为空")
    private String address;

    @Schema(description = "组品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "CP001")
    @NotNull(message = "组品编号不能为空")
    private String comboProductId;

    @Schema(description = "产品规格", requiredMode = Schema.RequiredMode.REQUIRED, example = "标准规格")
    private String productSpec;

    @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "产品数量不能为空")
    private Integer productQuantity;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotNull(message = "客户名称不能为空")
    private String customerName;

    @Schema(description = "样品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "样品状态不能为空")
    private Integer sampleStatus;

    @Schema(description = "参考", example = "参考内容")
    private String reference;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;
}
