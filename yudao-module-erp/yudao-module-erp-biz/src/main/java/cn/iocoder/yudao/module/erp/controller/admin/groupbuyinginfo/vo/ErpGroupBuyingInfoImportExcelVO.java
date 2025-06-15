package cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpGroupBuyingInfoImportExcelVO {

    @ExcelProperty("编号")
    private String no;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty(value = "客户职位", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_CUSTOMER_POSITION)
    private String customerPosition;

    @ExcelProperty("客户微信")
    private String customerWechat;

    @ExcelProperty(value = "平台名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PLATFORM_NAME)
    private String platformName;

    @ExcelProperty(value = "客户属性", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_CUSTOMER_ATTRIBUTE)
    private String customerAttribute;

    @ExcelProperty(value = "客户城市", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_CUSTOMER_CITY)
    private String customerCity;

    @ExcelProperty(value = "客户区县", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_CUSTOMER_DISTRICT)
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