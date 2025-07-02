package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 代发批发产品组品统计 Excel 导出 VO
 *
 * @author 芋道源码
 */
@Data
public class ErpDistributionWholesaleProductStatisticsExportVO {

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("产品类型")
    private String productType;

    @ExcelProperty("代发数量")
    private Integer distributionCount;

    @ExcelProperty("批发数量")
    private Integer wholesaleCount;

    @ExcelProperty("总数量")
    private Integer totalCount;

    @ExcelProperty("代发占比")
    private String distributionPercentage;

    @ExcelProperty("批发占比")
    private String wholesalePercentage;

} 