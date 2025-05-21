package cn.iocoder.yudao.module.erp.controller.admin.wholesale;

import cn.iocoder.yudao.framework.common.pojo.*;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesalePurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleSaleMapper;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesalePurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleSaleDO;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalePriceService;
import cn.iocoder.yudao.module.erp.service.wholesale.ErpWholesaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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

    @PostMapping("/create")
    @Operation(summary = "创建批发")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:create')")
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

    @GetMapping("/get")
    @Operation(summary = "获得批发")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<ErpWholesaleRespVO> getWholesale(@RequestParam("id") Long id) {
        // 1. 获取基础信息
        ErpWholesaleBaseDO wholesale = wholesaleService.getWholesale(id);
        if (wholesale == null) {
            return success(null);
        }

        // 2. 转换为RespVO
        ErpWholesaleRespVO respVO = BeanUtils.toBean(wholesale, ErpWholesaleRespVO.class);

        // 3. 获取并合并采购信息
        ErpWholesalePurchaseDO purchase = purchaseMapper.selectByBaseId(id);
        if (purchase != null) {
            BeanUtils.copyProperties(purchase, respVO, "id");

            // 通过组品ID获取组品信息并设置相关字段
            if (purchase.getComboProductId() != null) {
                ErpComboProductDO comboProduct = comboProductService.getCombo(purchase.getComboProductId());
                if (comboProduct != null) {
                    System.out.println("组品信息: " + comboProduct);
                    respVO.setProductName(comboProduct.getName());
                    respVO.setShippingCode(comboProduct.getShippingCode());
                    respVO.setPurchaser(comboProduct.getPurchaser());
                    respVO.setSupplier(comboProduct.getSupplier());
                    respVO.setPurchasePrice(comboProduct.getWholesalePrice());

                    // 计算采购运费
                    BigDecimal shippingFee = BigDecimal.ZERO;
                    System.out.println("运费计算类型: " + comboProduct.getShippingFeeType());
                    switch (comboProduct.getShippingFeeType()) {
                        case 0: // 固定运费
                            shippingFee = comboProduct.getFixedShippingFee();
                            break;
                        case 1: // 按件计费
                            int quantity = wholesale.getProductQuantity();
                            int additionalQuantity = comboProduct.getAdditionalItemQuantity();
                            BigDecimal additionalPrice = comboProduct.getAdditionalItemPrice();
                            System.out.println(String.format("按件计费 - 数量: %d, 附加数量: %d, 附加价格: %s",
                            quantity, additionalQuantity, additionalPrice));
                            if (additionalQuantity > 0) {
                                int additionalUnits = (int) Math.ceil((double) quantity / additionalQuantity);
                                shippingFee = additionalPrice.multiply(new BigDecimal(additionalUnits));
                                System.out.println("按件计费结果 - 附加单位: " + additionalUnits + ", 运费: " + shippingFee);
                            }
                            break;
                        case 2: // 按重计费
                            quantity = wholesale.getProductQuantity();
                            BigDecimal productWeight = comboProduct.getWeight();
                            BigDecimal totalWeight = productWeight.multiply(new BigDecimal(quantity));
                            System.out.println(String.format("按重计费 - 产品重量: %s, 数量: %d, 总重量: %s",
                                    productWeight, quantity, totalWeight));
                            if (totalWeight.compareTo(comboProduct.getFirstWeight()) <= 0) {
                                shippingFee = comboProduct.getFirstWeightPrice();
                                System.out.println("按重计费结果 - 运费: " + shippingFee);
                            } else {
                                BigDecimal additionalWeight = totalWeight.subtract(comboProduct.getFirstWeight());
                                BigDecimal additionalUnits = additionalWeight.divide(comboProduct.getAdditionalWeight(), 0, BigDecimal.ROUND_UP);
                                shippingFee = comboProduct.getFirstWeightPrice().add(
                                        comboProduct.getAdditionalWeightPrice().multiply(additionalUnits)
                                );
                                System.out.println(String.format("按重计费结果 - 超出重量: %s, 续重单位数: %s, 运费: %s",
                                additionalWeight, additionalUnits, shippingFee));
                            }
                            break;
                    }
                    respVO.setLogisticsFee(shippingFee);

                    // 计算采购总额 = 出货单价（即组品表的`wholesalePrice`） * 产品数量 + 出货货拉拉费 + 出货物流费用 + 出货杂费
                    BigDecimal totalAmount = comboProduct.getWholesalePrice()
                            .multiply(new BigDecimal(wholesale.getProductQuantity()))
                            .add(purchase.getTruckFee() != null ? purchase.getTruckFee() : BigDecimal.ZERO)
                            .add(respVO.getLogisticsFee() != null ? respVO.getLogisticsFee() : BigDecimal.ZERO)
                            .add(purchase.getOtherFees() != null ? purchase.getOtherFees() : BigDecimal.ZERO);
                            System.out.println("采购总额计算结果: " + totalAmount);
                    respVO.setTotalPurchaseAmount(totalAmount);
                }
            }
        }

        // 4. 获取并合并销售信息
        ErpWholesaleSaleDO sale = saleMapper.selectByBaseId(id);
        if (sale != null) {
            BeanUtils.copyProperties(sale, respVO, "id","otherFees","truckFee");
            respVO.setSaleTruckFee(sale.getTruckFee());
            respVO.setSaleLogisticsFee(sale.getLogisticsFee());
            respVO.setSaleOtherFees(sale.getOtherFees());

            // 根据客户名称和组品ID查询销售价格
            if (sale.getCustomerName() != null && purchase != null && purchase.getComboProductId() != null) {
                ErpSalePriceRespVO salePrice = salePriceService.getSalePriceByGroupProductIdAndCustomerName(
                        purchase.getComboProductId(), sale.getCustomerName());
                if (salePrice != null) {
                    respVO.setSalePrice(salePrice.getWholesalePrice());
                    System.out.println("销售价格信息: " + salePrice);
                    respVO.setSalePrice(salePrice.getWholesalePrice());

                    // 计算销售运费
                    BigDecimal saleShippingFee = BigDecimal.ZERO;
                    System.out.println("销售运费计算类型: " + salePrice.getShippingFeeType());
                    switch (salePrice.getShippingFeeType()) {
                        case 0: // 固定运费
                            saleShippingFee = salePrice.getFixedShippingFee();
                            break;
                        case 1: // 按件计费
                            int quantity = wholesale.getProductQuantity();
                            int additionalQuantity = salePrice.getAdditionalItemQuantity();
                            BigDecimal additionalPrice = salePrice.getAdditionalItemPrice();
                            System.out.println(String.format("销售按件计费 - 数量: %d, 附加数量: %d, 附加价格: %s",
                                    quantity, additionalQuantity, additionalPrice));
                            if (additionalQuantity > 0) {
                                int additionalUnits = (int) Math.ceil((double) quantity / additionalQuantity);
                                saleShippingFee = additionalPrice.multiply(new BigDecimal(additionalUnits));
                                System.out.println("销售按件计费结果 - 附加单位: " + additionalUnits + ", 运费: " + saleShippingFee);
                            }
                            break;
                        case 2: // 按重计费
                            quantity = wholesale.getProductQuantity();
                            ErpComboProductDO comboProduct = comboProductService.getCombo(purchase.getComboProductId());
                            BigDecimal productWeight = comboProduct.getWeight();
                            BigDecimal totalWeight = productWeight.multiply(new BigDecimal(quantity));
                            System.out.println(String.format("销售按重计费 - 产品重量: %s, 数量: %d, 总重量: %s",
                                    productWeight, quantity, totalWeight));

                            if (totalWeight.compareTo(salePrice.getFirstWeight()) <= 0) {
                                saleShippingFee = salePrice.getFirstWeightPrice();
                                System.out.println("销售按重计费结果 - 运费: " + saleShippingFee);
                            } else {
                                BigDecimal additionalWeight = totalWeight.subtract(salePrice.getFirstWeight());
                                BigDecimal additionalUnits = additionalWeight.divide(salePrice.getAdditionalWeight(), 0, BigDecimal.ROUND_UP);
                                saleShippingFee = salePrice.getFirstWeightPrice().add(
                                        salePrice.getAdditionalWeightPrice().multiply(additionalUnits)
                                );
                                System.out.println(String.format("销售按重计费结果 - 超出重量: %s, 续重单位数: %s, 运费: %s",
                                additionalWeight, additionalUnits, saleShippingFee));
                            }
                            break;
                    }
                    respVO.setSaleLogisticsFee(saleShippingFee);

                    // 计算采购总额 = 出货单价（即组品表的`wholesalePrice`） * 产品数量 + 出货货拉拉费 + 出货物流费用 + 出货杂费
                    BigDecimal totalAmount = salePrice.getWholesalePrice()
                            .multiply(new BigDecimal(wholesale.getProductQuantity()))
                            .add(sale.getTruckFee() != null ? sale.getTruckFee() : BigDecimal.ZERO)
                            .add(respVO.getSaleLogisticsFee() != null ? respVO.getSaleLogisticsFee() : BigDecimal.ZERO)
                            .add(sale.getOtherFees() != null ? sale.getOtherFees() : BigDecimal.ZERO);
                            System.out.println("销售总额计算结果: " + totalAmount);
                    respVO.setTotalSaleAmount(totalAmount);
                }
            }
        }

        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得批发分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<PageResult<ErpWholesaleRespVO>> getWholesalePage(@Valid ErpWholesalePageReqVO pageReqVO) {

        System.out.println("调用批发分页" );
        System.out.println("pageReqVO:" + pageReqVO);

        PageResult<ErpWholesaleRespVO> pageResult = wholesaleService.getWholesaleVOPage(pageReqVO);
        System.out.println("pageResult:" + pageResult);
        return success(pageResult);
    }
    // 未审核批发采购分页
    @GetMapping("/purchase/unreviewed-page")
    @Operation(summary = "获得未审核批发采购分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
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
        }

        // 创建返回结果
        WholesalePurchaseSummaryPageResult<ErpWholesalePurchaseAuditVO> result = new WholesalePurchaseSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, pageResult.getTotal()));
        result.setTotalPurchasePrice(totalPurchasePrice);
        result.setTotalTruckFee(totalTruckFee);
        result.setTotalLogisticsFee(totalLogisticsFee);
        result.setTotalOtherFees(totalOtherFees);
        result.setTotalPurchaseAmount(totalPurchaseAmount);

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
        }

        // 创建返回结果
        WholesalePurchaseSummaryPageResult<ErpWholesalePurchaseAuditVO> result = new WholesalePurchaseSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, pageResult.getTotal()));
        result.setTotalPurchasePrice(totalPurchasePrice);
        result.setTotalTruckFee(totalTruckFee);
        result.setTotalLogisticsFee(totalLogisticsFee);
        result.setTotalOtherFees(totalOtherFees);
        result.setTotalPurchaseAmount(totalPurchaseAmount);

        return success(result);
    }
    // 未审核批发销售分页
    @GetMapping("/sale/unreviewed-page")
    @Operation(summary = "获得未审核批发销售分页")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
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
        }

        // 创建返回结果
        WholesaleSalesSummaryPageResult<ErpWholesaleSaleAuditVO> result = new WholesaleSalesSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, pageResult.getTotal()));
        result.setTotalSalePrice(totalSalePrice);
        result.setTotalSaleTruckFee(totalSaleTruckFee);
        result.setTotalSaleLogisticsFee(totalSaleLogisticsFee);
        result.setTotalSaleOtherFees(totalSaleOtherFees);
        result.setTotalSaleAmount(totalSaleAmount);
        System.out.println("批发销售的返回"+result);

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
        }

        // 创建返回结果
        WholesaleSalesSummaryPageResult<ErpWholesaleSaleAuditVO> result = new WholesaleSalesSummaryPageResult<>();
        result.setPageResult(new PageResult<>(list, pageResult.getTotal()));
        result.setTotalSalePrice(totalSalePrice);
        result.setTotalSaleTruckFee(totalSaleTruckFee);
        result.setTotalSaleLogisticsFee(totalSaleLogisticsFee);
        result.setTotalSaleOtherFees(totalSaleOtherFees);
        result.setTotalSaleAmount(totalSaleAmount);

        return success(result);
    }
        // 获取采购详情
    @GetMapping("/purchase/get")
    @Operation(summary = "获得批发采购详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<ErpWholesalePurchaseAuditVO> getWholesalePurchase(@RequestParam("id") Long id) {
        // 1. 获取基础信息
        ErpWholesaleBaseDO wholesale = wholesaleService.getWholesale(id);
        if (wholesale == null) {
            return success(null);
        }

        // 2. 转换为ErpWholesalePurchaseAuditVO
        ErpWholesalePurchaseAuditVO vo = new ErpWholesalePurchaseAuditVO();
        BeanUtils.copyProperties(wholesale, vo);

        // 3. 获取并合并采购信息
        ErpWholesalePurchaseDO purchase = purchaseMapper.selectByBaseId(id);
        if (purchase != null) {
            BeanUtils.copyProperties(purchase, vo, "id");

            // 通过组品ID获取组品信息并设置相关字段
            if (purchase.getComboProductId() != null) {
                ErpComboProductDO comboProduct = comboProductService.getCombo(purchase.getComboProductId());
                if (comboProduct != null) {
                    vo.setProductName(comboProduct.getName());
                    vo.setShippingCode(comboProduct.getShippingCode());
                    vo.setPurchaser(comboProduct.getPurchaser());
                    vo.setSupplier(comboProduct.getSupplier());
                    vo.setPurchasePrice(comboProduct.getWholesalePrice());

                    // 计算采购运费
                    BigDecimal shippingFee = BigDecimal.ZERO;
                    switch (comboProduct.getShippingFeeType()) {
                        case 0: // 固定运费
                            shippingFee = comboProduct.getFixedShippingFee();
                            break;
                        case 1: // 按件计费
                            int quantity = wholesale.getProductQuantity();
                            int additionalQuantity = comboProduct.getAdditionalItemQuantity();
                            BigDecimal additionalPrice = comboProduct.getAdditionalItemPrice();

                            if (additionalQuantity > 0) {
                                int additionalUnits = (int) Math.ceil((double) quantity / additionalQuantity);
                                shippingFee = additionalPrice.multiply(new BigDecimal(additionalUnits));
                            }
                            break;
                        case 2: // 按重计费
                            quantity = wholesale.getProductQuantity();
                            BigDecimal productWeight = comboProduct.getWeight();
                            BigDecimal totalWeight = productWeight.multiply(new BigDecimal(quantity));
                            System.out.println(String.format("采购按重计费 - 产品重量: %s, 数量: %d, 总重量: %s",
                                    productWeight, quantity, totalWeight));
                            System.out.println(String.format("采购首重: %s, 首重价格: %s, 续重单位: %s, 续重价格: %s",
                                    comboProduct.getFirstWeight(), comboProduct.getFirstWeightPrice(),
                                    comboProduct.getAdditionalWeight(), comboProduct.getAdditionalWeightPrice()));

                            if (totalWeight.compareTo(comboProduct.getFirstWeight()) <= 0) {
                                shippingFee = comboProduct.getFirstWeightPrice();
                                System.out.println("采购总重量<=首重，运费=" + shippingFee);
                            } else {
                                BigDecimal additionalWeight = totalWeight.subtract(comboProduct.getFirstWeight());
                                BigDecimal additionalUnits = additionalWeight.divide(comboProduct.getAdditionalWeight(), 0, BigDecimal.ROUND_UP);
                                shippingFee = comboProduct.getFirstWeightPrice().add(
                                        comboProduct.getAdditionalWeightPrice().multiply(additionalUnits)
                                );
                                System.out.println(String.format("采购总重量>首重 - 超出重量: %s, 续重单位数: %s, 运费: %s",
                                        additionalWeight, additionalUnits, shippingFee));
                            }
                            break;
                    }
                    vo.setLogisticsFee(shippingFee);
                    // 计算采购总额
                    BigDecimal totalAmount = comboProduct.getWholesalePrice()
                            .multiply(new BigDecimal(wholesale.getProductQuantity()))
                            .add(purchase.getTruckFee() != null ? purchase.getTruckFee() : BigDecimal.ZERO)
                            .add(vo.getLogisticsFee() != null ? vo.getLogisticsFee() : BigDecimal.ZERO)
                            .add(purchase.getOtherFees() != null ? purchase.getOtherFees() : BigDecimal.ZERO);
                    vo.setTotalPurchaseAmount(totalAmount);
                    System.out.println("成功设置采购总额："+vo.getTotalPurchaseAmount());
                }
            }
        }

        return success(vo);
    }

    // 获取销售详情
    @GetMapping("/sale/get")
    @Operation(summary = "获得批发销售详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:query')")
    public CommonResult<ErpWholesaleSaleAuditVO> getWholesaleSale(@RequestParam("id") Long id) {
        // 1. 获取基础信息
        ErpWholesaleBaseDO wholesale = wholesaleService.getWholesale(id);
        if (wholesale == null) {
            return success(null);
        }

        // 2. 转换为ErpWholesaleSaleAuditVO
        ErpWholesaleSaleAuditVO vo = new ErpWholesaleSaleAuditVO();
        BeanUtils.copyProperties(wholesale, vo);

        // 3. 获取并合并销售信息
        ErpWholesaleSaleDO sale = saleMapper.selectByBaseId(id);
        if (sale != null) {
            BeanUtils.copyProperties(sale, vo, "id");
            vo.setSaleTruckFee(sale.getTruckFee());
            vo.setSaleOtherFees(sale.getOtherFees());

            // 根据客户名称和组品ID查询销售价格
            ErpWholesalePurchaseDO purchase = purchaseMapper.selectByBaseId(id);
            if (sale.getCustomerName() != null && purchase != null && purchase.getComboProductId() != null) {
                ErpSalePriceRespVO salePrice = salePriceService.getSalePriceByGroupProductIdAndCustomerName(
                        purchase.getComboProductId(), sale.getCustomerName());
                if (salePrice != null) {
                    vo.setSalePrice(salePrice.getWholesalePrice());

                    // 计算销售运费
                    BigDecimal saleShippingFee = BigDecimal.ZERO;
                    switch (salePrice.getShippingFeeType()) {
                        case 0: // 固定运费
                            saleShippingFee = salePrice.getFixedShippingFee();
                            break;
                        case 1: // 按件计费
                            int quantity = wholesale.getProductQuantity();
                            int additionalQuantity = salePrice.getAdditionalItemQuantity();
                            BigDecimal additionalPrice = salePrice.getAdditionalItemPrice();

                            if (additionalQuantity > 0) {
                                int additionalUnits = (int) Math.ceil((double) quantity / additionalQuantity);
                                saleShippingFee = additionalPrice.multiply(new BigDecimal(additionalUnits));
                            }
                            break;
                        case 2: // 按重计费
                            quantity = wholesale.getProductQuantity();
                            ErpComboProductDO comboProduct = comboProductService.getCombo(purchase.getComboProductId());
                            BigDecimal productWeight = comboProduct.getWeight();
                            BigDecimal totalWeight = productWeight.multiply(new BigDecimal(quantity));

                            if (totalWeight.compareTo(salePrice.getFirstWeight()) <= 0) {
                                saleShippingFee = salePrice.getFirstWeightPrice();
                            } else {
                                BigDecimal additionalWeight = totalWeight.subtract(salePrice.getFirstWeight());
                                BigDecimal additionalUnits = additionalWeight.divide(salePrice.getAdditionalWeight(), 0, BigDecimal.ROUND_UP);
                                saleShippingFee = salePrice.getFirstWeightPrice().add(
                                        salePrice.getAdditionalWeightPrice().multiply(additionalUnits)
                                );
                            }
                            break;
                    }
                    vo.setSaleLogisticsFee(saleShippingFee);

                    // 计算销售总额
                    BigDecimal totalAmount = salePrice.getWholesalePrice()
                            .multiply(new BigDecimal(wholesale.getProductQuantity()))
                            .add(sale.getTruckFee() != null ? sale.getTruckFee() : BigDecimal.ZERO)
                            .add(vo.getSaleLogisticsFee() != null ? vo.getSaleLogisticsFee()  : BigDecimal.ZERO)
                            .add(sale.getOtherFees() != null ? sale.getOtherFees() : BigDecimal.ZERO);
                    vo.setTotalSaleAmount(totalAmount);
                }
            }
        }

        return success(vo);
    }
    // 更新采购审核状态
    @PutMapping("/update-purchase-audit-status")
    @Operation(summary = "更新采购审核状态")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:update-status')")
    public CommonResult<Boolean> updatePurchaseAuditStatus(@RequestParam("id") Long id,
                                                           @RequestParam("purchaseAuditStatus") Integer purchaseAuditStatus,
                                                           @RequestParam("otherFees") BigDecimal otherFees) {
        wholesaleService.updatePurchaseAuditStatus(id, purchaseAuditStatus, otherFees);
        return success(true);
    }

        // 更新销售审核状态
    @PutMapping("/update-sale-audit-status")
    @Operation(summary = "更新销售审核状态")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:update-status')")
    public CommonResult<Boolean> updateSaleAuditStatus(@RequestParam("id") Long id,
                                                      @RequestParam("saleAuditStatus") Integer saleAuditStatus,
                                                      @RequestParam("otherFees") BigDecimal otherFees) {
        wholesaleService.updateSaleAuditStatus(id, saleAuditStatus, otherFees);
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
    @PreAuthorize("@ss.hasPermission('erp:wholesale:update-after-sales')")
    public CommonResult<Boolean> updatePurchaseAfterSales(@Valid @RequestBody ErpWholesalePurchaseAfterSalesUpdateReqVO reqVO) {
        wholesaleService.updatePurchaseAfterSales(reqVO);
        return success(true);
    }

    // 更新销售售后信息
    @PutMapping("/update-sale-after-sales")
    @Operation(summary = "更新销售售后信息")
    @PreAuthorize("@ss.hasPermission('erp:wholesale:update-after-sales')")
    public CommonResult<Boolean> updateSaleAfterSales(@Valid @RequestBody ErpWholesaleSaleAfterSalesUpdateReqVO reqVO) {
        System.out.println("销售售后前端传递的vo"+reqVO);
        wholesaleService.updateSaleAfterSales(reqVO);
        return success(true);
    }

}
