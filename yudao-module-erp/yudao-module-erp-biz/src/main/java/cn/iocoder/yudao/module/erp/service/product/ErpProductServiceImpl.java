package cn.iocoder.yudao.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSearchReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
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

import javax.annotation.Resource;
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

    @EventListener(ApplicationReadyEvent.class)
    public void initESIndex() {
        System.out.println("开始初始化ES索引...");
    try {
        IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpProductESDO.class);
        if (!indexOps.exists()) {
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping(ErpProductESDO.class));
            System.out.println("ERP产品索引创建成功");
        }
    } catch (Exception e) {
        System.err.println("ERP产品索引创建失败: " + e.getMessage());
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

//    @Override
//    public PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO) {
//        PageResult<ErpProductDO> pageResult = productMapper.selectPage(pageReqVO);
//        return new PageResult<>(buildProductVOList(pageResult.getList()), pageResult.getTotal());
//    }

//    @Override
//    public PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO) {
//        // 1. 构建ES查询条件
//        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
//                .withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()))
//                // 设置track_total_hits为true以获取准确的总命中数
//                .withTrackTotalHits(true);
//
//        // 添加查询条件
//        if (StringUtils.isNotBlank(pageReqVO.getName())) {
//            queryBuilder.withQuery(QueryBuilders.matchQuery("name", pageReqVO.getName()));
//        }
//        if (pageReqVO.getCategoryId() != null) {
//            queryBuilder.withQuery(QueryBuilders.termQuery("categoryId", pageReqVO.getCategoryId()));
//        }
//        if (pageReqVO.getCreateTime() != null) {
//            queryBuilder.withQuery(QueryBuilders.rangeQuery("createTime")
//                    .gte(pageReqVO.getCreateTime()[0])
//                    .lte(pageReqVO.getCreateTime()[1]));
//        }
//
//        // 2. 执行ES查询并直接获取完整数据
//        SearchHits<ErpProductESDO> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), ErpProductESDO.class);
//        // 3. 转换为VO并返回
//        List<ErpProductRespVO> voList = searchHits.stream()
//                .map(SearchHit::getContent)
//                .map(esDO -> BeanUtils.toBean(esDO, ErpProductRespVO.class))
//                .collect(Collectors.toList());
//        System.out.println("返回的数量"+searchHits.getTotalHits());
//
//        return new PageResult<>(voList, searchHits.getTotalHits());
//    }
    @Override
    public PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO) {

        try {
            // 1. 检查数据库是否有数据
            long dbCount = productMapper.selectCount(null);
            if (dbCount == 0) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }

            // 2. 检查ES索引是否存在
            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpProductESDO.class);
            if (!indexOps.exists()) {
                initESIndex(); // 如果索引不存在则创建
                fullSyncToES(); // 全量同步数据
                return getProductVOPageFromDB(pageReqVO); // 首次查询使用数据库
            }

            // 3. 检查ES是否有数据
            long esCount = elasticsearchRestTemplate.count(new NativeSearchQueryBuilder().build(), ErpProductESDO.class);
            if (esCount == 0) {
                fullSyncToES(); // 同步数据到ES
                return getProductVOPageFromDB(pageReqVO); // 首次查询使用数据库
            }
        // 1. 构建基础查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withPageable(PageRequest.of(0, pageReqVO.getPageSize()))
                .withTrackTotalHits(true)
                .withSort(Sort.by(Sort.Direction.ASC, "id")); // 必须包含唯一字段排序

//        // 添加查询条件
//        if (StringUtils.isNotBlank(pageReqVO.getName())) {
//            queryBuilder.withQuery(QueryBuilders.matchQuery("name", pageReqVO.getName()));
//        }
//        if (pageReqVO.getCategoryId() != null) {
//            queryBuilder.withQuery(QueryBuilders.termQuery("categoryId", pageReqVO.getCategoryId()));
//        }
//        if (pageReqVO.getCreateTime() != null && pageReqVO.getCreateTime().length == 2) { // 添加数组长度校验
//            queryBuilder.withQuery(QueryBuilders.rangeQuery("createTime")
//                    .gte(pageReqVO.getCreateTime()[0])
//                    .lte(pageReqVO.getCreateTime()[1]));
//        }

        // 替换原有条件构建逻辑
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (StringUtils.isNotBlank(pageReqVO.getName())) {
            boolQuery.must(QueryBuilders.matchQuery("name", pageReqVO.getName()));
        }
        if (pageReqVO.getCategoryId() != null) {
            boolQuery.must(QueryBuilders.termQuery("categoryId", pageReqVO.getCategoryId()));
        }
        if (pageReqVO.getCreateTime() != null && pageReqVO.getCreateTime().length == 2) {
            boolQuery.must(QueryBuilders.rangeQuery("createTime")
                    .gte(pageReqVO.getCreateTime()[0])
                    .lte(pageReqVO.getCreateTime()[1]));
        }

// 设置组合查询
        queryBuilder.withQuery(boolQuery); // 统一设置所有条件

        // 2. 如果是深度分页(超过10000条)，使用search_after
        if (pageReqVO.getPageNo() > 1) {
            return handleDeepPagination(pageReqVO, queryBuilder);
        }

        // 3. 普通分页处理
        SearchHits<ErpProductESDO> searchHits = elasticsearchRestTemplate.search(
                queryBuilder.build(),
                ErpProductESDO.class,
                IndexCoordinates.of("erp_products"));
                // 调试日志：打印ES查询结果
        searchHits.forEach(hit -> {
            System.out.println("ES数据内容: " + hit.getContent());
            System.out.println("productShortName值: " + hit.getContent().getProductShortName());
        });

        List<ErpProductRespVO> voList = searchHits.stream()
                .map(SearchHit::getContent)
                .map(esDO -> {
                    ErpProductRespVO vo = BeanUtils.toBean(esDO, ErpProductRespVO.class);
                    // 调试日志：打印映射前后的值
                    //System.out.println("映射前productShortName: " + esDO.getProductShortName());
                    //System.out.println("映射后productShortName: " + vo.getProductShortName());
                    return vo;
                })
                .collect(Collectors.toList());
        return new PageResult<>(voList, searchHits.getTotalHits());

    } catch (Exception e) {
        System.out.println("ES查询失败，回退到数据库查询: " + e.getMessage());
        return getProductVOPageFromDB(pageReqVO);
    }
    }

    // 添加数据库查询方法
    private PageResult<ErpProductRespVO> getProductVOPageFromDB(ErpProductPageReqVO pageReqVO) {
        PageResult<ErpProductDO> pageResult = productMapper.selectPage(pageReqVO);
        return new PageResult<>(buildProductVOList(pageResult.getList()), pageResult.getTotal());
    }

    private PageResult<ErpProductRespVO> handleDeepPagination(ErpProductPageReqVO pageReqVO,
                                                              NativeSearchQueryBuilder queryBuilder) {
        // 1. 计算需要跳过的记录数
        int skip = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();

        // 2. 使用search_after直接获取目标页
        NativeSearchQuery query = queryBuilder.build();

        // 设置分页参数
        query.setPageable(PageRequest.of(0, pageReqVO.getPageSize()));

        // 如果是深度分页，使用search_after
        if (skip > 0) {
            // 先获取前一页的最后一条记录
            NativeSearchQuery prevQuery = queryBuilder.build();
            prevQuery.setPageable(PageRequest.of(pageReqVO.getPageNo() - 2, 1));

            SearchHits<ErpProductESDO> prevHits = elasticsearchRestTemplate.search(
                    prevQuery,
                    ErpProductESDO.class,
                    IndexCoordinates.of("erp_products"));

            if (prevHits.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), prevHits.getTotalHits());
            }

            // 设置search_after参数
            SearchHit<ErpProductESDO> lastHit = prevHits.getSearchHits().get(0);
            query.setSearchAfter(lastHit.getSortValues());
        }

        // 3. 执行查询
        SearchHits<ErpProductESDO> searchHits = elasticsearchRestTemplate.search(
                query,
                ErpProductESDO.class,
                IndexCoordinates.of("erp_products"));

        List<ErpProductRespVO> voList = searchHits.stream()
                .map(SearchHit::getContent)
                .map(esDO -> BeanUtils.toBean(esDO, ErpProductRespVO.class))
                .collect(Collectors.toList());
        return new PageResult<>(voList, searchHits.getTotalHits());
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




// ========== ES 同步方法 ==========

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
    BeanUtils.copyProperties(product, es);
    return es;
}

/**
 * 全量同步到ES（每天凌晨执行）
 */
//@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
//@Async
//public void fullSyncToES() {
//    // 查询所有有效产品
//    List<ErpProductDO> products = productMapper.selectList(
//            new LambdaQueryWrapper<ErpProductDO>()
//                    .eq(ErpProductDO::getDeleted, false)
//    );
//
//    // 转换为ES对象并批量保存
//    List<ErpProductESDO> esList = products.stream()
//            .map(this::convertProductToES)
//            .collect(Collectors.toList());
//
//    productESRepository.saveAll(esList);
//}

@Async
public void fullSyncToES() {
    try {
        List<ErpProductDO> products = productMapper.selectList(
            new LambdaQueryWrapper<ErpProductDO>()
        );

        if (CollUtil.isEmpty(products)) {
            System.out.println("数据库中没有产品数据，跳过ES同步");
            return;
        }

        List<ErpProductESDO> esList = products.stream()
            .map(this::convertProductToES)
            .collect(Collectors.toList());

        productESRepository.saveAll(esList);
        System.out.println("全量同步ES数据完成，共同步" + esList.size() + "条记录");
    } catch (Exception e) {
        System.err.println("全量同步ES数据失败: " + e.getMessage());
    }
}
}
