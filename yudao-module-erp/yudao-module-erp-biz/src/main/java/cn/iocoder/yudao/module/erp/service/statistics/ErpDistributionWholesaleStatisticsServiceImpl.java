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
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDate;

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
        long startTime = System.currentTimeMillis();
        System.out.println("=== 开始代发批发统计查询(优化版) ===");
        System.out.println("请求参数: " + reqVO);
        
        ErpDistributionWholesaleStatisticsRespVO respVO = new ErpDistributionWholesaleStatisticsRespVO();
        respVO.setStatisticsType(reqVO.getStatisticsType());

        // 使用ES聚合查询直接获取统计结果
        List<ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> items = getAggregatedStatisticsData(reqVO);
        respVO.setItems(items);

        long endTime = System.currentTimeMillis();
        System.out.println("最终统计项数量: " + items.size());
        System.out.println("统计查询耗时: " + (endTime - startTime) + "ms");
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
                    // 🔥 修复：移除对已删除字段的调用，改为实时获取
                    String purchaser = getRealTimePurchaser(data.getComboProductId());
                    String supplier = getRealTimeSupplier(data.getComboProductId());
                    System.out.println("  ID: " + data.getId() + ", 创建时间: " + data.getCreateTime() +
                                     ", 采购人员: " + purchaser + ", 供应商: " + supplier +
                                     ", 销售人员: " + data.getSalesperson() + ", 客户: " + data.getCustomerName());
                });
            }

            if (wholesaleHits.getTotalHits() > 0) {
                System.out.println("批发数据样本:");
                wholesaleHits.getSearchHits().stream().limit(2).forEach(hit -> {
                    ErpWholesaleCombinedESDO data = hit.getContent();
                    // 🔥 修复：从组品ES中实时获取采购人员和供应商信息
                    String purchaser = getRealTimePurchaser(data.getComboProductId());
                    String supplier = getRealTimeSupplier(data.getComboProductId());
                    System.out.println("  ID: " + data.getId() + ", 创建时间: " + data.getCreateTime() +
                                     ", 采购人员: " + purchaser + ", 供应商: " + supplier +
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
                        // 🔥 修复：代发表不再有purchaser字段，需要从组品表查询
                        // 先查询符合条件的组品ID，再查询代发表
                        Set<Long> comboProductIds = getComboProductIdsByPurchaser(searchKeyword);
                        if (!comboProductIds.isEmpty()) {
                            boolQuery.must(QueryBuilders.termsQuery("combo_product_id", comboProductIds));
                        } else {
                            // 如果没有找到符合条件的组品，返回空结果
                            return new ArrayList<>();
                        }
                        break;
                    case "supplier":
                        // 🔥 修复：代发表不再有supplier字段，需要从组品表查询
                        Set<Long> supplierComboProductIds = getComboProductIdsBySupplier(searchKeyword);
                        if (!supplierComboProductIds.isEmpty()) {
                            boolQuery.must(QueryBuilders.termsQuery("combo_product_id", supplierComboProductIds));
                        } else {
                            // 如果没有找到符合条件的组品，返回空结果
                            return new ArrayList<>();
                        }
                        break;
                    case "salesperson":
                        boolQuery.must(QueryBuilders.wildcardQuery("salesperson", "*" + searchKeyword + "*"));
                        break;
                    case "customer":
                        boolQuery.must(QueryBuilders.wildcardQuery("customer_name", "*" + searchKeyword + "*"));
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
     * 🔥 根据采购人员关键词查询组品ID集合
     */
    private Set<Long> getComboProductIdsByPurchaser(String purchaserKeyword) {
        Set<Long> comboProductIds = new HashSet<>();
        try {
            BoolQueryBuilder comboQuery = QueryBuilders.boolQuery();
            comboQuery.must(QueryBuilders.wildcardQuery("purchaser", "*" + purchaserKeyword + "*"));
            
            NativeSearchQuery comboSearchQuery = new NativeSearchQueryBuilder()
                    .withQuery(comboQuery)
                    .withPageable(PageRequest.of(0, 10000))
                    .withSourceFilter(new org.springframework.data.elasticsearch.core.query.FetchSourceFilter(new String[]{"id"}, null))
                    .build();
            
            SearchHits<ErpComboProductES> comboHits = elasticsearchRestTemplate.search(
                    comboSearchQuery,
                    ErpComboProductES.class);
            
            comboProductIds = comboHits.stream()
                    .map(hit -> hit.getContent().getId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.err.println("根据采购人员查询组品ID失败: " + e.getMessage());
        }
        return comboProductIds;
    }

    /**
     * 🔥 根据供应商关键词查询组品ID集合
     */
    private Set<Long> getComboProductIdsBySupplier(String supplierKeyword) {
        Set<Long> comboProductIds = new HashSet<>();
        try {
            BoolQueryBuilder comboQuery = QueryBuilders.boolQuery();
            comboQuery.must(QueryBuilders.wildcardQuery("supplier", "*" + supplierKeyword + "*"));
            
            NativeSearchQuery comboSearchQuery = new NativeSearchQueryBuilder()
                    .withQuery(comboQuery)
                    .withPageable(PageRequest.of(0, 10000))
                    .withSourceFilter(new org.springframework.data.elasticsearch.core.query.FetchSourceFilter(new String[]{"id"}, null))
                    .build();
            
            SearchHits<ErpComboProductES> comboHits = elasticsearchRestTemplate.search(
                    comboSearchQuery,
                    ErpComboProductES.class);
            
            comboProductIds = comboHits.stream()
                    .map(hit -> hit.getContent().getId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.err.println("根据供应商查询组品ID失败: " + e.getMessage());
        }
        return comboProductIds;
    }

    /**
     * 🔥 实时获取采购人员信息
     */
    private String getRealTimePurchaser(Long comboProductId) {
        if (comboProductId == null) {
            return null;
        }
        try {
            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(comboProductId);
            return comboProductOpt.map(ErpComboProductES::getPurchaser).orElse(null);
        } catch (Exception e) {
            System.err.println("实时获取采购人员信息失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 🔥 实时获取供应商信息
     */
    private String getRealTimeSupplier(Long comboProductId) {
        if (comboProductId == null) {
            return null;
        }
        try {
            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(comboProductId);
            return comboProductOpt.map(ErpComboProductES::getSupplier).orElse(null);
        } catch (Exception e) {
            System.err.println("实时获取供应商信息失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 🔥 实时获取产品名称信息
     */
    private String getRealTimeProductName(Long comboProductId) {
        if (comboProductId == null) {
            return null;
        }
        try {
            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(comboProductId);
            return comboProductOpt.map(ErpComboProductES::getName).orElse(null);
        } catch (Exception e) {
            System.err.println("实时获取产品名称信息失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public ErpDistributionWholesaleStatisticsRespVO.AuditStatistics getAuditStatistics(ErpDistributionWholesaleStatisticsReqVO reqVO) {
        ErpDistributionWholesaleStatisticsRespVO.AuditStatistics auditStatistics = new ErpDistributionWholesaleStatisticsRespVO.AuditStatistics();

        try {
            // 获取代发数据
            List<ErpDistributionCombinedESDO> distributionData = getDistributionDataFromES(reqVO);
            
            // 获取批发数据
            List<ErpWholesaleCombinedESDO> wholesaleData = getWholesaleDataFromES(reqVO);

            // 统计代发数据
            for (ErpDistributionCombinedESDO distribution : distributionData) {
                // 代发采购审核状态统计
                Integer purchaseAuditStatus = distribution.getPurchaseAuditStatus();
                if (purchaseAuditStatus != null) {
                    if (purchaseAuditStatus == 10) { // 未审核
                        auditStatistics.setDistributionPurchaseUnauditedCount(
                            auditStatistics.getDistributionPurchaseUnauditedCount() + 1);
                    } else if (purchaseAuditStatus == 20) { // 已审核
                        auditStatistics.setDistributionPurchaseAuditedCount(
                            auditStatistics.getDistributionPurchaseAuditedCount() + 1);
                    }
                }

                // 代发采购售后状态统计
                Integer purchaseAfterSalesStatus = distribution.getPurchaseAfterSalesStatus();
                if (purchaseAfterSalesStatus != null) {
                    if (purchaseAfterSalesStatus == 30) { // 未售后
                        auditStatistics.setDistributionPurchaseNoAfterSalesCount(
                            auditStatistics.getDistributionPurchaseNoAfterSalesCount() + 1);
                    } else if (purchaseAfterSalesStatus == 40) { // 已售后
                        auditStatistics.setDistributionPurchaseAfterSalesCount(
                            auditStatistics.getDistributionPurchaseAfterSalesCount() + 1);
                    }
                }

                // 代发销售审核状态统计
                Integer saleAuditStatus = distribution.getSaleAuditStatus();
                if (saleAuditStatus != null) {
                    if (saleAuditStatus == 10) { // 未审核
                        auditStatistics.setDistributionSaleUnauditedCount(
                            auditStatistics.getDistributionSaleUnauditedCount() + 1);
                    } else if (saleAuditStatus == 20) { // 已审核
                        auditStatistics.setDistributionSaleAuditedCount(
                            auditStatistics.getDistributionSaleAuditedCount() + 1);
                    }
                }

                // 代发销售售后状态统计
                Integer saleAfterSalesStatus = distribution.getSaleAfterSalesStatus();
                if (saleAfterSalesStatus != null) {
                    if (saleAfterSalesStatus == 30) { // 未售后
                        auditStatistics.setDistributionSaleNoAfterSalesCount(
                            auditStatistics.getDistributionSaleNoAfterSalesCount() + 1);
                    } else if (saleAfterSalesStatus == 40) { // 已售后
                        auditStatistics.setDistributionSaleAfterSalesCount(
                            auditStatistics.getDistributionSaleAfterSalesCount() + 1);
                    }
                }
            }

            // 统计批发数据
            for (ErpWholesaleCombinedESDO wholesale : wholesaleData) {
                // 批发采购审核状态统计
                Integer purchaseAuditStatus = wholesale.getPurchaseAuditStatus();
                if (purchaseAuditStatus != null) {
                    if (purchaseAuditStatus == 10) { // 未审核
                        auditStatistics.setWholesalePurchaseUnauditedCount(
                            auditStatistics.getWholesalePurchaseUnauditedCount() + 1);
                    } else if (purchaseAuditStatus == 20) { // 已审核
                        auditStatistics.setWholesalePurchaseAuditedCount(
                            auditStatistics.getWholesalePurchaseAuditedCount() + 1);
                    }
                }

                // 批发采购售后状态统计
                Integer purchaseAfterSalesStatus = wholesale.getPurchaseAfterSalesStatus();
                if (purchaseAfterSalesStatus != null) {
                    if (purchaseAfterSalesStatus == 30) { // 未售后
                        auditStatistics.setWholesalePurchaseNoAfterSalesCount(
                            auditStatistics.getWholesalePurchaseNoAfterSalesCount() + 1);
                    } else if (purchaseAfterSalesStatus == 40) { // 已售后
                        auditStatistics.setWholesalePurchaseAfterSalesCount(
                            auditStatistics.getWholesalePurchaseAfterSalesCount() + 1);
                    }
                }

                // 批发销售审核状态统计
                Integer saleAuditStatus = wholesale.getSaleAuditStatus();
                if (saleAuditStatus != null) {
                    if (saleAuditStatus == 10) { // 未审核
                        auditStatistics.setWholesaleSaleUnauditedCount(
                            auditStatistics.getWholesaleSaleUnauditedCount() + 1);
                    } else if (saleAuditStatus == 20) { // 已审核
                        auditStatistics.setWholesaleSaleAuditedCount(
                            auditStatistics.getWholesaleSaleAuditedCount() + 1);
                    }
                }

                // 批发销售售后状态统计
                Integer saleAfterSalesStatus = wholesale.getSaleAfterSalesStatus();
                if (saleAfterSalesStatus != null) {
                    if (saleAfterSalesStatus == 30) { // 未售后
                        auditStatistics.setWholesaleSaleNoAfterSalesCount(
                            auditStatistics.getWholesaleSaleNoAfterSalesCount() + 1);
                    } else if (saleAfterSalesStatus == 40) { // 已售后
                        auditStatistics.setWholesaleSaleAfterSalesCount(
                            auditStatistics.getWholesaleSaleAfterSalesCount() + 1);
                    }
                }
            }

            // 计算总数
            auditStatistics.setDistributionPurchaseTotalCount(
                auditStatistics.getDistributionPurchaseUnauditedCount() + 
                auditStatistics.getDistributionPurchaseAuditedCount());
            
            auditStatistics.setDistributionSaleTotalCount(
                auditStatistics.getDistributionSaleUnauditedCount() + 
                auditStatistics.getDistributionSaleAuditedCount());
            
            auditStatistics.setWholesalePurchaseTotalCount(
                auditStatistics.getWholesalePurchaseUnauditedCount() + 
                auditStatistics.getWholesalePurchaseAuditedCount());
            
            auditStatistics.setWholesaleSaleTotalCount(
                auditStatistics.getWholesaleSaleUnauditedCount() + 
                auditStatistics.getWholesaleSaleAuditedCount());

        } catch (Exception e) {
            System.err.println("获取审核统计数据失败: " + e.getMessage());
            e.printStackTrace();
        }

        return auditStatistics;
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
                        // 🔥 修复：代发表不再有purchaser字段，需要从组品表查询
                        Set<Long> comboProductIds = getComboProductIdsByPurchaser(keyword);
                        if (!comboProductIds.isEmpty()) {
                            boolQuery.must(QueryBuilders.termsQuery("combo_product_id", comboProductIds));
                        } else {
                            // 如果没有找到符合条件的组品，添加一个不可能的条件来返回空结果
                            boolQuery.must(QueryBuilders.termQuery("id", -1L));
                        }
                        break;
                    case "supplier":
                        // 🔥 修复：代发表不再有supplier字段，需要从组品表查询
                        Set<Long> supplierComboProductIds = getComboProductIdsBySupplier(keyword);
                        if (!supplierComboProductIds.isEmpty()) {
                            boolQuery.must(QueryBuilders.termsQuery("combo_product_id", supplierComboProductIds));
                        } else {
                            // 如果没有找到符合条件的组品，添加一个不可能的条件来返回空结果
                            boolQuery.must(QueryBuilders.termQuery("id", -1L));
                        }
                        break;
                    case "salesperson":
                        boolQuery.must(QueryBuilders.wildcardQuery("salesperson", "*" + keyword + "*"));
                        break;
                    case "customer":
                        boolQuery.must(QueryBuilders.wildcardQuery("customer_name", "*" + keyword + "*"));
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
                    // 🔥 修复：移除对已删除字段的调用，改为实时获取
                    String purchaser = getRealTimePurchaser(data.getComboProductId());
                    String supplier = getRealTimeSupplier(data.getComboProductId());
                    System.out.println("  ID: " + data.getId() + ", 创建时间: " + data.getCreateTime() +
                                     ", 采购人员: " + purchaser + ", 供应商: " + supplier);
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
                    // 🔥 修复：从组品ES中实时获取采购人员和供应商信息
                    String purchaser = getRealTimePurchaser(data.getComboProductId());
                    String supplier = getRealTimeSupplier(data.getComboProductId());
                    System.out.println("  ID: " + data.getId() + ", 创建时间: " + data.getCreateTime() +
                                     ", 采购人员: " + purchaser + ", 供应商: " + supplier);
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
                    // 🔥 修复：实时从组品表获取采购人员信息
                    return getRealTimePurchaser(distribution.getComboProductId());
                case "supplier":
                    // 🔥 修复：实时从组品表获取供应商信息
                    return getRealTimeSupplier(distribution.getComboProductId());
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
                    // 🔥 修复：实时从组品表获取采购人员信息
                    return getRealTimePurchaser(wholesale.getComboProductId());
                case "supplier":
                    // 🔥 修复：实时从组品表获取供应商信息
                    return getRealTimeSupplier(wholesale.getComboProductId());
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

                // 🔥 修复：添加空值检查，避免NullPointerException
                BigDecimal purchasePrice = comboProduct.getPurchasePrice() != null ? comboProduct.getPurchasePrice() : BigDecimal.ZERO;
                BigDecimal productCost = purchasePrice.multiply(new BigDecimal(quantity));
                BigDecimal shippingFee = calculateDistributionShippingFee(comboProduct, quantity);
                BigDecimal otherFees = distribution.getPurchaseOtherFees() != null ? distribution.getPurchaseOtherFees() : BigDecimal.ZERO;
                purchaseAmount = productCost.add(shippingFee).add(otherFees);

                // 计算销售金额
                if (distribution.getCustomerName() != null) {
                    Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                            distribution.getComboProductId(), distribution.getCustomerName());
                    if (salePriceOpt.isPresent()) {
                        ErpSalePriceESDO salePrice = salePriceOpt.get();
                        // 🔥 修复：添加空值检查
                        BigDecimal distributionPrice = salePrice.getDistributionPrice() != null ? salePrice.getDistributionPrice() : BigDecimal.ZERO;
                        BigDecimal saleProductAmount = distributionPrice.multiply(new BigDecimal(quantity));
                        BigDecimal saleShippingFee = calculateDistributionSaleShippingFee(salePrice, quantity, comboProduct);
                        BigDecimal saleOtherFees = distribution.getSaleOtherFees() != null ? distribution.getSaleOtherFees() : BigDecimal.ZERO;
                        saleAmount = saleProductAmount.add(saleShippingFee).add(saleOtherFees);
                    } else {
                        // 🔥 修复：销售价格表没有数据时，也能计算销售金额，销售价格字段设置为0
                        BigDecimal saleProductAmount = BigDecimal.ZERO; // 销售价格为0
                        BigDecimal saleShippingFee = BigDecimal.ZERO; // 运费为0
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

                // 🔥 修复：添加空值检查，避免NullPointerException
                BigDecimal wholesalePrice = comboProduct.getWholesalePrice() != null ? comboProduct.getWholesalePrice() : BigDecimal.ZERO;
                BigDecimal productCost = wholesalePrice.multiply(new BigDecimal(quantity));
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
                        // 🔥 修复：添加空值检查
                        BigDecimal saleWholesalePrice = salePrice.getWholesalePrice() != null ? salePrice.getWholesalePrice() : BigDecimal.ZERO;
                        BigDecimal saleProductAmount = saleWholesalePrice.multiply(new BigDecimal(quantity));
                        BigDecimal saleTruckFee = wholesale.getSaleTruckFee() != null ? wholesale.getSaleTruckFee() : BigDecimal.ZERO;
                        BigDecimal saleLogisticsFee = wholesale.getSaleLogisticsFee() != null ? wholesale.getSaleLogisticsFee() : BigDecimal.ZERO;
                        BigDecimal saleOtherFees = wholesale.getSaleOtherFees() != null ? wholesale.getSaleOtherFees() : BigDecimal.ZERO;
                        saleAmount = saleProductAmount.add(saleTruckFee).add(saleLogisticsFee).add(saleOtherFees);
                    } else {
                        // 🔥 修复：销售价格表没有数据时，也能计算销售金额，销售价格字段设置为0
                        BigDecimal saleProductAmount = BigDecimal.ZERO; // 销售价格为0
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
                shippingFee = comboProduct.getFixedShippingFee() != null ? comboProduct.getFixedShippingFee() : BigDecimal.ZERO;
                break;
            case 1: // 按件计费
                if (comboProduct.getAdditionalItemQuantity() > 0) {
                    int additionalUnits = (int) Math.ceil((double) quantity / comboProduct.getAdditionalItemQuantity());
                    BigDecimal additionalItemPrice = comboProduct.getAdditionalItemPrice() != null ? comboProduct.getAdditionalItemPrice() : BigDecimal.ZERO;
                    shippingFee = additionalItemPrice.multiply(new BigDecimal(additionalUnits));
                }
                break;
            case 2: // 按重量计费
                BigDecimal weight = comboProduct.getWeight() != null ? comboProduct.getWeight() : BigDecimal.ZERO;
                BigDecimal totalWeight = weight.multiply(new BigDecimal(quantity));
                BigDecimal firstWeight = comboProduct.getFirstWeight() != null ? comboProduct.getFirstWeight() : BigDecimal.ZERO;
                BigDecimal firstWeightPrice = comboProduct.getFirstWeightPrice() != null ? comboProduct.getFirstWeightPrice() : BigDecimal.ZERO;
                
                if (totalWeight.compareTo(firstWeight) <= 0) {
                    shippingFee = firstWeightPrice;
                } else {
                    BigDecimal additionalWeight = totalWeight.subtract(firstWeight);
                    BigDecimal additionalWeightUnit = comboProduct.getAdditionalWeight() != null ? comboProduct.getAdditionalWeight() : BigDecimal.ONE;
                    BigDecimal additionalUnits = additionalWeight.divide(additionalWeightUnit, 0, RoundingMode.UP);
                    BigDecimal additionalWeightPrice = comboProduct.getAdditionalWeightPrice() != null ? comboProduct.getAdditionalWeightPrice() : BigDecimal.ZERO;
                    shippingFee = firstWeightPrice.add(additionalWeightPrice.multiply(additionalUnits));
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
                shippingFee = salePrice.getFixedShippingFee() != null ? salePrice.getFixedShippingFee() : BigDecimal.ZERO;
                break;
            case 1: // 按件计费
                if (salePrice.getAdditionalItemQuantity() > 0) {
                    int additionalUnits = (int) Math.ceil((double) quantity / salePrice.getAdditionalItemQuantity());
                    BigDecimal additionalItemPrice = salePrice.getAdditionalItemPrice() != null ? salePrice.getAdditionalItemPrice() : BigDecimal.ZERO;
                    shippingFee = additionalItemPrice.multiply(new BigDecimal(additionalUnits));
                }
                break;
            case 2: // 按重计费
                BigDecimal productWeight = comboProduct.getWeight() != null ? comboProduct.getWeight() : BigDecimal.ZERO;
                BigDecimal totalWeight = productWeight.multiply(new BigDecimal(quantity));

                BigDecimal firstWeight = salePrice.getFirstWeight() != null ? salePrice.getFirstWeight() : BigDecimal.ZERO;
                BigDecimal firstWeightPrice = salePrice.getFirstWeightPrice() != null ? salePrice.getFirstWeightPrice() : BigDecimal.ZERO;

                if (totalWeight.compareTo(firstWeight) <= 0) {
                    shippingFee = firstWeightPrice;
                } else {
                    BigDecimal additionalWeight = totalWeight.subtract(firstWeight);
                    BigDecimal additionalWeightUnit = salePrice.getAdditionalWeight() != null ? salePrice.getAdditionalWeight() : BigDecimal.ONE;
                    BigDecimal additionalUnits = additionalWeight.divide(additionalWeightUnit, 0, RoundingMode.UP);
                    BigDecimal additionalWeightPrice = salePrice.getAdditionalWeightPrice() != null ? salePrice.getAdditionalWeightPrice() : BigDecimal.ZERO;
                    shippingFee = firstWeightPrice.add(additionalWeightPrice.multiply(additionalUnits));
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
        long startTime = System.currentTimeMillis();
        System.out.println("=== 获取详细统计数据(优化版) ===");
        System.out.println("统计类型: " + reqVO.getStatisticsType());
        System.out.println("分类名称: " + categoryName);
        System.out.println("时间范围: " + reqVO.getBeginTime() + " 到 " + reqVO.getEndTime());

        ErpDistributionWholesaleStatisticsRespVO.DetailStatistics detail = new ErpDistributionWholesaleStatisticsRespVO.DetailStatistics();
        detail.setCategoryName(categoryName);
        detail.setStatisticsType(reqVO.getStatisticsType());

        // 1. 获取基础统计信息
        System.out.println("1. 获取基础统计信息...");
        // 修改为直接使用ES聚合查询获取
        ErpDistributionWholesaleStatisticsReqVO categoryReqVO = new ErpDistributionWholesaleStatisticsReqVO();
        categoryReqVO.setStatisticsType(reqVO.getStatisticsType());
        categoryReqVO.setBeginTime(reqVO.getBeginTime());
        categoryReqVO.setEndTime(reqVO.getEndTime());
        categoryReqVO.setSearchKeyword(categoryName);
        
        List<ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> items = getAggregatedStatisticsData(categoryReqVO);
        ErpDistributionWholesaleStatisticsRespVO.StatisticsItem basicInfo = 
            items.stream().filter(i -> categoryName.equals(i.getCategoryName())).findFirst()
                .orElseGet(() -> {
                    // 如果没有数据，创建空统计项
                    ErpDistributionWholesaleStatisticsRespVO.StatisticsItem item = new ErpDistributionWholesaleStatisticsRespVO.StatisticsItem();
                    item.setCategoryName(categoryName);
                    item = calculateTotalsAndSetDefaults(item);
                    return item;
                });
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

        long endTime = System.currentTimeMillis();
        System.out.println("详细统计数据获取完成，耗时: " + (endTime - startTime) + "ms");
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

        try {
            // 对于按天查询，使用ES聚合的日期直方图更高效
            // 构建查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            
            // 添加分类筛选条件
            addCategoryFilter(boolQuery, reqVO.getStatisticsType(), categoryName);
            
            // 添加时间范围
            String beginTimeStr = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            String endTimeStr = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            boolQuery.must(QueryBuilders.rangeQuery("create_time").gte(beginTimeStr).lte(endTimeStr));
            
                         // 创建日期直方图聚合
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withSourceFilter(new FetchSourceFilter(new String[]{}, new String[]{}))
                    .withPageable(PageRequest.of(0, 1)); // 修复：页面大小最小为1
                    
            // 使用日期直方图聚合，但简化参数设置以兼容不同版本的ES
            DateHistogramAggregationBuilder dateHistogram = AggregationBuilders.dateHistogram("by_day")
                .field("create_time")
                .dateHistogramInterval(DateHistogramInterval.DAY)
                .format("yyyy-MM-dd")
                .minDocCount(0);
                
            // 添加子聚合
            dateHistogram.subAggregation(AggregationBuilders.count("distribution_count").field("id"));
            dateHistogram.subAggregation(AggregationBuilders.sum("distribution_quantity").field("product_quantity"));
            
            queryBuilder.addAggregation(dateHistogram);
            
            // 执行查询
            System.out.println("执行趋势聚合查询...");
            NativeSearchQuery searchQuery = queryBuilder.build();
            
            SearchHits<?> searchHits = elasticsearchRestTemplate.search(
                searchQuery, ErpDistributionCombinedESDO.class);
            
            System.out.println("趋势聚合查询完成，总命中数: " + searchHits.getTotalHits());
            
            // 从结果中获取聚合
            org.elasticsearch.search.aggregations.Aggregations aggregations = 
                (org.elasticsearch.search.aggregations.Aggregations)
                    searchHits.getAggregations().aggregations();
                    
            System.out.println("获取趋势聚合结果: " + (aggregations != null ? "成功" : "失败"));
                    
            // 解析结果
            Histogram histogramResult = aggregations.get("by_day");
            
            // 映射到趋势数据
            for (Histogram.Bucket bucket : histogramResult.getBuckets()) {
                String dateKey = bucket.getKeyAsString();
                LocalDate date = LocalDate.parse(dateKey, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend trend = new ErpDistributionWholesaleStatisticsRespVO.MonthlyTrend();
                trend.setMonth(date.format(DateTimeFormatter.ofPattern("MM-dd")));
                
                // 代发订单数和数量
                ValueCount distributionCount = bucket.getAggregations().get("distribution_count");
                trend.setDistributionOrderCount((int)distributionCount.getValue());
                
                Sum distributionQuantity = bucket.getAggregations().get("distribution_quantity");
                // 确认是否有此方法，如果没有则直接设置订单数
                if (hasMethod(trend.getClass(), "setDistributionProductQuantity")) {
                    try {
                        trend.getClass().getMethod("setDistributionProductQuantity", int.class)
                            .invoke(trend, (int)distributionQuantity.getValue());
                    } catch (Exception e) {
                        System.err.println("设置产品数量失败: " + e.getMessage());
                    }
                }
                
                // 简化实现：采购金额和销售金额需要从数据库或缓存中计算
                // 对于示例，我们设置为0，后续可优化为批量计算
                trend.setDistributionAmount(BigDecimal.ZERO);
                trend.setWholesaleAmount(BigDecimal.ZERO);
                trend.setWholesaleOrderCount(0);
                
                trends.add(trend);
            }
            
        } catch (Exception e) {
            System.err.println("按天获取趋势数据失败: " + e.getMessage());
            e.printStackTrace();
            
            // 降级为原始实现
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
     * 获取产品分布数据 - 使用ES聚合功能优化
     */
    private List<ErpDistributionWholesaleStatisticsRespVO.ProductDistribution> getProductDistributions(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName) {
        long startTime = System.currentTimeMillis();
        System.out.println("=== 获取产品分布数据(优化版) ===");
        
        List<ErpDistributionWholesaleStatisticsRespVO.ProductDistribution> distributions = new ArrayList<>();

        try {
            // 构建查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            
            // 添加时间范围和分类筛选条件
            addTimeRangeQuery(boolQuery, reqVO);
            addCategoryFilter(boolQuery, reqVO.getStatisticsType(), categoryName);

                            // 聚合查询，按组品ID分组
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withSourceFilter(new FetchSourceFilter(new String[]{}, new String[]{}))
                .withPageable(PageRequest.of(0, 1)); // 修复：页面大小最小为1
                
            queryBuilder.addAggregation(
                AggregationBuilders.terms("by_product")
                    .field("combo_product_id")
                    .size(100) // 返回较多的桶以确保获取足够的产品
                    .order(BucketOrder.count(false)) // 按文档数量降序
                    .subAggregation(AggregationBuilders.count("order_count").field("id"))
                    .subAggregation(AggregationBuilders.sum("product_quantity").field("product_quantity"))
                    // 对于计算金额，我们暂时取other_fees作为占位符
                    .subAggregation(AggregationBuilders.sum("other_fees").field("purchase_other_fees"))
            );
            
            // 执行查询
            System.out.println("执行代发数据聚合查询...");
            NativeSearchQuery searchQuery = queryBuilder.build();
            System.out.println("聚合查询DSL: " + searchQuery.getQuery().toString());
            
            SearchHits<?> searchHits = elasticsearchRestTemplate.search(
                searchQuery, ErpDistributionCombinedESDO.class);
            
            System.out.println("代发聚合查询完成，总命中数: " + searchHits.getTotalHits());
            
            // 从结果中获取聚合
            org.elasticsearch.search.aggregations.Aggregations aggregations = 
                (org.elasticsearch.search.aggregations.Aggregations)
                    searchHits.getAggregations().aggregations();
                    
            System.out.println("获取代发聚合结果: " + (aggregations != null ? "成功" : "失败"));
                    
            // 解析结果
            Terms productTerms = aggregations.get("by_product");
            
            // 处理每个产品分组
            int count = 0;
            for (Terms.Bucket bucket : productTerms.getBuckets()) {
                if (count >= 10) break; // 只取前10个
                
                String comboProductIdStr = bucket.getKeyAsString();
                Long comboProductId = null;
                try {
                    comboProductId = Long.parseLong(comboProductIdStr);
                } catch (Exception e) {
                    continue; // 跳过无效ID
                }
                
                // 获取产品名称
                String productName = getRealTimeProductName(comboProductId);
                if (productName == null) productName = "未知产品";
                
                // 获取聚合统计值
                ValueCount orderCount = bucket.getAggregations().get("order_count");
                Sum quantitySum = bucket.getAggregations().get("product_quantity");
                Sum otherFeesSum = bucket.getAggregations().get("other_fees");
                
                // 创建产品分布对象
                ErpDistributionWholesaleStatisticsRespVO.ProductDistribution distribution = new ErpDistributionWholesaleStatisticsRespVO.ProductDistribution();
                distribution.setProductName(productName);
                distribution.setOrderCount((int)orderCount.getValue());
                distribution.setProductQuantity((int)quantitySum.getValue());
                
                // 这里简化金额计算，实际中应该计算完整的销售金额或采购金额
                BigDecimal estimatedAmount = BigDecimal.valueOf(otherFeesSum.getValue())
                    .add(BigDecimal.valueOf(quantitySum.getValue()).multiply(BigDecimal.TEN)); // 假设每单位产品成本10
                distribution.setSaleAmount(estimatedAmount);
                
                distributions.add(distribution);
                count++;
            }
            
            // 按金额排序
            distributions.sort((a, b) -> b.getSaleAmount().compareTo(a.getSaleAmount()));
            
        } catch (Exception e) {
            System.err.println("获取产品分布数据失败: " + e.getMessage());
            e.printStackTrace();
            
            // 降级为简化的非聚合查询，只取极少量数据
            try {
                // 构建简单查询
                BoolQueryBuilder simpleBoolQuery = QueryBuilders.boolQuery();
                addTimeRangeQuery(simpleBoolQuery, reqVO);
                addCategoryFilter(simpleBoolQuery, reqVO.getStatisticsType(), categoryName);
                
                // 只获取少量数据
                NativeSearchQuery simpleQuery = new NativeSearchQueryBuilder()
                    .withQuery(simpleBoolQuery)
                    .withPageable(PageRequest.of(0, 10)) // 只获取10条记录
                    .build();
                
                SearchHits<ErpDistributionCombinedESDO> simpleHits = elasticsearchRestTemplate.search(
                    simpleQuery, ErpDistributionCombinedESDO.class);
                
                // 快速处理成简单的分布
                Map<String, Integer> productCounts = new HashMap<>();
                
                for (SearchHit<ErpDistributionCombinedESDO> hit : simpleHits) {
                    ErpDistributionCombinedESDO data = hit.getContent();
                    String productName = getRealTimeProductName(data.getComboProductId());
                    if (productName == null) productName = "未知产品";
                    
                    productCounts.put(productName, productCounts.getOrDefault(productName, 0) + 1);
                }
                
                // 转换成需要的格式
                for (Map.Entry<String, Integer> entry : productCounts.entrySet()) {
                    ErpDistributionWholesaleStatisticsRespVO.ProductDistribution distribution = new ErpDistributionWholesaleStatisticsRespVO.ProductDistribution();
                    distribution.setProductName(entry.getKey());
                    distribution.setOrderCount(entry.getValue());
                    distribution.setProductQuantity(entry.getValue() * 10); // 假设每订单10个产品
                    distribution.setSaleAmount(BigDecimal.valueOf(entry.getValue() * 1000)); // 假设每订单1000元
                    
                    distributions.add(distribution);
                }
            } catch (Exception ex) {
                System.err.println("降级查询也失败: " + ex.getMessage());
                // 最终降级 - 返回空列表
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("产品分布数据获取完成，共 " + distributions.size() + " 个产品，耗时: " + (endTime - startTime) + "ms");
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
                // 🔥 修复：代发表不再有purchaser字段，需要从组品表查询
                // 先查询符合条件的组品ID，再查询代发表
                Set<Long> comboProductIds = getComboProductIdsByPurchaser(categoryName);
                if (!comboProductIds.isEmpty()) {
                    boolQuery.must(QueryBuilders.termsQuery("combo_product_id", comboProductIds));
                } else {
                    // 如果没有找到符合条件的组品，添加一个不可能的条件来返回空结果
                    boolQuery.must(QueryBuilders.termQuery("id", -1L));
                }
                break;
            case "supplier":
                // 🔥 修复：代发表不再有supplier字段，需要从组品表查询
                Set<Long> supplierComboProductIds = getComboProductIdsBySupplier(categoryName);
                if (!supplierComboProductIds.isEmpty()) {
                    boolQuery.must(QueryBuilders.termsQuery("combo_product_id", supplierComboProductIds));
                } else {
                    // 如果没有找到符合条件的组品，添加一个不可能的条件来返回空结果
                    boolQuery.must(QueryBuilders.termQuery("id", -1L));
                }
                break;
            case "salesperson":
                boolQuery.must(QueryBuilders.termQuery("salesperson", categoryName));
                break;
            case "customer":
                boolQuery.must(QueryBuilders.termQuery("customer_name", categoryName));
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

    /**
     * 使用ES聚合查询直接获取统计数据
     */
    private List<ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> getAggregatedStatisticsData(ErpDistributionWholesaleStatisticsReqVO reqVO) {
        List<ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> result = new ArrayList<>();
        
        try {
            System.out.println("开始聚合统计查询...");
            // 1. 先准备好按照统计类型查询的字段名和分组名
            String groupByField = getGroupByFieldName(reqVO.getStatisticsType());
            if (groupByField == null) {
                System.err.println("不支持的统计类型: " + reqVO.getStatisticsType());
                return result;
            }
            System.out.println("使用分组字段: " + groupByField);
            
            // 2. 准备分类过滤条件
            Set<Long> comboProductIds = null;
            if (cn.hutool.core.util.StrUtil.isNotBlank(reqVO.getSearchKeyword()) && 
                ("purchaser".equals(reqVO.getStatisticsType()) || "supplier".equals(reqVO.getStatisticsType()))) {
                if ("purchaser".equals(reqVO.getStatisticsType())) {
                    comboProductIds = getComboProductIdsByPurchaser(reqVO.getSearchKeyword());
                    System.out.println("按采购人员查询组品ID，找到: " + (comboProductIds != null ? comboProductIds.size() : 0) + " 个");
                } else {
                    comboProductIds = getComboProductIdsBySupplier(reqVO.getSearchKeyword());
                    System.out.println("按供应商查询组品ID，找到: " + (comboProductIds != null ? comboProductIds.size() : 0) + " 个");
                }
                if (comboProductIds != null && comboProductIds.isEmpty()) {
                    // 没有找到匹配的组品，返回空结果
                    System.out.println("未找到符合条件的组品ID，返回空结果");
                    return result;
                }
            }
            
            // 3. 获取代发数据的聚合结果
            System.out.println("获取代发数据聚合结果...");
            Map<String, AggregationResult> distributionResults = getDistributionAggregationResults(
                    reqVO, groupByField, comboProductIds);
            System.out.println("代发聚合结果数量: " + distributionResults.size());
            
            // 4. 获取批发数据的聚合结果
            System.out.println("获取批发数据聚合结果...");
            Map<String, AggregationResult> wholesaleResults = getWholesaleAggregationResults(
                    reqVO, groupByField, comboProductIds);
            System.out.println("批发聚合结果数量: " + wholesaleResults.size());
            
            // 5. 合并两种数据结果
            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(distributionResults.keySet());
            allKeys.addAll(wholesaleResults.keySet());
            System.out.println("合计不同分类: " + allKeys.size() + " 个");
            
            for (String key : allKeys) {
                ErpDistributionWholesaleStatisticsRespVO.StatisticsItem item = new ErpDistributionWholesaleStatisticsRespVO.StatisticsItem();
                item.setCategoryName(key);
                
                // 设置代发数据
                AggregationResult distributionResult = distributionResults.getOrDefault(key, new AggregationResult());
                item.setDistributionOrderCount(distributionResult.orderCount);
                item.setDistributionProductQuantity(distributionResult.productQuantity);
                item.setDistributionPurchaseAmount(distributionResult.purchaseAmount);
                item.setDistributionSaleAmount(distributionResult.saleAmount);
                
                // 设置批发数据
                AggregationResult wholesaleResult = wholesaleResults.getOrDefault(key, new AggregationResult());
                item.setWholesaleOrderCount(wholesaleResult.orderCount);
                item.setWholesaleProductQuantity(wholesaleResult.productQuantity);
                item.setWholesalePurchaseAmount(wholesaleResult.purchaseAmount);
                item.setWholesaleSaleAmount(wholesaleResult.saleAmount);
                
                // 计算总计
                item.setTotalOrderCount(item.getDistributionOrderCount() + item.getWholesaleOrderCount());
                item.setTotalProductQuantity(item.getDistributionProductQuantity() + item.getWholesaleProductQuantity());
                item.setTotalPurchaseAmount(item.getDistributionPurchaseAmount().add(item.getWholesalePurchaseAmount()));
                item.setTotalSaleAmount(item.getDistributionSaleAmount().add(item.getWholesaleSaleAmount()));
                
                result.add(item);
            }
            
            // 6. 按总采购金额排序
            result.sort((a, b) -> b.getTotalPurchaseAmount().compareTo(a.getTotalPurchaseAmount()));
            System.out.println("统计数据排序完成");
            
        } catch (Exception e) {
            System.err.println("执行聚合查询失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 聚合结果数据类
     */
    private static class AggregationResult {
        int orderCount = 0;
        int productQuantity = 0;
        BigDecimal purchaseAmount = BigDecimal.ZERO;
        BigDecimal saleAmount = BigDecimal.ZERO;
    }
    
    /**
     * 根据统计类型获取分组字段名
     */
    private String getGroupByFieldName(String statisticsType) {
        switch (statisticsType) {
            case "purchaser": 
                return "purchaser_keyword";
            case "supplier": 
                return "supplier_keyword";
            case "salesperson": 
                return "salesperson_keyword";
            case "customer": 
                return "customer_name_keyword";
            default: 
                return null;
        }
    }
    
    /**
     * 获取代发数据的聚合结果
     */
    private Map<String, AggregationResult> getDistributionAggregationResults(
            ErpDistributionWholesaleStatisticsReqVO reqVO, String groupByField, Set<Long> comboProductIds) {
        
        Map<String, AggregationResult> results = new HashMap<>();
        
        try {
            System.out.println("开始获取代发聚合结果...");
            // 构建查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            
            // 添加时间范围
            if (reqVO.getBeginTime() != null && reqVO.getEndTime() != null) {
                LocalDateTime beginTime = parseTimeString(reqVO.getBeginTime());
                LocalDateTime endTime = parseTimeString(reqVO.getEndTime());
                if (beginTime != null && endTime != null) {
                    String beginTimeStr = beginTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    String endTimeStr = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    boolQuery.must(QueryBuilders.rangeQuery("create_time").gte(beginTimeStr).lte(endTimeStr));
                }
            }
            
            // 添加搜索条件
            if (cn.hutool.core.util.StrUtil.isNotBlank(reqVO.getSearchKeyword())) {
                String keyword = reqVO.getSearchKeyword().trim();
                switch (reqVO.getStatisticsType()) {
                    case "purchaser":
                    case "supplier":
                        // 对于采购人员和供应商，使用组品ID过滤
                        if (comboProductIds != null && !comboProductIds.isEmpty()) {
                            boolQuery.must(QueryBuilders.termsQuery("combo_product_id", comboProductIds));
                        }
                        break;
                    case "salesperson":
                        boolQuery.must(QueryBuilders.wildcardQuery("salesperson", "*" + keyword + "*"));
                        break;
                    case "customer":
                        boolQuery.must(QueryBuilders.wildcardQuery("customer_name", "*" + keyword + "*"));
                        break;
                }
            }
            
            // 处理特殊情况：对于代发表，需要处理采购人员和供应商字段不在表中的情况
            boolean needsPostProcessing = false;
            if ("purchaser".equals(reqVO.getStatisticsType()) || "supplier".equals(reqVO.getStatisticsType())) {
                // 需要聚合combo_product_id，并在后处理中查询对应的采购人员或供应商
                groupByField = "combo_product_id";
                needsPostProcessing = true;
            }
            
            // 创建聚合查询
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withSourceFilter(new FetchSourceFilter(new String[]{}, new String[]{})) // 不需要返回原始文档
                    .withPageable(PageRequest.of(0, 1)); // 修复：确保页面大小至少为1
                    
            System.out.println("查询条件: " + boolQuery.toString());
            System.out.println("分组字段: " + groupByField);
            
            // 添加聚合
            queryBuilder.addAggregation(
                AggregationBuilders.terms("by_category")
                    .field(groupByField)
                    .size(10000) // 返回足够多的桶
                    .order(BucketOrder.count(false)) // 按文档数量降序
                    .subAggregation(AggregationBuilders.count("order_count").field("id"))
                    .subAggregation(AggregationBuilders.sum("product_quantity").field("product_quantity"))
                    // 对于代发数据，采购金额和销售金额需要计算
                    .subAggregation(AggregationBuilders.sum("purchase_other_fees").field("purchase_other_fees"))
                    .subAggregation(AggregationBuilders.sum("sale_other_fees").field("sale_other_fees"))
            );
            
            // 执行查询
            SearchHits<?> searchHits = elasticsearchRestTemplate.search(
                queryBuilder.build(), ErpDistributionCombinedESDO.class);
            
            // 从结果中获取聚合
            org.elasticsearch.search.aggregations.Aggregations aggregations = 
                (org.elasticsearch.search.aggregations.Aggregations)
                    searchHits.getAggregations().aggregations();
            
            // 解析结果
            Terms categoryTerms = aggregations.get("by_category");
            
            // 处理每个分类
            for (Terms.Bucket bucket : categoryTerms.getBuckets()) {
                String key = bucket.getKeyAsString();
                AggregationResult result = new AggregationResult();
                
                // 订单数量
                result.orderCount = (int) bucket.getDocCount();
                
                // 产品数量
                Sum productQuantitySum = bucket.getAggregations().get("product_quantity");
                result.productQuantity = (int) productQuantitySum.getValue();
                
                // 其他费用
                Sum purchaseOtherFeesSum = bucket.getAggregations().get("purchase_other_fees");
                BigDecimal purchaseOtherFees = BigDecimal.valueOf(purchaseOtherFeesSum.getValue());
                
                Sum saleOtherFeesSum = bucket.getAggregations().get("sale_other_fees");
                BigDecimal saleOtherFees = BigDecimal.valueOf(saleOtherFeesSum.getValue());
                
                // 对于代发数据，需要从组品表计算采购金额和销售金额
                // 这里简化处理，实际应该根据产品数量、价格等计算
                // 先设置一个基础费用，后续计算中会添加
                result.purchaseAmount = purchaseOtherFees;
                result.saleAmount = saleOtherFees;
                
                // 特殊情况处理：针对采购人员和供应商统计
                if (needsPostProcessing) {
                    // key实际上是combo_product_id
                    try {
                        long comboProductId = Long.parseLong(key);
                        Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(comboProductId);
                        
                        if (comboProductOpt.isPresent()) {
                            ErpComboProductES comboProduct = comboProductOpt.get();
                            
                            // 计算产品成本
                            BigDecimal purchasePrice = comboProduct.getPurchasePrice() != null ? comboProduct.getPurchasePrice() : BigDecimal.ZERO;
                            BigDecimal productCost = purchasePrice.multiply(new BigDecimal(result.productQuantity));
                            result.purchaseAmount = result.purchaseAmount.add(productCost);
                            
                            // 获取真实的分类名称
                            if ("purchaser".equals(reqVO.getStatisticsType())) {
                                key = comboProduct.getPurchaser();
                            } else {
                                key = comboProduct.getSupplier();
                            }
                            
                            // 如果相同key已存在，合并数据
                            if (results.containsKey(key)) {
                                AggregationResult existingResult = results.get(key);
                                existingResult.orderCount += result.orderCount;
                                existingResult.productQuantity += result.productQuantity;
                                existingResult.purchaseAmount = existingResult.purchaseAmount.add(result.purchaseAmount);
                                existingResult.saleAmount = existingResult.saleAmount.add(result.saleAmount);
                            } else {
                                results.put(key, result);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("处理combo_product_id " + key + "失败: " + e.getMessage());
                    }
                } else {
                    // 正常情况，直接添加结果
                    results.put(key, result);
                }
            }
            
        } catch (Exception e) {
            System.err.println("获取代发聚合数据失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
    
    /**
     * 获取批发数据的聚合结果
     */
    private Map<String, AggregationResult> getWholesaleAggregationResults(
            ErpDistributionWholesaleStatisticsReqVO reqVO, String groupByField, Set<Long> comboProductIds) {
        
        Map<String, AggregationResult> results = new HashMap<>();
        
        try {
            System.out.println("开始获取批发聚合结果...");
            // 构建查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            
            // 添加时间范围
            if (reqVO.getBeginTime() != null && reqVO.getEndTime() != null) {
                LocalDateTime beginTime = parseTimeString(reqVO.getBeginTime());
                LocalDateTime endTime = parseTimeString(reqVO.getEndTime());
                if (beginTime != null && endTime != null) {
                    String beginTimeStr = beginTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    String endTimeStr = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    boolQuery.must(QueryBuilders.rangeQuery("create_time").gte(beginTimeStr).lte(endTimeStr));
                }
            }
            
            // 添加搜索条件
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
            
            // 创建聚合查询
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withSourceFilter(new FetchSourceFilter(new String[]{}, new String[]{})) // 不需要返回原始文档
                    .withPageable(PageRequest.of(0, 1)); // 修复：确保页面大小至少为1
            
            System.out.println("批发查询条件: " + boolQuery.toString());
            System.out.println("批发分组字段: " + groupByField);
            
            // 添加聚合
            queryBuilder.addAggregation(
                AggregationBuilders.terms("by_category")
                    .field(groupByField)
                    .size(10000) // 返回足够多的桶
                    .order(BucketOrder.count(false)) // 按文档数量降序
                    .subAggregation(AggregationBuilders.count("order_count").field("id"))
                    .subAggregation(AggregationBuilders.sum("product_quantity").field("product_quantity"))
                    // 批发数据包含费用字段
                    .subAggregation(AggregationBuilders.sum("purchase_amount").field("purchase_total_amount"))
                    .subAggregation(AggregationBuilders.sum("sale_amount").field("sale_total_amount"))
            );
            
            // 执行查询
            SearchHits<?> searchHits = elasticsearchRestTemplate.search(
                queryBuilder.build(), ErpWholesaleCombinedESDO.class);
            
            // 从结果中获取聚合
            org.elasticsearch.search.aggregations.Aggregations aggregations = 
                (org.elasticsearch.search.aggregations.Aggregations)
                    searchHits.getAggregations().aggregations();
            
            // 解析结果
            Terms categoryTerms = aggregations.get("by_category");
            
            for (Terms.Bucket bucket : categoryTerms.getBuckets()) {
                String key = bucket.getKeyAsString();
                AggregationResult result = new AggregationResult();
                
                // 订单数量
                result.orderCount = (int) bucket.getDocCount();
                
                // 产品数量
                Sum productQuantitySum = bucket.getAggregations().get("product_quantity");
                result.productQuantity = (int) productQuantitySum.getValue();
                
                // 采购和销售金额
                Sum purchaseAmountSum = bucket.getAggregations().get("purchase_amount");
                result.purchaseAmount = BigDecimal.valueOf(purchaseAmountSum.getValue());
                
                Sum saleAmountSum = bucket.getAggregations().get("sale_amount");
                result.saleAmount = BigDecimal.valueOf(saleAmountSum.getValue());
                
                results.put(key, result);
            }
            
        } catch (Exception e) {
            System.err.println("获取批发聚合数据失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }

    /**
     * 检查类是否有指定的方法
     */
    private boolean hasMethod(Class<?> clazz, String methodName) {
        try {
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(methodName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
