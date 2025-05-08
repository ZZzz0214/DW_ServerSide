package cn.iocoder.yudao.module.erp.controller.admin.distribution;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.*;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionBaseDO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionPurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionSaleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionPurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionSaleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSalePriceMapper;
import cn.iocoder.yudao.module.erp.service.distribution.ErpDistributionService;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalePriceService;
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
                                                          @RequestParam("status") Integer status) {
        distributionService.updateDistributionStatus(id, status);
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
        ErpDistributionBaseDO distribution = distributionService.getDistribution(id);
        if (distribution == null) {
            return success(null);
        }

        // 2. 转换为RespVO
        ErpDistributionRespVO respVO = BeanUtils.toBean(distribution, ErpDistributionRespVO.class);

        // 3. 获取并合并采购信息
        ErpDistributionPurchaseDO purchase = purchaseMapper.selectByBaseId(id);
        if (purchase != null) {
            BeanUtils.copyProperties(purchase, respVO, "id");
            // 通过组品ID获取组品信息并设置相关字段
            if (purchase.getComboProductId() != null) {
                ErpComboProductDO comboProduct = comboProductService.getCombo(purchase.getComboProductId());
                if (comboProduct != null) {
                    respVO.setProductName(comboProduct.getName());
                    respVO.setShippingCode(comboProduct.getShippingCode());
                    respVO.setPurchaser(comboProduct.getPurchaser());
                    respVO.setSupplier(comboProduct.getSupplier());
                    respVO.setPurchasePrice(comboProduct.getPurchasePrice());

                    // 计算采购运费
                    BigDecimal shippingFee = BigDecimal.ZERO;
                    switch (comboProduct.getShippingFeeType()) {
                        case 0: // 固定运费
                            shippingFee = comboProduct.getFixedShippingFee();
                            break;
                        case 1: // 按件计费
                            int quantity = distribution.getProductQuantity();
                            int additionalQuantity = comboProduct.getAdditionalItemQuantity();
                            BigDecimal additionalPrice = comboProduct.getAdditionalItemPrice();

                            if (additionalQuantity > 0) {
                                int additionalUnits = (int) Math.ceil((double) quantity / additionalQuantity);
                                shippingFee = additionalPrice.multiply(new BigDecimal(additionalUnits));
                            }
                            break;
                        case 2: // 按重计费
                            quantity = distribution.getProductQuantity();
                            BigDecimal productWeight = comboProduct.getWeight(); // 使用组品重量
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
                    respVO.setShippingFee(shippingFee);

                    // 计算采购总额 = 采购单价*数量 + 运费 + 其他费用
                    BigDecimal totalAmount = comboProduct.getPurchasePrice()
                            .multiply(new BigDecimal(distribution.getProductQuantity()))
                            .add(shippingFee)
                            .add(purchase.getOtherFees() != null ? purchase.getOtherFees() : BigDecimal.ZERO);
                    respVO.setTotalPurchaseAmount(totalAmount);
                }
            }
        }

        // 4. 获取并合并销售信息
        ErpDistributionSaleDO sale = saleMapper.selectByBaseId(id);
        if (sale != null) {
            BeanUtils.copyProperties(sale, respVO, "id");
            respVO.setSaleOtherFees(sale.getOtherFees());

            // 根据客户名称和组品ID查询销售价格
            if (sale.getCustomerName() != null && purchase != null && purchase.getComboProductId() != null) {
                ErpSalePriceRespVO salePrice = salePriceService.getSalePriceByGroupProductIdAndCustomerName(
                        purchase.getComboProductId(), sale.getCustomerName());
                if (salePrice != null) {
                    respVO.setSalePrice(salePrice.getDistributionPrice());

                    // 计算销售运费
                    BigDecimal saleShippingFee = BigDecimal.ZERO;
                    switch (salePrice.getShippingFeeType()) {
                        case 0: // 固定运费
                            saleShippingFee = salePrice.getFixedShippingFee();
                            System.out.println("固定运费: " + saleShippingFee);
                            break;
                        case 1: // 按件计费
                            int quantity = distribution.getProductQuantity();
                            int additionalQuantity = salePrice.getAdditionalItemQuantity();
                            BigDecimal additionalPrice = salePrice.getAdditionalItemPrice();

                            if (additionalQuantity > 0) {
                                int additionalUnits = (int) Math.ceil((double) quantity / additionalQuantity);
                                saleShippingFee = additionalPrice.multiply(new BigDecimal(additionalUnits));
                                System.out.println(String.format("按件计费 - 数量: %d, 附加件数量: %d, 附加件价格: %s, 运费: %s",
                                        quantity, additionalQuantity, additionalPrice, saleShippingFee));
                            }
                            break;
                        case 2: // 按重计费
                            int quantity2 = distribution.getProductQuantity();
                            ErpComboProductDO comboProduct = comboProductService.getCombo(purchase.getComboProductId());
                            BigDecimal productWeight = comboProduct.getWeight(); // 使用组品重量
                            BigDecimal totalWeight = productWeight.multiply(new BigDecimal(quantity2));

                            System.out.println(String.format("按重计费 - 产品重量: %s, 数量: %d, 总重量: %s",
                                    productWeight, quantity2, totalWeight));
                            System.out.println(String.format("首重: %s, 首重价格: %s, 续重单位: %s, 续重价格: %s",
                                    salePrice.getFirstWeight(), salePrice.getFirstWeightPrice(),
                                    salePrice.getAdditionalWeight(), salePrice.getAdditionalWeightPrice()));

                            if (totalWeight.compareTo(salePrice.getFirstWeight()) <= 0) {
                                saleShippingFee = salePrice.getFirstWeightPrice();
                                System.out.println("总重量<=首重，运费=" + saleShippingFee);
                            } else {
                                BigDecimal additionalWeight = totalWeight.subtract(salePrice.getFirstWeight());
                                BigDecimal additionalUnits = additionalWeight.divide(salePrice.getAdditionalWeight(), 0, BigDecimal.ROUND_UP);
                                saleShippingFee = salePrice.getFirstWeightPrice().add(
                                        salePrice.getAdditionalWeightPrice().multiply(additionalUnits)
                                );
                                System.out.println(String.format("总重量>首重 - 超出重量: %s, 续重单位数: %s, 运费: %s",
                                        additionalWeight, additionalUnits, saleShippingFee));
                            }
                            break;
                    }
                    respVO.setSaleShippingFee(saleShippingFee);
                    System.out.println("最终销售运费：" + saleShippingFee);

                    // 计算销售总额 = 销售单价*数量 + 销售运费 + 销售其他费用
                    BigDecimal totalSaleAmount = salePrice.getDistributionPrice()
                            .multiply(new BigDecimal(distribution.getProductQuantity()))
                            .add(saleShippingFee)
                            .add(sale.getOtherFees() != null ? sale.getOtherFees() : BigDecimal.ZERO);
                    respVO.setTotalSaleAmount(totalSaleAmount);
                }
            }
        }

        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得代发分页")
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<PageResult<ErpDistributionRespVO>> getDistributionPage(@Valid ErpDistributionPageReqVO pageReqVO) {
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);
        pageResult.getList().forEach(item -> {
            System.out.println(String.format("订单ID: %s, 采购总额: %s, 采购运费: %s, 销售总额: %s, 销售运费: %s, 客户名称: %s",
                    item.getId(),
                    item.getTotalPurchaseAmount(),
                    item.getShippingFee(),
                    item.getTotalSaleAmount(),
                    item.getSaleShippingFee(),
                    item.getCustomerName()));
        });
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
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<PageResult<ErpDistributionPurchaseAuditVO>> getUnreviewedPurchasePage(@Valid ErpDistributionPageReqVO pageReqVO) {
        pageReqVO.setStatus(10); // 设置状态为未审核
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);
        List<ErpDistributionPurchaseAuditVO> list = pageResult.getList().stream().map(item -> {
            ErpDistributionPurchaseAuditVO vo = new ErpDistributionPurchaseAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    // 已审核代发采购分页
    @GetMapping("/purchase/reviewed-page")
    @Operation(summary = "获得已审核代发采购分页")
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<PageResult<ErpDistributionPurchaseAuditVO>> getReviewedPurchasePage(@Valid ErpDistributionPageReqVO pageReqVO) {
        pageReqVO.setStatus(20); // 设置状态为已审核
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);
        List<ErpDistributionPurchaseAuditVO> list = pageResult.getList().stream().map(item -> {
            ErpDistributionPurchaseAuditVO vo = new ErpDistributionPurchaseAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    // 未审核代发销售分页
    @GetMapping("/sale/unreviewed-page")
    @Operation(summary = "获得未审核代发销售分页")
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<PageResult<ErpDistributionSaleAuditVO>> getUnreviewedSalePage(@Valid ErpDistributionPageReqVO pageReqVO) {
        pageReqVO.setStatus(10); // 设置状态为未审核
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);
        List<ErpDistributionSaleAuditVO> list = pageResult.getList().stream().map(item -> {
            ErpDistributionSaleAuditVO vo = new ErpDistributionSaleAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    // 已审核代发销售分页
    @GetMapping("/sale/reviewed-page")
    @Operation(summary = "获得已审核代发销售分页")
    @PreAuthorize("@ss.hasPermission('erp:distribution:query')")
    public CommonResult<PageResult<ErpDistributionSaleAuditVO>> getReviewedSalePage(@Valid ErpDistributionPageReqVO pageReqVO) {
        pageReqVO.setStatus(20); // 设置状态为已审核
        PageResult<ErpDistributionRespVO> pageResult = distributionService.getDistributionVOPage(pageReqVO);
        List<ErpDistributionSaleAuditVO> list = pageResult.getList().stream().map(item -> {
            ErpDistributionSaleAuditVO vo = new ErpDistributionSaleAuditVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }
}
