package cn.iocoder.yudao.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboProductCreateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboSearchReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.*;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboProductItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.logging.log4j.core.config.Scheduled;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;

import cn.iocoder.yudao.module.erp.service.product.ErpComboProductESRepository;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductItemESRepository;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.COMBO_PRODUCT_NOT_EXISTS;

@Service
@Validated
public class ErpComboProductServiceImpl implements ErpComboProductService {

    @Resource
    private ErpComboMapper erpComboMapper;

    @Resource
    private ErpComboProductItemMapper erpComboProductItemMapper;

    @Resource
    private ErpProductMapper erpProductMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpComboProductESRepository comboProductESRepository;
    @Resource
    private ErpComboProductItemESRepository comboProductItemESRepository;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initESIndex() {
        System.out.println("开始初始化组合产品ES索引...");
        try {
            // 初始化组合产品主表索引
            IndexOperations comboIndexOps = elasticsearchRestTemplate.indexOps(ErpComboProductES.class);
            if (!comboIndexOps.exists()) {
                comboIndexOps.create();
                comboIndexOps.putMapping(comboIndexOps.createMapping(ErpComboProductES.class));
                System.out.println("组合产品主表索引创建成功");
            }

            // 初始化组合产品关联项索引
            IndexOperations itemIndexOps = elasticsearchRestTemplate.indexOps(ErpComboProductItemES.class);
            if (!itemIndexOps.exists()) {
                itemIndexOps.create();
                itemIndexOps.putMapping(itemIndexOps.createMapping(ErpComboProductItemES.class));
                System.out.println("组合产品关联项索引创建成功");
            }
        } catch (Exception e) {
            System.err.println("组合产品索引初始化失败: " + e.getMessage());
        }
    }
    @Override
    public Long createCombo(@Valid ErpComboSaveReqVO createReqVO) {
        long startTime = System.currentTimeMillis();

        // 生成组合产品编号
        String no = noRedisDAO.generate(ErpNoRedisDAO.COMBO_PRODUCT_NO_PREFIX);
        if (erpComboMapper.selectByNo(no) != null) {
            throw exception(COMBO_PRODUCT_NOT_EXISTS);
        }
        System.out.println("生成编号耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        startTime = System.currentTimeMillis();

        // 保存组品信息
        ErpComboProductDO comboProductDO = BeanUtils.toBean(createReqVO, ErpComboProductDO.class)
                .setNo(no);
        erpComboMapper.insert(comboProductDO);
        System.out.println("保存主表耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        startTime = System.currentTimeMillis();

        // 保存关联项
        if (createReqVO.getItems() != null) {
            for (ErpProductRespVO item : createReqVO.getItems()) {
                long itemStartTime = System.currentTimeMillis();

                ErpComboProductItemDO itemDO = new ErpComboProductItemDO();
                itemDO.setComboProductId(comboProductDO.getId());
                itemDO.setItemProductId(item.getId()); // 假设 ErpProductRespVO 中有 id 字段
                itemDO.setItemQuantity(item.getCount()); // 假设数量默认为 1，或者从其他字段获取
                erpComboProductItemMapper.insert(itemDO);

                System.out.println("保存关联项[" + item.getId() + "]耗时: " +
                    (System.currentTimeMillis() - itemStartTime) + "ms");

                // 同步项到 ES
                syncItemToES(itemDO.getId());
            }
        }
        System.out.println("保存所有关联项总耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        startTime = System.currentTimeMillis();

        // 同步主表到 ES
        syncComboToES(comboProductDO.getId());
        System.out.println("同步主表到ES耗时: " + (System.currentTimeMillis() - startTime) + "ms");

        return comboProductDO.getId();
    }
    @Override
    public void updateCombo(@Valid ErpComboSaveReqVO updateReqVO) {
        validateComboExists(updateReqVO.getId());
        ErpComboProductDO updateObj = BeanUtils.toBean(updateReqVO, ErpComboProductDO.class);
        erpComboMapper.updateById(updateObj);

        // 同步主表到 ES
        syncComboToES(updateReqVO.getId());

        // 如果有单品关联信息，先删除旧的关联，再保存新的关联
        if (updateReqVO.getItems() != null) {
            // 删除旧的关联
            //erpComboProductItemMapper.deleteByComboProductId(updateReqVO.getId());
            List<ErpComboProductItemDO> oldItems = erpComboProductItemMapper.selectByComboProductId(updateReqVO.getId());
            for (ErpComboProductItemDO oldItem : oldItems) {
                erpComboProductItemMapper.deleteById(oldItem.getId());
                comboProductItemESRepository.deleteById(oldItem.getId());
            }

            // 插入新的关联
            for (ErpProductRespVO item : updateReqVO.getItems()) {
                ErpComboProductItemDO itemDO = new ErpComboProductItemDO();
                itemDO.setComboProductId(updateReqVO.getId());
                itemDO.setItemProductId(item.getId()); // 假设 ErpProductRespVO 中有 id 字段
                itemDO.setItemQuantity(item.getCount()); // 假设数量默认为 1，或者从其他字段获取
                erpComboProductItemMapper.insert(itemDO);
                // 同步项到 ES
                syncItemToES(itemDO.getId());
            }
        }
    }

    @Override
    public void deleteCombo(Long id) {
        validateComboExists(id);
        // 删除主表（数据库和ES）
        erpComboMapper.deleteById(id);
        comboProductESRepository.deleteById(id);

        // 删除关联的单品信息
        //erpComboProductItemMapper.deleteByComboProductId(id);
        // 删除关联项（数据库和ES）
        List<ErpComboProductItemDO> items = erpComboProductItemMapper.selectByComboProductId(id);
        for (ErpComboProductItemDO item : items) {
            erpComboProductItemMapper.deleteById(item.getId());
            comboProductItemESRepository.deleteById(item.getId());
        }
    }

    @Override
    public List<ErpComboProductDO> validComboList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpComboMapper.selectBatchIds(ids);
    }

    private void validateComboExists(Long id) {
        if (erpComboMapper.selectById(id) == null) {
            throw exception(COMBO_PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    public ErpComboProductDO getCombo(Long id) {
        return erpComboMapper.selectById(id);
    }

    @Override
    public List<ErpComboRespVO> getComboVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpComboProductDO> list = erpComboMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpComboRespVO.class);
    }

//    @Override
//    public PageResult<ErpComboRespVO> getComboVOPage(ErpComboPageReqVO pageReqVO) {
//        PageResult<ErpComboProductDO> pageResult = erpComboMapper.selectPage(pageReqVO);
//        return new PageResult<>(BeanUtils.toBean(pageResult.getList(), ErpComboRespVO.class), pageResult.getTotal());
//    }
//    @Override
//    public PageResult<ErpComboRespVO> getComboVOPage(ErpComboPageReqVO pageReqVO) {
//        PageResult<ErpComboProductDO> pageResult = erpComboMapper.selectPage(pageReqVO);
//
//        // 转换结果并设置组合产品名称和重量
//        List<ErpComboRespVO> voList = pageResult.getList().stream().map(combo -> {
//            // 查询组合产品关联的单品
//            List<ErpComboProductItemDO> comboItems = erpComboProductItemMapper.selectByComboProductId(combo.getId());
//            List<Long> productIds = comboItems.stream()
//                    .map(ErpComboProductItemDO::getItemProductId)
//                    .collect(Collectors.toList());
//            List<ErpProductDO> products = erpProductMapper.selectBatchIds(productIds);
//
//            // 构建名称字符串
//            StringBuilder nameBuilder = new StringBuilder();
//            // 计算总重量
//            BigDecimal totalWeight = BigDecimal.ZERO;
//            for (int i = 0; i < products.size(); i++) {
//                if (i > 0) {
//                    nameBuilder.append("+");
//                }
//                nameBuilder.append(products.get(i).getName())
//                        .append("*")
//                        .append(comboItems.get(i).getItemQuantity());
//
//                // 计算重量
//                BigDecimal itemWeight = products.get(i).getWeight();
//                if (itemWeight != null) {
//                    BigDecimal quantity = new BigDecimal(comboItems.get(i).getItemQuantity());
//                    totalWeight = totalWeight.add(itemWeight.multiply(quantity));
//                }
//            }
//
//            // 转换VO并设置名称和重量
//            ErpComboRespVO vo = BeanUtils.toBean(combo, ErpComboRespVO.class);
//            vo.setName(nameBuilder.toString());
//            vo.setWeight(totalWeight);
//            return vo;
//        }).collect(Collectors.toList());
//
//        return new PageResult<>(voList, pageResult.getTotal());
//    }
    @Override
    //@Cacheable(value = "comboPageCache", key = "#pageReqVO.hashCode()")
    public PageResult<ErpComboRespVO> getComboVOPage(ErpComboPageReqVO pageReqVO) {
        //System.out.println("1");

        try {
            // 1. 检查数据库是否有数据
            long dbCount = erpComboMapper.selectCount(null);
            if (dbCount == 0) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }

            // 2. 检查ES索引是否存在
            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpComboProductES.class);
            if (!indexOps.exists()) {
                initESIndex(); // 如果索引不存在则创建
                fullSyncToES(); // 全量同步数据
                return getComboVOPageFromDB(pageReqVO); // 首次查询使用数据库
            }

            // 3. 检查ES是否有数据
            long esCount = elasticsearchRestTemplate.count(new NativeSearchQueryBuilder().build(), ErpComboProductES.class);
            if (esCount == 0) {
                fullSyncToES(); // 同步数据到ES
                return getComboVOPageFromDB(pageReqVO); // 首次查询使用数据库
            }
        // 1. 构建基础查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withPageable(PageRequest.of(0, pageReqVO.getPageSize()))
                .withTrackTotalHits(true)
                .withSort(Sort.by(Sort.Direction.ASC, "id")); // 必须包含唯一字段排序

        // 添加查询条件
        if (StringUtils.isNotBlank(pageReqVO.getName())) {
            System.out.println("搜索名称: " + pageReqVO.getName());
            //queryBuilder.withQuery(QueryBuilders.matchQuery("name", pageReqVO.getName()));
            //精确查询
//            queryBuilder.withQuery(QueryBuilders.multiMatchQuery(pageReqVO.getName())// 搜索text类型字段，使用分词器
//                    .field("name.keyword")  // 搜索keyword类型字段，精确匹配
//                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS));  // 使用最佳匹配策略
            //
        queryBuilder.withQuery(QueryBuilders.wildcardQuery("name.keyword", "*" + pageReqVO.getName() + "*"));


        }
//        if (pageReqVO.getStatus() != null) {
//            queryBuilder.withQuery(QueryBuilders.termQuery("status", pageReqVO.getStatus()));
//        }
        if (pageReqVO.getCreateTime() != null) {
            queryBuilder.withQuery(QueryBuilders.rangeQuery("createTime")
                    .gte(pageReqVO.getCreateTime()[0])
                    .lte(pageReqVO.getCreateTime()[1]));
        }

        // 2. 如果是深度分页(超过10000条)，使用search_after
        if (pageReqVO.getPageNo() > 1) {
            return handleDeepPagination(pageReqVO, queryBuilder);
        }

        // 3. 普通分页处理
        SearchHits<ErpComboProductES> searchHits = elasticsearchRestTemplate.search(
                queryBuilder.build(),
                ErpComboProductES.class,
                IndexCoordinates.of("erp_combo_products"));
                // 打印搜索结果
        System.out.println("===== 搜索结果 =====");
        System.out.println("总命中数: " + searchHits.getTotalHits());
        searchHits.forEach(hit -> {
            ErpComboProductES combo = hit.getContent();
            System.out.println("ID: " + combo.getId() +
                             ", 名称: " + combo.getName() +
                             ", 组合名称: " + combo.getComboName());
        });

        // 获取所有组合产品ID
        List<Long> comboIds = searchHits.stream()
                .map(hit -> hit.getContent().getId())
                .collect(Collectors.toList());

        // 从ES查询所有关联项
        NativeSearchQuery itemQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termsQuery("combo_product_id", comboIds))
                .withPageable(PageRequest.of(0, pageReqVO.getPageSize())) // 添加分页参数
                .withTrackTotalHits(true) // 确保获取全部命中数
                .build();
       // System.out.println("查询关联项条件: " + itemQuery.getQuery()); // 打印查询条件
        SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                itemQuery,
                ErpComboProductItemES.class,
                IndexCoordinates.of("erp_combo_product_items"));
       // System.out.println("关联项查询结果数量: " + itemHits.getTotalHits()); // 打印查询结果数量

        // 按组合产品ID分组关联项
        Map<Long, List<ErpComboProductItemES>> itemsMap = itemHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.groupingBy(ErpComboProductItemES::getComboProductId));
//                System.out.println("===== itemsMap详情 =====");
//        itemsMap.forEach((comboId, items) -> {
//            System.out.println("组合产品ID: " + comboId + ", 关联项数量: " + items.size());
//            items.forEach(item -> System.out.println("  关联项ID: " + item.getId()
//                + ", 产品ID: " + item.getItemProductId()
//                + ", 数量: " + item.getItemQuantity()));
//        });


        // 获取所有产品ID
        List<Long> productIds = itemHits.stream()
                .map(hit -> hit.getContent().getItemProductId())
                .distinct()
                .collect(Collectors.toList());

        // 从ES查询所有产品
        NativeSearchQuery productQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.idsQuery().addIds(productIds.stream().map(String::valueOf).toArray(String[]::new)))
                .withPageable(PageRequest.of(0, pageReqVO.getPageSize())) // 添加分页参数
                .withTrackTotalHits(true) // 确保获取全部命中数
                .build();
        SearchHits<ErpProductESDO> productHits = elasticsearchRestTemplate.search(
                productQuery,
                ErpProductESDO.class,
                IndexCoordinates.of("erp_products"));
        Map<Long, ErpProductESDO> productMap = productHits.stream()
                .collect(Collectors.toMap(
                        hit -> hit.getContent().getId(),
                        SearchHit::getContent));

//        System.out.println("===== productMap详情 =====");
//        productMap.forEach((productId, product) -> {
//            System.out.println("产品ID: " + productId
//                + ", 名称: " + product.getName()
//                + ", 重量: " + product.getWeight());
//        });

        // 转换结果并设置组合产品名称和重量
        List<ErpComboRespVO> voList = searchHits.stream()
                .map(SearchHit::getContent)
                .map(combo -> {
                    System.out.println("处理组合产品ID: " + combo.getId()); // 调试打印
                    List<ErpComboProductItemES> items = itemsMap.getOrDefault(combo.getId(), Collections.emptyList());
                   // System.out.println("关联项数量: " + items.size()); // 调试打印

                    // 构建名称字符串
                    StringBuilder nameBuilder = new StringBuilder();
                    // 计算总重量
                    BigDecimal totalWeight = BigDecimal.ZERO;
                    for (int i = 0; i < items.size(); i++) {
                        System.out.println("处理第" + (i+1) + "个关联项，产品ID: " + items.get(i).getItemProductId()); // 调试打印
                        ErpProductESDO product = productMap.get(items.get(i).getItemProductId());
                        if (product == null) {
                            System.out.println("未找到产品ID: " + items.get(i).getItemProductId()); // 调试打印
                            continue;
                        }

                        //System.out.println("找到产品: " + product.getName() + ", 重量: " + product.getWeight()); // 调试打印

                        if (i > 0) {
                            nameBuilder.append("+");
                        }
                        nameBuilder.append(product.getName())
                                .append("*")
                                .append(items.get(i).getItemQuantity());

                        // 计算重量
                        if (product.getWeight() != null) {
                            BigDecimal quantity = new BigDecimal(items.get(i).getItemQuantity());
                            totalWeight = totalWeight.add(product.getWeight().multiply(quantity));
                        }
                    }

                    // 转换VO并设置名称和重量
                    ErpComboRespVO vo = BeanUtils.toBean(combo, ErpComboRespVO.class);
                    vo.setName(nameBuilder.toString());
                    vo.setWeight(totalWeight);
                    return vo;
                })
                .collect(Collectors.toList());

                        // 打印完整的VO列表用于调试
        System.out.println("===== 最终VO列表 =====");
        voList.forEach(vo -> {
            System.out.println("组合产品ID: " + vo.getId());
            System.out.println("组合名称: " + vo.getName());
            System.out.println("总重量: " + vo.getWeight());
            System.out.println("包含单品: ");
            if (vo.getItems() != null) {
                vo.getItems().forEach(item ->
                    System.out.println("  单品ID: " + item.getId() +
                                     ", 名称: " + item.getName() +
                                     ", 数量: " + item.getCount() +
                                     ", 重量: " + item.getWeight()));
            }
            System.out.println("-------------------");
        });

        return new PageResult<>(voList, searchHits.getTotalHits());

               // ... 原有代码 ...
    } catch (Exception e) {
        System.err.println("ES查询失败，回退到数据库查询: " + e.getMessage());
        return getComboVOPageFromDB(pageReqVO);
    }
    }

    // 添加数据库查询方法
    private PageResult<ErpComboRespVO> getComboVOPageFromDB(ErpComboPageReqVO pageReqVO) {
        PageResult<ErpComboProductDO> pageResult = erpComboMapper.selectPage(pageReqVO);
        return new PageResult<>(BeanUtils.toBean(pageResult.getList(), ErpComboRespVO.class), pageResult.getTotal());
    }

    private PageResult<ErpComboRespVO> handleDeepPagination(ErpComboPageReqVO pageReqVO,
                                                           NativeSearchQueryBuilder queryBuilder) {
        //System.out.println("2");
        // 1. 计算需要跳过的记录数
        int skip = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();

        // 2. 使用search_after直接获取目标页
        NativeSearchQuery query = queryBuilder.build();
        query.setPageable(PageRequest.of(0, pageReqVO.getPageSize()));

        // 如果是深度分页，使用search_after
        if (skip > 0) {
            // 先获取前一页的最后一条记录
            NativeSearchQuery prevQuery = queryBuilder.build();
            prevQuery.setPageable(PageRequest.of(pageReqVO.getPageNo() - 2, 1));

            SearchHits<ErpComboProductES> prevHits = elasticsearchRestTemplate.search(
                    prevQuery,
                    ErpComboProductES.class,
                    IndexCoordinates.of("erp_combo_products"));

            if (prevHits.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), prevHits.getTotalHits());
            }

            // 设置search_after参数
            SearchHit<ErpComboProductES> lastHit = prevHits.getSearchHits().get(0);
            query.setSearchAfter(lastHit.getSortValues());
        }

        // 3. 执行查询
        SearchHits<ErpComboProductES> searchHits = elasticsearchRestTemplate.search(
                query,
                ErpComboProductES.class,
                IndexCoordinates.of("erp_combo_products"));

       // 获取所有组合产品ID
        List<Long> comboIds = searchHits.stream()
                .map(hit -> hit.getContent().getId())
                .collect(Collectors.toList());

        // 从ES查询所有关联项
        NativeSearchQuery itemQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termsQuery("combo_product_id", comboIds))
                .withPageable(PageRequest.of(0, pageReqVO.getPageSize())) // 添加分页参数
                .withTrackTotalHits(true) // 确保获取全部命中数
                .build();

        //System.out.println("查询关联项条件2: " + itemQuery.getQuery()); // 打印查询条件
        SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                itemQuery,
                ErpComboProductItemES.class,
                IndexCoordinates.of("erp_combo_product_items"));
        //System.out.println("关联项查询结果数量2: " + itemHits.getTotalHits());
        // 按组合产品ID分组关联项
        Map<Long, List<ErpComboProductItemES>> itemsMap = itemHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.groupingBy(ErpComboProductItemES::getComboProductId));
        //System.out.println("itemsMap大小2: " + itemsMap.size());
        // 获取所有产品ID
        List<Long> productIds = itemHits.stream()
                .map(hit -> hit.getContent().getItemProductId())
                .distinct()
                .collect(Collectors.toList());

        // 从ES查询所有产品
        NativeSearchQuery productQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.idsQuery().addIds(productIds.stream().map(String::valueOf).toArray(String[]::new)))
                .withPageable(PageRequest.of(0, pageReqVO.getPageSize())) // 添加分页参数
                .withTrackTotalHits(true) // 确保获取全部命中数
                .build();
        SearchHits<ErpProductESDO> productHits = elasticsearchRestTemplate.search(
                productQuery,
                ErpProductESDO.class,
                IndexCoordinates.of("erp_products"));
        Map<Long, ErpProductESDO> productMap = productHits.stream()
                .collect(Collectors.toMap(
                        hit -> hit.getContent().getId(),
                        SearchHit::getContent));

        // 转换结果并设置组合产品名称和重量
        List<ErpComboRespVO> voList = searchHits.stream()
                .map(SearchHit::getContent)
                .map(combo -> {
                    //System.out.println("深度翻页处理组合产品ID: " + combo.getId()); // 调试打印
                    List<ErpComboProductItemES> items = itemsMap.getOrDefault(combo.getId(), Collections.emptyList());
                    //System.out.println("关联项数量: " + items.size()); // 调试打印
                    // 构建名称字符串
                    StringBuilder nameBuilder = new StringBuilder();
                    // 计算总重量
                    BigDecimal totalWeight = BigDecimal.ZERO;
                    for (int i = 0; i < items.size(); i++) {
                        ErpProductESDO product = productMap.get(items.get(i).getItemProductId());
                        if (product == null) continue;

                        if (i > 0) {
                            nameBuilder.append("+");
                        }
                        nameBuilder.append(product.getName())
                                .append("*")
                                .append(items.get(i).getItemQuantity());

                        // 计算重量
                        if (product.getWeight() != null) {
                            BigDecimal quantity = new BigDecimal(items.get(i).getItemQuantity());
                            totalWeight = totalWeight.add(product.getWeight().multiply(quantity));
                        }
                    }

                    // 转换VO并设置名称和重量
                    ErpComboRespVO vo = BeanUtils.toBean(combo, ErpComboRespVO.class);
                    vo.setName(nameBuilder.toString());
                    vo.setWeight(totalWeight);
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(voList, searchHits.getTotalHits());
    }


    @Override
    public List<ErpComboRespVO> getComboProductVOListByStatus(Integer status) {
        List<ErpComboProductDO> comboProductList = erpComboMapper.selectListByStatus(status);
        return BeanUtils.toBean(comboProductList, ErpComboRespVO.class);
    }

    @Override
    public void createComboWithItems(ErpComboProductCreateReqVO createReqVO) {
        // 保存组品信息
        ErpComboProductDO comboProductDO = new ErpComboProductDO();
        comboProductDO.setName(createReqVO.getName());
        erpComboMapper.insert(comboProductDO);

        // 保存组品和单品的关联关系
        for (ErpComboProductCreateReqVO.ComboItem item : createReqVO.getItems()) {
            ErpComboProductItemDO itemDO = new ErpComboProductItemDO();
            itemDO.setComboProductId(comboProductDO.getId());
            itemDO.setItemProductId(item.getProductId());
            itemDO.setItemQuantity(item.getQuantity());
            erpComboProductItemMapper.insert(itemDO);
        }
    }

    @Override
    public ErpComboRespVO getComboWithItems(Long id) {
        // 查询组品基本信息
        ErpComboProductDO comboProduct = erpComboMapper.selectById(id);
        if (comboProduct == null) {
            return null;
        }

        // 查询组品关联的单品项
        List<ErpComboProductItemDO> comboItems = erpComboProductItemMapper.selectByComboProductId(id);

        // 提取单品ID列表
        List<Long> productIds = comboItems.stream()
                .map(ErpComboProductItemDO::getItemProductId)
                .collect(Collectors.toList());

        // 查询单品详细信息
        List<ErpProductDO> products = erpProductMapper.selectBatchIds(productIds);

        // 组装单品名称字符串 (单品A*数量+单品B*数量)
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) {
                nameBuilder.append("+");
            }
            nameBuilder.append(products.get(i).getName())
                      .append("*")
                      .append(comboItems.get(i).getItemQuantity());
        }
        // 计算总重量 (单品weight*数量)
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (int i = 0; i < products.size(); i++) {
            BigDecimal itemWeight = products.get(i).getWeight();
            if (itemWeight != null) {
                BigDecimal quantity = new BigDecimal(comboItems.get(i).getItemQuantity());
                totalWeight = totalWeight.add(itemWeight.multiply(quantity));
            }
        }
        // 组装响应对象
        ErpComboRespVO comboRespVO = BeanUtils.toBean(comboProduct, ErpComboRespVO.class);
        comboRespVO.setName(nameBuilder.toString());
        comboRespVO.setWeight(totalWeight);

        // 组装单品列表
        List<ErpProductRespVO> productVOs = products.stream()
                .map(product -> BeanUtils.toBean(product, ErpProductRespVO.class))
                .collect(Collectors.toList());

        // 将 itemQuantity 赋值给 count
        for (int i = 0; i < productVOs.size(); i++) {
            productVOs.get(i).setCount(comboItems.get(i).getItemQuantity());
        }
        comboRespVO.setItems(productVOs);
        return comboRespVO;
    }




    @Override
    public List<ErpComboRespVO> searchCombos(ErpComboSearchReqVO searchReqVO) {
        // 构造查询条件
        ErpComboProductDO comboProductDO = new ErpComboProductDO();
        if (searchReqVO.getId() != null) {
            comboProductDO.setId(searchReqVO.getId());
        }
        if (searchReqVO.getName() != null) {
            comboProductDO.setName(searchReqVO.getName());
        }
        if (searchReqVO.getCreateTime() != null) {
            comboProductDO.setCreateTime(searchReqVO.getCreateTime());
        }

        // 执行查询
        List<ErpComboProductDO> comboProductDOList = erpComboMapper.selectList(new LambdaQueryWrapper<ErpComboProductDO>()
                .eq(comboProductDO.getId() != null, ErpComboProductDO::getId, comboProductDO.getId())
                .like(comboProductDO.getName() != null, ErpComboProductDO::getName, comboProductDO.getName())
                .eq(comboProductDO.getCreateTime() != null, ErpComboProductDO::getCreateTime, comboProductDO.getCreateTime()));

        // 转换为响应对象
        return BeanUtils.toBean(comboProductDOList, ErpComboRespVO.class);
    }


    // ========== ES 同步方法 ==========

    /**
     * 同步组合产品到 ES
     */
    private void syncComboToES(Long comboId)
    {
       // long startTime = System.currentTimeMillis();
        ErpComboProductDO combo = erpComboMapper.selectById(comboId);
        //System.out.println("查询组合产品耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        if (combo == null ) {
            comboProductESRepository.deleteById(comboId);
           // System.out.println("删除ES组合产品耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        } else {
            ErpComboProductES es = convertComboToES(combo);
           // System.out.println("转换ES对象耗时: " + (System.currentTimeMillis() - startTime) + "ms");
            comboProductESRepository.save(es);
            System.out.println("保存ES组合产品ID: " + es.getId());

//            // 检查索引是否存在
//            boolean indexExists = elasticsearchRestTemplate.indexOps(IndexCoordinates.of("erp_combo_products")).exists();
//            System.out.println("索引erp_combo_products是否存在: " + indexExists);
//
//            // 直接查询刚保存的数据
//            ErpComboProductES savedES = comboProductESRepository.findById(es.getId()).orElse(null);
//            System.out.println("查询ES结果: " + (savedES != null ? "成功" : "失败"));
//System.out.println("保存ES组合产品耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    /**
     * 转换组合产品DO为ES对象
     */
    private ErpComboProductES convertComboToES(ErpComboProductDO combo) {
        ErpComboProductES es = new ErpComboProductES();
        BeanUtils.copyProperties(combo, es);

        // 设置组合名称（用于搜索）
        ErpComboRespVO comboWithItems = this.getComboWithItems(combo.getId());
        if (comboWithItems != null) {
            es.setComboName(comboWithItems.getName());
        }

        return es;
    }
//
    /**
     * 同步组合产品项到 ES
     */
    private void syncItemToES(Long itemId) {
        long startTime = System.currentTimeMillis();
        ErpComboProductItemDO item = erpComboProductItemMapper.selectById(itemId);
        System.out.println("查询关联项耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        if (item == null ) {
            comboProductItemESRepository.deleteById(itemId);
            System.out.println("删除ES关联项耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        } else {
            ErpComboProductItemES es = new ErpComboProductItemES();
            BeanUtils.copyProperties(item, es);
            System.out.println("转换ES关联项耗时: " + (System.currentTimeMillis() - startTime) + "ms");
           // startTime = System.currentTimeMillis();
            comboProductItemESRepository.save(es);
            System.out.println("保存ES关联项ID: " + es.getId());

            // 添加检测逻辑
//            boolean indexExists = elasticsearchRestTemplate.indexOps(IndexCoordinates.of("erp_combo_product_items")).exists();
//            System.out.println("索引erp_combo_product_items是否存在: " + indexExists);
//
//            ErpComboProductItemES savedItem = comboProductItemESRepository.findById(es.getId()).orElse(null);
//            System.out.println("查询ES关联项结果: " + (savedItem != null ? "成功" : "失败"));
//            System.out.println("保存ES关联项耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }



//    /**
//     * 全量同步到ES（每天凌晨执行）
//     */
//    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
//    @Async
//    public void fullSyncToES() {
//        // 1. 同步主表
//        List<ErpComboProductDO> combos = erpComboMapper.selectList(
//                new LambdaQueryWrapper<ErpComboProductDO>()
//                        .eq(ErpComboProductDO::getDeleted, false)
//        );
//
//        List<ErpComboProductES> comboESList = combos.stream()
//                .map(this::convertComboToES)
//                .collect(Collectors.toList());
//
//        comboProductESRepository.saveAll(comboESList);
//
//        // 2. 同步项表
//        List<ErpComboProductItemDO> items = erpComboProductItemMapper.selectList(
//                new LambdaQueryWrapper<ErpComboProductItemDO>()
//                        .eq(ErpComboProductItemDO::getDeleted, false)
//        );
//
//        List<ErpComboProductItemES> itemESList = items.stream()
//                .map(item -> {
//                    ErpComboProductItemES es = new ErpComboProductItemES();
//                    BeanUtils.copyProperties(item, es);
//                    return es;
//                })
//                .collect(Collectors.toList());
//
//        comboProductItemESRepository.saveAll(itemESList);
//    }

// 添加全量同步方法
        @Async
        public void fullSyncToES() {
            try {
                // 同步主表数据
                List<ErpComboProductDO> combos = erpComboMapper.selectList(null);
                if (CollUtil.isEmpty(combos)) {
                    System.out.println("数据库中没有组合产品数据，跳过ES同步");
                    return;
                }

                List<ErpComboProductES> comboESList = combos.stream()
                    .map(this::convertComboToES)
                    .collect(Collectors.toList());
                comboProductESRepository.saveAll(comboESList);

                // 同步关联项数据
                List<ErpComboProductItemDO> items = erpComboProductItemMapper.selectList(null);
                if (CollUtil.isEmpty(items)) {
                    System.out.println("没有组合产品关联项数据，跳过同步");
                    return;
                }

                List<ErpComboProductItemES> itemESList = items.stream()
                    .map(item -> {
                        ErpComboProductItemES es = new ErpComboProductItemES();
                        BeanUtils.copyProperties(item, es);
                        return es;
                    })
                    .collect(Collectors.toList());
                comboProductItemESRepository.saveAll(itemESList);

                System.out.println("全量同步ES数据完成，共同步" + comboESList.size() + "条组合产品和" + itemESList.size() + "条关联项");
            } catch (Exception e) {
                System.err.println("全量同步ES数据失败: " + e.getMessage());
            }
        }
}
