package cn.iocoder.yudao.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.*;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboProductItemMapper;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaserService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;

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

    @Resource
    private ErpComboProductService comboProductService;

    @Resource
    private ErpComboProductItemMapper comboProductItemMapper;

    @Resource
    private ErpPurchaserService purchaserService;

    @Resource
    private ErpSupplierService supplierService;

    // 用于存储当前搜索条件的ThreadLocal
    private static final ThreadLocal<String> CURRENT_SEARCH_NAME = new ThreadLocal<>();

    /**
     * ES状态枚举
     */
    private enum ESStatus {
        HEALTHY,        // ES健康，数据一致
        UNAVAILABLE,    // ES服务不可用
        EMPTY_INDEX,    // ES索引为空
        DATA_MISMATCH   // ES数据量与数据库不匹配
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initESIndex() {
        System.out.println("开始初始化ES索引...");
    try {
            // 检查ES是否可用
            if (!isESServiceAvailable()) {
                System.out.println("ES服务不可用，跳过索引初始化");
                return;
            }

        IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpProductESDO.class);

            // 检查索引是否存在
        if (!indexOps.exists()) {
                // 创建新索引
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping(ErpProductESDO.class));
            System.out.println("ERP产品索引创建成功");
            } else {
                System.out.println("ERP产品索引已存在，跳过创建");
        }

            // 注意：不在启动时进行全量同步，避免租户上下文问题
            System.out.println("ES索引初始化完成");

    } catch (Exception e) {
        System.err.println("ERP产品索引创建失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 检查ES服务是否可用
     */
    private boolean isESServiceAvailable() {
        try {
            elasticsearchRestTemplate.cluster().health();
            return true;
        } catch (Exception e) {
            System.err.println("ES服务不可用: " + e.getMessage());
            return false;
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

        // 🔥 注释掉：单品更新后，需要同步所有相关的组品ES索引
        // 这个同步会导致大量不必要的ES操作，影响性能
        // syncRelatedCombosToES(updateReqVO.getId());
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteProducts(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        for (Long id : ids) {
            validateProductExists(id);
        }
        // 2. 批量删除
        productMapper.deleteBatchIds(ids);

        // 3. 批量删除ES记录
        try {
            productESRepository.deleteAllById(ids);
        } catch (Exception e) {
            System.err.println("批量删除ES记录失败: " + e.getMessage());
            // ES删除失败不影响数据库删除
        }
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
            // 智能检查ES状态和数据一致性
            ESStatus esStatus = checkESStatus();

            switch (esStatus) {
                case UNAVAILABLE:
                    System.out.println("ES服务不可用，使用数据库查询");
                    return getProductVOPageFromDB(pageReqVO);

                case EMPTY_INDEX:
                    System.out.println("ES索引为空，自动同步数据后查询");
                    syncAllDataToES();
                    return searchProductsFromES(pageReqVO);

                case DATA_MISMATCH:
                    System.out.println("ES数据量不匹配，重新同步数据后查询");
                    syncAllDataToES();
                    return searchProductsFromES(pageReqVO);

                case HEALTHY:
                default:
                    // ES状态正常，直接查询
                    return searchProductsFromES(pageReqVO);
            }

        } catch (Exception e) {
            System.err.println("ES查询失败，回退到数据库查询: " + e.getMessage());
            e.printStackTrace();
            return getProductVOPageFromDB(pageReqVO);
        }
    }

    /**
     * 智能检查ES状态和数据一致性
     */
    private ESStatus checkESStatus() {
        try {
            // 1. 检查ES服务是否可用
            if (!isESServiceAvailable()) {
                return ESStatus.UNAVAILABLE;
            }

            // 2. 检查索引是否存在
            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpProductESDO.class);
            if (!indexOps.exists()) {
                System.out.println("ES索引不存在，需要创建并同步数据");
                // 创建索引
                indexOps.create();
                indexOps.putMapping(indexOps.createMapping(ErpProductESDO.class));
                return ESStatus.EMPTY_INDEX;
            }

            // 3. 检查数据量是否一致
         long dbCount = productMapper.selectCount(null);
            long esCount = 0;

            try {
                esCount = elasticsearchRestTemplate.count(
                    new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).build(),
                    ErpProductESDO.class
                );
            } catch (Exception e) {
                System.err.println("ES计数查询失败: " + e.getMessage());
                return ESStatus.DATA_MISMATCH;
            }

            System.out.println("数据量检查 - 数据库: " + dbCount + ", ES: " + esCount);

            // 4. 判断数据一致性
            if (dbCount == 0) {
                // 数据库为空时，ES也应该为空
                if (esCount == 0) {
                    return ESStatus.HEALTHY;
                } else {
                    System.out.println("数据库为空但ES有数据，需要清空ES");
                    return ESStatus.DATA_MISMATCH;
                }
            } else if (esCount == 0 && dbCount > 0) {
                // ES为空但数据库有数据
                return ESStatus.EMPTY_INDEX;
            } else if (Math.abs(dbCount - esCount) > 5) { // 允许5条差异
                return ESStatus.DATA_MISMATCH;
            } else {
                return ESStatus.HEALTHY;
            }

        } catch (Exception e) {
            System.err.println("ES状态检查失败: " + e.getMessage());
            return ESStatus.UNAVAILABLE;
        }
    }

    /**
     * 检查ES是否可用（简化版本，用于其他地方调用）
     */
    private boolean isESAvailable() {
        ESStatus status = checkESStatus();
        return status == ESStatus.HEALTHY || status == ESStatus.EMPTY_INDEX || status == ESStatus.DATA_MISMATCH;
    }

    /**
     * 确保ES索引存在，仅在必要时同步数据（优化性能版本）
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

            // 索引存在时，只进行轻量级检查（不查询数据库）
            // 如果需要精确同步，可以调用手动同步接口
            System.out.println("ES索引存在，跳过数据同步检查（提高查询性能）");

        } catch (Exception e) {
            System.err.println("ES索引检查失败: " + e.getMessage());
            // 检查失败时不抛出异常，让查询降级到数据库
        }
    }

    /**
     * 全量同步到ES（手动触发）
     */
    @Override
    public void fullSyncToES() {
        syncAllDataToES();
    }

    /**
     * 检查ES索引中的产品编号数据
     */
    public void checkESProductNoData() {
        try {
            System.out.println("=== 检查ES索引中的产品编号数据 ===");

            // 检查ES索引中的产品编号字段
            NativeSearchQuery query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchAllQuery())
                    .withPageable(PageRequest.of(0, 10))
                    .build();

            SearchHits<ErpProductESDO> hits = elasticsearchRestTemplate.search(
                    query,
                    ErpProductESDO.class,
                    IndexCoordinates.of("erp_products"));

            System.out.println("ES中总记录数: " + hits.getTotalHits());
            System.out.println("前10条记录的产品编号数据:");

            hits.getSearchHits().forEach(hit -> {
                ErpProductESDO content = hit.getContent();
                System.out.println("ID=" + content.getId() +
                                 ", no='" + content.getNo() + "'" +
                                 ", name='" + content.getName() + "'");
            });

            // 检查数据库中的产品编号数据
            System.out.println("\n对比数据库中的产品编号数据:");
            List<ErpProductDO> dbProducts = productMapper.selectList(
                new LambdaQueryWrapper<ErpProductDO>().last("LIMIT 10"));

            dbProducts.forEach(product -> {
                System.out.println("DB: ID=" + product.getId() +
                                 ", no='" + product.getNo() + "'" +
                                 ", name='" + product.getName() + "'");
            });

            System.out.println("=== 检查完成 ===");

            // 检查ES索引映射
            System.out.println("\n=== 检查ES索引映射 ===");
            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpProductESDO.class);
            if (indexOps.exists()) {
                try {
                    Map<String, Object> mapping = indexOps.getMapping();
                    System.out.println("ES索引映射: " + mapping.toString());
                } catch (Exception e) {
                    System.err.println("获取ES索引映射失败: " + e.getMessage());
                }
            } else {
                System.out.println("ES索引不存在");
            }

        } catch (Exception e) {
            System.err.println("检查ES产品编号数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 手动检查并同步ES数据（供手动调用）
     */
    @Override
    public void checkAndSyncES() {
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

            System.out.println("检查数据一致性 - 数据库: " + dbCount + ", ES: " + esCount);

            // 判断是否需要同步
            boolean needSync = false;
            if (dbCount == 0) {
                // 数据库为空时，ES也应该为空
                if (esCount > 0) {
                    System.out.println("数据库为空但ES有数据，需要清空ES");
                    needSync = true;
                }
            } else if (Math.abs(dbCount - esCount) > 10) { // 允许10条的差异
                System.out.println("检测到数据量不匹配，需要同步数据");
                needSync = true;
            }

            if (needSync) {
                System.out.println("开始同步数据...");
                syncAllDataToES();
            } else {
                System.out.println("ES索引和数据都正常。数据库:" + dbCount + ", ES:" + esCount);
            }

        } catch (Exception e) {
            System.err.println("ES索引检查失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 同步所有数据到ES（优化版本）
     */
    private void syncAllDataToES() {
        try {
            System.out.println("开始全量同步数据到ES...");

            // 获取数据库数据
            List<ErpProductDO> products = productMapper.selectList(null);

            // 先清空ES数据
            try {
                productESRepository.deleteAll();
                Thread.sleep(1000); // 等待删除完成
                System.out.println("已清空ES数据");
            } catch (Exception e) {
                System.err.println("清空ES数据失败: " + e.getMessage());
            }

            // 如果数据库为空，直接返回
            if (CollUtil.isEmpty(products)) {
                System.out.println("数据库中没有产品数据，ES已清空，同步完成");
                return;
            }

            // 分批处理，提高效率
            int batchSize = 100;
            int totalSize = products.size();
            System.out.println("总共需要同步 " + totalSize + " 条记录，分批处理，每批 " + batchSize + " 条");

            // 分批同步数据
            for (int i = 0; i < totalSize; i += batchSize) {
                int endIndex = Math.min(i + batchSize, totalSize);
                List<ErpProductDO> batch = products.subList(i, endIndex);

                try {
                    // 转换为ES对象
                    List<ErpProductESDO> esBatch = batch.stream()
                        .map(this::convertProductToESSimple)
                        .filter(Objects::nonNull) // 过滤转换失败的数据
                        .collect(Collectors.toList());

                    if (!esBatch.isEmpty()) {
                        productESRepository.saveAll(esBatch);
                        System.out.println("已同步第 " + (i/batchSize + 1) + " 批，共 " + esBatch.size() + " 条记录");
                    }

                    // 短暂休息，避免ES压力过大
                    if (i + batchSize < totalSize) {
                        Thread.sleep(100);
                    }

                } catch (Exception e) {
                    System.err.println("同步第 " + (i/batchSize + 1) + " 批数据失败: " + e.getMessage());
                    // 继续处理下一批
                }
            }

            // 验证同步结果
            Thread.sleep(2000); // 等待ES索引完成
            long esCount = elasticsearchRestTemplate.count(
                new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).build(),
                ErpProductESDO.class
            );

            System.out.println("全量同步完成！数据库: " + totalSize + " 条，ES: " + esCount + " 条");

        } catch (Exception e) {
            System.err.println("全量同步数据失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("同步数据失败: " + e.getMessage());
        }
    }

    /**
     * 从ES搜索产品（支持智能匹配查询和深度分页）
     *
     * 查询策略说明：
     * 使用keyword类型字段和模糊查询的组合：
     * 1. 精确匹配（权重最高）：完全相等
     * 2. 前缀匹配（权重次之）：以搜索词开头
     * 3. 包含匹配（权重再次）：包含搜索词
     * 4. 通配符查询（权重最低）：支持更灵活的模式匹配
     */
    private PageResult<ErpProductRespVO> searchProductsFromES(ErpProductPageReqVO pageReqVO) {
        try {
            // 设置当前搜索条件到ThreadLocal
            if (StrUtil.isNotBlank(pageReqVO.getName())) {
                CURRENT_SEARCH_NAME.set(pageReqVO.getName().trim());
            }

            // 1. 构建ES查询
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 全文搜索（优先级最高）
            if (StrUtil.isNotBlank(pageReqVO.getKeyword())) {
                BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
                String keyword = pageReqVO.getKeyword().trim();

                // 多字段搜索，使用should表示OR关系
                keywordQuery
                        // 精确匹配（权重最高）
                        .should(QueryBuilders.termQuery("name", keyword).boost(10.0f))
                        .should(QueryBuilders.termQuery("no", keyword).boost(10.0f))
                        
                        // 前缀匹配（权重次之）
                        .should(QueryBuilders.prefixQuery("name", keyword).boost(5.0f))
                        .should(QueryBuilders.prefixQuery("no", keyword).boost(5.0f))
                        
                        // 包含匹配（通配符）
                        .should(QueryBuilders.wildcardQuery("name", "*" + keyword + "*").boost(3.0f))
                        .should(QueryBuilders.wildcardQuery("no", "*" + keyword + "*").boost(3.0f))
                        .should(QueryBuilders.wildcardQuery("product_short_name", "*" + keyword + "*").boost(2.5f))
                        .should(QueryBuilders.wildcardQuery("shipping_code", "*" + keyword + "*").boost(2.5f))
                        .should(QueryBuilders.wildcardQuery("brand", "*" + keyword + "*").boost(2.0f))
                        .should(QueryBuilders.wildcardQuery("purchaser", "*" + keyword + "*").boost(1.5f))
                        .should(QueryBuilders.wildcardQuery("supplier", "*" + keyword + "*").boost(1.5f))
                        .should(QueryBuilders.wildcardQuery("creator", "*" + keyword + "*").boost(1.0f))
                        .should(QueryBuilders.wildcardQuery("standard", "*" + keyword + "*").boost(1.0f))
                        .should(QueryBuilders.wildcardQuery("product_selling_points", "*" + keyword + "*").boost(1.0f))
                        .minimumShouldMatch(1);

                boolQuery.must(keywordQuery);
            } else {
                // 产品编号查询 
                if (StrUtil.isNotBlank(pageReqVO.getNo())) {
                    BoolQueryBuilder noQuery = QueryBuilders.boolQuery();
                    String no = pageReqVO.getNo().trim();

                    // 编号匹配策略
                    noQuery.should(QueryBuilders.termQuery("no", no).boost(10.0f))
                           .should(QueryBuilders.prefixQuery("no", no).boost(5.0f))
                           .should(QueryBuilders.wildcardQuery("no", "*" + no + "*").boost(3.0f))
                           .minimumShouldMatch(1);
                    
                    boolQuery.must(noQuery);
                }

                // 产品名称查询
                if (StrUtil.isNotBlank(pageReqVO.getName())) {
                    BoolQueryBuilder nameQuery = QueryBuilders.boolQuery();
                    String name = pageReqVO.getName().trim();

                    nameQuery.should(QueryBuilders.termQuery("name", name).boost(10.0f))
                            .should(QueryBuilders.prefixQuery("name", name).boost(5.0f))
                            .should(QueryBuilders.wildcardQuery("name", "*" + name + "*").boost(3.0f))
                            .minimumShouldMatch(1);
                    
                    boolQuery.must(nameQuery);
                }

                // 产品简称查询
                if (StrUtil.isNotBlank(pageReqVO.getProductShortName())) {
                    BoolQueryBuilder shortNameQuery = QueryBuilders.boolQuery();
                    String shortName = pageReqVO.getProductShortName().trim();

                    shortNameQuery.should(QueryBuilders.termQuery("product_short_name", shortName).boost(10.0f))
                               .should(QueryBuilders.prefixQuery("product_short_name", shortName).boost(5.0f))
                               .should(QueryBuilders.wildcardQuery("product_short_name", "*" + shortName + "*").boost(3.0f))
                               .minimumShouldMatch(1);
                    
                    boolQuery.must(shortNameQuery);
                }

                // 发货编码查询
                if (StrUtil.isNotBlank(pageReqVO.getShippingCode())) {
                    BoolQueryBuilder codeQuery = QueryBuilders.boolQuery();
                    String code = pageReqVO.getShippingCode().trim();

                    codeQuery.should(QueryBuilders.termQuery("shipping_code", code).boost(10.0f))
                           .should(QueryBuilders.prefixQuery("shipping_code", code).boost(5.0f))
                           .should(QueryBuilders.wildcardQuery("shipping_code", "*" + code + "*").boost(3.0f))
                           .minimumShouldMatch(1);
                    
                    boolQuery.must(codeQuery);
                }

                // 品牌名称查询
                if (StrUtil.isNotBlank(pageReqVO.getBrand())) {
                    BoolQueryBuilder brandQuery = QueryBuilders.boolQuery();
                    String brand = pageReqVO.getBrand().trim();

                    // 使用精确匹配
                    brandQuery.must(QueryBuilders.termQuery("brand", brand));
                    boolQuery.must(brandQuery);
                }

                // 采购人员查询
                if (StrUtil.isNotBlank(pageReqVO.getPurchaser())) {
                    BoolQueryBuilder purchaserQuery = QueryBuilders.boolQuery();
                    String purchaser = pageReqVO.getPurchaser().trim();

                    purchaserQuery.should(QueryBuilders.termQuery("purchaser", purchaser).boost(10.0f))
                                .should(QueryBuilders.prefixQuery("purchaser", purchaser).boost(5.0f))
                                .should(QueryBuilders.wildcardQuery("purchaser", "*" + purchaser + "*").boost(3.0f))
                                .minimumShouldMatch(1);
                    
                    boolQuery.must(purchaserQuery);
                }

                // 供应商名查询
                if (StrUtil.isNotBlank(pageReqVO.getSupplier())) {
                    BoolQueryBuilder supplierQuery = QueryBuilders.boolQuery();
                    String supplier = pageReqVO.getSupplier().trim();

                    supplierQuery.should(QueryBuilders.termQuery("supplier", supplier).boost(10.0f))
                               .should(QueryBuilders.prefixQuery("supplier", supplier).boost(5.0f))
                               .should(QueryBuilders.wildcardQuery("supplier", "*" + supplier + "*").boost(3.0f))
                               .minimumShouldMatch(1);
                    
                    boolQuery.must(supplierQuery);
                }

                // 创建人员查询
                if (StrUtil.isNotBlank(pageReqVO.getCreator())) {
                    BoolQueryBuilder creatorQuery = QueryBuilders.boolQuery();
                    String creator = pageReqVO.getCreator().trim();

                    creatorQuery.should(QueryBuilders.termQuery("creator", creator).boost(10.0f))
                             .should(QueryBuilders.prefixQuery("creator", creator).boost(5.0f))
                             .should(QueryBuilders.wildcardQuery("creator", "*" + creator + "*").boost(3.0f))
                             .minimumShouldMatch(1);
                    
                    boolQuery.must(creatorQuery);
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

            // 3. 处理分页参数
            // 检查是否是导出操作（pageSize为-1）
            if (PageParam.PAGE_SIZE_NONE.equals(pageReqVO.getPageSize())) {
                // 导出所有数据，不使用分页，但限制最大返回数量防止内存溢出
                queryBuilder.withPageable(PageRequest.of(0, 10000)); // 最多返回10000条
                System.out.println("检测到导出操作，查询所有数据（最多10000条）");
            } else {
                // 正常分页查询
                int offset = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();
                if (offset >= 10000) {
                    // 使用深度分页
                    return handleDeepPagination(pageReqVO, queryBuilder, boolQuery);
                } else {
                    // 普通分页
                    queryBuilder.withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()));
                }
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
                                 ", 产品编号=" + content.getNo() +
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
            // 检查pageSize是否为导出标志，如果是则使用合理的分页大小
            int pageSize = PageParam.PAGE_SIZE_NONE.equals(pageReqVO.getPageSize()) ? 10000 : pageReqVO.getPageSize();
            queryBuilder.withPageable(PageRequest.of(0, pageSize));

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
            // 检查pageSize是否为导出标志，如果是则使用合理的分页大小
            int pageSize = PageParam.PAGE_SIZE_NONE.equals(pageReqVO.getPageSize()) ? 10000 : pageReqVO.getPageSize();
            int pageNo = PageParam.PAGE_SIZE_NONE.equals(pageReqVO.getPageSize()) ? 0 : (pageReqVO.getPageNo() - 1);
            queryBuilder.withPageable(PageRequest.of(pageNo, pageSize));
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
        // 检查pageSize是否为导出标志，如果是则使用合理的分页大小
        int pageSize = PageParam.PAGE_SIZE_NONE.equals(pageReqVO.getPageSize()) ? 10000 : pageReqVO.getPageSize();
        queryBuilder.withPageable(PageRequest.of(0, pageSize));

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

        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String username = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
        LocalDateTime now = LocalDateTime.now();

        try {
            // 2. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateAllImportData(importProducts, isUpdateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 3. 批量处理列表
            List<ErpProductDO> createList = new ArrayList<>();
            List<ErpProductDO> updateList = new ArrayList<>();

            // 4. 批量查询已存在的产品
            Set<String> noSet = importProducts.stream()
                    .map(ErpProductImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());

            // 使用数据库查询替代ES查询，确保数据一致性
            Map<String, ErpProductDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(productMapper.selectListByNoIn(noSet), ErpProductDO::getNo);

            // 5. 批量转换和保存数据
            for (int i = 0; i < importProducts.size(); i++) {
                ErpProductImportExcelVO importVO = importProducts.get(i);

                // 数据转换
                ErpProductDO product = convertImportVOToDO(importVO);

                // 判断是新增还是更新
                ErpProductDO existProduct = existMap.get(importVO.getNo());
                if (existProduct == null) {
                    // 创建产品
                    product.setNo(noRedisDAO.generate(ErpNoRedisDAO.PRODUCT_NO_PREFIX)).setCreator(username).setCreateTime(now);
                    //product.setNo(importVO.getNo()).setCreator(username).setCreateTime(now);
                    createList.add(product);
                    respVO.getCreateNames().add(product.getName());
                } else if (isUpdateSupport) {
                    // 更新产品
                    product.setId(existProduct.getId()).setCreator(username).setCreateTime(now);
                    updateList.add(product);
                    respVO.getUpdateNames().add(product.getName());
                }
            }

            // 6. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                // 批量插入新产品
                productMapper.insertBatch(createList);

                // 批量同步到ES
                batchSyncProductsToES(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                // 批量更新产品
                productMapper.updateBatch(updateList);

                // 批量同步到ES
                batchSyncProductsToES(updateList);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        } finally {
            // 清除转换错误
            ConversionErrorHolder.clearErrors();
        }

        return respVO;
    }

    /**
     * 统一校验所有导入数据（包括数据类型校验和业务逻辑校验）
     * 如果出现任何错误信息都记录下来并返回，后续操作就不进行了
     */
    private Map<String, String> validateAllImportData(List<ErpProductImportExcelVO> importProducts, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importProducts);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的产品
        Set<String> noSet = importProducts.stream()
                .map(ErpProductImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, ErpProductDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(productMapper.selectListByNoIn(noSet), ErpProductDO::getNo);

        // 3. 批量查询所有采购人员名称，验证采购人员是否存在
        Set<String> purchaserNames = importProducts.stream()
                .map(ErpProductImportExcelVO::getPurchaser)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, Boolean> purchaserExistsMap = new HashMap<>();
        for (String purchaserName : purchaserNames) {
            List<ErpPurchaserRespVO> purchasers = purchaserService.searchPurchasers(
                    new ErpPurchaserPageReqVO().setPurchaserName(purchaserName));
            purchaserExistsMap.put(purchaserName, CollUtil.isNotEmpty(purchasers));
        }

        // 4. 批量查询所有供应商名称，验证供应商是否存在
        Set<String> supplierNames = importProducts.stream()
                .map(ErpProductImportExcelVO::getSupplier)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, Boolean> supplierExistsMap = new HashMap<>();
        for (String supplierName : supplierNames) {
            List<ErpSupplierRespVO> suppliers = supplierService.searchSuppliers(
                    new ErpSupplierPageReqVO().setName(supplierName));
            supplierExistsMap.put(supplierName, CollUtil.isNotEmpty(suppliers));
        }

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();

        // 5. 逐行校验业务逻辑
        for (int i = 0; i < importProducts.size(); i++) {
            ErpProductImportExcelVO importVO = importProducts.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getName()) ? "(" + importVO.getName() + ")" : "");

            try {
                // 5.1 基础数据校验
                if (StrUtil.isEmpty(importVO.getName())) {
                    allErrors.put(errorKey, "产品名称不能为空");
                    continue;
                }

                // 5.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "产品编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 5.3 校验采购人员是否存在
                if (StrUtil.isNotBlank(importVO.getPurchaser())) {
                    Boolean purchaserExists = purchaserExistsMap.get(importVO.getPurchaser());
                    if (purchaserExists == null || !purchaserExists) {
                        allErrors.put(errorKey, "采购人员不存在: " + importVO.getPurchaser());
                        continue;
                    }
                }

                // 5.4 校验供应商是否存在
                if (StrUtil.isNotBlank(importVO.getSupplier())) {
                    Boolean supplierExists = supplierExistsMap.get(importVO.getSupplier());
                    if (supplierExists == null || !supplierExists) {
                        allErrors.put(errorKey, "供应商不存在: " + importVO.getSupplier());
                        continue;
                    }
                }

                // 5.5 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpProductDO product = convertImportVOToDO(importVO);
                    if (product == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 5.6 判断是新增还是更新，并进行相应校验
                ErpProductDO existProduct = existMap.get(importVO.getNo());
                if (existProduct == null) {
                    // 新增校验：校验产品名称唯一性
                    try {
                        validateProductNameUnique(importVO.getName(), null);
                    } catch (ServiceException ex) {
                        allErrors.put(errorKey, ex.getMessage());
                    }
                } else if (isUpdateSupport) {
                    // 更新校验：校验产品名称唯一性（排除自身）
                    try {
                        validateProductNameUnique(importVO.getName(), existProduct.getId());
                    } catch (ServiceException ex) {
                        allErrors.put(errorKey, ex.getMessage());
                    }
                } else {
                    allErrors.put(errorKey, "产品编号不存在且不支持更新: " + importVO.getNo());
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
    private Map<String, String> validateDataTypeErrors(List<ErpProductImportExcelVO> importProducts) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取产品名称 - 修复行号索引问题
                String productName = "未知产品";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importProducts.size()) {
                    ErpProductImportExcelVO importVO = importProducts.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getName())) {
                        productName = importVO.getName();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + productName + ")";
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
     * 转换产品DO为ES对象（优化版本）
     */
    private ErpProductESDO convertProductToES(ErpProductDO product) {
        if (product == null) {
            return null;
        }

        try {
            ErpProductESDO es = new ErpProductESDO();

            // 手动复制所有字段，避免类型转换问题
            es.setId(product.getId());
            es.setNo(product.getNo());
            es.setName(product.getName());
            es.setImage(product.getImage());
            es.setProductShortName(product.getProductShortName());
            es.setShippingCode(product.getShippingCode());
            es.setUnitId(product.getUnitId());
            es.setStandard(product.getStandard());
            es.setWeight(product.getWeight());
            es.setExpiryDay(product.getExpiryDay());
            es.setBrand(product.getBrand());
            es.setCategoryId(product.getCategoryId());
            es.setStatus(product.getStatus());
            es.setProductSellingPoints(product.getProductSellingPoints());
            es.setBarCode(product.getBarCode());
            es.setProductRecord(product.getProductRecord());
            es.setExecutionCode(product.getExecutionCode());
            es.setTrademarkCode(product.getTrademarkCode());
            es.setTotalQuantity(product.getTotalQuantity());
            es.setPackagingMaterialQuantity(product.getPackagingMaterialQuantity());
            es.setOrderReplenishmentLeadTime(product.getOrderReplenishmentLeadTime());
            es.setProductLength(product.getProductLength());
            es.setProductCartonSpec(product.getProductCartonSpec());
            es.setCartonLength(product.getCartonLength());
            es.setShippingAddress(product.getShippingAddress());
            es.setReturnAddress(product.getReturnAddress());
            es.setLogisticsCompany(product.getLogisticsCompany());
            es.setNonshippingArea(product.getNonshippingArea());
            es.setAddonShippingArea(product.getAddonShippingArea());
            es.setAfterSalesStandard(product.getAfterSalesStandard());
            es.setAfterSalesScript(product.getAfterSalesScript());
            es.setPublicDomainEventMinimumPrice(product.getPublicDomainEventMinimumPrice());
            es.setLiveStreamingEventMinimunPrice(product.getLiveStreamingEventMinimunPrice());
            es.setPinduoduoEventMinimumPrice(product.getPinduoduoEventMinimumPrice());
            es.setAlibabaEventMinimunPrice(product.getAlibabaEventMinimunPrice());
            es.setGroupBuyEventMinimunPrice(product.getGroupBuyEventMinimunPrice());
            es.setPurchaser(product.getPurchaser());
            es.setSupplier(product.getSupplier());
            es.setPurchasePrice(product.getPurchasePrice());
            es.setWholesalePrice(product.getWholesalePrice());
            es.setRemark(product.getRemark());
            es.setMinPurchasePrice(product.getMinPurchasePrice());
            es.setMinWholesalePrice(product.getMinWholesalePrice());
            es.setShippingFeeType(product.getShippingFeeType());
            es.setFixedShippingFee(product.getFixedShippingFee());
            es.setAdditionalItemQuantity(product.getAdditionalItemQuantity());
            es.setAdditionalItemPrice(product.getAdditionalItemPrice());
            es.setFirstWeight(product.getFirstWeight());
            es.setFirstWeightPrice(product.getFirstWeightPrice());
            es.setAdditionalWeight(product.getAdditionalWeight());
            es.setAdditionalWeightPrice(product.getAdditionalWeightPrice());
            es.setCreator(product.getCreator());
            es.setCartonWeight(product.getCartonWeight());

            // 安全处理日期字段 - 转换为字符串
            if (product.getProductionDate() != null) {
                es.setProductionDate(product.getProductionDate().toString());
            }
            if (product.getCreateTime() != null) {
                es.setCreateTime(product.getCreateTime().toString());
            }

            // 根据用户要求，简化分类名称和单位名称的设置，直接设置为空字符串
            es.setCategoryName("");
            es.setUnitName("");

            return es;

        } catch (Exception e) {
            System.err.println("转换产品到ES对象失败，产品ID: " + (product.getId() != null ? product.getId() : "null") + ", 错误: " + e.getMessage());
            // 返回null，让调用方过滤掉
            return null;
        }
    }

    /**
     * 转换产品DO为ES对象（带缓存版本，用于批量操作）
     */
    private ErpProductESDO convertProductToESWithCache(ErpProductDO product,
                                                     Map<Long, String> categoryNameMap,
                                                     Map<Long, String> unitNameMap) {
        if (product == null) {
            return null;
        }

        try {
            ErpProductESDO es = new ErpProductESDO();

            // 手动复制所有字段，避免类型转换问题
            es.setId(product.getId());
            es.setNo(product.getNo());
            es.setName(product.getName());
            es.setImage(product.getImage());
            es.setProductShortName(product.getProductShortName());
            es.setShippingCode(product.getShippingCode());
            es.setUnitId(product.getUnitId());
            es.setStandard(product.getStandard());
            es.setWeight(product.getWeight());
            es.setExpiryDay(product.getExpiryDay());
            es.setBrand(product.getBrand());
            es.setCategoryId(product.getCategoryId());
            es.setStatus(product.getStatus());
            es.setProductSellingPoints(product.getProductSellingPoints());
            es.setBarCode(product.getBarCode());
            es.setProductRecord(product.getProductRecord());
            es.setExecutionCode(product.getExecutionCode());
            es.setTrademarkCode(product.getTrademarkCode());
            es.setTotalQuantity(product.getTotalQuantity());
            es.setPackagingMaterialQuantity(product.getPackagingMaterialQuantity());
            es.setOrderReplenishmentLeadTime(product.getOrderReplenishmentLeadTime());
            es.setProductLength(product.getProductLength());
            es.setProductCartonSpec(product.getProductCartonSpec());
            es.setCartonLength(product.getCartonLength());
            es.setShippingAddress(product.getShippingAddress());
            es.setReturnAddress(product.getReturnAddress());
            es.setLogisticsCompany(product.getLogisticsCompany());
            es.setNonshippingArea(product.getNonshippingArea());
            es.setAddonShippingArea(product.getAddonShippingArea());
            es.setAfterSalesStandard(product.getAfterSalesStandard());
            es.setAfterSalesScript(product.getAfterSalesScript());
            es.setPublicDomainEventMinimumPrice(product.getPublicDomainEventMinimumPrice());
            es.setLiveStreamingEventMinimunPrice(product.getLiveStreamingEventMinimunPrice());
            es.setPinduoduoEventMinimumPrice(product.getPinduoduoEventMinimumPrice());
            es.setAlibabaEventMinimunPrice(product.getAlibabaEventMinimunPrice());
            es.setGroupBuyEventMinimunPrice(product.getGroupBuyEventMinimunPrice());
            es.setPurchaser(product.getPurchaser());
            es.setSupplier(product.getSupplier());
            es.setPurchasePrice(product.getPurchasePrice());
            es.setWholesalePrice(product.getWholesalePrice());
            es.setRemark(product.getRemark());
            es.setMinPurchasePrice(product.getMinPurchasePrice());
            es.setMinWholesalePrice(product.getMinWholesalePrice());
            es.setShippingFeeType(product.getShippingFeeType());
            es.setFixedShippingFee(product.getFixedShippingFee());
            es.setAdditionalItemQuantity(product.getAdditionalItemQuantity());
            es.setAdditionalItemPrice(product.getAdditionalItemPrice());
            es.setFirstWeight(product.getFirstWeight());
            es.setFirstWeightPrice(product.getFirstWeightPrice());
            es.setAdditionalWeight(product.getAdditionalWeight());
            es.setAdditionalWeightPrice(product.getAdditionalWeightPrice());
            es.setCreator(product.getCreator());
            es.setCartonWeight(product.getCartonWeight());

            // 安全处理日期字段 - 转换为字符串
            if (product.getProductionDate() != null) {
                es.setProductionDate(product.getProductionDate().toString());
            }
            if (product.getCreateTime() != null) {
                es.setCreateTime(product.getCreateTime().toString());
            }

            // 分类名称和单位名称暂时设为空，避免查询其他服务
            es.setCategoryName("");
            es.setUnitName("");

            return es;

        } catch (Exception e) {
            System.err.println("转换产品到ES对象失败，产品ID: " + (product.getId() != null ? product.getId() : "null") + ", 错误: " + e.getMessage());
            // 返回null，让调用方过滤掉
            return null;
        }
    }

    /**
     * 转换产品DO为ES对象（简化版本，不查询其他服务）
     */
    private ErpProductESDO convertProductToESSimple(ErpProductDO product) {
        if (product == null) {
            return null;
        }

        try {
            ErpProductESDO es = new ErpProductESDO();

            // 复制基础属性（排除日期字段）
            BeanUtils.copyProperties(product, es, "productionDate", "createTime");

            // 安全处理日期字段 - 转换为字符串
            if (product.getProductionDate() != null) {
                es.setProductionDate(product.getProductionDate().toString());
            }
            if (product.getCreateTime() != null) {
                es.setCreateTime(product.getCreateTime().toString());
            }

            // 分类名称和单位名称暂时设为空，避免查询其他服务
            es.setCategoryName("");
            es.setUnitName("");

            return es;

        } catch (Exception e) {
            System.err.println("转换产品到ES对象失败，产品ID: " + (product.getId() != null ? product.getId() : "null") + ", 错误: " + e.getMessage());
            // 返回null，让调用方过滤掉
            return null;
        }
    }

    /**
     * 批量同步产品到ES（优化版本）
     */
    private void batchSyncProductsToES(List<ErpProductDO> products) {
        if (CollUtil.isEmpty(products)) {
            return;
        }

        try {
            // 批量转换产品为ES对象
            List<ErpProductESDO> esList = products.stream()
                    .map(this::convertProductToESSimple)
                    .filter(Objects::nonNull) // 过滤转换失败的数据
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(esList)) {
                // 批量保存到ES
                productESRepository.saveAll(esList);
                System.out.println("批量同步 " + esList.size() + " 条产品到ES成功");
            }
        } catch (Exception e) {
            System.err.println("批量同步产品到ES失败: " + e.getMessage());
            // 降级为单条同步
            for (ErpProductDO product : products) {
                try {
                    ErpProductESDO es = convertProductToESSimple(product);
                    if (es != null) {
                        productESRepository.save(es);
                    }
                } catch (Exception ex) {
                    System.err.println("单条同步产品到ES失败，产品ID: " + product.getId() + ", 错误: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * 重建ES索引（删除重建）
     */
    @Override
    public void rebuildESIndex() {
        try {
            System.out.println("开始重建ES索引...");

            // 检查ES是否可用
            if (!isESServiceAvailable()) {
                throw new RuntimeException("ES服务不可用，无法重建索引");
            }

            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpProductESDO.class);

            // 删除现有索引（如果存在）
            if (indexOps.exists()) {
                System.out.println("删除现有索引...");
                indexOps.delete();
            }

            // 创建新索引
            System.out.println("创建新索引...");
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping(ErpProductESDO.class));
            System.out.println("索引创建成功");

            // 全量同步数据
            System.out.println("开始同步数据...");
            syncAllDataToES();
            System.out.println("ES索引重建完成");

        } catch (Exception e) {
            System.err.println("重建ES索引失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("重建ES索引失败: " + e.getMessage());
        }
    }

        /**
     * 校验产品名称是否唯一
     * 使用name字段进行精确查询，确保完全匹配
     */
    private void validateProductNameUnique(String name, Long excludeId) {
        if (StrUtil.isEmpty(name)) {
            return;
        }

        // 使用name字段进行精确查询，而不是name_keyword字段
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("name", name))
                .build();

        SearchHits<ErpProductESDO> hits = elasticsearchRestTemplate.search(
                query,
                ErpProductESDO.class,
                IndexCoordinates.of("erp_products"));

        System.out.println("产品名称唯一性校验 - 查询名称: " + name + ", 查询结果命中数: " + hits.getTotalHits());
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

    /**
     * 🔥 关键方法：单品更新后，同步所有相关的组品ES索引
     * 确保组品的采购单价、批发单价、重量等实时反映单品变化
     */
    private void syncRelatedCombosToES(Long productId) {
        try {
            // 查询包含该产品的组品
            List<ErpComboProductItemDO> items = comboProductItemMapper.selectList(
                    new LambdaQueryWrapper<ErpComboProductItemDO>()
                            .eq(ErpComboProductItemDO::getItemProductId, productId)
            );

            if (CollUtil.isNotEmpty(items)) {
                Set<Long> comboProductIds = items.stream()
                        .map(ErpComboProductItemDO::getComboProductId)
                        .collect(Collectors.toSet());

                // 通知组品服务同步这些组品到ES
                for (Long comboProductId : comboProductIds) {
                    comboProductService.manualSyncComboToES(comboProductId);
                }
                System.out.println("已同步 " + comboProductIds.size() + " 个相关组品到ES");
            }
        } catch (Exception e) {
            System.err.println("同步相关组品到ES失败: " + e.getMessage());
        }
    }

    /**
     * 检查字符串是否包含太多重复字符（连续相同字符超过2个）
     * 用于避免像"0001"这样的模式匹配到多条记录
     */
    private boolean containsTooManyRepeatedChars(String str) {
        if (str.length() < 3) {
            return false;
        }

        int repeatCount = 1;
        char prevChar = str.charAt(0);

        for (int i = 1; i < str.length(); i++) {
            char currentChar = str.charAt(i);
            if (currentChar == prevChar) {
                repeatCount++;
                if (repeatCount > 2) { // 连续超过2个相同字符就认为是重复过多
                    return true;
                }
            } else {
                repeatCount = 1;
                prevChar = currentChar;
            }
        }
        return false;
    }

    /**
     * 检查字符串是否包含太多重复数字（连续相同数字超过3个）
     * 修改逻辑：只检查连续相同的数字，而不是连续的数字
     */
    private boolean containsTooManyRepeatedDigits(String str) {
        if (str.length() < 4) {
            return false;
        }

        int sameDigitCount = 1;
        char prevChar = str.charAt(0);

        for (int i = 1; i < str.length(); i++) {
            char currentChar = str.charAt(i);
            if (Character.isDigit(currentChar) && Character.isDigit(prevChar) && currentChar == prevChar) {
                sameDigitCount++;
                if (sameDigitCount > 3) { // 连续超过3个相同数字才认为是重复过多
                    return true;
                }
            } else {
                sameDigitCount = 1;
                prevChar = currentChar;
            }
        }
        return false;
    }

    /**
     * 将导入VO转换为DO
     * 特别注意处理字段类型转换，如categoryId和status从String转为Long/Integer
     */
    private ErpProductDO convertImportVOToDO(ErpProductImportExcelVO importVO) {
        if (importVO == null) {
            return null;
        }

        // 添加调试信息
        System.out.println("=== 转换调试信息 ===");
        System.out.println("产品名称: " + importVO.getName());
        System.out.println("保质日期: " + importVO.getExpiryDay() + " (类型: " + (importVO.getExpiryDay() != null ? importVO.getExpiryDay().getClass().getSimpleName() : "null") + ")");
        System.out.println("采购单价: " + importVO.getPurchasePrice() + " (类型: " + (importVO.getPurchasePrice() != null ? importVO.getPurchasePrice().getClass().getSimpleName() : "null") + ")");
        System.out.println("产品日期: " + importVO.getProductionDate() + " (类型: " + (importVO.getProductionDate() != null ? importVO.getProductionDate().getClass().getSimpleName() : "null") + ")");
        System.out.println("箱规重量: " + importVO.getCartonWeight() + " (类型: " + (importVO.getCartonWeight() != null ? importVO.getCartonWeight().getClass().getSimpleName() : "null") + ")");
        System.out.println("==================");

        // 使用BeanUtils进行基础转换
        ErpProductDO product = BeanUtils.toBean(importVO, ErpProductDO.class);

        // 手动设置转换器处理的字段，确保数据正确传递
        product.setExpiryDay(importVO.getExpiryDay());
        product.setPurchasePrice(importVO.getPurchasePrice());
        product.setWholesalePrice(importVO.getWholesalePrice());
        product.setProductionDate(importVO.getProductionDate());
        product.setCartonWeight(importVO.getCartonWeight());
        product.setShippingFeeType(importVO.getShippingFeeType());
        product.setFixedShippingFee(importVO.getFixedShippingFee());
        product.setAdditionalItemQuantity(importVO.getAdditionalItemQuantity());
        product.setAdditionalItemPrice(importVO.getAdditionalItemPrice());
        product.setFirstWeight(importVO.getFirstWeight());
        product.setFirstWeightPrice(importVO.getFirstWeightPrice());
        product.setAdditionalWeight(importVO.getAdditionalWeight());
        product.setAdditionalWeightPrice(importVO.getAdditionalWeightPrice());
        product.setTotalQuantity(importVO.getTotalQuantity());
        product.setPackagingMaterialQuantity(importVO.getPackagingMaterialQuantity());
        product.setWeight(importVO.getWeight());

        // 特殊处理：categoryId从String转为Long
        if (StrUtil.isNotBlank(importVO.getCategoryId())) {
            try {
                product.setCategoryId(Long.valueOf(importVO.getCategoryId()));
            } catch (NumberFormatException e) {
                // 如果转换失败，记录错误但不抛出异常，让字段保持null
                System.err.println("categoryId转换失败: " + importVO.getCategoryId() + ", 错误: " + e.getMessage());
                product.setCategoryId(null);
            }
        } else {
            product.setCategoryId(null);
        }

        // 特殊处理：status从String转为Integer
        if (StrUtil.isNotBlank(importVO.getStatus())) {
            try {
                product.setStatus(Integer.valueOf(importVO.getStatus()));
            } catch (NumberFormatException e) {
                // 如果转换失败，记录错误但不抛出异常，让字段保持null
                System.err.println("status转换失败: " + importVO.getStatus() + ", 错误: " + e.getMessage());
                product.setStatus(null);
            }
        } else {
            product.setStatus(null);
        }

        // 添加转换后的调试信息
        System.out.println("=== 转换后调试信息 ===");
        System.out.println("产品名称: " + product.getName());
        System.out.println("保质日期: " + product.getExpiryDay() + " (类型: " + (product.getExpiryDay() != null ? product.getExpiryDay().getClass().getSimpleName() : "null") + ")");
        System.out.println("采购单价: " + product.getPurchasePrice() + " (类型: " + (product.getPurchasePrice() != null ? product.getPurchasePrice().getClass().getSimpleName() : "null") + ")");
        System.out.println("产品日期: " + product.getProductionDate() + " (类型: " + (product.getProductionDate() != null ? product.getProductionDate().getClass().getSimpleName() : "null") + ")");
        System.out.println("箱规重量: " + product.getCartonWeight() + " (类型: " + (product.getCartonWeight() != null ? product.getCartonWeight().getClass().getSimpleName() : "null") + ")");
        System.out.println("==================");

        return product;
    }
}
