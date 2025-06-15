package cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 团购信息导出 VO")
@Data
@Builder
public class ErpGroupBuyingInfoExportVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "GBI001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "客户名称", example = "张三")
    @ExcelProperty("客户名称")
    private String customerName;

    @Schema(description = "客户职位", example = "采购经理")
    @ExcelProperty(value = "客户职位", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_CUSTOMER_POSITION)
    private String customerPosition;

    @Schema(description = "客户微信", example = "zhangsan123")
    @ExcelProperty("客户微信")
    private String customerWechat;

    @Schema(description = "平台名称", example = "美团")
    @ExcelProperty(value = "平台名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PLATFORM_NAME)
    private String platformName;

    @Schema(description = "客户属性", example = "KA客户")
    @ExcelProperty(value = "客户属性", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_CUSTOMER_ATTRIBUTE)
    private String customerAttribute;

    @Schema(description = "客户城市", example = "北京市")
    @ExcelProperty(value = "客户城市", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_CUSTOMER_CITY)
    private String customerCity;

    @Schema(description = "客户区县", example = "朝阳区")
    @ExcelProperty(value = "客户区县", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_CUSTOMER_DISTRICT)
    private String customerDistrict;

    @Schema(description = "用户画像", example = "高端用户")
    @ExcelProperty("用户画像")
    private String userPortrait;

    @Schema(description = "招商类目", example = "食品饮料")
    @ExcelProperty("招商类目")
    private String recruitmentCategory;

    @Schema(description = "选品标准", example = "高毛利")
    @ExcelProperty("选品标准")
    private String selectionCriteria;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "创建者")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
} 