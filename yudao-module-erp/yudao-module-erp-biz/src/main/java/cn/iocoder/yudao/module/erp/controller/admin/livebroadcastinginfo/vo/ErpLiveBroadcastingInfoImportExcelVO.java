package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo;


import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpLiveBroadcastingInfoImportExcelVO {

    @ExcelProperty("编号")
    private String no;

    @ExcelProperty("客户名称")
    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    @Schema(description = "客户职位", example = "运营总监")
    @ExcelProperty(value = "客户职位", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_LIVE_CUSTOMER_POSITION)
    private String customerPosition;

    @ExcelProperty("客户微信")
    private String customerWechat;

    @Schema(description = "平台名称", example = "抖音")
    @ExcelProperty(value = "平台名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_LIVE_PLATFORM_NAME)
    private String platformName;

    @Schema(description = "客户属性", example = "KA客户")
    @ExcelProperty(value = "客户属性", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_LIVE_CUSTOMER_ATTRIBUTE)
    private String customerAttribute;

    @Schema(description = "客户城市", example = "北京")
    @ExcelProperty(value = "客户城市", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_LIVE_CUSTOMER_CITY)
    private String customerCity;

    @Schema(description = "客户区县", example = "朝阳区")
    @ExcelProperty(value = "客户区县", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_LIVE_CUSTOMER_DISTRICT)
    private String customerDistrict;

    @ExcelProperty("用户画像")
    private String userPortrait;

    @ExcelProperty("招商类目")
    private String recruitmentCategory;

    @ExcelProperty("选品标准")
    private String selectionCriteria;

    @ExcelProperty("备注信息")
    private String remark;
}
