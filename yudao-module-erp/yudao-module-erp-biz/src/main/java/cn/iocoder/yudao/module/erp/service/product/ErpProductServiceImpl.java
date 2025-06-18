package cn.iocoder.yudao.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.search.Scroll;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.domain.PageRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
/**
 * ERP 产品 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpProductServiceImpl implements ErpProductService {

    @Resource
    private ErpProductESRepository productESRepository;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private ErpProductMapper productMapper;

    @Resource
    private ErpProductCategoryService productCategoryService;
    @Resource
    private ErpProductUnitService productUnitService;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private DictDataApi dictDataApi;
    
    // 用于存储当前搜索条件的ThreadLocal
    private static final ThreadLocal<String> CURRENT_SEARCH_NAME = new ThreadLocal<>();

    @EventListener(ApplicationReadyEvent.class)
    public void initESIndex() {
        System.out.println("开始初始化ES索引...");
        try {
            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpProductESDO.class);
            
            // 检查索引是否存在，如果存在则删除重建（确保字段映射更新）
            if (indexOps.exists()) {
                System.out.println("删除现有索引以更新字段映射...");
                indexOps.delete();
            }
            
            // 创建新索引
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping(ErpProductESDO.class));
            System.out.println("ERP产品索引创建成功");
            
            // 注意：不在启动时进行全量同步，避免租户上下文问题
            // 数据会在实际使用时自动同步
            System.out.println("ES索引初始化完成，数据将在使用时自动同步");
            
        } catch (Exception e) {
            System.err.println("ERP产品索引创建失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Long createProduct(ProductSaveReqVO createReqVO) {
        // TODO 芋艿：校验分类
        // 生成产品编号
        String no = noRedisDAO.generate(ErpNoRedisDAO.PRODUCT_NO_PREFIX);
        if (productMapper.selectByNo(no) != null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        validateProductNameUnique(createReqVO.getName(), null);

        // 插入
        ErpProductDO product = BeanUtils.toBean(createReqVO, ErpProductDO.class)
                .setNo(no);
        productMapper.insert(product);

        // 同步到ES
        syncProductToES(product.getId());
        // 返回
        return product.getId();
    }

    @Override
    public void updateProduct(ProductSaveReqVO updateReqVO) {
        // TODO 芋艿：校验分类
        // 校验存在
        validateProductExists(updateReqVO.getId());

        validateProductNameUnique(updateReqVO.getName(), updateReqVO.getId());
        // 更新
        ErpProductDO updateObj = BeanUtils.toBean(updateReqVO, ErpProductDO.class);
        productMapper.updateById(updateObj);

        // 同步到ES
        syncProductToES(updateReqVO.getId());
    }

    @Override
    public void deleteProduct(Long id) {
        // 校验存在
        validateProductExists(id);
        // 删除
        productMapper.deleteById(id);

        // 删除ES记录
        productESRepository.deleteById(id);
    }

    @Override
    public List<ErpProductDO> validProductList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = productMapper.selectBatchIds(ids);
        Map<Long, ErpProductDO> productMap = convertMap(list, ErpProductDO::getId);
        for (Long id : ids) {
            ErpProductDO product = productMap.get(id);
            if (productMap.get(id) == null) {
                throw exception(PRODUCT_NOT_EXISTS);
            }
            if (CommonStatusEnum.isDisable(product.getStatus())) {
                throw exception(PRODUCT_NOT_ENABLE, product.getName());
            }
        }
        return list;
    }

    private void validateProductExists(Long id) {
        if (productMapper.selectById(id) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    public ErpProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<ErpProductRespVO> getProductVOListByStatus(Integer status) {
        List<ErpProductDO> list = productMapper.selectListByStatus(status);
        return buildProductVOList(list);
    }

    @Override
    public List<ErpProductRespVO> getProductVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = productMapper.selectBatchIds(ids);
        return buildProductVOList(list);
    }

    @Override
    public PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO) {
        try {
            // 1. 确保ES索引存在并同步数据
            ensureESIndexAndSync();

            // 2. 构建ES查询
            return searchProductsFromES(pageReqVO);
            
        } catch (Exception e) {
            System.err.println("ES查询失败，回退到数据库查询: " + e.getMessage());
            e.printStackTrace();
            return getProductVOPageFromDB(pageReqVO);
        }
    }

    /**
     * 确保ES索引存在，仅在必要时同步数据
     */
    private void ensureESIndexAndSync() {
        try {
            // 检查ES索引是否存在
            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpProductESDO.class);
            boolean indexExists = indexOps.exists();
            
            if (!indexExists) {
                // 索引不存在，创建索引并同步数据
                System.out.println("ES索引不存在，开始创建索引...");
                indexOps.create();
                indexOps.putMapping(indexOps.createMapping(ErpProductESDO.class));
                System.out.println("ES索引创建成功");
                
                // 索引不存在时需要全量同步
                syncAllDataToES();
                return;
            }
            
            // 索引存在，检查数据量是否匹配
            long dbCount = productMapper.selectCount(null);
            long esCount = elasticsearchRestTemplate.count(new NativeSearchQueryBuilder().build(), ErpProductESDO.class);
            
            // 数据量不匹配时才需要同步（允许10条的差异）
            if (Math.abs(dbCount - esCount) > 10) {
                System.out.println("检测到数据量不匹配。数据库:" + dbCount + ", ES:" + esCount + "，开始同步数据...");
                syncAllDataToES();
            } else {
                System.out.println("ES索引和数据都正常。数据库:" + dbCount + ", ES:" + esCount);
            }
            
        } catch (Exception e) {
            System.err.println("ES索引检查失败: " + e.getMessage());
            // 检查失败时不抛出异常，让查询降级到数据库
        }
    }

    /**
     * 同步所有数据到ES
     */
    private void syncAllDataToES() {
        try {
            System.out.println("开始全量同步数据到ES...");
            
            List<ErpProductDO> products = productMapper.selectList(null);
            if (CollUtil.isEmpty(products)) {
                System.out.println("数据库中没有产品数据，跳过同步");
                return;
            }

            // 先清空ES数据
            productESRepository.deleteAll();
            
            // 批量同步数据
            List<ErpProductESDO> esList = products.stream()
                .map(this::convertProductToES)
                .collect(Collectors.toList());

            productESRepository.saveAll(esList);
            System.out.println("全量同步完成，共同步" + esList.size() + "条记录");
            
        } catch (Exception e) {
            System.err.println("全量同步数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从ES搜索产品（支持智能匹配查询和深度分页）
     * 
     * 查询策略说明：
     * 智能匹配策略，按权重优先级进行多层匹配：
     * 1. 完全精确匹配（权重1,000,000）：keyword字段完全相同
     * 2. 前缀匹配（权重100,000）：keyword字段以搜索词开头
     * 3. 通配符包含匹配（权重10,000）：keyword字段包含搜索词
     * 4. 智能分词匹配（权重100-500）：根据搜索词长度智能选择匹配策略
     *    - 单字/双字搜索：使用OR匹配，权重100（支持单字搜索）
     *    - 多字搜索：使用AND匹配，权重500（减少误匹配）
     * 
     * 关键特性：
     * - 精确匹配优先：完全匹配的结果排在最前面，避免"产品名称2"误匹配"产品名称"
     * - 支持单字搜索：搜索"品"可以匹配"产品名称"（通过分词匹配）
     * - 减少误匹配：多字搜索时要求所有分词都匹配，避免不相关结果
     * - 智能权重：分词匹配权重远低于精确匹配，确保精确结果优先
     * 
     * 示例：
     * - 搜索 "品" → 返回包含"品"字的产品（分词匹配，权重100）
     * - 搜索 "产品名称2" → 优先返回完全匹配的产品（精确匹配，权重1,000,000）
     * - 搜索 "产品名称" → 精确匹配优先，不会误匹配"产品名称2"
     * 
     * 深度分页：
     * - offset < 10000：使用普通分页
     * - offset >= 10000：自动切换到search_after机制，支持无限深度分页
     */
    private PageResult<ErpProductRespVO> searchProductsFromES(ErpProductPageReqVO pageReqVO) {
        try {
            // 设置当前搜索条件到ThreadLocal
            if (StringUtils.isNotBlank(pageReqVO.getName())) {
                CURRENT_SEARCH_NAME.set(pageReqVO.getName().trim());
            }
            
            // 1. 构建ES查询
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            
            // 全文搜索（优先级最高）
            if (StringUtils.isNotBlank(pageReqVO.getKeyword())) {
                BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
                String keyword = pageReqVO.getKeyword().trim();
                
                // 多字段搜索，使用should表示OR关系，优先精确匹配
                keywordQuery
                        // 1. 精确词匹配
                        .should(QueryBuilders.termQuery("name_keyword", keyword).boost(8.0f))
                        // 3. 分词匹配（要求所有词都匹配）
                        .should(QueryBuilders.matchQuery("name", keyword).operator(Operator.AND).boost(6.0f))
                        // 4. 包含匹配（降低权重）
                        .should(QueryBuilders.matchQuery("name", keyword).operator(Operator.OR).boost(2.0f))
                        // 5. 其他字段精确匹配
                        .should(QueryBuilders.matchPhraseQuery("product_short_name", keyword).boost(5.0f))
                        .should(QueryBuilders.matchPhraseQuery("brand", keyword).boost(4.0f))
                        .should(QueryBuilders.matchPhraseQuery("shipping_code", keyword).boost(4.0f))
                        .should(QueryBuilders.matchPhraseQuery("purchaser", keyword).boost(3.0f))
                        .should(QueryBuilders.matchPhraseQuery("supplier", keyword).boost(3.0f))
                        // 6. 其他字段分词匹配
                        .should(QueryBuilders.matchQuery("product_short_name", keyword).operator(Operator.AND).boost(2.0f))
                        .should(QueryBuilders.matchQuery("standard", keyword).operator(Operator.AND).boost(1.5f))
                        .should(QueryBuilders.matchQuery("product_selling_points", keyword).operator(Operator.AND).boost(1.0f))
                        .minimumShouldMatch(1);
                
                boolQuery.must(keywordQuery);
            } else {
                // 产品名称查询 - 智能匹配策略（精确匹配优先，分词匹配兜底）
                if (StringUtils.isNotBlank(pageReqVO.getName())) {
                    BoolQueryBuilder nameQuery = QueryBuilders.boolQuery();
                    String name = pageReqVO.getName().trim();
                    
                    BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                    
                    // 第一优先级：完全精确匹配（最高权重）
                    multiMatchQuery.should(QueryBuilders.termQuery("name_keyword", name).boost(1000000.0f));
                    
                    // 第二优先级：前缀匹配
                    multiMatchQuery.should(QueryBuilders.prefixQuery("name_keyword", name).boost(100000.0f));
                    
                    // 第三优先级：通配符包含匹配（支持中间字符搜索）
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("name_keyword", "*" + name + "*").boost(10000.0f));
                    
                    // 第四优先级：分词匹配（权重大幅降低，仅作为兜底方案）
                    // 对于单字搜索，使用较低权重的分词匹配
                    if (name.length() <= 2) {
                        // 单字或双字搜索，使用分词匹配作为兜底
                        multiMatchQuery.should(QueryBuilders.matchQuery("name", name).operator(Operator.OR).boost(100.0f));
                    } else {
                        // 多字搜索，使用更严格的分词匹配
                        multiMatchQuery.should(QueryBuilders.matchQuery("name", name).operator(Operator.AND).boost(500.0f));
                    }
                    
                    multiMatchQuery.minimumShouldMatch(1);
                    nameQuery.must(multiMatchQuery);
                    boolQuery.must(nameQuery);
                }
                
                // 产品简称查询 - 智能匹配策略
                if (StringUtils.isNotBlank(pageReqVO.getProductShortName())) {
                    BoolQueryBuilder shortNameQuery = QueryBuilders.boolQuery();
                    String shortName = pageReqVO.getProductShortName().trim();
                    
                    BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                    multiMatchQuery.should(QueryBuilders.termQuery("product_short_name_keyword", shortName).boost(1000000.0f));
                    multiMatchQuery.should(QueryBuilders.prefixQuery("product_short_name_keyword", shortName).boost(100000.0f));
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("product_short_name_keyword", "*" + shortName + "*").boost(10000.0f));
                    
                    // 智能分词匹配
                    if (shortName.length() <= 2) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("product_short_name", shortName).operator(Operator.OR).boost(100.0f));
                    } else {
                        multiMatchQuery.should(QueryBuilders.matchQuery("product_short_name", shortName).operator(Operator.AND).boost(500.0f));
                    }
                    
                    multiMatchQuery.minimumShouldMatch(1);
                    shortNameQuery.must(multiMatchQuery);
                    boolQuery.must(shortNameQuery);
                }
                
                // 发货编码查询 - 智能匹配策略
                if (StringUtils.isNotBlank(pageReqVO.getShippingCode())) {
                    BoolQueryBuilder codeQuery = QueryBuilders.boolQuery();
                    String code = pageReqVO.getShippingCode().trim();
                    
                    BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                    multiMatchQuery.should(QueryBuilders.termQuery("shipping_code_keyword", code).boost(1000000.0f));
                    multiMatchQuery.should(QueryBuilders.prefixQuery("shipping_code_keyword", code).boost(100000.0f));
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("shipping_code_keyword", "*" + code + "*").boost(10000.0f));
                    
                    // 智能分词匹配
                    if (code.length() <= 2) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("shipping_code", code).operator(Operator.OR).boost(100.0f));
                    } else {
                        multiMatchQuery.should(QueryBuilders.matchQuery("shipping_code", code).operator(Operator.AND).boost(500.0f));
                    }
                    
                    multiMatchQuery.minimumShouldMatch(1);
                    codeQuery.must(multiMatchQuery);
                    boolQuery.must(codeQuery);
                }
                
                // 品牌名称查询 - 智能匹配策略
                if (StringUtils.isNotBlank(pageReqVO.getBrand())) {
                    BoolQueryBuilder brandQuery = QueryBuilders.boolQuery();
                    String brand = pageReqVO.getBrand().trim();
                    
                    BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                    multiMatchQuery.should(QueryBuilders.termQuery("brand_keyword", brand).boost(1000000.0f));
                    multiMatchQuery.should(QueryBuilders.prefixQuery("brand_keyword", brand).boost(100000.0f));
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("brand_keyword", "*" + brand + "*").boost(10000.0f));
                    
                    // 智能分词匹配
                    if (brand.length() <= 2) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("brand", brand).operator(Operator.OR).boost(100.0f));
                    } else {
                        multiMatchQuery.should(QueryBuilders.matchQuery("brand", brand).operator(Operator.AND).boost(500.0f));
                    }
                    
                    multiMatchQuery.minimumShouldMatch(1);
                    brandQuery.must(multiMatchQuery);
                    boolQuery.must(brandQuery);
                }
                
                // 采购人员查询 - 智能匹配策略
                if (StringUtils.isNotBlank(pageReqVO.getPurchaser())) {
                    BoolQueryBuilder purchaserQuery = QueryBuilders.boolQuery();
                    String purchaser = pageReqVO.getPurchaser().trim();
                    
                    BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                    multiMatchQuery.should(QueryBuilders.termQuery("purchaser_keyword", purchaser).boost(1000000.0f));
                    multiMatchQuery.should(QueryBuilders.prefixQuery("purchaser_keyword", purchaser).boost(100000.0f));
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("purchaser_keyword", "*" + purchaser + "*").boost(10000.0f));
                    
                    // 智能分词匹配
                    if (purchaser.length() <= 2) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("purchaser", purchaser).operator(Operator.OR).boost(100.0f));
                    } else {
                        multiMatchQuery.should(QueryBuilders.matchQuery("purchaser", purchaser).operator(Operator.AND).boost(500.0f));
                    }
                    
                    multiMatchQuery.minimumShouldMatch(1);
                    purchaserQuery.must(multiMatchQuery);
                    boolQuery.must(purchaserQuery);
                }
                
                // 供应商名查询 - 智能匹配策略
                if (StringUtils.isNotBlank(pageReqVO.getSupplier())) {
                    BoolQueryBuilder supplierQuery = QueryBuilders.boolQuery();
                    String supplier = pageReqVO.getSupplier().trim();
                    
                    BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                    multiMatchQuery.should(QueryBuilders.termQuery("supplier_keyword", supplier).boost(1000000.0f));
                    multiMatchQuery.should(QueryBuilders.prefixQuery("supplier_keyword", supplier).boost(100000.0f));
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("supplier_keyword", "*" + supplier + "*").boost(10000.0f));
                    
                    // 智能分词匹配
                    if (supplier.length() <= 2) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("supplier", supplier).operator(Operator.OR).boost(100.0f));
                    } else {
                        multiMatchQuery.should(QueryBuilders.matchQuery("supplier", supplier).operator(Operator.AND).boost(500.0f));
                    }
                    
                    multiMatchQuery.minimumShouldMatch(1);
                    supplierQuery.must(multiMatchQuery);
                    boolQuery.must(supplierQuery);
                }
            }
            
            // 产品分类精确查询
            if (pageReqVO.getCategoryId() != null) {
                boolQuery.must(QueryBuilders.termQuery("category_id", pageReqVO.getCategoryId()));
            }
            
            // 产品状态精确查询
            if (pageReqVO.getStatus() != null) {
                boolQuery.must(QueryBuilders.termQuery("status", pageReqVO.getStatus()));
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
            
            // 2. 构建查询请求
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withTrackTotalHits(true)
                    .withSort(Sort.by(Sort.Direction.DESC, "_score"))  // 按相关性排序
                    .withSort(Sort.by(Sort.Direction.DESC, "id"));     // 相关性相同时按ID排序
            
            // 3. 判断是否需要深度分页
            int offset = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();
            if (offset >= 10000) {
                // 使用深度分页
                return handleDeepPagination(pageReqVO, queryBuilder, boolQuery);
            } else {
                // 普通分页
                queryBuilder.withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()));
            }
            
            // 4. 执行查询
            NativeSearchQuery finalQuery = queryBuilder.build();
            
            // 添加调试日志
            System.out.println("=== ES查询调试信息 ===");
            System.out.println("查询参数: " + pageReqVO.getName());
            System.out.println("查询语句: " + finalQuery.getQuery().toString());
            System.out.println("==================");
            
            SearchHits<ErpProductESDO> searchHits = elasticsearchRestTemplate.search(
                    finalQuery,
                    ErpProductESDO.class,
                    IndexCoordinates.of("erp_products"));
            
            // 添加结果调试日志
            System.out.println("=== ES查询结果 ===");
            System.out.println("总命中数: " + searchHits.getTotalHits());
            searchHits.getSearchHits().forEach(hit -> {
                ErpProductESDO content = hit.getContent();
                System.out.println("命中产品: ID=" + content.getId() + 
                                 ", 名称=" + content.getName() + 
                                 ", name_keyword=" + content.getNameKeyword() +
                                 ", 得分=" + hit.getScore());
            });
            System.out.println("================");
            
            // 5. 转换结果
            return convertSearchHitsToPageResult(searchHits);
            
        } catch (Exception e) {
            System.err.println("ES查询执行失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            // 清理ThreadLocal
            CURRENT_SEARCH_NAME.remove();
        }
    }

    /**
     * 处理深度分页（offset >= 10000时使用search_after）
     */
    private PageResult<ErpProductRespVO> handleDeepPagination(ErpProductPageReqVO pageReqVO, 
                                                            NativeSearchQueryBuilder queryBuilder,
                                                            BoolQueryBuilder boolQuery) {
        try {
            // 1. 如果有lastId，使用search_after
            if (pageReqVO.getLastId() != null) {
                return searchAfterPagination(pageReqVO, queryBuilder);
            }
            
            // 2. 没有lastId时，需要先获取到目标位置的最后一条记录的ID
            int targetOffset = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();
            Long searchAfterValue = findSearchAfterValue(boolQuery, targetOffset);
            
            if (searchAfterValue == null) {
                // 如果找不到search_after值，返回空结果
                return new PageResult<>(Collections.emptyList(), 0L);
            }
            
            // 3. 使用找到的search_after值进行查询
            queryBuilder.withPageable(PageRequest.of(0, pageReqVO.getPageSize()));
            
            NativeSearchQuery query = queryBuilder.build();
            query.setSearchAfter(Arrays.asList(searchAfterValue));
            
            SearchHits<ErpProductESDO> searchHits = elasticsearchRestTemplate.search(
                    query,
                    ErpProductESDO.class,
                    IndexCoordinates.of("erp_products"));
            
            return convertSearchHitsToPageResult(searchHits);
            
        } catch (Exception e) {
            System.err.println("深度分页查询失败: " + e.getMessage());
            // 降级到普通分页（可能会有性能问题，但保证功能可用）
            queryBuilder.withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()));
            SearchHits<ErpProductESDO> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpProductESDO.class,
                    IndexCoordinates.of("erp_products"));
            return convertSearchHitsToPageResult(searchHits);
        }
    }

    /**
     * 使用search_after进行分页查询
     */
    private PageResult<ErpProductRespVO> searchAfterPagination(ErpProductPageReqVO pageReqVO, 
                                                             NativeSearchQueryBuilder queryBuilder) {
        queryBuilder.withPageable(PageRequest.of(0, pageReqVO.getPageSize()));
        
        NativeSearchQuery query = queryBuilder.build();
        query.setSearchAfter(Arrays.asList(pageReqVO.getLastId()));
        
        SearchHits<ErpProductESDO> searchHits = elasticsearchRestTemplate.search(
                query,
                ErpProductESDO.class,
                IndexCoordinates.of("erp_products"));
        
        return convertSearchHitsToPageResult(searchHits);
    }

    /**
     * 查找指定偏移位置的search_after值
     */
    private Long findSearchAfterValue(BoolQueryBuilder boolQuery, int targetOffset) {
        try {
            // 分批获取，避免一次性获取过多数据
            int batchSize = Math.min(1000, targetOffset);
            int batches = (targetOffset + batchSize - 1) / batchSize;
            
            List<Object> lastSortValues = null;
            
            for (int i = 0; i < batches; i++) {
                NativeSearchQueryBuilder batchQueryBuilder = new NativeSearchQueryBuilder()
                        .withQuery(boolQuery)
                        .withPageable(PageRequest.of(0, batchSize))
                        .withSort(Sort.by(Sort.Direction.DESC, "id"))
                        .withTrackTotalHits(false); // 提高性能
                
                NativeSearchQuery batchQuery = batchQueryBuilder.build();
                if (lastSortValues != null) {
                    batchQuery.setSearchAfter(lastSortValues);
                }
                
                SearchHits<ErpProductESDO> batchHits = elasticsearchRestTemplate.search(
                        batchQuery,
                        ErpProductESDO.class,
                        IndexCoordinates.of("erp_products"));
                
                if (batchHits.isEmpty()) {
                    break;
                }
                
                // 获取最后一条记录的排序值
                SearchHit<ErpProductESDO> lastHit = batchHits.getSearchHits().get(batchHits.getSearchHits().size() - 1);
                List<Object> sortValuesList = lastHit.getSortValues();
                if (sortValuesList != null && !sortValuesList.isEmpty()) {
                    lastSortValues = new ArrayList<>(sortValuesList);
                }
            }
            
            return lastSortValues != null && !lastSortValues.isEmpty() ? (Long) lastSortValues.get(0) : null;
            
        } catch (Exception e) {
            System.err.println("查找search_after值失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 转换搜索结果为分页结果
     */
    private PageResult<ErpProductRespVO> convertSearchHitsToPageResult(SearchHits<ErpProductESDO> searchHits) {
        List<ErpProductRespVO> voList = searchHits.stream()
                .map(SearchHit::getContent)
                .map(esDO -> {
                    // 先进行基础转换
                    ErpProductRespVO vo = BeanUtils.toBean(esDO, ErpProductRespVO.class);
                    
                    // 手动处理日期字段转换
                    if (StrUtil.isNotBlank(esDO.getProductionDate())) {
                        try {
                            vo.setProductionDate(LocalDateTime.parse(esDO.getProductionDate()));
                        } catch (Exception e) {
                            System.err.println("解析生产日期失败: " + esDO.getProductionDate());
                            vo.setProductionDate(null);
                        }
                    } else {
                        vo.setProductionDate(null);
                    }
                    
                    if (StrUtil.isNotBlank(esDO.getCreateTime())) {
                        try {
                            vo.setCreateTime(LocalDateTime.parse(esDO.getCreateTime()));
                        } catch (Exception e) {
                            System.err.println("解析创建时间失败: " + esDO.getCreateTime());
                            vo.setCreateTime(null);
                        }
                    } else {
                        vo.setCreateTime(null);
                    }
                    
                    // 确保关键字段不为空
                    if (vo.getCategoryName() == null && vo.getCategoryId() != null) {
                        vo.setCategoryName(getCategoryNameById(vo.getCategoryId()));
                    }
                    if (vo.getUnitName() == null && vo.getUnitId() != null) {
                        vo.setUnitName(getUnitNameById(vo.getUnitId()));
                    }
                    
                    // 设置lastId用于下一页的search_after
                    vo.setLastId(vo.getId());
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 应用层二次排序 - 确保精确匹配优先
        voList = applySecondarySort(voList);
        
        return new PageResult<>(voList, searchHits.getTotalHits());
    }
    
    /**
     * 应用层二次排序 - 确保精确匹配优先
     */
    private List<ErpProductRespVO> applySecondarySort(List<ErpProductRespVO> voList) {
        // 如果列表为空或只有一个元素，直接返回
        if (voList == null || voList.size() <= 1) {
            return voList;
        }
        
        // 获取当前搜索条件
        String searchName = CURRENT_SEARCH_NAME.get();
        if (StrUtil.isBlank(searchName)) {
            return voList;
        }
        
        // 去掉引号（如果有）
        if (searchName.startsWith("\"") && searchName.endsWith("\"")) {
            searchName = searchName.substring(1, searchName.length() - 1);
        }
        
        final String finalSearchName = searchName;
        
        return voList.stream()
                .sorted((a, b) -> {
                    String nameA = a.getName();
                    String nameB = b.getName();
                    
                    if (nameA == null && nameB == null) return 0;
                    if (nameA == null) return 1;
                    if (nameB == null) return -1;
                    
                    // 1. 完全精确匹配优先（最高优先级）
                    boolean exactMatchA = finalSearchName.equals(nameA);
                    boolean exactMatchB = finalSearchName.equals(nameB);
                    
                    if (exactMatchA && !exactMatchB) return -1;
                    if (!exactMatchA && exactMatchB) return 1;
                    if (exactMatchA && exactMatchB) return 0;
                    
                    // 2. 前缀匹配优先
                    boolean prefixMatchA = nameA.startsWith(finalSearchName);
                    boolean prefixMatchB = nameB.startsWith(finalSearchName);
                    
                    if (prefixMatchA && !prefixMatchB) return -1;
                    if (!prefixMatchA && prefixMatchB) return 1;
                    
                    // 3. 包含匹配，但名称越短越靠前
                    boolean containsA = nameA.contains(finalSearchName);
                    boolean containsB = nameB.contains(finalSearchName);
                    
                    if (containsA && !containsB) return -1;
                    if (!containsA && containsB) return 1;
                    
                    if (containsA && containsB) {
                        // 都包含的情况下，名称越短越靠前
                        int lengthCompare = Integer.compare(nameA.length(), nameB.length());
                        if (lengthCompare != 0) return lengthCompare;
                    }
                    
                    // 4. 最后按ID排序保证稳定性
                    return Long.compare(
                        a.getId() != null ? a.getId() : 0L,
                        b.getId() != null ? b.getId() : 0L
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据分类ID获取分类名称
     */
    private String getCategoryNameById(Long categoryId) {
        try {
            ErpProductCategoryDO category = productCategoryService.getProductCategory(categoryId);
            return category != null ? category.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据单位ID获取单位名称
     */
    private String getUnitNameById(Long unitId) {
        try {
            ErpProductUnitDO unit = productUnitService.getProductUnit(unitId);
            return unit != null ? unit.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // 添加数据库查询方法
    private PageResult<ErpProductRespVO> getProductVOPageFromDB(ErpProductPageReqVO pageReqVO) {
        PageResult<ErpProductDO> pageResult = productMapper.selectPage(pageReqVO);
        return new PageResult<>(buildProductVOList(pageResult.getList()), pageResult.getTotal());
    }



    private List<ErpProductRespVO> buildProductVOList(List<ErpProductDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, ErpProductCategoryDO> categoryMap = productCategoryService.getProductCategoryMap(
                convertSet(list, ErpProductDO::getCategoryId));
        Map<Long, ErpProductUnitDO> unitMap = productUnitService.getProductUnitMap(
                convertSet(list, ErpProductDO::getUnitId));
        return BeanUtils.toBean(list, ErpProductRespVO.class, product -> {
            MapUtils.findAndThen(categoryMap, product.getCategoryId(),
                    category -> product.setCategoryName(category.getName()));
            MapUtils.findAndThen(unitMap, product.getUnitId(),
                    unit -> product.setUnitName(unit.getName()));
        });
    }

    @Override
    public Long getProductCountByCategoryId(Long categoryId) {
        return productMapper.selectCountByCategoryId(categoryId);
    }

    @Override
    public Long getProductCountByUnitId(Long unitId) {
        return productMapper.selectCountByUnitId(unitId);
    }

    @Override
    public List<ErpProductRespVO> searchProducts(ErpProductSearchReqVO searchReqVO) {
        // 构造查询条件
        ErpProductDO productDO = new ErpProductDO();
        if (searchReqVO.getId() != null) {
            productDO.setId(searchReqVO.getId());
        }
        if (searchReqVO.getName() != null) {
            productDO.setName(searchReqVO.getName());
        }
        if (searchReqVO.getCreateTime() != null) {
            productDO.setCreateTime(searchReqVO.getCreateTime());
        }

        // 执行查询
        List<ErpProductDO> productDOList = productMapper.selectList(new LambdaQueryWrapper<ErpProductDO>()
                .eq(productDO.getId() != null, ErpProductDO::getId, productDO.getId())
                .like(productDO.getName() != null, ErpProductDO::getName, productDO.getName())
                .eq(productDO.getCreateTime() != null, ErpProductDO::getCreateTime, productDO.getCreateTime()));

        // 转换为响应对象
        return CollectionUtils.convertList(productDOList, product -> BeanUtils.toBean(product, ErpProductRespVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpProductImportRespVO importProductList(List<ErpProductImportExcelVO> importProducts, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importProducts)) {
            throw exception(PRODUCT_IMPORT_LIST_IS_EMPTY);
        }

        // 1. 初始化返回结果
        ErpProductImportRespVO respVO = ErpProductImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 2. 批量处理列表
        List<ErpProductDO> createList = new ArrayList<>();
        List<ErpProductDO> updateList = new ArrayList<>();

        try {
            // 3. 批量查询已存在的产品
            Set<String> noSet = importProducts.stream()
                    .map(ErpProductImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());

            // 使用数据库查询替代ES查询，确保数据一致性
            Map<String, ErpProductDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(productMapper.selectListByNoIn(noSet), ErpProductDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();

            // 4. 批量转换数据
            for (int i = 0; i < importProducts.size(); i++) {
                ErpProductImportExcelVO importVO = importProducts.get(i);
                try {
                    // 4.1 基础数据校验
                    if (StrUtil.isEmpty(importVO.getName())) {
                        throw exception(PRODUCT_IMPORT_NAME_EMPTY, i + 1);
                    }

                    // 4.2 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(PRODUCT_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 4.3 判断是否支持更新
                    ErpProductDO existProduct = existMap.get(importVO.getNo());
                    if (existProduct == null) {
                        // 创建产品
                        ErpProductDO product = BeanUtils.toBean(importVO, ErpProductDO.class);
                        if (StrUtil.isEmpty(product.getNo())) {
                            product.setNo(noRedisDAO.generate(ErpNoRedisDAO.PRODUCT_NO_PREFIX));
                        }

                        // 校验产品名称唯一性（对于新增的产品）
                        validateProductNameUniqueForImport(product.getName(), null, createList, updateList);

                        createList.add(product);
                        respVO.getCreateNames().add(product.getName());
                    } else if (isUpdateSupport) {
                        // 更新产品
                        ErpProductDO updateProduct = BeanUtils.toBean(importVO, ErpProductDO.class);
                        updateProduct.setId(existProduct.getId());

                        // 校验产品名称唯一性（对于更新的产品）
                        validateProductNameUniqueForImport(updateProduct.getName(), updateProduct.getId(), createList, updateList);

                        updateList.add(updateProduct);
                        respVO.getUpdateNames().add(updateProduct.getName());
                    } else {
                        throw exception(PRODUCT_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
                    }
                } catch (ServiceException ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getName()) ? "(" + importVO.getName() + ")" : "");
                    respVO.getFailureNames().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getName()) ? "(" + importVO.getName() + ")" : "");
                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 5. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                // 批量插入新产品
                for (ErpProductDO product : createList) {
                    productMapper.insert(product);
                    // 同步到ES
                    syncProductToES(product.getId());
                }
            }
            if (CollUtil.isNotEmpty(updateList)) {
                // 批量更新产品
                for (ErpProductDO product : updateList) {
                    productMapper.updateById(product);
                    // 同步到ES
                    syncProductToES(product.getId());
                }
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }


        return respVO;
    }

    /**
     * 校验产品名称是否唯一（导入专用）
     */
    private void validateProductNameUniqueForImport(String name, Long excludeId,
                                                   List<ErpProductDO> createList, List<ErpProductDO> updateList) {
        if (StrUtil.isEmpty(name)) {
            return;
        }

        // 检查当前批次中是否有重复名称
        boolean duplicateInBatch = false;

        // 检查创建列表中的重复
        for (ErpProductDO product : createList) {
            if (name.equals(product.getName())) {
                duplicateInBatch = true;
                break;
            }
        }

        // 检查更新列表中的重复
        if (!duplicateInBatch) {
            for (ErpProductDO product : updateList) {
                if (name.equals(product.getName()) && !product.getId().equals(excludeId)) {
                    duplicateInBatch = true;
                    break;
                }
            }
        }

        if (duplicateInBatch) {
            throw exception(PRODUCT_NAME_DUPLICATE, name);
        }

        // 检查数据库中的重复
        validateProductNameUnique(name, excludeId);
    }

    /**
     * 同步产品到ES
     */
    private void syncProductToES(Long productId) {
        ErpProductDO product = productMapper.selectById(productId);
        if (product == null) {
            productESRepository.deleteById(productId);
        } else {
            ErpProductESDO es = convertProductToES(product);
            productESRepository.save(es);
        }
    }

    /**
     * 转换产品DO为ES对象
     */
    private ErpProductESDO convertProductToES(ErpProductDO product) {
        ErpProductESDO es = new ErpProductESDO();
        
        try {
            // 复制基础属性（排除日期字段）
            BeanUtils.copyProperties(product, es, "productionDate", "createTime");
            
            // 安全处理日期字段 - 转换为字符串
            if (product.getProductionDate() != null) {
                es.setProductionDate(product.getProductionDate().toString());
            }
            if (product.getCreateTime() != null) {
                es.setCreateTime(product.getCreateTime().toString());
            }
            
            // 设置keyword字段（用于精确匹配和通配符查询）
            es.setNameKeyword(product.getName());
            es.setProductShortNameKeyword(product.getProductShortName());
            es.setShippingCodeKeyword(product.getShippingCode());
            es.setBrandKeyword(product.getBrand());
            es.setPurchaserKeyword(product.getPurchaser());
            es.setSupplierKeyword(product.getSupplier());
            
            // 设置分类名称
            if (product.getCategoryId() != null) {
                try {
                    ErpProductCategoryDO category = productCategoryService.getProductCategory(product.getCategoryId());
                    if (category != null) {
                        es.setCategoryName(category.getName());
                    }
                } catch (Exception e) {
                    System.err.println("获取分类名称失败: " + e.getMessage());
                }
            }
            
            // 设置单位名称
            if (product.getUnitId() != null) {
                try {
                    ErpProductUnitDO unit = productUnitService.getProductUnit(product.getUnitId());
                    if (unit != null) {
                        es.setUnitName(unit.getName());
                    }
                } catch (Exception e) {
                    System.err.println("获取单位名称失败: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("转换产品到ES对象失败，产品ID: " + product.getId() + ", 错误: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return es;
    }

    /**
     * 全量同步到ES（手动触发）
     */
    public void fullSyncToES() {
        syncAllDataToES();
    }

    /**
     * 校验产品名称是否唯一（使用ES查询）
     */
    private void validateProductNameUnique(String name, Long excludeId) {
        if (StrUtil.isEmpty(name)) {
            return;
        }

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchPhraseQuery("name", name))
                .build();

        SearchHits<ErpProductESDO> hits = elasticsearchRestTemplate.search(
                query,
                ErpProductESDO.class,
                IndexCoordinates.of("erp_products"));
        System.out.println("查询结果命中数: " + hits.getTotalHits());
        hits.getSearchHits().forEach(hit ->
            System.out.println("命中产品: ID=" + hit.getContent().getId() + ", 名称=" + hit.getContent().getName())
        );

        // 检查是否有同名产品（排除自身）
        hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .filter(product -> !product.getId().equals(excludeId))
                .findFirst()
                .ifPresent(product -> {
                    throw exception(PRODUCT_NAME_DUPLICATE, name);
                });
    }
}
