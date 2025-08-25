package cn.iocoder.yudao.module.erp.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleProductStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleProductStatisticsRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleProductStatisticsExportVO;
import cn.iocoder.yudao.module.erp.service.statistics.ErpDistributionWholesaleProductStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - ERP 代发批发产品组品统计")
@RestController
@RequestMapping("/erp/statistics/distribution-wholesale-product")
@Validated
public class ErpDistributionWholesaleProductStatisticsController {

    @Resource
    private ErpDistributionWholesaleProductStatisticsService statisticsService;

    @GetMapping("/get")
    @Operation(summary = "获得代发批发产品组品统计")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<ErpDistributionWholesaleProductStatisticsRespVO> getDistributionWholesaleProductStatistics(@Valid ErpDistributionWholesaleProductStatisticsReqVO reqVO) {
        return success(statisticsService.getDistributionWholesaleProductStatistics(reqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得代发批发产品组品统计（分页）")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<ErpDistributionWholesaleProductStatisticsRespVO> getDistributionWholesaleProductStatisticsPage(@Valid ErpDistributionWholesaleProductStatisticsReqVO reqVO) {
        return success(statisticsService.getDistributionWholesaleProductStatisticsPage(reqVO));
    }

    @GetMapping("/export")
    @Operation(summary = "导出代发批发产品组品统计")
    @PreAuthorize("@ss.hasPermission('erp:statistics:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDistributionWholesaleProductStatistics(@Valid ErpDistributionWholesaleProductStatisticsReqVO reqVO,
                                                           HttpServletResponse response) throws IOException {
        System.out.println("=== 开始导出代发批发产品组品统计 ===");
        System.out.println("导出参数: startDate=" + reqVO.getStartDate() + ", endDate=" + reqVO.getEndDate() + 
                          ", supplier=" + reqVO.getSupplier() + ", customerName=" + reqVO.getCustomerName());
        
        // 导出时获取所有数据，不进行分页
        ErpDistributionWholesaleProductStatisticsRespVO data = statisticsService.getDistributionWholesaleProductStatistics(reqVO);
        
        // 转换为导出格式
        List<ErpDistributionWholesaleProductStatisticsExportVO> exportList = new ArrayList<>();
        
        // 导出单品统计
        if (data.getSingleProductPageResult() != null && data.getSingleProductPageResult().getList() != null) {
            for (ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics product : data.getSingleProductPageResult().getList()) {
                ErpDistributionWholesaleProductStatisticsExportVO exportVO = new ErpDistributionWholesaleProductStatisticsExportVO();
                exportVO.setProductName(product.getProductName());
                exportVO.setProductType("单品");
                exportVO.setDistributionCount(product.getDistributionCount());
                exportVO.setWholesaleCount(product.getWholesaleCount());
                exportVO.setTotalCount(product.getTotalCount());
                exportVO.setDistributionPercentage(product.getDistributionPercentage() + "%");
                exportVO.setWholesalePercentage(product.getWholesalePercentage() + "%");
                exportList.add(exportVO);
            }
        }
        
        // 导出组品统计
        if (data.getComboProductPageResult() != null && data.getComboProductPageResult().getList() != null) {
            for (ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics combo : data.getComboProductPageResult().getList()) {
                ErpDistributionWholesaleProductStatisticsExportVO exportVO = new ErpDistributionWholesaleProductStatisticsExportVO();
                exportVO.setProductName(combo.getComboProductName() + " (组品)");
                exportVO.setProductType("组品");
                exportVO.setDistributionCount(combo.getDistributionComboCount());
                exportVO.setWholesaleCount(combo.getWholesaleComboCount());
                exportVO.setTotalCount(combo.getTotalComboCount());
                exportVO.setDistributionPercentage(combo.getDistributionPercentage() + "%");
                exportVO.setWholesalePercentage(combo.getWholesalePercentage() + "%");
                exportList.add(exportVO);
            }
        }
        
        // 添加调试信息
        System.out.println("导出数据统计:");
        System.out.println("- 单品数量: " + (data.getSingleProductPageResult() != null ? data.getSingleProductPageResult().getList().size() : 0));
        System.out.println("- 组品数量: " + (data.getComboProductPageResult() != null ? data.getComboProductPageResult().getList().size() : 0));
        System.out.println("- 导出列表总数: " + exportList.size());
        
        ExcelUtils.write(response, "代发批发产品组品统计.xlsx", "数据", ErpDistributionWholesaleProductStatisticsExportVO.class, exportList);
    }

} 