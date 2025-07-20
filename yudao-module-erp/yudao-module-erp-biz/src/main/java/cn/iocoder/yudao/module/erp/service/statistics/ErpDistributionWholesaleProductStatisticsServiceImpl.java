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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Sort;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductESRepository;
import java.util.Comparator;
import java.util.Collections;
import java.util.Objects;

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

    @Resource
    private ErpComboProductESRepository comboProductESRepository;
    
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public ErpDistributionWholesaleProductStatisticsRespVO getDistributionWholesaleProductStatistics(ErpDistributionWholesaleProductStatisticsReqVO reqVO) {
        System.out.println("=== 开始统计代发批发产品组品数据 ===");
        System.out.println("请求参数: startDate=" + reqVO.getStartDate() + ", endDate=" + reqVO.getEndDate());

        // 构建查询条件 - 使用字符串格式日期优化ES查询
        String startDateStr = reqVO.getStartDate() + " 00:00:00";
        String endDateStr = reqVO.getEndDate() + " 23:59:59";
        System.out.println("查询时间范围: " + startDateStr + " 到 " + endDateStr);

        // 查询代发表数据 - 使用search_after实现深度分页不限量查询
        List<ErpDistributionCombinedESDO> distributionList = new ArrayList<>();
        try {
            // 构建原生查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            // 时间范围查询
            boolQuery.must(QueryBuilders.rangeQuery("create_time")
                    .gte(startDateStr)
                    .lte(endDateStr));

            // 执行search_after查询
            final int batchSize = 2000; // 每批次大小
            int batchCount = 0;
            int totalCount = 0;
            
            // 首次查询，不带search_after参数
            NativeSearchQuery initialQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withSort(Sort.by(Sort.Direction.ASC, "id")) // 必须有稳定排序
                    .withSort(Sort.by(Sort.Direction.ASC, "_id")) // 添加_id排序确保稳定性
                    .withPageable(PageRequest.of(0, batchSize))
                    .build();
            
            SearchHits<ErpDistributionCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    initialQuery, ErpDistributionCombinedESDO.class);
            
            // 处理第一批结果
            List<SearchHit<ErpDistributionCombinedESDO>> hits = new ArrayList<>(searchHits.getSearchHits());
            if (!hits.isEmpty()) {
                // 提取内容
                List<ErpDistributionCombinedESDO> firstBatch = hits.stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList());
                
                distributionList.addAll(firstBatch);
                totalCount += firstBatch.size();
                batchCount++;
                System.out.println("代发表查询批次" + batchCount + ", 获取数据: " + firstBatch.size() + 
                                   ", 累计: " + totalCount);
                
                // 如果第一批数据量等于批次大小，说明可能还有更多数据
                while (hits.size() == batchSize) {
                    // 获取最后一个文档的排序值
                    List<Object> sortValues = hits.get(hits.size() - 1).getSortValues();
                    
                    // 构建下一批次查询，使用search_after
                    NativeSearchQuery nextQuery = new NativeSearchQueryBuilder()
                            .withQuery(boolQuery)
                            .withSort(Sort.by(Sort.Direction.ASC, "id"))
                            .withSort(Sort.by(Sort.Direction.ASC, "_id"))
                            .withPageable(PageRequest.of(0, batchSize))
                            .withSearchAfter(sortValues)
                            .build();
                    
                    // 执行下一批次查询
                    searchHits = elasticsearchRestTemplate.search(nextQuery, ErpDistributionCombinedESDO.class);
                    hits = new ArrayList<>(searchHits.getSearchHits());
                    
                    // 处理查询结果
                    List<ErpDistributionCombinedESDO> nextBatch = hits.stream()
                            .map(SearchHit::getContent)
                            .collect(Collectors.toList());
                    
                    distributionList.addAll(nextBatch);
                    totalCount += nextBatch.size();
                    batchCount++;
                    System.out.println("代发表查询批次" + batchCount + ", 获取数据: " + nextBatch.size() + 
                                       ", 累计: " + totalCount);
                    
                    // 如果批次为空，退出循环
                    if (hits.isEmpty()) {
                        break;
                    }
                }
            }
            
            System.out.println("ES查询代发表成功，总批次: " + batchCount + ", 数据量: " + totalCount);
        } catch (Exception e) {
            System.out.println("ES查询代发表失败: " + e.getMessage());
            e.printStackTrace(); // 打印详细错误信息以便排查
        }

        // 查询批发表数据 - 使用search_after实现深度分页不限量查询
        List<ErpWholesaleCombinedESDO> wholesaleList = new ArrayList<>();
        try {
            // 构建原生查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            // 时间范围查询
            boolQuery.must(QueryBuilders.rangeQuery("create_time")
                    .gte(startDateStr)
                    .lte(endDateStr));

            // 执行search_after查询
            final int batchSize = 2000; // 每批次大小
            int batchCount = 0;
            int totalCount = 0;
            
            // 首次查询，不带search_after参数
            NativeSearchQuery initialQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withSort(Sort.by(Sort.Direction.ASC, "id")) // 必须有稳定排序
                    .withSort(Sort.by(Sort.Direction.ASC, "_id")) // 添加_id排序确保稳定性
                    .withPageable(PageRequest.of(0, batchSize))
                    .build();
            
            SearchHits<ErpWholesaleCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    initialQuery, ErpWholesaleCombinedESDO.class);
            
            // 处理第一批结果
            List<SearchHit<ErpWholesaleCombinedESDO>> hits = new ArrayList<>(searchHits.getSearchHits());
            if (!hits.isEmpty()) {
                // 提取内容
                List<ErpWholesaleCombinedESDO> firstBatch = hits.stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList());
                
                wholesaleList.addAll(firstBatch);
                totalCount += firstBatch.size();
                batchCount++;
                System.out.println("批发表查询批次" + batchCount + ", 获取数据: " + firstBatch.size() + 
                                   ", 累计: " + totalCount);
                
                // 如果第一批数据量等于批次大小，说明可能还有更多数据
                while (hits.size() == batchSize) {
                    // 获取最后一个文档的排序值
                    List<Object> sortValues = hits.get(hits.size() - 1).getSortValues();
                    
                    // 构建下一批次查询，使用search_after
                    NativeSearchQuery nextQuery = new NativeSearchQueryBuilder()
                            .withQuery(boolQuery)
                            .withSort(Sort.by(Sort.Direction.ASC, "id"))
                            .withSort(Sort.by(Sort.Direction.ASC, "_id"))
                            .withPageable(PageRequest.of(0, batchSize))
                            .withSearchAfter(sortValues)
                            .build();
                    
                    // 执行下一批次查询
                    searchHits = elasticsearchRestTemplate.search(nextQuery, ErpWholesaleCombinedESDO.class);
                    hits = new ArrayList<>(searchHits.getSearchHits());
                    
                    // 处理查询结果
                    List<ErpWholesaleCombinedESDO> nextBatch = hits.stream()
                            .map(SearchHit::getContent)
                            .collect(Collectors.toList());
                    
                    wholesaleList.addAll(nextBatch);
                    totalCount += nextBatch.size();
                    batchCount++;
                    System.out.println("批发表查询批次" + batchCount + ", 获取数据: " + nextBatch.size() + 
                                       ", 累计: " + totalCount);
                    
                    // 如果批次为空，退出循环
                    if (hits.isEmpty()) {
                        break;
                    }
                }
            }
            
            System.out.println("ES查询批发表成功，总批次: " + batchCount + ", 数据量: " + totalCount);
        } catch (Exception e) {
            System.out.println("ES查询批发表失败: " + e.getMessage());
            e.printStackTrace(); // 打印详细错误信息以便排查
        }

        // 1. 统计组品数据
        Map<Long, ComboProductData> comboProductDataMap = new HashMap<>();

        // 🔥 批量查组品信息
        Set<Long> allComboProductIds = distributionList.stream()
            .map(ErpDistributionCombinedESDO::getComboProductId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        allComboProductIds.addAll(wholesaleList.stream()
            .map(ErpWholesaleCombinedESDO::getComboProductId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()));
        Map<Long, ErpComboProductES> comboProductMap = new HashMap<>();
        if (!allComboProductIds.isEmpty()) {
            try {
                Iterable<ErpComboProductES> comboProducts = comboProductESRepository.findAllById(allComboProductIds);
                comboProducts.forEach(combo -> comboProductMap.put(combo.getId(), combo));
            } catch (Exception e) {
                System.out.println("ES查询组品信息失败: " + e.getMessage());
                e.printStackTrace(); // 打印详细错误信息以便排查
            }
        }

        // 代发表组品统计
        for (ErpDistributionCombinedESDO distribution : distributionList) {
            if (distribution.getComboProductId() != null) {
                ComboProductData comboData = comboProductDataMap.computeIfAbsent(distribution.getComboProductId(),
                    k -> new ComboProductData());

                comboData.setComboProductId(distribution.getComboProductId());
                // 实时查组品编号
                ErpComboProductES comboProduct = comboProductMap.get(distribution.getComboProductId());
                if (comboProduct != null) {
                    comboData.setComboProductNo(comboProduct.getNo());
                }
                comboData.setDistributionComboCount(comboData.getDistributionComboCount() + distribution.getProductQuantity());
            }
        }

        // 批发表组品统计
        for (ErpWholesaleCombinedESDO wholesale : wholesaleList) {
            if (wholesale.getComboProductId() != null) {
                ComboProductData comboData = comboProductDataMap.computeIfAbsent(wholesale.getComboProductId(),
                    k -> new ComboProductData());

                comboData.setComboProductId(wholesale.getComboProductId());
                // 实时查组品编号
                ErpComboProductES comboProduct = comboProductMap.get(wholesale.getComboProductId());
                if (comboProduct != null) {
                    comboData.setComboProductNo(comboProduct.getNo());
                }
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

            // 获取组品名称（实时查）
            ErpComboProductES comboProduct = comboProductMap.get(comboData.getComboProductId());
            if (comboProduct != null && comboProduct.getName() != null) {
                comboStat.setComboProductName(comboProduct.getName());
            } else {
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
        long startTime = System.currentTimeMillis();
        System.out.println("=== 开始统计代发批发产品组品数据（分页） ===");
        System.out.println("请求参数: startDate=" + reqVO.getStartDate() + ", endDate=" + reqVO.getEndDate() + 
                          ", pageNo=" + reqVO.getPageNo() + ", pageSize=" + reqVO.getPageSize());
        
        // 构建查询条件 - 使用字符串格式日期优化ES查询
        String startDateStr = reqVO.getStartDate() + " 00:00:00";
        String endDateStr = reqVO.getEndDate() + " 23:59:59";
        System.out.println("查询时间范围: " + startDateStr + " 到 " + endDateStr);

        // 查询代发表数据 - 使用search_after实现深度分页不限量查询
        List<ErpDistributionCombinedESDO> distributionList = new ArrayList<>();
        try {
            // 构建原生查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            // 时间范围查询
            boolQuery.must(QueryBuilders.rangeQuery("create_time")
                    .gte(startDateStr)
                    .lte(endDateStr));

            // 执行search_after查询
            final int batchSize = 2000; // 每批次大小
            int batchCount = 0;
            int totalCount = 0;
            
            // 首次查询，不带search_after参数
            NativeSearchQuery initialQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withSort(Sort.by(Sort.Direction.ASC, "id")) // 必须有稳定排序
                    .withSort(Sort.by(Sort.Direction.ASC, "_id")) // 添加_id排序确保稳定性
                    .withPageable(PageRequest.of(0, batchSize))
                    .build();
            
            SearchHits<ErpDistributionCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    initialQuery, ErpDistributionCombinedESDO.class);
            
            // 处理第一批结果
            List<SearchHit<ErpDistributionCombinedESDO>> hits = new ArrayList<>(searchHits.getSearchHits());
            if (!hits.isEmpty()) {
                // 提取内容
                List<ErpDistributionCombinedESDO> firstBatch = hits.stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList());
                
                distributionList.addAll(firstBatch);
                totalCount += firstBatch.size();
                batchCount++;
                System.out.println("代发表查询批次" + batchCount + ", 获取数据: " + firstBatch.size() + 
                                   ", 累计: " + totalCount);
                
                // 如果第一批数据量等于批次大小，说明可能还有更多数据
                while (hits.size() == batchSize) {
                    // 获取最后一个文档的排序值
                    List<Object> sortValues = hits.get(hits.size() - 1).getSortValues();
                    
                    // 构建下一批次查询，使用search_after
                    NativeSearchQuery nextQuery = new NativeSearchQueryBuilder()
                            .withQuery(boolQuery)
                            .withSort(Sort.by(Sort.Direction.ASC, "id"))
                            .withSort(Sort.by(Sort.Direction.ASC, "_id"))
                            .withPageable(PageRequest.of(0, batchSize))
                            .withSearchAfter(sortValues)
                            .build();
                    
                    // 执行下一批次查询
                    searchHits = elasticsearchRestTemplate.search(nextQuery, ErpDistributionCombinedESDO.class);
                    hits = new ArrayList<>(searchHits.getSearchHits());
                    
                    // 处理查询结果
                    List<ErpDistributionCombinedESDO> nextBatch = hits.stream()
                            .map(SearchHit::getContent)
                            .collect(Collectors.toList());
                    
                    distributionList.addAll(nextBatch);
                    totalCount += nextBatch.size();
                    batchCount++;
                    System.out.println("代发表查询批次" + batchCount + ", 获取数据: " + nextBatch.size() + 
                                       ", 累计: " + totalCount);
                    
                    // 如果批次为空，退出循环
                    if (hits.isEmpty()) {
                        break;
                    }
                }
            }
            
            System.out.println("ES查询代发表成功，总批次: " + batchCount + ", 数据量: " + totalCount);
        } catch (Exception e) {
            System.out.println("ES查询代发表失败: " + e.getMessage());
            e.printStackTrace(); // 打印详细错误信息以便排查
        }
        
        // 查询批发表数据 - 使用search_after实现深度分页不限量查询
        List<ErpWholesaleCombinedESDO> wholesaleList = new ArrayList<>();
        try {
            // 构建原生查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            // 时间范围查询
            boolQuery.must(QueryBuilders.rangeQuery("create_time")
                    .gte(startDateStr)
                    .lte(endDateStr));

            // 执行search_after查询
            final int batchSize = 2000; // 每批次大小
            int batchCount = 0;
            int totalCount = 0;
            
            // 首次查询，不带search_after参数
            NativeSearchQuery initialQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withSort(Sort.by(Sort.Direction.ASC, "id")) // 必须有稳定排序
                    .withSort(Sort.by(Sort.Direction.ASC, "_id")) // 添加_id排序确保稳定性
                    .withPageable(PageRequest.of(0, batchSize))
                    .build();
            
            SearchHits<ErpWholesaleCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    initialQuery, ErpWholesaleCombinedESDO.class);
            
            // 处理第一批结果
            List<SearchHit<ErpWholesaleCombinedESDO>> hits = new ArrayList<>(searchHits.getSearchHits());
            if (!hits.isEmpty()) {
                // 提取内容
                List<ErpWholesaleCombinedESDO> firstBatch = hits.stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList());
                
                wholesaleList.addAll(firstBatch);
                totalCount += firstBatch.size();
                batchCount++;
                System.out.println("批发表查询批次" + batchCount + ", 获取数据: " + firstBatch.size() + 
                                   ", 累计: " + totalCount);
                
                // 如果第一批数据量等于批次大小，说明可能还有更多数据
                while (hits.size() == batchSize) {
                    // 获取最后一个文档的排序值
                    List<Object> sortValues = hits.get(hits.size() - 1).getSortValues();
                    
                    // 构建下一批次查询，使用search_after
                    NativeSearchQuery nextQuery = new NativeSearchQueryBuilder()
                            .withQuery(boolQuery)
                            .withSort(Sort.by(Sort.Direction.ASC, "id"))
                            .withSort(Sort.by(Sort.Direction.ASC, "_id"))
                            .withPageable(PageRequest.of(0, batchSize))
                            .withSearchAfter(sortValues)
                            .build();
                    
                    // 执行下一批次查询
                    searchHits = elasticsearchRestTemplate.search(nextQuery, ErpWholesaleCombinedESDO.class);
                    hits = new ArrayList<>(searchHits.getSearchHits());
                    
                    // 处理查询结果
                    List<ErpWholesaleCombinedESDO> nextBatch = hits.stream()
                            .map(SearchHit::getContent)
                            .collect(Collectors.toList());
                    
                    wholesaleList.addAll(nextBatch);
                    totalCount += nextBatch.size();
                    batchCount++;
                    System.out.println("批发表查询批次" + batchCount + ", 获取数据: " + nextBatch.size() + 
                                       ", 累计: " + totalCount);
                    
                    // 如果批次为空，退出循环
                    if (hits.isEmpty()) {
                        break;
                    }
                }
            }
            
            // 调试：输出批发表中组品ID信息
            Set<Long> wholesaleComboIds = wholesaleList.stream()
                .map(ErpWholesaleCombinedESDO::getComboProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            System.out.println("批发表中包含的组品ID数量: " + wholesaleComboIds.size());
            if (!wholesaleComboIds.isEmpty()) {
                System.out.println("批发表中的组品ID示例: " + wholesaleComboIds.iterator().next());
            }
            
            System.out.println("ES查询批发表成功，总批次: " + batchCount + ", 数据量: " + totalCount);
        } catch (Exception e) {
            System.out.println("ES查询批发表失败: " + e.getMessage());
            e.printStackTrace(); // 打印详细错误信息以便排查
        }
        System.out.println("ES查询耗时: " + (System.currentTimeMillis() - startTime) + "ms");

        // 1. 统计组品数据 (使用并行流提升处理速度)
        Map<Long, ComboProductData> comboProductDataMap = new ConcurrentHashMap<>();
        
        // 🔥 批量查组品信息
        Set<Long> allComboProductIds = distributionList.parallelStream()
            .map(ErpDistributionCombinedESDO::getComboProductId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
            
        allComboProductIds.addAll(wholesaleList.parallelStream()
            .map(ErpWholesaleCombinedESDO::getComboProductId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()));
        
        System.out.println("合计组品ID数量: " + allComboProductIds.size());
        
        Map<Long, ErpComboProductES> comboProductMap = new ConcurrentHashMap<>();
        if (!allComboProductIds.isEmpty()) {
            try {
                Iterable<ErpComboProductES> comboProducts = comboProductESRepository.findAllById(allComboProductIds);
                for (ErpComboProductES combo : comboProducts) {
                    comboProductMap.put(combo.getId(), combo);
                }
                System.out.println("ES查询组品信息成功，数量: " + comboProductMap.size());
            } catch (Exception e) {
                System.out.println("ES查询组品信息失败: " + e.getMessage());
                e.printStackTrace(); // 打印详细错误信息以便排查
            }
        }
        
        // 代发表组品统计 (使用并行流提升处理速度)
        distributionList.parallelStream().forEach(distribution -> {
            if (distribution.getComboProductId() != null) {
                ComboProductData comboData = comboProductDataMap.computeIfAbsent(distribution.getComboProductId(), 
                    k -> new ComboProductData());
                
                comboData.setComboProductId(distribution.getComboProductId());
                // 实时查组品编号
                ErpComboProductES comboProduct = comboProductMap.get(distribution.getComboProductId());
                if (comboProduct != null) {
                    comboData.setComboProductNo(comboProduct.getNo());
                }
                
                // 使用原子操作避免并发问题
                synchronized (comboData) {
                    comboData.setDistributionComboCount(comboData.getDistributionComboCount() + distribution.getProductQuantity());
                }
            }
        });
        
        // 批发表组品统计 (使用并行流提升处理速度)
        wholesaleList.parallelStream().forEach(wholesale -> {
            if (wholesale.getComboProductId() != null) {
                ComboProductData comboData = comboProductDataMap.computeIfAbsent(wholesale.getComboProductId(),
                    k -> new ComboProductData());

                comboData.setComboProductId(wholesale.getComboProductId());
                // 实时查组品编号
                ErpComboProductES comboProduct = comboProductMap.get(wholesale.getComboProductId());
                if (comboProduct != null) {
                    comboData.setComboProductNo(comboProduct.getNo());
                }
                
                // 使用原子操作避免并发问题
                synchronized (comboData) {
                    comboData.setWholesaleComboCount(comboData.getWholesaleComboCount() + wholesale.getProductQuantity());
                }
            }
        });

        // 调试输出批发表统计结果
        int totalWholesaleItems = comboProductDataMap.values().stream()
            .mapToInt(ComboProductData::getWholesaleComboCount)
            .sum();
        System.out.println("批发表组品统计总数: " + totalWholesaleItems);

        // 2. 从组品单品明细中获取单品统计 (使用并行流和并发集合提升处理速度)
        Map<Long, Integer> distributionSingleProductCount = new ConcurrentHashMap<>();
        Map<Long, Integer> wholesaleSingleProductCount = new ConcurrentHashMap<>();

        // 使用并发处理并缓存组品明细查询结果
        Map<Long, List<ErpComboProductItemDO>> comboItemsCache = new ConcurrentHashMap<>();
        
        // 批量加载所有组品明细，避免重复查询
        allComboProductIds.forEach(comboId -> {
            try {
                List<ErpComboProductItemDO> items = comboProductItemMapper.selectByComboProductId(comboId);
                if (items != null && !items.isEmpty()) {
                    comboItemsCache.put(comboId, items);
                }
            } catch (Exception e) {
                System.out.println("加载组品明细失败, comboId=" + comboId + ": " + e.getMessage());
            }
        });
        System.out.println("组品明细缓存数量: " + comboItemsCache.size());

        // 从代发表组品中提取单品统计 (使用并行流提升处理速度)
        distributionList.parallelStream().forEach(distribution -> {
            if (distribution.getComboProductId() != null) {
                // 从缓存获取组品单品明细
                List<ErpComboProductItemDO> comboItems = comboItemsCache.get(distribution.getComboProductId());
                if (comboItems != null) {
                    for (ErpComboProductItemDO item : comboItems) {
                        // 单品数量 = 组品单品明细中的单品数量 × 代发表中的产品数量
                        int itemQuantity = item.getItemQuantity() * distribution.getProductQuantity();
                        // 使用ConcurrentHashMap的原子操作
                        distributionSingleProductCount.compute(item.getItemProductId(), 
                            (k, v) -> (v == null) ? itemQuantity : v + itemQuantity);
                    }
                }
            }
        });

        // 从批发表组品中提取单品统计 (使用并行流提升处理速度)
        wholesaleList.parallelStream().forEach(wholesale -> {
            if (wholesale.getComboProductId() != null) {
                // 从缓存获取组品单品明细
                List<ErpComboProductItemDO> comboItems = comboItemsCache.get(wholesale.getComboProductId());
                if (comboItems != null) {
                    for (ErpComboProductItemDO item : comboItems) {
                        // 单品数量 = 组品单品明细中的单品数量 × 批发表中的产品数量
                        int itemQuantity = item.getItemQuantity() * wholesale.getProductQuantity();
                        // 使用ConcurrentHashMap的原子操作
                        wholesaleSingleProductCount.compute(item.getItemProductId(), 
                            (k, v) -> (v == null) ? itemQuantity : v + itemQuantity);
                    }
                }
            }
        });

        System.out.println("代发表单品统计数量: " + distributionSingleProductCount.size());
        System.out.println("批发表单品统计数量: " + wholesaleSingleProductCount.size());

        // 3. 获取单品详细信息 (批量加载提高性能)
        Set<Long> allProductIds = new HashSet<>();
        allProductIds.addAll(distributionSingleProductCount.keySet());
        allProductIds.addAll(wholesaleSingleProductCount.keySet());

        Map<Long, ErpProductDO> productMap = new HashMap<>();
        if (!allProductIds.isEmpty()) {
            // 分批加载产品信息，避免一次性加载过多数据
            List<Long> productIdList = new ArrayList<>(allProductIds);
            int batchSize = 100;
            for (int i = 0; i < productIdList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, productIdList.size());
                List<Long> batchIds = productIdList.subList(i, end);
                List<ErpProductDO> products = productMapper.selectBatchIds(batchIds);
                products.forEach(p -> productMap.put(p.getId(), p));
            }
            System.out.println("加载产品信息成功，数量: " + productMap.size());
        }

        // 4. 构建单品统计列表
        List<ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics> allSingleProductStatistics = 
            Collections.synchronizedList(new ArrayList<>());

        allProductIds.parallelStream().forEach(productId -> {
            ErpProductDO product = productMap.get(productId);
            if (product != null) {
                ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics singleStat = 
                    new ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics();
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

                synchronized (allSingleProductStatistics) {
                    allSingleProductStatistics.add(singleStat);
                }
            }
        });

        // 预排序单品统计，提高分页效率
        List<ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics> sortedSingleProductStatistics = 
            allSingleProductStatistics.stream()
                .sorted(Comparator.comparing(ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics::getTotalCount).reversed())
                .collect(Collectors.toList());

        // 5. 构建组品统计列表 (并行处理)
        List<ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics> allComboProductStatistics = 
            Collections.synchronizedList(new ArrayList<>());

        comboProductDataMap.entrySet().parallelStream().forEach(entry -> {
            Long comboId = entry.getKey();
            ComboProductData comboData = entry.getValue();
            
            ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics comboStat = 
                new ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics();

            comboStat.setComboProductId(comboData.getComboProductId());
            comboStat.setComboProductNo(comboData.getComboProductNo());

            // 优先从ES缓存中获取组品名称
            ErpComboProductES comboProduct = comboProductMap.get(comboId);
            if (comboProduct != null && comboProduct.getName() != null) {
                comboStat.setComboProductName(comboProduct.getName());
            } else {
                // 回退到数据库查询
                try {
                    ErpComboProductDO comboProductDO = comboProductService.getCombo(comboId);
                    if (comboProductDO != null && comboProductDO.getName() != null) {
                        comboStat.setComboProductName(comboProductDO.getName());
                    } else {
                        comboStat.setComboProductName("未知组品");
                    }
                } catch (Exception e) {
                    comboStat.setComboProductName("未知组品");
                }
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

            // 获取组品单品明细（从缓存中获取）
            List<ErpDistributionWholesaleProductStatisticsRespVO.ComboProductItemDetail> itemDetails = new ArrayList<>();
            List<ErpComboProductItemDO> comboItems = comboItemsCache.get(comboId);
            if (comboItems != null) {
                for (ErpComboProductItemDO item : comboItems) {
                    ErpProductDO product = productMap.get(item.getItemProductId());
                    if (product != null) {
                        ErpDistributionWholesaleProductStatisticsRespVO.ComboProductItemDetail itemDetail = 
                            new ErpDistributionWholesaleProductStatisticsRespVO.ComboProductItemDetail();
                        itemDetail.setProductName(product.getName());
                        itemDetail.setProductSpecification(product.getStandard());
                        itemDetail.setItemQuantity(item.getItemQuantity());
                        itemDetails.add(itemDetail);
                    }
                }
            }
            comboStat.setItemDetails(itemDetails);

            synchronized (allComboProductStatistics) {
                allComboProductStatistics.add(comboStat);
            }
        });

        // 预排序组品统计，提高分页效率
        List<ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics> sortedComboProductStatistics = 
            allComboProductStatistics.stream()
                .sorted(Comparator.comparing(ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics::getTotalComboCount).reversed())
                .collect(Collectors.toList());

        // 6. 计算总计
        int totalDistributionSingleCount = sortedSingleProductStatistics.stream()
                .mapToInt(ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics::getDistributionCount)
                .sum();
        int totalWholesaleSingleCount = sortedSingleProductStatistics.stream()
                .mapToInt(ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics::getWholesaleCount)
                .sum();
        int totalSingleCount = totalDistributionSingleCount + totalWholesaleSingleCount;

        int totalDistributionComboCount = sortedComboProductStatistics.stream()
                .mapToInt(ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics::getDistributionComboCount)
                .sum();
        int totalWholesaleComboCount = sortedComboProductStatistics.stream()
                .mapToInt(ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics::getWholesaleComboCount)
                .sum();
        int totalComboCount = totalDistributionComboCount + totalWholesaleComboCount;

        // 7. 分页处理
        int pageNo = reqVO.getPageNo();
        int pageSize = reqVO.getPageSize();

        // 单品分页 (使用预排序列表)
        int singleStartIndex = (pageNo - 1) * pageSize;
        int singleEndIndex = Math.min(singleStartIndex + pageSize, sortedSingleProductStatistics.size());
        List<ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics> singleProductStatistics;
        if (singleStartIndex < sortedSingleProductStatistics.size()) {
            singleProductStatistics = sortedSingleProductStatistics.subList(singleStartIndex, singleEndIndex);
        } else {
            singleProductStatistics = new ArrayList<>();
        }

        // 组品分页 (使用预排序列表)
        int comboStartIndex = (pageNo - 1) * pageSize;
        int comboEndIndex = Math.min(comboStartIndex + pageSize, sortedComboProductStatistics.size());
        List<ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics> comboProductStatistics;
        if (comboStartIndex < sortedComboProductStatistics.size()) {
            comboProductStatistics = sortedComboProductStatistics.subList(comboStartIndex, comboEndIndex);
        } else {
            comboProductStatistics = new ArrayList<>();
        }

        // 8. 构建分页结果
        PageResult<ErpDistributionWholesaleProductStatisticsRespVO.SingleProductStatistics> singleProductPageResult =
            new PageResult<>(singleProductStatistics, (long) sortedSingleProductStatistics.size());
        PageResult<ErpDistributionWholesaleProductStatisticsRespVO.ComboProductStatistics> comboProductPageResult =
            new PageResult<>(comboProductStatistics, (long) sortedComboProductStatistics.size());

        // 9. 构建响应对象
        ErpDistributionWholesaleProductStatisticsRespVO respVO = new ErpDistributionWholesaleProductStatisticsRespVO();
        respVO.setSingleProductPageResult(singleProductPageResult);
        respVO.setComboProductPageResult(comboProductPageResult);

        System.out.println("分页统计完成:");
        System.out.println("- 单品总数: " + sortedSingleProductStatistics.size() + ", 当前页: " + singleProductStatistics.size());
        System.out.println("- 组品总数: " + sortedComboProductStatistics.size() + ", 当前页: " + comboProductStatistics.size());
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
        
        System.out.println("统计处理总耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        return respVO;
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
