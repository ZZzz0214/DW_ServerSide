package cn.iocoder.yudao.module.erp.controller.admin.wholesale;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.*;
import cn.iocoder.yudao.framework.excel.core.listener.RowIndexListener;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.*;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.*;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesalePurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleSaleMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductESRepository;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalePriceESRepository;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalePriceService;
import cn.iocoder.yudao.module.erp.service.wholesale.ErpWholesaleBaseESRepository;
import cn.iocoder.yudao.module.erp.service.wholesale.ErpWholesalePurchaseESRepository;
import cn.iocoder.yudao.module.erp.service.wholesale.ErpWholesaleSaleESRepository;
import cn.iocoder.yudao.module.erp.service.wholesale.ErpWholesaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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

@Tag(name = "管理后台 - ERP 批发")
@RestController
@RequestMapping("/erp/wholesale")
@Validated
public class ErpWholesaleController {

    @Resource
    private ErpWholesaleService wholesaleService;

    @Resource
    private ErpWholesalePurchaseMapper purchaseMapper;

    @Resource
    private ErpWholesaleSaleMapper saleMapper;

    @Resource
    private ErpComboProductService comboProductService;

    @Resource
    private ErpSalePriceService salePriceService;

    @Resource
    private ErpWholesaleBaseESRepository wholesaleBaseESRepository;
    @Resource
    private ErpWholesalePurchaseESRepository wholesalePurchaseESRepository;
    @Resource
    private ErpWholesaleSaleESRepository wholesaleSaleESRepository;
    @Resource
    private ErpComboProductESRepository comboProductESRepository;
    @Resource
    private ErpSalePriceESRepository salePriceESRepository;

    @PostMapping("/create")
    @Operation(summary = "创建批发")
    //@PreAuthorize("@ss.hasPermission('erp:wholesale:create')")
    @PreAuthorize("@ss.hasAnyPermissions('erp:wholesale:create','erp:wholesale:CopyCreate')")
    public CommonResult<Long> createWholesale(@Valid @RequestBody ErpWholesaleSaveReqVO createReqVO) {
        return success(wholesaleService.createWholesale(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新批发")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:update')")
    public CommonResult<Boolean> updateWholesale(@Valid @RequestBody ErpWholesaleSaveReqVO updateReqVO) {
        wholesaleService.updateWholesale(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除批发")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:wholesale:delete')")
    public CommonResult<Boolean> deleteWholesale(@RequestParam("ids") List<Long> ids) {
        wholesaleService.deleteWholesale(ids);
        return success(true);
    }

//    @GetMapping("/get2")
//    @Operation(summary = "获得批发")
//    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
//    public CommonResult<ErpWholesaleRespVO> getWholesale2(@RequestParam("id") Long id) {
//        // 1. 获取基础信息
//        ErpWholesaleBaseDO wholesale = wholesaleService.getWholesale(id);
//        if (wholesale == null) {
//            return success(null);
//        }
//
//        // 2. 转换为RespVO
//        ErpWholesaleRespVO respVO = BeanUtils.toBean(wholesale, ErpWholesaleRespVO.class);
//
//        // 3. 获取并合并采购信息
//        ErpWholesalePurchaseDO purchase = purchaseMapper.selectByBaseId(id);
//        if (purchase != null) {
//            BeanUtils.copyProperties(purchase, respVO, "id");
//
//            // 通过组品ID获取组品信息并设置相关字段
//            if (purchase.getComboProductId() != null) {
//                ErpComboProductDO comboProduct = comboProductService.getCombo(purchase.getComboProductId());
//                if (comboProduct != null) {
//                    System.out.println("组品信息: " + comboProduct);
//                    respVO.setProductName(comboProduct.getName());
//                    respVO.setShippingCode(comboProduct.getShippingCode());
//                    respVO.setPurchaser(comboProduct.getPurchaser());
//                    respVO.setSupplier(comboProduct.getSupplier());
//                    respVO.setPurchasePrice(comboProduct.getWholesalePrice());
//
//
//                    respVO.setLogisticsFee(purchase.getLogisticsFee());
//
//                    // 计算采购总额 = 出货单价（即组品表的`wholesalePrice`） * 产品数量 + 出货货拉拉费 + 出货物流费用 + 出货杂费
//                    BigDecimal totalAmount = comboProduct.getWholesalePrice()
//                            .multiply(new BigDecimal(wholesale.getProductQuantity()))
//                            .add(purchase.getTruckFee() != null ? purchase.getTruckFee() : BigDecimal.ZERO)
//                            .add(purchase.getLogisticsFee() != null ? purchase.getLogisticsFee() : BigDecimal.ZERO)
//                            .add(purchase.getOtherFees() != null ? purchase.getOtherFees() : BigDecimal.ZERO);
//                            System.out.println("采购总额计算结果: " + totalAmount);
//                    respVO.setTotalPurchaseAmount(totalAmount);
//                }
//            }
//        }
//
//        // 4. 获取并合并销售信息
//        ErpWholesaleSaleDO sale = saleMapper.selectByBaseId(id);
//        if (sale != null) {
//            BeanUtils.copyProperties(sale, respVO, "id","otherFees","truckFee");
//            respVO.setSaleTruckFee(sale.getTruckFee());
//            respVO.setSaleLogisticsFee(sale.getLogisticsFee());
//            respVO.setSaleOtherFees(sale.getOtherFees());
//
//            // 根据客户名称和组品ID查询销售价格
//            if (sale.getCustomerName() != null && purchase != null && purchase.getComboProductId() != null) {
//                ErpSalePriceRespVO salePrice = salePriceService.getSalePriceByGroupProductIdAndCustomerName(
//                        purchase.getComboProductId(), sale.getCustomerName());
//                if (salePrice != null) {
//                    respVO.setSalePrice(salePrice.getWholesalePrice());
//
//                    // 计算销售总额 = 销售单价*数量 + 销售货拉拉费 + 销售物流费用 + 销售其他费用
//                    BigDecimal totalAmount = salePrice.getWholesalePrice()
//                            .multiply(new BigDecimal(wholesale.getProductQuantity()))
//                            .add(sale.getTruckFee() != null ? sale.getTruckFee() : BigDecimal.ZERO)
//                            .add(sale.getLogisticsFee() != null ? sale.getLogisticsFee() : BigDecimal.ZERO)
//                            .add(sale.getOtherFees() != null ? sale.getOtherFees() : BigDecimal.ZERO);
//                    //System.out.println("销售总额计算结果: " + totalAmount);
//                    respVO.setTotalSaleAmount(totalAmount);
//                }
//            }
//        }
//
//        return success(respVO);
//    }

    @GetMapping("/get")
    @Operation(summary = "获得批发")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<ErpWholesaleRespVO> getWholesale(@RequestParam("id") Long id) {
        ErpWholesaleRespVO wholesale = wholesaleService.getWholesale(id);
        if (wholesale == null) {
            return success(null);
        }
        return success(wholesale);
    }


    @GetMapping("/page")
    @Operation(summary = "获得批发分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<PageResult<ErpWholesaleRespVO>> getWholesalePage(@Valid ErpWholesalePageReqVO pageReqVO) {
        PageResult<ErpWholesaleRespVO> pageResult = wholesaleService.getWholesaleVOPage(pageReqVO);
        System.out.println("pageResult:" + pageResult);
        return success(pageResult);
    }
    // 未审核批发采购分页
    @GetMapping("/purchase/unreviewed-page")
    @Operation(summary = "获得未审核批发采购分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesalePurchaseAu:query')")
    public CommonResult<WholesalePurchaseSummaryPageResult<ErpWholesalePurchaseAuditVO>> getUnreviewedPurchasePage(@Valid ErpWholesalePageReqVO pageReqVO) {
            // 获取分页数据
        PageResult<ErpWholesaleRespVO> pageResult = wholesaleService.getWholesaleVOPage(pageReqVO);

            // 转换为ErpWholesalePurchaseAuditVO列表
        List<ErpWholesalePurchaseAuditVO> list = pageResult.getList().stream().map(item -> {
                ErpWholesalePurchaseAuditVO vo = new ErpWholesalePurchaseAuditVO();
                BeanUtils.copyProperties(item, vo);
                return vo;
        }).collect(Collectors.toList());

            // 计算合计值
        BigDecimal totalPurchasePrice = BigDecimal.ZERO;
        BigDecimal totalTruckFee = BigDecimal.ZERO;
        BigDecimal totalLogisticsFee = BigDecimal.ZERO;
        BigDecimal totalOtherFees = BigDecimal.ZERO;
        BigDecimal totalPurchaseAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAfterSalesAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAuditTotalAmount = BigDecimal.ZERO;

        for (ErpWholesalePurchaseAuditVO vo : list) {
            if (vo.getPurchasePrice() != null) {
                totalPurchasePrice = totalPurchasePrice.add(vo.getPurchasePrice());
            }
            if (vo.getTruckFee() != null) {
                totalTruckFee = totalTruckFee.add(vo.getTruckFee());
            }
            if (vo.getLogisticsFee() != null) {
                totalLogisticsFee = totalLogisticsFee.add(vo.getLogisticsFee());
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
        WholesalePurchaseSummaryPageResult<ErpWholesalePurchaseAuditVO> result = new WholesalePurchaseSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, pageResult.getTotal()));
        result.setTotalPurchasePrice(totalPurchasePrice);
        result.setTotalTruckFee(totalTruckFee);
        result.setTotalLogisticsFee(totalLogisticsFee);
        result.setTotalOtherFees(totalOtherFees);
        result.setTotalPurchaseAmount(totalPurchaseAmount);
        result.setTotalPurchaseAfterSalesAmount(totalPurchaseAfterSalesAmount);
        result.setTotalPurchaseAuditTotalAmount(totalPurchaseAuditTotalAmount);

        return success(result);
    }

        // 已审核批发采购分页
    @GetMapping("/purchase/reviewed-page")
    @Operation(summary = "获得已审核批发采购分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<WholesalePurchaseSummaryPageResult<ErpWholesalePurchaseAuditVO>> getReviewedPurchasePage(@Valid ErpWholesalePageReqVO pageReqVO) {
        pageReqVO.setPurchaseAuditStatus(20); // 设置状态为已审核
        PageResult<ErpWholesaleRespVO> pageResult = wholesaleService.getWholesaleVOPage(pageReqVO);

        // 转换为ErpWholesalePurchaseAuditVO列表
        List<ErpWholesalePurchaseAuditVO> list = pageResult.getList().stream().map(item -> {
            ErpWholesalePurchaseAuditVO vo = new ErpWholesalePurchaseAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        // 计算采购合计值
        BigDecimal totalPurchasePrice = BigDecimal.ZERO;
        BigDecimal totalTruckFee = BigDecimal.ZERO;
        BigDecimal totalLogisticsFee = BigDecimal.ZERO;
        BigDecimal totalOtherFees = BigDecimal.ZERO;
        BigDecimal totalPurchaseAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAfterSalesAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAuditTotalAmount = BigDecimal.ZERO;

        for (ErpWholesalePurchaseAuditVO vo : list) {
            if (vo.getPurchasePrice() != null) {
                totalPurchasePrice = totalPurchasePrice.add(vo.getPurchasePrice());
            }
            if (vo.getTruckFee() != null) {
                totalTruckFee = totalTruckFee.add(vo.getTruckFee());
            }
            if (vo.getLogisticsFee() != null) {
                totalLogisticsFee = totalLogisticsFee.add(vo.getLogisticsFee());
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
        WholesalePurchaseSummaryPageResult<ErpWholesalePurchaseAuditVO> result = new WholesalePurchaseSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, pageResult.getTotal()));
        result.setTotalPurchasePrice(totalPurchasePrice);
        result.setTotalTruckFee(totalTruckFee);
        result.setTotalLogisticsFee(totalLogisticsFee);
        result.setTotalOtherFees(totalOtherFees);
        result.setTotalPurchaseAmount(totalPurchaseAmount);
        result.setTotalPurchaseAfterSalesAmount(totalPurchaseAfterSalesAmount);
        result.setTotalPurchaseAuditTotalAmount(totalPurchaseAuditTotalAmount);

        return success(result);
    }
    // 未审核批发销售分页
    @GetMapping("/sale/unreviewed-page")
    @Operation(summary = "获得未审核批发销售分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesaleSaleAu:query')")
    public CommonResult<WholesaleSalesSummaryPageResult<ErpWholesaleSaleAuditVO>> getUnreviewedSalePage(@Valid ErpWholesalePageReqVO pageReqVO) {
        // 获取分页数据
        PageResult<ErpWholesaleRespVO> pageResult = wholesaleService.getWholesaleVOPage(pageReqVO);

        // 转换为ErpWholesaleSaleAuditVO列表
        List<ErpWholesaleSaleAuditVO> list = pageResult.getList().stream().map(item -> {
            ErpWholesaleSaleAuditVO vo = new ErpWholesaleSaleAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        // 计算销售合计值
        BigDecimal totalSalePrice = BigDecimal.ZERO;
        BigDecimal totalSaleTruckFee = BigDecimal.ZERO;
        BigDecimal totalSaleLogisticsFee = BigDecimal.ZERO;
        BigDecimal totalSaleOtherFees = BigDecimal.ZERO;
        BigDecimal totalSaleAmount = BigDecimal.ZERO;
        BigDecimal totalSaleAfterSalesAmount = BigDecimal.ZERO;
        BigDecimal totalSaleAuditTotalAmount = BigDecimal.ZERO;

        for (ErpWholesaleSaleAuditVO vo : list) {
            if (vo.getSalePrice() != null) {
                totalSalePrice = totalSalePrice.add(vo.getSalePrice());
            }
            if (vo.getSaleTruckFee() != null) {
                totalSaleTruckFee = totalSaleTruckFee.add(vo.getSaleTruckFee());
            }
            if (vo.getSaleLogisticsFee() != null) {
                totalSaleLogisticsFee = totalSaleLogisticsFee.add(vo.getSaleLogisticsFee());
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
        WholesaleSalesSummaryPageResult<ErpWholesaleSaleAuditVO> result = new WholesaleSalesSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, pageResult.getTotal()));
        result.setTotalSalePrice(totalSalePrice);
        result.setTotalSaleTruckFee(totalSaleTruckFee);
        result.setTotalSaleLogisticsFee(totalSaleLogisticsFee);
        result.setTotalSaleOtherFees(totalSaleOtherFees);
        result.setTotalSaleAmount(totalSaleAmount);
        result.setTotalSaleAfterSalesAmount(totalSaleAfterSalesAmount);
        result.setTotalSaleAuditTotalAmount(totalSaleAuditTotalAmount);

        return success(result);
    }

    // 已审核批发销售分页
    @GetMapping("/sale/reviewed-page")
    @Operation(summary = "获得已审核批发销售分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<WholesaleSalesSummaryPageResult<ErpWholesaleSaleAuditVO>> getReviewedSalePage(@Valid ErpWholesalePageReqVO pageReqVO) {
        pageReqVO.setSaleAuditStatus(20); // 设置状态为已审核
        PageResult<ErpWholesaleRespVO> pageResult = wholesaleService.getWholesaleVOPage(pageReqVO);

        // 转换为ErpWholesaleSaleAuditVO列表
        List<ErpWholesaleSaleAuditVO> list = pageResult.getList().stream().map(item -> {
            ErpWholesaleSaleAuditVO vo = new ErpWholesaleSaleAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        // 计算销售合计值
        BigDecimal totalSalePrice = BigDecimal.ZERO;
        BigDecimal totalSaleTruckFee = BigDecimal.ZERO;
        BigDecimal totalSaleLogisticsFee = BigDecimal.ZERO;
        BigDecimal totalSaleOtherFees = BigDecimal.ZERO;
        BigDecimal totalSaleAmount = BigDecimal.ZERO;
        BigDecimal totalSaleAfterSalesAmount = BigDecimal.ZERO;
        BigDecimal totalSaleAuditTotalAmount = BigDecimal.ZERO;

        for (ErpWholesaleSaleAuditVO vo : list) {
            if (vo.getSalePrice() != null) {
                totalSalePrice = totalSalePrice.add(vo.getSalePrice());
            }
            if (vo.getSaleTruckFee() != null) {
                totalSaleTruckFee = totalSaleTruckFee.add(vo.getSaleTruckFee());
            }
            if (vo.getSaleLogisticsFee() != null) {
                totalSaleLogisticsFee = totalSaleLogisticsFee.add(vo.getSaleLogisticsFee());
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
        WholesaleSalesSummaryPageResult<ErpWholesaleSaleAuditVO> result = new WholesaleSalesSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, pageResult.getTotal()));
        result.setTotalSalePrice(totalSalePrice);
        result.setTotalSaleTruckFee(totalSaleTruckFee);
        result.setTotalSaleLogisticsFee(totalSaleLogisticsFee);
        result.setTotalSaleOtherFees(totalSaleOtherFees);
        result.setTotalSaleAmount(totalSaleAmount);
        result.setTotalSaleAfterSalesAmount(totalSaleAfterSalesAmount);
        result.setTotalSaleAuditTotalAmount(totalSaleAuditTotalAmount);

        return success(result);
    }

    @GetMapping("/purchase/unreviewed-summary")
    @Operation(summary = "获得未审核批发采购合计")
    @PreAuthorize("@ss.hasPermission('erp:wholesalePurchaseAu:query')")
    public CommonResult<WholesalePurchaseSummaryPageResult<ErpWholesalePurchaseAuditVO>> getUnreviewedPurchaseSummary(@Valid ErpWholesalePageReqVO pageReqVO) {
        // 设置未审核状态
        pageReqVO.setPurchaseAuditStatus(10); // 10表示未审核
        List<ErpWholesaleRespVO> allList = wholesaleService.exportAllWholesales(pageReqVO);

        // 转换为ErpWholesalePurchaseAuditVO列表
        List<ErpWholesalePurchaseAuditVO> list = allList.stream().map(item -> {
            ErpWholesalePurchaseAuditVO vo = new ErpWholesalePurchaseAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        // 计算合计值
        BigDecimal totalPurchasePrice = BigDecimal.ZERO;
        BigDecimal totalTruckFee = BigDecimal.ZERO;
        BigDecimal totalLogisticsFee = BigDecimal.ZERO;
        BigDecimal totalOtherFees = BigDecimal.ZERO;
        BigDecimal totalPurchaseAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAfterSalesAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAuditTotalAmount = BigDecimal.ZERO;

        for (ErpWholesalePurchaseAuditVO vo : list) {
            if (vo.getPurchasePrice() != null) {
                totalPurchasePrice = totalPurchasePrice.add(vo.getPurchasePrice());
            }
            if (vo.getTruckFee() != null) {
                totalTruckFee = totalTruckFee.add(vo.getTruckFee());
            }
            if (vo.getLogisticsFee() != null) {
                totalLogisticsFee = totalLogisticsFee.add(vo.getLogisticsFee());
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
        WholesalePurchaseSummaryPageResult<ErpWholesalePurchaseAuditVO> result = new WholesalePurchaseSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, (long) list.size()));
        result.setTotalPurchasePrice(totalPurchasePrice);
        result.setTotalTruckFee(totalTruckFee);
        result.setTotalLogisticsFee(totalLogisticsFee);
        result.setTotalOtherFees(totalOtherFees);
        result.setTotalPurchaseAmount(totalPurchaseAmount);
        result.setTotalPurchaseAfterSalesAmount(totalPurchaseAfterSalesAmount);
        result.setTotalPurchaseAuditTotalAmount(totalPurchaseAuditTotalAmount);

        return success(result);
    }

    @GetMapping("/purchase/reviewed-summary")
    @Operation(summary = "获得已审核批发采购合计")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<WholesalePurchaseSummaryPageResult<ErpWholesalePurchaseAuditVO>> getReviewedPurchaseSummary(@Valid ErpWholesalePageReqVO pageReqVO) {
        // 设置已审核状态
        pageReqVO.setPurchaseAuditStatus(20); // 20表示已审核
        List<ErpWholesaleRespVO> allList = wholesaleService.exportAllWholesales(pageReqVO);

        // 转换为ErpWholesalePurchaseAuditVO列表
        List<ErpWholesalePurchaseAuditVO> list = allList.stream().map(item -> {
            ErpWholesalePurchaseAuditVO vo = new ErpWholesalePurchaseAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        // 计算合计值
        BigDecimal totalPurchasePrice = BigDecimal.ZERO;
        BigDecimal totalTruckFee = BigDecimal.ZERO;
        BigDecimal totalLogisticsFee = BigDecimal.ZERO;
        BigDecimal totalOtherFees = BigDecimal.ZERO;
        BigDecimal totalPurchaseAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAfterSalesAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAuditTotalAmount = BigDecimal.ZERO;

        for (ErpWholesalePurchaseAuditVO vo : list) {
            if (vo.getPurchasePrice() != null) {
                totalPurchasePrice = totalPurchasePrice.add(vo.getPurchasePrice());
            }
            if (vo.getTruckFee() != null) {
                totalTruckFee = totalTruckFee.add(vo.getTruckFee());
            }
            if (vo.getLogisticsFee() != null) {
                totalLogisticsFee = totalLogisticsFee.add(vo.getLogisticsFee());
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
        WholesalePurchaseSummaryPageResult<ErpWholesalePurchaseAuditVO> result = new WholesalePurchaseSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, (long) list.size()));
        result.setTotalPurchasePrice(totalPurchasePrice);
        result.setTotalTruckFee(totalTruckFee);
        result.setTotalLogisticsFee(totalLogisticsFee);
        result.setTotalOtherFees(totalOtherFees);
        result.setTotalPurchaseAmount(totalPurchaseAmount);
        result.setTotalPurchaseAfterSalesAmount(totalPurchaseAfterSalesAmount);
        result.setTotalPurchaseAuditTotalAmount(totalPurchaseAuditTotalAmount);

        return success(result);
    }

    @GetMapping("/sale/unreviewed-summary")
    @Operation(summary = "获得未审核批发销售合计")
    @PreAuthorize("@ss.hasPermission('erp:wholesaleSaleAu:query')")
    public CommonResult<WholesaleSalesSummaryPageResult<ErpWholesaleSaleAuditVO>> getUnreviewedSaleSummary(@Valid ErpWholesalePageReqVO pageReqVO) {
        // 设置未审核状态
        pageReqVO.setSaleAuditStatus(10); // 10表示未审核
        List<ErpWholesaleRespVO> allList = wholesaleService.exportAllWholesales(pageReqVO);

        // 转换为ErpWholesaleSaleAuditVO列表
        List<ErpWholesaleSaleAuditVO> list = allList.stream().map(item -> {
            ErpWholesaleSaleAuditVO vo = new ErpWholesaleSaleAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        // 计算销售合计值
        BigDecimal totalSalePrice = BigDecimal.ZERO;
        BigDecimal totalSaleTruckFee = BigDecimal.ZERO;
        BigDecimal totalSaleLogisticsFee = BigDecimal.ZERO;
        BigDecimal totalSaleOtherFees = BigDecimal.ZERO;
        BigDecimal totalSaleAmount = BigDecimal.ZERO;
        BigDecimal totalSaleAfterSalesAmount = BigDecimal.ZERO;
        BigDecimal totalSaleAuditTotalAmount = BigDecimal.ZERO;

        for (ErpWholesaleSaleAuditVO vo : list) {
            if (vo.getSalePrice() != null) {
                totalSalePrice = totalSalePrice.add(vo.getSalePrice());
            }
            if (vo.getSaleTruckFee() != null) {
                totalSaleTruckFee = totalSaleTruckFee.add(vo.getSaleTruckFee());
            }
            if (vo.getSaleLogisticsFee() != null) {
                totalSaleLogisticsFee = totalSaleLogisticsFee.add(vo.getSaleLogisticsFee());
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
        WholesaleSalesSummaryPageResult<ErpWholesaleSaleAuditVO> result = new WholesaleSalesSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, (long) list.size()));
        result.setTotalSalePrice(totalSalePrice);
        result.setTotalSaleTruckFee(totalSaleTruckFee);
        result.setTotalSaleLogisticsFee(totalSaleLogisticsFee);
        result.setTotalSaleOtherFees(totalSaleOtherFees);
        result.setTotalSaleAmount(totalSaleAmount);
        result.setTotalSaleAfterSalesAmount(totalSaleAfterSalesAmount);
        result.setTotalSaleAuditTotalAmount(totalSaleAuditTotalAmount);

        return success(result);
    }

    @GetMapping("/sale/reviewed-summary")
    @Operation(summary = "获得已审核批发销售合计")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<WholesaleSalesSummaryPageResult<ErpWholesaleSaleAuditVO>> getReviewedSaleSummary(@Valid ErpWholesalePageReqVO pageReqVO) {
        // 设置已审核状态
        pageReqVO.setSaleAuditStatus(20); // 20表示已审核
        List<ErpWholesaleRespVO> allList = wholesaleService.exportAllWholesales(pageReqVO);

        // 转换为ErpWholesaleSaleAuditVO列表
        List<ErpWholesaleSaleAuditVO> list = allList.stream().map(item -> {
            ErpWholesaleSaleAuditVO vo = new ErpWholesaleSaleAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        // 计算销售合计值
        BigDecimal totalSalePrice = BigDecimal.ZERO;
        BigDecimal totalSaleTruckFee = BigDecimal.ZERO;
        BigDecimal totalSaleLogisticsFee = BigDecimal.ZERO;
        BigDecimal totalSaleOtherFees = BigDecimal.ZERO;
        BigDecimal totalSaleAmount = BigDecimal.ZERO;
        BigDecimal totalSaleAfterSalesAmount = BigDecimal.ZERO;
        BigDecimal totalSaleAuditTotalAmount = BigDecimal.ZERO;

        for (ErpWholesaleSaleAuditVO vo : list) {
            if (vo.getSalePrice() != null) {
                totalSalePrice = totalSalePrice.add(vo.getSalePrice());
            }
            if (vo.getSaleTruckFee() != null) {
                totalSaleTruckFee = totalSaleTruckFee.add(vo.getSaleTruckFee());
            }
            if (vo.getSaleLogisticsFee() != null) {
                totalSaleLogisticsFee = totalSaleLogisticsFee.add(vo.getSaleLogisticsFee());
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
        WholesaleSalesSummaryPageResult<ErpWholesaleSaleAuditVO> result = new WholesaleSalesSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, (long) list.size()));
        result.setTotalSalePrice(totalSalePrice);
        result.setTotalSaleTruckFee(totalSaleTruckFee);
        result.setTotalSaleLogisticsFee(totalSaleLogisticsFee);
        result.setTotalSaleOtherFees(totalSaleOtherFees);
        result.setTotalSaleAmount(totalSaleAmount);
        result.setTotalSaleAfterSalesAmount(totalSaleAfterSalesAmount);
        result.setTotalSaleAuditTotalAmount(totalSaleAuditTotalAmount);

        return success(result);
    }

        // 获取采购详情
//    @GetMapping("/purchase/get2")
//    @Operation(summary = "获得批发采购详情")
//    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
//    public CommonResult<ErpWholesalePurchaseAuditVO> getWholesalePurchase2(@RequestParam("id") Long id) {
//        // 1. 获取基础信息
//        ErpWholesaleRespVO wholesale = wholesaleService.getWholesale(id);
//        if (wholesale == null) {
//            return success(null);
//        }
//
//        // 2. 转换为ErpWholesalePurchaseAuditVO
//        ErpWholesalePurchaseAuditVO vo = new ErpWholesalePurchaseAuditVO();
//        BeanUtils.copyProperties(wholesale, vo);
//
//        // 3. 获取并合并采购信息
//        ErpWholesalePurchaseDO purchase = purchaseMapper.selectByBaseId(id);
//        if (purchase != null) {
//            BeanUtils.copyProperties(purchase, vo, "id");
//
//            // 通过组品ID获取组品信息并设置相关字段
//            if (purchase.getComboProductId() != null) {
//                ErpComboProductDO comboProduct = comboProductService.getCombo(purchase.getComboProductId());
//                if (comboProduct != null) {
//                    vo.setProductName(comboProduct.getName());
//                    vo.setShippingCode(comboProduct.getShippingCode());
//                    vo.setPurchaser(comboProduct.getPurchaser());
//                    vo.setSupplier(comboProduct.getSupplier());
//                    vo.setPurchasePrice(comboProduct.getWholesalePrice());
//
//
//                    vo.setLogisticsFee(purchase.getLogisticsFee());
//                    // 计算采购总额
//                    BigDecimal totalAmount = comboProduct.getWholesalePrice()
//                            .multiply(new BigDecimal(wholesale.getProductQuantity()))
//                            .add(purchase.getTruckFee() != null ? purchase.getTruckFee() : BigDecimal.ZERO)
//                            .add(purchase.getLogisticsFee() != null ? purchase.getLogisticsFee() : BigDecimal.ZERO)
//                            .add(purchase.getOtherFees() != null ? purchase.getOtherFees() : BigDecimal.ZERO);
//                    vo.setTotalPurchaseAmount(totalAmount);
//                    System.out.println("成功设置采购总额："+vo.getTotalPurchaseAmount());
//                }
//            }
//        }
//
//        return success(vo);
//    }
//
//    // 获取销售详情
//    @GetMapping("/sale/get2")
//    @Operation(summary = "获得批发销售详情")
//    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
//    public CommonResult<ErpWholesaleSaleAuditVO> getWholesaleSale2(@RequestParam("id") Long id) {
//        // 1. 获取基础信息
//        ErpWholesaleBaseDO wholesale = wholesaleService.getWholesale(id);
//        if (wholesale == null) {
//            return success(null);
//        }
//
//        // 2. 转换为ErpWholesaleSaleAuditVO
//        ErpWholesaleSaleAuditVO vo = new ErpWholesaleSaleAuditVO();
//        BeanUtils.copyProperties(wholesale, vo);
//
//        // 3. 获取并合并销售信息
//        ErpWholesaleSaleDO sale = saleMapper.selectByBaseId(id);
//        if (sale != null) {
//            BeanUtils.copyProperties(sale, vo, "id");
//            vo.setSaleTruckFee(sale.getTruckFee());
//            vo.setSaleOtherFees(sale.getOtherFees());
//
//            // 根据客户名称和组品ID查询销售价格
//            ErpWholesalePurchaseDO purchase = purchaseMapper.selectByBaseId(id);
//            if (sale.getCustomerName() != null && purchase != null && purchase.getComboProductId() != null) {
//                ErpSalePriceRespVO salePrice = salePriceService.getSalePriceByGroupProductIdAndCustomerName(
//                        purchase.getComboProductId(), sale.getCustomerName());
//                if (salePrice != null) {
//                    vo.setSalePrice(salePrice.getWholesalePrice());
//
//                    // 计算销售总额 = 销售单价*数量 + 销售货拉拉费 + 销售物流费用 + 销售其他费用
//                    BigDecimal totalAmount = salePrice.getWholesalePrice()
//                            .multiply(new BigDecimal(wholesale.getProductQuantity()))
//                            .add(sale.getTruckFee() != null ? sale.getTruckFee() : BigDecimal.ZERO)
//                            .add(sale.getLogisticsFee() != null ? sale.getLogisticsFee() : BigDecimal.ZERO)
//                            .add(sale.getOtherFees() != null ? sale.getOtherFees() : BigDecimal.ZERO);
//                    System.out.println("销售总额计算结果: " + totalAmount);
//                    vo.setTotalSaleAmount(totalAmount);
//                }
//            }
//        }
//
//        return success(vo);
//    }


    @GetMapping("/purchase/get")
    @Operation(summary = "获得批发采购详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<ErpWholesalePurchaseAuditVO> getWholesalePurchase(@RequestParam("id") Long id) {
        try {

            ErpWholesaleRespVO wholesale = wholesaleService.getWholesale(id);
            if (wholesale == null) {
            return success(null);
        }

           //  2. 转换为ErpWholesalePurchaseAuditVO
        ErpWholesalePurchaseAuditVO vo = new ErpWholesalePurchaseAuditVO();
        BeanUtils.copyProperties(wholesale, vo);

//            // 1. 从ES获取基础信息
//            Optional<ErpWholesaleBaseESDO> baseOpt = wholesaleBaseESRepository.findById(id);
//            if (!baseOpt.isPresent()) {
//                return success(null);
//            }
//            ErpWholesaleBaseESDO wholesale = baseOpt.get();
//
//            // 2. 转换为VO对象
//            ErpWholesalePurchaseAuditVO vo = BeanUtils.toBean(wholesale, ErpWholesalePurchaseAuditVO.class);
//
//            // 3. 从ES获取并合并采购信息
//            Optional<ErpWholesalePurchaseESDO> purchaseOpt = wholesalePurchaseESRepository.findByBaseId(id);
//
//            if (purchaseOpt.isPresent()) {
//                ErpWholesalePurchaseESDO purchase = purchaseOpt.get();
//                System.out.println("售后查看："+purchase);
//                BeanUtils.copyProperties(purchase, vo, "id");
//
//                // 通过组品ID从ES获取组品信息并设置相关字段
//                if (purchase.getComboProductId() != null) {
//                    Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(purchase.getComboProductId());
//                    if (comboProductOpt.isPresent()) {
//                        ErpComboProductES comboProduct = comboProductOpt.get();
//                        vo.setProductName(comboProduct.getName());
//                        vo.setShippingCode(comboProduct.getShippingCode());
//                        vo.setPurchaser(comboProduct.getPurchaser());
//                        vo.setSupplier(comboProduct.getSupplier());
//                        vo.setPurchasePrice(comboProduct.getWholesalePrice());
//
//
//                        vo.setLogisticsFee(purchase.getLogisticsFee());
//
//                        // 计算采购总额
//                        BigDecimal totalAmount = comboProduct.getWholesalePrice()
//                                .multiply(new BigDecimal(wholesale.getProductQuantity()))
//                                .add(purchase.getTruckFee() != null ? purchase.getTruckFee() : BigDecimal.ZERO)
//                                .add(purchase.getLogisticsFee() != null ? purchase.getLogisticsFee() : BigDecimal.ZERO)
//                                .add(purchase.getOtherFees() != null ? purchase.getOtherFees() : BigDecimal.ZERO);
//                        vo.setTotalPurchaseAmount(totalAmount);
//                    }
//                }
//            }

            return success(vo);
        } catch (Exception e) {
            System.out.println("ES查询失败，回退到数据库查询: " + e.getMessage());
            return null;
        }
    }
    @GetMapping("/sale/get")
    @Operation(summary = "获得批发销售详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<ErpWholesaleSaleAuditVO> getWholesaleSale(@RequestParam("id") Long id) {
        try {

                    // 1. 获取基础信息
            ErpWholesaleRespVO wholesale = wholesaleService.getWholesale(id);
        if (wholesale == null) {
            return success(null);
        }

        // 2. 转换为ErpWholesaleSaleAuditVO
        ErpWholesaleSaleAuditVO vo = new ErpWholesaleSaleAuditVO();
        BeanUtils.copyProperties(wholesale, vo);
//
//            // 1. 从ES获取基础信息
//            Optional<ErpWholesaleBaseESDO> baseOpt = wholesaleBaseESRepository.findById(id);
//            if (!baseOpt.isPresent()) {
//                return success(null);
//            }
//            ErpWholesaleBaseESDO wholesale = baseOpt.get();
//
//            // 2. 转换为VO对象
//            ErpWholesaleSaleAuditVO vo = BeanUtils.toBean(wholesale, ErpWholesaleSaleAuditVO.class);
//
//            // 3. 从ES获取并合并销售信息
//            Optional<ErpWholesaleSaleESDO> saleOpt = wholesaleSaleESRepository.findByBaseId(id);
//            if (saleOpt.isPresent()) {
//                ErpWholesaleSaleESDO sale = saleOpt.get();
//                BeanUtils.copyProperties(sale, vo, "id");
//                vo.setSaleOtherFees(sale.getOtherFees());
//                vo.setSaleLogisticsFee(sale.getLogisticsFee());
//                vo.setSaleTruckFee(sale.getTruckFee());
//
//                // 根据客户名称和组品ID从ES查询销售价格
//                Optional<ErpWholesalePurchaseESDO> purchaseOpt = wholesalePurchaseESRepository.findByBaseId(id);
//                if (sale.getCustomerName() != null && purchaseOpt.isPresent() &&
//                    purchaseOpt.get().getComboProductId() != null) {
//
//                    Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
//                            purchaseOpt.get().getComboProductId(), sale.getCustomerName());
//                    if (salePriceOpt.isPresent()) {
//                        ErpSalePriceESDO salePrice = salePriceOpt.get();
//                        vo.setSalePrice(salePrice.getWholesalePrice());
//
//                        // 计算销售总额
//                        BigDecimal totalAmount = salePrice.getWholesalePrice()
//                                .multiply(new BigDecimal(wholesale.getProductQuantity()))
//                                .add(sale.getTruckFee() != null ? sale.getTruckFee() : BigDecimal.ZERO)
//                                .add(sale.getLogisticsFee() != null ? sale.getLogisticsFee() : BigDecimal.ZERO)
//                                .add(sale.getOtherFees() != null ? sale.getOtherFees() : BigDecimal.ZERO);
//                        vo.setTotalSaleAmount(totalAmount);
//                    }
//                }
//            }

            return success(vo);
        } catch (Exception e) {
            System.out.println("ES查询失败，回退到数据库查询: " + e.getMessage());
            return null;
        }
    }

//    private CommonResult<ErpWholesalePurchaseAuditVO> getWholesalePurchaseFromDB(Long id) {
//        // 原有数据库查询逻辑...
//        return getWholesalePurchase2(id);
//    }
//
//    private CommonResult<ErpWholesaleSaleAuditVO> getWholesaleSaleFromDB(Long id) {
//        // 原有数据库查询逻辑...
//        return getWholesaleSale2(id);
//    }
    // 更新采购审核状态
    @PutMapping("/update-purchase-audit-status")
    @Operation(summary = "更新采购审核状态")
    @PreAuthorize("@ss.hasPermission('erp:wholesalePurchaseAu:update-status-one')")
    public CommonResult<Boolean> updatePurchaseAuditStatus(@RequestParam("id") Long id,
                                                           @RequestParam("purchaseAuditStatus") Integer purchaseAuditStatus,
                                                           @RequestParam("otherFees") BigDecimal otherFees,
                                                           @RequestParam(value = "purchaseAuditTotalAmount", required = false) BigDecimal purchaseAuditTotalAmount) {
        System.out.println("更改的订单id为 " + id);
        System.out.println("更改的采购审核状态为 " + purchaseAuditStatus);
        System.out.println("其他费用为 " + otherFees);
        System.out.println("批发采购审核总额为 " + purchaseAuditTotalAmount);
        wholesaleService.updatePurchaseAuditStatus(id, purchaseAuditStatus, otherFees, purchaseAuditTotalAmount);
        return success(true);
    }

    @PutMapping("/batch-update-purchase-audit-status")
    @Operation(summary = "批量更新采购审核状态")
    @PreAuthorize("@ss.hasPermission('erp:wholesalePurchaseAu:update-status')")
    public CommonResult<Boolean> batchUpdatePurchaseAuditStatus(@RequestBody @Valid ErpWholesaleBatchUpdatePurchaseAuditReqVO reqVO) {
        wholesaleService.batchUpdatePurchaseAuditStatus(reqVO);
        return success(true);
    }

        // 更新销售审核状态
    @PutMapping("/update-sale-audit-status")
    @Operation(summary = "更新销售审核状态")
    @PreAuthorize("@ss.hasPermission('erp:wholesaleSaleAu:update-status-one')")
    public CommonResult<Boolean> updateSaleAuditStatus(@RequestParam("id") Long id,
                                                      @RequestParam("saleAuditStatus") Integer saleAuditStatus,
                                                      @RequestParam("otherFees") BigDecimal otherFees,
                                                      @RequestParam(value = "saleAuditTotalAmount", required = false) BigDecimal saleAuditTotalAmount) {
        System.out.println("更改的订单id为 " + id);
        System.out.println("更改的销售审核状态为 " + saleAuditStatus);
        System.out.println("其他费用为 " + otherFees);
        System.out.println("批发销售审核总额为 " + saleAuditTotalAmount);
        wholesaleService.updateSaleAuditStatus(id, saleAuditStatus, otherFees, saleAuditTotalAmount);
        return success(true);
    }

    @PutMapping("/batch-update-sale-audit-status")
    @Operation(summary = "批量更新销售审核状态")
    @PreAuthorize("@ss.hasPermission('erp:wholesaleSaleAu:update-status')")
    public CommonResult<Boolean> batchUpdateSaleAuditStatus(@RequestParam("ids") List<Long> ids,
                                                            @RequestParam("saleAuditStatus") Integer saleAuditStatus) {
        wholesaleService.batchUpdateSaleAuditStatus(ids, saleAuditStatus);
        return success(true);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据ID列表获得批发列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<List<ErpWholesaleRespVO>> getWholesaleListByIds(@RequestParam("ids") List<Long> ids) {
        List<ErpWholesaleRespVO> list = wholesaleService.getWholesaleVOList(ids);
        return success(list);
    }
    @PutMapping("/update-purchase-after-sales")
    @Operation(summary = "更新采购售后信息")
   // @PreAuthorize("@ss.hasPermission('erp:wholesale:update-after-sales-one')")
    public CommonResult<Boolean> updatePurchaseAfterSales(@Valid @RequestBody ErpWholesalePurchaseAfterSalesUpdateReqVO reqVO) {
        wholesaleService.updatePurchaseAfterSales(reqVO);
        return success(true);
    }

    // 更新销售售后信息
    @PutMapping("/update-sale-after-sales")
    @Operation(summary = "更新销售售后信息")
    //@PreAuthorize("@ss.hasPermission('erp:wholesale:update-after-sales-one')")
    public CommonResult<Boolean> updateSaleAfterSales(@Valid @RequestBody ErpWholesaleSaleAfterSalesUpdateReqVO reqVO) {
        System.out.println("销售售后前端传递的vo"+reqVO);
        wholesaleService.updateSaleAfterSales(reqVO);
        return success(true);
    }

    @PutMapping("/batch-update-purchase-after-sales")
    @Operation(summary = "批量更新采购售后状态")
   // @PreAuthorize("@ss.hasPermission('erp:wholesale:update-after-sales')")
    public CommonResult<Boolean> batchUpdatePurchaseAfterSales(@RequestParam("ids") List<Long> ids,
                                                               @RequestParam("purchaseAfterSalesStatus") Integer purchaseAfterSalesStatus) {
        wholesaleService.batchUpdatePurchaseAfterSales(ids, purchaseAfterSalesStatus);
        return success(true);
    }

    @PutMapping("/batch-update-sale-after-sales")
    @Operation(summary = "批量更新销售售后状态")
    //@PreAuthorize("@ss.hasPermission('erp:wholesale:update-after-sales')")
    public CommonResult<Boolean> batchUpdateSaleAfterSales(@RequestParam("ids") List<Long> ids,
                                                           @RequestParam("saleAfterSalesStatus") Integer saleAfterSalesStatus) {
        wholesaleService.batchUpdateSaleAfterSales(ids, saleAfterSalesStatus);
        return success(true);
    }

    @GetMapping("/export-basic")
    @Operation(summary = "导出批发基础订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:importBasic')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportBasicWholesaleExcel(@Valid ErpWholesalePageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        System.out.println("开始导出批发基础订单数据...");
        long startTime = System.currentTimeMillis();

        // 使用导出全部数据的方法，不使用分页
        List<ErpWholesaleRespVO> dataList = wholesaleService.exportAllWholesales(pageReqVO);
        System.out.println("获取批发基础订单数据完成，共 " + dataList.size() + " 条");

        // 转换为导出VO
        List<ErpWholesaleBasicExportExcelVO> exportList = BeanUtils.toBean(dataList, ErpWholesaleBasicExportExcelVO.class);
        System.out.println("转换为基础导出VO完成");

        // 导出Excel
        ExcelUtils.write(response, "批发基础订单信息.xlsx", "数据", ErpWholesaleBasicExportExcelVO.class, exportList);

        long endTime = System.currentTimeMillis();
        System.out.println("批发基础订单导出完成，耗时：" + (endTime - startTime) + "ms");
    }

    @GetMapping("/export-purchase")
    @Operation(summary = "导出批发采购订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:importPurchase')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseWholesaleExcel(@Valid ErpWholesalePageReqVO pageReqVO,
                                           HttpServletResponse response) throws IOException {
        System.out.println("开始导出批发采购订单数据...");
        long startTime = System.currentTimeMillis();

        // 使用导出全部数据的方法，不使用分页
        List<ErpWholesaleRespVO> dataList = wholesaleService.exportAllWholesales(pageReqVO);
        System.out.println("获取批发采购订单数据完成，共 " + dataList.size() + " 条");

        // 转换为导出VO
        List<ErpWholesalePurchaseExportExcelVO> exportList = BeanUtils.toBean(dataList, ErpWholesalePurchaseExportExcelVO.class);
        System.out.println("转换为采购导出VO完成");

        // 导出Excel
        ExcelUtils.write(response, "批发采购订单信息.xlsx", "数据", ErpWholesalePurchaseExportExcelVO.class, exportList);

        long endTime = System.currentTimeMillis();
        System.out.println("批发采购订单导出完成，耗时：" + (endTime - startTime) + "ms");
    }

    @GetMapping("/export-sale")
    @Operation(summary = "导出批发出货订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:importSale')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSaleWholesaleExcel(@Valid ErpWholesalePageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        System.out.println("开始导出批发出货订单数据...");
        long startTime = System.currentTimeMillis();

        // 使用导出全部数据的方法，不使用分页
        List<ErpWholesaleRespVO> dataList = wholesaleService.exportAllWholesales(pageReqVO);
        System.out.println("获取批发出货订单数据完成，共 " + dataList.size() + " 条");

        // 转换为导出VO
        List<ErpWholesaleSaleExportExcelVO> exportList = BeanUtils.toBean(dataList, ErpWholesaleSaleExportExcelVO.class);
        System.out.println("转换为出货导出VO完成");

        // 导出Excel
        ExcelUtils.write(response, "批发出货订单信息.xlsx", "数据", ErpWholesaleSaleExportExcelVO.class, exportList);

        long endTime = System.currentTimeMillis();
        System.out.println("批发出货订单导出完成，耗时：" + (endTime - startTime) + "ms");
    }

    @GetMapping("/export-ship")
    @Operation(summary = "导出批发发货订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:importShip')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportShipWholesaleExcel(@Valid ErpWholesalePageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        System.out.println("开始导出批发发货订单数据...");
        long startTime = System.currentTimeMillis();

        // 使用导出全部数据的方法，不使用分页
        List<ErpWholesaleRespVO> dataList = wholesaleService.exportAllWholesales(pageReqVO);
        System.out.println("获取批发发货订单数据完成，共 " + dataList.size() + " 条");

        // 转换为导出VO
        List<ErpWholesaleShipExportExcelVO> exportList = BeanUtils.toBean(dataList, ErpWholesaleShipExportExcelVO.class);
        System.out.println("转换为发货导出VO完成");

        // 导出Excel
        ExcelUtils.write(response, "批发发货订单信息.xlsx", "数据", ErpWholesaleShipExportExcelVO.class, exportList);

        long endTime = System.currentTimeMillis();
        System.out.println("批发发货订单导出完成，耗时：" + (endTime - startTime) + "ms");
    }


        // 采购审批导出
        @GetMapping("/purchase/export-approved")
        @Operation(summary = "导出批发采购订单")
        @PreAuthorize("@ss.hasPermission('erp:wholesalePurchaseAu:export')")
        @ApiAccessLog(operateType = EXPORT)
        public void exportApprovedPurchaseExcel(@Valid ErpWholesalePageReqVO pageReqVO,
                                              HttpServletResponse response) throws IOException {
            System.out.println("开始导出批发采购订单数据...");
            long startTime = System.currentTimeMillis();

            // 使用导出全部数据的方法，不使用分页
            List<ErpWholesaleRespVO> dataList = wholesaleService.exportAllWholesales(pageReqVO);
            System.out.println("获取批发采购订单数据完成，共 " + dataList.size() + " 条");

            // 转换为导出VO
            List<ErpWholesalePurchaseAuditExportVO> exportList = BeanUtils.toBean(dataList, ErpWholesalePurchaseAuditExportVO.class);
            System.out.println("转换为采购导出VO完成");

            // 导出Excel
            ExcelUtils.write(response, "批发采购订单.xlsx", "数据", ErpWholesalePurchaseAuditExportVO.class, exportList);

            long endTime = System.currentTimeMillis();
            System.out.println("批发采购订单导出完成，耗时：" + (endTime - startTime) + "ms");
        }

        // 采购反审批导出
        @GetMapping("/purchase/export-unapproved")
        @Operation(summary = "导出已反审核批发采购订单")
        @PreAuthorize("@ss.hasPermission('erp:wholesale:export')")
        @ApiAccessLog(operateType = EXPORT)
        public void exportUnapprovedPurchaseExcel(@Valid ErpWholesalePageReqVO pageReqVO,
                                                HttpServletResponse response) throws IOException {
            System.out.println("开始导出已反审核批发采购订单数据...");
            long startTime = System.currentTimeMillis();

            // 设置反审核状态过滤条件
            pageReqVO.setPurchaseAuditStatus(20); // 已反审核状态

            // 使用导出全部数据的方法，不使用分页
            List<ErpWholesaleRespVO> dataList = wholesaleService.exportAllWholesales(pageReqVO);
            System.out.println("获取已反审核批发采购订单数据完成，共 " + dataList.size() + " 条");

            // 转换为导出VO
            List<ErpWholesalePurchaseAuditExportVO> exportList = BeanUtils.toBean(dataList, ErpWholesalePurchaseAuditExportVO.class);
            System.out.println("转换为采购导出VO完成");

            // 导出Excel
            ExcelUtils.write(response, "已反审核批发采购订单.xlsx", "数据", ErpWholesalePurchaseAuditExportVO.class, exportList);

            long endTime = System.currentTimeMillis();
            System.out.println("已反审核批发采购订单导出完成，耗时：" + (endTime - startTime) + "ms");
        }

        // 销售审批导出
        @GetMapping("/sale/export-approved")
        @Operation(summary = "导出批发销售订单")
        @PreAuthorize("@ss.hasPermission('erp:wholesaleSaleAu:export')")
        @ApiAccessLog(operateType = EXPORT)
        public void exportApprovedSaleExcel(@Valid ErpWholesalePageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
            System.out.println("开始导出批发销售订单数据...");
            long startTime = System.currentTimeMillis();

            // 使用导出全部数据的方法，不使用分页
            List<ErpWholesaleRespVO> dataList = wholesaleService.exportAllWholesales(pageReqVO);
            System.out.println("获取批发销售订单数据完成，共 " + dataList.size() + " 条");

            // 转换为导出VO
            List<ErpWholesaleSaleAuditExportVO> exportList = BeanUtils.toBean(dataList, ErpWholesaleSaleAuditExportVO.class);
            System.out.println("转换为销售导出VO完成");

            // 导出Excel
            ExcelUtils.write(response, "批发销售订单.xlsx", "数据", ErpWholesaleSaleAuditExportVO.class, exportList);

            long endTime = System.currentTimeMillis();
            System.out.println("批发销售订单导出完成，耗时：" + (endTime - startTime) + "ms");
        }

        // 销售反审批导出
        @GetMapping("/sale/export-unapproved")
        @Operation(summary = "导出已反审核批发销售订单")
        @PreAuthorize("@ss.hasPermission('erp:wholesale:export')")
        @ApiAccessLog(operateType = EXPORT)
        public void exportUnapprovedSaleExcel(@Valid ErpWholesalePageReqVO pageReqVO,
                                            HttpServletResponse response) throws IOException {
            System.out.println("开始导出已反审核批发销售订单数据...");
            long startTime = System.currentTimeMillis();

            // 设置反审核状态过滤条件
            pageReqVO.setSaleAuditStatus(20); // 已反审核状态

            // 使用导出全部数据的方法，不使用分页
            List<ErpWholesaleRespVO> dataList = wholesaleService.exportAllWholesales(pageReqVO);
            System.out.println("获取已反审核批发销售订单数据完成，共 " + dataList.size() + " 条");

            // 转换为导出VO
            List<ErpWholesaleSaleAuditExportVO> exportList = BeanUtils.toBean(dataList, ErpWholesaleSaleAuditExportVO.class);
            System.out.println("转换为销售导出VO完成");

            // 导出Excel
            ExcelUtils.write(response, "已反审核批发销售订单.xlsx", "数据", ErpWholesaleSaleAuditExportVO.class, exportList);

            long endTime = System.currentTimeMillis();
            System.out.println("已反审核批发销售订单导出完成，耗时：" + (endTime - startTime) + "ms");
        }


        @GetMapping("/get-import-template")
        @Operation(summary = "获得导入批发模板")
        public void importTemplate(HttpServletResponse response) throws IOException {
            // 手动创建导出 demo
            List<ErpWholesaleImportTemplateExcelVO> list = Arrays.asList(
            );
            // 输出
            ExcelUtils.write(response, "批发导入模板.xlsx", "批发列表", ErpWholesaleImportTemplateExcelVO.class, list);
        }

        @PostMapping("/import")
        @Operation(summary = "导入批发")
        @Parameters({
                @Parameter(name = "file", description = "Excel 文件", required = true),
                @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
        })
        @PreAuthorize("@ss.hasPermission('erp:wholesale:import')")
        public CommonResult<ErpWholesaleImportRespVO> importExcel(
                @RequestParam("file") MultipartFile file,
                @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
            try (InputStream inputStream = file.getInputStream()) {
                List<ErpWholesaleImportExcelVO> list = ExcelUtils.read(inputStream, ErpWholesaleImportExcelVO.class, new RowIndexListener<>());
                return success(wholesaleService.importWholesaleList(list, updateSupport));
            } catch (Exception e) {
                throw new RuntimeException("导入失败: " + e.getMessage());
            }
        }

        @GetMapping("/get-purchase-audit-import-template")
        @Operation(summary = "获得批发采购审核导入模板")
        public void importPurchaseAuditTemplate(HttpServletResponse response) throws IOException {
            // 手动创建导出 demo
            List<ErpWholesalePurchaseAuditImportExcelVO> list = Arrays.asList(
                    ErpWholesalePurchaseAuditImportExcelVO.builder()
                            .build(),
                    ErpWholesalePurchaseAuditImportExcelVO.builder()
                            .build()
            );
            // 输出
            ExcelUtils.write(response, "批发采购审核导入模板.xlsx", "批发采购审核列表", ErpWholesalePurchaseAuditImportExcelVO.class, list);
        }

        @PostMapping("/import-purchase-audit")
        @Operation(summary = "导入批发采购审核")
        @Parameters({
                @Parameter(name = "file", description = "Excel 文件", required = true),
                @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
        })
        @PreAuthorize("@ss.hasPermission('erp:wholesalePurchaseAu:import-purchase-audit')")
        public CommonResult<ErpWholesaleImportRespVO> importPurchaseAuditExcel(
                @RequestParam("file") MultipartFile file,
                @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
            try (InputStream inputStream = file.getInputStream()) {
                List<ErpWholesalePurchaseAuditImportExcelVO> list = ExcelUtils.read(inputStream, ErpWholesalePurchaseAuditImportExcelVO.class, new RowIndexListener<>());
                return success(wholesaleService.importWholesalePurchaseAuditList(list, updateSupport));
            } catch (Exception e) {
                throw new RuntimeException("导入失败: " + e.getMessage());
            }
        }

        @GetMapping("/get-sale-audit-import-template")
        @Operation(summary = "获得批发销售审核导入模板")
        public void importSaleAuditTemplate(HttpServletResponse response) throws IOException {
            // 手动创建导出 demo
            List<ErpWholesaleSaleAuditImportExcelVO> list = Arrays.asList(
                    ErpWholesaleSaleAuditImportExcelVO.builder()
                            .build(),
                    ErpWholesaleSaleAuditImportExcelVO.builder()
                            .build()
            );
            // 输出
            ExcelUtils.write(response, "批发销售审核导入模板.xlsx", "批发销售审核列表", ErpWholesaleSaleAuditImportExcelVO.class, list);
        }

        @PostMapping("/import-sale-audit")
        @Operation(summary = "导入批发销售审核")
        @Parameters({
                @Parameter(name = "file", description = "Excel 文件", required = true),
                @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
        })
        @PreAuthorize("@ss.hasPermission('erp:wholesaleSaleAu:import-sale-audit')")
        public CommonResult<ErpWholesaleImportRespVO> importSaleAuditExcel(
                @RequestParam("file") MultipartFile file,
                @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) {
            try (InputStream inputStream = file.getInputStream()) {
                List<ErpWholesaleSaleAuditImportExcelVO> list = ExcelUtils.read(inputStream, ErpWholesaleSaleAuditImportExcelVO.class, new RowIndexListener<>());
                return success(wholesaleService.importWholesaleSaleAuditList(list, updateSupport));
            } catch (Exception e) {
                throw new RuntimeException("导入失败: " + e.getMessage());
            }
        }

        @GetMapping("/export-all-data")
        @Operation(summary = "获取全部批发数据（用于批量操作）")
        @PreAuthorize("@ss.hasAnyPermissions('erp:wholesalePurchaseAu:query','erp:wholesale:query')")
        public CommonResult<List<ErpWholesaleRespVO>> exportAllData(@Valid ErpWholesalePageReqVO pageReqVO) {
            List<ErpWholesaleRespVO> list = wholesaleService.exportAllWholesales(pageReqVO);
            return success(list);
        }




}
