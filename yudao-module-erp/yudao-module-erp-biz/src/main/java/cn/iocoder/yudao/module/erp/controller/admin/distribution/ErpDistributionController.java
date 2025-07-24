package cn.iocoder.yudao.module.erp.controller.admin.distribution;
import cn.iocoder.yudao.framework.common.pojo.*;
import cn.iocoder.yudao.framework.excel.core.listener.RowIndexListener;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.*;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO.*;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceESDO;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionPurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionSaleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSalePriceMapper;
import cn.iocoder.yudao.module.erp.service.distribution.*;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductESRepository;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalePriceESRepository;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalePriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;

@Tag(name = "管理后台 - ERP 代发")
@RestController
@RequestMapping("/erp/distribution")
@Validated
public class ErpDistributionController {

    @Resource
    private ErpDistributionService distributionService;
    @Resource
    private ErpDistributionPurchaseMapper purchaseMapper;
    @Resource
    private ErpDistributionSaleMapper saleMapper;

    @Resource
    private ErpComboProductService comboProductService;
    @Resource
    private ErpSalePriceService salePriceService;

    @Resource
    private ErpDistributionBaseESRepository distributionBaseESRepository;
    @Resource
    private ErpDistributionPurchaseESRepository distributionPurchaseESRepository;
    @Resource
    private ErpDistributionSaleESRepository distributionSaleESRepository;
    @Resource
    private ErpComboProductESRepository comboProductESRepository;
    @Resource
    private ErpSalePriceESRepository salePriceESRepository;

    @Resource
    private ErpDistributionCombinedESRepository distributionCombinedESRepository;

    @PostMapping("/create")
    @Operation(summary = "创建代发")
    @PreAuthorize("@ss.hasPermission('erp:distribution:create')")
    public CommonResult<Long> createDistribution(@Valid @RequestBody ErpDistributionSaveReqVO createReqVO) {
        return success(distributionService.createDistribution(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新代发")
    @PreAuthorize("@ss.hasPermission('erp:distribution:update')")
    public CommonResult<Boolean> updateDistribution(@Valid @RequestBody ErpDistributionSaveReqVO updateReqVO) {
        distributionService.updateDistribution(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新代发状态")
    @PreAuthorize("@ss.hasPermission('erp:distribution:update-status')")
    public CommonResult<Boolean> updateDistributionStatus(@RequestParam("id") Long id,
                                                          @RequestParam("status") Integer status,
                                                          @RequestParam("otherFees") BigDecimal otherFees) {
        distributionService.updateDistributionStatus(id, status, otherFees);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除代发")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:distribution:delete')")
    public CommonResult<Boolean> deleteDistribution(@RequestParam("ids") List<Long> ids) {
        distributionService.deleteDistribution(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得代发")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<ErpDistributionRespVO> getDistribution(@RequestParam("id") Long id) {
        // 1. 获取基础信息
        ErpDistributionRespVO distribution = distributionService.getDistribution(id);
        if (distribution == null) {
            return success(null);
        }

        return success(distribution);
    }

    @GetMapping("/page")
    @Operation(summary = "获得代发分页")
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<PageResult<ErpDistributionRespVO>> getDistributionPage(@Valid ErpDistributionPageReqVO pageReqVO) {
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);
//        pageResult.getList().forEach(item -> {
//            System.out.println(String.format("订单ID: %s, 组品编号: %s, 采购运费: %s, 销售总额: %s, 售后状况: %s, 售后时间: %s",
//                    item.getId(),
//                    item.getComboProductNo(),
//                    item.getShippingFee(),
//                    item.getTotalSaleAmount(),
//                    item.getAfterSalesStatus(),
//                    item.getAfterSalesTime()));
//        });
        return success(pageResult);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得代发列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<List<ErpDistributionRespVO>> getDistributionListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpDistributionRespVO> list = distributionService.getDistributionVOList(ids);
        return success(list);
    }

    // 未审核代发采购分页
    @GetMapping("/purchase/unreviewed-page")
    @Operation(summary = "获得未审核代发采购分页")
    @PreAuthorize("@ss.hasPermission('erp:distributionPurchaseAu:query')")
    public CommonResult<PageResultWithSummary<ErpDistributionPurchaseAuditVO>> getUnreviewedPurchasePage(@Valid ErpDistributionPageReqVO pageReqVO) {
        // 获取分页数据
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);
        System.out.println("查看采购前面返回的数据"+pageResult);

        // 转换为ErpDistributionPurchaseAuditVO列表
        List<ErpDistributionPurchaseAuditVO> list = pageResult.getList().stream().map(item -> {
            ErpDistributionPurchaseAuditVO vo = new ErpDistributionPurchaseAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        // 计算合计值
        BigDecimal totalPurchasePrice = BigDecimal.ZERO;
        BigDecimal totalShippingFee = BigDecimal.ZERO;
        BigDecimal totalOtherFees = BigDecimal.ZERO;
        BigDecimal totalPurchaseAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAfterSalesAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAuditTotalAmount = BigDecimal.ZERO;

        for (ErpDistributionPurchaseAuditVO vo : list) {
            if (vo.getPurchasePrice() != null) {
                totalPurchasePrice = totalPurchasePrice.add(vo.getPurchasePrice());
            }
            if (vo.getShippingFee() != null) {
                totalShippingFee = totalShippingFee.add(vo.getShippingFee());
            }
            if (vo.getOtherFees() != null) {
                totalOtherFees = totalOtherFees.add(vo.getOtherFees());
            }
            if (vo.getTotalPurchaseAmount() != null) {
                totalPurchaseAmount = totalPurchaseAmount.add(vo.getTotalPurchaseAmount());
            }
            if (vo.getPurchaseAfterSalesAmount() != null) {
                totalPurchaseAfterSalesAmount = totalPurchaseAfterSalesAmount.add(vo.getPurchaseAfterSalesAmount());
            }
            if (vo.getTotalPurchaseAmount() != null) {
                totalPurchaseAuditTotalAmount = totalPurchaseAuditTotalAmount.add(vo.getTotalPurchaseAmount());
            }
        }

        // 创建返回结果
        PageResultWithSummary<ErpDistributionPurchaseAuditVO> result = new PageResultWithSummary<>();
        result.setPageResult(new PageResult<>(list, pageResult.getTotal()));
        result.setTotalPurchasePrice(totalPurchasePrice);
        result.setTotalShippingFee(totalShippingFee);
        result.setTotalOtherFees(totalOtherFees);
        result.setTotalPurchaseAmount(totalPurchaseAmount);
        result.setTotalPurchaseAfterSalesAmount(totalPurchaseAfterSalesAmount);
        result.setTotalPurchaseAuditTotalAmount(totalPurchaseAuditTotalAmount);

        return success(result);
    }

    // 已审核代发采购分页
    @GetMapping("/purchase/reviewed-page")
    @Operation(summary = "获得已审核代发采购分页")
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<PageResultWithSummary<ErpDistributionPurchaseAuditVO>> getReviewedPurchasePage(@Valid ErpDistributionPageReqVO pageReqVO) {
        pageReqVO.setPurchaseAuditStatus(20);
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);

        // 转换为ErpDistributionPurchaseAuditVO列表
        List<ErpDistributionPurchaseAuditVO> list = pageResult.getList().stream().map(item -> {
            ErpDistributionPurchaseAuditVO vo = new ErpDistributionPurchaseAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
        // 计算合计值
        BigDecimal totalPurchasePrice = BigDecimal.ZERO;
        BigDecimal totalShippingFee = BigDecimal.ZERO;
        BigDecimal totalOtherFees = BigDecimal.ZERO;
        BigDecimal totalPurchaseAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAfterSalesAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAuditTotalAmount = BigDecimal.ZERO;

        for (ErpDistributionPurchaseAuditVO vo : list) {
            if (vo.getPurchasePrice() != null) {
                totalPurchasePrice = totalPurchasePrice.add(vo.getPurchasePrice());
            }
            if (vo.getShippingFee() != null) {
                totalShippingFee = totalShippingFee.add(vo.getShippingFee());
            }
            if (vo.getOtherFees() != null) {
                totalOtherFees = totalOtherFees.add(vo.getOtherFees());
            }
            if (vo.getTotalPurchaseAmount() != null) {
                totalPurchaseAmount = totalPurchaseAmount.add(vo.getTotalPurchaseAmount());
            }
            if (vo.getPurchaseAfterSalesAmount() != null) {
                totalPurchaseAfterSalesAmount = totalPurchaseAfterSalesAmount.add(vo.getPurchaseAfterSalesAmount());
            }
            if (vo.getPurchaseAuditTotalAmount() != null) {
                totalPurchaseAuditTotalAmount = totalPurchaseAuditTotalAmount.add(vo.getPurchaseAuditTotalAmount());
            }
        }

        // 创建返回结果
        PageResultWithSummary<ErpDistributionPurchaseAuditVO> result = new PageResultWithSummary<>();
        result.setPageResult(new PageResult<>(list, pageResult.getTotal()));
        result.setTotalPurchasePrice(totalPurchasePrice);
        result.setTotalShippingFee(totalShippingFee);
        result.setTotalOtherFees(totalOtherFees);
        result.setTotalPurchaseAmount(totalPurchaseAmount);
        result.setTotalPurchaseAfterSalesAmount(totalPurchaseAfterSalesAmount);
        result.setTotalPurchaseAuditTotalAmount(totalPurchaseAuditTotalAmount);

        return success(result);
    }

    // 未审核代发销售分页
    @GetMapping("/sale/unreviewed-page")
    @Operation(summary = "获得未审核代发销售分页")
    @PreAuthorize("@ss.hasPermission('erp:distributionSaleAu:query')")
    public CommonResult<SalesSummaryPageResult<ErpDistributionSaleAuditVO>> getUnreviewedSalePage(@Valid ErpDistributionPageReqVO pageReqVO) {
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);

        // 转换为ErpDistributionSaleAuditVO列表
        List<ErpDistributionSaleAuditVO> list = pageResult.getList().stream().map(item -> {
            ErpDistributionSaleAuditVO vo = new ErpDistributionSaleAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        // 计算销售合计值
        BigDecimal totalSalePrice = BigDecimal.ZERO;
        BigDecimal totalSaleShippingFee = BigDecimal.ZERO;
        BigDecimal totalSaleOtherFees = BigDecimal.ZERO;
        BigDecimal totalSaleAmount = BigDecimal.ZERO;
        BigDecimal totalSaleAfterSalesAmount = BigDecimal.ZERO;
        BigDecimal totalSaleAuditTotalAmount = BigDecimal.ZERO;

        for (ErpDistributionSaleAuditVO vo : list) {
            if (vo.getSalePrice() != null) {
                totalSalePrice = totalSalePrice.add(vo.getSalePrice());
            }
            if (vo.getSaleShippingFee() != null) {
                totalSaleShippingFee = totalSaleShippingFee.add(vo.getSaleShippingFee());
            }
            if (vo.getSaleOtherFees() != null) {
                totalSaleOtherFees = totalSaleOtherFees.add(vo.getSaleOtherFees());
            }
            if (vo.getTotalSaleAmount() != null) {
                totalSaleAmount = totalSaleAmount.add(vo.getTotalSaleAmount());
            }
            if (vo.getSaleAfterSalesAmount() != null) {
                totalSaleAfterSalesAmount = totalSaleAfterSalesAmount.add(vo.getSaleAfterSalesAmount());
            }
            if (vo.getSaleAuditTotalAmount() != null) {
                totalSaleAuditTotalAmount = totalSaleAuditTotalAmount.add(vo.getSaleAuditTotalAmount());
            }
        }

        // 创建返回结果
        SalesSummaryPageResult<ErpDistributionSaleAuditVO> result = new SalesSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, pageResult.getTotal()));
        result.setTotalSalePrice(totalSalePrice);
        result.setTotalSaleShippingFee(totalSaleShippingFee);
        result.setTotalSaleOtherFees(totalSaleOtherFees);
        result.setTotalSaleAmount(totalSaleAmount);
        result.setTotalSaleAfterSalesAmount(totalSaleAfterSalesAmount);
        result.setTotalSaleAuditTotalAmount(totalSaleAuditTotalAmount);
        return success(result);
    }

    @GetMapping("/sale/reviewed-page")
    @Operation(summary = "获得已审核代发销售分页")
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<SalesSummaryPageResult<ErpDistributionSaleAuditVO>> getReviewedSalePage(@Valid ErpDistributionPageReqVO pageReqVO) {
        pageReqVO.setSaleAuditStatus(20); // 设置状态为已审核
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);

        // 转换为ErpDistributionSaleAuditVO列表
        List<ErpDistributionSaleAuditVO> list = pageResult.getList().stream().map(item -> {
            ErpDistributionSaleAuditVO vo = new ErpDistributionSaleAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        // 计算销售合计值
        BigDecimal totalSalePrice = BigDecimal.ZERO;
        BigDecimal totalSaleShippingFee = BigDecimal.ZERO;
        BigDecimal totalSaleOtherFees = BigDecimal.ZERO;
        BigDecimal totalSaleAmount = BigDecimal.ZERO;
        BigDecimal totalSaleAfterSalesAmount = BigDecimal.ZERO;
        BigDecimal totalSaleAuditTotalAmount = BigDecimal.ZERO;

        for (ErpDistributionSaleAuditVO vo : list) {
            if (vo.getSalePrice() != null) {
                totalSalePrice = totalSalePrice.add(vo.getSalePrice());
            }
            if (vo.getSaleShippingFee() != null) {
                totalSaleShippingFee = totalSaleShippingFee.add(vo.getSaleShippingFee());
            }
            if (vo.getSaleOtherFees() != null) {
                totalSaleOtherFees = totalSaleOtherFees.add(vo.getSaleOtherFees());
            }
            if (vo.getTotalSaleAmount() != null) {
                totalSaleAmount = totalSaleAmount.add(vo.getTotalSaleAmount());
            }
            if (vo.getSaleAfterSalesAmount() != null) {
                totalSaleAfterSalesAmount = totalSaleAfterSalesAmount.add(vo.getSaleAfterSalesAmount());
            }
            if (vo.getSaleAuditTotalAmount() != null) {
                totalSaleAuditTotalAmount = totalSaleAuditTotalAmount.add(vo.getSaleAuditTotalAmount());
            }
        }

        // 创建返回结果
        SalesSummaryPageResult<ErpDistributionSaleAuditVO> result = new SalesSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, pageResult.getTotal()));
        result.setTotalSalePrice(totalSalePrice);
        result.setTotalSaleShippingFee(totalSaleShippingFee);
        result.setTotalSaleOtherFees(totalSaleOtherFees);
        result.setTotalSaleAmount(totalSaleAmount);
        result.setTotalSaleAfterSalesAmount(totalSaleAfterSalesAmount);
        result.setTotalSaleAuditTotalAmount(totalSaleAuditTotalAmount);

        return success(result);
    }

    @GetMapping("/purchase/get")
    @Operation(summary = "获得代发采购订单详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<ErpDistributionPurchaseAuditVO> getDistributionPurchase2(@RequestParam("id") Long id) {
        System.out.println("获得代发采购订单详情"+id);
        // 1. 获取基础信息
        ErpDistributionRespVO distribution = distributionService.getDistribution(id);
        if (distribution == null) {
            return success(null);
        }

        // 2. 转换为VO对象
        ErpDistributionPurchaseAuditVO respVO = BeanUtils.toBean(distribution, ErpDistributionPurchaseAuditVO.class);

        return success(respVO);
    }

    @GetMapping("/sale/get")
    @Operation(summary = "获得代发销售审核详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<ErpDistributionSaleAuditVO> getDistributionSale(@RequestParam("id") Long id) {
        // 1. 获取基础信息
        ErpDistributionRespVO distribution = distributionService.getDistribution(id);
        if (distribution == null) {
            return success(null);
        }

        // 2. 转换为VO对象
        System.out.println("总额"+distribution.getTotalSaleAmount());
        ErpDistributionSaleAuditVO respVO = BeanUtils.toBean(distribution, ErpDistributionSaleAuditVO.class);
        System.out.println("销售总额："+respVO.getTotalSaleAmount());

        return success(respVO);
    }

    @PutMapping("/update-purchase-after-sales")
    @Operation(summary = "更新采购售后信息")
    //@PreAuthorize("@ss.hasPermission('erp:distribution:update-one')")
    public CommonResult<Boolean> updatePurchaseAfterSales(@Valid @RequestBody ErpDistributionPurchaseAfterSalesUpdateReqVO reqVO) {
        System.out.println("售后信息"+reqVO);
        distributionService.updatePurchaseAfterSales(reqVO);
        return success(true);
    }

    @PutMapping("/update-sale-after-sales")
    @Operation(summary = "更新销售售后信息")
    //@PreAuthorize("@ss.hasPermission('erp:distribution:update-one')")
    public CommonResult<Boolean> updateSaleAfterSales(@Valid @RequestBody ErpDistributionSaleAfterSalesUpdateReqVO reqVO) {
        System.out.println("销售售后信息：" + reqVO);
        distributionService.updateSaleAfterSales(reqVO);
        return success(true);
    }

    @PutMapping("/batch-update-purchase-after-sales")
    @Operation(summary = "批量更新采购售后状态")
    //@PreAuthorize("@ss.hasPermission('erp:distribution:update-after-sales')")
    public CommonResult<Boolean> batchUpdatePurchaseAfterSales(@RequestParam("ids") List<Long> ids,
                                                               @RequestParam("purchaseAfterSalesStatus") Integer purchaseAfterSalesStatus) {
        distributionService.batchUpdatePurchaseAfterSales(ids, purchaseAfterSalesStatus);
        return success(true);
    }

    @PutMapping("/batch-update-sale-after-sales")
    @Operation(summary = "批量更新销售售后状态")
    //@PreAuthorize("@ss.hasPermission('erp:distribution:update-after-sales')")
    public CommonResult<Boolean> batchUpdateSaleAfterSales(@RequestParam("ids") List<Long> ids,
                                                           @RequestParam("saleAfterSalesStatus") Integer saleAfterSalesStatus) {
        distributionService.batchUpdateSaleAfterSales(ids, saleAfterSalesStatus);
        return success(true);
    }

    @PutMapping("/update-purchase-audit-status")
    @Operation(summary = "更新采购审核状态")
    @PreAuthorize("@ss.hasPermission('erp:distributionPurchaseAu:update-purchase-audit-status-one')")
    public CommonResult<Boolean> updatePurchaseAuditStatus(@RequestParam("id") Long id,
                                                      @RequestParam("purchaseAuditStatus") Integer purchaseAuditStatus,
                                                      @RequestParam("otherFees") BigDecimal otherFees,
                                                      @RequestParam(value = "purchaseAuditTotalAmount", required = false) BigDecimal purchaseAuditTotalAmount) {
        System.out.println("更改的订单id为 " + id);
        System.out.println("更改的采购审核状态为 " + purchaseAuditStatus);
        System.out.println("其他费用为 " + otherFees);
        System.out.println("代发采购审核总额为 " + purchaseAuditTotalAmount);
        distributionService.updatePurchaseAuditStatus(id, purchaseAuditStatus, otherFees, purchaseAuditTotalAmount);
        return success(true);
    }

    @PutMapping("/batch-update-purchase-audit-status")
    @Operation(summary = "批量更新采购审核状态")
    @PreAuthorize("@ss.hasPermission('erp:distributionPurchaseAu:update-purchase-audit-status')")
    public CommonResult<Boolean> batchUpdatePurchaseAuditStatus(@RequestParam("ids") List<Long> ids,
                                                                @RequestParam("purchaseAuditStatus") Integer purchaseAuditStatus) {
        distributionService.batchUpdatePurchaseAuditStatus(ids, purchaseAuditStatus);
        return success(true);
    }

    @PutMapping("/update-sale-audit-status")
    @Operation(summary = "更新销售审核状态")
    @PreAuthorize("@ss.hasPermission('erp:distributionSaleAu:update-sale-audit-status-one')")
    public CommonResult<Boolean> updateSaleAuditStatus(@RequestParam("id") Long id,
                                                    @RequestParam("saleAuditStatus") Integer saleAuditStatus,
                                                    @RequestParam("otherFees") BigDecimal otherFees,
                                                    @RequestParam(value = "saleAuditTotalAmount", required = false) BigDecimal saleAuditTotalAmount) {
        System.out.println("更改的订单id为 " + id);
        System.out.println("更改的销售审核状态为 " + saleAuditStatus);
        System.out.println("其他费用为 " + otherFees);
        System.out.println("代发销售审核总额为 " + saleAuditTotalAmount);
        distributionService.updateSaleAuditStatus(id, saleAuditStatus, otherFees, saleAuditTotalAmount);
        return success(true);
    }

    @PutMapping("/batch-update-sale-audit-status")
    @Operation(summary = "批量更新销售审核状态")
    @PreAuthorize("@ss.hasPermission('erp:distributionSaleAu:update-sale-audit-status')")
    public CommonResult<Boolean> batchUpdateSaleAuditStatus(@RequestParam("ids") List<Long> ids,
                                                            @RequestParam("saleAuditStatus") Integer saleAuditStatus) {
        distributionService.batchUpdateSaleAuditStatus(ids, saleAuditStatus);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出代发 Excel")
    @PreAuthorize("@ss.hasPermission('erp:distribution:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDistributionExcel(@Valid ErpDistributionPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        // 设置分页大小
        pageReqVO.setPageSize(10000);
        // 获取分页数据
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);
        System.out.println("查看代发数据：" + pageResult.getList());

        // 转换为导出VO
        List<ErpDistributionExportExcelVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpDistributionExportExcelVO.class);

        // 导出Excel
        ExcelUtils.write(response, "代发信息.xlsx", "数据", ErpDistributionExportExcelVO.class, exportList);
    }

    @GetMapping("/export-basic-excel")
    @Operation(summary = "导出代发基础信息 Excel")
    @PreAuthorize("@ss.hasPermission('erp:distribution:importBasic')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportBasicExcel(@Valid ErpDistributionPageReqVO pageReqVO,
                                HttpServletResponse response) throws IOException {
        // 设置分页大小
        pageReqVO.setPageSize(10000);
        // 获取分页数据
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);

        // 转换为基础导出VO
        List<ErpDistributionBasicExportExcelVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpDistributionBasicExportExcelVO.class);

        // 导出Excel
        ExcelUtils.write(response, "代发基础信息.xlsx", "数据", ErpDistributionBasicExportExcelVO.class, exportList);
    }

    @GetMapping("/export-purchase-excel")
    @Operation(summary = "导出代发采购信息 Excel")
    @PreAuthorize("@ss.hasPermission('erp:distribution:importPurchase')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseExcel(@Valid ErpDistributionPageReqVO pageReqVO,
                                   HttpServletResponse response) throws IOException {
        // 全量查，保证和分页一致
        List<ErpDistributionRespVO> allList = distributionService.exportAllDistributions(pageReqVO);
        // 转换为采购导出VO
        List<ErpDistributionPurchaseExportExcelVO> exportList = BeanUtils.toBean(allList, ErpDistributionPurchaseExportExcelVO.class);
        // 导出Excel
        ExcelUtils.write(response, "代发采购信息.xlsx", "数据", ErpDistributionPurchaseExportExcelVO.class, exportList);
    }

    @GetMapping("/export-sale-excel")
    @Operation(summary = "导出代发出货信息 Excel")
    @PreAuthorize("@ss.hasPermission('erp:distribution:importSale')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSaleExcel(@Valid ErpDistributionPageReqVO pageReqVO,
                               HttpServletResponse response) throws IOException {
        // 全量查，保证和分页一致
        List<ErpDistributionRespVO> allList = distributionService.exportAllDistributions(pageReqVO);
        // 转换为出货导出VO
        List<ErpDistributionSaleExportExcelVO> exportList = BeanUtils.toBean(allList, ErpDistributionSaleExportExcelVO.class);
        // 导出Excel
        ExcelUtils.write(response, "代发出货信息.xlsx", "数据", ErpDistributionSaleExportExcelVO.class, exportList);
    }

    @GetMapping("/export-ship-excel")
    @Operation(summary = "导出代发发货信息 Excel")
    @PreAuthorize("@ss.hasPermission('erp:distribution:importShip')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportShipExcel(@Valid ErpDistributionPageReqVO pageReqVO,
                               HttpServletResponse response) throws IOException {
        // 设置分页大小
        pageReqVO.setPageSize(10000);
        // 获取分页数据
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);

        // 转换为发货导出VO
        List<ErpDistributionShipExportExcelVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpDistributionShipExportExcelVO.class);

        // 导出Excel
        ExcelUtils.write(response, "代发发货信息.xlsx", "数据", ErpDistributionShipExportExcelVO.class, exportList);
    }

    @GetMapping("/export-purchase-audit-excel")
    @Operation(summary = "导出代发采购审核 Excel")
    @PreAuthorize("@ss.hasPermission('erp:distributionPurchaseAu:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseAuditExcel(@Valid ErpDistributionPageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);

        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);

        // 转换为采购审核导出VO
        List<ErpDistributionPurchaseAuditExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpDistributionPurchaseAuditExportVO.class);

        ExcelUtils.write(response, "代发采购审核信息.xlsx", "数据", ErpDistributionPurchaseAuditExportVO.class, exportList);
    }

    @GetMapping("/export-sale-audit-excel")
    @Operation(summary = "导出代发销售审核 Excel")
    @PreAuthorize("@ss.hasPermission('erp:distributionSaleAu:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSaleAuditExcel(@Valid ErpDistributionPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);

        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);

        // 转换为销售审核导出VO
        List<ErpDistributionSaleAuditExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpDistributionSaleAuditExportVO.class);

        ExcelUtils.write(response, "代发销售审核信息.xlsx", "数据", ErpDistributionSaleAuditExportVO.class, exportList);
    }
    @GetMapping("/export-reviewed-purchase-excel")
    @Operation(summary = "导出已审核代发采购 Excel")
    @PreAuthorize("@ss.hasPermission('erp:distribution:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReviewedPurchaseExcel(@Valid ErpDistributionPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        pageReqVO.setPurchaseAuditStatus(20); // 设置采购审核状态为已审核

        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);

        List<ErpDistributionPurchaseAuditExportOutVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpDistributionPurchaseAuditExportOutVO.class);

        ExcelUtils.write(response, "已审核代发采购信息.xlsx", "数据", ErpDistributionPurchaseAuditExportOutVO.class, exportList);
    }

    @GetMapping("/export-reviewed-sale-excel")
    @Operation(summary = "导出已审核代发销售 Excel")
    @PreAuthorize("@ss.hasPermission('erp:distribution:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReviewedSaleExcel(@Valid ErpDistributionPageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        pageReqVO.setSaleAuditStatus(20); // 设置销售审核状态为已审核

        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);

        List<ErpDistributionSaleAuditOutExportVO> exportList = BeanUtils.toBean(pageResult.getList(), ErpDistributionSaleAuditOutExportVO.class);

        ExcelUtils.write(response, "已审核代发销售信息.xlsx", "数据", ErpDistributionSaleAuditOutExportVO.class, exportList);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入代发模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpDistributionImportExcelTemplateVO> list = Arrays.asList(
                ErpDistributionImportExcelTemplateVO.builder()
                .build()
        );
        // 输出
        ExcelUtils.write(response, "代发导入模板.xlsx", "代发列表", ErpDistributionImportExcelTemplateVO.class, list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入代发")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true),
            @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('erp:distribution:import')")
    public CommonResult<ErpDistributionImportRespVO> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpDistributionImportExcelVO> list = ExcelUtils.read(inputStream, ErpDistributionImportExcelVO.class, new RowIndexListener<>());
            return success(distributionService.importDistributionList(list, updateSupport));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/purchase/get-import-template")
    @Operation(summary = "获得导入代发采购审核模板")
    public void importPurchaseAuditTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpDistributionPurchaseAuditImportExcelVO> list = Arrays.asList(
                ErpDistributionPurchaseAuditImportExcelVO.builder()
                .build()
        );
        // 输出
        ExcelUtils.write(response, "代发采购审核导入模板.xlsx", "采购审核列表", ErpDistributionPurchaseAuditImportExcelVO.class, list);
    }

    @PostMapping("/purchase/import")
    @Operation(summary = "导入代发采购审核")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true)
    })
    @PreAuthorize("@ss.hasPermission('erp:distributionPurchaseAu:import')")
    public CommonResult<ErpDistributionImportRespVO> importPurchaseAuditExcel(
            @RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpDistributionPurchaseAuditImportExcelVO> list = ExcelUtils.read(inputStream, ErpDistributionPurchaseAuditImportExcelVO.class);
            return success(distributionService.importPurchaseAuditList(list));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/sale/get-import-template")
    @Operation(summary = "获得导入代发销售审核模板")
    public void importSaleAuditTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpDistributionSaleAuditImportExcelVO> list = Arrays.asList(
                ErpDistributionSaleAuditImportExcelVO.builder()
                .build()
        );
        // 输出
        ExcelUtils.write(response, "代发销售审核导入模板.xlsx", "销售审核列表", ErpDistributionSaleAuditImportExcelVO.class, list);
    }

    @PostMapping("/sale/import")
    @Operation(summary = "导入代发销售审核")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true)
    })
    @PreAuthorize("@ss.hasPermission('erp:distributionSaleAu:import')")
    public CommonResult<ErpDistributionImportRespVO> importSaleAuditExcel(
            @RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpDistributionSaleAuditImportExcelVO> list = ExcelUtils.read(inputStream, ErpDistributionSaleAuditImportExcelVO.class);
            return success(distributionService.importSaleAuditList(list));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/logistics/get-import-template")
    @Operation(summary = "获得导入代发物流信息模板")
    public void importLogisticsTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<ErpDistributionLogisticsImportTemplateVO> list = Arrays.asList(
                ErpDistributionLogisticsImportTemplateVO.builder()
                .build()
        );
        // 输出
        ExcelUtils.write(response, "代发物流信息导入模板.xlsx", "物流信息列表", ErpDistributionLogisticsImportTemplateVO.class, list);
    }

    @PostMapping("/logistics/import")
    @Operation(summary = "导入代发物流信息")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true)
    })
    @PreAuthorize("@ss.hasPermission('erp:distribution:importLogistics')")
    public CommonResult<ErpDistributionImportRespVO> importLogisticsExcel(
            @RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            List<ErpDistributionLogisticsImportExcelVO> list = ExcelUtils.read(inputStream, ErpDistributionLogisticsImportExcelVO.class);
            return success(distributionService.importLogisticsList(list));
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }



}
