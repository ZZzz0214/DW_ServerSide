package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.*;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceESDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSalePriceMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.service.distribution.ErpDistributionService;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.erp.service.wholesale.ErpWholesaleService;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpSalePriceServiceImpl implements ErpSalePriceService {

    @Resource
    private ErpSalePriceMapper erpSalePriceMapper;

    @Resource
    private ErpComboProductService erpComboProductService;

    @Resource
    private ErpComboMapper erpComboMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private ErpSalePriceESRepository salePriceESRepository;

    @Resource
    private ErpCustomerService erpCustomerService;

    @Resource
    private ErpDistributionService distributionService;

    @Resource
    private ErpWholesaleService wholesaleService;

    // 缓存组品信息，避免重复查询
    private final Map<String, ErpComboProductDO> comboProductCache = new ConcurrentHashMap<>();
    private final Map<Long, ErpComboRespVO> comboRespVOCache = new ConcurrentHashMap<>();

    // ES索引初始化
    @EventListener(ApplicationReadyEvent.class)
    public void initESIndex() {
        System.out.println("开始初始化销售价格ES索引...");
        try {
            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpSalePriceESDO.class);
            if (!indexOps.exists()) {
                indexOps.create();
                indexOps.putMapping(indexOps.createMapping(ErpSalePriceESDO.class));
                System.out.println("销售价格索引创建成功");
            }
        } catch (Exception e) {
            System.err.println("销售价格索引初始化失败: " + e.getMessage());
        }
    }

    // 批量同步数据到ES
    private void batchSyncToES(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        try {
            List<ErpSalePriceDO> salePrices = erpSalePriceMapper.selectBatchIds(ids);
            List<ErpSalePriceESDO> esList = salePrices.stream()
                    .map(this::convertToES)
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(esList)) {
                salePriceESRepository.saveAll(esList);
                // 强制刷新ES索引
                try {
                    elasticsearchRestTemplate.indexOps(ErpSalePriceESDO.class).refresh();
                } catch (Exception e) {
                    System.err.println("ES索引刷新失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("批量同步ES失败: " + e.getMessage());
        }
    }

    // 单个同步数据到ES
    private void syncToES(Long id) {
        try {
        ErpSalePriceDO salePrice = erpSalePriceMapper.selectById(id);
        if (salePrice == null) {
            salePriceESRepository.deleteById(id);
        } else {
            ErpSalePriceESDO es = convertToES(salePrice);
            salePriceESRepository.save(es);
            }
        } catch (Exception e) {
            System.err.println("同步ES失败，ID: " + id + ", 错误: " + e.getMessage());
        }
    }

    // 转换方法 - 支持keyword字段，参考组品表实现
    private ErpSalePriceESDO convertToES(ErpSalePriceDO salePrice) {
        ErpSalePriceESDO es = new ErpSalePriceESDO();
        BeanUtils.copyProperties(salePrice, es);

        // 如果是从数据库查询的完整对象，确保ID被正确设置
        if (salePrice.getId() != null) {
            es.setId(salePrice.getId());
        }

        // 处理产品名称和产品简称 - 通过组品编号获取完整的组品信息
        try {
            if (salePrice.getGroupProductId() != null) {
                // 获取组品的完整信息（使用ES查询替代数据库查询，提高效率）
                ErpComboRespVO comboInfo = getComboRespVOFromCache(salePrice.getGroupProductId());
                if (comboInfo != null) {
                    // 使用组品的完整名称和简称
                    String comboName = comboInfo.getName();
                    String comboShortName = comboInfo.getShortName();

                    if (StrUtil.isNotBlank(comboName)) {
                        es.setProductName(comboName);
                        es.setProductNameKeyword(comboName); // 设置keyword字段用于精确匹配
                    } else if (StrUtil.isNotBlank(salePrice.getProductName())) {
                        // 如果获取组品信息失败，使用数据库中的产品名称
                        es.setProductName(salePrice.getProductName());
                        es.setProductNameKeyword(salePrice.getProductName());
                    } else {
                        es.setProductName("");
                        es.setProductNameKeyword("");
                    }

                    if (StrUtil.isNotBlank(comboShortName)) {
                        es.setProductShortName(comboShortName);
                        es.setProductShortNameKeyword(comboShortName); // 设置keyword字段用于精确匹配
                    } else if (StrUtil.isNotBlank(salePrice.getProductShortName())) {
                        // 如果获取组品信息失败，使用数据库中的产品简称
                        es.setProductShortName(salePrice.getProductShortName());
                        es.setProductShortNameKeyword(salePrice.getProductShortName());
                    } else {
                        es.setProductShortName("");
                        es.setProductShortNameKeyword("");
                    }

                    System.out.println("销售价格ES转换 - 设置产品名称: " + comboName + ", 产品简称: " + comboShortName);
                } else {
                    // 如果获取组品信息失败，使用数据库中的值
                    String productName = salePrice.getProductName() != null ? salePrice.getProductName() : "";
                    String productShortName = salePrice.getProductShortName() != null ? salePrice.getProductShortName() : "";
                    es.setProductName(productName);
                    es.setProductNameKeyword(productName);
                    es.setProductShortName(productShortName);
                    es.setProductShortNameKeyword(productShortName);
                    System.out.println("销售价格ES转换 - 使用数据库产品名称: " + productName + ", 产品简称: " + productShortName);
                }
            } else {
                // 没有组品ID时，直接使用数据库中的值
                String productName = salePrice.getProductName() != null ? salePrice.getProductName() : "";
                String productShortName = salePrice.getProductShortName() != null ? salePrice.getProductShortName() : "";
                es.setProductName(productName);
                es.setProductNameKeyword(productName);
                es.setProductShortName(productShortName);
                es.setProductShortNameKeyword(productShortName);
            }
        } catch (Exception e) {
            System.err.println("获取组品名称失败，销售价格ID: " + salePrice.getId() + ", 错误: " + e.getMessage());
            // 如果获取失败，使用数据库中的值
            String productName = salePrice.getProductName() != null ? salePrice.getProductName() : "";
            String productShortName = salePrice.getProductShortName() != null ? salePrice.getProductShortName() : "";
            es.setProductName(productName);
            es.setProductNameKeyword(productName);
            es.setProductShortName(productShortName);
            es.setProductShortNameKeyword(productShortName);
        }

        return es;
    }

    // 全量同步方法
    @Async
    public void fullSyncToES() {
        try {
            System.out.println("开始全量同步销售价格数据到ES...");

            // 分批处理，避免内存溢出
            int batchSize = 1000;
            int offset = 0;

            while (true) {
                List<ErpSalePriceDO> batch = erpSalePriceMapper.selectList(
                    new LambdaQueryWrapper<ErpSalePriceDO>()
                        .last("LIMIT " + batchSize + " OFFSET " + offset)
                );

                if (CollUtil.isEmpty(batch)) {
                    break;
                }

                List<ErpSalePriceESDO> esList = batch.stream()
                        .map(this::convertToES)
                        .collect(Collectors.toList());
                salePriceESRepository.saveAll(esList);

                offset += batchSize;
                System.out.println("已同步 " + offset + " 条销售价格数据到ES");
            }

            // 强制刷新ES索引
            elasticsearchRestTemplate.indexOps(ErpSalePriceESDO.class).refresh();
            System.out.println("销售价格全量同步ES数据完成");
        } catch (Exception e) {
            System.err.println("销售价格全量同步ES数据失败: " + e.getMessage());
        }
    }

    @Override
    public Long createSalePrice(@Valid ErpSalePriceSaveReqVO createReqVO) {
        // 1. 生成销售价格表编号
        String no = noRedisDAO.generate(ErpNoRedisDAO.SALE_PRICE_NO_PREFIX);
        if (erpSalePriceMapper.selectByNo(no) != null) {
            throw exception(SALE_PRICE_NOT_EXISTS);
        }
        validateCustomerProductUnique(createReqVO.getCustomerName(), createReqVO.getGroupProductId(), null);

        // 2. 根据groupProductId获取组品信息
        Long groupProductId = createReqVO.getGroupProductId();
        ErpComboRespVO comboInfo = null;
        if (groupProductId != null) {
            comboInfo = getComboRespVOFromCache(groupProductId);
        }

        // 3. 保存销售价格信息
        ErpSalePriceDO salePriceDO = BeanUtils.toBean(createReqVO, ErpSalePriceDO.class)
                .setNo(no);

        // 设置从组品获取的信息
        if (comboInfo != null) {
            salePriceDO.setProductName(comboInfo.getName());
            salePriceDO.setProductShortName(comboInfo.getShortName());
            salePriceDO.setProductImage(comboInfo.getImage());
            System.out.println("创建销售价格 - 设置产品名称: " + comboInfo.getName());
        }

        erpSalePriceMapper.insert(salePriceDO);

        // 从数据库查询完整数据（包含创建人、创建时间等）后同步到ES
        ErpSalePriceDO fullData = erpSalePriceMapper.selectById(salePriceDO.getId());
        ErpSalePriceESDO es = convertToES(fullData);
        salePriceESRepository.save(es);

        // 刷新ES索引，确保数据立即可见
        try {
            elasticsearchRestTemplate.indexOps(ErpSalePriceESDO.class).refresh();
        } catch (Exception e) {
            System.err.println("刷新ES索引失败: " + e.getMessage());
        }

        return salePriceDO.getId();
    }

    @Override
    public void updateSalePrice(@Valid ErpSalePriceSaveReqVO updateReqVO) {
        validateSalePriceExists(updateReqVO.getId());
        validateCustomerProductUnique(updateReqVO.getCustomerName(), updateReqVO.getGroupProductId(), updateReqVO.getId());

        // 根据groupProductId获取组品信息
        Long groupProductId = updateReqVO.getGroupProductId();
        ErpComboRespVO comboInfo = null;
        if (groupProductId != null) {
            comboInfo = getComboRespVOFromCache(groupProductId);
        }

        ErpSalePriceDO updateObj = BeanUtils.toBean(updateReqVO, ErpSalePriceDO.class);

        // 设置从组品获取的信息
        if (comboInfo != null) {
            updateObj.setProductName(comboInfo.getName());
            updateObj.setProductShortName(comboInfo.getShortName());
            updateObj.setProductImage(comboInfo.getImage());
            System.out.println("更新销售价格 - 设置产品名称: " + comboInfo.getName());
        }

        erpSalePriceMapper.updateById(updateObj);

        // 同步更新ES，确保立即可搜索
        ErpSalePriceDO fullData = erpSalePriceMapper.selectById(updateObj.getId());
        ErpSalePriceESDO es = convertToES(fullData);
        salePriceESRepository.save(es);

        // 刷新ES索引，确保数据立即可见
        try {
            elasticsearchRestTemplate.indexOps(ErpSalePriceESDO.class).refresh();
        } catch (Exception e) {
            System.err.println("刷新ES索引失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteSalePrice(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            validateSalePriceExists(id);
        }

        erpSalePriceMapper.deleteBatchIds(ids);

        // 同步删除ES数据，确保立即生效
        try {
        ids.forEach(id -> salePriceESRepository.deleteById(id));
            // 刷新ES索引，确保删除立即可见
            elasticsearchRestTemplate.indexOps(ErpSalePriceESDO.class).refresh();
        } catch (Exception e) {
            System.err.println("批量删除ES数据失败: " + e.getMessage());
        }
    }

    @Override
    public List<ErpSalePriceDO> validSalePriceList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpSalePriceMapper.selectBatchIds(ids);
    }

    private void validateSalePriceExists(Long id) {
        if (erpSalePriceMapper.selectById(id) == null) {
            throw exception(SALE_PRICE_NOT_EXISTS);
        }
    }

    @Override
    public ErpSalePriceDO getSalePrice(Long id) {
        return erpSalePriceMapper.selectById(id);
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpSalePriceDO> list = erpSalePriceMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpSalePriceRespVO.class);
    }

    @Override
    public PageResult<ErpSalePriceRespVO> getSalePriceVOPage(ErpSalePricePageReqVO pageReqVO) {
        try {
            // 1. 检查数据库是否有数据
            long dbCount = erpSalePriceMapper.selectCount(null);

            // 2. 检查ES索引是否存在
            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpSalePriceESDO.class);
            boolean indexExists = indexOps.exists();

            // 3. 检查ES数据量
            long esCount = 0;
            if (indexExists) {
                esCount = elasticsearchRestTemplate.count(new NativeSearchQueryBuilder().build(), ErpSalePriceESDO.class);
            }

            // 4. 处理数据库和ES数据不一致的情况
            if (dbCount == 0) {
                if (indexExists && esCount > 0) {
                    // 数据库为空但ES有数据，清空ES
                    salePriceESRepository.deleteAll();
                }
                return new PageResult<>(Collections.emptyList(), 0L);
            }

            if (!indexExists) {
                initESIndex();
                fullSyncToES();
                return getSalePriceVOPageFromDB(pageReqVO);
            }

            if (esCount == 0 || dbCount != esCount) {
                fullSyncToES();
                if (esCount == 0) {
                    return getSalePriceVOPageFromDB(pageReqVO);
                }
            }

            // 5. 使用ES查询
            return getSalePriceVOPageFromES(pageReqVO);
        } catch (Exception e) {
            System.err.println("ES查询失败，回退到数据库查询: " + e.getMessage());
            return getSalePriceVOPageFromDB(pageReqVO);
        }
    }

    /**
     * 优化的ES分页查询 - 参考产品表的智能搜索策略
     */
    private PageResult<ErpSalePriceRespVO> getSalePriceVOPageFromES(ErpSalePricePageReqVO pageReqVO) {
        // 🔥 关键修复：参考产品表的实现，正确处理导出场景
        // 验证分页参数 - 但不覆盖PAGE_SIZE_NONE
        if (pageReqVO.getPageSize() == null) {
            pageReqVO.setPageSize(10); // 设置默认页大小
        }
        if (pageReqVO.getPageNo() == null || pageReqVO.getPageNo() <= 0) {
            pageReqVO.setPageNo(1); // 设置默认页码
        }

        // 构建ES查询 - 参考产品表的智能搜索策略
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 编号搜索 - 智能匹配策略
        if (StringUtils.isNotBlank(pageReqVO.getNo())) {
            BoolQueryBuilder noQuery = QueryBuilders.boolQuery();
            String no = pageReqVO.getNo().trim();

            // 完全精确匹配（最高权重）
            noQuery.should(QueryBuilders.termQuery("no_keyword", no).boost(1000000.0f));
            // 前缀匹配
            noQuery.should(QueryBuilders.prefixQuery("no_keyword", no).boost(100000.0f));
            // 通配符包含匹配
            noQuery.should(QueryBuilders.wildcardQuery("no_keyword", "*" + no + "*").boost(10000.0f));
            // 分词匹配（根据长度智能选择策略）
            noQuery.should(createIntelligentMatchQuery("no", no, 800.0f, 600.0f, 500.0f));

            noQuery.minimumShouldMatch(1);
            boolQuery.must(noQuery);
        }

        // 产品名称搜索 - 通过组品表进行链表查询
        if (StringUtils.isNotBlank(pageReqVO.getProductName())) {
            String name = pageReqVO.getProductName().trim();

            try {
                // 第一步：通过组品表的name字段搜索匹配的组品ID
                BoolQueryBuilder comboNameQuery = QueryBuilders.boolQuery();

                // 参考组品表的简化搜索策略
                // 第一优先级：完全精确匹配（最高权重）
                comboNameQuery.should(QueryBuilders.termQuery("name_keyword", name).boost(1000000.0f));
                // 第二优先级：前缀匹配
                comboNameQuery.should(QueryBuilders.prefixQuery("name_keyword", name).boost(100000.0f));
                // 第三优先级：通配符包含匹配（支持中间字符搜索）
                comboNameQuery.should(QueryBuilders.wildcardQuery("name_keyword", "*" + name + "*").boost(50000.0f));

                comboNameQuery.minimumShouldMatch(1);

                NativeSearchQuery comboQuery = new NativeSearchQueryBuilder()
                        .withQuery(comboNameQuery)
                        .withPageable(PageRequest.of(0, 1000)) // 最多获取1000个匹配的组品
                        .build();

                SearchHits<ErpComboProductES> comboHits = elasticsearchRestTemplate.search(
                        comboQuery,
                        ErpComboProductES.class,
                        IndexCoordinates.of("erp_combo_products"));

                if (comboHits.getTotalHits() > 0) {
                    // 提取匹配的组品ID列表
                    List<Long> matchedComboIds = comboHits.stream()
                            .map(hit -> hit.getContent().getId())
                            .collect(Collectors.toList());

                    // 第二步：通过组品ID查询销售价格表
                    boolQuery.must(QueryBuilders.termsQuery("group_product_id", matchedComboIds));
                } else {
                    // 如果没有找到匹配的组品，设置一个不可能的条件，让搜索结果为空
                    boolQuery.must(QueryBuilders.termQuery("group_product_id", -1L));
                }
            } catch (Exception e) {
                System.err.println("组品名称搜索失败: " + e.getMessage());
                // 搜索失败时，设置一个不可能的条件
                boolQuery.must(QueryBuilders.termQuery("group_product_id", -1L));
            }
        }

        // 产品简称搜索 - 通过组品表进行链表查询
        if (StringUtils.isNotBlank(pageReqVO.getProductShortName())) {
            String shortName = pageReqVO.getProductShortName().trim();

            try {
                // 第一步：通过组品表的short_name字段搜索匹配的组品ID
                BoolQueryBuilder comboShortNameQuery = QueryBuilders.boolQuery();

                // 参考组品表的简化搜索策略
                // 第一优先级：完全精确匹配（最高权重）
                comboShortNameQuery.should(QueryBuilders.termQuery("short_name_keyword", shortName).boost(1000000.0f));
                // 第二优先级：前缀匹配
                comboShortNameQuery.should(QueryBuilders.prefixQuery("short_name_keyword", shortName).boost(100000.0f));
                // 第三优先级：通配符包含匹配（支持中间字符搜索）
                comboShortNameQuery.should(QueryBuilders.wildcardQuery("short_name_keyword", "*" + shortName + "*").boost(50000.0f));

                comboShortNameQuery.minimumShouldMatch(1);

                NativeSearchQuery comboQuery = new NativeSearchQueryBuilder()
                        .withQuery(comboShortNameQuery)
                        .withPageable(PageRequest.of(0, 1000)) // 最多获取1000个匹配的组品
                        .build();

                SearchHits<ErpComboProductES> comboHits = elasticsearchRestTemplate.search(
                        comboQuery,
                        ErpComboProductES.class,
                        IndexCoordinates.of("erp_combo_products"));

                if (comboHits.getTotalHits() > 0) {
                    // 提取匹配的组品ID列表
                    List<Long> matchedComboIds = comboHits.stream()
                            .map(hit -> hit.getContent().getId())
                            .collect(Collectors.toList());

                    // 第二步：通过组品ID查询销售价格表
                    boolQuery.must(QueryBuilders.termsQuery("group_product_id", matchedComboIds));
                } else {
                    // 如果没有找到匹配的组品，设置一个不可能的条件，让搜索结果为空
                    boolQuery.must(QueryBuilders.termQuery("group_product_id", -1L));
                }
            } catch (Exception e) {
                System.err.println("组品简称搜索失败: " + e.getMessage());
                // 搜索失败时，设置一个不可能的条件
                boolQuery.must(QueryBuilders.termQuery("group_product_id", -1L));
            }
        }

        // 客户名称搜索 - 智能匹配策略
        if (StringUtils.isNotBlank(pageReqVO.getCustomerName())) {
            BoolQueryBuilder customerQuery = QueryBuilders.boolQuery();
            String customer = pageReqVO.getCustomerName().trim();

            customerQuery.should(QueryBuilders.termQuery("customer_name.keyword", customer).boost(1000000.0f));
            customerQuery.should(QueryBuilders.prefixQuery("customer_name.keyword", customer).boost(100000.0f));
            customerQuery.should(QueryBuilders.wildcardQuery("customer_name.keyword", "*" + customer + "*").boost(10000.0f));
            customerQuery.should(createIntelligentMatchQuery("customer_name", customer, 800.0f, 600.0f, 500.0f));

            customerQuery.minimumShouldMatch(1);
            boolQuery.must(customerQuery);
        }

        // 创建人员搜索 - 智能匹配策略
        if (StringUtils.isNotBlank(pageReqVO.getCreator())) {
            BoolQueryBuilder creatorQuery = QueryBuilders.boolQuery();
            String creator = pageReqVO.getCreator().trim();

            creatorQuery.should(QueryBuilders.termQuery("creator.keyword", creator).boost(1000000.0f));
            creatorQuery.should(QueryBuilders.prefixQuery("creator.keyword", creator).boost(100000.0f));
            creatorQuery.should(QueryBuilders.wildcardQuery("creator.keyword", "*" + creator + "*").boost(10000.0f));
            creatorQuery.should(createIntelligentMatchQuery("creator", creator, 800.0f, 600.0f, 500.0f));

            creatorQuery.minimumShouldMatch(1);
            boolQuery.must(creatorQuery);
        }

        // 组品ID精确查询
        if (pageReqVO.getGroupProductId() != null) {
            boolQuery.must(QueryBuilders.termQuery("group_product_id", pageReqVO.getGroupProductId()));
        }

        // 组品编号搜索 - 智能匹配策略
        if (StringUtils.isNotBlank(pageReqVO.getGroupProductNo())) {
            BoolQueryBuilder groupNoQuery = QueryBuilders.boolQuery();
            String groupNo = pageReqVO.getGroupProductNo().trim();

            // 由于需要根据组品编号查找对应的组品ID，然后用ID进行搜索
            // 这里我们需要先通过组品编号查找组品ID
            try {
                // 查找对应的组品ID
                ErpComboProductDO combo = erpComboMapper.selectByNo(groupNo);
                if (combo != null) {
                    // 如果找到了对应的组品，使用组品ID进行搜索
                    boolQuery.must(QueryBuilders.termQuery("group_product_id", combo.getId()));
                } else {
                    // 如果没找到对应的组品，设置一个不可能的条件，让搜索结果为空
                    boolQuery.must(QueryBuilders.termQuery("group_product_id", -1L));
                }
            } catch (Exception e) {
                System.err.println("查找组品编号失败: " + e.getMessage());
                // 查找失败时，设置一个不可能的条件
                boolQuery.must(QueryBuilders.termQuery("group_product_id", -1L));
            }
        }

        // 代发单价精确查询
        if (pageReqVO.getDistributionPrice() != null) {
            boolQuery.must(QueryBuilders.termQuery("distribution_price", pageReqVO.getDistributionPrice()));
        }

        // 批发单价精确查询
        if (pageReqVO.getWholesalePrice() != null) {
            boolQuery.must(QueryBuilders.termQuery("wholesale_price", pageReqVO.getWholesalePrice()));
        }

        // 创建时间范围查询
        if (pageReqVO.getCreateTime() != null && pageReqVO.getCreateTime().length == 2) {
            boolQuery.must(QueryBuilders.rangeQuery("create_time")
                    .gte(pageReqVO.getCreateTime()[0].toString())
                    .lte(pageReqVO.getCreateTime()[1].toString()));
        }

        // 如果没有任何查询条件，使用match_all
        if (!boolQuery.hasClauses()) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }

        // 🔥 关键修复：参考产品表的导出处理逻辑
        // 处理分页参数
        // 检查是否是导出操作（pageSize为-1）
        if (PageParam.PAGE_SIZE_NONE.equals(pageReqVO.getPageSize())) {
            // 导出所有数据，不使用分页，但限制最大返回数量防止内存溢出
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(PageRequest.of(0, 10000)) // 最多返回10000条
                    .withTrackTotalHits(true)
                    .withSort(Sort.by(Sort.Direction.DESC, "create_time")) // 按创建时间倒序排列
                    .withSort(Sort.by(Sort.Direction.DESC, "id")); // 辅助排序：ID倒序

            SearchHits<ErpSalePriceESDO> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpSalePriceESDO.class,
                    IndexCoordinates.of("erp_sale_price"));

            // 批量获取组合产品信息，减少重复查询
            List<ErpSalePriceRespVO> voList = convertESToVO(searchHits);

            return new PageResult<>(voList, searchHits.getTotalHits());
        }

        // 处理深度分页问题
        if (pageReqVO.getPageNo() > 100) { // 超过100页使用scroll
            return handleDeepPaginationWithScroll(pageReqVO, boolQuery);
        }

        // 普通分页查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()))
                .withTrackTotalHits(true)
                .withSort(Sort.by(Sort.Direction.DESC, "_score"))  // 按相关性排序
                .withSort(Sort.by(Sort.Direction.DESC, "id"));     // 相关性相同时按ID排序

        SearchHits<ErpSalePriceESDO> searchHits = elasticsearchRestTemplate.search(
                queryBuilder.build(),
                ErpSalePriceESDO.class,
                IndexCoordinates.of("erp_sale_price"));

        // 批量获取组合产品信息，减少重复查询
        List<ErpSalePriceRespVO> voList = convertESToVO(searchHits);

        return new PageResult<>(voList, searchHits.getTotalHits());
    }

    /**
     * 使用scroll处理深度分页
     */
    private PageResult<ErpSalePriceRespVO> handleDeepPaginationWithScroll(ErpSalePricePageReqVO pageReqVO, BoolQueryBuilder boolQuery) {
        // 🔥 关键修复：确保深度分页不会影响导出功能
        // 如果是导出操作（PAGE_SIZE_NONE），直接返回空结果，因为导出应该在前面处理
        if (PageParam.PAGE_SIZE_NONE.equals(pageReqVO.getPageSize())) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        // 计算需要跳过的记录数
        int skip = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();

        NativeSearchQuery scrollQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(0, Math.min(skip + pageReqVO.getPageSize(), 10000)))
                .withSort(Sort.by(Sort.Direction.DESC, "id"))
                .build();

        SearchHits<ErpSalePriceESDO> searchHits = elasticsearchRestTemplate.search(
                scrollQuery,
                ErpSalePriceESDO.class,
                IndexCoordinates.of("erp_sale_price"));

        // 获取目标页的数据
        List<SearchHit<ErpSalePriceESDO>> hits = searchHits.getSearchHits();
        List<SearchHit<ErpSalePriceESDO>> targetHits = hits.stream()
                .skip(skip)
                .limit(pageReqVO.getPageSize())
                .collect(Collectors.toList());

        List<ErpSalePriceRespVO> voList = targetHits.stream()
                .map(SearchHit::getContent)
                .map(this::convertESDOToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, searchHits.getTotalHits());
    }

    /**
     * 数据库分页查询（回退方案）
     */
    private PageResult<ErpSalePriceRespVO> getSalePriceVOPageFromDB(ErpSalePricePageReqVO pageReqVO) {
        // 🔥 关键修复：正确处理PAGE_SIZE_NONE的情况
        if (PageParam.PAGE_SIZE_NONE.equals(pageReqVO.getPageSize())) {
            // 导出所有数据，不使用分页
            List<ErpSalePriceDO> allSalePrices = erpSalePriceMapper.selectList(null);
            List<ErpSalePriceRespVO> voList = convertDOToVO(allSalePrices);
            return new PageResult<>(voList, (long) allSalePrices.size());
        } else {
            // 正常分页查询
            PageResult<ErpSalePriceDO> pageResult = erpSalePriceMapper.selectPage(pageReqVO);
            List<ErpSalePriceRespVO> voList = convertDOToVO(pageResult.getList());
            return new PageResult<>(voList, pageResult.getTotal());
        }
    }

    /**
     * 批量转换ES结果为VO
     */
    private List<ErpSalePriceRespVO> convertESToVO(SearchHits<ErpSalePriceESDO> searchHits) {
        List<ErpSalePriceESDO> esList = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return esList.stream()
                .map(this::convertESDOToVO)
                .filter(Objects::nonNull) // 过滤掉null值
                .collect(Collectors.toList());
    }

    /**
     * 批量转换DO为VO
     */
    private List<ErpSalePriceRespVO> convertDOToVO(List<ErpSalePriceDO> doList) {
        return doList.stream()
                .map(this::convertDOToVO)
                .collect(Collectors.toList());
    }

    /**
     * 单个ESDO转VO
     */
    private ErpSalePriceRespVO convertESDOToVO(ErpSalePriceESDO esDO) {
        try {
            ErpSalePriceRespVO vo = BeanUtils.toBean(esDO, ErpSalePriceRespVO.class);

            if (esDO.getGroupProductId() != null) {
                // 实时从组品表获取最新的组品信息
                ErpComboRespVO comboRespVO = getComboRespVOFromCache(esDO.getGroupProductId());

                if (comboRespVO != null) {
                    vo.setComboList(Collections.singletonList(comboRespVO));
                    vo.setGroupProductId(comboRespVO.getId());
                    vo.setGroupProductNo(comboRespVO.getNo());
                    // 从组品信息中获取最新的产品名称和产品简称
                    vo.setProductName(comboRespVO.getName());
                    vo.setProductShortName(comboRespVO.getShortName());
                } else {
                    // 如果获取组品信息失败，使用ES中的值作为兜底
                    vo.setGroupProductId(esDO.getGroupProductId());
                    vo.setProductName(esDO.getProductName());
                    vo.setProductShortName(esDO.getProductShortName());
                }
            }

            return vo;
        } catch (Exception e) {
            System.err.println("转换ES记录到VO时发生异常: " + e.getMessage());

            // 发生异常时，创建基本的VO
            try {
                ErpSalePriceRespVO vo = new ErpSalePriceRespVO();
                vo.setId(esDO.getId());
                vo.setNo(esDO.getNo());
                vo.setGroupProductId(esDO.getGroupProductId());
                vo.setProductName(esDO.getProductName());
                vo.setProductShortName(esDO.getProductShortName());
                vo.setCustomerName(esDO.getCustomerName());
                vo.setDistributionPrice(esDO.getDistributionPrice());
                vo.setWholesalePrice(esDO.getWholesalePrice());
                vo.setRemark(esDO.getRemark());
                vo.setShippingFeeType(esDO.getShippingFeeType());
                vo.setFixedShippingFee(esDO.getFixedShippingFee());
                vo.setAdditionalItemQuantity(esDO.getAdditionalItemQuantity());
                vo.setAdditionalItemPrice(esDO.getAdditionalItemPrice());
                vo.setFirstWeight(esDO.getFirstWeight());
                vo.setFirstWeightPrice(esDO.getFirstWeightPrice());
                vo.setAdditionalWeight(esDO.getAdditionalWeight());
                vo.setAdditionalWeightPrice(esDO.getAdditionalWeightPrice());
                vo.setCreator(esDO.getCreator());
                vo.setCreateTime(esDO.getCreateTime());
                vo.setTenantId(esDO.getTenantId());
                return vo;
            } catch (Exception ex) {
                System.err.println("创建基本VO也失败: " + ex.getMessage());
                return null;
            }
        }
    }

    /**
     * 单个DO转VO
     */
    private ErpSalePriceRespVO convertDOToVO(ErpSalePriceDO doObj) {
        ErpSalePriceRespVO vo = BeanUtils.toBean(doObj, ErpSalePriceRespVO.class);
        if (doObj.getGroupProductId() != null) {
            ErpComboRespVO comboRespVO = getComboRespVOFromCache(doObj.getGroupProductId());
            if (comboRespVO != null) {
                vo.setComboList(Collections.singletonList(comboRespVO));
                vo.setGroupProductId(comboRespVO.getId());
                vo.setGroupProductNo(comboRespVO.getNo());
                // 从组品信息中获取最新的产品名称和产品简称
                vo.setProductName(comboRespVO.getName());
                vo.setProductShortName(comboRespVO.getShortName());
            } else {
                // 如果获取组品信息失败，使用数据库中的值作为兜底
                vo.setProductName(doObj.getProductName());
                vo.setProductShortName(doObj.getProductShortName());
            }
        }
        return vo;
    }

    /**
     * 从缓存获取组品信息，优先使用ES查询
     */
    private ErpComboRespVO getComboRespVOFromCache(Long groupProductId) {
        return comboRespVOCache.computeIfAbsent(groupProductId, id -> {
            try {
                // 优先使用ES查询获取组品信息，提高查询效率
                return erpComboProductService.getComboWithItems(id);
            } catch (Exception e) {
                System.err.println("获取组品信息失败，ID: " + id + ", 错误: " + e.getMessage());
                return null;
            }
        });
    }

    @Override
    public ErpSalePriceRespVO getSalePriceWithItems(Long id) {
        // 查询销售价格基本信息
        ErpSalePriceDO salePrice = erpSalePriceMapper.selectById(id);
        if (salePrice == null) {
            return null;
        }

        // 组装响应对象
        ErpSalePriceRespVO respVO = BeanUtils.toBean(salePrice, ErpSalePriceRespVO.class);

        // 根据 groupProductId 实时查询组合产品信息
        if (salePrice.getGroupProductId() != null) {
            ErpComboRespVO comboRespVO = getComboRespVOFromCache(salePrice.getGroupProductId());
            if (comboRespVO != null) {
                respVO.setComboList(Collections.singletonList(comboRespVO));
                respVO.setGroupProductId(comboRespVO.getId());
                respVO.setGroupProductNo(comboRespVO.getNo());
                // 从组品信息中获取最新的产品名称和产品简称
                respVO.setProductName(comboRespVO.getName());
                respVO.setProductShortName(comboRespVO.getShortName());
            } else {
                // 如果获取组品信息失败，使用数据库中的值作为兜底
                respVO.setProductName(salePrice.getProductName());
                respVO.setProductShortName(salePrice.getProductShortName());
            }
        }

        return respVO;
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOListByGroupProductId(Long groupProductId) {
        return null;
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOListByCustomerName(String customerName) {
        return null;
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOListByStatus(Integer status) {
        return null;
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOListByComboStatus() {
        // 1. 查询所有销售价格记录
        List<ErpSalePriceDO> salePrices = erpSalePriceMapper.selectList();

        // 2. 过滤出组合品状态符合条件的记录
        return salePrices.stream()
                .filter(salePrice -> {
                    ErpComboProductDO comboProduct = erpComboMapper.selectById(salePrice.getGroupProductId());
                    return comboProduct != null && comboProduct.getStatus() != null
                            && comboProduct.getStatus().equals(CommonStatusEnum.ENABLE.getStatus());
                })
                .map(salePrice -> BeanUtils.toBean(salePrice, ErpSalePriceRespVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ErpSalePriceRespVO> searchProducts(ErpSalePriceSearchReqVO searchReqVO) {
        try {
            // 优先使用ES搜索，确保能搜索到最新数据
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            if (searchReqVO.getGroupProductId() != null) {
                boolQuery.must(QueryBuilders.termQuery("group_product_id", searchReqVO.getGroupProductId()));
            }
            if (StrUtil.isNotBlank(searchReqVO.getCustomerName())) {
                // 尝试多种匹配方式
                BoolQueryBuilder customerQuery = QueryBuilders.boolQuery()
                    .should(QueryBuilders.termQuery("customer_name.keyword", searchReqVO.getCustomerName()))
                    .should(QueryBuilders.termQuery("customer_name", searchReqVO.getCustomerName()))
                    .should(QueryBuilders.matchQuery("customer_name", searchReqVO.getCustomerName()))
                    .minimumShouldMatch(1);

                boolQuery.must(customerQuery);
            }

            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(PageRequest.of(0, 1000)) // 最多返回1000条
                    .withSort(Sort.by(Sort.Direction.DESC, "id"))
                    .build();

            SearchHits<ErpSalePriceESDO> searchHits = elasticsearchRestTemplate.search(
                    searchQuery,
                    ErpSalePriceESDO.class,
                    IndexCoordinates.of("erp_sale_price"));

            // 转换ES结果为VO
            List<ErpSalePriceRespVO> result = convertESToVO(searchHits);
            return result;

        } catch (Exception e) {
            System.err.println("ES搜索失败，回退到数据库查询: " + e.getMessage());

            // ES搜索失败时，回退到数据库查询
            List<ErpSalePriceDO> list = erpSalePriceMapper.selectList(new LambdaQueryWrapper<ErpSalePriceDO>()
                    .eq(searchReqVO.getGroupProductId() != null, ErpSalePriceDO::getGroupProductId, searchReqVO.getGroupProductId())
                    .eq(searchReqVO.getCustomerName() != null, ErpSalePriceDO::getCustomerName, searchReqVO.getCustomerName()));

            return convertDOToVO(list);
        }
    }

    @Override
    public ErpSalePriceRespVO getSalePriceByGroupProductIdAndCustomerName(Long groupProductId, String customerName) {
        // 1. 查询销售价格基本信息
        ErpSalePriceRespVO respVO = erpSalePriceMapper.selectByGroupProductIdAndCustomerName(groupProductId, customerName);
        if (respVO == null) {
            return null;
        }

        // 2. 设置组合产品信息
        if (groupProductId != null) {
            ErpComboRespVO comboRespVO = getComboRespVOFromCache(groupProductId);
            if (comboRespVO != null) {
                respVO.setComboList(Collections.singletonList(comboRespVO));
                respVO.setGroupProductId(comboRespVO.getId());
                // 从组品信息中获取产品名称和产品简称
                respVO.setProductName(comboRespVO.getName());
                respVO.setProductShortName(comboRespVO.getShortName());
                respVO.setOriginalQuantity(comboRespVO.getTotalQuantity());
                respVO.setShippingCode(comboRespVO.getShippingCode());
            }
        }

        return respVO;
    }

    @Override
    public List<ErpSalePriceRespVO> getMissingPrices() {
        return erpSalePriceMapper.selectMissingPrices();
    }

    private void validateCustomerProductUnique(String customerName, Long groupProductId, Long excludeId) {
        if (StrUtil.isBlank(customerName) || groupProductId == null) {
            return;
        }

        Long count = erpSalePriceMapper.selectCount(new LambdaQueryWrapper<ErpSalePriceDO>()
                .eq(ErpSalePriceDO::getCustomerName, customerName)
                .eq(ErpSalePriceDO::getGroupProductId, groupProductId)
                .ne(excludeId != null, ErpSalePriceDO::getId, excludeId));
        if (count > 0) {
            throw exception(SALE_PRICE_CUSTOMER_PRODUCT_DUPLICATE);
        }
    }

    /**
     * 使用预加载的数据校验客户名称+组品ID的唯一性
     */
    private void validateCustomerProductUniqueWithCache(String customerName, Long groupProductId,
            Map<String, Set<Long>> customerProductMap, Long excludeId) {
        if (StrUtil.isBlank(customerName) || groupProductId == null) {
            return;
        }

        Set<Long> productIds = customerProductMap.get(customerName);
        if (productIds != null && productIds.contains(groupProductId)) {
            // 如果是更新操作，需要进一步检查是否是同一条记录
            if (excludeId != null) {
                Long count = erpSalePriceMapper.selectCount(new LambdaQueryWrapper<ErpSalePriceDO>()
                        .eq(ErpSalePriceDO::getCustomerName, customerName)
                        .eq(ErpSalePriceDO::getGroupProductId, groupProductId)
                        .ne(ErpSalePriceDO::getId, excludeId));
                if (count > 0) {
                    throw exception(SALE_PRICE_CUSTOMER_PRODUCT_DUPLICATE,
                            "客户名称(" + customerName + ")和组品编号的组合已存在");
                }
            } else {
                throw exception(SALE_PRICE_CUSTOMER_PRODUCT_DUPLICATE,
                        "客户名称(" + customerName + ")和组品编号的组合已存在");
            }
        }
    }

    /**
     * 优化的导入功能 - 批量处理，缓存优化，减少数据库查询
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpSalePriceImportRespVO importSalePriceList(List<ErpSalePriceImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(SALE_PRICE_IMPORT_LIST_IS_EMPTY);
        }

        System.out.println("开始导入销售价格数据，共" + importList.size() + "条记录");

        // 初始化返回结果
        ErpSalePriceImportRespVO respVO = ErpSalePriceImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String username = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        LocalDateTime now = LocalDateTime.now();

        try {
            // 1. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateAllImportData(importList, isUpdateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 2. 批量预加载组品信息到缓存
            Set<String> groupProductNos = importList.stream()
                    .map(ErpSalePriceImportExcelVO::getGroupProductNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());

            Map<String, ErpComboProductDO> groupProductMap = preloadComboProducts(groupProductNos);

            // 3. 批量查询已存在的销售价格记录
            Set<String> noSet = importList.stream()
                    .map(ErpSalePriceImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());

            Map<String, ErpSalePriceDO> existingSalePriceMap = Collections.emptyMap();
            if (CollUtil.isNotEmpty(noSet)) {
                List<ErpSalePriceDO> existList = erpSalePriceMapper.selectListByNoIn(noSet);
                existingSalePriceMap = convertMap(existList, ErpSalePriceDO::getNo);
            }

            // 4. 批量处理数据
            List<ErpSalePriceDO> toCreateList = new ArrayList<>();
            List<ErpSalePriceDO> toUpdateList = new ArrayList<>();
            List<Long> processedIds = new ArrayList<>();

            for (int i = 0; i < importList.size(); i++) {
                ErpSalePriceImportExcelVO importVO = importList.get(i);
                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "第" + (i + 1) + "行";

                try {
                    // 验证组品编号
                    ErpComboProductDO comboProduct = groupProductMap.get(importVO.getGroupProductNo());
                    if (comboProduct == null) {
                        throw exception(SALE_PRICE_GROUP_PRODUCT_ID_REQUIRED, "组品编号不存在: " + importVO.getGroupProductNo());
                    }

                    // 判断是否支持更新
                    ErpSalePriceDO existSalePrice = existingSalePriceMap.get(importVO.getNo());

                    if (existSalePrice == null) {
                        // 创建新记录
                        ErpSalePriceDO salePrice = BeanUtils.toBean(importVO, ErpSalePriceDO.class)
                                .setGroupProductId(comboProduct.getId());
                        String newNo = noRedisDAO.generate(ErpNoRedisDAO.SALE_PRICE_NO_PREFIX);
                        salePrice.setNo(newNo);
                        // 设置组品相关信息（始终使用组品的数据）
                        salePrice.setProductName(comboProduct.getName());
                        salePrice.setProductShortName(comboProduct.getShortName()).setCreator(username).setCreateTime(now);
                        salePrice.setProductImage(comboProduct.getImage());

                        toCreateList.add(salePrice);
                        respVO.getCreateNames().add(salePrice.getNo());

                    } else if (isUpdateSupport) {
                        // 更新记录 - 只更新导入文件中提供的字段
                        ErpSalePriceDO updateSalePrice = new ErpSalePriceDO();
                        updateSalePrice.setId(existSalePrice.getId());
                        updateSalePrice.setGroupProductId(comboProduct.getId()).setCreator(username).setCreateTime(now);

                        // 只更新导入文件中提供的字段
                        if (importVO.getCustomerName() != null) {
                            updateSalePrice.setCustomerName(importVO.getCustomerName());
                        }
                        if (importVO.getDistributionPrice() != null) {
                            updateSalePrice.setDistributionPrice(importVO.getDistributionPrice());
                        }
                        if (importVO.getWholesalePrice() != null) {
                            updateSalePrice.setWholesalePrice(importVO.getWholesalePrice());
                        }
                        if (importVO.getShippingFeeType() != null) {
                            updateSalePrice.setShippingFeeType(importVO.getShippingFeeType());
                        }
                        if (importVO.getFixedShippingFee() != null) {
                            updateSalePrice.setFixedShippingFee(importVO.getFixedShippingFee());
                        }
                        if (importVO.getAdditionalItemQuantity() != null) {
                            updateSalePrice.setAdditionalItemQuantity(importVO.getAdditionalItemQuantity());
                        }
                        if (importVO.getAdditionalItemPrice() != null) {
                            updateSalePrice.setAdditionalItemPrice(importVO.getAdditionalItemPrice());
                        }
                        if (importVO.getFirstWeight() != null) {
                            updateSalePrice.setFirstWeight(importVO.getFirstWeight());
                        }
                        if (importVO.getFirstWeightPrice() != null) {
                            updateSalePrice.setFirstWeightPrice(importVO.getFirstWeightPrice());
                        }
                        if (importVO.getAdditionalWeight() != null) {
                            updateSalePrice.setAdditionalWeight(importVO.getAdditionalWeight());
                        }
                        if (importVO.getAdditionalWeightPrice() != null) {
                            updateSalePrice.setAdditionalWeightPrice(importVO.getAdditionalWeightPrice());
                        }
                        if (importVO.getRemark() != null) {
                            updateSalePrice.setRemark(importVO.getRemark());
                        }

                        // 设置组品相关信息（始终使用组品的数据）
                        updateSalePrice.setProductName(comboProduct.getName());
                        updateSalePrice.setProductShortName(comboProduct.getShortName());
                        updateSalePrice.setProductImage(comboProduct.getImage());

                        toUpdateList.add(updateSalePrice);
                        respVO.getUpdateNames().add(existSalePrice.getNo());
                        processedIds.add(updateSalePrice.getId());

                    } else {
                        throw exception(SALE_PRICE_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
                    }

                } catch (ServiceException ex) {
                    respVO.getFailureNames().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    System.err.println("导入第" + (i + 1) + "行数据异常: " + ex.getMessage());
                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 5. 批量执行数据库操作
            try {
                // 批量插入
                if (CollUtil.isNotEmpty(toCreateList)) {
                    batchInsertSalePrices(toCreateList);
                    // 收集新创建的ID用于ES同步
                    toCreateList.forEach(item -> processedIds.add(item.getId()));
                }

                // 批量更新
                if (CollUtil.isNotEmpty(toUpdateList)) {
                    batchUpdateSalePrices(toUpdateList);
                }

                // 6. 批量同步到ES（同步执行，确保导入后立即可搜索）
                if (CollUtil.isNotEmpty(processedIds)) {
                    batchSyncToESWithFullData(processedIds);
                }

            } catch (Exception e) {
                System.err.println("批量操作数据库失败: " + e.getMessage());
                throw new RuntimeException("批量导入失败: " + e.getMessage(), e);
            }

        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        } finally {
            // 清除转换错误
            ConversionErrorHolder.clearErrors();
        }

        System.out.println("导入完成，成功创建：" + respVO.getCreateNames().size() +
                          "，成功更新：" + respVO.getUpdateNames().size() +
                          "，失败：" + respVO.getFailureNames().size());
        return respVO;
    }

    /**
     * 统一校验所有导入数据（包括数据类型校验和业务逻辑校验）
     * 如果出现任何错误信息都记录下来并返回，后续操作就不进行了
     */
    private Map<String, String> validateAllImportData(List<ErpSalePriceImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量预加载组品信息到缓存
        Set<String> groupProductNos = importList.stream()
                .map(ErpSalePriceImportExcelVO::getGroupProductNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, ErpComboProductDO> groupProductMap = preloadComboProducts(groupProductNos);

        // 3. 批量查询已存在的销售价格记录
        Set<String> noSet = importList.stream()
                .map(ErpSalePriceImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, ErpSalePriceDO> existingSalePriceMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(noSet)) {
            List<ErpSalePriceDO> existList = erpSalePriceMapper.selectListByNoIn(noSet);
            existingSalePriceMap = convertMap(existList, ErpSalePriceDO::getNo);
        }

        // 4. 批量查询所有客户名称，验证客户是否存在
        Set<String> customerNames = importList.stream()
                .map(ErpSalePriceImportExcelVO::getCustomerName)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, Boolean> customerExistsMap = new HashMap<>();
        for (String customerName : customerNames) {
            List<ErpCustomerSaveReqVO> customers = erpCustomerService.searchCustomers(
                    new ErpCustomerPageReqVO().setName(customerName));
            customerExistsMap.put(customerName, CollUtil.isNotEmpty(customers));
        }

        // 5. 批量查询已存在的客户名称+组品ID组合
        List<ErpSalePriceDO> allExistingPrices = erpSalePriceMapper.selectList(null);
        Map<String, Set<Long>> customerProductMap = new HashMap<>();
        for (ErpSalePriceDO price : allExistingPrices) {
            customerProductMap.computeIfAbsent(price.getCustomerName(), k -> new HashSet<>())
                    .add(price.getGroupProductId());
        }

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();

        // 6. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpSalePriceImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (importVO.getGroupProductNo()+importVO.getCustomerName());

            try {
                // 6.1 基础数据校验
                if (StrUtil.isEmpty(importVO.getGroupProductNo())) {
                    allErrors.put(errorKey, "组品编号不能为空");
                    continue;
                }

                if (StrUtil.isEmpty(importVO.getCustomerName())) {
                    allErrors.put(errorKey, "客户名称不能为空");
                    continue;
                }

                // 6.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "销售价格编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 6.3 校验组品编号是否存在
                ErpComboProductDO comboProduct = groupProductMap.get(importVO.getGroupProductNo());
                if (comboProduct == null) {
                    allErrors.put(errorKey, "组品编号不存在: " + importVO.getGroupProductNo());
                    continue;
                }

                // 6.4 校验客户是否存在
                Boolean customerExists = customerExistsMap.get(importVO.getCustomerName());
                if (customerExists == null || !customerExists) {
                    allErrors.put(errorKey, "客户不存在: " + importVO.getCustomerName());
                    continue;
                }

                // 6.5 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpSalePriceDO salePrice = convertImportVOToDO(importVO, comboProduct);
                    if (salePrice == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 6.6 判断是新增还是更新，并进行相应校验
                ErpSalePriceDO existSalePrice = existingSalePriceMap.get(importVO.getNo());
                if (existSalePrice == null) {
                    // 新增校验：校验客户名称+组品ID的唯一性
                    try {
                        validateCustomerProductUniqueWithCache(importVO.getCustomerName(), comboProduct.getId(), customerProductMap, null);
                    } catch (ServiceException ex) {
                        allErrors.put(errorKey, ex.getMessage());
                    }
                } else if (isUpdateSupport) {
                    // 更新校验：校验客户名称+组品ID的唯一性（排除自身）
                    try {
                        validateCustomerProductUniqueWithCache(importVO.getCustomerName(), comboProduct.getId(), customerProductMap, existSalePrice.getId());
                    } catch (ServiceException ex) {
                        allErrors.put(errorKey, ex.getMessage());
                    }
                } else {
                    allErrors.put(errorKey, "销售价格编号不存在且不支持更新: " + importVO.getNo());
                }
            } catch (Exception ex) {
                allErrors.put(errorKey, "系统异常: " + ex.getMessage());
            }
        }

        return allErrors;
    }

    /**
     * 数据类型校验前置检查
     * 检查所有转换错误，如果有错误则返回错误信息，不进行后续导入
     */
    private Map<String, String> validateDataTypeErrors(List<ErpSalePriceImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取客户名称 - 修复行号索引问题
                String customerName = "未知组品编号+客户";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpSalePriceImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                        customerName = importVO.getNo()+importVO.getCustomerName();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + customerName + ")";
                List<String> errorMessages = new ArrayList<>();

                for (ConversionErrorHolder.ConversionError error : errors) {
                    errorMessages.add(error.getErrorMessage());
                }

                String errorMsg = String.join("; ", errorMessages);
                dataTypeErrors.put(errorKey, "数据类型错误: " + errorMsg);
            }
        }

        return dataTypeErrors;
    }

    /**
     * 将导入VO转换为DO
     * 特别注意处理字段类型转换
     */
    private ErpSalePriceDO convertImportVOToDO(ErpSalePriceImportExcelVO importVO, ErpComboProductDO comboProduct) {
        if (importVO == null) {
            return null;
        }

        // 添加调试信息
        System.out.println("=== 销售价格转换调试信息 ===");
        System.out.println("客户名称: " + importVO.getCustomerName());
        System.out.println("组品编号: " + importVO.getGroupProductNo());
        System.out.println("代发单价: " + importVO.getDistributionPrice() + " (类型: " + (importVO.getDistributionPrice() != null ? importVO.getDistributionPrice().getClass().getSimpleName() : "null") + ")");
        System.out.println("批发单价: " + importVO.getWholesalePrice() + " (类型: " + (importVO.getWholesalePrice() != null ? importVO.getWholesalePrice().getClass().getSimpleName() : "null") + ")");
        System.out.println("固定运费: " + importVO.getFixedShippingFee() + " (类型: " + (importVO.getFixedShippingFee() != null ? importVO.getFixedShippingFee().getClass().getSimpleName() : "null") + ")");
        System.out.println("==================");

        // 使用BeanUtils进行基础转换
        ErpSalePriceDO salePrice = BeanUtils.toBean(importVO, ErpSalePriceDO.class);

        // 手动设置转换器处理的字段，确保数据正确传递
        salePrice.setDistributionPrice(importVO.getDistributionPrice());
        salePrice.setWholesalePrice(importVO.getWholesalePrice());
        salePrice.setShippingFeeType(importVO.getShippingFeeType());
        salePrice.setFixedShippingFee(importVO.getFixedShippingFee());
        salePrice.setAdditionalItemQuantity(importVO.getAdditionalItemQuantity());
        salePrice.setAdditionalItemPrice(importVO.getAdditionalItemPrice());
        salePrice.setFirstWeight(importVO.getFirstWeight());
        salePrice.setFirstWeightPrice(importVO.getFirstWeightPrice());
        salePrice.setAdditionalWeight(importVO.getAdditionalWeight());
        salePrice.setAdditionalWeightPrice(importVO.getAdditionalWeightPrice());

        // 设置组品ID
        if (comboProduct != null) {
            salePrice.setGroupProductId(comboProduct.getId());
            // 设置组品相关信息（始终使用组品的数据）
            salePrice.setProductName(comboProduct.getName());
            salePrice.setProductShortName(comboProduct.getShortName());
            salePrice.setProductImage(comboProduct.getImage());
        }

        // 添加转换后的调试信息
        System.out.println("=== 转换后调试信息 ===");
        System.out.println("客户名称: " + salePrice.getCustomerName());
        System.out.println("组品ID: " + salePrice.getGroupProductId());
        System.out.println("代发单价: " + salePrice.getDistributionPrice() + " (类型: " + (salePrice.getDistributionPrice() != null ? salePrice.getDistributionPrice().getClass().getSimpleName() : "null") + ")");
        System.out.println("批发单价: " + salePrice.getWholesalePrice() + " (类型: " + (salePrice.getWholesalePrice() != null ? salePrice.getWholesalePrice().getClass().getSimpleName() : "null") + ")");
        System.out.println("固定运费: " + salePrice.getFixedShippingFee() + " (类型: " + (salePrice.getFixedShippingFee() != null ? salePrice.getFixedShippingFee().getClass().getSimpleName() : "null") + ")");
        System.out.println("==================");

        return salePrice;
    }

    /**
     * 预加载组品信息到缓存
     */
    private Map<String, ErpComboProductDO> preloadComboProducts(Set<String> groupProductNos) {
        if (CollUtil.isEmpty(groupProductNos)) {
            return Collections.emptyMap();
        }

        List<ErpComboProductDO> comboProducts = erpComboMapper.selectList(
            new LambdaQueryWrapper<ErpComboProductDO>()
                .in(ErpComboProductDO::getNo, groupProductNos)
        );

        Map<String, ErpComboProductDO> result = convertMap(comboProducts, ErpComboProductDO::getNo);

        // 更新缓存
        comboProducts.forEach(combo -> comboProductCache.put(combo.getNo(), combo));

        return result;
    }

    /**
     * 批量插入销售价格
     */
    private void batchInsertSalePrices(List<ErpSalePriceDO> salePrices) {
        // 分批插入，避免SQL过长
        int batchSize = 500;
        for (int i = 0; i < salePrices.size(); i += batchSize) {
            int end = Math.min(i + batchSize, salePrices.size());
            List<ErpSalePriceDO> batch = salePrices.subList(i, end);

            for (ErpSalePriceDO salePrice : batch) {
                erpSalePriceMapper.insert(salePrice);
            }
        }
    }

    /**
     * 批量更新销售价格
     */
    private void batchUpdateSalePrices(List<ErpSalePriceDO> salePrices) {
        for (ErpSalePriceDO salePrice : salePrices) {
            // 使用 LambdaUpdateWrapper 进行选择性更新
            LambdaUpdateWrapper<ErpSalePriceDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ErpSalePriceDO::getId, salePrice.getId());

            // 只更新非null的字段
            if (salePrice.getGroupProductId() != null) {
                updateWrapper.set(ErpSalePriceDO::getGroupProductId, salePrice.getGroupProductId());
            }
            if (salePrice.getCustomerName() != null) {
                updateWrapper.set(ErpSalePriceDO::getCustomerName, salePrice.getCustomerName());
            }
            if (salePrice.getDistributionPrice() != null) {
                updateWrapper.set(ErpSalePriceDO::getDistributionPrice, salePrice.getDistributionPrice());
            }
            if (salePrice.getWholesalePrice() != null) {
                updateWrapper.set(ErpSalePriceDO::getWholesalePrice, salePrice.getWholesalePrice());
            }
            if (salePrice.getShippingFeeType() != null) {
                updateWrapper.set(ErpSalePriceDO::getShippingFeeType, salePrice.getShippingFeeType());
            }
            if (salePrice.getFixedShippingFee() != null) {
                updateWrapper.set(ErpSalePriceDO::getFixedShippingFee, salePrice.getFixedShippingFee());
            }
            if (salePrice.getAdditionalItemQuantity() != null) {
                updateWrapper.set(ErpSalePriceDO::getAdditionalItemQuantity, salePrice.getAdditionalItemQuantity());
            }
            if (salePrice.getAdditionalItemPrice() != null) {
                updateWrapper.set(ErpSalePriceDO::getAdditionalItemPrice, salePrice.getAdditionalItemPrice());
            }
            if (salePrice.getFirstWeight() != null) {
                updateWrapper.set(ErpSalePriceDO::getFirstWeight, salePrice.getFirstWeight());
            }
            if (salePrice.getFirstWeightPrice() != null) {
                updateWrapper.set(ErpSalePriceDO::getFirstWeightPrice, salePrice.getFirstWeightPrice());
            }
            if (salePrice.getAdditionalWeight() != null) {
                updateWrapper.set(ErpSalePriceDO::getAdditionalWeight, salePrice.getAdditionalWeight());
            }
            if (salePrice.getAdditionalWeightPrice() != null) {
                updateWrapper.set(ErpSalePriceDO::getAdditionalWeightPrice, salePrice.getAdditionalWeightPrice());
            }
            if (salePrice.getRemark() != null) {
                updateWrapper.set(ErpSalePriceDO::getRemark, salePrice.getRemark());
            }
            // 组品相关信息
            if (salePrice.getProductName() != null) {
                updateWrapper.set(ErpSalePriceDO::getProductName, salePrice.getProductName());
            }
            if (salePrice.getProductShortName() != null) {
                updateWrapper.set(ErpSalePriceDO::getProductShortName, salePrice.getProductShortName());
            }
            if (salePrice.getProductImage() != null) {
                updateWrapper.set(ErpSalePriceDO::getProductImage, salePrice.getProductImage());
            }

            erpSalePriceMapper.update(null, updateWrapper);
        }
    }

    /**
     * 批量同步数据到ES（增强版，包含创建人和时间信息）
     */
    private void batchSyncToESWithFullData(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        try {
            // 从数据库查询完整数据，包含创建人、创建时间等信息
            List<ErpSalePriceDO> salePrices = erpSalePriceMapper.selectBatchIds(ids);
            List<ErpSalePriceESDO> esList = salePrices.stream()
                    .map(this::convertToES)
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(esList)) {
                salePriceESRepository.saveAll(esList);
                // 强制刷新ES索引
                try {
                    elasticsearchRestTemplate.indexOps(ErpSalePriceESDO.class).refresh();
                } catch (Exception e) {
                    System.err.println("ES索引刷新失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("批量同步ES失败: " + e.getMessage());
        }
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        comboProductCache.clear();
        comboRespVOCache.clear();
    }

    /**
     * 手动全量同步数据到ES
     */
    @Override
    public void manualFullSyncToES() {
        System.out.println("开始手动全量同步销售价格数据到ES...");

        // 先清空ES索引
        try {
            salePriceESRepository.deleteAll();
            System.out.println("已清空ES索引");
        } catch (Exception e) {
            System.err.println("清空ES索引失败: " + e.getMessage());
        }

        // 重新全量同步
        fullSyncToES();

        System.out.println("手动全量同步完成");
    }

    /**
     * 创建智能匹配查询
     * 根据关键词长度智能选择匹配策略
     *
     * @param fieldName 字段名
     * @param keyword 关键词
     * @param singleCharBoost 单字搜索权重
     * @param doubleCharBoost 双字搜索权重
     * @param multiCharBoost 多字搜索权重
     * @return 智能匹配查询
     */
    private BoolQueryBuilder createIntelligentMatchQuery(String fieldName, String keyword,
                                                        float singleCharBoost, float doubleCharBoost, float multiCharBoost) {
        BoolQueryBuilder intelligentQuery = QueryBuilders.boolQuery();

        if (keyword.length() == 1) {
            // 单字搜索，使用OR匹配，确保能找到包含该字的结果
            intelligentQuery.should(QueryBuilders.matchQuery(fieldName, keyword).operator(Operator.OR).boost(singleCharBoost));
        } else if (keyword.length() == 2) {
            // 双字搜索，使用AND匹配避免误匹配，同时添加短语匹配提高精确度
            intelligentQuery.should(QueryBuilders.matchQuery(fieldName, keyword).operator(Operator.AND).boost(doubleCharBoost));
            intelligentQuery.should(QueryBuilders.matchPhraseQuery(fieldName, keyword).boost(doubleCharBoost * 1.5f));
        } else {
            // 多字搜索，使用严格的AND匹配和短语匹配
            intelligentQuery.should(QueryBuilders.matchQuery(fieldName, keyword).operator(Operator.AND).boost(multiCharBoost));
            intelligentQuery.should(QueryBuilders.matchPhraseQuery(fieldName, keyword).boost(multiCharBoost * 1.5f));
        }

        intelligentQuery.minimumShouldMatch(1);
        return intelligentQuery;
    }

    @Override
    public PageResult<ErpDistributionMissingPriceVO> getDistributionMissingPrices(ErpSalePricePageReqVO pageReqVO) {
        try {
            // 调用代发服务获取缺失价格记录
            return distributionService.getDistributionMissingPrices(pageReqVO);
        } catch (Exception e) {
            System.err.println("获取代发缺失价格记录失败: " + e.getMessage());
            return new PageResult<>(Collections.emptyList(), 0L);
        }
    }

    @Override
    public PageResult<ErpWholesaleMissingPriceVO> getWholesaleMissingPrices(ErpSalePricePageReqVO pageReqVO) {
        try {
            // 调用批发服务获取缺失价格记录
            return wholesaleService.getWholesaleMissingPrices(pageReqVO);
        } catch (Exception e) {
            System.err.println("获取批发缺失价格记录失败: " + e.getMessage());
            return new PageResult<>(Collections.emptyList(), 0L);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSetDistributionPrices(List<ErpDistributionPriceSetReqVO> reqList) {
        if (CollUtil.isEmpty(reqList)) {
            return;
        }

        List<ErpSalePriceDO> toCreateList = new ArrayList<>();
        List<ErpSalePriceDO> toUpdateList = new ArrayList<>();

        for (ErpDistributionPriceSetReqVO req : reqList) {
            // 查找是否已存在该组品和客户的价格记录
            ErpSalePriceDO existing = erpSalePriceMapper.selectOne(
                new LambdaQueryWrapper<ErpSalePriceDO>()
                    .eq(ErpSalePriceDO::getGroupProductId, req.getGroupProductId())
                    .eq(ErpSalePriceDO::getCustomerName, req.getCustomerName())
            );

            if (existing != null) {
                // 更新现有记录的代发单价
                existing.setDistributionPrice(req.getDistributionPrice());
                toUpdateList.add(existing);
            } else {
                // 创建新的价格记录
                ErpSalePriceDO newRecord = new ErpSalePriceDO();
                newRecord.setGroupProductId(req.getGroupProductId());
                newRecord.setCustomerName(req.getCustomerName());
                newRecord.setDistributionPrice(req.getDistributionPrice());

                // 获取组品信息设置产品名称等
                try {
                    ErpComboRespVO comboInfo = getComboRespVOFromCache(req.getGroupProductId());
                    if (comboInfo != null) {
                        newRecord.setProductName(comboInfo.getName());
                        newRecord.setProductShortName(comboInfo.getShortName());
                        newRecord.setProductImage(comboInfo.getImage());
                    }
                } catch (Exception e) {
                    System.err.println("获取组品信息失败: " + e.getMessage());
                }

                toCreateList.add(newRecord);
            }
        }

        // 批量创建和更新
        if (CollUtil.isNotEmpty(toCreateList)) {
            batchInsertSalePrices(toCreateList);
            // 同步到ES
            List<Long> newIds = toCreateList.stream().map(ErpSalePriceDO::getId).collect(Collectors.toList());
            batchSyncToESWithFullData(newIds);
        }

        if (CollUtil.isNotEmpty(toUpdateList)) {
            batchUpdateSalePrices(toUpdateList);
            // 同步到ES
            List<Long> updateIds = toUpdateList.stream().map(ErpSalePriceDO::getId).collect(Collectors.toList());
            batchSyncToESWithFullData(updateIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSetWholesalePrices(List<ErpWholesalePriceSetReqVO> reqList) {
        if (CollUtil.isEmpty(reqList)) {
            return;
        }

        List<ErpSalePriceDO> toCreateList = new ArrayList<>();
        List<ErpSalePriceDO> toUpdateList = new ArrayList<>();

        for (ErpWholesalePriceSetReqVO req : reqList) {
            // 查找是否已存在该组品和客户的价格记录
            ErpSalePriceDO existing = erpSalePriceMapper.selectOne(
                new LambdaQueryWrapper<ErpSalePriceDO>()
                    .eq(ErpSalePriceDO::getGroupProductId, req.getGroupProductId())
                    .eq(ErpSalePriceDO::getCustomerName, req.getCustomerName())
            );

            if (existing != null) {
                // 更新现有记录的批发单价
                existing.setWholesalePrice(req.getWholesalePrice());
                toUpdateList.add(existing);
            } else {
                // 创建新的价格记录
                ErpSalePriceDO newRecord = new ErpSalePriceDO();
                newRecord.setGroupProductId(req.getGroupProductId());
                newRecord.setCustomerName(req.getCustomerName());
                newRecord.setWholesalePrice(req.getWholesalePrice());

                // 获取组品信息设置产品名称等
                try {
                    ErpComboRespVO comboInfo = getComboRespVOFromCache(req.getGroupProductId());
                    if (comboInfo != null) {
                        newRecord.setProductName(comboInfo.getName());
                        newRecord.setProductShortName(comboInfo.getShortName());
                        newRecord.setProductImage(comboInfo.getImage());
                    }
                } catch (Exception e) {
                    System.err.println("获取组品信息失败: " + e.getMessage());
                }

                toCreateList.add(newRecord);
            }
        }

        // 批量创建和更新
        if (CollUtil.isNotEmpty(toCreateList)) {
            batchInsertSalePrices(toCreateList);
            // 同步到ES
            List<Long> newIds = toCreateList.stream().map(ErpSalePriceDO::getId).collect(Collectors.toList());
            batchSyncToESWithFullData(newIds);
        }

        if (CollUtil.isNotEmpty(toUpdateList)) {
            batchUpdateSalePrices(toUpdateList);
            // 同步到ES
            List<Long> updateIds = toUpdateList.stream().map(ErpSalePriceDO::getId).collect(Collectors.toList());
            batchSyncToESWithFullData(updateIds);
        }
    }

    @Override
    public PageResult<ErpCombinedMissingPriceVO> getCombinedMissingPrices(ErpSalePricePageReqVO pageReqVO) {
        try {
            System.out.println("=== 获取统一缺失价格记录 ===");

            // 获取代发缺失价格记录
            PageResult<ErpDistributionMissingPriceVO> distributionResult = distributionService.getDistributionMissingPrices(pageReqVO);

            // 获取批发缺失价格记录
            PageResult<ErpWholesaleMissingPriceVO> wholesaleResult = wholesaleService.getWholesaleMissingPrices(pageReqVO);

            // 合并数据，按组品ID+客户名称分组
            Map<String, ErpCombinedMissingPriceVO> combinedMap = new HashMap<>();

            // 处理代发数据
            for (ErpDistributionMissingPriceVO distributionVO : distributionResult.getList()) {
                String key = distributionVO.getComboProductId() + "_" + distributionVO.getCustomerName();
                ErpCombinedMissingPriceVO combined = combinedMap.computeIfAbsent(key, k -> {
                    ErpCombinedMissingPriceVO vo = new ErpCombinedMissingPriceVO();
                    vo.setComboProductId(distributionVO.getComboProductId());
                    vo.setComboProductNo(distributionVO.getComboProductNo());
                    vo.setProductName(distributionVO.getProductName());
                    vo.setCustomerName(distributionVO.getCustomerName());
                    vo.setCurrentDistributionPrice(distributionVO.getDistributionPrice());
                    return vo;
                });

                // 设置代发信息
                ErpCombinedMissingPriceVO.DistributionOrderInfo distributionInfo = new ErpCombinedMissingPriceVO.DistributionOrderInfo();
                distributionInfo.setOrderCount(distributionVO.getOrderCount());
                distributionInfo.setTotalProductQuantity(distributionVO.getTotalProductQuantity());
                distributionInfo.setOrderNumbers(distributionVO.getOrderNumbers());
                distributionInfo.setOrderIds(distributionVO.getOrderIds());
                distributionInfo.setEarliestCreateTime(distributionVO.getEarliestCreateTime());
                distributionInfo.setLatestCreateTime(distributionVO.getLatestCreateTime());
                combined.setDistributionInfo(distributionInfo);
            }

            // 处理批发数据
            for (ErpWholesaleMissingPriceVO wholesaleVO : wholesaleResult.getList()) {
                String key = wholesaleVO.getComboProductId() + "_" + wholesaleVO.getCustomerName();
                ErpCombinedMissingPriceVO combined = combinedMap.computeIfAbsent(key, k -> {
                    ErpCombinedMissingPriceVO vo = new ErpCombinedMissingPriceVO();
                    vo.setComboProductId(wholesaleVO.getComboProductId());
                    vo.setComboProductNo(wholesaleVO.getComboProductNo());
                    vo.setProductName(wholesaleVO.getProductName());
                    vo.setCustomerName(wholesaleVO.getCustomerName());
                    vo.setCurrentWholesalePrice(wholesaleVO.getWholesalePrice());
                    return vo;
                });

                // 设置批发信息
                ErpCombinedMissingPriceVO.WholesaleOrderInfo wholesaleInfo = new ErpCombinedMissingPriceVO.WholesaleOrderInfo();
                wholesaleInfo.setOrderCount(wholesaleVO.getOrderCount());
                wholesaleInfo.setTotalProductQuantity(wholesaleVO.getTotalProductQuantity());
                wholesaleInfo.setOrderNumbers(wholesaleVO.getOrderNumbers());
                wholesaleInfo.setOrderIds(wholesaleVO.getOrderIds());
                wholesaleInfo.setEarliestCreateTime(wholesaleVO.getEarliestCreateTime());
                wholesaleInfo.setLatestCreateTime(wholesaleVO.getLatestCreateTime());
                combined.setWholesaleInfo(wholesaleInfo);
            }

            // 查询当前销售价格表中的价格信息
            for (ErpCombinedMissingPriceVO combined : combinedMap.values()) {
                try {
                    ErpSalePriceRespVO currentPrice = getSalePriceByGroupProductIdAndCustomerName(
                        combined.getComboProductId(), combined.getCustomerName());
                    if (currentPrice != null) {
                        if (combined.getCurrentDistributionPrice() == null) {
                            combined.setCurrentDistributionPrice(currentPrice.getDistributionPrice());
                        }
                        if (combined.getCurrentWholesalePrice() == null) {
                            combined.setCurrentWholesalePrice(currentPrice.getWholesalePrice());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("查询当前价格失败: " + e.getMessage());
                }
            }

            // 转换为列表并分页
            List<ErpCombinedMissingPriceVO> resultList = new ArrayList<>(combinedMap.values());

            // 简单分页处理
            int start = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();
            int end = Math.min(start + pageReqVO.getPageSize(), resultList.size());

            List<ErpCombinedMissingPriceVO> pageList = start < resultList.size() ?
                resultList.subList(start, end) : Collections.emptyList();

            System.out.println("统一缺失价格记录数量: " + resultList.size());
            return new PageResult<>(pageList, (long) resultList.size());

        } catch (Exception e) {
            System.err.println("获取统一缺失价格记录失败: " + e.getMessage());
            e.printStackTrace();
            return new PageResult<>(Collections.emptyList(), 0L);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSetCombinedPrices(List<ErpCombinedPriceSetReqVO> reqList) {
        if (CollUtil.isEmpty(reqList)) {
            return;
        }

        System.out.println("=== 批量设置统一价格 ===");
        System.out.println("设置价格请求数量: " + reqList.size());

        List<ErpSalePriceDO> toCreateList = new ArrayList<>();
        List<ErpSalePriceDO> toUpdateList = new ArrayList<>();

        for (ErpCombinedPriceSetReqVO req : reqList) {
            System.out.println("处理组品: " + req.getGroupProductId() + ", 客户: " + req.getCustomerName());

            // 查找是否已存在该组品和客户的价格记录
            ErpSalePriceDO existing = erpSalePriceMapper.selectOne(
                new LambdaQueryWrapper<ErpSalePriceDO>()
                    .eq(ErpSalePriceDO::getGroupProductId, req.getGroupProductId())
                    .eq(ErpSalePriceDO::getCustomerName, req.getCustomerName())
            );

            if (existing != null) {
                // 更新现有记录
                System.out.println("更新现有记录，ID: " + existing.getId());

                // 只更新非null的价格
                if (req.getDistributionPrice() != null) {
                    existing.setDistributionPrice(req.getDistributionPrice());
                    System.out.println("设置代发单价: " + req.getDistributionPrice());
                }
                if (req.getWholesalePrice() != null) {
                    existing.setWholesalePrice(req.getWholesalePrice());
                    System.out.println("设置批发单价: " + req.getWholesalePrice());
                }

                // 设置运费信息
                if (req.getShippingFeeType() != null) {
                    existing.setShippingFeeType(req.getShippingFeeType());
                    System.out.println("设置运费类型: " + req.getShippingFeeType());
                }
                if (req.getFixedShippingFee() != null) {
                    existing.setFixedShippingFee(req.getFixedShippingFee());
                    System.out.println("设置固定运费: " + req.getFixedShippingFee());
                }
                if (req.getAdditionalItemQuantity() != null) {
                    existing.setAdditionalItemQuantity(req.getAdditionalItemQuantity());
                }
                if (req.getAdditionalItemPrice() != null) {
                    existing.setAdditionalItemPrice(req.getAdditionalItemPrice());
                }
                if (req.getFirstWeight() != null) {
                    existing.setFirstWeight(req.getFirstWeight());
                }
                if (req.getFirstWeightPrice() != null) {
                    existing.setFirstWeightPrice(req.getFirstWeightPrice());
                }
                if (req.getAdditionalWeight() != null) {
                    existing.setAdditionalWeight(req.getAdditionalWeight());
                }
                if (req.getAdditionalWeightPrice() != null) {
                    existing.setAdditionalWeightPrice(req.getAdditionalWeightPrice());
                }

                toUpdateList.add(existing);
            } else {
                // 创建新的价格记录
                System.out.println("创建新的价格记录");

                ErpSalePriceDO newRecord = new ErpSalePriceDO();

                // 生成编号
                String no = noRedisDAO.generate(ErpNoRedisDAO.SALE_PRICE_NO_PREFIX);
                newRecord.setNo(no);

                newRecord.setGroupProductId(req.getGroupProductId());
                newRecord.setCustomerName(req.getCustomerName());

                if (req.getDistributionPrice() != null) {
                    newRecord.setDistributionPrice(req.getDistributionPrice());
                    System.out.println("设置代发单价: " + req.getDistributionPrice());
                }
                if (req.getWholesalePrice() != null) {
                    newRecord.setWholesalePrice(req.getWholesalePrice());
                    System.out.println("设置批发单价: " + req.getWholesalePrice());
                }

                // 设置运费信息
                if (req.getShippingFeeType() != null) {
                    newRecord.setShippingFeeType(req.getShippingFeeType());
                    System.out.println("设置运费类型: " + req.getShippingFeeType());
                }
                if (req.getFixedShippingFee() != null) {
                    newRecord.setFixedShippingFee(req.getFixedShippingFee());
                    System.out.println("设置固定运费: " + req.getFixedShippingFee());
                }
                if (req.getAdditionalItemQuantity() != null) {
                    newRecord.setAdditionalItemQuantity(req.getAdditionalItemQuantity());
                }
                if (req.getAdditionalItemPrice() != null) {
                    newRecord.setAdditionalItemPrice(req.getAdditionalItemPrice());
                }
                if (req.getFirstWeight() != null) {
                    newRecord.setFirstWeight(req.getFirstWeight());
                }
                if (req.getFirstWeightPrice() != null) {
                    newRecord.setFirstWeightPrice(req.getFirstWeightPrice());
                }
                if (req.getAdditionalWeight() != null) {
                    newRecord.setAdditionalWeight(req.getAdditionalWeight());
                }
                if (req.getAdditionalWeightPrice() != null) {
                    newRecord.setAdditionalWeightPrice(req.getAdditionalWeightPrice());
                }

                // 获取组品信息设置产品名称等
                try {
                    ErpComboRespVO comboInfo = getComboRespVOFromCache(req.getGroupProductId());
                    if (comboInfo != null) {
                        newRecord.setProductName(comboInfo.getName());
                        newRecord.setProductShortName(comboInfo.getShortName());
                        newRecord.setProductImage(comboInfo.getImage());
                        System.out.println("设置产品信息: " + comboInfo.getName());
                    }
                } catch (Exception e) {
                    System.err.println("获取组品信息失败: " + e.getMessage());
                }

                toCreateList.add(newRecord);
            }
        }

        // 批量创建和更新
        if (CollUtil.isNotEmpty(toCreateList)) {
            System.out.println("批量创建记录数量: " + toCreateList.size());
            batchInsertSalePrices(toCreateList);
            // 同步到ES
            List<Long> newIds = toCreateList.stream().map(ErpSalePriceDO::getId).collect(Collectors.toList());
            batchSyncToESWithFullData(newIds);
        }

        if (CollUtil.isNotEmpty(toUpdateList)) {
            System.out.println("批量更新记录数量: " + toUpdateList.size());
            batchUpdateSalePrices(toUpdateList);
            // 同步到ES
            List<Long> updateIds = toUpdateList.stream().map(ErpSalePriceDO::getId).collect(Collectors.toList());
            batchSyncToESWithFullData(updateIds);
        }

        System.out.println("批量设置统一价格完成");
    }
}
