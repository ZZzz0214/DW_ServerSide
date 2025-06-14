package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.*;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceESDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSalePriceMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
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
import java.util.*;
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

    // 转换方法
    private ErpSalePriceESDO convertToES(ErpSalePriceDO salePrice) {
        ErpSalePriceESDO es = new ErpSalePriceESDO();
        BeanUtils.copyProperties(salePrice, es);
        
        // 如果是从数据库查询的完整对象，确保ID被正确设置
        if (salePrice.getId() != null) {
            es.setId(salePrice.getId());
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
            // 1. 检查数据库和ES索引状态
            long dbCount = erpSalePriceMapper.selectCount(null);
            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpSalePriceESDO.class);
            boolean indexExists = indexOps.exists();
            long esCount = indexExists ? elasticsearchRestTemplate.count(new NativeSearchQueryBuilder().build(), ErpSalePriceESDO.class) : 0;

            // 2. 处理数据不一致情况
            if (dbCount == 0) {
                if (indexExists && esCount > 0) {
                    salePriceESRepository.deleteAll();
                }
                return new PageResult<>(Collections.emptyList(), 0L);
            }
            
            if (!indexExists || Math.abs(dbCount - esCount) > 100) { // 允许少量差异
                initESIndex();
                fullSyncToES();
                // 如果ES数据为空，先从数据库查询
                if (esCount == 0) {
                    return getSalePriceVOPageFromDB(pageReqVO);
                }
            }

            // 3. 使用优化的ES查询
            return getSalePriceVOPageFromES(pageReqVO);
            
        } catch (Exception e) {
            System.err.println("ES查询失败，回退到数据库查询: " + e.getMessage());
            return getSalePriceVOPageFromDB(pageReqVO);
        }
    }

    /**
     * 优化的ES分页查询
     */
    private PageResult<ErpSalePriceRespVO> getSalePriceVOPageFromES(ErpSalePricePageReqVO pageReqVO) {
        // 验证分页参数
        if (pageReqVO.getPageSize() == null || pageReqVO.getPageSize() <= 0) {
            pageReqVO.setPageSize(10); // 设置默认页大小
        }
        if (pageReqVO.getPageNo() == null || pageReqVO.getPageNo() <= 0) {
            pageReqVO.setPageNo(1); // 设置默认页码
        }
        
        // 构建ES查询
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        
        // 添加查询条件
            if (StringUtils.isNotBlank(pageReqVO.getCustomerName())) {
                boolQuery.must(QueryBuilders.matchQuery("customerName", pageReqVO.getCustomerName()));
            }
            if (pageReqVO.getGroupProductId() != null) {
                boolQuery.must(QueryBuilders.termQuery("groupProductId", pageReqVO.getGroupProductId()));
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
                .withSort(Sort.by(Sort.Direction.DESC, "id"));

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
        PageResult<ErpSalePriceDO> pageResult = erpSalePriceMapper.selectPage(pageReqVO);
        List<ErpSalePriceRespVO> voList = convertDOToVO(pageResult.getList());
        return new PageResult<>(voList, pageResult.getTotal());
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
                    ErpSalePriceRespVO vo = BeanUtils.toBean(esDO, ErpSalePriceRespVO.class);
                    if (esDO.getGroupProductId() != null) {
            ErpComboRespVO comboRespVO = getComboRespVOFromCache(esDO.getGroupProductId());
                        if (comboRespVO != null) {
                            vo.setComboList(Collections.singletonList(comboRespVO));
                            vo.setGroupProductId(comboRespVO.getId());
                            vo.setGroupProductNo(comboRespVO.getNo());
                // 从组品信息中获取产品名称和产品简称
                vo.setProductName(comboRespVO.getName());
                vo.setProductShortName(comboRespVO.getShortName());
                        }
                    }
                    return vo;
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
                // 从组品信息中获取产品名称和产品简称
                vo.setProductName(comboRespVO.getName());
                vo.setProductShortName(comboRespVO.getShortName());
            }
        }
        return vo;
    }

    /**
     * 从缓存获取组品信息
     */
    private ErpComboRespVO getComboRespVOFromCache(Long groupProductId) {
        return comboRespVOCache.computeIfAbsent(groupProductId, id -> {
            try {
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
        
    // 根据 groupProductId 查询组合产品信息
    if (salePrice.getGroupProductId() != null) {
            ErpComboRespVO comboRespVO = getComboRespVOFromCache(salePrice.getGroupProductId());
        if (comboRespVO != null) {
            respVO.setComboList(Collections.singletonList(comboRespVO));
            respVO.setGroupProductId(comboRespVO.getId());
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
                boolQuery.must(QueryBuilders.termQuery("groupProductId", searchReqVO.getGroupProductId()));
            }
            if (StrUtil.isNotBlank(searchReqVO.getCustomerName())) {
                boolQuery.must(QueryBuilders.termQuery("customerName", searchReqVO.getCustomerName()));
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
            return convertESToVO(searchHits);
            
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

        // 1. 批量预加载组品信息到缓存
        Set<String> groupProductNos = importList.stream()
                .map(ErpSalePriceImportExcelVO::getGroupProductNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        
        Map<String, ErpComboProductDO> groupProductMap = preloadComboProducts(groupProductNos);

        // 2. 批量查询已存在的销售价格记录
        Set<String> noSet = importList.stream()
                .map(ErpSalePriceImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, ErpSalePriceDO> existingSalePriceMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(noSet)) {
        List<ErpSalePriceDO> existList = erpSalePriceMapper.selectListByNoIn(noSet);
            existingSalePriceMap = convertMap(existList, ErpSalePriceDO::getNo);
        }

        // 2.1 批量查询所有客户名称，验证客户是否存在
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

        // 2.2 批量查询已存在的客户名称+组品ID组合
        List<ErpSalePriceDO> allExistingPrices = erpSalePriceMapper.selectList(null);
        Map<String, Set<Long>> customerProductMap = new HashMap<>();
        for (ErpSalePriceDO price : allExistingPrices) {
            customerProductMap.computeIfAbsent(price.getCustomerName(), k -> new HashSet<>())
                    .add(price.getGroupProductId());
        }

        // 3. 批量处理数据
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

                // 校验客户是否存在
                if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                    Boolean customerExists = customerExistsMap.get(importVO.getCustomerName());
                    if (customerExists == null || !customerExists) {
                        throw exception(SALE_PRICE_CUSTOMER_NOT_EXISTS, "客户不存在: " + importVO.getCustomerName());
                    }
                }

                // 判断是否支持更新
                ErpSalePriceDO existSalePrice = existingSalePriceMap.get(importVO.getNo());

                if (existSalePrice == null) {
                    // 创建新记录时，校验组品编号+客户名称的唯一性
                    validateCustomerProductUniqueWithCache(importVO.getCustomerName(), comboProduct.getId(), customerProductMap, null);
                    
                    // 创建新记录
                    ErpSalePriceDO salePrice = BeanUtils.toBean(importVO, ErpSalePriceDO.class)
                            .setGroupProductId(comboProduct.getId());

                    if (StrUtil.isEmpty(salePrice.getNo())) {
                        String newNo = noRedisDAO.generate(ErpNoRedisDAO.SALE_PRICE_NO_PREFIX);
                        salePrice.setNo(newNo);
                    }

                    // 设置组品相关信息（始终使用组品的数据）
                    salePrice.setProductName(comboProduct.getName());
                    salePrice.setProductShortName(comboProduct.getShortName());
                    salePrice.setProductImage(comboProduct.getImage());

                    toCreateList.add(salePrice);
                    respVO.getCreateNames().add(salePrice.getNo());
                    
                    // 更新缓存，确保同一批次导入的数据也能被正确校验
                    customerProductMap.computeIfAbsent(importVO.getCustomerName(), k -> new HashSet<>())
                            .add(comboProduct.getId());
                    
                } else if (isUpdateSupport) {
                    // 更新记录 - 只更新导入文件中提供的字段
                    ErpSalePriceDO updateSalePrice = new ErpSalePriceDO();
                    updateSalePrice.setId(existSalePrice.getId());
                    updateSalePrice.setGroupProductId(comboProduct.getId());
                    
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

        // 4. 批量执行数据库操作
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

            // 5. 批量同步到ES（同步执行，确保导入后立即可搜索）
            if (CollUtil.isNotEmpty(processedIds)) {
                batchSyncToESWithFullData(processedIds);
            }

        } catch (Exception e) {
            System.err.println("批量操作数据库失败: " + e.getMessage());
            throw new RuntimeException("批量导入失败: " + e.getMessage(), e);
        }

        System.out.println("导入完成，成功创建：" + respVO.getCreateNames().size() +
                          "，成功更新：" + respVO.getUpdateNames().size() +
                          "，失败：" + respVO.getFailureNames().size());
        return respVO;
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
        fullSyncToES();
    }
}
