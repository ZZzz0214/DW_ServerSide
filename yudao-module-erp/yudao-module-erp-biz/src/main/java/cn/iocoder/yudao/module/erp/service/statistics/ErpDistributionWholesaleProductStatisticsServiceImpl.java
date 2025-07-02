package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleProductStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleProductStatisticsRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionCombinedESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleCombinedESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboProductItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import cn.iocoder.yudao.module.erp.service.distribution.ErpDistributionCombinedESRepository;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import cn.iocoder.yudao.module.erp.service.wholesale.ErpWholesaleCombinedESRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ERP 代发批发产品组品统计 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpDistributionWholesaleProductStatisticsServiceImpl implements ErpDistributionWholesaleProductStatisticsService {

    @Resource
    private ErpDistributionMapper distributionMapper;

    @Resource
    private ErpWholesaleMapper wholesaleMapper;

    @Resource
    private ErpComboProductService comboProductService;

    @Resource
    private ErpComboProductItemMapper comboProductItemMapper;

    @Resource
    private ErpProductMapper productMapper;

    @Resource
    private ErpDistributionCombinedESRepository distributionCombinedESRepository;

    @Resource
    private ErpWholesaleCombinedESRepository wholesaleCombinedESRepository;

    @Override
    public ErpDistributionWholesaleProductStatisticsRespVO getDistributionWholesaleProductStatistics(ErpDistributionWholesaleProductStatisticsReqVO reqVO) {
        System.out.println("=== 开始统计代发批发产品组品数据 ===");
        System.out.println("请求参数: startDate=" + reqVO.getStartDate() + ", endDate=" + reqVO.getEndDate());

        // 构建查询条件
        LocalDateTime startTime = reqVO.getStartDate().atStartOfDay();
        LocalDateTime endTime = reqVO.getEndDate().atTime(23, 59, 59);
        System.out.println("查询时间范围: " + startTime + " 到 " + endTime);

        // 查询代发表数据 - 优先使用ES搜索
        List<ErpDistributionCombinedESDO> distributionList;
        try {
            distributionList = distributionCombinedESRepository.findByCreateTimeBetween(startTime, endTime);
            System.out.println("ES查询代发表成功，数据量: " + distributionList.size());
        } catch (Exception e) {
            System.out.println("ES查询代发表失败，回退到数据库查询: " + e.getMessage());
            // ES查询失败，回退到数据库查询
            List<ErpDistributionBaseDO> dbDistributionList = distributionMapper.selectListByCreateTimeBetween(startTime, endTime);
            distributionList = dbDistributionList.stream()
                    .map(this::convertToCombinedESDO)
                    .collect(Collectors.toList());
            System.out.println("数据库查询代发表成功，数据量: " + distributionList.size());
        }

        // 查询批发表数据 - 优先使用ES搜索
        List<ErpWholesaleCombinedESDO> wholesaleList;
        try {
            wholesaleList = wholesaleCombinedESRepository.findByCreateTimeBetween(startTime, endTime);
            System.out.println("ES查询批发表成功，数据量: " + wholesaleList.size());
        } catch (Exception e) {
            System.out.println("ES查询批发表失败，回退到数据库查询: " + e.getMessage());
            // ES查询失败，回退到数据库查询
            List<ErpWholesaleBaseDO> dbWholesaleList = wholesaleMapper.selectListByCreateTimeBetween(startTime, endTime);
            wholesaleList = dbWholesaleList.stream()
                    .map(this::convertToCombinedESDO)
                    .collect(Collectors.toList());
            System.out.println("数据库查询批发表成功，数据量: " + wholesaleList.size());
        }

        // 1. 统计组品数据
        Map<Long, ComboProductData> comboProductDataMap = new HashMap<>();

        // 代发表组品统计
        for (ErpDistributionCombinedESDO distribution : distributionList) {
            if (distribution.getComboProductId() != null) {
                ComboProductData comboData = comboProductDataMap.computeIfAbsent(distribution.getComboProductId(),
                    k -> new ComboProductData());

                comboData.setComboProductId(distribution.getComboProductId());
                comboData.setComboProductNo(distribution.getComboProductNo());
                comboData.setDistributionComboCount(comboData.getDistributionComboCount() + distribution.getProductQuantity());
            }
        }

        // 批发表组品统计
        for (ErpWholesaleCombinedESDO wholesale : wholesaleList) {
            if (wholesale.getComboProductId() != null) {
                ComboProductData comboData = comboProductDataMap.computeIfAbsent(wholesale.getComboProductId(),
                    k -> new ComboProductData());

                comboData.setComboProductId(wholesale.getComboProductId());
                comboData.setComboProductNo(wholesale.getComboProductNo());
                comboData.setWholesaleComboCount(comboData.getWholesaleComboCount() + wholesale.getProductQuantity());
            }
        }

        // 2. 从组品单品明细中获取单品统计
        Map<Long, Integer> distributionSingleProductCount = new HashMap<>();
        Map<Long, Integer> wholesaleSingleProductCount = new HashMap<>();

        // 从代发表组品中提取单品统计
        for (ErpDistributionCombinedESDO distribution : distributionList) {
            if (distribution.getComboProductId() != null) {
                // 获取组品单品明细
                List<ErpComboProductItemDO> comboItems = comboProductItemMapper.selectByComboProductId(distribution.getComboProductId());
                for (ErpComboProductItemDO item : comboItems) {
                    // 单品数量 = 组品单品明细中的单品数量 × 代发表中的产品数量
                    int itemQuantity = item.getItemQuantity() * distribution.getProductQuantity();
                    distributionSingleProductCount.merge(item.getItemProductId(), itemQuantity, Integer::sum);
                }
            }
        }

        // 从批发表组品中提取单品统计
        for (ErpWholesaleCombinedESDO wholesale : wholesaleList) {
            if (wholesale.getComboProductId() != null) {
                // 获取组品单品明细
                List<ErpComboProductItemDO> comboItems = comboProductItemMapper.selectByComboProductId(wholesale.getComboProductId());
                for (ErpComboProductItemDO item : comboItems) {
                    // 单品数量 = 组品单品明细中的单品数量 × 批发表中的产品数量
                    int itemQuantity = item.getItemQuantity() * wholesale.getProductQuantity();
                    wholesaleSingleProductCount.merge(item.getItemProductId(), itemQuantity, Integer::sum);
                }
            }
        }

        // 3. 获取单品详细信息
        Set<Long> allProductIds = new HashSet<>();
        allProductIds.addAll(distributionSingleProductCount.keySet());
        allProductIds.addAll(wholesaleSingleProductCount.keySet());

        Map<Long, ErpProductDO> productMap = new HashMap<>();
        if (!allProductIds.isEmpty()) {
            List<ErpProductDO> products = productMapper.selectBatchIds(allProductIds);
            productMap = products.stream().collect(Collectors.toMap(ErpProductDO::getId, p -> p));
        }

        // 4. 构建单品统计列表
        List<ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics> singleProductStatistics = new ArrayList<>();

        for (Long productId : allProductIds) {
            ErpProductDO product = productMap.get(productId);
            if (product != null) {
                ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics singleStat = new ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics();
                singleStat.setProductName(product.getName());
                singleStat.setProductSpecification(product.getStandard());

                int distributionCount = distributionSingleProductCount.getOrDefault(productId, 0);
                int wholesaleCount = wholesaleSingleProductCount.getOrDefault(productId, 0);
                int totalCount = distributionCount + wholesaleCount;

                singleStat.setDistributionCount(distributionCount);
                singleStat.setWholesaleCount(wholesaleCount);
                singleStat.setTotalCount(totalCount);

                // 计算占比
                if (totalCount > 0) {
                    BigDecimal distributionPercentage = BigDecimal.valueOf(distributionCount)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
                    BigDecimal wholesalePercentage = BigDecimal.valueOf(wholesaleCount)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);

                    singleStat.setDistributionPercentage(distributionPercentage);
                    singleStat.setWholesalePercentage(wholesalePercentage);
                } else {
                    singleStat.setDistributionPercentage(BigDecimal.ZERO);
                    singleStat.setWholesalePercentage(BigDecimal.ZERO);
                }

                singleProductStatistics.add(singleStat);
            }
        }

        // 5. 构建组品统计列表
        List<ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics> comboProductStatistics = new ArrayList<>();

        for (ComboProductData comboData : comboProductDataMap.values()) {
            ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics comboStat = new ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics();

            comboStat.setComboProductId(comboData.getComboProductId());
            comboStat.setComboProductNo(comboData.getComboProductNo());

            // 获取组品名称
            try {
                ErpComboProductDO comboProduct = comboProductService.getCombo(comboData.getComboProductId());
                if (comboProduct != null && comboProduct.getName() != null) {
                    comboStat.setComboProductName(comboProduct.getName());
                } else {
                    comboStat.setComboProductName("未知组品");
                }
            } catch (Exception e) {
                comboStat.setComboProductName("未知组品");
            }

            comboStat.setDistributionComboCount(comboData.getDistributionComboCount());
            comboStat.setWholesaleComboCount(comboData.getWholesaleComboCount());
            comboStat.setTotalComboCount(comboData.getDistributionComboCount() + comboData.getWholesaleComboCount());

            // 计算占比
            int totalComboCount = comboStat.getTotalComboCount();
            if (totalComboCount > 0) {
                BigDecimal distributionPercentage = BigDecimal.valueOf(comboData.getDistributionComboCount())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalComboCount), 2, RoundingMode.HALF_UP);
                BigDecimal wholesalePercentage = BigDecimal.valueOf(comboData.getWholesaleComboCount())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalComboCount), 2, RoundingMode.HALF_UP);

                comboStat.setDistributionPercentage(distributionPercentage);
                comboStat.setWholesalePercentage(wholesalePercentage);
            } else {
                comboStat.setDistributionPercentage(BigDecimal.ZERO);
                comboStat.setWholesalePercentage(BigDecimal.ZERO);
            }

            // 获取组品单品明细
            List<ErpDistributionWholesaleProductStatisticsRespVO.ComboProductItemDetail> itemDetails = new ArrayList<>();
            try {
                List<ErpComboProductItemDO> comboItems = comboProductItemMapper.selectByComboProductId(comboData.getComboProductId());
                for (ErpComboProductItemDO item : comboItems) {
                    ErpProductDO product = productMap.get(item.getItemProductId());
                    if (product != null) {
                        ErpDistributionWholesaleProductStatisticsRespVO.ComboProductItemDetail itemDetail = new ErpDistributionWholesaleProductStatisticsRespVO.ComboProductItemDetail();
                        itemDetail.setProductName(product.getName());
                        itemDetail.setProductSpecification(product.getStandard());
                        itemDetail.setItemQuantity(item.getItemQuantity());
                        itemDetails.add(itemDetail);
                    }
                }
            } catch (Exception e) {
                // 忽略错误，继续处理
            }
            comboStat.setItemDetails(itemDetails);

            comboProductStatistics.add(comboStat);
        }

        // 6. 构建响应对象
        ErpDistributionWholesaleProductStatisticsRespVO respVO = new ErpDistributionWholesaleProductStatisticsRespVO();
        // 创建分页结果对象
        PageResult<ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics> singleProductPageResult = 
            new PageResult<>(singleProductStatistics, (long) singleProductStatistics.size());
        PageResult<ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics> comboProductPageResult = 
            new PageResult<>(comboProductStatistics, (long) comboProductStatistics.size());
        
        respVO.setSingleProductPageResult(singleProductPageResult);
        respVO.setComboProductPageResult(comboProductPageResult);

        System.out.println("统计完成:");
        System.out.println("- 单品统计数量: " + singleProductStatistics.size());
        System.out.println("- 组品统计数量: " + comboProductStatistics.size());

        // 计算总计
        int totalDistributionSingleCount = singleProductStatistics.stream()
                .mapToInt(ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics::getDistributionCount)
                .sum();
        int totalWholesaleSingleCount = singleProductStatistics.stream()
                .mapToInt(ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics::getWholesaleCount)
                .sum();
        int totalSingleCount = totalDistributionSingleCount + totalWholesaleSingleCount;

        int totalDistributionComboCount = comboProductStatistics.stream()
                .mapToInt(ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics::getDistributionComboCount)
                .sum();
        int totalWholesaleComboCount = comboProductStatistics.stream()
                .mapToInt(ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics::getWholesaleComboCount)
                .sum();
        int totalComboCount = totalDistributionComboCount + totalWholesaleComboCount;

        respVO.setTotalDistributionSingleCount(totalDistributionSingleCount);
        respVO.setTotalWholesaleSingleCount(totalWholesaleSingleCount);
        respVO.setTotalSingleCount(totalSingleCount);
        respVO.setTotalDistributionComboCount(totalDistributionComboCount);
        respVO.setTotalWholesaleComboCount(totalWholesaleComboCount);
        respVO.setTotalComboCount(totalComboCount);

        return respVO;
    }

    @Override
    public ErpDistributionWholesaleProductStatisticsRespVO getDistributionWholesaleProductStatisticsPage(ErpDistributionWholesaleProductStatisticsReqVO reqVO) {
        System.out.println("=== 开始统计代发批发产品组品数据（分页） ===");
        System.out.println("请求参数: startDate=" + reqVO.getStartDate() + ", endDate=" + reqVO.getEndDate() + 
                          ", pageNo=" + reqVO.getPageNo() + ", pageSize=" + reqVO.getPageSize());
        
        // 构建查询条件
        LocalDateTime startTime = reqVO.getStartDate().atStartOfDay();
        LocalDateTime endTime = reqVO.getEndDate().atTime(23, 59, 59);
        System.out.println("查询时间范围: " + startTime + " 到 " + endTime);

        // 查询代发表数据 - 优先使用ES搜索
        List<ErpDistributionCombinedESDO> distributionList;
        try {
            distributionList = distributionCombinedESRepository.findByCreateTimeBetween(startTime, endTime);
            System.out.println("ES查询代发表成功，数据量: " + distributionList.size());
        } catch (Exception e) {
            System.out.println("ES查询代发表失败，回退到数据库查询: " + e.getMessage());
            // ES查询失败，回退到数据库查询
            List<ErpDistributionBaseDO> dbDistributionList = distributionMapper.selectListByCreateTimeBetween(startTime, endTime);
            distributionList = dbDistributionList.stream()
                    .map(this::convertToCombinedESDO)
                    .collect(Collectors.toList());
            System.out.println("数据库查询代发表成功，数据量: " + distributionList.size());
        }
        
        // 查询批发表数据 - 优先使用ES搜索
        List<ErpWholesaleCombinedESDO> wholesaleList;
        try {
            wholesaleList = wholesaleCombinedESRepository.findByCreateTimeBetween(startTime, endTime);
            System.out.println("ES查询批发表成功，数据量: " + wholesaleList.size());
        } catch (Exception e) {
            System.out.println("ES查询批发表失败，回退到数据库查询: " + e.getMessage());
            // ES查询失败，回退到数据库查询
            List<ErpWholesaleBaseDO> dbWholesaleList = wholesaleMapper.selectListByCreateTimeBetween(startTime, endTime);
            wholesaleList = dbWholesaleList.stream()
                    .map(this::convertToCombinedESDO)
                    .collect(Collectors.toList());
            System.out.println("数据库查询批发表成功，数据量: " + wholesaleList.size());
        }

        // 1. 统计组品数据
        Map<Long, ComboProductData> comboProductDataMap = new HashMap<>();
        
        // 代发表组品统计
        for (ErpDistributionCombinedESDO distribution : distributionList) {
            if (distribution.getComboProductId() != null) {
                ComboProductData comboData = comboProductDataMap.computeIfAbsent(distribution.getComboProductId(), 
                    k -> new ComboProductData());
                
                comboData.setComboProductId(distribution.getComboProductId());
                comboData.setComboProductNo(distribution.getComboProductNo());
                comboData.setDistributionComboCount(comboData.getDistributionComboCount() + distribution.getProductQuantity());
            }
        }
        
        // 批发表组品统计
        for (ErpWholesaleCombinedESDO wholesale : wholesaleList) {
            if (wholesale.getComboProductId() != null) {
                ComboProductData comboData = comboProductDataMap.computeIfAbsent(wholesale.getComboProductId(), 
                    k -> new ComboProductData());
                
                comboData.setComboProductId(wholesale.getComboProductId());
                comboData.setComboProductNo(wholesale.getComboProductNo());
                comboData.setWholesaleComboCount(comboData.getWholesaleComboCount() + wholesale.getProductQuantity());
            }
        }

        // 2. 从组品单品明细中获取单品统计
        Map<Long, Integer> distributionSingleProductCount = new HashMap<>();
        Map<Long, Integer> wholesaleSingleProductCount = new HashMap<>();
        
        // 从代发表组品中提取单品统计
        for (ErpDistributionCombinedESDO distribution : distributionList) {
            if (distribution.getComboProductId() != null) {
                // 获取组品单品明细
                List<ErpComboProductItemDO> comboItems = comboProductItemMapper.selectByComboProductId(distribution.getComboProductId());
                for (ErpComboProductItemDO item : comboItems) {
                    // 单品数量 = 组品单品明细中的单品数量 × 代发表中的产品数量
                    int itemQuantity = item.getItemQuantity() * distribution.getProductQuantity();
                    distributionSingleProductCount.merge(item.getItemProductId(), itemQuantity, Integer::sum);
                }
            }
        }
        
        // 从批发表组品中提取单品统计
        for (ErpWholesaleCombinedESDO wholesale : wholesaleList) {
            if (wholesale.getComboProductId() != null) {
                // 获取组品单品明细
                List<ErpComboProductItemDO> comboItems = comboProductItemMapper.selectByComboProductId(wholesale.getComboProductId());
                for (ErpComboProductItemDO item : comboItems) {
                    // 单品数量 = 组品单品明细中的单品数量 × 批发表中的产品数量
                    int itemQuantity = item.getItemQuantity() * wholesale.getProductQuantity();
                    wholesaleSingleProductCount.merge(item.getItemProductId(), itemQuantity, Integer::sum);
                }
            }
        }

        // 3. 获取单品详细信息
        Set<Long> allProductIds = new HashSet<>();
        allProductIds.addAll(distributionSingleProductCount.keySet());
        allProductIds.addAll(wholesaleSingleProductCount.keySet());
        
        Map<Long, ErpProductDO> productMap = new HashMap<>();
        if (!allProductIds.isEmpty()) {
            List<ErpProductDO> products = productMapper.selectBatchIds(allProductIds);
            productMap = products.stream().collect(Collectors.toMap(ErpProductDO::getId, p -> p));
        }

        // 4. 构建单品统计列表
        List<ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics> allSingleProductStatistics = new ArrayList<>();
        
        for (Long productId : allProductIds) {
            ErpProductDO product = productMap.get(productId);
            if (product != null) {
                ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics singleStat = new ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics();
                singleStat.setProductName(product.getName());
                singleStat.setProductSpecification(product.getStandard());
                
                int distributionCount = distributionSingleProductCount.getOrDefault(productId, 0);
                int wholesaleCount = wholesaleSingleProductCount.getOrDefault(productId, 0);
                int totalCount = distributionCount + wholesaleCount;
                
                singleStat.setDistributionCount(distributionCount);
                singleStat.setWholesaleCount(wholesaleCount);
                singleStat.setTotalCount(totalCount);
                
                // 计算占比
                if (totalCount > 0) {
                    BigDecimal distributionPercentage = BigDecimal.valueOf(distributionCount)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
                    BigDecimal wholesalePercentage = BigDecimal.valueOf(wholesaleCount)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
                    
                    singleStat.setDistributionPercentage(distributionPercentage);
                    singleStat.setWholesalePercentage(wholesalePercentage);
                } else {
                    singleStat.setDistributionPercentage(BigDecimal.ZERO);
                    singleStat.setWholesalePercentage(BigDecimal.ZERO);
                }
                
                allSingleProductStatistics.add(singleStat);
            }
        }

        // 5. 构建组品统计列表
        List<ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics> allComboProductStatistics = new ArrayList<>();
        
        for (ComboProductData comboData : comboProductDataMap.values()) {
            ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics comboStat = new ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics();
            
            comboStat.setComboProductId(comboData.getComboProductId());
            comboStat.setComboProductNo(comboData.getComboProductNo());
            
            // 获取组品名称
            try {
                ErpComboProductDO comboProduct = comboProductService.getCombo(comboData.getComboProductId());
                if (comboProduct != null && comboProduct.getName() != null) {
                    comboStat.setComboProductName(comboProduct.getName());
                } else {
                    comboStat.setComboProductName("未知组品");
                }
            } catch (Exception e) {
                comboStat.setComboProductName("未知组品");
            }
            
            comboStat.setDistributionComboCount(comboData.getDistributionComboCount());
            comboStat.setWholesaleComboCount(comboData.getWholesaleComboCount());
            comboStat.setTotalComboCount(comboData.getDistributionComboCount() + comboData.getWholesaleComboCount());
            
            // 计算占比
            int totalComboCount = comboStat.getTotalComboCount();
            if (totalComboCount > 0) {
                BigDecimal distributionPercentage = BigDecimal.valueOf(comboData.getDistributionComboCount())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalComboCount), 2, RoundingMode.HALF_UP);
                BigDecimal wholesalePercentage = BigDecimal.valueOf(comboData.getWholesaleComboCount())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalComboCount), 2, RoundingMode.HALF_UP);
                
                comboStat.setDistributionPercentage(distributionPercentage);
                comboStat.setWholesalePercentage(wholesalePercentage);
            } else {
                comboStat.setDistributionPercentage(BigDecimal.ZERO);
                comboStat.setWholesalePercentage(BigDecimal.ZERO);
            }
            
            // 获取组品单品明细
            List<ErpDistributionWholesaleProductStatisticsRespVO.ComboProductItemDetail> itemDetails = new ArrayList<>();
            try {
                List<ErpComboProductItemDO> comboItems = comboProductItemMapper.selectByComboProductId(comboData.getComboProductId());
                for (ErpComboProductItemDO item : comboItems) {
                    ErpProductDO product = productMap.get(item.getItemProductId());
                    if (product != null) {
                        ErpDistributionWholesaleProductStatisticsRespVO.ComboProductItemDetail itemDetail = new ErpDistributionWholesaleProductStatisticsRespVO.ComboProductItemDetail();
                        itemDetail.setProductName(product.getName());
                        itemDetail.setProductSpecification(product.getStandard());
                        itemDetail.setItemQuantity(item.getItemQuantity());
                        itemDetails.add(itemDetail);
                    }
                }
            } catch (Exception e) {
                // 忽略错误，继续处理
            }
            comboStat.setItemDetails(itemDetails);
            
            allComboProductStatistics.add(comboStat);
        }

        // 6. 计算总计
        int totalDistributionSingleCount = allSingleProductStatistics.stream()
                .mapToInt(ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics::getDistributionCount)
                .sum();
        int totalWholesaleSingleCount = allSingleProductStatistics.stream()
                .mapToInt(ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics::getWholesaleCount)
                .sum();
        int totalSingleCount = totalDistributionSingleCount + totalWholesaleSingleCount;
        
        int totalDistributionComboCount = allComboProductStatistics.stream()
                .mapToInt(ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics::getDistributionComboCount)
                .sum();
        int totalWholesaleComboCount = allComboProductStatistics.stream()
                .mapToInt(ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics::getWholesaleComboCount)
                .sum();
        int totalComboCount = totalDistributionComboCount + totalWholesaleComboCount;

        // 7. 分页处理
        int pageNo = reqVO.getPageNo();
        int pageSize = reqVO.getPageSize();
        
        // 单品分页
        int singleStartIndex = (pageNo - 1) * pageSize;
        int singleEndIndex = Math.min(singleStartIndex + pageSize, allSingleProductStatistics.size());
        List<ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics> singleProductStatistics = 
            allSingleProductStatistics.subList(singleStartIndex, singleEndIndex);
        
        // 组品分页
        int comboStartIndex = (pageNo - 1) * pageSize;
        int comboEndIndex = Math.min(comboStartIndex + pageSize, allComboProductStatistics.size());
        List<ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics> comboProductStatistics = 
            allComboProductStatistics.subList(comboStartIndex, comboEndIndex);

        // 8. 构建分页结果
        PageResult<ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics> singleProductPageResult = 
            new PageResult<>(singleProductStatistics, (long) allSingleProductStatistics.size());
        PageResult<ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics> comboProductPageResult = 
            new PageResult<>(comboProductStatistics, (long) allComboProductStatistics.size());

        // 9. 构建响应对象
        ErpDistributionWholesaleProductStatisticsRespVO respVO = new ErpDistributionWholesaleProductStatisticsRespVO();
        respVO.setSingleProductPageResult(singleProductPageResult);
        respVO.setComboProductPageResult(comboProductPageResult);
        
        System.out.println("分页统计完成:");
        System.out.println("- 单品总数: " + allSingleProductStatistics.size() + ", 当前页: " + singleProductStatistics.size());
        System.out.println("- 组品总数: " + allComboProductStatistics.size() + ", 当前页: " + comboProductStatistics.size());
        System.out.println("- 代发表单品总数: " + totalDistributionSingleCount);
        System.out.println("- 批发表单品总数: " + totalWholesaleSingleCount);
        System.out.println("- 代发表组品总数: " + totalDistributionComboCount);
        System.out.println("- 批发表组品总数: " + totalWholesaleComboCount);
        
        respVO.setTotalDistributionSingleCount(totalDistributionSingleCount);
        respVO.setTotalWholesaleSingleCount(totalWholesaleSingleCount);
        respVO.setTotalSingleCount(totalSingleCount);
        respVO.setTotalDistributionComboCount(totalDistributionComboCount);
        respVO.setTotalWholesaleComboCount(totalWholesaleComboCount);
        respVO.setTotalComboCount(totalComboCount);
        
        return respVO;
    }

    /**
     * 将数据库DO转换为CombinedESDO
     */
    private ErpDistributionCombinedESDO convertToCombinedESDO(ErpDistributionBaseDO dbDO) {
        ErpDistributionCombinedESDO esDO = new ErpDistributionCombinedESDO();
        BeanUtils.copyProperties(dbDO, esDO);
        return esDO;
    }

    /**
     * 将数据库DO转换为CombinedESDO
     */
    private ErpWholesaleCombinedESDO convertToCombinedESDO(ErpWholesaleBaseDO dbDO) {
        ErpWholesaleCombinedESDO esDO = new ErpWholesaleCombinedESDO();
        BeanUtils.copyProperties(dbDO, esDO);
        return esDO;
    }

    /**
     * 组品数据内部类
     */
    private static class ComboProductData {
        private Long comboProductId;
        private String comboProductNo;
        private int distributionComboCount = 0;
        private int wholesaleComboCount = 0;

        // Getters and Setters
        public Long getComboProductId() { return comboProductId; }
        public void setComboProductId(Long comboProductId) { this.comboProductId = comboProductId; }

        public String getComboProductNo() { return comboProductNo; }
        public void setComboProductNo(String comboProductNo) { this.comboProductNo = comboProductNo; }

        public int getDistributionComboCount() { return distributionComboCount; }
        public void setDistributionComboCount(int distributionComboCount) { this.distributionComboCount = distributionComboCount; }

        public int getWholesaleComboCount() { return wholesaleComboCount; }
        public void setWholesaleComboCount(int wholesaleComboCount) { this.wholesaleComboCount = wholesaleComboCount; }
    }
}
