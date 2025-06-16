package cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class ErpLiveBroadcastingImportExcelVO {

    @ExcelProperty("编号")
    private String no;

    @ExcelProperty("产品图片")
    private String productImage;

    @ExcelProperty(value = "品牌名称", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_PRODUCT_BRAND)
    private Long brandId;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("产品规格")
    private String productSpec;

    @ExcelProperty("产品SKU")
    private String productSku;

    @ExcelProperty("市场价格")
    private BigDecimal marketPrice;

    @ExcelProperty("保质日期")
    private LocalDate shelfLife;

    @ExcelProperty("产品库存")
    private Integer productStock;

    @ExcelProperty("核心卖点")
    private String coreSellingPoint;

    @ExcelProperty("备注信息")
    private String remark;

    @ExcelProperty("直播价格")
    private BigDecimal livePrice;

    @ExcelProperty("直播佣金")
    private BigDecimal liveCommission;

    @ExcelProperty("公开佣金")
    private BigDecimal publicCommission;

    @ExcelProperty("返点佣金")
    private BigDecimal rebateCommission;

    @ExcelProperty("快递公司")
    private String expressCompany;

    @ExcelProperty("发货时效")
    private String shippingTime;

    @ExcelProperty("发货地区")
    private String shippingArea;

    @ExcelProperty(value = "直播货盘状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERP_LIVE_STATUS)
    private String liveStatus;
} 