package cn.iocoder.yudao.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.ErpComboImport.ErpComboImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.ErpComboImport.ErpComboImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.*;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.*;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboProductItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.core.config.Scheduled;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
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
import org.springframework.transaction.annotation.Transactional;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.COMBO_PRODUCT_NOT_EXISTS;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaserService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;

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

    @Resource
    private ErpPurchaserService purchaserService;

    @Resource
    private ErpSupplierService supplierService;

    @EventListener(ApplicationReadyEvent.class)
    public void initESIndex() {
        try {
            // 初始化组合产品主表索引
            IndexOperations comboIndexOps = elasticsearchRestTemplate.indexOps(ErpComboProductES.class);
            if (!comboIndexOps.exists()) {
                comboIndexOps.create();
                comboIndexOps.putMapping(comboIndexOps.createMapping(ErpComboProductES.class));
            }

            // 初始化组合产品关联项索引
            IndexOperations itemIndexOps = elasticsearchRestTemplate.indexOps(ErpComboProductItemES.class);
            if (!itemIndexOps.exists()) {
                itemIndexOps.create();
                itemIndexOps.putMapping(itemIndexOps.createMapping(ErpComboProductItemES.class));
            }
        } catch (Exception e) {
            System.err.println("组合产品索引初始化失败: " + e.getMessage());
        }
    }
    @Override
    public Long createCombo(@Valid ErpComboSaveReqVO createReqVO) {
        // 生成组合产品编号
        String no = noRedisDAO.generate(ErpNoRedisDAO.COMBO_PRODUCT_NO_PREFIX);
        if (erpComboMapper.selectByNo(no) != null) {
            throw exception(COMBO_PRODUCT_NOT_EXISTS);
        }
        // 校验名称唯一性
        validateComboNameUnique(createReqVO.getName(), null);
        // System.out.println("生成编号耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        //startTime = System.currentTimeMillis();

        // 保存组品信息
        ErpComboProductDO comboProductDO = BeanUtils.toBean(createReqVO, ErpComboProductDO.class)
                .setNo(no);
        erpComboMapper.insert(comboProductDO);
        //System.out.println("保存主表耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        //startTime = System.currentTimeMillis();

        // 保存关联项
        if (createReqVO.getItems() != null) {
            for (ErpProductRespVO item : createReqVO.getItems()) {
                //long itemStartTime = System.currentTimeMillis();

                ErpComboProductItemDO itemDO = new ErpComboProductItemDO();
                itemDO.setComboProductId(comboProductDO.getId());
                itemDO.setItemProductId(item.getId()); // 假设 ErpProductRespVO 中有 id 字段
                itemDO.setItemQuantity(item.getCount()); // 假设数量默认为 1，或者从其他字段获取
                erpComboProductItemMapper.insert(itemDO);

//                System.out.println("保存关联项[" + item.getId() + "]耗时: " +
//                    (System.currentTimeMillis() - itemStartTime) + "ms");

                // 同步项到 ES
                syncItemToES(itemDO.getId());
            }
        }
        //System.out.println("保存所有关联项总耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        //startTime = System.currentTimeMillis();

        // 同步主表到 ES
        syncComboToES(comboProductDO.getId());
        //System.out.println("同步主表到ES耗时: " + (System.currentTimeMillis() - startTime) + "ms");

        return comboProductDO.getId();
    }
    @Override
    public void updateCombo(@Valid ErpComboSaveReqVO updateReqVO) {
        validateComboExists(updateReqVO.getId());
        // 校验名称唯一性
        validateComboNameUnique(updateReqVO.getName(), updateReqVO.getId());
        ErpComboProductDO updateObj = BeanUtils.toBean(updateReqVO, ErpComboProductDO.class);
        erpComboMapper.updateById(updateObj);

        // 如果有单品关联信息，先删除旧的关联，再保存新的关联
        if (updateReqVO.getItems() != null) {
            // 删除旧的关联
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

        // 在所有关联项更新完成后，再同步主表到 ES
        syncComboToES(updateReqVO.getId());

        // 🔥 新增：强制刷新ES索引，确保代发表能立即获取到最新的采购单价
        try {
            elasticsearchRestTemplate.indexOps(ErpComboProductES.class).refresh();
        } catch (Exception e) {
            System.err.println("组品更新后强制刷新ES索引失败: " + e.getMessage());
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteCombos(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        for (Long id : ids) {
            validateComboExists(id);
        }

        // 2. 批量删除关联的单品信息
        for (Long id : ids) {
            List<ErpComboProductItemDO> items = erpComboProductItemMapper.selectByComboProductId(id);
            if (CollUtil.isNotEmpty(items)) {
                List<Long> itemIds = items.stream().map(ErpComboProductItemDO::getId).collect(Collectors.toList());
                erpComboProductItemMapper.deleteBatchIds(itemIds);

                // 批量删除关联项ES记录
                try {
                    comboProductItemESRepository.deleteAllById(itemIds);
                } catch (Exception e) {
                    System.err.println("批量删除组品关联项ES记录失败: " + e.getMessage());
                }
            }
        }

        // 3. 批量删除主表
        erpComboMapper.deleteBatchIds(ids);

        // 4. 批量删除主表ES记录
        try {
            comboProductESRepository.deleteAllById(ids);
        } catch (Exception e) {
            System.err.println("批量删除组品ES记录失败: " + e.getMessage());
            // ES删除失败不影响数据库删除
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

    @Override
    public PageResult<ErpComboRespVO> getComboVOPage(ErpComboPageReqVO pageReqVO) {
        try {
            // 1. 检查数据库是否有数据
            long dbCount = erpComboMapper.selectCount(null);

            // 2. 检查ES索引是否存在
            IndexOperations comboIndexOps = elasticsearchRestTemplate.indexOps(ErpComboProductES.class);
            boolean comboIndexExists = comboIndexOps.exists();
            IndexOperations itemIndexOps = elasticsearchRestTemplate.indexOps(ErpComboProductItemES.class);
            boolean itemIndexExists = itemIndexOps.exists();

            // 3. 检查ES数据量
            long comboEsCount = 0;
            long itemEsCount = 0;
            if (comboIndexExists) {
                comboEsCount = elasticsearchRestTemplate.count(new NativeSearchQueryBuilder().build(), ErpComboProductES.class);
            }
            if (itemIndexExists) {
                itemEsCount = elasticsearchRestTemplate.count(new NativeSearchQueryBuilder().build(), ErpComboProductItemES.class);
            }

            // 4. 处理数据库和ES数据不一致的情况
            if (dbCount == 0) {
                if (comboIndexExists && comboEsCount > 0) {
                    // 数据库为空但组合产品ES有数据，清空ES
                    comboProductESRepository.deleteAll();
                }
                if (itemIndexExists && itemEsCount > 0) {
                    // 数据库为空但组合产品关联项ES有数据，清空ES
                    comboProductItemESRepository.deleteAll();
                }
                return new PageResult<>(Collections.emptyList(), 0L);
            }

            if (!comboIndexExists) {
                initESIndex();
                fullSyncToES();
                return getComboVOPageFromDB(pageReqVO);
            }

            if (comboEsCount == 0 || dbCount != comboEsCount) {
                fullSyncToES();
                if (comboEsCount == 0) {
                    return getComboVOPageFromDB(pageReqVO);
                }
            }

            // 1. 构建基础查询条件
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withTrackTotalHits(true)
                    .withSort(Sort.by(Sort.Direction.DESC, "create_time")) // 修改：按创建时间倒序排列（新增的在前面）
                    .withSort(Sort.by(Sort.Direction.DESC, "id")); // 辅助排序：ID倒序

            // 处理分页参数
            // 检查是否是导出操作（pageSize为-1）
            if (PageParam.PAGE_SIZE_NONE.equals(pageReqVO.getPageSize())) {
                // 导出所有数据，不使用分页，但限制最大返回数量防止内存溢出
                queryBuilder.withPageable(PageRequest.of(0, 10000)); // 最多返回10000条
            } else {
                // 正常分页查询
                queryBuilder.withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()));
            }

            // 添加查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            if (StringUtils.isNotBlank(pageReqVO.getKeyword())) {
                // 统一关键字搜索，针对name、no、shippingCode、purchaser、supplier、shortName
                String keyword = pageReqVO.getKeyword().trim();
                BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
                keywordQuery.should(createSimplifiedKeywordMatchQuery("name", keyword));
                keywordQuery.should(createSimplifiedKeywordMatchQuery("no", keyword));
                keywordQuery.should(createSimplifiedKeywordMatchQuery("shipping_code", keyword));
                keywordQuery.should(createSimplifiedKeywordMatchQuery("purchaser", keyword));
                keywordQuery.should(createSimplifiedKeywordMatchQuery("supplier", keyword));
                keywordQuery.should(createSimplifiedKeywordMatchQuery("short_name", keyword));
                keywordQuery.minimumShouldMatch(1);
                boolQuery.must(keywordQuery);
            } else {
                if (StringUtils.isNotBlank(pageReqVO.getNo())) {
                    boolQuery.must(createSimplifiedKeywordMatchQuery("no", pageReqVO.getNo().trim()));
                }
                if (StringUtils.isNotBlank(pageReqVO.getName())) {
                    boolQuery.must(createSimplifiedKeywordMatchQuery("name", pageReqVO.getName().trim()));
                }
                if (StringUtils.isNotBlank(pageReqVO.getShortName())) {
                    boolQuery.must(createSimplifiedKeywordMatchQuery("short_name", pageReqVO.getShortName().trim()));
                }
                if (StringUtils.isNotBlank(pageReqVO.getShippingCode())) {
                    boolQuery.must(createSimplifiedKeywordMatchQuery("shipping_code", pageReqVO.getShippingCode().trim()));
                }
                if (StringUtils.isNotBlank(pageReqVO.getPurchaser())) {
                    boolQuery.must(createSimplifiedKeywordMatchQuery("purchaser", pageReqVO.getPurchaser().trim()));
                }
                if (StringUtils.isNotBlank(pageReqVO.getSupplier())) {
                    boolQuery.must(createSimplifiedKeywordMatchQuery("supplier", pageReqVO.getSupplier().trim()));
                }
                if (StringUtils.isNotBlank(pageReqVO.getCreator())) {
                    boolQuery.must(createSimplifiedKeywordMatchQuery("creator", pageReqVO.getCreator().trim()));
                }
                // 时间范围
                if (pageReqVO.getCreateTime() != null && pageReqVO.getCreateTime().length == 2) {
                    boolQuery.must(QueryBuilders.rangeQuery("create_time")
                            .gte(pageReqVO.getCreateTime()[0].toString())
                            .lte(pageReqVO.getCreateTime()[1].toString()));
                }
            }

            // 如果有任何查询条件，添加到查询构建器
            if (boolQuery.hasClauses()) {
                queryBuilder.withQuery(boolQuery);
            } else {
                // 如果没有查询条件，使用matchAllQuery
                queryBuilder.withQuery(QueryBuilders.matchAllQuery());
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

            // 获取所有组合产品ID
            List<Long> comboIds = searchHits.stream()
                    .map(hit -> hit.getContent().getId())
                    .collect(Collectors.toList());

            // 从ES查询所有关联项
            NativeSearchQuery itemQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termsQuery("combo_product_id", comboIds))
                    .withPageable(PageRequest.of(0, 10000)) // 修复：移除分页限制，确保获取所有关联项
                    .withTrackTotalHits(true) // 确保获取全部命中数
                    .build();

            SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                    itemQuery,
                    ErpComboProductItemES.class,
                    IndexCoordinates.of("erp_combo_product_items"));

            // 按组合产品ID分组关联项
            Map<Long, List<ErpComboProductItemES>> itemsMap = itemHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.groupingBy(ErpComboProductItemES::getComboProductId));

            // 🔥 关键修复：对每个组合产品的关联项按ID排序，确保顺序与数据库一致
            itemsMap.forEach((comboId, items) -> {
                items.sort(Comparator.comparing(ErpComboProductItemES::getId));
            });

            // 获取所有产品ID
            List<Long> productIds = itemHits.stream()
                    .map(hit -> hit.getContent().getItemProductId())
                    .distinct()
                    .collect(Collectors.toList());

            // 从ES查询所有产品
            NativeSearchQuery productQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.idsQuery().addIds(productIds.stream().map(String::valueOf).toArray(String[]::new)))
                    .withPageable(PageRequest.of(0, 10000)) // 修复：移除分页限制，确保获取所有产品
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
                        List<ErpComboProductItemES> items = itemsMap.getOrDefault(combo.getId(), Collections.emptyList());
                        StringBuilder nameBuilder = new StringBuilder();
                        StringBuilder itemsStringBuilder = new StringBuilder();
                        BigDecimal totalWeight = BigDecimal.ZERO;

                        for (int i = 0; i < items.size(); i++) {
                            ErpProductESDO product = productMap.get(items.get(i).getItemProductId());
                            if (product == null) continue;

                            if (i > 0) {
                                nameBuilder.append("｜");
                                itemsStringBuilder.append(";");
                            }
                            nameBuilder.append(product.getName())
                                    .append("×")
                                    .append(items.get(i).getItemQuantity());

                            itemsStringBuilder.append(product.getNo())
                                    .append(",")
                                    .append(items.get(i).getItemQuantity());

                            if (product.getWeight() != null) {
                                BigDecimal quantity = new BigDecimal(items.get(i).getItemQuantity());
                                totalWeight = totalWeight.add(product.getWeight().multiply(quantity));
                            }
                        }

                        ErpComboRespVO vo = BeanUtils.toBean(combo, ErpComboRespVO.class);
                        vo.setName(nameBuilder.toString());
                        vo.setWeight(totalWeight);
                        vo.setItemsString(itemsStringBuilder.toString());
                        return vo;
                    })
                    .collect(Collectors.toList());

            return new PageResult<>(voList, searchHits.getTotalHits());
        } catch (Exception e) {
            System.err.println("ES查询失败，回退到数据库查询: " + e.getMessage());
            return getComboVOPageFromDB(pageReqVO);
        }
    }

    private PageResult<ErpComboRespVO> handleDeepPagination(ErpComboPageReqVO pageReqVO,
                                                            NativeSearchQueryBuilder queryBuilder) {
        // 1. 计算需要跳过的记录数
        int skip = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();

        // 2. 使用search_after直接获取目标页
        NativeSearchQuery query = queryBuilder.build();
        query.setPageable(PageRequest.of(0, pageReqVO.getPageSize()));
        // 保持与原始查询相同的排序方式: 先按create_time降序，再按id降序
        query.addSort(Sort.by(Sort.Direction.DESC, "create_time"));
        query.addSort(Sort.by(Sort.Direction.DESC, "id"));

        // 如果是深度分页，使用search_after
        if (skip > 0) {
            // 先获取前skip条记录
            NativeSearchQueryBuilder prevQueryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(queryBuilder.build().getQuery())
                    .withPageable(PageRequest.of(0, skip))
                    // 保持与原始查询相同的排序方式
                    .withSort(Sort.by(Sort.Direction.DESC, "create_time"))
                    .withSort(Sort.by(Sort.Direction.DESC, "id"))
                    .withTrackTotalHits(true);

            SearchHits<ErpComboProductES> prevHits = elasticsearchRestTemplate.search(
                    prevQueryBuilder.build(),
                    ErpComboProductES.class,
                    IndexCoordinates.of("erp_combo_products"));

            if (prevHits.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), prevHits.getTotalHits());
            }

            // 获取最后一条记录作为search_after的起点
            SearchHit<ErpComboProductES> lastHit = prevHits.getSearchHits().get(prevHits.getSearchHits().size() - 1);
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
                .withPageable(PageRequest.of(0, 10000)) // 修复：移除分页限制，确保获取所有关联项
                .withTrackTotalHits(true) // 确保获取全部命中数
                .build();

        SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                itemQuery,
                ErpComboProductItemES.class,
                IndexCoordinates.of("erp_combo_product_items"));

        // 按组合产品ID分组关联项
        Map<Long, List<ErpComboProductItemES>> itemsMap = itemHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.groupingBy(ErpComboProductItemES::getComboProductId));

        // 🔥 关键修复：对每个组合产品的关联项按ID排序，确保顺序与数据库一致
        itemsMap.forEach((comboId, items) -> {
            items.sort(Comparator.comparing(ErpComboProductItemES::getId));
        });

        // 获取所有产品ID
        List<Long> productIds = itemHits.stream()
                .map(hit -> hit.getContent().getItemProductId())
                .distinct()
                .collect(Collectors.toList());

        // 从ES查询所有产品
        NativeSearchQuery productQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.idsQuery().addIds(productIds.stream().map(String::valueOf).toArray(String[]::new)))
                .withPageable(PageRequest.of(0, 10000)) // 修复：移除分页限制，确保获取所有产品
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
                    List<ErpComboProductItemES> items = itemsMap.getOrDefault(combo.getId(), Collections.emptyList());
                    StringBuilder nameBuilder = new StringBuilder();
                    StringBuilder itemsStringBuilder = new StringBuilder();
                    BigDecimal totalWeight = BigDecimal.ZERO;
                    for (int i = 0; i < items.size(); i++) {
                        ErpProductESDO product = productMap.get(items.get(i).getItemProductId());
                        if (product == null) continue;

                        if (i > 0) {
                            nameBuilder.append("｜");
                            itemsStringBuilder.append(";");
                        }
                        nameBuilder.append(product.getName())
                                .append("×")
                                .append(items.get(i).getItemQuantity());

                        itemsStringBuilder.append(product.getNo())
                                .append(",")
                                .append(items.get(i).getItemQuantity());

                        if (product.getWeight() != null) {
                            BigDecimal quantity = new BigDecimal(items.get(i).getItemQuantity());
                            totalWeight = totalWeight.add(product.getWeight().multiply(quantity));
                        }
                    }

                    ErpComboRespVO vo = BeanUtils.toBean(combo, ErpComboRespVO.class);
                    vo.setName(nameBuilder.toString());
                    vo.setWeight(totalWeight);
                    vo.setItemsString(itemsStringBuilder.toString());
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(voList, searchHits.getTotalHits());
    }

    // 添加数据库查询方法
    private PageResult<ErpComboRespVO> getComboVOPageFromDB(ErpComboPageReqVO pageReqVO) {
        // 执行数据库分页查询，确保传递所有的搜索条件
        PageResult<ErpComboProductDO> pageResult = erpComboMapper.selectPage(pageReqVO);
        List<ErpComboRespVO> voList = new ArrayList<>();

        // 如果没有数据，直接返回空列表
        if (CollUtil.isEmpty(pageResult.getList())) {
            return new PageResult<>(voList, pageResult.getTotal());
        }

        // 获取所有组合产品ID
        List<Long> comboIds = pageResult.getList().stream()
                .map(ErpComboProductDO::getId)
                .collect(Collectors.toList());

        // 批量查询组合产品项
        List<ErpComboProductItemDO> allComboItems = erpComboProductItemMapper.selectByComboProductIds(comboIds);

        // 按组合产品ID分组
        Map<Long, List<ErpComboProductItemDO>> itemsMap = allComboItems.stream()
                .collect(Collectors.groupingBy(ErpComboProductItemDO::getComboProductId));

        // 提取所有单品ID
        Set<Long> productIds = allComboItems.stream()
                .map(ErpComboProductItemDO::getItemProductId)
                .collect(Collectors.toSet());

        // 批量查询单品详细信息
        List<ErpProductDO> products = erpProductMapper.selectBatchIds(productIds);
        Map<Long, ErpProductDO> productMap = products.stream()
                .collect(Collectors.toMap(ErpProductDO::getId, p -> p, (p1, p2) -> p1));

        // 构建响应VO
        for (ErpComboProductDO combo : pageResult.getList()) {
            ErpComboRespVO vo = BeanUtils.toBean(combo, ErpComboRespVO.class);

            // 处理组合产品项
            List<ErpComboProductItemDO> comboItems = itemsMap.getOrDefault(combo.getId(), Collections.emptyList());

            // 确保项目顺序一致
            comboItems.sort(Comparator.comparing(ErpComboProductItemDO::getId));

            // 构建itemsString和计算总重量
            StringBuilder itemsStringBuilder = new StringBuilder();
            StringBuilder nameBuilder = new StringBuilder();
            BigDecimal totalWeight = BigDecimal.ZERO;

            for (int i = 0; i < comboItems.size(); i++) {
                ErpComboProductItemDO item = comboItems.get(i);
                ErpProductDO product = productMap.get(item.getItemProductId());
                if (product == null) continue;

                if (i > 0) {
                    itemsStringBuilder.append(";");
                    nameBuilder.append("｜");
                }

                // 添加到itemsString
                itemsStringBuilder.append(product.getNo())
                        .append(",")
                        .append(item.getItemQuantity());

                // 构建名称
                nameBuilder.append(product.getName())
                        .append("×")
                        .append(item.getItemQuantity());

                // 计算总重量
                if (product.getWeight() != null) {
                    BigDecimal quantity = new BigDecimal(item.getItemQuantity());
                    totalWeight = totalWeight.add(product.getWeight().multiply(quantity));
                }
            }

            // 设置组合产品名称、项目字符串和重量
            if (StrUtil.isBlank(vo.getName())) {
                vo.setName(nameBuilder.toString());
            }
            vo.setItemsString(itemsStringBuilder.toString());
            vo.setWeight(totalWeight);

            voList.add(vo);
        }

        return new PageResult<>(voList, pageResult.getTotal());
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
        try {
            // 从ES查询组品基本信息
            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(id);
            if (!comboProductOpt.isPresent()) {
                return null;
            }
            ErpComboProductES comboProduct = comboProductOpt.get();

            // 从ES查询组品关联的单品项
            NativeSearchQuery itemQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("combo_product_id", id))
                    .withSort(Sort.by(Sort.Direction.ASC, "id")) // 按ID升序排序
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                    itemQuery,
                    ErpComboProductItemES.class,
                    IndexCoordinates.of("erp_combo_product_items"));

            if (itemHits.isEmpty()) {
                // 如果没有关联项，返回基本信息
                ErpComboRespVO comboRespVO = BeanUtils.toBean(comboProduct, ErpComboRespVO.class);
                comboRespVO.setItems(Collections.emptyList());
                return comboRespVO;
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

            // 组装单品名称字符串 (单品A*数量+单品B*数量)
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

            // 计算总重量、采购总价、批发总价
            BigDecimal totalWeight = BigDecimal.ZERO;
            BigDecimal totalPurchasePrice = BigDecimal.ZERO;
            BigDecimal totalWholesalePrice = BigDecimal.ZERO;

            for (ErpComboProductItemES item : items) {
                ErpProductESDO product = productMap.get(item.getItemProductId());
                if (product != null) {
                    BigDecimal itemQuantity = new BigDecimal(item.getItemQuantity());

                    // 计算总重量
                    if (product.getWeight() != null) {
                        totalWeight = totalWeight.add(product.getWeight().multiply(itemQuantity));
                    }

                    // 计算采购总价
                    if (product.getPurchasePrice() != null) {
                        totalPurchasePrice = totalPurchasePrice.add(product.getPurchasePrice().multiply(itemQuantity));
                    }

                    // 计算批发总价
                    if (product.getWholesalePrice() != null) {
                        totalWholesalePrice = totalWholesalePrice.add(product.getWholesalePrice().multiply(itemQuantity));
                    }
                }
            }

            // 组装响应对象
            ErpComboRespVO comboRespVO = BeanUtils.toBean(comboProduct, ErpComboRespVO.class);
            comboRespVO.setName(nameBuilder.toString());
            comboRespVO.setWeight(totalWeight);
            comboRespVO.setPurchasePrice(totalPurchasePrice);
            comboRespVO.setWholesalePrice(totalWholesalePrice);

            // 组装单品列表
            List<ErpProductRespVO> productVOs = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                ErpProductESDO product = productMap.get(items.get(i).getItemProductId());
                if (product != null) {
                    ErpProductRespVO productVO = BeanUtils.toBean(product, ErpProductRespVO.class);
                    productVO.setCount(items.get(i).getItemQuantity());
                    productVOs.add(productVO);
                }
            }
            comboRespVO.setItems(productVOs);

            // 构建itemsString字段，格式为"产品编号,数量;产品编号,数量"
            StringBuilder itemsStringBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    itemsStringBuilder.append(";");
                }
                ErpProductESDO product = productMap.get(items.get(i).getItemProductId());
                if (product != null) {
                    itemsStringBuilder.append(product.getNo())
                            .append(",")
                            .append(items.get(i).getItemQuantity());
                }
            }
            comboRespVO.setItemsString(itemsStringBuilder.toString());

            return comboRespVO;
        } catch (Exception e) {
            System.err.println("ES查询组合产品详情失败，ID: " + id + ", 错误: " + e.getMessage());
            // 如果ES查询失败，回退到数据库查询
            return getComboWithItemsFromDB(id);
        }
    }

    /**
     * 从数据库获取组合产品详情（ES查询失败时的回退方案）
     */
    private ErpComboRespVO getComboWithItemsFromDB(Long id) {
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
                nameBuilder.append("｜");
            }
            nameBuilder.append(products.get(i).getName())
                    .append("×")
                    .append(comboItems.get(i).getItemQuantity());
        }

        // 计算总重量、采购总价、批发总价
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalPurchasePrice = BigDecimal.ZERO;
        BigDecimal totalWholesalePrice = BigDecimal.ZERO;

        for (int i = 0; i < products.size(); i++) {
            BigDecimal itemWeight = products.get(i).getWeight();
            if (itemWeight != null) {
                BigDecimal quantity = new BigDecimal(comboItems.get(i).getItemQuantity());
                totalWeight = totalWeight.add(itemWeight.multiply(quantity));
            }

            // 计算采购总价
            BigDecimal purchasePrice = products.get(i).getPurchasePrice();
            if (purchasePrice != null) {
                BigDecimal itemQuantity = new BigDecimal(comboItems.get(i).getItemQuantity());
                totalPurchasePrice = totalPurchasePrice.add(purchasePrice.multiply(itemQuantity));
            }

            // 计算批发总价
            BigDecimal wholesalePrice = products.get(i).getWholesalePrice();
            if (wholesalePrice != null) {
                BigDecimal itemQuantity = new BigDecimal(comboItems.get(i).getItemQuantity());
                totalWholesalePrice = totalWholesalePrice.add(wholesalePrice.multiply(itemQuantity));
            }
        }

        // 组装响应对象
        ErpComboRespVO comboRespVO = BeanUtils.toBean(comboProduct, ErpComboRespVO.class);
        comboRespVO.setName(nameBuilder.toString());
        comboRespVO.setWeight(totalWeight);
        comboRespVO.setPurchasePrice(totalPurchasePrice);
        comboRespVO.setWholesalePrice(totalWholesalePrice);

        // 组装单品列表
        List<ErpProductRespVO> productVOs = products.stream()
                .map(product -> BeanUtils.toBean(product, ErpProductRespVO.class))
                .collect(Collectors.toList());

        // 将 itemQuantity 赋值给 count
        for (int i = 0; i < productVOs.size(); i++) {
            productVOs.get(i).setCount(comboItems.get(i).getItemQuantity());
        }
        comboRespVO.setItems(productVOs);

        // 构建itemsString字段，格式为"产品编号,数量;产品编号,数量"
        StringBuilder itemsStringBuilder = new StringBuilder();
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) {
                itemsStringBuilder.append(";");
            }
            itemsStringBuilder.append(products.get(i).getNo())
                    .append(",")
                    .append(comboItems.get(i).getItemQuantity());
        }
        comboRespVO.setItemsString(itemsStringBuilder.toString());

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
    private void syncComboToES(Long comboId) {
        try {
            ErpComboProductDO combo = erpComboMapper.selectById(comboId);
            if (combo == null) {
                comboProductESRepository.deleteById(comboId);
            } else {
                ErpComboProductES es = convertComboToES(combo);
                comboProductESRepository.save(es);

                // 强制刷新ES索引，确保数据立即可见
                try {
                    elasticsearchRestTemplate.indexOps(ErpComboProductES.class).refresh();
                } catch (Exception refreshException) {
                    System.err.println("刷新ES索引失败: " + refreshException.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("同步组合产品到ES失败，ID: " + comboId + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 转换组合产品DO为ES对象
     * 🔥 重要：确保ES中的采购单价、批发单价、重量等都是实时计算的
     */
    private ErpComboProductES convertComboToES(ErpComboProductDO combo) {
        ErpComboProductES es = new ErpComboProductES();
        BeanUtils.copyProperties(combo, es);

        // 设置keyword字段（用于精确匹配和通配符查询）- 与产品表保持完全一致
        es.setNo(combo.getNo());
        es.setShippingCode(combo.getShippingCode());
        es.setPurchaser(combo.getPurchaser());
        es.setSupplier(combo.getSupplier());
        es.setCreator(combo.getCreator());

        // 🔥 关键修复：实时计算采购单价、批发单价、重量等
        try {
            // 从ES查询组品关联的单品项
            NativeSearchQuery itemQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("combo_product_id", combo.getId()))
                    .withSort(Sort.by(Sort.Direction.ASC, "id")) // 按ID升序排序
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                    itemQuery,
                    ErpComboProductItemES.class,
                    IndexCoordinates.of("erp_combo_product_items"));

            if (!itemHits.isEmpty()) {
                // 提取单品ID列表
                List<Long> productIds = itemHits.stream()
                        .map(hit -> hit.getContent().getItemProductId())
                        .collect(Collectors.toList());

                if (!productIds.isEmpty()) {
                    // 从ES查询单品详细信息
                    NativeSearchQuery productQuery = new NativeSearchQueryBuilder()
                            .withQuery(QueryBuilders.idsQuery().addIds(productIds.stream().map(String::valueOf).toArray(String[]::new)))
                            .withPageable(PageRequest.of(0, 1000))
                            .build();

                    SearchHits<ErpProductESDO> productHits = elasticsearchRestTemplate.search(
                            productQuery,
                            ErpProductESDO.class,
                            IndexCoordinates.of("erp_products"));

                    if (!productHits.isEmpty()) {
                        // 创建单品ID到单品对象的映射
                        Map<Long, ErpProductESDO> productMap = productHits.stream()
                                .collect(Collectors.toMap(
                                        hit -> hit.getContent().getId(),
                                        SearchHit::getContent));

                        // 实时计算采购总价、批发总价、总重量
                        BigDecimal totalPurchasePrice = BigDecimal.ZERO;
                        BigDecimal totalWholesalePrice = BigDecimal.ZERO;
                        BigDecimal totalWeight = BigDecimal.ZERO;

                        for (SearchHit<ErpComboProductItemES> itemHit : itemHits) {
                            ErpComboProductItemES item = itemHit.getContent();
                            ErpProductESDO product = productMap.get(item.getItemProductId());
                            if (product != null) {
                                BigDecimal itemQuantity = new BigDecimal(item.getItemQuantity());

                                // 计算采购总价
                                if (product.getPurchasePrice() != null) {
                                    totalPurchasePrice = totalPurchasePrice.add(
                                            product.getPurchasePrice().multiply(itemQuantity)
                                    );
                                }

                                // 计算批发总价
                                if (product.getWholesalePrice() != null) {
                                    totalWholesalePrice = totalWholesalePrice.add(
                                            product.getWholesalePrice().multiply(itemQuantity)
                                    );
                                }

                                // 计算总重量
                                if (product.getWeight() != null) {
                                    totalWeight = totalWeight.add(
                                            product.getWeight().multiply(itemQuantity)
                                    );
                                }
                            }
                        }

                        // 🔥 核心：使用实时计算的价格和重量覆盖数据库中的值
                        es.setPurchasePrice(totalPurchasePrice);
                        es.setWholesalePrice(totalWholesalePrice);
                        es.setWeight(totalWeight);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ES实时计算组品价格和重量失败，ID: " + combo.getId() + ", 错误: " + e.getMessage());
            // 如果ES计算失败，回退到数据库计算
            calculatePricesAndWeightFromDB(combo.getId(), es);
        }

        // 🔥 关键修复：构建完整的组合名称，确保与前端显示顺序一致
        try {
            String fullComboName = buildComboNameWithOrder(combo.getId());
            String originalName = combo.getName() != null ? combo.getName() : "";

            // 使用构建的完整组合名称作为name和name_keyword
            if (StrUtil.isNotBlank(fullComboName)) {
                // 🔥 修复：只设置主字段
                es.setName(fullComboName);
                es.setNormalizedName(normalizeComboName(fullComboName));
                if (StrUtil.isNotBlank(originalName) && !originalName.equals(fullComboName)) {
                    // 可以通过多值字段或者额外的搜索逻辑来处理
                }
            } else {
                es.setName(originalName);
                es.setNormalizedName(normalizeComboName(originalName));
            }
        } catch (Exception e) {
            System.err.println("构建组合产品名称失败，ID: " + combo.getId() + ", 错误: " + e.getMessage());
            String fallbackName = combo.getName() != null ? combo.getName() : "";
            es.setName(fallbackName);
            es.setNormalizedName(normalizeComboName(fallbackName));
        }

        return es;
    }

    /**
     * 标准化组合产品名称，用于唯一性校验
     * 移除空格、转换为小写、排序单品名称等
     */
    private String normalizeComboName(String comboName) {
        if (StrUtil.isBlank(comboName)) {
            return "";
        }

        try {
            // 解析组合名称，提取单品和数量
            Map<String, Integer> nameMap = extractNameMap(comboName);

            // 按单品名称排序，确保相同组合的不同顺序被视为相同
            List<String> sortedItems = nameMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "×" + entry.getValue())
                    .collect(Collectors.toList());

            // 重新组合为标准格式
            return String.join("｜", sortedItems);
        } catch (Exception e) {
            // 如果解析失败，返回原名称的标准化版本
            return comboName.trim().toLowerCase().replaceAll("\\s+", "");
        }
    }

    /**
     * 构建组合产品名称（确保与前端显示顺序一致）
     * 🔥 关键修复：确保ES中存储的name_keyword字段与前端显示的产品名称顺序完全一致
     * 🔥 性能优化：使用ES查询替代数据库查询，提高搜索效率
     */
    private String buildComboNameWithOrder(Long comboId) {
        try {
            // 从ES查询组合产品关联的单品项，按照ID顺序
            NativeSearchQuery itemQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("combo_product_id", comboId))
                    .withSort(Sort.by(Sort.Direction.ASC, "id")) // 按ID升序排序，确保顺序与插入顺序一致
                    .withPageable(PageRequest.of(0, 1000)) // 限制最大数量
                    .build();

            SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                    itemQuery,
                    ErpComboProductItemES.class,
                    IndexCoordinates.of("erp_combo_product_items"));

            if (itemHits.isEmpty()) {
                return "";
            }

            // 提取单品ID列表，保持ES返回的顺序
            List<Long> productIds = itemHits.stream()
                    .map(hit -> hit.getContent().getItemProductId())
                    .collect(Collectors.toList());

            if (productIds.isEmpty()) {
                return "";
            }

            // 从ES查询所有产品信息
            NativeSearchQuery productQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.idsQuery().addIds(productIds.stream().map(String::valueOf).toArray(String[]::new)))
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            SearchHits<ErpProductESDO> productHits = elasticsearchRestTemplate.search(
                    productQuery,
                    ErpProductESDO.class,
                    IndexCoordinates.of("erp_products"));

            // 创建产品ID到产品对象的映射
            Map<Long, ErpProductESDO> productMap = productHits.stream()
                    .collect(Collectors.toMap(
                            hit -> hit.getContent().getId(),
                            SearchHit::getContent));

            // 构建名称字符串，严格按照ES返回的关联项顺序
            StringBuilder nameBuilder = new StringBuilder();
            List<ErpComboProductItemES> items = itemHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            for (int i = 0; i < items.size(); i++) {
                ErpComboProductItemES item = items.get(i);
                ErpProductESDO product = productMap.get(item.getItemProductId());
                if (product == null) {
                    continue;
                }

                if (i > 0) {
                    nameBuilder.append("｜");
                }
                nameBuilder.append(product.getName())
                        .append("×")
                        .append(item.getItemQuantity());
            }

            return nameBuilder.toString();
        } catch (Exception e) {
            System.err.println("ES构建组合产品名称失败，ID: " + comboId + ", 错误: " + e.getMessage());
            // 如果ES查询失败，回退到数据库查询
            return buildComboNameFromDB(comboId);
        }
    }

    /**
     * 从数据库构建组合产品名称（ES查询失败时的回退方案）
     */
    private String buildComboNameFromDB(Long comboId) {
        // 查询组合产品关联的单品项，按照数据库中的顺序
        List<ErpComboProductItemDO> comboItems = erpComboProductItemMapper.selectByComboProductId(comboId);
        if (CollUtil.isEmpty(comboItems)) {
            return "";
        }

        // 提取单品ID列表，保持原有顺序
        List<Long> productIds = comboItems.stream()
                .map(ErpComboProductItemDO::getItemProductId)
                .collect(Collectors.toList());

        // 查询单品详细信息
        List<ErpProductDO> products = erpProductMapper.selectBatchIds(productIds);
        if (CollUtil.isEmpty(products)) {
            return "";
        }

        // 构建名称字符串，严格按照comboItems的顺序
        StringBuilder nameBuilder = new StringBuilder();
        Map<Long, ErpProductDO> productMap = products.stream()
                .collect(Collectors.toMap(ErpProductDO::getId, p -> p));

        for (int i = 0; i < comboItems.size(); i++) {
            ErpComboProductItemDO item = comboItems.get(i);
            ErpProductDO product = productMap.get(item.getItemProductId());
            if (product == null) {
                continue;
            }

            if (i > 0) {
                nameBuilder.append("｜");
            }
            nameBuilder.append(product.getName())
                    .append("×")
                    .append(item.getItemQuantity());
        }

        return nameBuilder.toString();
    }

    /**
     * 同步组合产品项到 ES
     */
    private void syncItemToES(Long itemId) {
        try {
            ErpComboProductItemDO item = erpComboProductItemMapper.selectById(itemId);
            if (item == null) {
                comboProductItemESRepository.deleteById(itemId);
            } else {
                ErpComboProductItemES es = new ErpComboProductItemES();
                BeanUtils.copyProperties(item, es);
                comboProductItemESRepository.save(es);

                // 强制刷新ES索引，确保数据立即可见
                try {
                    elasticsearchRestTemplate.indexOps(ErpComboProductItemES.class).refresh();
                } catch (Exception refreshException) {
                    System.err.println("刷新ES关联项索引失败: " + refreshException.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("同步组合产品关联项到ES失败，ID: " + itemId + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

            // 强制刷新ES索引
            try {
                elasticsearchRestTemplate.indexOps(ErpComboProductES.class).refresh();
                elasticsearchRestTemplate.indexOps(ErpComboProductItemES.class).refresh();
            } catch (Exception refreshException) {
                System.err.println("刷新ES索引失败: " + refreshException.getMessage());
            }

            System.out.println("全量同步ES数据完成，共同步" + comboESList.size() + "条组合产品和" + itemESList.size() + "条关联项");
        } catch (Exception e) {
            System.err.println("全量同步ES数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 手动同步单个组合产品到ES（包括主表和关联项）
     */
    @Override
    public void manualSyncComboToES(Long comboId) {
        try {
            System.out.println("开始手动同步组合产品到ES，ID: " + comboId);

            // 同步主表
            syncComboToES(comboId);

            // 同步关联项
            List<ErpComboProductItemDO> items = erpComboProductItemMapper.selectByComboProductId(comboId);
            for (ErpComboProductItemDO item : items) {
                syncItemToES(item.getId());
            }

            System.out.println("手动同步组合产品完成，ID: " + comboId);
        } catch (Exception e) {
            System.err.println("手动同步组合产品失败，ID: " + comboId + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateComboNameUnique(String name, Long id) {
        if (StrUtil.isBlank(name)) {
            return; // 如果名称为空，直接返回
        }

        // 1. 提取组合产品名称的关键信息（忽略顺序）
        Map<String, Integer> nameMap = extractNameMap(name);

        // 2. 构建 ES 查询条件 - 使用精确查询而不是分词查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("name", name)) // 使用termQuery进行精确查询
                .withPageable(PageRequest.of(0, 1)); // 只需要查询一条记录即可

        // 3. 执行查询
        SearchHits<ErpComboProductES> searchHits = elasticsearchRestTemplate.search(
                queryBuilder.build(),
                ErpComboProductES.class,
                IndexCoordinates.of("erp_combo_products"));

        // 4. 比较查询结果
        if (!searchHits.isEmpty()) {
            ErpComboProductES existingCombo = searchHits.getSearchHits().get(0).getContent();
            if (id == null || !existingCombo.getId().equals(id)) { // 如果是新增，或者更新但不是同一个ID
                // 比较关键信息是否一致
                Map<String, Integer> existingNameMap = extractNameMap(existingCombo.getName());
                if (nameMap.equals(existingNameMap)) {
                    throw exception(COMBO_PRODUCT_NAME_DUPLICATE, "组合产品名称重复: " + name ); // 抛出异常
                }
            }
        }
    }

    /**
     * 校验组合产品名称在ES中是否唯一（导入专用）
     * 参考新增时的校验方法，但专门用于导入场景
     */
    private void validateComboNameUniqueInES(String name, Long excludeId) {
        if (StrUtil.isBlank(name)) {
            return; // 如果名称为空，直接返回
        }

        try {
            // 1. 提取组合产品名称的关键信息（忽略顺序）
            Map<String, Integer> nameMap = extractNameMap(name);

            // 2. 构建 ES 查询条件 - 使用精确查询而不是分词查询
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("name", name)) // 使用termQuery进行精确查询
                    .withPageable(PageRequest.of(0, 1)); // 只需要查询一条记录即可

            // 3. 执行查询
            SearchHits<ErpComboProductES> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpComboProductES.class,
                    IndexCoordinates.of("erp_combo_products"));

            // 4. 比较查询结果
            if (!searchHits.isEmpty()) {
                ErpComboProductES existingCombo = searchHits.getSearchHits().get(0).getContent();
                if (excludeId == null || !existingCombo.getId().equals(excludeId)) { // 如果是新增，或者更新但不是同一个ID
                    // 比较关键信息是否一致
                    Map<String, Integer> existingNameMap = extractNameMap(existingCombo.getName());
                    if (nameMap.equals(existingNameMap)) {
                        throw exception(COMBO_PRODUCT_NAME_DUPLICATE, "组合产品名称在系统中已存在: " + name); // 抛出异常
                    }
                }
            }
        } catch (Exception e) {
            // 如果ES查询失败，记录错误但不阻止导入（避免ES问题影响导入）
            System.err.println("ES校验组合产品名称失败: " + e.getMessage());
        }
    }

    /**
     * 提取组合产品名称中的单品名称和数量
     * @param name 组合产品名称
     * @return 单品名称和数量的映射
     */
    private Map<String, Integer> extractNameMap(String name) {
        Map<String, Integer> nameMap = new HashMap<>();
        String[] items = name.split("\\｜");
        for (String item : items) {
            String[] parts = item.split("×");
            if (parts.length != 2) {
                throw new IllegalArgumentException("组合产品名称格式不正确，每个单品部分应包含名称和数量，格式为 '名称×数量'，但实际为: " + item);
            }
            String productName = parts[0].trim();
            int quantity;
            try {
                quantity = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("组合产品名称中的数量部分格式不正确，应为整数，但实际为: " + parts[1].trim());
            }
            nameMap.put(productName, quantity);
        }
        return nameMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpComboImportRespVO importComboList(List<ErpComboImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(COMBO_PRODUCT_IMPORT_LIST_IS_EMPTY);
        }

        // 1. 初始化返回结果
        ErpComboImportRespVO respVO = ErpComboImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        try {
            // 2. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateAllImportData(importList, isUpdateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            String username = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
            LocalDateTime now = LocalDateTime.now();
            // 3. 批量处理列表
            List<ErpComboProductDO> createList = new ArrayList<>();
            List<ErpComboProductDO> updateList = new ArrayList<>();
            List<ErpComboProductItemDO> createItemList = new ArrayList<>();
            List<ErpComboProductItemDO> updateItemList = new ArrayList<>();

            // 4. 批量查询已存在的组合产品
            Set<String> noSet = importList.stream()
                    .map(ErpComboImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());

            Map<String, ErpComboProductDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(erpComboMapper.selectListByNoIn(noSet), ErpComboProductDO::getNo);

            // 5. 批量查询所有单品编号，验证单品是否存在
            Set<String> allProductNos = new HashSet<>();
            for (ErpComboImportExcelVO importVO : importList) {
                if (StrUtil.isNotBlank(importVO.getItemsString())) {
                    List<ComboItem> items = parseItemsString(importVO.getItemsString());
                    for (ComboItem item : items) {
                        allProductNos.add(item.getNo());
                    }
                }
            }

            Map<String, ErpProductDO> productMap = new HashMap<>();
            if (!allProductNos.isEmpty()) {
                List<ErpProductDO> products = erpProductMapper.selectListByNoIn(allProductNos);
                productMap = convertMap(products, ErpProductDO::getNo);
            }

            // 6. 批量转换和保存数据
            Map<String, String> noToItemsStringMap = new HashMap<>(); // 存储编号到itemsString的映射

            for (int i = 0; i < importList.size(); i++) {
                ErpComboImportExcelVO importVO = importList.get(i);

                // 计算组合产品名称
                String calculatedName = calculateComboName(importVO.getItemsString(), productMap);

                // 判断是新增还是更新
                ErpComboProductDO existCombo = existMap.get(importVO.getNo());

                if (existCombo == null) {
                    // 创建组合产品
                    ErpComboProductDO comboProduct = BeanUtils.toBean(importVO, ErpComboProductDO.class);
                    comboProduct.setNo(noRedisDAO.generate(ErpNoRedisDAO.COMBO_PRODUCT_NO_PREFIX));

                    // 计算价格和重量，并设置计算出的名称
                    calculateAndSetPricesAndWeight(importVO, comboProduct, productMap);
                    comboProduct.setName(calculatedName).setCreator(username).setCreateTime(now);

                    createList.add(comboProduct);
                    respVO.getCreateNames().add(comboProduct.getNo());

                    // 保存编号到itemsString的映射
                    noToItemsStringMap.put(comboProduct.getNo(), importVO.getItemsString());
                } else if (isUpdateSupport) {
                    // 更新组合产品
                    ErpComboProductDO updateCombo = BeanUtils.toBean(importVO, ErpComboProductDO.class);
                    updateCombo.setId(existCombo.getId());
                    updateCombo.setNo(existCombo.getNo()); // 保持原有编号

                    // 计算价格和重量，并设置计算出的名称
                    calculateAndSetPricesAndWeight(importVO, updateCombo, productMap);
                    updateCombo.setName(calculatedName).setCreator(username).setCreateTime(now);

                    updateList.add(updateCombo);
                    respVO.getUpdateNames().add(updateCombo.getNo());

                    // 保存编号到itemsString的映射
                    noToItemsStringMap.put(updateCombo.getNo(), importVO.getItemsString());
                }
            }

            // 7. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                // 批量插入新组合产品
                erpComboMapper.insertBatch(createList);

                // 批量同步到ES
                batchSyncCombosToES(createList);

                // 批量插入组合产品项
                for (ErpComboProductDO combo : createList) {
                    String itemsString = noToItemsStringMap.get(combo.getNo());
                    if (StrUtil.isNotBlank(itemsString)) {
                        List<ErpComboProductItemDO> items = buildComboItems(combo, itemsString, productMap);
                        createItemList.addAll(items);
                    }
                }

                // 批量插入组合产品项
                if (CollUtil.isNotEmpty(createItemList)) {
                    erpComboProductItemMapper.insertBatch(createItemList);
                    // 批量同步项到ES
                    batchSyncComboItemsToES(createItemList);
                }
            }

            if (CollUtil.isNotEmpty(updateList)) {
                // 先批量删除旧的组合产品项
                List<Long> updateIds = updateList.stream().map(ErpComboProductDO::getId).collect(Collectors.toList());
                List<ErpComboProductItemDO> oldItems = erpComboProductItemMapper.selectByComboProductIds(updateIds);
                if (CollUtil.isNotEmpty(oldItems)) {
                    List<Long> oldItemIds = oldItems.stream().map(ErpComboProductItemDO::getId).collect(Collectors.toList());
                    erpComboProductItemMapper.deleteBatchIds(oldItemIds);
                    // 批量删除ES中的关联项
                    comboProductItemESRepository.deleteAllById(oldItemIds);
                }

                // 批量更新组合产品
                erpComboMapper.updateBatch(updateList);

                // 批量同步到ES
                batchSyncCombosToES(updateList);

                // 批量插入新的组合产品项
                for (ErpComboProductDO combo : updateList) {
                    String itemsString = noToItemsStringMap.get(combo.getNo());
                    if (StrUtil.isNotBlank(itemsString)) {
                        List<ErpComboProductItemDO> items = buildComboItems(combo, itemsString, productMap);
                        updateItemList.addAll(items);
                    }
                }

                // 批量插入组合产品项
                if (CollUtil.isNotEmpty(updateItemList)) {
                    erpComboProductItemMapper.insertBatch(updateItemList);
                    // 批量同步项到ES
                    batchSyncComboItemsToES(updateItemList);
                }
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
    private Map<String, String> validateAllImportData(List<ErpComboImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的组合产品
        Set<String> noSet = importList.stream()
                .map(ErpComboImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, ErpComboProductDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(erpComboMapper.selectListByNoIn(noSet), ErpComboProductDO::getNo);

        // 3. 批量查询所有单品编号，验证单品是否存在
        Set<String> allProductNos = new HashSet<>();
        for (ErpComboImportExcelVO importVO : importList) {
            if (StrUtil.isNotBlank(importVO.getItemsString())) {
                List<ComboItem> items = parseItemsString(importVO.getItemsString());
                for (ComboItem item : items) {
                    allProductNos.add(item.getNo());
                }
            }
        }

        Map<String, ErpProductDO> productMap = new HashMap<>();
        if (!allProductNos.isEmpty()) {
            List<ErpProductDO> products = erpProductMapper.selectListByNoIn(allProductNos);
            productMap = convertMap(products, ErpProductDO::getNo);
        }

        // 4. 批量查询所有采购人员名称，验证采购人员是否存在
        Set<String> purchaserNames = importList.stream()
                .map(ErpComboImportExcelVO::getPurchaser)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, Boolean> purchaserExistsMap = new HashMap<>();
        for (String purchaserName : purchaserNames) {
            List<ErpPurchaserRespVO> purchasers = purchaserService.searchPurchasers(
                    new ErpPurchaserPageReqVO().setPurchaserName(purchaserName));
            purchaserExistsMap.put(purchaserName, CollUtil.isNotEmpty(purchasers));
        }

        // 5. 批量查询所有供应商名称，验证供应商是否存在
        Set<String> supplierNames = importList.stream()
                .map(ErpComboImportExcelVO::getSupplier)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, Boolean> supplierExistsMap = new HashMap<>();
        for (String supplierName : supplierNames) {
            List<ErpSupplierRespVO> suppliers = supplierService.searchSuppliers(
                    new ErpSupplierPageReqVO().setName(supplierName));
            supplierExistsMap.put(supplierName, CollUtil.isNotEmpty(suppliers));
        }
        // 用于跟踪Excel内部重复的名称
        Set<String> processedNames = new HashSet<>();

        // 6. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpComboImportExcelVO importVO = importList.get(i);
            String calculatedName = "";
            String errorKey = "第" + (i + 1) + "行";

            try {
                // 6.1 基础数据校验
                if (StrUtil.isBlank(importVO.getItemsString())) {
                    allErrors.put(errorKey, "单品列表不能为空");
                    continue;
                }

                // 6.2 校验单品编号是否存在
                List<ComboItem> comboItems = parseItemsString(importVO.getItemsString());
                for (ComboItem item : comboItems) {
                    if (!productMap.containsKey(item.getNo())) {
                        allErrors.put(errorKey, "单品编号不存在: " + item.getNo());
                        continue;
                    }
                }

                // 6.3 校验采购人员是否存在
                if (StrUtil.isNotBlank(importVO.getPurchaser())) {
                    Boolean purchaserExists = purchaserExistsMap.get(importVO.getPurchaser());
                    if (purchaserExists == null || !purchaserExists) {
                        allErrors.put(errorKey, "采购人员不存在: " + importVO.getPurchaser());
                        continue;
                    }
                }

                // 6.4 校验供应商是否存在
                if (StrUtil.isNotBlank(importVO.getSupplier())) {
                    Boolean supplierExists = supplierExistsMap.get(importVO.getSupplier());
                    if (supplierExists == null || !supplierExists) {
                        allErrors.put(errorKey, "供应商不存在: " + importVO.getSupplier());
                        continue;
                    }
                }

                // 6.5 计算组合产品名称
                calculatedName = calculateComboName(importVO.getItemsString(), productMap);
                errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getItemsString()) ? "(" + calculatedName + ")" : "");

                // 6.6 检查Excel内部名称重复
                if (StrUtil.isNotBlank(calculatedName)) {
                    if (processedNames.contains(calculatedName)) {
                        allErrors.put(errorKey, "组合产品名称重复: " + importVO.getItemsString());
                        continue;
                    }
                    processedNames.add(calculatedName);

                    // 6.6.1 校验组合产品名称在ES中是否已存在（参考新增时的校验方法）
                    try {
                        validateComboNameUniqueInES(calculatedName, null);
                    } catch (ServiceException ex) {
                        allErrors.put(errorKey, ex.getMessage());
                        continue;
                    }
                }

                // 6.7 判断是新增还是更新，并进行相应校验
                ErpComboProductDO existCombo = existMap.get(importVO.getNo());
                if (existCombo == null) {
                    // 新增校验：校验组合产品名称唯一性
                    try {
                        validateComboNameUnique(calculatedName, null);
                    } catch (ServiceException ex) {
                        allErrors.put(errorKey, ex.getMessage());
                    }
                } else if (isUpdateSupport) {
                    // 更新校验：校验组合产品名称唯一性（排除自身）
                    try {
                        validateComboNameUnique(calculatedName, existCombo.getId());
                        // 校验组合产品名称在ES中是否已存在（更新时排除自身）
                        if (StrUtil.isNotBlank(calculatedName)) {
                            validateComboNameUniqueInES(calculatedName, existCombo.getId());
                        }
                    } catch (ServiceException ex) {
                        allErrors.put(errorKey, ex.getMessage());
                    }
                } else {
                    allErrors.put(errorKey, "组合产品编号已存在，不支持更新: " + importVO.getNo());
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
    private Map<String, String> validateDataTypeErrors(List<ErpComboImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取产品名称
                String productName = "未知单品组合编号";
                // 修复：与产品表保持一致的行号处理逻辑

                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpComboImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getItemsString())) {
                        productName = importVO.getItemsString();
                    }
                }

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
     * 构建组合产品项列表
     */
    private List<ErpComboProductItemDO> buildComboItems(ErpComboProductDO combo, String itemsString, Map<String, ErpProductDO> productMap) {
        if (StrUtil.isBlank(itemsString)) {
            return Collections.emptyList();
        }

        List<ComboItem> items = parseItemsString(itemsString);
        if (CollUtil.isEmpty(items)) {
            return Collections.emptyList();
        }

        List<ErpComboProductItemDO> itemList = new ArrayList<>();
        for (ComboItem item : items) {
            ErpProductDO product = productMap.get(item.getNo());
            if (product != null) {
                ErpComboProductItemDO itemDO = new ErpComboProductItemDO();
                itemDO.setComboProductId(combo.getId());
                itemDO.setItemProductId(product.getId());
                itemDO.setItemQuantity(item.getItemQuantity());
                itemList.add(itemDO);
            }
        }

        return itemList;
    }

    /**
     * 批量同步组合产品到ES
     */
    private void batchSyncCombosToES(List<ErpComboProductDO> combos) {
        if (CollUtil.isEmpty(combos)) {
            return;
        }

        try {
            // 批量转换组合产品为ES对象
            List<ErpComboProductES> esList = combos.stream()
                    .map(this::convertComboToES)
                    .filter(Objects::nonNull) // 过滤转换失败的数据
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(esList)) {
                // 批量保存到ES
                comboProductESRepository.saveAll(esList);
                System.out.println("批量同步 " + esList.size() + " 条组合产品到ES成功");
            }
        } catch (Exception e) {
            System.err.println("批量同步组合产品到ES失败: " + e.getMessage());
            // 降级为单条同步
            for (ErpComboProductDO combo : combos) {
                try {
                    syncComboToES(combo.getId());
                } catch (Exception ex) {
                    System.err.println("单条同步组合产品到ES失败，ID: " + combo.getId() + ", 错误: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * 批量同步组合产品项到ES
     */
    private void batchSyncComboItemsToES(List<ErpComboProductItemDO> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }

        try {
            // 批量转换组合产品项为ES对象
            List<ErpComboProductItemES> esList = items.stream()
                    .map(item -> {
                        ErpComboProductItemES es = new ErpComboProductItemES();
                        BeanUtils.copyProperties(item, es);
                        return es;
                    })
                    .filter(Objects::nonNull) // 过滤转换失败的数据
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(esList)) {
                // 批量保存到ES
                comboProductItemESRepository.saveAll(esList);
                System.out.println("批量同步 " + esList.size() + " 条组合产品项到ES成功");
            }
        } catch (Exception e) {
            System.err.println("批量同步组合产品项到ES失败: " + e.getMessage());
            // 降级为单条同步
            for (ErpComboProductItemDO item : items) {
                try {
                    syncItemToES(item.getId());
                } catch (Exception ex) {
                    System.err.println("单条同步组合产品项到ES失败，ID: " + item.getId() + ", 错误: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * 保存组合产品项
     */
    private void saveComboItems(ErpComboProductDO combo, String itemsString, Map<String, ErpProductDO> productMap) {
        if (StrUtil.isBlank(itemsString)) {
            return;
        }

        List<ComboItem> items = parseItemsString(itemsString);
        if (CollUtil.isEmpty(items)) {
            return;
        }

        for (ComboItem item : items) {
            ErpProductDO product = productMap.get(item.getNo());
            if (product != null) {
                ErpComboProductItemDO itemDO = new ErpComboProductItemDO();
                itemDO.setComboProductId(combo.getId());
                itemDO.setItemProductId(product.getId());
                itemDO.setItemQuantity(item.getItemQuantity());

                erpComboProductItemMapper.insert(itemDO);
                // 同步项到ES
                syncItemToES(itemDO.getId());
            }
        }
    }

    /**
     * 校验组合产品名称是否唯一（导入专用）
     */
    private void validateComboNameUniqueForImport(String name, Long excludeId,
                                                  List<ErpComboProductDO> createList, List<ErpComboProductDO> updateList) {
        if (StrUtil.isEmpty(name)) {
            return;
        }

        // 检查当前批次中是否有重复名称
        boolean duplicateInBatch = false;

        // 检查创建列表中的重复
        for (ErpComboProductDO combo : createList) {
            if (name.equals(combo.getName())) {
                duplicateInBatch = true;
                break;
            }
        }

        // 检查更新列表中的重复
        if (!duplicateInBatch) {
            for (ErpComboProductDO combo : updateList) {
                if (name.equals(combo.getName()) && !combo.getId().equals(excludeId)) {
                    duplicateInBatch = true;
                    break;
                }
            }
        }

        if (duplicateInBatch) {
            throw exception(COMBO_PRODUCT_NAME_DUPLICATE, name);
        }

        // 检查数据库中的重复
        validateComboNameUnique(name, excludeId);
    }

    /**
     * 解析itemsString为ComboItem列表，并合并相同单品编号的数量
     */
    private List<ComboItem> parseItemsString(String itemsString) {
        if (StrUtil.isBlank(itemsString)) {
            return Collections.emptyList();
        }

        // 先解析所有项目
        List<ComboItem> rawItems = Arrays.stream(itemsString.split(";"))
                .map(item -> {
                    String[] parts = item.split(",");
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("单品列表格式不正确，应为'产品编号,数量'格式");
                    }
                    return new ComboItem(parts[0], Integer.parseInt(parts[1]));
                })
                .collect(Collectors.toList());

        // 合并相同单品编号的数量
        Map<String, Integer> mergedItems = new HashMap<>();
        for (ComboItem item : rawItems) {
            String no = item.getNo();
            Integer existingQuantity = mergedItems.get(no);
            if (existingQuantity != null) {
                // 如果已存在相同编号，数量相加
                mergedItems.put(no, existingQuantity + item.getItemQuantity());
                System.out.println("合并相同单品编号: " + no + ", 原数量: " + existingQuantity +
                        ", 新增数量: " + item.getItemQuantity() + ", 合并后: " + (existingQuantity + item.getItemQuantity()));
            } else {
                // 如果不存在，直接添加
                mergedItems.put(no, item.getItemQuantity());
            }
        }

        // 转换回ComboItem列表
        List<ComboItem> result = mergedItems.entrySet().stream()
                .map(entry -> new ComboItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // 输出合并结果
        if (rawItems.size() != result.size()) {
            System.out.println("itemsString合并结果: 原始项目数=" + rawItems.size() + ", 合并后项目数=" + result.size());
            System.out.println("原始数据: " + itemsString);
            System.out.println("合并后数据: " + result.stream()
                    .map(item -> item.getNo() + "," + item.getItemQuantity())
                    .collect(Collectors.joining(";")));
        }

        return result;
    }

    /**
     * 计算并设置组合产品的采购总价、批发总价和总重量
     */
    private void calculateAndSetPricesAndWeight(ErpComboImportExcelVO importVO, ErpComboProductDO comboProduct, Map<String, ErpProductDO> productMap) {
        List<ComboItem> items = parseItemsString(importVO.getItemsString());
        if (CollUtil.isEmpty(items)) {
            return;
        }

        BigDecimal totalPurchasePrice = BigDecimal.ZERO;
        BigDecimal totalWholesalePrice = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        int totalQuantity = 0;
        StringBuilder nameBuilder = new StringBuilder();

        for (int i = 0; i < items.size(); i++) {
            ComboItem item = items.get(i);
            ErpProductDO product = productMap.get(item.getNo());
            if (product == null) {
                throw exception(COMBO_PRODUCT_IMPORT_ITEM_NO_EXISTS, item.getNo());
            }

            BigDecimal quantity = new BigDecimal(item.getItemQuantity());

            // 构建名称字符串
            if (i > 0) {
                nameBuilder.append("｜");
            }
            nameBuilder.append(product.getName())
                    .append("×")
                    .append(item.getItemQuantity());

            // 计算采购总价
            if (product.getPurchasePrice() != null) {
                totalPurchasePrice = totalPurchasePrice.add(
                        product.getPurchasePrice().multiply(quantity)
                );
            }

            // 计算批发总价
            if (product.getWholesalePrice() != null) {
                totalWholesalePrice = totalWholesalePrice.add(
                        product.getWholesalePrice().multiply(quantity)
                );
            }

            // 计算总重量
            if (product.getWeight() != null) {
                totalWeight = totalWeight.add(
                        product.getWeight().multiply(quantity)
                );
            }

            totalQuantity += item.getItemQuantity();
        }

        // 设置计算结果
        comboProduct.setName(nameBuilder.toString());
        comboProduct.setPurchasePrice(totalPurchasePrice);
        comboProduct.setWholesalePrice(totalWholesalePrice);
        comboProduct.setWeight(totalWeight);
        comboProduct.setTotalQuantity(totalQuantity);
    }

    /**
     * 计算组合产品名称
     */
    private String calculateComboName(String itemsString, Map<String, ErpProductDO> productMap) {
        if (StrUtil.isBlank(itemsString)) {
            return "";
        }

        List<ComboItem> items = parseItemsString(itemsString);
        if (CollUtil.isEmpty(items)) {
            return "";
        }

        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            ComboItem item = items.get(i);
            ErpProductDO product = productMap.get(item.getNo());
            if (product == null) {
                continue;
            }

            if (i > 0) {
                nameBuilder.append("｜");
            }
            nameBuilder.append(product.getName())
                    .append("×")
                    .append(item.getItemQuantity());
        }

        return nameBuilder.toString();
    }

    @Data
    @AllArgsConstructor
    private static class ComboItem {
        private String no;
        private Integer itemQuantity;
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
     * 构建组合产品名称
     */
    private String buildComboName(Long comboId) {
        // 查询组合产品关联的单品项
        List<ErpComboProductItemDO> comboItems = erpComboProductItemMapper.selectByComboProductId(comboId);
        if (CollUtil.isEmpty(comboItems)) {
            return "";
        }

        // 提取单品ID列表
        List<Long> productIds = comboItems.stream()
                .map(ErpComboProductItemDO::getItemProductId)
                .collect(Collectors.toList());

        // 查询单品详细信息
        List<ErpProductDO> products = erpProductMapper.selectBatchIds(productIds);
        if (CollUtil.isEmpty(products)) {
            return "";
        }

        // 构建名称字符串
        StringBuilder nameBuilder = new StringBuilder();
        Map<Long, ErpProductDO> productMap = products.stream()
                .collect(Collectors.toMap(ErpProductDO::getId, p -> p));

        for (int i = 0; i < comboItems.size(); i++) {
            ErpComboProductItemDO item = comboItems.get(i);
            ErpProductDO product = productMap.get(item.getItemProductId());
            if (product == null) {
                continue;
            }

            if (i > 0) {
                nameBuilder.append("｜");
            }
            nameBuilder.append(product.getName())
                    .append("×")
                    .append(item.getItemQuantity());
        }

        return nameBuilder.toString();
    }

    /**
     * 从数据库计算组合产品的价格和重量（ES计算失败时的回退方案）
     */
    private void calculatePricesAndWeightFromDB(Long comboId, ErpComboProductES es) {
        try {
            // 查询组品关联的单品项
            List<ErpComboProductItemDO> comboItems = erpComboProductItemMapper.selectByComboProductId(comboId);
            if (CollUtil.isNotEmpty(comboItems)) {
                // 提取单品ID列表
                List<Long> productIds = comboItems.stream()
                        .map(ErpComboProductItemDO::getItemProductId)
                        .collect(Collectors.toList());

                // 查询单品详细信息
                List<ErpProductDO> products = erpProductMapper.selectBatchIds(productIds);
                if (CollUtil.isNotEmpty(products)) {
                    // 创建单品ID到单品对象的映射
                    Map<Long, ErpProductDO> productMap = products.stream()
                            .collect(Collectors.toMap(ErpProductDO::getId, p -> p));

                    // 实时计算采购总价、批发总价、总重量
                    BigDecimal totalPurchasePrice = BigDecimal.ZERO;
                    BigDecimal totalWholesalePrice = BigDecimal.ZERO;
                    BigDecimal totalWeight = BigDecimal.ZERO;

                    for (ErpComboProductItemDO item : comboItems) {
                        ErpProductDO product = productMap.get(item.getItemProductId());
                        if (product != null) {
                            BigDecimal itemQuantity = new BigDecimal(item.getItemQuantity());

                            // 计算采购总价
                            if (product.getPurchasePrice() != null) {
                                totalPurchasePrice = totalPurchasePrice.add(
                                        product.getPurchasePrice().multiply(itemQuantity)
                                );
                            }

                            // 计算批发总价
                            if (product.getWholesalePrice() != null) {
                                totalWholesalePrice = totalWholesalePrice.add(
                                        product.getWholesalePrice().multiply(itemQuantity)
                                );
                            }

                            // 计算总重量
                            if (product.getWeight() != null) {
                                totalWeight = totalWeight.add(
                                        product.getWeight().multiply(itemQuantity)
                                );
                            }
                        }
                    }

                    // 使用实时计算的价格和重量覆盖数据库中的值
                    es.setPurchasePrice(totalPurchasePrice);
                    es.setWholesalePrice(totalWholesalePrice);
                    es.setWeight(totalWeight);
                }
            }
        } catch (Exception e) {
            System.err.println("数据库计算组品价格和重量也失败，ID: " + comboId + ", 错误: " + e.getMessage());
            // 如果数据库计算也失败，保留ES中的原值
        }
    }

    /**
     * 参考批发表/代发表的三层搜索策略
     */
    private BoolQueryBuilder createSimplifiedKeywordMatchQuery(String keywordFieldName, String keyword) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
        // 第一优先级：完全精确匹配
        multiMatchQuery.should(QueryBuilders.termQuery(keywordFieldName, keyword).boost(1000000.0f));
        // 第二优先级：前缀匹配
        multiMatchQuery.should(QueryBuilders.prefixQuery(keywordFieldName, keyword).boost(100000.0f));
        // 第三优先级：通配符包含匹配
        multiMatchQuery.should(QueryBuilders.wildcardQuery(keywordFieldName, "*" + keyword + "*").boost(10000.0f));
        multiMatchQuery.minimumShouldMatch(1);
        query.must(multiMatchQuery);
        return query;
    }
}

