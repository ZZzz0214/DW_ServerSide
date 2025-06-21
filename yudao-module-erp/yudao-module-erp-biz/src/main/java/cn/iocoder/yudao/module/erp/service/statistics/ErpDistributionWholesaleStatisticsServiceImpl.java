package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleStatisticsRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionCombinedESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleCombinedESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceESDO;
import cn.iocoder.yudao.module.erp.service.distribution.ErpDistributionCombinedESRepository;
import cn.iocoder.yudao.module.erp.service.wholesale.ErpWholesaleCombinedESRepository;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductESRepository;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalePriceESRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ERP 代发批发统计 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class ErpDistributionWholesaleStatisticsServiceImpl implements ErpDistributionWholesaleStatisticsService {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private ErpDistributionCombinedESRepository distributionCombinedESRepository;

    @Resource
    private ErpWholesaleCombinedESRepository wholesaleCombinedESRepository;

    @Resource
    private ErpComboProductESRepository comboProductESRepository;

    @Resource
    private ErpSalePriceESRepository salePriceESRepository;

    @Override
    public ErpDistributionWholesaleStatisticsRespVO getDistributionWholesaleStatistics(ErpDistributionWholesaleStatisticsReqVO reqVO) {
        ErpDistributionWholesaleStatisticsRespVO respVO = new ErpDistributionWholesaleStatisticsRespVO();
        respVO.setStatisticsType(reqVO.getStatisticsType());

        // 添加总体调试信息
        System.out.println("=== 开始代发批发统计查询 ===");
        System.out.println("请求参数: " + reqVO);

        // 先测试ES中是否有数据
        testESDataAvailability();

        // 测试无时间限制的查询
        testQueryWithoutTimeLimit(reqVO);

        // 从ES获取代发和批发数据
        List<ErpDistributionCombinedESDO> distributionData = getDistributionDataFromES(reqVO);
        List<ErpWholesaleCombinedESDO> wholesaleData = getWholesaleDataFromES(reqVO);

        System.out.println("获取到代发数据: " + distributionData.size() + " 条");
        System.out.println("获取到批发数据: " + wholesaleData.size() + " 条");

        // 合并统计数据
        List<ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> items = mergeStatisticsData(
                distributionData, wholesaleData, reqVO.getStatisticsType());
        respVO.setItems(items);

        System.out.println("最终统计项数量: " + items.size());
        System.out.println("=== 代发批发统计查询结束 ===");

        return respVO;
    }

    /**
     * 测试ES数据可用性
     */
    private void testESDataAvailability() {
        try {
            // 测试代发数据
            NativeSearchQuery testDistributionQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchAllQuery())
                    .withPageable(PageRequest.of(0, 1))
                    .build();

            SearchHits<ErpDistributionCombinedESDO> distributionHits = elasticsearchRestTemplate.search(
                    testDistributionQuery, ErpDistributionCombinedESDO.class);
            System.out.println("ES中代发数据总数: " + distributionHits.getTotalHits());

            // 测试批发数据
            NativeSearchQuery testWholesaleQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchAllQuery())
                    .withPageable(PageRequest.of(0, 1))
                    .build();

            SearchHits<ErpWholesaleCombinedESDO> wholesaleHits = elasticsearchRestTemplate.search(
                    testWholesaleQuery, ErpWholesaleCombinedESDO.class);
            System.out.println("ES中批发数据总数: " + wholesaleHits.getTotalHits());

        } catch (Exception e) {
            System.err.println("测试ES数据可用性失败: " + e.getMessage());
        }
    }

    /**
     * 测试无时间限制的查询
     */
    private void testQueryWithoutTimeLimit(ErpDistributionWholesaleStatisticsReqVO reqVO) {
        try {
            System.out.println("=== 测试无时间限制查询 ===");

            // 代发数据无时间限制查询
            BoolQueryBuilder distributionQuery = QueryBuilders.boolQuery();
            NativeSearchQuery testDistributionQuery = new NativeSearchQueryBuilder()
                    .withQuery(distributionQuery)
                    .withPageable(PageRequest.of(0, 10))
                    .build();

            SearchHits<ErpDistributionCombinedESDO> distributionHits = elasticsearchRestTemplate.search(
                    testDistributionQuery, ErpDistributionCombinedESDO.class);
            System.out.println("无时间限制代发数据查询结果: " + distributionHits.getTotalHits());

            // 批发数据无时间限制查询
            BoolQueryBuilder wholesaleQuery = QueryBuilders.boolQuery();
            NativeSearchQuery testWholesaleQuery = new NativeSearchQueryBuilder()
                    .withQuery(wholesaleQuery)
                    .withPageable(PageRequest.of(0, 10))
                    .build();

            SearchHits<ErpWholesaleCombinedESDO> wholesaleHits = elasticsearchRestTemplate.search(
                    testWholesaleQuery, ErpWholesaleCombinedESDO.class);
            System.out.println("无时间限制批发数据查询结果: " + wholesaleHits.getTotalHits());

            // 输出几个样本数据用于调试
            if (distributionHits.getTotalHits() > 0) {
                System.out.println("代发数据样本:");
                distributionHits.getSearchHits().stream().limit(2).forEach(hit -> {
                    ErpDistributionCombinedESDO data = hit.getContent();
                    System.out.println("  ID: " + data.getId() + ", 创建时间: " + data.getCreateTime() +
                                     ", 采购人员: " + data.getPurchaser() + ", 供应商: " + data.getSupplier() +
                                     ", 销售人员: " + data.getSalesperson() + ", 客户: " + data.getCustomerName());
                });
            }

            if (wholesaleHits.getTotalHits() > 0) {
                System.out.println("批发数据样本:");
                wholesaleHits.getSearchHits().stream().limit(2).forEach(hit -> {
                    ErpWholesaleCombinedESDO data = hit.getContent();
                    System.out.println("  ID: " + data.getId() + ", 创建时间: " + data.getCreateTime() +
                                     ", 采购人员: " + data.getPurchaser() + ", 供应商: " + data.getSupplier() +
                                     ", 销售人员: " + data.getSalesperson() + ", 客户: " + data.getCustomerName());
                });
            }

            System.out.println("=== 无时间限制查询测试结束 ===");

        } catch (Exception e) {
            System.err.println("测试无时间限制查询失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getCategoryList(String statisticsType, String keyword) {
        Set<String> categorySet = new HashSet<>();

        try {
            // 构建查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 如果有关键词，添加搜索条件
            if (cn.hutool.core.util.StrUtil.isNotBlank(keyword)) {
                String searchKeyword = keyword.trim();
                switch (statisticsType) {
                    case "purchaser":
                        boolQuery.must(QueryBuilders.wildcardQuery("purchaser_keyword", "*" + searchKeyword + "*"));
                        break;
                    case "supplier":
                        boolQuery.must(QueryBuilders.wildcardQuery("supplier_keyword", "*" + searchKeyword + "*"));
                        break;
                    case "salesperson":
                        boolQuery.must(QueryBuilders.wildcardQuery("salesperson_keyword", "*" + searchKeyword + "*"));
                        break;
                    case "customer":
                        boolQuery.must(QueryBuilders.wildcardQuery("customer_name_keyword", "*" + searchKeyword + "*"));
                        break;
                }
            }

            // 查询代发数据
            NativeSearchQuery distributionQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            SearchHits<ErpDistributionCombinedESDO> distributionHits = elasticsearchRestTemplate.search(
                    distributionQuery, ErpDistributionCombinedESDO.class);

            for (SearchHit<ErpDistributionCombinedESDO> hit : distributionHits) {
                String categoryName = getCategoryName(hit.getContent(), statisticsType);
                if (categoryName != null) {
                    categorySet.add(categoryName);
                }
            }

            // 查询批发数据
            NativeSearchQuery wholesaleQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            SearchHits<ErpWholesaleCombinedESDO> wholesaleHits = elasticsearchRestTemplate.search(
                    wholesaleQuery, ErpWholesaleCombinedESDO.class);

            for (SearchHit<ErpWholesaleCombinedESDO> hit : wholesaleHits) {
                String categoryName = getCategoryName(hit.getContent(), statisticsType);
                if (categoryName != null) {
                    categorySet.add(categoryName);
                }
            }

        } catch (Exception e) {
            System.err.println("获取分类列表失败: " + e.getMessage());
        }

        return categorySet.stream()
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 从ES获取代发数据
     */
    private List<ErpDistributionCombinedESDO> getDistributionDataFromES(ErpDistributionWholesaleStatisticsReqVO reqVO) {
        try {
            // 构建查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 添加调试信息
            System.out.println("=== 代发数据ES查询调试 ===");
            System.out.println("开始时间: " + reqVO.getBeginTime());
            System.out.println("结束时间: " + reqVO.getEndTime());
            System.out.println("统计类型: " + reqVO.getStatisticsType());
            System.out.println("搜索关键词: " + reqVO.getSearchKeyword());

            if (reqVO.getBeginTime() != null && reqVO.getEndTime() != null) {
                // 解析时间字符串为LocalDateTime
                LocalDateTime beginTime = parseTimeString(reqVO.getBeginTime());
                LocalDateTime endTime = parseTimeString(reqVO.getEndTime());

                if (beginTime != null && endTime != null) {
                    System.out.println("原始解析结果 - 开始时间: " + beginTime + ", 结束时间: " + endTime);

                    // 🔥 关键修复：使用字符串格式的时间查询，避免LocalDateTime序列化问题
                    String beginTimeStr = beginTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    String endTimeStr = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    System.out.println("转换为字符串格式 - 开始时间: " + beginTimeStr + ", 结束时间: " + endTimeStr);

                    boolQuery.must(QueryBuilders.rangeQuery("create_time")
                            .gte(beginTimeStr)
                            .lte(endTimeStr));
                    System.out.println("添加了时间范围查询条件: " + beginTimeStr + " 到 " + endTimeStr);
                } else {
                    System.out.println("时间解析失败，跳过时间范围查询");
                }
            }

            // 如果有搜索关键词，根据统计类型添加搜索条件
            if (cn.hutool.core.util.StrUtil.isNotBlank(reqVO.getSearchKeyword())) {
                String keyword = reqVO.getSearchKeyword().trim();
                switch (reqVO.getStatisticsType()) {
                    case "purchaser":
                        boolQuery.must(QueryBuilders.wildcardQuery("purchaser_keyword", "*" + keyword + "*"));
                        break;
                    case "supplier":
                        boolQuery.must(QueryBuilders.wildcardQuery("supplier_keyword", "*" + keyword + "*"));
                        break;
                    case "salesperson":
                        boolQuery.must(QueryBuilders.wildcardQuery("salesperson_keyword", "*" + keyword + "*"));
                        break;
                    case "customer":
                        boolQuery.must(QueryBuilders.wildcardQuery("customer_name_keyword", "*" + keyword + "*"));
                        break;
                }
            }

            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(PageRequest.of(0, 10000)) // 获取大量数据用于统计
                    .build();

            SearchHits<ErpDistributionCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    searchQuery, ErpDistributionCombinedESDO.class);

            System.out.println("代发数据查询结果数量: " + searchHits.getTotalHits());

            List<ErpDistributionCombinedESDO> result = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            System.out.println("实际返回代发数据数量: " + result.size());

            // 输出前几条数据用于调试
            if (!result.isEmpty()) {
                System.out.println("代发数据样本（前3条）:");
                result.stream().limit(3).forEach(data -> {
                    System.out.println("  ID: " + data.getId() + ", 创建时间: " + data.getCreateTime() +
                                     ", 采购人员: " + data.getPurchaser() + ", 供应商: " + data.getSupplier());
                });
            }

            System.out.println("=== 代发数据ES查询调试结束 ===");

            return result;
        } catch (Exception e) {
            System.err.println("从ES获取代发数据失败: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 从ES获取批发数据
     */
    private List<ErpWholesaleCombinedESDO> getWholesaleDataFromES(ErpDistributionWholesaleStatisticsReqVO reqVO) {
        try {
            // 构建查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 添加调试信息
            System.out.println("=== 批发数据ES查询调试 ===");
            System.out.println("开始时间: " + reqVO.getBeginTime());
            System.out.println("结束时间: " + reqVO.getEndTime());

            if (reqVO.getBeginTime() != null && reqVO.getEndTime() != null) {
                // 解析时间字符串为LocalDateTime
                LocalDateTime beginTime = parseTimeString(reqVO.getBeginTime());
                LocalDateTime endTime = parseTimeString(reqVO.getEndTime());

                if (beginTime != null && endTime != null) {
                    System.out.println("原始解析结果 - 开始时间: " + beginTime + ", 结束时间: " + endTime);

                    // 🔥 关键修复：使用字符串格式的时间查询，避免LocalDateTime序列化问题
                    String beginTimeStr = beginTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    String endTimeStr = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    System.out.println("转换为字符串格式 - 开始时间: " + beginTimeStr + ", 结束时间: " + endTimeStr);

                    boolQuery.must(QueryBuilders.rangeQuery("create_time")
                            .gte(beginTimeStr)
                            .lte(endTimeStr));
                    System.out.println("添加了时间范围查询条件: " + beginTimeStr + " 到 " + endTimeStr);
                } else {
                    System.out.println("时间解析失败，跳过时间范围查询");
                }
            }

            // 如果有搜索关键词，根据统计类型添加搜索条件
            if (cn.hutool.core.util.StrUtil.isNotBlank(reqVO.getSearchKeyword())) {
                String keyword = reqVO.getSearchKeyword().trim();
                switch (reqVO.getStatisticsType()) {
                    case "purchaser":
                        boolQuery.must(QueryBuilders.wildcardQuery("purchaser_keyword", "*" + keyword + "*"));
                        break;
                    case "supplier":
                        boolQuery.must(QueryBuilders.wildcardQuery("supplier_keyword", "*" + keyword + "*"));
                        break;
                    case "salesperson":
                        boolQuery.must(QueryBuilders.wildcardQuery("salesperson_keyword", "*" + keyword + "*"));
                        break;
                    case "customer":
                        boolQuery.must(QueryBuilders.wildcardQuery("customer_name_keyword", "*" + keyword + "*"));
                        break;
                }
            }

            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(PageRequest.of(0, 10000)) // 获取大量数据用于统计
                    .build();

            SearchHits<ErpWholesaleCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    searchQuery, ErpWholesaleCombinedESDO.class);

            System.out.println("批发数据查询结果数量: " + searchHits.getTotalHits());

            List<ErpWholesaleCombinedESDO> result = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            System.out.println("实际返回批发数据数量: " + result.size());

            // 输出前几条数据用于调试
            if (!result.isEmpty()) {
                System.out.println("批发数据样本（前3条）:");
                result.stream().limit(3).forEach(data -> {
                    System.out.println("  ID: " + data.getId() + ", 创建时间: " + data.getCreateTime() +
                                     ", 采购人员: " + data.getPurchaser() + ", 供应商: " + data.getSupplier());
                });
            }

            System.out.println("=== 批发数据ES查询调试结束 ===");

            return result;
        } catch (Exception e) {
            System.err.println("从ES获取批发数据失败: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 合并代发和批发统计数据
     */
    private List<ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> mergeStatisticsData(
            List<ErpDistributionCombinedESDO> distributionData,
            List<ErpWholesaleCombinedESDO> wholesaleData,
            String statisticsType) {

        // 创建分类名称到统计项的映射
        Map<String, ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> itemMap = new HashMap<>();

        // 处理代发数据
        processDistributionData(distributionData, itemMap, statisticsType);

        // 处理批发数据
        processWholesaleData(wholesaleData, itemMap, statisticsType);

        // 计算总计并排序
        return itemMap.values().stream()
                .map(this::calculateTotalsAndSetDefaults)
                .sorted((a, b) -> {
                    // 按总采购金额降序排序
                    BigDecimal totalA = a.getTotalPurchaseAmount();
                    BigDecimal totalB = b.getTotalPurchaseAmount();
                    return totalB.compareTo(totalA);
                })
                .collect(Collectors.toList());
    }

    /**
     * 处理代发数据
     */
    private void processDistributionData(List<ErpDistributionCombinedESDO> distributionData,
                                       Map<String, ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> itemMap,
                                       String statisticsType) {

        for (ErpDistributionCombinedESDO distribution : distributionData) {
            String categoryName = getCategoryName(distribution, statisticsType);
            if (categoryName == null) continue;

            ErpDistributionWholesaleStatisticsRespVO.StatisticsItem item = itemMap.computeIfAbsent(categoryName,
                    k -> new ErpDistributionWholesaleStatisticsRespVO.StatisticsItem());

            item.setCategoryName(categoryName);

            // 累加代发订单数
            item.setDistributionOrderCount((item.getDistributionOrderCount() == null ? 0 : item.getDistributionOrderCount()) + 1);

            // 累加代发产品数量
            int productQuantity = distribution.getProductQuantity() != null ? distribution.getProductQuantity() : 0;
            item.setDistributionProductQuantity((item.getDistributionProductQuantity() == null ? 0 : item.getDistributionProductQuantity()) + productQuantity);

            // 计算代发采购和销售金额
            BigDecimal[] amounts = calculateDistributionAmounts(distribution);
            BigDecimal purchaseAmount = amounts[0];
            BigDecimal saleAmount = amounts[1];

            item.setDistributionPurchaseAmount((item.getDistributionPurchaseAmount() == null ? BigDecimal.ZERO : item.getDistributionPurchaseAmount()).add(purchaseAmount));
            item.setDistributionSaleAmount((item.getDistributionSaleAmount() == null ? BigDecimal.ZERO : item.getDistributionSaleAmount()).add(saleAmount));
        }
    }

    /**
     * 处理批发数据
     */
    private void processWholesaleData(List<ErpWholesaleCombinedESDO> wholesaleData,
                                    Map<String, ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> itemMap,
                                    String statisticsType) {

        for (ErpWholesaleCombinedESDO wholesale : wholesaleData) {
            String categoryName = getCategoryName(wholesale, statisticsType);
            if (categoryName == null) continue;

            ErpDistributionWholesaleStatisticsRespVO.StatisticsItem item = itemMap.computeIfAbsent(categoryName,
                    k -> new ErpDistributionWholesaleStatisticsRespVO.StatisticsItem());

            item.setCategoryName(categoryName);

            // 累加批发订单数
            item.setWholesaleOrderCount((item.getWholesaleOrderCount() == null ? 0 : item.getWholesaleOrderCount()) + 1);

            // 累加批发产品数量
            int productQuantity = wholesale.getProductQuantity() != null ? wholesale.getProductQuantity() : 0;
            item.setWholesaleProductQuantity((item.getWholesaleProductQuantity() == null ? 0 : item.getWholesaleProductQuantity()) + productQuantity);

            // 计算批发采购和销售金额
            BigDecimal[] amounts = calculateWholesaleAmounts(wholesale);
            BigDecimal purchaseAmount = amounts[0];
            BigDecimal saleAmount = amounts[1];

            item.setWholesalePurchaseAmount((item.getWholesalePurchaseAmount() == null ? BigDecimal.ZERO : item.getWholesalePurchaseAmount()).add(purchaseAmount));
            item.setWholesaleSaleAmount((item.getWholesaleSaleAmount() == null ? BigDecimal.ZERO : item.getWholesaleSaleAmount()).add(saleAmount));
        }
    }

    /**
     * 根据统计类型获取分类名称
     */
    private String getCategoryName(Object data, String statisticsType) {
        if (data instanceof ErpDistributionCombinedESDO) {
            ErpDistributionCombinedESDO distribution = (ErpDistributionCombinedESDO) data;
            switch (statisticsType) {
                case "purchaser":
                    return distribution.getPurchaser();
                case "supplier":
                    return distribution.getSupplier();
                case "salesperson":
                    return distribution.getSalesperson();
                case "customer":
                    return distribution.getCustomerName();
                default:
                    return null;
            }
        } else if (data instanceof ErpWholesaleCombinedESDO) {
            ErpWholesaleCombinedESDO wholesale = (ErpWholesaleCombinedESDO) data;
            switch (statisticsType) {
                case "purchaser":
                    return wholesale.getPurchaser();
                case "supplier":
                    return wholesale.getSupplier();
                case "salesperson":
                    return wholesale.getSalesperson();
                case "customer":
                    return wholesale.getCustomerName();
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 计算代发订单的采购和销售金额
     */
    private BigDecimal[] calculateDistributionAmounts(ErpDistributionCombinedESDO distribution) {
        BigDecimal purchaseAmount = BigDecimal.ZERO;
        BigDecimal saleAmount = BigDecimal.ZERO;

        if (distribution.getComboProductId() != null) {
            // 从ES获取组品信息
            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(distribution.getComboProductId());
            if (comboProductOpt.isPresent()) {
                ErpComboProductES comboProduct = comboProductOpt.get();
                int quantity = distribution.getProductQuantity() != null ? distribution.getProductQuantity() : 0;

                // 计算采购金额
                BigDecimal productCost = comboProduct.getPurchasePrice().multiply(new BigDecimal(quantity));
                BigDecimal shippingFee = calculateDistributionShippingFee(comboProduct, quantity);
                BigDecimal otherFees = distribution.getPurchaseOtherFees() != null ? distribution.getPurchaseOtherFees() : BigDecimal.ZERO;
                purchaseAmount = productCost.add(shippingFee).add(otherFees);

                // 计算销售金额
                if (distribution.getCustomerName() != null) {
                    Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                            distribution.getComboProductId(), distribution.getCustomerName());
                    if (salePriceOpt.isPresent()) {
                        ErpSalePriceESDO salePrice = salePriceOpt.get();
                        BigDecimal saleProductAmount = salePrice.getDistributionPrice().multiply(new BigDecimal(quantity));
                        BigDecimal saleShippingFee = calculateDistributionSaleShippingFee(salePrice, quantity, comboProduct);
                        BigDecimal saleOtherFees = distribution.getSaleOtherFees() != null ? distribution.getSaleOtherFees() : BigDecimal.ZERO;
                        saleAmount = saleProductAmount.add(saleShippingFee).add(saleOtherFees);
                    }
                }
            }
        }

        return new BigDecimal[]{purchaseAmount, saleAmount};
    }

    /**
     * 计算批发订单的采购和销售金额
     */
    private BigDecimal[] calculateWholesaleAmounts(ErpWholesaleCombinedESDO wholesale) {
        BigDecimal purchaseAmount = BigDecimal.ZERO;
        BigDecimal saleAmount = BigDecimal.ZERO;

        if (wholesale.getComboProductId() != null) {
            // 从ES获取组品信息
            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(wholesale.getComboProductId());
            if (comboProductOpt.isPresent()) {
                ErpComboProductES comboProduct = comboProductOpt.get();
                int quantity = wholesale.getProductQuantity() != null ? wholesale.getProductQuantity() : 0;

                // 🔥 修复：计算采购金额 - 使用组品的批发价格（getWholesalePrice）
                BigDecimal productCost = comboProduct.getWholesalePrice().multiply(new BigDecimal(quantity));
                BigDecimal truckFee = wholesale.getPurchaseTruckFee() != null ? wholesale.getPurchaseTruckFee() : BigDecimal.ZERO;
                BigDecimal logisticsFee = wholesale.getPurchaseLogisticsFee() != null ? wholesale.getPurchaseLogisticsFee() : BigDecimal.ZERO;
                BigDecimal otherFees = wholesale.getPurchaseOtherFees() != null ? wholesale.getPurchaseOtherFees() : BigDecimal.ZERO;
                purchaseAmount = productCost.add(truckFee).add(logisticsFee).add(otherFees);

                // 计算销售金额
                if (wholesale.getCustomerName() != null) {
                    Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                            wholesale.getComboProductId(), wholesale.getCustomerName());
                    if (salePriceOpt.isPresent()) {
                        ErpSalePriceESDO salePrice = salePriceOpt.get();
                        BigDecimal saleProductAmount = salePrice.getWholesalePrice().multiply(new BigDecimal(quantity));
                        BigDecimal saleTruckFee = wholesale.getSaleTruckFee() != null ? wholesale.getSaleTruckFee() : BigDecimal.ZERO;
                        BigDecimal saleLogisticsFee = wholesale.getSaleLogisticsFee() != null ? wholesale.getSaleLogisticsFee() : BigDecimal.ZERO;
                        BigDecimal saleOtherFees = wholesale.getSaleOtherFees() != null ? wholesale.getSaleOtherFees() : BigDecimal.ZERO;
                        saleAmount = saleProductAmount.add(saleTruckFee).add(saleLogisticsFee).add(saleOtherFees);
                    }
                }
            }
        }

        return new BigDecimal[]{purchaseAmount, saleAmount};
    }

    /**
     * 计算代发采购运费
     */
    private BigDecimal calculateDistributionShippingFee(ErpComboProductES comboProduct, Integer quantity) {
        BigDecimal shippingFee = BigDecimal.ZERO;
        switch (comboProduct.getShippingFeeType()) {
            case 0: // 固定运费
                shippingFee = comboProduct.getFixedShippingFee();
                break;
            case 1: // 按件计费
                if (comboProduct.getAdditionalItemQuantity() > 0) {
                    int additionalUnits = (int) Math.ceil((double) quantity / comboProduct.getAdditionalItemQuantity());
                    shippingFee = comboProduct.getAdditionalItemPrice().multiply(new BigDecimal(additionalUnits));
                }
                break;
            case 2: // 按重量计费
                BigDecimal totalWeight = comboProduct.getWeight().multiply(new BigDecimal(quantity));
                if (totalWeight.compareTo(comboProduct.getFirstWeight()) <= 0) {
                    shippingFee = comboProduct.getFirstWeightPrice();
                } else {
                    BigDecimal additionalWeight = totalWeight.subtract(comboProduct.getFirstWeight());
                    BigDecimal additionalUnits = additionalWeight.divide(comboProduct.getAdditionalWeight(), 0, RoundingMode.UP);
                    shippingFee = comboProduct.getFirstWeightPrice().add(
                            comboProduct.getAdditionalWeightPrice().multiply(additionalUnits)
                    );
                }
                break;
        }
        return shippingFee;
    }

    /**
     * 计算代发销售运费
     */
    private BigDecimal calculateDistributionSaleShippingFee(ErpSalePriceESDO salePrice, Integer quantity, ErpComboProductES comboProduct) {
        BigDecimal shippingFee = BigDecimal.ZERO;
        switch (salePrice.getShippingFeeType()) {
            case 0: // 固定运费
                shippingFee = salePrice.getFixedShippingFee();
                break;
            case 1: // 按件计费
                if (salePrice.getAdditionalItemQuantity() > 0) {
                    int additionalUnits = (int) Math.ceil((double) quantity / salePrice.getAdditionalItemQuantity());
                    shippingFee = salePrice.getAdditionalItemPrice().multiply(new BigDecimal(additionalUnits));
                }
                break;
            case 2: // 按重计费
                BigDecimal productWeight = comboProduct.getWeight();
                BigDecimal totalWeight = productWeight.multiply(new BigDecimal(quantity));

                if (totalWeight.compareTo(salePrice.getFirstWeight()) <= 0) {
                    shippingFee = salePrice.getFirstWeightPrice();
                } else {
                    BigDecimal additionalWeight = totalWeight.subtract(salePrice.getFirstWeight());
                    BigDecimal additionalUnits = additionalWeight.divide(salePrice.getAdditionalWeight(), 0, RoundingMode.UP);
                    shippingFee = salePrice.getFirstWeightPrice().add(
                            salePrice.getAdditionalWeightPrice().multiply(additionalUnits)
                    );
                }
                break;
        }
        return shippingFee;
    }

    /**
     * 计算总计并设置默认值
     */
    private ErpDistributionWholesaleStatisticsRespVO.StatisticsItem calculateTotalsAndSetDefaults(
            ErpDistributionWholesaleStatisticsRespVO.StatisticsItem item) {

        // 设置默认值
        if (item.getDistributionOrderCount() == null) item.setDistributionOrderCount(0);
        if (item.getDistributionProductQuantity() == null) item.setDistributionProductQuantity(0);
        if (item.getDistributionPurchaseAmount() == null) item.setDistributionPurchaseAmount(BigDecimal.ZERO);
        if (item.getDistributionSaleAmount() == null) item.setDistributionSaleAmount(BigDecimal.ZERO);

        if (item.getWholesaleOrderCount() == null) item.setWholesaleOrderCount(0);
        if (item.getWholesaleProductQuantity() == null) item.setWholesaleProductQuantity(0);
        if (item.getWholesalePurchaseAmount() == null) item.setWholesalePurchaseAmount(BigDecimal.ZERO);
        if (item.getWholesaleSaleAmount() == null) item.setWholesaleSaleAmount(BigDecimal.ZERO);

        // 计算总计
        item.setTotalOrderCount(item.getDistributionOrderCount() + item.getWholesaleOrderCount());
        item.setTotalProductQuantity(item.getDistributionProductQuantity() + item.getWholesaleProductQuantity());
        item.setTotalPurchaseAmount(item.getDistributionPurchaseAmount().add(item.getWholesalePurchaseAmount()));
        item.setTotalSaleAmount(item.getDistributionSaleAmount().add(item.getWholesaleSaleAmount()));

        return item;
    }

    @Override
    public ErpDistributionWholesaleStatisticsRespVO.DetailStatistics getDetailStatistics(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName) {
        System.out.println("=== 获取详细统计数据 ===");
        System.out.println("统计类型: " + reqVO.getStatisticsType());
        System.out.println("分类名称: " + categoryName);
        System.out.println("时间范围: " + reqVO.getBeginTime() + " 到 " + reqVO.getEndTime());

        ErpDistributionWholesaleStatisticsRespVO.DetailStatistics detail = new ErpDistributionWholesaleStatisticsRespVO.DetailStatistics();
        detail.setCategoryName(categoryName);
        detail.setStatisticsType(reqVO.getStatisticsType());

        // 1. 获取基础统计信息
        System.out.println("1. 获取基础统计信息...");
        ErpDistributionWholesaleStatisticsRespVO.StatisticsItem basicInfo = getStatisticsForCategory(reqVO, categoryName);
        detail.setBasicInfo(basicInfo);

        // 2. 获取趋势数据
        System.out.println("2. 获取趋势数据...");
        List<ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend> monthlyTrends = getMonthlyTrends(reqVO, categoryName);
        detail.setMonthlyTrends(monthlyTrends);
        System.out.println("趋势数据获取完成，共 " + monthlyTrends.size() + " 个时间点");

        // 3. 获取产品分布数据
        System.out.println("3. 获取产品分布数据...");
        List<ErpDistributionWholesaleStatisticsRespVO.ProductDistribution> productDistributions = getProductDistributions(reqVO, categoryName);
        detail.setProductDistributions(productDistributions);
        System.out.println("产品分布数据获取完成，共 " + productDistributions.size() + " 个产品");

        // 4. 计算利润分析
        System.out.println("4. 计算利润分析...");
        ErpDistributionWholesaleStatisticsRespVO.ProfitAnalysis profitAnalysis = calculateProfitAnalysis(basicInfo);
        detail.setProfitAnalysis(profitAnalysis);

        // 5. 不再获取最近订单明细，根据需求移除

        System.out.println("详细统计数据获取完成");
        return detail;
    }

    /**
     * 获取单个分类的统计信息
     */
    private ErpDistributionWholesaleStatisticsRespVO.StatisticsItem getStatisticsForCategory(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName) {
        System.out.println("=== 获取单个分类统计信息 ===");
        System.out.println("统计类型: " + reqVO.getStatisticsType());
        System.out.println("分类名称: " + categoryName);
        System.out.println("时间范围: " + reqVO.getBeginTime() + " 到 " + reqVO.getEndTime());

        // 直接查询ES数据，使用精确的分类筛选
        List<ErpDistributionCombinedESDO> distributionData = getDistributionDataForCategory(reqVO, categoryName);
        List<ErpWholesaleCombinedESDO> wholesaleData = getWholesaleDataForCategory(reqVO, categoryName);

        System.out.println("代发数据查询结果: " + distributionData.size() + " 条记录");
        System.out.println("批发数据查询结果: " + wholesaleData.size() + " 条记录");

        // 合并数据
        List<ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> items = mergeStatisticsData(
                distributionData, wholesaleData, reqVO.getStatisticsType());

        if (!items.isEmpty()) {
            ErpDistributionWholesaleStatisticsRespVO.StatisticsItem result = items.get(0);
            System.out.println("统计结果: 总采购金额=" + result.getTotalPurchaseAmount() +
                             ", 总销售金额=" + result.getTotalSaleAmount());
            return result;
        }

        // 如果没有找到数据，返回空的统计项
        System.out.println("未找到数据，返回空统计项");
        ErpDistributionWholesaleStatisticsRespVO.StatisticsItem item = new ErpDistributionWholesaleStatisticsRespVO.StatisticsItem();
        item.setCategoryName(categoryName);
        item.setDistributionOrderCount(0);
        item.setDistributionProductQuantity(0);
        item.setDistributionPurchaseAmount(BigDecimal.ZERO);
        item.setDistributionSaleAmount(BigDecimal.ZERO);
        item.setWholesaleOrderCount(0);
        item.setWholesaleProductQuantity(0);
        item.setWholesalePurchaseAmount(BigDecimal.ZERO);
        item.setWholesaleSaleAmount(BigDecimal.ZERO);
        item.setTotalOrderCount(0);
        item.setTotalProductQuantity(0);
        item.setTotalPurchaseAmount(BigDecimal.ZERO);
        item.setTotalSaleAmount(BigDecimal.ZERO);
        return item;
    }

    /**
     * 获取指定分类的代发数据
     */
    private List<ErpDistributionCombinedESDO> getDistributionDataForCategory(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName) {
        try {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 添加时间范围查询
            addTimeRangeQuery(boolQuery, reqVO);

            // 添加分类筛选条件
            addCategoryFilter(boolQuery, reqVO.getStatisticsType(), categoryName);

            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(PageRequest.of(0, 10000))
                    .build();

            SearchHits<ErpDistributionCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    searchQuery, ErpDistributionCombinedESDO.class,
                    IndexCoordinates.of("erp_distribution_combined"));

            return searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取指定分类的代发数据失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取指定分类的批发数据
     */
    private List<ErpWholesaleCombinedESDO> getWholesaleDataForCategory(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName) {
        try {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 添加时间范围查询
            addTimeRangeQuery(boolQuery, reqVO);

            // 添加分类筛选条件
            addCategoryFilter(boolQuery, reqVO.getStatisticsType(), categoryName);

            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(PageRequest.of(0, 10000))
                    .build();

            SearchHits<ErpWholesaleCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    searchQuery, ErpWholesaleCombinedESDO.class,
                    IndexCoordinates.of("erp_wholesale_combined"));

            return searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取指定分类的批发数据失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取时间范围内的趋势数据（不再限制为月度，根据用户选择的时间范围动态分组）
     */
    private List<ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend> getMonthlyTrends(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName) {
        List<ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend> trends = new ArrayList<>();

        // 解析时间范围
        LocalDateTime startTime = parseTimeString(reqVO.getBeginTime());
        LocalDateTime endTime = parseTimeString(reqVO.getEndTime());

        if (startTime == null || endTime == null) {
            // 如果没有指定时间范围，默认显示最近12个月
            endTime = LocalDateTime.now();
            startTime = endTime.minusMonths(11).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }

        // 计算时间跨度，决定分组方式
        long daysBetween = java.time.Duration.between(startTime, endTime).toDays();

        if (daysBetween <= 31) {
            // 31天内：按天分组
            trends = getTrendsByDay(reqVO, categoryName, startTime, endTime);
        } else if (daysBetween <= 365) {
            // 1年内：按月分组
            trends = getTrendsByMonth(reqVO, categoryName, startTime, endTime);
        } else {
            // 超过1年：按季度分组
            trends = getTrendsByQuarter(reqVO, categoryName, startTime, endTime);
        }

        return trends;
    }

    /**
     * 按天获取趋势数据
     */
    private List<ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend> getTrendsByDay(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName, LocalDateTime startTime, LocalDateTime endTime) {
        List<ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend> trends = new ArrayList<>();

        LocalDateTime currentDay = startTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        while (!currentDay.isAfter(endTime)) {
            LocalDateTime dayStart = currentDay;
            LocalDateTime dayEnd = currentDay.withHour(23).withMinute(59).withSecond(59);

            ErpDistributionWholesaleStatisticsRespVO.StatisticsItem dayStats = getStatisticsForTimeRange(reqVO, categoryName, dayStart, dayEnd);

            ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend trend = new ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend();
            trend.setMonth(currentDay.format(DateTimeFormatter.ofPattern("MM-dd")));
            setTrendAmounts(trend, dayStats, reqVO.getStatisticsType());

            trends.add(trend);
            currentDay = currentDay.plusDays(1);
        }

        return trends;
    }

    /**
     * 按月获取趋势数据
     */
    private List<ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend> getTrendsByMonth(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName, LocalDateTime startTime, LocalDateTime endTime) {
        List<ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend> trends = new ArrayList<>();

        LocalDateTime currentMonth = startTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        while (!currentMonth.isAfter(endTime)) {
            LocalDateTime monthStart = currentMonth;
            LocalDateTime monthEnd = currentMonth.plusMonths(1).minusNanos(1);
            if (monthEnd.isAfter(endTime)) {
                monthEnd = endTime;
            }

            ErpDistributionWholesaleStatisticsRespVO.StatisticsItem monthStats = getStatisticsForTimeRange(reqVO, categoryName, monthStart, monthEnd);

            ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend trend = new ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend();
            trend.setMonth(currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            setTrendAmounts(trend, monthStats, reqVO.getStatisticsType());

            trends.add(trend);
            currentMonth = currentMonth.plusMonths(1);
        }

        return trends;
    }

    /**
     * 按季度获取趋势数据
     */
    private List<ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend> getTrendsByQuarter(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName, LocalDateTime startTime, LocalDateTime endTime) {
        List<ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend> trends = new ArrayList<>();

        // 计算开始季度
        LocalDateTime currentQuarter = startTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        int startMonth = ((currentQuarter.getMonthValue() - 1) / 3) * 3 + 1;
        currentQuarter = currentQuarter.withMonth(startMonth);

        while (!currentQuarter.isAfter(endTime)) {
            LocalDateTime quarterStart = currentQuarter;
            LocalDateTime quarterEnd = currentQuarter.plusMonths(3).minusNanos(1);
            if (quarterEnd.isAfter(endTime)) {
                quarterEnd = endTime;
            }

            ErpDistributionWholesaleStatisticsRespVO.StatisticsItem quarterStats = getStatisticsForTimeRange(reqVO, categoryName, quarterStart, quarterEnd);

            ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend trend = new ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend();
            int quarter = (currentQuarter.getMonthValue() - 1) / 3 + 1;
            trend.setMonth(currentQuarter.getYear() + "-Q" + quarter);
            setTrendAmounts(trend, quarterStats, reqVO.getStatisticsType());

            trends.add(trend);
            currentQuarter = currentQuarter.plusMonths(3);
        }

        return trends;
    }

    /**
     * 设置趋势数据的金额，根据统计类型决定显示采购金额还是销售金额
     */
    private void setTrendAmounts(ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend trend,
                                ErpDistributionWholesaleStatisticsRespVO.StatisticsItem stats,
                                String statisticsType) {
        trend.setDistributionOrderCount(stats.getDistributionOrderCount());
        trend.setWholesaleOrderCount(stats.getWholesaleOrderCount());

        // 根据统计类型决定显示采购金额还是销售金额
        if ("purchaser".equals(statisticsType) || "supplier".equals(statisticsType)) {
            trend.setDistributionAmount(stats.getDistributionPurchaseAmount());
            trend.setWholesaleAmount(stats.getWholesalePurchaseAmount());
            System.out.println("  设置采购金额 - 代发: " + stats.getDistributionPurchaseAmount() + ", 批发: " + stats.getWholesalePurchaseAmount());
        } else {
            trend.setDistributionAmount(stats.getDistributionSaleAmount());
            trend.setWholesaleAmount(stats.getWholesaleSaleAmount());
            System.out.println("  设置销售金额 - 代发: " + stats.getDistributionSaleAmount() + ", 批发: " + stats.getWholesaleSaleAmount());
        }
    }

    /**
     * 获取指定时间范围内的统计数据
     */
    private ErpDistributionWholesaleStatisticsRespVO.StatisticsItem getStatisticsForTimeRange(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName, LocalDateTime startTime, LocalDateTime endTime) {
        ErpDistributionWholesaleStatisticsReqVO timeRangeReqVO = new ErpDistributionWholesaleStatisticsReqVO();
        timeRangeReqVO.setStatisticsType(reqVO.getStatisticsType());
        timeRangeReqVO.setBeginTime(startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeRangeReqVO.setEndTime(endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeRangeReqVO.setSearchKeyword(categoryName);

        return getStatisticsForCategory(timeRangeReqVO, categoryName);
    }

    /**
     * 获取产品分布数据
     */
    private List<ErpDistributionWholesaleStatisticsRespVO.ProductDistribution> getProductDistributions(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName) {
        List<ErpDistributionWholesaleStatisticsRespVO.ProductDistribution> distributions = new ArrayList<>();

        try {

            // 构建代发数据查询条件
            BoolQueryBuilder distributionBoolQuery = QueryBuilders.boolQuery();
            addTimeRangeQuery(distributionBoolQuery, reqVO);
            addCategoryFilter(distributionBoolQuery, reqVO.getStatisticsType(), categoryName);

            // 构建批发数据查询条件
            BoolQueryBuilder wholesaleBoolQuery = QueryBuilders.boolQuery();
            addTimeRangeQuery(wholesaleBoolQuery, reqVO);
            addCategoryFilter(wholesaleBoolQuery, reqVO.getStatisticsType(), categoryName);

            // 代发数据查询
            NativeSearchQuery distributionQuery = new NativeSearchQueryBuilder()
                    .withQuery(distributionBoolQuery)
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            SearchHits<ErpDistributionCombinedESDO> distributionHits = elasticsearchRestTemplate.search(
                    distributionQuery,
                    ErpDistributionCombinedESDO.class,
                    IndexCoordinates.of("erp_distribution_combined"));

            // 批发数据查询 - 使用明确的索引名称
            SearchHits<ErpWholesaleCombinedESDO> wholesaleHits;
            try {
                NativeSearchQuery wholesaleQuery = new NativeSearchQueryBuilder()
                        .withQuery(wholesaleBoolQuery)
                        .withPageable(PageRequest.of(0, 1000))
                        .build();

                wholesaleHits = elasticsearchRestTemplate.search(
                        wholesaleQuery,
                        ErpWholesaleCombinedESDO.class,
                        IndexCoordinates.of("erp_wholesale_combined"));
            } catch (Exception e) {
                // 如果批发表查询失败，跳过批发数据处理，将wholesaleHits设为null
                wholesaleHits = null;
            }

            // 按产品名称分组统计
            Map<String, ErpDistributionWholesaleStatisticsRespVO.ProductDistribution> productMap = new HashMap<>();

            // 处理代发数据
            for (SearchHit<ErpDistributionCombinedESDO> hit : distributionHits) {
                ErpDistributionCombinedESDO distribution = hit.getContent();
                String productName = distribution.getProductName();
                if (productName == null) productName = "未知产品";

                ErpDistributionWholesaleStatisticsRespVO.ProductDistribution product = productMap.computeIfAbsent(productName,
                    k -> {
                        ErpDistributionWholesaleStatisticsRespVO.ProductDistribution p = new ErpDistributionWholesaleStatisticsRespVO.ProductDistribution();
                        p.setProductName(k);
                        p.setOrderCount(0);
                        p.setProductQuantity(0);
                        p.setSaleAmount(BigDecimal.ZERO);
                        return p;
                    });

                product.setOrderCount(product.getOrderCount() + 1);
                product.setProductQuantity(product.getProductQuantity() + (distribution.getProductQuantity() != null ? distribution.getProductQuantity() : 0));

                // 根据统计类型决定显示采购金额还是销售金额
                BigDecimal[] amounts = calculateDistributionAmounts(distribution);
                BigDecimal targetAmount;
                if ("purchaser".equals(reqVO.getStatisticsType()) || "supplier".equals(reqVO.getStatisticsType())) {
                    targetAmount = amounts[0]; // 采购金额
                } else {
                    targetAmount = amounts[1]; // 销售金额
                }
                product.setSaleAmount(product.getSaleAmount().add(targetAmount));
            }

            // 处理批发数据（如果查询成功）
            if (wholesaleHits != null) {
                for (SearchHit<ErpWholesaleCombinedESDO> hit : wholesaleHits) {
                    ErpWholesaleCombinedESDO wholesale = hit.getContent();
                    String productName = wholesale.getProductName();
                    if (productName == null) productName = "未知产品";

                    ErpDistributionWholesaleStatisticsRespVO.ProductDistribution product = productMap.computeIfAbsent(productName,
                        k -> {
                            ErpDistributionWholesaleStatisticsRespVO.ProductDistribution p = new ErpDistributionWholesaleStatisticsRespVO.ProductDistribution();
                            p.setProductName(k);
                            p.setOrderCount(0);
                            p.setProductQuantity(0);
                            p.setSaleAmount(BigDecimal.ZERO);
                            return p;
                        });

                    product.setOrderCount(product.getOrderCount() + 1);
                    product.setProductQuantity(product.getProductQuantity() + (wholesale.getProductQuantity() != null ? wholesale.getProductQuantity() : 0));

                    // 根据统计类型决定显示采购金额还是销售金额
                    BigDecimal[] amounts = calculateWholesaleAmounts(wholesale);
                    BigDecimal targetAmount;
                    if ("purchaser".equals(reqVO.getStatisticsType()) || "supplier".equals(reqVO.getStatisticsType())) {
                        targetAmount = amounts[0]; // 采购金额
                    } else {
                        targetAmount = amounts[1]; // 销售金额
                    }
                    product.setSaleAmount(product.getSaleAmount().add(targetAmount));
                }
            }

            // 转换为列表并按金额排序（可能是采购金额或销售金额，取决于统计类型）
            distributions = productMap.values().stream()
                    .sorted((a, b) -> b.getSaleAmount().compareTo(a.getSaleAmount()))
                    .limit(10) // 只取前10个产品
                    .collect(Collectors.toList());

        } catch (Exception e) {
            // 查询失败时返回空列表
        }

        return distributions;
    }

    /**
     * 计算利润分析
     */
    private ErpDistributionWholesaleStatisticsRespVO.ProfitAnalysis calculateProfitAnalysis(ErpDistributionWholesaleStatisticsRespVO.StatisticsItem basicInfo) {
        ErpDistributionWholesaleStatisticsRespVO.ProfitAnalysis analysis = new ErpDistributionWholesaleStatisticsRespVO.ProfitAnalysis();

        analysis.setTotalPurchaseCost(basicInfo.getTotalPurchaseAmount());
        analysis.setTotalSaleRevenue(basicInfo.getTotalSaleAmount());

        BigDecimal totalProfit = basicInfo.getTotalSaleAmount().subtract(basicInfo.getTotalPurchaseAmount());
        analysis.setTotalProfit(totalProfit);

        // 计算利润率
        if (basicInfo.getTotalSaleAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profitRate = totalProfit.divide(basicInfo.getTotalSaleAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            analysis.setProfitRate(profitRate);
        } else {
            analysis.setProfitRate(BigDecimal.ZERO);
        }

        // 计算代发利润
        BigDecimal distributionProfit = basicInfo.getDistributionSaleAmount().subtract(basicInfo.getDistributionPurchaseAmount());
        analysis.setDistributionProfit(distributionProfit);

        // 计算批发利润
        BigDecimal wholesaleProfit = basicInfo.getWholesaleSaleAmount().subtract(basicInfo.getWholesalePurchaseAmount());
        analysis.setWholesaleProfit(wholesaleProfit);

        return analysis;
    }

    // 移除getRecentOrders方法，根据需求不再需要最近订单明细



    /**
     * 添加时间范围查询条件
     */
    private void addTimeRangeQuery(BoolQueryBuilder boolQuery, ErpDistributionWholesaleStatisticsReqVO reqVO) {
        if (reqVO.getBeginTime() != null && reqVO.getEndTime() != null) {
            LocalDateTime beginTime = parseTimeString(reqVO.getBeginTime());
            LocalDateTime endTime = parseTimeString(reqVO.getEndTime());
            if (beginTime != null && endTime != null) {
                // 使用与主查询相同的时间处理逻辑
                if (beginTime.getHour() == 0 && beginTime.getMinute() == 0 && beginTime.getSecond() == 0) {
                    beginTime = beginTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
                }
                if (endTime.getHour() == 23 && endTime.getMinute() == 59 && endTime.getSecond() == 59) {
                    endTime = endTime.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
                }

                // 格式化为ES期望的格式（不包含毫秒和时区）
                DateTimeFormatter esFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                String beginTimeStr = beginTime.format(esFormatter);
                String endTimeStr = endTime.format(esFormatter);

                boolQuery.must(QueryBuilders.rangeQuery("create_time")
                        .gte(beginTimeStr)
                        .lte(endTimeStr));
            }
        }
    }

    /**
     * 添加分类筛选条件
     */
    private void addCategoryFilter(BoolQueryBuilder boolQuery, String statisticsType, String categoryName) {
        switch (statisticsType) {
            case "purchaser":
                boolQuery.must(QueryBuilders.termQuery("purchaser_keyword", categoryName));
                break;
            case "supplier":
                boolQuery.must(QueryBuilders.termQuery("supplier_keyword", categoryName));
                break;
            case "salesperson":
                boolQuery.must(QueryBuilders.termQuery("salesperson_keyword", categoryName));
                break;
            case "customer":
                boolQuery.must(QueryBuilders.termQuery("customer_name_keyword", categoryName));
                break;
        }
    }

    /**
     * 解析时间字符串为LocalDateTime
     */
    private LocalDateTime parseTimeString(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }

        try {
            // 先检查是否为时间戳格式
            try {
                long timestamp = Long.parseLong(timeStr);
                // 判断是秒级还是毫秒级时间戳
                if (timeStr.length() <= 10) { // 秒级
                    return LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(timestamp), java.time.ZoneId.systemDefault());
                } else { // 毫秒级
                    return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneId.systemDefault());
                }
            } catch (NumberFormatException e) {
                // 如果不是时间戳，继续原有解析逻辑
            }

            // 尝试解析 yyyy-MM-dd HH:mm:ss 格式
            return LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e1) {
            try {
                // 尝试解析 yyyy-MM-dd'T'HH:mm:ss 格式
                return LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } catch (DateTimeParseException e2) {
                try {
                    // 尝试解析 yyyy-MM-dd'T'HH:mm 格式 (没有秒)
                    LocalDateTime result = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                    System.out.println("使用 yyyy-MM-dd'T'HH:mm 格式解析成功: " + result);
                    return result;
                } catch (DateTimeParseException e3) {
                    try {
                        // 尝试解析 yyyy-MM-dd 格式（只有日期）
                        java.time.LocalDate date = java.time.LocalDate.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        LocalDateTime result = date.atStartOfDay();
                        System.out.println("使用 yyyy-MM-dd 格式解析成功: " + result);
                        return result;
                    } catch (DateTimeParseException e4) {
                        try {
                            // 尝试解析带时区的ISO 8601格式（如2025-05-21T05:52:26.000Z）
                            java.time.OffsetDateTime offsetDateTime = java.time.OffsetDateTime.parse(timeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            LocalDateTime result = offsetDateTime.atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime();
                            System.out.println("使用 ISO_OFFSET_DATE_TIME 格式解析成功: " + result);
                            return result;
                        } catch (DateTimeParseException e5) {
                            try {
                                // 尝试解析 ISO 格式
                                LocalDateTime result = LocalDateTime.parse(timeStr);
                                System.out.println("使用 ISO 格式解析成功: " + result);
                                return result;
                            } catch (DateTimeParseException e6) {
                                System.err.println("无法解析时间字符串: " + timeStr + ", 错误: " + e6.getMessage());
                                return null;
                            }
                        }
                    }
                }
            }
        }
    }

}
