package cn.iocoder.yudao.module.erp.controller.admin.dropship.vo;


import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 代发辅助保存 Request VO")
@Data
public class ErpDropshipAssistSaveReqVO {

    @Schema(description = "代发辅助表编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "DF202403250001")
    @NotNull(message = "编号不能为空")
    private String no;

    @Schema(description = "原表商品", example = "商品A")
    private String originalProduct;

    @Schema(description = "原表规格", example = "规格A")
    private String originalSpec;

    @Schema(description = "原表数量", example = "100")
    private Integer originalQuantity;

    @Schema(description = "组品编号", example = "1")
    @NotNull(message = "组品编号不能为空")
    private String comboProductId;

    @Schema(description = "产品规格", example = "规格B")
    private String productSpec;

    @Schema(description = "产品数量", example = "100")
    private Integer productQuantity;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;


    @ExcelProperty(value = "状态信息", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_DROPSHIP_STATUS)
    private String status;

}
