package cn.iocoder.yudao.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.ErpComboImport.ErpComboImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.ErpComboImport.ErpComboImportRespVO;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
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
        //long startTime = System.currentTimeMillis();

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
            System.out.println("组品更新后强制刷新ES索引成功，组品ID: " + updateReqVO.getId());
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
        //System.out.println("1");

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
                    System.out.println("检测到数据库为空但组合产品ES有数据，已清空组合产品ES索引");
                }
                if (itemIndexExists && itemEsCount > 0) {
                    // 数据库为空但组合产品关联项ES有数据，清空ES
                    comboProductItemESRepository.deleteAll();
                    System.out.println("检测到数据库为空但组合产品关联项ES有数据，已清空组合产品关联项ES索引");
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
                System.out.println("检测到组品导出操作，查询所有数据（最多10000条）");
            } else {
                // 正常分页查询
                queryBuilder.withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()));
            }

        // 添加查询条件
            if (StringUtils.isNotBlank(pageReqVO.getKeyword())) {
                // 全文搜索（优先级最高）
                BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
                String keyword = pageReqVO.getKeyword().trim();

                System.out.println("=== 组品全文搜索调试信息 ===");
                System.out.println("搜索关键词: " + keyword);
                System.out.println("关键词长度: " + keyword.length());

                // 1. 精确词匹配（完全匹配优先）
                keywordQuery.should(QueryBuilders.termQuery("name_keyword", keyword).boost(1000000.0f));

                // 2. 前缀匹配
                keywordQuery.should(QueryBuilders.prefixQuery("name_keyword", keyword).boost(100000.0f));

                // 3. 第三优先级：通配符包含匹配（支持中间字符搜索，关键修复）
                keywordQuery.should(QueryBuilders.wildcardQuery("name_keyword", "*" + keyword + "*").boost(10000.0f));

                // 4. 第四优先级：对于多字搜索，添加子字符串通配符匹配（支持"色口红"匹配"变色口红"）
                if (keyword.length() >= 2) {
                    // 添加从第二个字符开始的子字符串匹配，如"色口红"可以匹配"变色口红"
                    for (int i = 1; i < keyword.length(); i++) {
                        String substring = keyword.substring(i);
                        if (substring.length() >= 2) { // 至少2个字符才有意义
                            keywordQuery.should(QueryBuilders.wildcardQuery("name_keyword", "*" + substring + "*").boost(3000.0f));
                        }
                    }
                }

                // 5. 智能分词匹配 - 根据关键词长度调整策略
                keywordQuery.should(createIntelligentMatchQuery("name", keyword, 800.0f, 600.0f, 500.0f));

                // 6. 其他字段精确匹配
                keywordQuery.should(QueryBuilders.matchPhraseQuery("short_name", keyword).boost(5.0f));
                keywordQuery.should(QueryBuilders.matchPhraseQuery("no", keyword).boost(4.0f));
                keywordQuery.should(QueryBuilders.matchPhraseQuery("shipping_code", keyword).boost(4.0f));
                keywordQuery.should(QueryBuilders.matchPhraseQuery("purchaser", keyword).boost(3.0f));
                keywordQuery.should(QueryBuilders.matchPhraseQuery("supplier", keyword).boost(3.0f));
                keywordQuery.should(QueryBuilders.matchPhraseQuery("creator", keyword).boost(2.5f));

                // 7. 其他字段智能分词匹配
                keywordQuery.should(createIntelligentMatchQuery("short_name", keyword, 2.0f, 1.8f, 1.5f));
                keywordQuery.should(createIntelligentMatchQuery("remark", keyword, 1.0f, 0.8f, 0.5f));
                keywordQuery.minimumShouldMatch(1);

                queryBuilder.withQuery(keywordQuery);
                System.out.println("全文搜索ES查询语句: " + keywordQuery.toString());
            } else {
                // 分字段查询
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

                // 组品编码查询 - 完全使用产品表的简化搜索策略
                if (StringUtils.isNotBlank(pageReqVO.getNo())) {
                    BoolQueryBuilder noQuery = QueryBuilders.boolQuery();
                    String no = pageReqVO.getNo().trim();

                    // 添加调试信息
                    System.out.println("=== 组品编号搜索调试 ===");
                    System.out.println("查询关键词: '" + no + "', 长度: " + no.length());

                    BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();

                    // 🔥 简化的编号匹配策略：只保留核心匹配逻辑
                    // 由于no字段现在是keyword类型，不会分词，可以大幅简化匹配策略

                    System.out.println("使用简化的编号匹配策略，查询词长度: " + no.length());

                    // 第一优先级：完全精确匹配（最高权重）
                    multiMatchQuery.should(QueryBuilders.termQuery("no_keyword", no).boost(1000000.0f));
                    System.out.println("添加精确匹配: no_keyword = '" + no + "', 权重: 1000000");

                    // 第二优先级：前缀匹配（支持"CPXX2025"匹配"CPXX2025..."）
                    multiMatchQuery.should(QueryBuilders.prefixQuery("no_keyword", no).boost(100000.0f));
                    System.out.println("添加前缀匹配: no_keyword 前缀 = '" + no + "', 权重: 100000");

                    // 第三优先级：包含匹配（支持任意位置的模糊匹配）
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("no_keyword", "*" + no + "*").boost(50000.0f));
                    System.out.println("添加包含匹配: *" + no + "*, 权重: 50000");

                    // 注意：移除复杂的智能子字符串匹配，因为keyword字段已经足够支持模糊匹配

                    multiMatchQuery.minimumShouldMatch(1);
                    noQuery.must(multiMatchQuery);
                    boolQuery.must(noQuery);

                    System.out.println("=== 组品编号搜索调试结束 ===");
                }

                // 产品名称查询 - 智能匹配策略
        if (StringUtils.isNotBlank(pageReqVO.getName())) {
                    BoolQueryBuilder nameQuery = QueryBuilders.boolQuery();
                    String name = pageReqVO.getName().trim();

                    System.out.println("=== 组品产品名称搜索调试信息 ===");
                    System.out.println("搜索产品名称: " + name);
                    System.out.println("名称长度: " + name.length());

                    // 先查看ES中所有数据，用于调试
                    try {
                        NativeSearchQuery debugQuery = new NativeSearchQueryBuilder()
                                .withQuery(QueryBuilders.matchAllQuery())
                                .withPageable(PageRequest.of(0, 10))
                                .build();
                        SearchHits<ErpComboProductES> debugHits = elasticsearchRestTemplate.search(
                                debugQuery,
                                ErpComboProductES.class,
                                IndexCoordinates.of("erp_combo_products"));

                        System.out.println("=== ES中的所有组品数据 ===");
                        debugHits.getSearchHits().forEach(hit -> {
                            ErpComboProductES content = hit.getContent();
                            System.out.println("组品ID: " + content.getId());
                            System.out.println("name: " + content.getName());
                            System.out.println("name_keyword: " + content.getNameKeyword());
                            System.out.println("---");
                        });
                        System.out.println("========================");
                    } catch (Exception debugEx) {
                        System.err.println("调试查询失败: " + debugEx.getMessage());
                    }

                    BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                    multiMatchQuery.should(QueryBuilders.termQuery("name_keyword", name).boost(1000000.0f));
                    multiMatchQuery.should(QueryBuilders.prefixQuery("name_keyword", name).boost(100000.0f));
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("name_keyword", "*" + name + "*").boost(10000.0f));

                    // 第四优先级：对于多字搜索，添加子字符串通配符匹配（支持"色口红"匹配"变色口红"）
                    if (name.length() >= 2) {
                        // 添加从第二个字符开始的子字符串匹配，如"色口红"可以匹配"变色口红"
                        for (int i = 1; i < name.length(); i++) {
                            String substring = name.substring(i);
                            if (substring.length() >= 2) { // 至少2个字符才有意义
                                multiMatchQuery.should(QueryBuilders.wildcardQuery("name_keyword", "*" + substring + "*").boost(3000.0f));
                                System.out.println("添加子字符串通配符匹配: *" + substring + "*");
                            }
                        }
                    }

                    // 智能分词匹配
                    if (name.length() == 1) {
                        // 单字搜索
                        multiMatchQuery.should(QueryBuilders.matchQuery("name", name).operator(Operator.OR).boost(800.0f));
                        System.out.println("单字搜索查询: " + QueryBuilders.matchQuery("name", name).operator(Operator.OR).boost(800.0f));
                    } else if (name.length() == 2) {
                        // 双字搜索，使用AND匹配避免误匹配，但也添加OR匹配作为兜底
                        multiMatchQuery.should(QueryBuilders.matchQuery("name", name).operator(Operator.AND).boost(600.0f));
                        multiMatchQuery.should(QueryBuilders.matchPhraseQuery("name", name).boost(1200.0f));
                        // 添加OR匹配作为兜底，权重较低
                        multiMatchQuery.should(QueryBuilders.matchQuery("name", name).operator(Operator.OR).boost(400.0f));
                        System.out.println("双字搜索AND查询: " + QueryBuilders.matchQuery("name", name).operator(Operator.AND).boost(600.0f));
                        System.out.println("双字搜索短语查询: " + QueryBuilders.matchPhraseQuery("name", name).boost(1200.0f));
                        System.out.println("双字搜索OR兜底查询: " + QueryBuilders.matchQuery("name", name).operator(Operator.OR).boost(400.0f));
                    } else {
                        // 多字搜索
                        multiMatchQuery.should(QueryBuilders.matchQuery("name", name).operator(Operator.AND).boost(500.0f));
                        multiMatchQuery.should(QueryBuilders.matchPhraseQuery("name", name).boost(1000.0f));
                    }

                    multiMatchQuery.minimumShouldMatch(1);
                    nameQuery.must(multiMatchQuery);
                    boolQuery.must(nameQuery);

                    System.out.println("产品名称ES查询语句: " + multiMatchQuery.toString());
                }

                // 产品简称查询 - 智能匹配策略
                if (StringUtils.isNotBlank(pageReqVO.getShortName())) {
                    BoolQueryBuilder shortNameQuery = QueryBuilders.boolQuery();
                    String shortName = pageReqVO.getShortName().trim();

                    BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                    // 第一优先级：完全精确匹配
                    multiMatchQuery.should(QueryBuilders.termQuery("short_name_keyword", shortName).boost(1000000.0f));
                    // 第二优先级：前缀匹配
                    multiMatchQuery.should(QueryBuilders.prefixQuery("short_name_keyword", shortName).boost(100000.0f));
                    // 第三优先级：通配符包含匹配
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("short_name_keyword", "*" + shortName + "*").boost(10000.0f));

                    // 第四优先级：子字符串通配符匹配
                    if (shortName.length() >= 2) {
                        for (int i = 1; i < shortName.length(); i++) {
                            String substring = shortName.substring(i);
                            if (substring.length() >= 2) {
                                multiMatchQuery.should(QueryBuilders.wildcardQuery("short_name_keyword", "*" + substring + "*").boost(3000.0f));
                            }
                        }
                    }

                    // 智能分词匹配
                    if (shortName.length() == 1) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("short_name", shortName).operator(Operator.OR).boost(800.0f));
                    } else if (shortName.length() == 2) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("short_name", shortName).operator(Operator.AND).boost(600.0f));
                        multiMatchQuery.should(QueryBuilders.matchPhraseQuery("short_name", shortName).boost(1200.0f));
                        multiMatchQuery.should(QueryBuilders.matchQuery("short_name", shortName).operator(Operator.OR).boost(400.0f));
                    } else {
                        multiMatchQuery.should(QueryBuilders.matchQuery("short_name", shortName).operator(Operator.AND).boost(500.0f));
                        multiMatchQuery.should(QueryBuilders.matchPhraseQuery("short_name", shortName).boost(1000.0f));
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
                    // 第一优先级：完全精确匹配
                    multiMatchQuery.should(QueryBuilders.termQuery("shipping_code_keyword", code).boost(1000000.0f));
                    // 第二优先级：前缀匹配
                    multiMatchQuery.should(QueryBuilders.prefixQuery("shipping_code_keyword", code).boost(100000.0f));
                    // 第三优先级：通配符包含匹配
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("shipping_code_keyword", "*" + code + "*").boost(10000.0f));

                    // 第四优先级：子字符串通配符匹配
                    if (code.length() >= 2) {
                        for (int i = 1; i < code.length(); i++) {
                            String substring = code.substring(i);
                            if (substring.length() >= 2) {
                                multiMatchQuery.should(QueryBuilders.wildcardQuery("shipping_code_keyword", "*" + substring + "*").boost(3000.0f));
                            }
                        }
                    }

                    // 智能分词匹配
                    if (code.length() == 1) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("shipping_code", code).operator(Operator.OR).boost(800.0f));
                    } else if (code.length() == 2) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("shipping_code", code).operator(Operator.AND).boost(600.0f));
                        multiMatchQuery.should(QueryBuilders.matchPhraseQuery("shipping_code", code).boost(1200.0f));
                        multiMatchQuery.should(QueryBuilders.matchQuery("shipping_code", code).operator(Operator.OR).boost(400.0f));
                    } else {
                        multiMatchQuery.should(QueryBuilders.matchQuery("shipping_code", code).operator(Operator.AND).boost(500.0f));
                        multiMatchQuery.should(QueryBuilders.matchPhraseQuery("shipping_code", code).boost(1000.0f));
                    }

                    multiMatchQuery.minimumShouldMatch(1);
                    codeQuery.must(multiMatchQuery);
                    boolQuery.must(codeQuery);
                }

                // 采购人员查询 - 智能匹配策略
                if (StringUtils.isNotBlank(pageReqVO.getPurchaser())) {
                    BoolQueryBuilder purchaserQuery = QueryBuilders.boolQuery();
                    String purchaser = pageReqVO.getPurchaser().trim();

                    BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                    // 第一优先级：完全精确匹配
                    multiMatchQuery.should(QueryBuilders.termQuery("purchaser_keyword", purchaser).boost(1000000.0f));
                    // 第二优先级：前缀匹配
                    multiMatchQuery.should(QueryBuilders.prefixQuery("purchaser_keyword", purchaser).boost(100000.0f));
                    // 第三优先级：通配符包含匹配
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("purchaser_keyword", "*" + purchaser + "*").boost(10000.0f));

                    // 第四优先级：子字符串通配符匹配
                    if (purchaser.length() >= 2) {
                        for (int i = 1; i < purchaser.length(); i++) {
                            String substring = purchaser.substring(i);
                            if (substring.length() >= 2) {
                                multiMatchQuery.should(QueryBuilders.wildcardQuery("purchaser_keyword", "*" + substring + "*").boost(3000.0f));
                            }
                        }
                    }

                    // 智能分词匹配
                    if (purchaser.length() == 1) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("purchaser", purchaser).operator(Operator.OR).boost(800.0f));
                    } else if (purchaser.length() == 2) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("purchaser", purchaser).operator(Operator.AND).boost(600.0f));
                        multiMatchQuery.should(QueryBuilders.matchPhraseQuery("purchaser", purchaser).boost(1200.0f));
                        multiMatchQuery.should(QueryBuilders.matchQuery("purchaser", purchaser).operator(Operator.OR).boost(400.0f));
                    } else {
                        multiMatchQuery.should(QueryBuilders.matchQuery("purchaser", purchaser).operator(Operator.AND).boost(500.0f));
                        multiMatchQuery.should(QueryBuilders.matchPhraseQuery("purchaser", purchaser).boost(1000.0f));
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
                    // 第一优先级：完全精确匹配
                    multiMatchQuery.should(QueryBuilders.termQuery("supplier_keyword", supplier).boost(1000000.0f));
                    // 第二优先级：前缀匹配
                    multiMatchQuery.should(QueryBuilders.prefixQuery("supplier_keyword", supplier).boost(100000.0f));
                    // 第三优先级：通配符包含匹配
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("supplier_keyword", "*" + supplier + "*").boost(10000.0f));

                    // 第四优先级：子字符串通配符匹配
                    if (supplier.length() >= 2) {
                        for (int i = 1; i < supplier.length(); i++) {
                            String substring = supplier.substring(i);
                            if (substring.length() >= 2) {
                                multiMatchQuery.should(QueryBuilders.wildcardQuery("supplier_keyword", "*" + substring + "*").boost(3000.0f));
                            }
                        }
                    }

                    // 智能分词匹配
                    if (supplier.length() == 1) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("supplier", supplier).operator(Operator.OR).boost(800.0f));
                    } else if (supplier.length() == 2) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("supplier", supplier).operator(Operator.AND).boost(600.0f));
                        multiMatchQuery.should(QueryBuilders.matchPhraseQuery("supplier", supplier).boost(1200.0f));
                        multiMatchQuery.should(QueryBuilders.matchQuery("supplier", supplier).operator(Operator.OR).boost(400.0f));
                    } else {
                        multiMatchQuery.should(QueryBuilders.matchQuery("supplier", supplier).operator(Operator.AND).boost(500.0f));
                        multiMatchQuery.should(QueryBuilders.matchPhraseQuery("supplier", supplier).boost(1000.0f));
                    }

                    multiMatchQuery.minimumShouldMatch(1);
                    supplierQuery.must(multiMatchQuery);
                    boolQuery.must(supplierQuery);
                }

                // 创建人员查询 - 智能匹配策略
                if (StringUtils.isNotBlank(pageReqVO.getCreator())) {
                    BoolQueryBuilder creatorQuery = QueryBuilders.boolQuery();
                    String creator = pageReqVO.getCreator().trim();

                    BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                    // 第一优先级：完全精确匹配
                    multiMatchQuery.should(QueryBuilders.termQuery("creator_keyword", creator).boost(1000000.0f));
                    // 第二优先级：前缀匹配
                    multiMatchQuery.should(QueryBuilders.prefixQuery("creator_keyword", creator).boost(100000.0f));
                    // 第三优先级：通配符包含匹配
                    multiMatchQuery.should(QueryBuilders.wildcardQuery("creator_keyword", "*" + creator + "*").boost(10000.0f));

                    // 第四优先级：子字符串通配符匹配
                    if (creator.length() >= 2) {
                        for (int i = 1; i < creator.length(); i++) {
                            String substring = creator.substring(i);
                            if (substring.length() >= 2) {
                                multiMatchQuery.should(QueryBuilders.wildcardQuery("creator_keyword", "*" + substring + "*").boost(3000.0f));
                            }
                        }
                    }

                    // 智能分词匹配
                    if (creator.length() == 1) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("creator", creator).operator(Operator.OR).boost(800.0f));
                    } else if (creator.length() == 2) {
                        multiMatchQuery.should(QueryBuilders.matchQuery("creator", creator).operator(Operator.AND).boost(600.0f));
                        multiMatchQuery.should(QueryBuilders.matchPhraseQuery("creator", creator).boost(1200.0f));
                        multiMatchQuery.should(QueryBuilders.matchQuery("creator", creator).operator(Operator.OR).boost(400.0f));
                    } else {
                        multiMatchQuery.should(QueryBuilders.matchQuery("creator", creator).operator(Operator.AND).boost(500.0f));
                        multiMatchQuery.should(QueryBuilders.matchPhraseQuery("creator", creator).boost(1000.0f));
                    }

                    multiMatchQuery.minimumShouldMatch(1);
                    creatorQuery.must(multiMatchQuery);
                    boolQuery.must(creatorQuery);
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

                queryBuilder.withQuery(boolQuery);
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

            // 添加查询结果调试日志
            System.out.println("=== 组品ES查询结果 ===");
            System.out.println("总命中数: " + searchHits.getTotalHits());
            searchHits.getSearchHits().forEach(hit -> {
                ErpComboProductES content = hit.getContent();
                System.out.println("命中组品: ID=" + content.getId() +
                                 ", 组品名称=" + content.getName() +
                                 ", name_keyword=" + content.getNameKeyword() +
                                 ", 得分=" + hit.getScore());
            });
            System.out.println("================");

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

        SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                itemQuery,
                ErpComboProductItemES.class,
                IndexCoordinates.of("erp_combo_product_items"));

        // 按组合产品ID分组关联项
        Map<Long, List<ErpComboProductItemES>> itemsMap = itemHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.groupingBy(ErpComboProductItemES::getComboProductId));

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
                    List<ErpComboProductItemES> items = itemsMap.getOrDefault(combo.getId(), Collections.emptyList());
                    StringBuilder nameBuilder = new StringBuilder();
                    BigDecimal totalWeight = BigDecimal.ZERO;
                    for (int i = 0; i < items.size(); i++) {
                        ErpProductESDO product = productMap.get(items.get(i).getItemProductId());
                            if (product == null) continue;

                        if (i > 0) {
                            nameBuilder.append("+");
                        }
                        nameBuilder.append(product.getName())
                                .append("×")
                                .append(items.get(i).getItemQuantity());

                        if (product.getWeight() != null) {
                            BigDecimal quantity = new BigDecimal(items.get(i).getItemQuantity());
                            totalWeight = totalWeight.add(product.getWeight().multiply(quantity));
                        }
                        }

                    ErpComboRespVO vo = BeanUtils.toBean(combo, ErpComboRespVO.class);
                    vo.setName(nameBuilder.toString());
                    vo.setWeight(totalWeight);
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
        query.addSort(Sort.by(Sort.Direction.ASC, "id")); // 保持一致的排序方式

        // 如果是深度分页，使用search_after
        if (skip > 0) {
            // 先获取前skip条记录
            NativeSearchQuery prevQuery = new NativeSearchQueryBuilder()
                    .withQuery(queryBuilder.build().getQuery())
                    .withPageable(PageRequest.of(0, skip))
                    .withSort(Sort.by(Sort.Direction.ASC, "id"))
                    .build();

            SearchHits<ErpComboProductES> prevHits = elasticsearchRestTemplate.search(
                    prevQuery,
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
                .withPageable(PageRequest.of(0, pageReqVO.getPageSize()))
                .withTrackTotalHits(true)
                .build();

        SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                itemQuery,
                ErpComboProductItemES.class,
                IndexCoordinates.of("erp_combo_product_items"));

        // 按组合产品ID分组关联项
        Map<Long, List<ErpComboProductItemES>> itemsMap = itemHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.groupingBy(ErpComboProductItemES::getComboProductId));

        // 获取所有产品ID
        List<Long> productIds = itemHits.stream()
                .map(hit -> hit.getContent().getItemProductId())
                .distinct()
                .collect(Collectors.toList());

        // 从ES查询所有产品
        NativeSearchQuery productQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.idsQuery().addIds(productIds.stream().map(String::valueOf).toArray(String[]::new)))
                .withPageable(PageRequest.of(0, pageReqVO.getPageSize()))
                .withTrackTotalHits(true)
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
                    BigDecimal totalWeight = BigDecimal.ZERO;
                    for (int i = 0; i < items.size(); i++) {
                        ErpProductESDO product = productMap.get(items.get(i).getItemProductId());
                        if (product == null) continue;

                        if (i > 0) {
                            nameBuilder.append("+");
                        }
                        nameBuilder.append(product.getName())
                                .append("×")
                                .append(items.get(i).getItemQuantity());

                        if (product.getWeight() != null) {
                            BigDecimal quantity = new BigDecimal(items.get(i).getItemQuantity());
                            totalWeight = totalWeight.add(product.getWeight().multiply(quantity));
                        }
                    }

                    ErpComboRespVO vo = BeanUtils.toBean(combo, ErpComboRespVO.class);
                    vo.setName(nameBuilder.toString());
                    vo.setWeight(totalWeight);
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(voList, searchHits.getTotalHits());
    }

    // 添加数据库查询方法
    private PageResult<ErpComboRespVO> getComboVOPageFromDB(ErpComboPageReqVO pageReqVO) {
        PageResult<ErpComboProductDO> pageResult = erpComboMapper.selectPage(pageReqVO);
        return new PageResult<>(BeanUtils.toBean(pageResult.getList(), ErpComboRespVO.class), pageResult.getTotal());
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
                      .append("×")
                      .append(comboItems.get(i).getItemQuantity());
        }
        // 计算总重量 (单品weight*数量)
        BigDecimal totalWeight = BigDecimal.ZERO;
//        for (int i = 0; i < products.size(); i++) {
//            BigDecimal itemWeight = products.get(i).getWeight();
//            if (itemWeight != null) {
//                BigDecimal quantity = new BigDecimal(comboItems.get(i).getItemQuantity());
//                totalWeight = totalWeight.add(itemWeight.multiply(quantity));
//            }
//        }

        // 计算采购总价 (单品purchasePrice*数量)
        BigDecimal totalPurchasePrice = BigDecimal.ZERO;
        // 计算批发总价 (单品wholesalePrice*数量)
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
                System.out.println("删除ES组合产品ID: " + comboId);
        } else {
            ErpComboProductES es = convertComboToES(combo);
            comboProductESRepository.save(es);
                System.out.println("保存ES组合产品ID: " + es.getId() + ", 组合名称: " + es.getName());

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
        es.setNoKeyword(combo.getNo());
        es.setShortNameKeyword(combo.getShortName());
        es.setShippingCodeKeyword(combo.getShippingCode());
        es.setPurchaserKeyword(combo.getPurchaser());
        es.setSupplierKeyword(combo.getSupplier());
        es.setCreatorKeyword(combo.getCreator());

        // 添加调试信息
        System.out.println("转换组品到ES: ID=" + combo.getId() +
                         ", no='" + combo.getNo() + "'" +
                         ", no_keyword='" + es.getNoKeyword() + "'");

        System.out.println("=== 组品ES数据转换调试 ===");
        System.out.println("组品ID: " + combo.getId());
        System.out.println("数据库purchasePrice: " + combo.getPurchasePrice());

        // 🔥 关键修复：实时计算采购单价、批发单价、重量等
        try {
            // 查询组品关联的单品项
            List<ErpComboProductItemDO> comboItems = erpComboProductItemMapper.selectByComboProductId(combo.getId());
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

                    // 🔥 核心：使用实时计算的价格和重量覆盖数据库中的值
                    es.setPurchasePrice(totalPurchasePrice);
                    es.setWholesalePrice(totalWholesalePrice);
                    es.setWeight(totalWeight);

                    System.out.println("实时计算采购单价: " + totalPurchasePrice);
                    System.out.println("实时计算批发单价: " + totalWholesalePrice);
                    System.out.println("实时计算总重量: " + totalWeight);
                }
            }
        } catch (Exception e) {
            System.err.println("实时计算组品价格和重量失败，ID: " + combo.getId() + ", 错误: " + e.getMessage());
            // 如果计算失败，保留数据库中的原值
        }

        // 构建完整的组合名称
        try {
            String fullComboName = buildComboName(combo.getId());
            System.out.println("构建的完整组合名称: " + fullComboName);

            // 使用构建的完整组合名称作为name和name_keyword
            if (StrUtil.isNotBlank(fullComboName)) {
                es.setName(fullComboName);
                es.setNameKeyword(fullComboName);
                System.out.println("设置ES name: " + fullComboName);
                System.out.println("设置ES name_keyword: " + fullComboName);
            } else {
                // 如果构建失败，使用数据库中的name
                es.setName(combo.getName() != null ? combo.getName() : "");
                es.setNameKeyword(combo.getName() != null ? combo.getName() : "");
                System.out.println("构建失败，使用数据库name: " + combo.getName());
            }
        } catch (Exception e) {
            System.err.println("构建组合产品名称失败，ID: " + combo.getId() + ", 错误: " + e.getMessage());
            // 如果构建失败，使用原有的name字段
            es.setName(combo.getName() != null ? combo.getName() : "");
            es.setNameKeyword(combo.getName() != null ? combo.getName() : "");
        }

        System.out.println("最终ES name: " + es.getName());
        System.out.println("最终ES name_keyword: " + es.getNameKeyword());
        System.out.println("最终ES purchasePrice: " + es.getPurchasePrice());
        System.out.println("========================");

        return es;
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
                nameBuilder.append("+");
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
                System.out.println("删除ES关联项ID: " + itemId);
        } else {
            ErpComboProductItemES es = new ErpComboProductItemES();
            BeanUtils.copyProperties(item, es);
            comboProductItemESRepository.save(es);
                System.out.println("保存ES关联项ID: " + es.getId() + ", 组合产品ID: " + es.getComboProductId());

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

        // 2. 构建 ES 查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("name", name))
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
     * 提取组合产品名称中的单品名称和数量
     * @param name 组合产品名称
     * @return 单品名称和数量的映射
     */
    private Map<String, Integer> extractNameMap(String name) {
        Map<String, Integer> nameMap = new HashMap<>();
        String[] items = name.split("\\+");
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

        // 初始化返回结果
        ErpComboImportRespVO respVO = ErpComboImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 查询已存在的组合产品记录
        Set<String> noSet = importList.stream()
                .map(ErpComboImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        List<ErpComboProductDO> existList = erpComboMapper.selectListByNoIn(noSet);
        Map<String, ErpComboProductDO> noComboMap = convertMap(existList, ErpComboProductDO::getNo);

        // 遍历处理每个导入项
        for (int i = 0; i < importList.size(); i++) {
            ErpComboImportExcelVO importVO = importList.get(i);
            try {
                // 判断是否支持更新
                ErpComboProductDO existCombo = noComboMap.get(importVO.getNo());
                if (existCombo == null) {
                    // 创建
                    ErpComboProductDO comboProduct = BeanUtils.toBean(importVO, ErpComboProductDO.class);
                        comboProduct.setNo(noRedisDAO.generate(ErpNoRedisDAO.COMBO_PRODUCT_NO_PREFIX));


                    // 计算并设置总价和总重量
                    calculateAndSetPricesAndWeight(importVO, comboProduct);

                    erpComboMapper.insert(comboProduct);

                    // 保存组品项
                    saveComboItems(importVO, comboProduct.getId());

                    // 同步到ES
                    syncComboToES(comboProduct.getId());

                    respVO.getCreateNames().add(comboProduct.getNo());
                } else if (isUpdateSupport) {
                    // 更新
                    ErpComboProductDO updateCombo = BeanUtils.toBean(importVO, ErpComboProductDO.class);
                    updateCombo.setId(existCombo.getId());

                    // 计算并设置总价和总重量
                    calculateAndSetPricesAndWeight(importVO, updateCombo);

                    erpComboMapper.updateById(updateCombo);

                    // 先删除旧的组品项
                    List<ErpComboProductItemDO> oldItems = erpComboProductItemMapper.selectByComboProductId(existCombo.getId());
                    for (ErpComboProductItemDO oldItem : oldItems) {
                        erpComboProductItemMapper.deleteById(oldItem.getId());
                        comboProductItemESRepository.deleteById(oldItem.getId());
                    }

                    // 保存新的组品项
                    saveComboItems(importVO, existCombo.getId());

                    // 同步到ES
                    syncComboToES(existCombo.getId());

                    respVO.getUpdateNames().add(updateCombo.getNo());
                } else {
                    throw exception(COMBO_PRODUCT_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
                }
            } catch (ServiceException ex) {
                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知组合产品";
                respVO.getFailureNames().put(errorKey, ex.getMessage());
            } catch (Exception ex) {
                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知组合产品";
                respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
            }
        }

        return respVO;
    }

   /**
     * 解析itemsString为ComboItem列表
     */
    private List<ComboItem> parseItemsString(String itemsString) {
        if (StrUtil.isBlank(itemsString)) {
            return Collections.emptyList();
        }

        return Arrays.stream(itemsString.split(";"))
                .map(item -> {
                    String[] parts = item.split(",");
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("单品列表格式不正确，应为'产品编号,数量'格式");
                    }
                    return new ComboItem(parts[0], Integer.parseInt(parts[1]));
                })
                .collect(Collectors.toList());
    }

    /**
     * 计算并设置组合产品的采购总价、批发总价和总重量
     */
    private void calculateAndSetPricesAndWeight(ErpComboImportExcelVO importVO, ErpComboProductDO comboProduct) {
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
            ErpProductDO product = erpProductMapper.selectByNo(item.getNo());
            if (product == null) {
                throw exception(COMBO_PRODUCT_IMPORT_ITEM_NO_EXISTS, item.getNo());
            }

            BigDecimal quantity = new BigDecimal(item.getItemQuantity());

            // 构建名称字符串
            if (i > 0) {
                nameBuilder.append("+");
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
     * 保存组合产品项
     */
    private void saveComboItems(ErpComboImportExcelVO importVO, Long comboProductId) {
        List<ComboItem> items = parseItemsString(importVO.getItemsString());
        if (CollUtil.isEmpty(items)) {
            return;
        }

        for (ComboItem item : items) {
            // 根据单品编号查询单品ID
            ErpProductDO product = erpProductMapper.selectByNo(item.getNo());
            if (product == null) {
                throw exception(COMBO_PRODUCT_IMPORT_ITEM_NO_EXISTS, item.getNo());
            }

            // 保存组品项
            ErpComboProductItemDO itemDO = new ErpComboProductItemDO();
            itemDO.setComboProductId(comboProductId);
            itemDO.setItemProductId(product.getId());
            itemDO.setItemQuantity(item.getItemQuantity());
            erpComboProductItemMapper.insert(itemDO);

            // 同步到ES
            syncItemToES(itemDO.getId());
        }
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
            // 双字搜索，使用AND匹配避免误匹配，但也添加OR匹配作为兜底
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

    @Data
    @AllArgsConstructor
    private static class ComboItem {
        private String no;
        private Integer itemQuantity;
    }
}
