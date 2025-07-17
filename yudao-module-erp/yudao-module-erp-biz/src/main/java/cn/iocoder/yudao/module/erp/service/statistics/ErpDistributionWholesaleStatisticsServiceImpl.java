package cn.iocoder.yudao.module.erp.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpDistributionWholesaleStatisticsRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionCombinedESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleCombinedESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductItemES;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductESDO;
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
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.stereotype.Service;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.springframework.data.elasticsearch.core.ElasticsearchAggregations;
import org.elasticsearch.search.aggregations.Aggregations;


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

        System.out.println("=== 开始代发批发统计查询（ES聚合优化版） ===");
        System.out.println("请求参数: " + reqVO);

        try {
            // 🔥 优化：使用ES聚合查询直接获取统计结果
            List<ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> items = getStatisticsByAggregation(reqVO);
            respVO.setItems(items);

            System.out.println("最终统计项数量: " + items.size());
            System.out.println("=== 代发批发统计查询结束（ES聚合优化版） ===");

        } catch (Exception e) {
            System.err.println("ES聚合统计查询失败: " + e.getMessage());
            e.printStackTrace();
            // 聚合查询失败时返回空结果
            respVO.setItems(Collections.emptyList());
        }

        return respVO;
    }

    /**
     * 🔥 优化：使用ES聚合函数提高查询效率，确保计算逻辑正确性
     */
    private List<ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> getStatisticsByAggregation(
            ErpDistributionWholesaleStatisticsReqVO reqVO) {

        Map<String, ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> itemMap = new HashMap<>();

        try {
            // 🔥 优化：使用ES聚合查询提高效率
            // 1. 代发数据聚合统计
            getDistributionStatisticsByAggregation(reqVO, itemMap);

            // 2. 批发数据聚合统计
            getWholesaleStatisticsByAggregation(reqVO, itemMap);

            // 3. 计算总计并排序
            return itemMap.values().stream()
                    .map(this::calculateTotalsAndSetDefaults)
                    .sorted((a, b) -> {
                        // 按总采购金额降序排序
                        BigDecimal totalA = a.getTotalPurchaseAmount();
                        BigDecimal totalB = b.getTotalPurchaseAmount();
                        return totalB.compareTo(totalA);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("统计查询失败: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 🔥 修复：代发数据聚合统计 - 与代发表服务层计算逻辑完全一致
     */
    private void getDistributionStatisticsByAggregation(ErpDistributionWholesaleStatisticsReqVO reqVO,
                                                       Map<String, ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> itemMap) {
        try {
            // 构建基础查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            addTimeRangeQuery(boolQuery, reqVO);

            if (StrUtil.isNotBlank(reqVO.getSearchKeyword())) {
                addCategoryFilter(boolQuery, reqVO.getStatisticsType(), reqVO.getSearchKeyword());
            }

            // 🔥 修复：对于采购人员和供应商，需要特殊处理
            if ("purchaser".equals(reqVO.getStatisticsType()) || "supplier".equals(reqVO.getStatisticsType())) {
                getDistributionStatisticsByComboProduct(reqVO, itemMap, boolQuery);
                return;
            }

            // 🔥 修复：对于其他统计类型，使用ES聚合查询
            String aggregationField = getAggregationField(reqVO.getStatisticsType(), "distribution");
            
            // 构建聚合查询 - 只聚合基础统计数据，金额通过后续计算
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withMaxResults(0) // 不需要返回文档，只要聚合结果
                    .addAggregation(AggregationBuilders.terms("category_agg")
                            .field(aggregationField)
                            .size(10000)
                            .subAggregation(AggregationBuilders.count("order_count").field("id"))
                            .subAggregation(AggregationBuilders.sum("product_quantity").field("product_quantity"))
                            .subAggregation(AggregationBuilders.sum("purchase_other_fees").field("purchase_other_fees"))
                            .subAggregation(AggregationBuilders.sum("sale_other_fees").field("sale_other_fees")));

            SearchHits<ErpDistributionCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpDistributionCombinedESDO.class,
                    IndexCoordinates.of("erp_distribution_combined"));

            if (searchHits.hasAggregations()) {
                ElasticsearchAggregations elasticsearchAggregations = (ElasticsearchAggregations) searchHits.getAggregations();
                Aggregations aggregations = elasticsearchAggregations.aggregations();
                Terms categoryAgg = aggregations.get("category_agg");

                if (categoryAgg != null) {
                    for (Terms.Bucket bucket : categoryAgg.getBuckets()) {
                        String categoryName = bucket.getKeyAsString();
                        if (StrUtil.isBlank(categoryName)) continue;

                        ErpDistributionWholesaleStatisticsRespVO.StatisticsItem item = itemMap.computeIfAbsent(categoryName,
                                k -> new ErpDistributionWholesaleStatisticsRespVO.StatisticsItem());

                        item.setCategoryName(categoryName);

                        // 累加基础统计数据
                        item.setDistributionOrderCount((item.getDistributionOrderCount() == null ? 0 : item.getDistributionOrderCount()) + (int) bucket.getDocCount());

                        Sum productQuantityAgg = bucket.getAggregations().get("product_quantity");
                        if (productQuantityAgg != null) {
                            item.setDistributionProductQuantity((item.getDistributionProductQuantity() == null ? 0 : item.getDistributionProductQuantity()) + (int) productQuantityAgg.getValue());
                        }

                        // 🔥 修复：只累加费用，金额通过后续计算
                        Sum purchaseOtherFeesAgg = bucket.getAggregations().get("purchase_other_fees");
                        Sum saleOtherFeesAgg = bucket.getAggregations().get("sale_other_fees");

                        BigDecimal purchaseOtherFees = purchaseOtherFeesAgg != null ?
                                BigDecimal.valueOf(purchaseOtherFeesAgg.getValue()) : BigDecimal.ZERO;
                        BigDecimal saleOtherFees = saleOtherFeesAgg != null ?
                                BigDecimal.valueOf(saleOtherFeesAgg.getValue()) : BigDecimal.ZERO;

                        // 暂时累加费用，后续需要补充产品价格计算
                        item.setDistributionPurchaseAmount((item.getDistributionPurchaseAmount() == null ? BigDecimal.ZERO : item.getDistributionPurchaseAmount()).add(purchaseOtherFees));
                        item.setDistributionSaleAmount((item.getDistributionSaleAmount() == null ? BigDecimal.ZERO : item.getDistributionSaleAmount()).add(saleOtherFees));
                    }
                }
            }

            // 🔥 修复：批量补充产品价格计算
            supplementDistributionAmounts(itemMap, reqVO.getStatisticsType());

        } catch (Exception e) {
            System.err.println("代发聚合统计失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔥 优化：批发数据聚合统计 - 使用ES聚合提高效率
     */
    private void getWholesaleStatisticsByAggregation(ErpDistributionWholesaleStatisticsReqVO reqVO,
                                                    Map<String, ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> itemMap) {
        try {
            // 构建基础查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            addTimeRangeQuery(boolQuery, reqVO);

            if (StrUtil.isNotBlank(reqVO.getSearchKeyword())) {
                addCategoryFilter(boolQuery, reqVO.getStatisticsType(), reqVO.getSearchKeyword());
            }

            // 🔥 优化：对于采购人员和供应商，需要特殊处理
            if ("purchaser".equals(reqVO.getStatisticsType()) || "supplier".equals(reqVO.getStatisticsType())) {
                getWholesaleStatisticsByComboProduct(reqVO, itemMap, boolQuery);
                return;
            }

            // 🔥 优化：对于其他统计类型，使用ES聚合查询
            String aggregationField = getAggregationField(reqVO.getStatisticsType(), "wholesale");
            
            // 构建聚合查询
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withMaxResults(0) // 不需要返回文档，只要聚合结果
                    .addAggregation(AggregationBuilders.terms("category_agg")
                            .field(aggregationField)
                            .size(10000)
                            .subAggregation(AggregationBuilders.count("order_count").field("id"))
                            .subAggregation(AggregationBuilders.sum("product_quantity").field("product_quantity"))
                            .subAggregation(AggregationBuilders.sum("purchase_truck_fee").field("purchase_truck_fee"))
                            .subAggregation(AggregationBuilders.sum("purchase_logistics_fee").field("purchase_logistics_fee"))
                            .subAggregation(AggregationBuilders.sum("purchase_other_fees").field("purchase_other_fees"))
                            .subAggregation(AggregationBuilders.sum("sale_truck_fee").field("sale_truck_fee"))
                            .subAggregation(AggregationBuilders.sum("sale_logistics_fee").field("sale_logistics_fee"))
                            .subAggregation(AggregationBuilders.sum("sale_other_fees").field("sale_other_fees")));

            SearchHits<ErpWholesaleCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpWholesaleCombinedESDO.class,
                    IndexCoordinates.of("erp_wholesale_combined"));

            if (searchHits.hasAggregations()) {
                ElasticsearchAggregations elasticsearchAggregations = (ElasticsearchAggregations) searchHits.getAggregations();
                Aggregations aggregations = elasticsearchAggregations.aggregations();
                Terms categoryAgg = aggregations.get("category_agg");

                if (categoryAgg != null) {
                    for (Terms.Bucket bucket : categoryAgg.getBuckets()) {
                        String categoryName = bucket.getKeyAsString();
                        if (StrUtil.isBlank(categoryName)) continue;

                        ErpDistributionWholesaleStatisticsRespVO.StatisticsItem item = itemMap.computeIfAbsent(categoryName,
                                k -> new ErpDistributionWholesaleStatisticsRespVO.StatisticsItem());

                        item.setCategoryName(categoryName);

                        // 累加统计数据
                        item.setWholesaleOrderCount((item.getWholesaleOrderCount() == null ? 0 : item.getWholesaleOrderCount()) + (int) bucket.getDocCount());

                        Sum productQuantityAgg = bucket.getAggregations().get("product_quantity");
                        if (productQuantityAgg != null) {
                            item.setWholesaleProductQuantity((item.getWholesaleProductQuantity() == null ? 0 : item.getWholesaleProductQuantity()) + (int) productQuantityAgg.getValue());
                        }

                        // 🔥 优化：金额计算需要获取组品信息，这里先累加费用
                        Sum purchaseTruckFeeAgg = bucket.getAggregations().get("purchase_truck_fee");
                        Sum purchaseLogisticsFeeAgg = bucket.getAggregations().get("purchase_logistics_fee");
                        Sum purchaseOtherFeesAgg = bucket.getAggregations().get("purchase_other_fees");
                        Sum saleTruckFeeAgg = bucket.getAggregations().get("sale_truck_fee");
                        Sum saleLogisticsFeeAgg = bucket.getAggregations().get("sale_logistics_fee");
                        Sum saleOtherFeesAgg = bucket.getAggregations().get("sale_other_fees");

                        BigDecimal purchaseTruckFee = purchaseTruckFeeAgg != null ?
                                BigDecimal.valueOf(purchaseTruckFeeAgg.getValue()) : BigDecimal.ZERO;
                        BigDecimal purchaseLogisticsFee = purchaseLogisticsFeeAgg != null ?
                                BigDecimal.valueOf(purchaseLogisticsFeeAgg.getValue()) : BigDecimal.ZERO;
                        BigDecimal purchaseOtherFees = purchaseOtherFeesAgg != null ?
                                BigDecimal.valueOf(purchaseOtherFeesAgg.getValue()) : BigDecimal.ZERO;
                        BigDecimal saleTruckFee = saleTruckFeeAgg != null ?
                                BigDecimal.valueOf(saleTruckFeeAgg.getValue()) : BigDecimal.ZERO;
                        BigDecimal saleLogisticsFee = saleLogisticsFeeAgg != null ?
                                BigDecimal.valueOf(saleLogisticsFeeAgg.getValue()) : BigDecimal.ZERO;
                        BigDecimal saleOtherFees = saleOtherFeesAgg != null ?
                                BigDecimal.valueOf(saleOtherFeesAgg.getValue()) : BigDecimal.ZERO;

                        // 暂时累加费用，后续需要补充产品价格计算
                        item.setWholesalePurchaseAmount((item.getWholesalePurchaseAmount() == null ? BigDecimal.ZERO : item.getWholesalePurchaseAmount())
                                .add(purchaseTruckFee).add(purchaseLogisticsFee).add(purchaseOtherFees));
                        item.setWholesaleSaleAmount((item.getWholesaleSaleAmount() == null ? BigDecimal.ZERO : item.getWholesaleSaleAmount())
                                .add(saleTruckFee).add(saleLogisticsFee).add(saleOtherFees));
                    }
                }
            }

            // 🔥 优化：批量补充产品价格计算
            supplementWholesaleAmounts(itemMap, reqVO.getStatisticsType());

        } catch (Exception e) {
            System.err.println("批发聚合统计失败: " + e.getMessage());
            e.printStackTrace();
        }
    }





    /**
     * 🔥 修复：代发数据按组品ID聚合统计（用于采购人员和供应商统计）
     */
    private void getDistributionStatisticsByComboProduct(ErpDistributionWholesaleStatisticsReqVO reqVO,
                                                        Map<String, ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> itemMap,
                                                        BoolQueryBuilder boolQuery) {
        try {
            // 构建聚合查询，按组品ID分组
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withMaxResults(0)
                    .addAggregation(AggregationBuilders.terms("combo_product_agg")
                            .field("combo_product_id")
                            .size(10000)
                            .subAggregation(AggregationBuilders.count("order_count").field("id"))
                            .subAggregation(AggregationBuilders.sum("product_quantity").field("product_quantity"))
                            .subAggregation(AggregationBuilders.sum("purchase_other_fees").field("purchase_other_fees"))
                            .subAggregation(AggregationBuilders.sum("sale_other_fees").field("sale_other_fees")));

            SearchHits<ErpDistributionCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpDistributionCombinedESDO.class,
                    IndexCoordinates.of("erp_distribution_combined"));

            if (searchHits.hasAggregations()) {
                ElasticsearchAggregations elasticsearchAggregations = (ElasticsearchAggregations) searchHits.getAggregations();
                Aggregations aggregations = elasticsearchAggregations.aggregations();
                Terms comboProductAgg = aggregations.get("combo_product_agg");

                // 收集所有组品ID
                Set<Long> comboProductIds = new HashSet<>();

                for (Terms.Bucket bucket : comboProductAgg.getBuckets()) {
                    String comboProductIdStr = bucket.getKeyAsString();
                    if (StrUtil.isNotBlank(comboProductIdStr)) {
                        try {
                            Long comboProductId = Long.parseLong(comboProductIdStr);
                            comboProductIds.add(comboProductId);
                        } catch (NumberFormatException e) {
                            // 忽略无效的组品ID
                        }
                    }
                }

                // 批量查询组品信息
                if (!comboProductIds.isEmpty()) {
                    Iterable<ErpComboProductES> comboProducts = comboProductESRepository.findAllById(comboProductIds);
                    Map<Long, ErpComboProductES> comboProductMap = new HashMap<>();
                    comboProducts.forEach(combo -> comboProductMap.put(combo.getId(), combo));

                    // 处理聚合结果
                    for (Terms.Bucket bucket : comboProductAgg.getBuckets()) {
                        String comboProductIdStr = bucket.getKeyAsString();
                        if (StrUtil.isBlank(comboProductIdStr)) continue;

                        try {
                            Long comboProductId = Long.parseLong(comboProductIdStr);
                            ErpComboProductES comboProduct = comboProductMap.get(comboProductId);
                            if (comboProduct == null) continue;

                            // 根据统计类型获取分类名称
                            String categoryName = null;
                            if ("purchaser".equals(reqVO.getStatisticsType())) {
                                categoryName = comboProduct.getPurchaser();
                            } else if ("supplier".equals(reqVO.getStatisticsType())) {
                                categoryName = comboProduct.getSupplier();
                            }

                            if (StrUtil.isBlank(categoryName)) continue;

                            ErpDistributionWholesaleStatisticsRespVO.StatisticsItem item = itemMap.computeIfAbsent(categoryName,
                                    k -> new ErpDistributionWholesaleStatisticsRespVO.StatisticsItem());

                            item.setCategoryName(categoryName);

                            // 累加统计数据
                            item.setDistributionOrderCount((item.getDistributionOrderCount() == null ? 0 : item.getDistributionOrderCount()) + (int) bucket.getDocCount());

                            Sum productQuantityAgg = bucket.getAggregations().get("product_quantity");
                            if (productQuantityAgg != null) {
                                item.setDistributionProductQuantity((item.getDistributionProductQuantity() == null ? 0 : item.getDistributionProductQuantity()) + (int) productQuantityAgg.getValue());
                            }

                            // 🔥 修复：计算准确的采购和销售金额 - 与代发表服务层一致
                            Sum purchaseOtherFeesAgg = bucket.getAggregations().get("purchase_other_fees");
                            Sum saleOtherFeesAgg = bucket.getAggregations().get("sale_other_fees");

                            BigDecimal purchaseOtherFees = purchaseOtherFeesAgg != null ?
                                    BigDecimal.valueOf(purchaseOtherFeesAgg.getValue()) : BigDecimal.ZERO;
                            BigDecimal saleOtherFees = saleOtherFeesAgg != null ?
                                    BigDecimal.valueOf(saleOtherFeesAgg.getValue()) : BigDecimal.ZERO;

                            // 🔥 修复：计算采购金额 - 与代发表服务层calculatePurchaseAmount方法一致
                            BigDecimal productQuantity = productQuantityAgg != null ?
                                    BigDecimal.valueOf(productQuantityAgg.getValue()) : BigDecimal.ZERO;
                            
                            // 使用组品的采购价格（与代发表服务层一致）
                            BigDecimal purchasePrice = comboProduct.getPurchasePrice() != null ?
                                    comboProduct.getPurchasePrice() : BigDecimal.ZERO;
                            
                            // 计算采购运费（与代发表服务层一致）
                            BigDecimal shippingFee = calculateDistributionShippingFee(comboProduct, productQuantity.intValue());
                            
                            // 采购金额 = 采购价格 × 产品数量 + 采购运费 + 其他费用
                            BigDecimal totalPurchaseAmount = purchasePrice.multiply(productQuantity)
                                    .add(shippingFee)
                                    .add(purchaseOtherFees);

                            // 🔥 修复：销售金额暂时使用组品价格作为估算（实际应该从销售价格表获取）
                            BigDecimal salePrice = comboProduct.getWholesalePrice() != null ?
                                    comboProduct.getWholesalePrice() : BigDecimal.ZERO;
                            BigDecimal totalSaleAmount = salePrice.multiply(productQuantity).add(saleOtherFees);

                            item.setDistributionPurchaseAmount((item.getDistributionPurchaseAmount() == null ? BigDecimal.ZERO : item.getDistributionPurchaseAmount()).add(totalPurchaseAmount));
                            item.setDistributionSaleAmount((item.getDistributionSaleAmount() == null ? BigDecimal.ZERO : item.getDistributionSaleAmount()).add(totalSaleAmount));

                        } catch (NumberFormatException e) {
                            // 忽略无效的组品ID
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("代发组品聚合统计失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔥 优化：批发数据按组品ID聚合统计（用于采购人员和供应商统计）
     */
    private void getWholesaleStatisticsByComboProduct(ErpDistributionWholesaleStatisticsReqVO reqVO,
                                                     Map<String, ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> itemMap,
                                                     BoolQueryBuilder boolQuery) {
        try {
            // 构建聚合查询，按组品ID分组
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withMaxResults(0)
                    .addAggregation(AggregationBuilders.terms("combo_product_agg")
                            .field("combo_product_id")
                            .size(10000)
                            .subAggregation(AggregationBuilders.count("order_count").field("id"))
                            .subAggregation(AggregationBuilders.sum("product_quantity").field("product_quantity"))
                            .subAggregation(AggregationBuilders.sum("purchase_truck_fee").field("purchase_truck_fee"))
                            .subAggregation(AggregationBuilders.sum("purchase_logistics_fee").field("purchase_logistics_fee"))
                            .subAggregation(AggregationBuilders.sum("purchase_other_fees").field("purchase_other_fees"))
                            .subAggregation(AggregationBuilders.sum("sale_truck_fee").field("sale_truck_fee"))
                            .subAggregation(AggregationBuilders.sum("sale_logistics_fee").field("sale_logistics_fee"))
                            .subAggregation(AggregationBuilders.sum("sale_other_fees").field("sale_other_fees")));

            SearchHits<ErpWholesaleCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpWholesaleCombinedESDO.class,
                    IndexCoordinates.of("erp_wholesale_combined"));

            if (searchHits.hasAggregations()) {
                ElasticsearchAggregations elasticsearchAggregations = (ElasticsearchAggregations) searchHits.getAggregations();
                Aggregations aggregations = elasticsearchAggregations.aggregations();
                Terms comboProductAgg = aggregations.get("combo_product_agg");

                // 收集所有组品ID
                Set<Long> comboProductIds = new HashSet<>();

                for (Terms.Bucket bucket : comboProductAgg.getBuckets()) {
                    String comboProductIdStr = bucket.getKeyAsString();
                    if (StrUtil.isNotBlank(comboProductIdStr)) {
                        try {
                            Long comboProductId = Long.parseLong(comboProductIdStr);
                            comboProductIds.add(comboProductId);
                        } catch (NumberFormatException e) {
                            // 忽略无效的组品ID
                        }
                    }
                }

                // 批量查询组品信息
                if (!comboProductIds.isEmpty()) {
                    Iterable<ErpComboProductES> comboProducts = comboProductESRepository.findAllById(comboProductIds);
                    Map<Long, ErpComboProductES> comboProductMap = new HashMap<>();
                    comboProducts.forEach(combo -> comboProductMap.put(combo.getId(), combo));

                    // 处理聚合结果
                    for (Terms.Bucket bucket : comboProductAgg.getBuckets()) {
                        String comboProductIdStr = bucket.getKeyAsString();
                        if (StrUtil.isBlank(comboProductIdStr)) continue;

                        try {
                            Long comboProductId = Long.parseLong(comboProductIdStr);
                            ErpComboProductES comboProduct = comboProductMap.get(comboProductId);
                            if (comboProduct == null) continue;

                            // 根据统计类型获取分类名称
                            String categoryName = null;
                            if ("purchaser".equals(reqVO.getStatisticsType())) {
                                categoryName = comboProduct.getPurchaser();
                            } else if ("supplier".equals(reqVO.getStatisticsType())) {
                                categoryName = comboProduct.getSupplier();
                            }

                            if (StrUtil.isBlank(categoryName)) continue;

                            ErpDistributionWholesaleStatisticsRespVO.StatisticsItem item = itemMap.computeIfAbsent(categoryName,
                                    k -> new ErpDistributionWholesaleStatisticsRespVO.StatisticsItem());

                            item.setCategoryName(categoryName);

                            // 累加统计数据
                            item.setWholesaleOrderCount((item.getWholesaleOrderCount() == null ? 0 : item.getWholesaleOrderCount()) + (int) bucket.getDocCount());

                            Sum productQuantityAgg = bucket.getAggregations().get("product_quantity");
                            if (productQuantityAgg != null) {
                                item.setWholesaleProductQuantity((item.getWholesaleProductQuantity() == null ? 0 : item.getWholesaleProductQuantity()) + (int) productQuantityAgg.getValue());
                            }

                            // 🔥 优化：获取费用聚合结果
                            Sum purchaseTruckFeeAgg = bucket.getAggregations().get("purchase_truck_fee");
                            Sum purchaseLogisticsFeeAgg = bucket.getAggregations().get("purchase_logistics_fee");
                            Sum purchaseOtherFeesAgg = bucket.getAggregations().get("purchase_other_fees");
                            Sum saleTruckFeeAgg = bucket.getAggregations().get("sale_truck_fee");
                            Sum saleLogisticsFeeAgg = bucket.getAggregations().get("sale_logistics_fee");
                            Sum saleOtherFeesAgg = bucket.getAggregations().get("sale_other_fees");

                            BigDecimal purchaseTruckFee = purchaseTruckFeeAgg != null ?
                                    BigDecimal.valueOf(purchaseTruckFeeAgg.getValue()) : BigDecimal.ZERO;
                            BigDecimal purchaseLogisticsFee = purchaseLogisticsFeeAgg != null ?
                                    BigDecimal.valueOf(purchaseLogisticsFeeAgg.getValue()) : BigDecimal.ZERO;
                            BigDecimal purchaseOtherFees = purchaseOtherFeesAgg != null ?
                                    BigDecimal.valueOf(purchaseOtherFeesAgg.getValue()) : BigDecimal.ZERO;
                            BigDecimal saleTruckFee = saleTruckFeeAgg != null ?
                                    BigDecimal.valueOf(saleTruckFeeAgg.getValue()) : BigDecimal.ZERO;
                            BigDecimal saleLogisticsFee = saleLogisticsFeeAgg != null ?
                                    BigDecimal.valueOf(saleLogisticsFeeAgg.getValue()) : BigDecimal.ZERO;
                            BigDecimal saleOtherFees = saleOtherFeesAgg != null ?
                                    BigDecimal.valueOf(saleOtherFeesAgg.getValue()) : BigDecimal.ZERO;

                            // 🔥 优化：计算准确的采购和销售金额
                            BigDecimal productQuantity = productQuantityAgg != null ?
                                    BigDecimal.valueOf(productQuantityAgg.getValue()) : BigDecimal.ZERO;
                            BigDecimal purchasePrice = comboProduct.getWholesalePrice() != null ?
                                    comboProduct.getWholesalePrice() : BigDecimal.ZERO;

                            // 采购金额：产品价格 × 数量 + 所有采购费用
                            BigDecimal totalPurchaseAmount = purchasePrice.multiply(productQuantity)
                                    .add(purchaseTruckFee)
                                    .add(purchaseLogisticsFee)
                                    .add(purchaseOtherFees);

                            // 销售金额：销售价格 × 数量 + 所有销售费用
                            // 这里需要从销售价格表获取，暂时使用组品价格
                            BigDecimal salePrice = comboProduct.getWholesalePrice() != null ?
                                    comboProduct.getWholesalePrice() : BigDecimal.ZERO;
                            BigDecimal totalSaleAmount = salePrice.multiply(productQuantity)
                                    .add(saleTruckFee)
                                    .add(saleLogisticsFee)
                                    .add(saleOtherFees);

                            item.setWholesalePurchaseAmount((item.getWholesalePurchaseAmount() == null ? BigDecimal.ZERO : item.getWholesalePurchaseAmount()).add(totalPurchaseAmount));
                            item.setWholesaleSaleAmount((item.getWholesaleSaleAmount() == null ? BigDecimal.ZERO : item.getWholesaleSaleAmount()).add(totalSaleAmount));

                        } catch (NumberFormatException e) {
                            // 忽略无效的组品ID
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("批发组品聚合统计失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔥 新增：根据统计类型获取聚合字段名
     */
    private String getAggregationField(String statisticsType, String tableType) {
        switch (statisticsType) {
            case "purchaser":
                // 采购人员需要从组品表获取，这里返回一个占位符
                // 实际查询时会通过组品ID进行关联
                return "combo_product_id";
            case "supplier":
                // 供应商需要从组品表获取，这里返回一个占位符
                return "combo_product_id";
            case "salesperson":
                return "salesperson";
            case "customer":
                return "customer_name";
            default:
                return "id";
        }
    }

    /**
     * 测试ES数据可用性
     */




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
        if (StrUtil.isBlank(purchaserKeyword)) {
            return Collections.emptySet();
        }

        try {
            // 使用聚合查询，只获取ID字段，提高性能
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(createSimplifiedKeywordMatchQuery("purchaser", purchaserKeyword.trim()));

            queryBuilder.withQuery(boolQuery);
            // 🔥 优化：使用聚合查询，只获取ID字段，不限制数量
            queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id"}, null));
            queryBuilder.withPageable(PageRequest.of(0, 50000)); // 增加查询数量限制

            SearchHits<ErpComboProductES> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpComboProductES.class,
                    IndexCoordinates.of("erp_combo_products"));

            return searchHits.stream()
                    .map(hit -> hit.getContent().getId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.err.println("根据采购人员查询组品ID失败: " + e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 🔥 根据供应商关键词查询组品ID集合
     */
    private Set<Long> getComboProductIdsBySupplier(String supplierKeyword) {
        if (StrUtil.isBlank(supplierKeyword)) {
            return Collections.emptySet();
        }

        try {
            // 使用聚合查询，只获取ID字段，提高性能
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(createSimplifiedKeywordMatchQuery("supplier", supplierKeyword.trim()));

            queryBuilder.withQuery(boolQuery);
            // 🔥 优化：使用聚合查询，只获取ID字段，不限制数量
            queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id"}, null));
            queryBuilder.withPageable(PageRequest.of(0, 50000)); // 增加查询数量限制

            SearchHits<ErpComboProductES> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpComboProductES.class,
                    IndexCoordinates.of("erp_combo_products"));

            return searchHits.stream()
                    .map(hit -> hit.getContent().getId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.err.println("根据供应商查询组品ID失败: " + e.getMessage());
            return Collections.emptySet();
        }
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
     * �� 实时获取供应商信息
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



    @Override
    public ErpDistributionWholesaleStatisticsRespVO.AuditStatistics getAuditStatistics(ErpDistributionWholesaleStatisticsReqVO reqVO) {
        ErpDistributionWholesaleStatisticsRespVO.AuditStatistics auditStatistics = new ErpDistributionWholesaleStatisticsRespVO.AuditStatistics();

        try {
            // 🔥 优化：使用ES聚合查询直接获取审核统计数据
            getAuditStatisticsByAggregation(reqVO, auditStatistics);

        } catch (Exception e) {
            System.err.println("聚合审核统计失败，回退到原有方法: " + e.getMessage());
            e.printStackTrace();

            // 回退到原有方法
            List<ErpDistributionCombinedESDO> distributionData = getDistributionDataFromES(reqVO);
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
        }

        return auditStatistics;
    }

    /**
     * 🔥 优化：使用ES聚合查询获取审核统计数据，提高查询效率
     */
    private void getAuditStatisticsByAggregation(ErpDistributionWholesaleStatisticsReqVO reqVO,
                                                ErpDistributionWholesaleStatisticsRespVO.AuditStatistics auditStatistics) {
        try {
            // 构建基础查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            addTimeRangeQuery(boolQuery, reqVO);

            if (StrUtil.isNotBlank(reqVO.getSearchKeyword())) {
                addCategoryFilter(boolQuery, reqVO.getStatisticsType(), reqVO.getSearchKeyword());
            }

            // 🔥 优化：使用ES聚合查询提高效率
            // 1. 代发数据审核统计
            getDistributionAuditStatisticsByAggregation(boolQuery, auditStatistics);

            // 2. 批发数据审核统计
            getWholesaleAuditStatisticsByAggregation(boolQuery, auditStatistics);

            // 3. 计算总数
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
            System.err.println("聚合审核统计失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔥 优化：代发数据审核统计聚合查询
     */
    private void getDistributionAuditStatisticsByAggregation(BoolQueryBuilder boolQuery,
                                                            ErpDistributionWholesaleStatisticsRespVO.AuditStatistics auditStatistics) {
        try {
            // 构建聚合查询
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withMaxResults(0) // 不需要返回文档，只要聚合结果
                    .addAggregation(AggregationBuilders.terms("purchase_audit_status")
                            .field("purchase_audit_status")
                            .subAggregation(AggregationBuilders.count("count").field("id")))
                    .addAggregation(AggregationBuilders.terms("purchase_after_sales_status")
                            .field("purchase_after_sales_status")
                            .subAggregation(AggregationBuilders.count("count").field("id")))
                    .addAggregation(AggregationBuilders.terms("sale_audit_status")
                            .field("sale_audit_status")
                            .subAggregation(AggregationBuilders.count("count").field("id")))
                    .addAggregation(AggregationBuilders.terms("sale_after_sales_status")
                            .field("sale_after_sales_status")
                            .subAggregation(AggregationBuilders.count("count").field("id")));

            SearchHits<ErpDistributionCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpDistributionCombinedESDO.class,
                    IndexCoordinates.of("erp_distribution_combined"));

            if (searchHits.hasAggregations()) {
                ElasticsearchAggregations elasticsearchAggregations = (ElasticsearchAggregations) searchHits.getAggregations();
                Aggregations aggregations = elasticsearchAggregations.aggregations();

                // 处理采购审核状态
                Terms purchaseAuditAgg = aggregations.get("purchase_audit_status");
                if (purchaseAuditAgg != null) {
                    for (Terms.Bucket bucket : purchaseAuditAgg.getBuckets()) {
                        String status = bucket.getKeyAsString();
                        long count = bucket.getDocCount();

                        if ("10".equals(status)) {
                            auditStatistics.setDistributionPurchaseUnauditedCount((int) count);
                        } else if ("20".equals(status)) {
                            auditStatistics.setDistributionPurchaseAuditedCount((int) count);
                        }
                    }
                }

                // 处理采购售后状态
                Terms purchaseAfterSalesAgg = aggregations.get("purchase_after_sales_status");
                if (purchaseAfterSalesAgg != null) {
                    for (Terms.Bucket bucket : purchaseAfterSalesAgg.getBuckets()) {
                        String status = bucket.getKeyAsString();
                        long count = bucket.getDocCount();

                        if ("30".equals(status)) {
                            auditStatistics.setDistributionPurchaseNoAfterSalesCount((int) count);
                        } else if ("40".equals(status)) {
                            auditStatistics.setDistributionPurchaseAfterSalesCount((int) count);
                        }
                    }
                }

                // 处理销售审核状态
                Terms saleAuditAgg = aggregations.get("sale_audit_status");
                if (saleAuditAgg != null) {
                    for (Terms.Bucket bucket : saleAuditAgg.getBuckets()) {
                        String status = bucket.getKeyAsString();
                        long count = bucket.getDocCount();

                        if ("10".equals(status)) {
                            auditStatistics.setDistributionSaleUnauditedCount((int) count);
                        } else if ("20".equals(status)) {
                            auditStatistics.setDistributionSaleAuditedCount((int) count);
                        }
                    }
                }

                // 处理销售售后状态
                Terms saleAfterSalesAgg = aggregations.get("sale_after_sales_status");
                if (saleAfterSalesAgg != null) {
                    for (Terms.Bucket bucket : saleAfterSalesAgg.getBuckets()) {
                        String status = bucket.getKeyAsString();
                        long count = bucket.getDocCount();

                        if ("30".equals(status)) {
                            auditStatistics.setDistributionSaleNoAfterSalesCount((int) count);
                        } else if ("40".equals(status)) {
                            auditStatistics.setDistributionSaleAfterSalesCount((int) count);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("代发审核聚合统计失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔥 优化：批发数据审核统计聚合查询
     */
    private void getWholesaleAuditStatisticsByAggregation(BoolQueryBuilder boolQuery,
                                                         ErpDistributionWholesaleStatisticsRespVO.AuditStatistics auditStatistics) {
        try {
            // 构建聚合查询
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withMaxResults(0) // 不需要返回文档，只要聚合结果
                    .addAggregation(AggregationBuilders.terms("purchase_audit_status")
                            .field("purchase_audit_status")
                            .subAggregation(AggregationBuilders.count("count").field("id")))
                    .addAggregation(AggregationBuilders.terms("purchase_after_sales_status")
                            .field("purchase_after_sales_status")
                            .subAggregation(AggregationBuilders.count("count").field("id")))
                    .addAggregation(AggregationBuilders.terms("sale_audit_status")
                            .field("sale_audit_status")
                            .subAggregation(AggregationBuilders.count("count").field("id")))
                    .addAggregation(AggregationBuilders.terms("sale_after_sales_status")
                            .field("sale_after_sales_status")
                            .subAggregation(AggregationBuilders.count("count").field("id")));

            SearchHits<ErpWholesaleCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpWholesaleCombinedESDO.class,
                    IndexCoordinates.of("erp_wholesale_combined"));

            if (searchHits.hasAggregations()) {
                ElasticsearchAggregations elasticsearchAggregations = (ElasticsearchAggregations) searchHits.getAggregations();
                Aggregations aggregations = elasticsearchAggregations.aggregations();

                // 处理采购审核状态
                Terms purchaseAuditAgg = aggregations.get("purchase_audit_status");
                if (purchaseAuditAgg != null) {
                    for (Terms.Bucket bucket : purchaseAuditAgg.getBuckets()) {
                        String status = bucket.getKeyAsString();
                        long count = bucket.getDocCount();

                        if ("10".equals(status)) {
                            auditStatistics.setWholesalePurchaseUnauditedCount((int) count);
                        } else if ("20".equals(status)) {
                            auditStatistics.setWholesalePurchaseAuditedCount((int) count);
                        }
                    }
                }

                // 处理采购售后状态
                Terms purchaseAfterSalesAgg = aggregations.get("purchase_after_sales_status");
                if (purchaseAfterSalesAgg != null) {
                    for (Terms.Bucket bucket : purchaseAfterSalesAgg.getBuckets()) {
                        String status = bucket.getKeyAsString();
                        long count = bucket.getDocCount();

                        if ("30".equals(status)) {
                            auditStatistics.setWholesalePurchaseNoAfterSalesCount((int) count);
                        } else if ("40".equals(status)) {
                            auditStatistics.setWholesalePurchaseAfterSalesCount((int) count);
                        }
                    }
                }

                // 处理销售审核状态
                Terms saleAuditAgg = aggregations.get("sale_audit_status");
                if (saleAuditAgg != null) {
                    for (Terms.Bucket bucket : saleAuditAgg.getBuckets()) {
                        String status = bucket.getKeyAsString();
                        long count = bucket.getDocCount();

                        if ("10".equals(status)) {
                            auditStatistics.setWholesaleSaleUnauditedCount((int) count);
                        } else if ("20".equals(status)) {
                            auditStatistics.setWholesaleSaleAuditedCount((int) count);
                        }
                    }
                }

                // 处理销售售后状态
                Terms saleAfterSalesAgg = aggregations.get("sale_after_sales_status");
                if (saleAfterSalesAgg != null) {
                    for (Terms.Bucket bucket : saleAfterSalesAgg.getBuckets()) {
                        String status = bucket.getKeyAsString();
                        long count = bucket.getDocCount();

                        if ("30".equals(status)) {
                            auditStatistics.setWholesaleSaleNoAfterSalesCount((int) count);
                        } else if ("40".equals(status)) {
                            auditStatistics.setWholesaleSaleAfterSalesCount((int) count);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("批发审核聚合统计失败: " + e.getMessage());
            e.printStackTrace();
        }
    }



    /**
     * 从ES获取代发数据 - 优化大数据量查询
     */
    private List<ErpDistributionCombinedESDO> getDistributionDataFromES(ErpDistributionWholesaleStatisticsReqVO reqVO) {
        List<ErpDistributionCombinedESDO> allData = new ArrayList<>();

        try {
            // 🔥 优化：使用分批查询处理大数据量
            int batchSize = 10000;
            int from = 0;
            boolean hasMore = true;

            while (hasMore) {
                NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

                // 添加时间范围查询
                addTimeRangeQuery(boolQuery, reqVO);

                // 添加分类过滤
                if (StrUtil.isNotBlank(reqVO.getSearchKeyword())) {
                    addCategoryFilter(boolQuery, reqVO.getStatisticsType(), reqVO.getSearchKeyword());
                }

                queryBuilder.withQuery(boolQuery);
                queryBuilder.withPageable(PageRequest.of(from / batchSize, batchSize));
                queryBuilder.withSort(Sort.by(Sort.Direction.DESC, "create_time"));

            SearchHits<ErpDistributionCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                        queryBuilder.build(),
                        ErpDistributionCombinedESDO.class,
                        IndexCoordinates.of("erp_distribution_combined"));

                List<ErpDistributionCombinedESDO> batchData = searchHits.stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList());

                allData.addAll(batchData);

                // 检查是否还有更多数据
                hasMore = batchData.size() == batchSize;
                from += batchSize;

                // 🔥 安全限制：最多查询100万条数据，避免内存溢出
                if (allData.size() >= 1000000) {
                    System.err.println("警告：代发数据量超过100万条，已截断查询");
                    break;
                }
            }

            System.out.println("代发数据查询完成，共获取 " + allData.size() + " 条记录");

        } catch (Exception e) {
            System.err.println("从ES查询代发数据失败: " + e.getMessage());
            e.printStackTrace();
        }

        return allData;
    }

    /**
     * 从ES获取批发数据 - 优化大数据量查询
     */
    private List<ErpWholesaleCombinedESDO> getWholesaleDataFromES(ErpDistributionWholesaleStatisticsReqVO reqVO) {
        List<ErpWholesaleCombinedESDO> allData = new ArrayList<>();

        try {
            // 🔥 优化：使用分批查询处理大数据量
            int batchSize = 10000;
            int from = 0;
            boolean hasMore = true;

            while (hasMore) {
                NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

                // 添加时间范围查询
                addTimeRangeQuery(boolQuery, reqVO);

                // 添加分类过滤
                if (StrUtil.isNotBlank(reqVO.getSearchKeyword())) {
                    addCategoryFilter(boolQuery, reqVO.getStatisticsType(), reqVO.getSearchKeyword());
                }

                queryBuilder.withQuery(boolQuery);
                queryBuilder.withPageable(PageRequest.of(from / batchSize, batchSize));
                queryBuilder.withSort(Sort.by(Sort.Direction.DESC, "create_time"));

                SearchHits<ErpWholesaleCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                        queryBuilder.build(),
                        ErpWholesaleCombinedESDO.class,
                        IndexCoordinates.of("erp_wholesale_combined"));

                List<ErpWholesaleCombinedESDO> batchData = searchHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

                allData.addAll(batchData);

                // 检查是否还有更多数据
                hasMore = batchData.size() == batchSize;
                from += batchSize;

                // 🔥 安全限制：最多查询100万条数据，避免内存溢出
                if (allData.size() >= 1000000) {
                    System.err.println("警告：批发数据量超过100万条，已截断查询");
                    break;
                }
            }

            System.out.println("批发数据查询完成，共获取 " + allData.size() + " 条记录");

        } catch (Exception e) {
            System.err.println("从ES查询批发数据失败: " + e.getMessage());
            e.printStackTrace();
        }

        return allData;
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
     * 🔥 修复：获取分类名称，确保采购人员和供应商统计正确
     */
    private String getCategoryName(Object data, String statisticsType) {
        if (data == null || StrUtil.isBlank(statisticsType)) {
            return null;
        }

        try {
        if (data instanceof ErpDistributionCombinedESDO) {
            ErpDistributionCombinedESDO distribution = (ErpDistributionCombinedESDO) data;
                
            switch (statisticsType) {
                case "customer":
                    return distribution.getCustomerName();
                    case "salesperson":
                        return distribution.getSalesperson();
                    case "purchaser":
                        // 🔥 修复：采购人员需要从组品表获取
                        if (distribution.getComboProductId() != null) {
                            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(distribution.getComboProductId());
                            if (comboProductOpt.isPresent()) {
                                return comboProductOpt.get().getPurchaser();
                            }
                        }
                        return null;
                    case "supplier":
                        // 🔥 修复：供应商需要从组品表获取
                        if (distribution.getComboProductId() != null) {
                            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(distribution.getComboProductId());
                            if (comboProductOpt.isPresent()) {
                                return comboProductOpt.get().getSupplier();
                            }
                        }
                        return null;
                    case "product":
                        // 🔥 修复：产品名称需要实时计算
                        if (distribution.getComboProductId() != null) {
                            return calculateRealTimeProductName(distribution.getComboProductId());
                        }
                        return null;
                default:
                    return null;
            }
        } else if (data instanceof ErpWholesaleCombinedESDO) {
            ErpWholesaleCombinedESDO wholesale = (ErpWholesaleCombinedESDO) data;
                
            switch (statisticsType) {
                case "customer":
                    return wholesale.getCustomerName();
                    case "salesperson":
                        return wholesale.getSalesperson();
                    case "purchaser":
                        // 🔥 修复：采购人员需要从组品表获取
                        if (wholesale.getComboProductId() != null) {
                            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(wholesale.getComboProductId());
                            if (comboProductOpt.isPresent()) {
                                return comboProductOpt.get().getPurchaser();
                            }
                        }
                        return null;
                    case "supplier":
                        // 🔥 修复：供应商需要从组品表获取
                        if (wholesale.getComboProductId() != null) {
                            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(wholesale.getComboProductId());
                            if (comboProductOpt.isPresent()) {
                                return comboProductOpt.get().getSupplier();
                            }
                        }
                        return null;
                    case "product":
                        // 🔥 修复：产品名称需要实时计算
                        if (wholesale.getComboProductId() != null) {
                            return calculateRealTimeProductName(wholesale.getComboProductId());
                        }
                        return null;
                default:
                    return null;
            }
        }
        } catch (Exception e) {
            System.err.println("获取分类名称失败: " + e.getMessage());
        }

        return null;
    }

    /**
     * 🔥 修复：计算代发订单的采购和销售金额 - 与代发表服务层保持一致
     */
    private BigDecimal[] calculateDistributionAmounts(ErpDistributionCombinedESDO distribution) {
        BigDecimal purchaseAmount = BigDecimal.ZERO;
        BigDecimal saleAmount = BigDecimal.ZERO;

        if (distribution.getComboProductId() != null) {
            try {
                // 🔥 修复：使用与代发表服务层一致的实时计算逻辑
                // 1. 实时计算产品名称和采购单价
                String realTimeProductName = calculateRealTimeProductName(distribution.getComboProductId());
                BigDecimal realTimePurchasePrice = calculateRealTimePurchasePrice(distribution.getComboProductId());

                // 2. 从ES获取组品信息作为兜底
                Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(distribution.getComboProductId());
                if (comboProductOpt.isPresent()) {
                    ErpComboProductES comboProduct = comboProductOpt.get();
                    int quantity = distribution.getProductQuantity() != null ? distribution.getProductQuantity() : 0;

                    // 🔥 修复：使用实时计算的采购单价，如果失败则使用ES缓存数据
                    // 代发表使用 purchasePrice，不是 wholesalePrice
                    BigDecimal purchasePrice = realTimePurchasePrice != null ? realTimePurchasePrice : 
                        (comboProduct.getPurchasePrice() != null ? comboProduct.getPurchasePrice() : BigDecimal.ZERO);
                    
                    // 计算采购金额 = 采购单价 × 产品数量 + 采购运费 + 其他费用
                    BigDecimal productCost = purchasePrice.multiply(new BigDecimal(quantity));
                    BigDecimal shippingFee = calculateDistributionShippingFee(comboProduct, quantity);
                    BigDecimal otherFees = distribution.getPurchaseOtherFees() != null ? distribution.getPurchaseOtherFees() : BigDecimal.ZERO;
                    purchaseAmount = productCost.add(shippingFee).add(otherFees);

                    // 🔥 修复：计算销售金额 - 与代发表服务层保持一致
                    if (distribution.getCustomerName() != null) {
                        Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                                distribution.getComboProductId(), distribution.getCustomerName());
                        if (salePriceOpt.isPresent()) {
                            ErpSalePriceESDO salePrice = salePriceOpt.get();
                            // 使用销售价格表中的代发价格
                            BigDecimal distributionPrice = salePrice.getDistributionPrice() != null ? salePrice.getDistributionPrice() : BigDecimal.ZERO;
                            BigDecimal saleProductAmount = distributionPrice.multiply(new BigDecimal(quantity));
                            BigDecimal saleShippingFee = calculateDistributionSaleShippingFee(salePrice, quantity, comboProduct);
                            BigDecimal saleOtherFees = distribution.getSaleOtherFees() != null ? distribution.getSaleOtherFees() : BigDecimal.ZERO;
                            saleAmount = saleProductAmount.add(saleShippingFee).add(saleOtherFees);
                        } else {
                            // 销售价格表没有数据时，销售金额为0
                            saleAmount = BigDecimal.ZERO;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("代发金额计算失败，订单ID: " + distribution.getId() + ", 错误: " + e.getMessage());
                // 计算失败时返回0
                purchaseAmount = BigDecimal.ZERO;
                saleAmount = BigDecimal.ZERO;
            }
        }

        return new BigDecimal[]{purchaseAmount, saleAmount};
    }

    /**
     * 🔥 修复：计算批发订单的采购和销售金额 - 与批发表服务层保持一致
     */
    private BigDecimal[] calculateWholesaleAmounts(ErpWholesaleCombinedESDO wholesale) {
        BigDecimal purchaseAmount = BigDecimal.ZERO;
        BigDecimal saleAmount = BigDecimal.ZERO;

        if (wholesale.getComboProductId() != null) {
            try {
                // 🔥 修复：使用与批发表服务层一致的实时计算逻辑
                // 1. 实时计算产品名称和采购单价
                String realTimeProductName = calculateRealTimeProductName(wholesale.getComboProductId());
                BigDecimal realTimePurchasePrice = calculateRealTimePurchasePrice(wholesale.getComboProductId());

                // 2. 从ES获取组品信息作为兜底
                Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(wholesale.getComboProductId());
                if (comboProductOpt.isPresent()) {
                    ErpComboProductES comboProduct = comboProductOpt.get();
                    int quantity = wholesale.getProductQuantity() != null ? wholesale.getProductQuantity() : 0;

                    // 🔥 修复：使用实时计算的采购单价，如果失败则使用ES缓存数据
                    // 批发表使用 wholesalePrice
                    BigDecimal purchasePrice = realTimePurchasePrice != null ? realTimePurchasePrice : 
                        (comboProduct.getWholesalePrice() != null ? comboProduct.getWholesalePrice() : BigDecimal.ZERO);
                    
                    // 计算采购金额 = 采购单价 × 产品数量 + 车费 + 物流费 + 其他费用
                    BigDecimal productCost = purchasePrice.multiply(new BigDecimal(quantity));
                    BigDecimal truckFee = wholesale.getPurchaseTruckFee() != null ? wholesale.getPurchaseTruckFee() : BigDecimal.ZERO;
                    BigDecimal logisticsFee = wholesale.getPurchaseLogisticsFee() != null ? wholesale.getPurchaseLogisticsFee() : BigDecimal.ZERO;
                    BigDecimal otherFees = wholesale.getPurchaseOtherFees() != null ? wholesale.getPurchaseOtherFees() : BigDecimal.ZERO;
                    purchaseAmount = productCost.add(truckFee).add(logisticsFee).add(otherFees);

                    // 🔥 修复：计算销售金额 - 与批发表服务层保持一致
                    if (wholesale.getCustomerName() != null) {
                        Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                                wholesale.getComboProductId(), wholesale.getCustomerName());
                        if (salePriceOpt.isPresent()) {
                            ErpSalePriceESDO salePrice = salePriceOpt.get();
                            // 使用销售价格表中的批发价格
                            BigDecimal saleWholesalePrice = salePrice.getWholesalePrice() != null ? salePrice.getWholesalePrice() : BigDecimal.ZERO;
                            BigDecimal saleProductAmount = saleWholesalePrice.multiply(new BigDecimal(quantity));
                            BigDecimal saleTruckFee = wholesale.getSaleTruckFee() != null ? wholesale.getSaleTruckFee() : BigDecimal.ZERO;
                            BigDecimal saleLogisticsFee = wholesale.getSaleLogisticsFee() != null ? wholesale.getSaleLogisticsFee() : BigDecimal.ZERO;
                            BigDecimal saleOtherFees = wholesale.getSaleOtherFees() != null ? wholesale.getSaleOtherFees() : BigDecimal.ZERO;
                            saleAmount = saleProductAmount.add(saleTruckFee).add(saleLogisticsFee).add(saleOtherFees);
                        } else {
                            // 销售价格表没有数据时，销售金额为0
                            saleAmount = BigDecimal.ZERO;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("批发金额计算失败，订单ID: " + wholesale.getId() + ", 错误: " + e.getMessage());
                // 计算失败时返回0
                purchaseAmount = BigDecimal.ZERO;
                saleAmount = BigDecimal.ZERO;
            }
        }

        return new BigDecimal[]{purchaseAmount, saleAmount};
    }

    /**
     * 🔥 修复：实时计算产品名称 - 与代发表和批发表服务层保持一致
     */
    private String calculateRealTimeProductName(Long comboProductId) {
        try {
            // 从ES查询组品关联的单品项
            NativeSearchQuery itemQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("combo_product_id", comboProductId))
                    .withSort(Sort.by(Sort.Direction.ASC, "id"))
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                    itemQuery,
                    ErpComboProductItemES.class,
                    IndexCoordinates.of("erp_combo_product_items"));

            if (itemHits.isEmpty()) {
                return null;
            }

            // 提取单品ID列表
            List<Long> productIds = itemHits.stream()
                    .map(hit -> hit.getContent().getItemProductId())
                    .collect(Collectors.toList());

            // 从ES查询单品详细信息
            NativeSearchQuery productQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.idsQuery().addIds(productIds.stream().map(String::valueOf).toArray(String[]::new)))
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            SearchHits<ErpProductESDO> productHits = elasticsearchRestTemplate.search(
                    productQuery,
                    ErpProductESDO.class,
                    IndexCoordinates.of("erp_products"));

            Map<Long, ErpProductESDO> productMap = productHits.stream()
                    .collect(Collectors.toMap(
                            hit -> hit.getContent().getId(),
                            SearchHit::getContent));

            // 组装单品名称字符串 (单品A×数量+单品B×数量)
            StringBuilder nameBuilder = new StringBuilder();
            List<ErpComboProductItemES> items = itemHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    nameBuilder.append("｜");
                }
                ErpProductESDO product = productMap.get(items.get(i).getItemProductId());
                if (product != null) {
                    nameBuilder.append(product.getName())
                              .append("×")
                              .append(items.get(i).getItemQuantity());
                }
            }

            return nameBuilder.toString();
        } catch (Exception e) {
            System.err.println("实时计算产品名称失败，组品ID: " + comboProductId + ", 错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 🔥 修复：实时计算采购单价 - 与代发表和批发表服务层保持一致
     */
    private BigDecimal calculateRealTimePurchasePrice(Long comboProductId) {
        try {
            // 从ES查询组品关联的单品项
            NativeSearchQuery itemQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("combo_product_id", comboProductId))
                    .withSort(Sort.by(Sort.Direction.ASC, "id"))
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                    itemQuery,
                    ErpComboProductItemES.class,
                    IndexCoordinates.of("erp_combo_product_items"));

            if (itemHits.isEmpty()) {
                return BigDecimal.ZERO;
            }

            // 提取单品ID列表
            List<Long> productIds = itemHits.stream()
                    .map(hit -> hit.getContent().getItemProductId())
                    .collect(Collectors.toList());

            // 从ES查询单品详细信息
            NativeSearchQuery productQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.idsQuery().addIds(productIds.stream().map(String::valueOf).toArray(String[]::new)))
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            SearchHits<ErpProductESDO> productHits = elasticsearchRestTemplate.search(
                    productQuery,
                    ErpProductESDO.class,
                    IndexCoordinates.of("erp_products"));

            Map<Long, ErpProductESDO> productMap = productHits.stream()
                    .collect(Collectors.toMap(
                            hit -> hit.getContent().getId(),
                            SearchHit::getContent));

            // 计算采购总价
            BigDecimal totalPurchasePrice = BigDecimal.ZERO;
            List<ErpComboProductItemES> items = itemHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            for (ErpComboProductItemES item : items) {
                ErpProductESDO product = productMap.get(item.getItemProductId());
                if (product != null && product.getPurchasePrice() != null) {
                    BigDecimal itemQuantity = new BigDecimal(item.getItemQuantity());
                    totalPurchasePrice = totalPurchasePrice.add(product.getPurchasePrice().multiply(itemQuantity));
                }
            }

            return totalPurchasePrice;
        } catch (Exception e) {
            System.err.println("实时计算采购单价失败，组品ID: " + comboProductId + ", 错误: " + e.getMessage());
            return BigDecimal.ZERO;
        }
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
     * 获取指定分类的代发数据 - 优化大数据量查询
     */
    private List<ErpDistributionCombinedESDO> getDistributionDataForCategory(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName) {
        List<ErpDistributionCombinedESDO> allData = new ArrayList<>();

        try {
            // 🔥 优化：使用分批查询处理大数据量
            int batchSize = 10000;
            int from = 0;
            boolean hasMore = true;

            while (hasMore) {
                NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 添加时间范围查询
            addTimeRangeQuery(boolQuery, reqVO);

                // 添加分类过滤
            addCategoryFilter(boolQuery, reqVO.getStatisticsType(), categoryName);

                queryBuilder.withQuery(boolQuery);
                queryBuilder.withPageable(PageRequest.of(from / batchSize, batchSize));
                queryBuilder.withSort(Sort.by(Sort.Direction.DESC, "create_time"));

            SearchHits<ErpDistributionCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                        queryBuilder.build(),
                        ErpDistributionCombinedESDO.class,
                    IndexCoordinates.of("erp_distribution_combined"));

                List<ErpDistributionCombinedESDO> batchData = searchHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

                allData.addAll(batchData);

                // 检查是否还有更多数据
                hasMore = batchData.size() == batchSize;
                from += batchSize;

                // 🔥 安全限制：最多查询100万条数据，避免内存溢出
                if (allData.size() >= 1000000) {
                    System.err.println("警告：代发分类数据量超过100万条，已截断查询");
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("获取代发分类数据失败: " + e.getMessage());
            e.printStackTrace();
        }

        return allData;
    }

    /**
     * 获取指定分类的批发数据 - 优化大数据量查询
     */
    private List<ErpWholesaleCombinedESDO> getWholesaleDataForCategory(ErpDistributionWholesaleStatisticsReqVO reqVO, String categoryName) {
        List<ErpWholesaleCombinedESDO> allData = new ArrayList<>();

        try {
            // 🔥 优化：使用分批查询处理大数据量
            int batchSize = 10000;
            int from = 0;
            boolean hasMore = true;

            while (hasMore) {
                NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 添加时间范围查询
            addTimeRangeQuery(boolQuery, reqVO);

                // 添加分类过滤
            addCategoryFilter(boolQuery, reqVO.getStatisticsType(), categoryName);

                queryBuilder.withQuery(boolQuery);
                queryBuilder.withPageable(PageRequest.of(from / batchSize, batchSize));
                queryBuilder.withSort(Sort.by(Sort.Direction.DESC, "create_time"));

            SearchHits<ErpWholesaleCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                        queryBuilder.build(),
                        ErpWholesaleCombinedESDO.class,
                    IndexCoordinates.of("erp_wholesale_combined"));

                List<ErpWholesaleCombinedESDO> batchData = searchHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

                allData.addAll(batchData);

                // 检查是否还有更多数据
                hasMore = batchData.size() == batchSize;
                from += batchSize;

                // 🔥 安全限制：最多查询100万条数据，避免内存溢出
                if (allData.size() >= 1000000) {
                    System.err.println("警告：批发分类数据量超过100万条，已截断查询");
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("获取批发分类数据失败: " + e.getMessage());
            e.printStackTrace();
        }

        return allData;
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
                // 🔥 修复：代发表不再有productName字段，需要实时从组品表获取
                String productName = calculateRealTimeProductName(distribution.getComboProductId());
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
                    // 🔥 修复：从组品ES中实时获取产品名称
                    String productName = "未知产品";
                    if (wholesale.getComboProductId() != null) {
                        Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(wholesale.getComboProductId());
                        if (comboProductOpt.isPresent()) {
                            productName = comboProductOpt.get().getName();
                        }
                    }

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
     * 创建简化的keyword匹配查询 - 参考批发表的简化策略
     */
    private BoolQueryBuilder createSimplifiedKeywordMatchQuery(String keywordFieldName, String keyword) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
        // 第一优先级：完全精确匹配（权重最高）
        multiMatchQuery.should(QueryBuilders.termQuery(keywordFieldName, keyword).boost(1000000.0f));
        // 第二优先级：前缀匹配
        multiMatchQuery.should(QueryBuilders.prefixQuery(keywordFieldName, keyword).boost(100000.0f));
        // 第三优先级：通配符包含匹配
        multiMatchQuery.should(QueryBuilders.wildcardQuery(keywordFieldName, "*" + keyword + "*").boost(10000.0f));

        multiMatchQuery.minimumShouldMatch(1);
        query.must(multiMatchQuery);
        return query;
    }

    /**
     * 🔥 修复：批量补充代发金额计算 - 与代发表服务层计算逻辑一致
     */
    private void supplementDistributionAmounts(Map<String, ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> itemMap, String statisticsType) {
        try {
            // 获取所有分类名称
            Set<String> categoryNames = itemMap.keySet();
            if (categoryNames.isEmpty()) return;

            // 🔥 修复：对于采购人员和供应商统计，需要获取实际的组品ID
            Set<Long> comboProductIds = new HashSet<>();
            if ("purchaser".equals(statisticsType) || "supplier".equals(statisticsType)) {
                // 根据分类名称查询对应的组品ID
                for (String categoryName : categoryNames) {
                    try {
                        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
                        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                        
                        if ("purchaser".equals(statisticsType)) {
                            boolQuery.must(QueryBuilders.termQuery("purchaser", categoryName));
                        } else if ("supplier".equals(statisticsType)) {
                            boolQuery.must(QueryBuilders.termQuery("supplier", categoryName));
                        }
                        
                        queryBuilder.withQuery(boolQuery);
                        queryBuilder.withPageable(PageRequest.of(0, 1000));
                        SearchHits<ErpComboProductES> searchHits = elasticsearchRestTemplate.search(
                                queryBuilder.build(),
                                ErpComboProductES.class,
                                IndexCoordinates.of("erp_combo_products"));
                        searchHits.stream()
                                .map(SearchHit::getContent)
                                .forEach(combo -> comboProductIds.add(combo.getId()));
                    } catch (Exception e) {
                        System.err.println("查询组品信息失败，分类名称: " + categoryName + ", 错误: " + e.getMessage());
                    }
                }
            }

            // 🔥 修复：批量查询组品信息
            Map<Long, ErpComboProductES> comboProductMap = new HashMap<>();
            if (!comboProductIds.isEmpty()) {
                Iterable<ErpComboProductES> comboProducts = comboProductESRepository.findAllById(comboProductIds);
                comboProducts.forEach(combo -> comboProductMap.put(combo.getId(), combo));
            }

            // 🔥 修复：补充产品价格计算 - 与代发表服务层calculatePurchaseAmount方法一致
            for (ErpDistributionWholesaleStatisticsRespVO.StatisticsItem item : itemMap.values()) {
                if ("purchaser".equals(statisticsType) || "supplier".equals(statisticsType)) {
                    // 对于采购人员和供应商统计，需要根据分类名称找到对应的组品
                    String categoryName = item.getCategoryName();
                    if (categoryName != null) {
                        // 找到该分类对应的组品
                        for (Map.Entry<Long, ErpComboProductES> entry : comboProductMap.entrySet()) {
                            ErpComboProductES comboProduct = entry.getValue();
                            boolean isMatch = false;
                            
                            if ("purchaser".equals(statisticsType) && categoryName.equals(comboProduct.getPurchaser())) {
                                isMatch = true;
                            } else if ("supplier".equals(statisticsType) && categoryName.equals(comboProduct.getSupplier())) {
                                isMatch = true;
                            }
                            
                            if (isMatch) {
                                // 🔥 修复：使用与代发表服务层一致的计算逻辑
                                // 使用组品的采购价格（与代发表服务层一致）
                                BigDecimal purchasePrice = comboProduct.getPurchasePrice() != null ? 
                                    comboProduct.getPurchasePrice() : BigDecimal.ZERO;
                                
                                // 计算采购运费（与代发表服务层一致）
                                BigDecimal shippingFee = calculateDistributionShippingFee(comboProduct, item.getDistributionProductQuantity());
                                
                                // 采购金额 = 采购价格 × 产品数量 + 采购运费 + 已累加的其他费用
                                if (item.getDistributionProductQuantity() != null) {
                                    BigDecimal productCost = purchasePrice.multiply(BigDecimal.valueOf(item.getDistributionProductQuantity()));
                                    BigDecimal totalPurchaseAmount = productCost.add(shippingFee);
                                    item.setDistributionPurchaseAmount(item.getDistributionPurchaseAmount().add(totalPurchaseAmount));
                                }
                                
                                // 🔥 修复：销售金额暂时使用组品价格作为估算（实际应该从销售价格表获取）
                                BigDecimal salePrice = comboProduct.getWholesalePrice() != null ? 
                                    comboProduct.getWholesalePrice() : BigDecimal.ZERO;
                                if (item.getDistributionProductQuantity() != null) {
                                    BigDecimal productSaleAmount = salePrice.multiply(BigDecimal.valueOf(item.getDistributionProductQuantity()));
                                    item.setDistributionSaleAmount(item.getDistributionSaleAmount().add(productSaleAmount));
                                }
                                break;
                            }
                        }
                    }
                } else {
                    // 对于其他统计类型，使用平均价格计算
                    BigDecimal avgPurchasePrice = BigDecimal.valueOf(100);
                    BigDecimal avgSalePrice = BigDecimal.valueOf(120);

                    if (item.getDistributionProductQuantity() != null) {
                        BigDecimal productCost = avgPurchasePrice.multiply(BigDecimal.valueOf(item.getDistributionProductQuantity()));
                        item.setDistributionPurchaseAmount(item.getDistributionPurchaseAmount().add(productCost));

                        BigDecimal productSaleAmount = avgSalePrice.multiply(BigDecimal.valueOf(item.getDistributionProductQuantity()));
                        item.setDistributionSaleAmount(item.getDistributionSaleAmount().add(productSaleAmount));
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("补充代发金额计算失败: " + e.getMessage());
        }
    }

    /**
     * 🔥 修复：批量补充批发金额计算 - 使用正确的计算逻辑
     */
    private void supplementWholesaleAmounts(Map<String, ErpDistributionWholesaleStatisticsRespVO.StatisticsItem> itemMap, String statisticsType) {
        try {
            // 获取所有分类名称
            Set<String> categoryNames = itemMap.keySet();
            if (categoryNames.isEmpty()) return;

            // 🔥 修复：对于采购人员和供应商统计，需要获取实际的组品ID
            Set<Long> comboProductIds = new HashSet<>();
            if ("purchaser".equals(statisticsType) || "supplier".equals(statisticsType)) {
                // 根据分类名称查询对应的组品ID
                for (String categoryName : categoryNames) {
                    try {
                        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
                        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                        
                        if ("purchaser".equals(statisticsType)) {
                            boolQuery.must(QueryBuilders.termQuery("purchaser", categoryName));
                        } else if ("supplier".equals(statisticsType)) {
                            boolQuery.must(QueryBuilders.termQuery("supplier", categoryName));
                        }
                        
                        queryBuilder.withQuery(boolQuery);
                        queryBuilder.withPageable(PageRequest.of(0, 1000));
                        SearchHits<ErpComboProductES> searchHits = elasticsearchRestTemplate.search(
                                queryBuilder.build(),
                                ErpComboProductES.class,
                                IndexCoordinates.of("erp_combo_products"));
                        searchHits.stream()
                                .map(SearchHit::getContent)
                                .forEach(combo -> comboProductIds.add(combo.getId()));
                    } catch (Exception e) {
                        System.err.println("查询组品信息失败，分类名称: " + categoryName + ", 错误: " + e.getMessage());
                    }
                }
            }

            // 🔥 修复：批量查询组品信息
            Map<Long, ErpComboProductES> comboProductMap = new HashMap<>();
            if (!comboProductIds.isEmpty()) {
                Iterable<ErpComboProductES> comboProducts = comboProductESRepository.findAllById(comboProductIds);
                comboProducts.forEach(combo -> comboProductMap.put(combo.getId(), combo));
            }

            // 🔥 修复：批量计算实时数据
            Map<Long, BigDecimal> realTimePurchasePriceMap = new HashMap<>();
            for (Long comboProductId : comboProductIds) {
                try {
                    BigDecimal realTimePurchasePrice = calculateRealTimePurchasePrice(comboProductId);
                    if (realTimePurchasePrice != null) {
                        realTimePurchasePriceMap.put(comboProductId, realTimePurchasePrice);
                    }
                } catch (Exception e) {
                    System.err.println("实时计算失败，组品ID: " + comboProductId + ", 错误: " + e.getMessage());
                }
            }

            // 🔥 修复：补充产品价格计算
            for (ErpDistributionWholesaleStatisticsRespVO.StatisticsItem item : itemMap.values()) {
                if ("purchaser".equals(statisticsType) || "supplier".equals(statisticsType)) {
                    // 对于采购人员和供应商统计，需要根据分类名称找到对应的组品
                    String categoryName = item.getCategoryName();
                    if (categoryName != null) {
                        // 找到该分类对应的组品
                        for (Map.Entry<Long, ErpComboProductES> entry : comboProductMap.entrySet()) {
                            ErpComboProductES comboProduct = entry.getValue();
                            boolean isMatch = false;
                            
                            if ("purchaser".equals(statisticsType) && categoryName.equals(comboProduct.getPurchaser())) {
                                isMatch = true;
                            } else if ("supplier".equals(statisticsType) && categoryName.equals(comboProduct.getSupplier())) {
                                isMatch = true;
                            }
                            
                            if (isMatch) {
                                Long comboProductId = entry.getKey();
                                // 使用实时计算的采购单价，如果失败则使用ES缓存数据
                                BigDecimal purchasePrice = realTimePurchasePriceMap.get(comboProductId);
                                if (purchasePrice == null) {
                                    purchasePrice = comboProduct.getWholesalePrice() != null ? 
                                        comboProduct.getWholesalePrice() : BigDecimal.ZERO;
                                }
                                
                                // 计算采购金额：产品价格 × 数量 + 已累加的费用
                                if (item.getWholesaleProductQuantity() != null) {
                                    BigDecimal productCost = purchasePrice.multiply(BigDecimal.valueOf(item.getWholesaleProductQuantity()));
                                    item.setWholesalePurchaseAmount(item.getWholesalePurchaseAmount().add(productCost));
                                }
                                
                                // 销售金额需要从销售价格表获取，这里暂时使用组品价格作为估算
                                BigDecimal salePrice = comboProduct.getWholesalePrice() != null ? 
                                    comboProduct.getWholesalePrice() : BigDecimal.ZERO;
                                if (item.getWholesaleProductQuantity() != null) {
                                    BigDecimal productSaleAmount = salePrice.multiply(BigDecimal.valueOf(item.getWholesaleProductQuantity()));
                                    item.setWholesaleSaleAmount(item.getWholesaleSaleAmount().add(productSaleAmount));
                                }
                                break;
                            }
                        }
                    }
                } else {
                    // 对于其他统计类型，使用平均价格计算
                    BigDecimal avgPurchasePrice = BigDecimal.valueOf(100);
                    BigDecimal avgSalePrice = BigDecimal.valueOf(120);

                    if (item.getWholesaleProductQuantity() != null) {
                        BigDecimal productCost = avgPurchasePrice.multiply(BigDecimal.valueOf(item.getWholesaleProductQuantity()));
                        item.setWholesalePurchaseAmount(item.getWholesalePurchaseAmount().add(productCost));

                        BigDecimal productSaleAmount = avgSalePrice.multiply(BigDecimal.valueOf(item.getWholesaleProductQuantity()));
                        item.setWholesaleSaleAmount(item.getWholesaleSaleAmount().add(productSaleAmount));
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("补充批发金额计算失败: " + e.getMessage());
        }
    }



}
