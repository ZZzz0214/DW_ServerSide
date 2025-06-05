package cn.iocoder.yudao.module.erp.service.distribution;



import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceESDO;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionPurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionSaleMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductESRepository;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalePriceESRepository;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpDistributionServiceImpl implements ErpDistributionService {

    @Resource
    private ErpDistributionMapper distributionMapper;

    @Resource
    private ErpDistributionPurchaseMapper purchaseMapper;

    @Resource
    private ErpDistributionSaleMapper saleMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Resource
    private ErpDistributionBaseESRepository distributionBaseESRepository;
    @Resource
    private ErpDistributionPurchaseESRepository distributionPurchaseESRepository;
    @Resource
    private ErpDistributionSaleESRepository distributionSaleESRepository;

    @Resource
    private ErpComboProductESRepository comboProductESRepository;

    @Resource
    private ErpSalePriceESRepository salePriceESRepository;

     // 初始化ES索引
     @EventListener(ApplicationReadyEvent.class)
     public void initESIndex() {
         System.out.println("开始初始化代发订单ES索引...");
         try {
             // 初始化基础表索引
             IndexOperations baseIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionBaseESDO.class);
             if (!baseIndexOps.exists()) {
                 baseIndexOps.create();
                 baseIndexOps.putMapping(baseIndexOps.createMapping(ErpDistributionBaseESDO.class));
                 System.out.println("代发基础表索引创建成功");
             }

             // 初始化采购表索引
             IndexOperations purchaseIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionPurchaseESDO.class);
             if (!purchaseIndexOps.exists()) {
                 purchaseIndexOps.create();
                 purchaseIndexOps.putMapping(purchaseIndexOps.createMapping(ErpDistributionPurchaseESDO.class));
                 System.out.println("代发采购表索引创建成功");
             }

             // 初始化销售表索引
             IndexOperations saleIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionSaleESDO.class);
             if (!saleIndexOps.exists()) {
                 saleIndexOps.create();
                 saleIndexOps.putMapping(saleIndexOps.createMapping(ErpDistributionSaleESDO.class));
                 System.out.println("代发销售表索引创建成功");
             }
         } catch (Exception e) {
             System.err.println("代发订单索引初始化失败: " + e.getMessage());
         }
     }

       // 同步基础表数据到ES
    private void syncBaseToES(Long baseId) {
        ErpDistributionBaseDO base = distributionMapper.selectById(baseId);
        if (base == null) {
            distributionBaseESRepository.deleteById(baseId);
        } else {
            ErpDistributionBaseESDO es = convertBaseToES(base);
            distributionBaseESRepository.save(es);

        }
    }

    // 同步采购表数据到ES
    private void syncPurchaseToES(Long purchaseId) {
        ErpDistributionPurchaseDO purchase = purchaseMapper.selectById(purchaseId);
        if (purchase == null) {
            distributionPurchaseESRepository.deleteByBaseId(purchaseId);
        } else {
            ErpDistributionPurchaseESDO es = convertPurchaseToES(purchase);
            distributionPurchaseESRepository.save(es);


        }
    }

    // 同步销售表数据到ES
    private void syncSaleToES(Long saleId) {
        ErpDistributionSaleDO sale = saleMapper.selectById(saleId);
        if (sale == null) {
            distributionSaleESRepository.deleteByBaseId(saleId);
        } else {
            ErpDistributionSaleESDO es = convertSaleToES(sale);
            distributionSaleESRepository.save(es);

        }
    }

    // 转换方法
    private ErpDistributionBaseESDO convertBaseToES(ErpDistributionBaseDO base) {
        ErpDistributionBaseESDO es = new ErpDistributionBaseESDO();
        BeanUtils.copyProperties(base, es);
        return es;
    }

    private ErpDistributionPurchaseESDO convertPurchaseToES(ErpDistributionPurchaseDO purchase) {
        ErpDistributionPurchaseESDO es = new ErpDistributionPurchaseESDO();
        BeanUtils.copyProperties(purchase, es);
        return es;
    }

    private ErpDistributionSaleESDO convertSaleToES(ErpDistributionSaleDO sale) {
        ErpDistributionSaleESDO es = new ErpDistributionSaleESDO();
        BeanUtils.copyProperties(sale, es);
        return es;
    }

    // 全量同步方法
    @Async
    public void fullSyncToES() {
        try {
            // 同步基础表
            List<ErpDistributionBaseDO> bases = distributionMapper.selectList(null);
            if (CollUtil.isNotEmpty(bases)) {
                List<ErpDistributionBaseESDO> baseESList = bases.stream()
                        .map(this::convertBaseToES)
                        .collect(Collectors.toList());
                distributionBaseESRepository.saveAll(baseESList);
            }

            // 同步采购表
            List<ErpDistributionPurchaseDO> purchases = purchaseMapper.selectList(null);
            if (CollUtil.isNotEmpty(purchases)) {
                List<ErpDistributionPurchaseESDO> purchaseESList = purchases.stream()
                        .map(this::convertPurchaseToES)
                        .collect(Collectors.toList());
                distributionPurchaseESRepository.saveAll(purchaseESList);
            }

            // 同步销售表
            List<ErpDistributionSaleDO> sales = saleMapper.selectList(null);
            if (CollUtil.isNotEmpty(sales)) {
                List<ErpDistributionSaleESDO> saleESList = sales.stream()
                        .map(this::convertSaleToES)
                        .collect(Collectors.toList());
                distributionSaleESRepository.saveAll(saleESList);
            }

            System.out.println("代发订单全量同步ES数据完成");
        } catch (Exception e) {
            System.err.println("代发订单全量同步ES数据失败: " + e.getMessage());
        }
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDistribution(ErpDistributionSaveReqVO createReqVO) {
        // 1. 校验数据
        validateDistributionForCreateOrUpdate(null, createReqVO);
       // System.out.println(createReqVO);

        // 2. 生成代发单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.DISTRIBUTION_NO_PREFIX);
        if (distributionMapper.selectByNo(no) != null) {
            throw exception(DISTRIBUTION_NO_EXISTS);
        }
        LocalDateTime AfterSalesTime = parseDateTime(createReqVO.getAfterSalesTime());

        // 3. 插入代发记录
        ErpDistributionBaseDO distribution = BeanUtils.toBean(createReqVO, ErpDistributionBaseDO.class)
                .setNo(no)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setAfterSalesTime(AfterSalesTime);
        distributionMapper.insert(distribution);

        // 4. 插入采购信息
        ErpDistributionPurchaseDO purchase = BeanUtils.toBean(createReqVO, ErpDistributionPurchaseDO.class)
                .setBaseId(distribution.getId())
                .setPurchaseAuditStatus(ErpAuditStatus.PROCESS.getStatus())
                .setPurchaseAfterSalesStatus(30);

        purchaseMapper.insert(purchase);

        // 5. 插入销售信息
        ErpDistributionSaleDO sale = BeanUtils.toBean(createReqVO, ErpDistributionSaleDO.class)
                .setBaseId(distribution.getId())
                .setSaleAuditStatus(ErpAuditStatus.PROCESS.getStatus())
                .setSaleAfterSalesStatus(30)
                .setShippingFee(createReqVO.getSaleShippingFee())
                .setOtherFees(createReqVO.getSaleOtherFees());
        saleMapper.insert(sale);

    // 同步到ES
    syncBaseToES(distribution.getId());
    // 修改为传入采购记录ID和销售记录ID
    syncPurchaseToES(purchase.getId());
    syncSaleToES(sale.getId());

        return distribution.getId();
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateDistribution(ErpDistributionSaveReqVO updateReqVO) {
//        // 1.1 校验存在
//        ErpDistributionBaseDO distribution = validateDistribution(updateReqVO.getId());
//        if (ErpAuditStatus.APPROVE.getStatus().equals(distribution.getStatus())) {
//            throw exception(DISTRIBUTION_UPDATE_FAIL_APPROVE, distribution.getNo());
//        }
//        // 1.2 校验数据
//        validateDistributionForCreateOrUpdate(updateReqVO.getId(), updateReqVO);
//
//        // 2. 更新代发记录
//        ErpDistributionBaseDO updateObj = BeanUtils.toBean(updateReqVO, ErpDistributionBaseDO.class);
//        distributionMapper.updateById(updateObj);
//
//        // 3. 更新采购信息
////        if (updateReqVO.getComboProductId() != null) {
//            ErpDistributionPurchaseDO purchase = BeanUtils.toBean(updateReqVO, ErpDistributionPurchaseDO.class)
//                    .setBaseId(updateReqVO.getId());
//                    purchaseMapper.update(purchase,
//                    new LambdaUpdateWrapper<ErpDistributionPurchaseDO>()
//                        .eq(ErpDistributionPurchaseDO::getBaseId, updateReqVO.getId()));
////        }
//
//        // 4. 更新销售信息
////        if (updateReqVO.getSalePriceId() !=null) {
//            ErpDistributionSaleDO sale = BeanUtils.toBean(updateReqVO, ErpDistributionSaleDO.class)
//                    .setBaseId(updateReqVO.getId())
//                    .setOtherFees(updateReqVO.getSaleOtherFees());
//                    saleMapper.update(sale,
//                    new LambdaUpdateWrapper<ErpDistributionSaleDO>()
//                        .eq(ErpDistributionSaleDO::getBaseId, updateReqVO.getId()));
////        }
//    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDistribution(ErpDistributionSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpDistributionBaseDO distribution = validateDistribution(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(distribution.getStatus())) {
            throw exception(DISTRIBUTION_UPDATE_FAIL_APPROVE, distribution.getNo());
        }
        // 1.2 校验数据
        validateDistributionForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新代发记录
        ErpDistributionBaseDO updateObj = BeanUtils.toBean(updateReqVO, ErpDistributionBaseDO.class);
        distributionMapper.updateById(updateObj);

        // 3. 更新采购信息（独立检查审核状态）
        ErpDistributionPurchaseDO purchase = purchaseMapper.selectByBaseId(updateReqVO.getId());
        if (purchase != null) {
            if (!ErpAuditStatus.APPROVE.getStatus().equals(purchase.getPurchaseAuditStatus())) {
                Long originalId = purchase.getId();
                purchase = BeanUtils.toBean(updateReqVO, ErpDistributionPurchaseDO.class)
                        .setBaseId(updateReqVO.getId())
                        .setId(originalId); // 恢复原始ID
                purchaseMapper.update(purchase,
                    new LambdaUpdateWrapper<ErpDistributionPurchaseDO>()
                        .eq(ErpDistributionPurchaseDO::getBaseId, updateReqVO.getId()));
            }
        }

        // 4. 更新销售信息（独立检查审核状态）
        ErpDistributionSaleDO sale = saleMapper.selectByBaseId(updateReqVO.getId());
        if (sale != null) {
            if (!ErpAuditStatus.APPROVE.getStatus().equals(sale.getSaleAuditStatus())) {
                Long originalId = sale.getId();
                sale = BeanUtils.toBean(updateReqVO, ErpDistributionSaleDO.class)
                        .setBaseId(updateReqVO.getId())
                        .setShippingFee(updateReqVO.getSaleShippingFee())
                        .setOtherFees(updateReqVO.getSaleOtherFees())
                        .setId(originalId); // 恢复原始ID
                saleMapper.update(sale,
                    new LambdaUpdateWrapper<ErpDistributionSaleDO>()
                        .eq(ErpDistributionSaleDO::getBaseId, updateReqVO.getId()));
            }
        }
        // 5. 同步到ES（在数据库更新完成后）
        syncBaseToES(updateReqVO.getId());
        if (purchase != null) {
            //System.out.println("传入到采购里面的是"+purchase.getId());
            syncPurchaseToES(purchase.getId());
        }
        if (sale != null) {
            syncSaleToES(sale.getId());
        }


    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDistribution(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpDistributionBaseDO> distributions = distributionMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(distributions)) {
            throw exception(DISTRIBUTION_NOT_EXISTS);
        }
        // 2. 删除代发记录
        distributionMapper.deleteBatchIds(ids);

        // 3. 删除采购信息
        purchaseMapper.deleteByBaseIds(ids);

        // 4. 删除销售信息
        saleMapper.deleteByBaseIds(ids);

        // 从ES删除
        ids.forEach(id -> {
            //System.out.println("删除id"+id);
            distributionBaseESRepository.deleteById(id);
            // 直接通过baseId删除采购和销售记录
            distributionPurchaseESRepository.deleteByBaseId(id);
            distributionSaleESRepository.deleteByBaseId(id);
        });
    }

    @Override
    public ErpDistributionBaseDO getDistribution(Long id) {
        return distributionMapper.selectById(id);
    }

    @Override
    public ErpDistributionBaseDO validateDistribution(Long id) {
        ErpDistributionBaseDO distribution = distributionMapper.selectById(id);
        if (distribution == null) {
            throw exception(DISTRIBUTION_NOT_EXISTS);
        }
//        if (ObjectUtil.notEqual(distribution.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
//            throw exception(DISTRIBUTION_NOT_APPROVE);
//        }
        return distribution;
    }

//    @Override
//    public PageResult<ErpDistributionRespVO> getDistributionVOPage(ErpDistributionPageReqVO pageReqVO) {
//        return distributionMapper.selectPage(pageReqVO);
//    }
    @Override
    public PageResult<ErpDistributionRespVO> getDistributionVOPage(ErpDistributionPageReqVO pageReqVO) {
        try {
            // 1. 检查数据库是否有数据
            long dbCount = distributionMapper.selectCount(null);
            if (dbCount == 0) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }

            // 2. 检查ES索引是否存在
            IndexOperations baseIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionBaseESDO.class);
            if (!baseIndexOps.exists()) {
                initESIndex(); // 如果索引不存在则创建
                fullSyncToES(); // 全量同步数据
                return getDistributionVOPageFromDB(pageReqVO); // 首次查询使用数据库
            }

            // 3. 检查ES是否有数据
            long esCount = elasticsearchRestTemplate.count(new NativeSearchQueryBuilder().build(), ErpDistributionBaseESDO.class);
            if (esCount == 0) {
                fullSyncToES(); // 同步数据到ES
                return getDistributionVOPageFromDB(pageReqVO); // 首次查询使用数据库
            }

            // 1. 构建基础查询条件
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()))
                    .withTrackTotalHits(true)
                    .withSort(Sort.by(Sort.Direction.DESC, "id")); // 按照 ID 降序排序

            // 添加查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            if (StringUtils.isNotBlank(pageReqVO.getNo())) {
                boolQuery.must(QueryBuilders.matchQuery("no", pageReqVO.getNo()));
            }
            if (pageReqVO.getStatus() != null) {
                boolQuery.must(QueryBuilders.termQuery("status", pageReqVO.getStatus()));
            }
            if (StringUtils.isNotBlank(pageReqVO.getLogisticsCompany())) {
                boolQuery.must(QueryBuilders.matchQuery("logisticsCompany", pageReqVO.getLogisticsCompany()));
            }
            if (StringUtils.isNotBlank(pageReqVO.getTrackingNumber())) {
                boolQuery.must(QueryBuilders.matchQuery("trackingNumber", pageReqVO.getTrackingNumber()));
            }
            if (StringUtils.isNotBlank(pageReqVO.getReceiverName())) {
                boolQuery.must(QueryBuilders.matchQuery("receiverName", pageReqVO.getReceiverName()));
            }
            if (pageReqVO.getCreateTime() != null && pageReqVO.getCreateTime().length == 2) {
                boolQuery.must(QueryBuilders.rangeQuery("createTime")
                        .gte(pageReqVO.getCreateTime()[0])
                        .lte(pageReqVO.getCreateTime()[1]));
            }
            if (pageReqVO.getPurchaseAuditStatus() != null) {
                boolQuery.must(QueryBuilders.termQuery("purchaseAuditStatus", pageReqVO.getPurchaseAuditStatus()));
            }
            if (pageReqVO.getSaleAuditStatus() != null) {
                boolQuery.must(QueryBuilders.termQuery("saleAuditStatus", pageReqVO.getSaleAuditStatus()));
            }

            queryBuilder.withQuery(boolQuery);

            // 2. 如果是深度分页(超过10000条)，使用search_after
            if (pageReqVO.getPageNo() > 1) {
                return handleDeepPagination(pageReqVO, queryBuilder);
            }

             // 3. 普通分页处理
             SearchHits<ErpDistributionBaseESDO> searchHits = elasticsearchRestTemplate.search(
                queryBuilder.build(),
                ErpDistributionBaseESDO.class,
                IndexCoordinates.of("erp_distribution_base"));

        List<ErpDistributionRespVO> voList = searchHits.stream()
                .map(SearchHit::getContent)
                .map(esDO -> {
                    ErpDistributionRespVO vo = BeanUtils.toBean(esDO, ErpDistributionRespVO.class);

                    // 从ES查询采购信息（通过baseId匹配）
                    Optional<ErpDistributionPurchaseESDO> purchaseOpt = pageReqVO.getPurchaseAuditStatus() != null
                    ? distributionPurchaseESRepository.findByBaseIdAndPurchaseAuditStatus(esDO.getId(), pageReqVO.getPurchaseAuditStatus())
                    : distributionPurchaseESRepository.findByBaseId(esDO.getId());
                    if (purchaseOpt.isPresent()) {
                        ErpDistributionPurchaseESDO purchase = purchaseOpt.get();
                        vo.setPurchaseAuditStatus(purchase.getPurchaseAuditStatus());
                        vo.setOtherFees(purchase.getOtherFees());
                        vo.setComboProductId(purchase.getComboProductId());
                        vo.setPurchaseAfterSalesSituation(purchase.getPurchaseAfterSalesSituation());
                        vo.setPurchaseAfterSalesStatus(purchase.getPurchaseAfterSalesStatus());
                        vo.setPurchaseAfterSalesAmount(purchase.getPurchaseAfterSalesAmount());

                        // 从ES查询组品信息
                        if (purchase.getComboProductId() != null) {
                            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(purchase.getComboProductId());
                            if (comboProductOpt.isPresent()) {
                                ErpComboProductES comboProduct = comboProductOpt.get();
                                vo.setProductName(comboProduct.getName());
                                vo.setShippingCode(comboProduct.getShippingCode());
                                vo.setPurchaser(comboProduct.getPurchaser());
                                vo.setSupplier(comboProduct.getSupplier());
                                vo.setPurchasePrice(comboProduct.getPurchasePrice());

                                // 计算运费和采购总额
                                calculatePurchaseAmount(vo, comboProduct, purchase);
                            }
                        }
                    }

                    // 从ES查询销售信息（通过baseId匹配）
                    Optional<ErpDistributionSaleESDO> saleOpt = pageReqVO.getSaleAuditStatus() != null
                    ? distributionSaleESRepository.findByBaseIdAndSaleAuditStatus(esDO.getId(), pageReqVO.getSaleAuditStatus())
                    : distributionSaleESRepository.findByBaseId(esDO.getId());
                    if (saleOpt.isPresent()) {
                        ErpDistributionSaleESDO sale = saleOpt.get();
                        vo.setSalesperson(sale.getSalesperson());
                        vo.setCustomerName(sale.getCustomerName());
                        vo.setSaleOtherFees(sale.getOtherFees());
                        vo.setTransferPerson(sale.getTransferPerson());
                        vo.setSaleAfterSalesStatus(sale.getSaleAfterSalesStatus());
                        vo.setSaleAfterSalesSituation(sale.getSaleAfterSalesSituation());
                        vo.setSaleAfterSalesAmount(sale.getSaleAfterSalesAmount());
                        vo.setSaleAfterSalesTime(sale.getSaleAfterSalesTime());
                        vo.setSaleAuditStatus(sale.getSaleAuditStatus());
                        vo.setShippingFee(sale.getShippingFee());

                        // 计算销售总额
                        if (purchaseOpt.isPresent() && sale.getCustomerName() != null) {
                            calculateSaleAmount(vo, purchaseOpt.get(), sale);
                        }
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(voList, searchHits.getTotalHits());

    } catch (Exception e) {
        System.out.println("ES查询失败，回退到数据库查询: " + e.getMessage());
        return getDistributionVOPageFromDB(pageReqVO);
    }
    }

    private PageResult<ErpDistributionRespVO> getDistributionVOPageFromDB(ErpDistributionPageReqVO pageReqVO) {
        return distributionMapper.selectPage(pageReqVO);
    }

    private PageResult<ErpDistributionRespVO> handleDeepPagination(ErpDistributionPageReqVO pageReqVO,
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

            SearchHits<ErpDistributionBaseESDO> prevHits = elasticsearchRestTemplate.search(
                    prevQuery,
                    ErpDistributionBaseESDO.class,
                    IndexCoordinates.of("erp_distribution_base"));

            if (prevHits.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), prevHits.getTotalHits());
            }

            // 设置search_after参数
            SearchHit<ErpDistributionBaseESDO> lastHit = prevHits.getSearchHits().get(0);
            query.setSearchAfter(lastHit.getSortValues());
        }

        // 3. 执行查询
        SearchHits<ErpDistributionBaseESDO> searchHits = elasticsearchRestTemplate.search(
                query,
                ErpDistributionBaseESDO.class,
                IndexCoordinates.of("erp_distribution_base"));

               // 4. 转换为VO并补充关联数据
        List<ErpDistributionRespVO> voList = searchHits.stream()
        .map(SearchHit::getContent)
        .map(esDO -> {
            ErpDistributionRespVO vo = BeanUtils.toBean(esDO, ErpDistributionRespVO.class);

            // 从ES查询采购信息（通过baseId匹配）
            Optional<ErpDistributionPurchaseESDO> purchaseOpt = pageReqVO.getPurchaseAuditStatus() != null
        ? distributionPurchaseESRepository.findByBaseIdAndPurchaseAuditStatus(esDO.getId(), pageReqVO.getPurchaseAuditStatus())
        : distributionPurchaseESRepository.findByBaseId(esDO.getId());
            if (purchaseOpt.isPresent()) {
                ErpDistributionPurchaseESDO purchase = purchaseOpt.get();
                BeanUtils.copyProperties(purchase, vo);

                // 从ES查询组品信息
                if (purchase.getComboProductId() != null) {
                    Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(purchase.getComboProductId());
                    if (comboProductOpt.isPresent()) {
                        ErpComboProductES comboProduct = comboProductOpt.get();
                        vo.setProductName(comboProduct.getName());
                        vo.setShippingCode(comboProduct.getShippingCode());
                        vo.setPurchaser(comboProduct.getPurchaser());
                        vo.setSupplier(comboProduct.getSupplier());
                        vo.setPurchasePrice(comboProduct.getPurchasePrice());

                        // 计算运费和采购总额
                        calculatePurchaseAmount(vo, comboProduct, purchase);
                    }
                }
            }

            // 从ES查询销售信息（通过baseId匹配）
            Optional<ErpDistributionSaleESDO> saleOpt = pageReqVO.getSaleAuditStatus() != null
            ? distributionSaleESRepository.findByBaseIdAndSaleAuditStatus(esDO.getId(), pageReqVO.getSaleAuditStatus())
            : distributionSaleESRepository.findByBaseId(esDO.getId());
            if (saleOpt.isPresent()) {
                ErpDistributionSaleESDO sale = saleOpt.get();
                vo.setSalesperson(sale.getSalesperson());
                vo.setCustomerName(sale.getCustomerName());
                vo.setSaleOtherFees(sale.getOtherFees());
                vo.setTransferPerson(sale.getTransferPerson());
                vo.setSaleAfterSalesStatus(sale.getSaleAfterSalesStatus());
                vo.setSaleAfterSalesSituation(sale.getSaleAfterSalesSituation());
                vo.setSaleAfterSalesAmount(sale.getSaleAfterSalesAmount());
                vo.setSaleAfterSalesTime(sale.getSaleAfterSalesTime());
                vo.setSaleAuditStatus(sale.getSaleAuditStatus());
                vo.setShippingFee(sale.getShippingFee());

                // 计算销售总额
                if (purchaseOpt.isPresent() && sale.getCustomerName() != null) {
                    calculateSaleAmount(vo, purchaseOpt.get(), sale);
                }
            }

            return vo;
        })
        .collect(Collectors.toList());

        return new PageResult<>(voList, searchHits.getTotalHits());
    }


    // 计算采购总额
    private void calculatePurchaseAmount(ErpDistributionRespVO vo, ErpComboProductES comboProduct,
                                    ErpDistributionPurchaseESDO purchase) {
        BigDecimal shippingFee = BigDecimal.ZERO;
        switch (comboProduct.getShippingFeeType()) {
            case 0: // 固定运费
                shippingFee = comboProduct.getFixedShippingFee();
                break;
            case 1: // 按件计费
                if (comboProduct.getAdditionalItemQuantity() > 0) {
                    int quantity = (int) Math.ceil((double) vo.getProductQuantity() / comboProduct.getAdditionalItemQuantity());
                    shippingFee = comboProduct.getAdditionalItemPrice().multiply(new BigDecimal(quantity));
                }
                break;
            case 2: // 按重量计费
                BigDecimal totalWeight = comboProduct.getWeight().multiply(new BigDecimal(vo.getProductQuantity()));
                if (totalWeight.compareTo(comboProduct.getFirstWeight()) <= 0) {
                    shippingFee = comboProduct.getFirstWeightPrice();
                } else {
                    BigDecimal additionalWeight = totalWeight.subtract(comboProduct.getFirstWeight());
                    int quantity = (int) Math.ceil(additionalWeight.divide(comboProduct.getAdditionalWeight(), 2, RoundingMode.UP).doubleValue());
                    shippingFee = comboProduct.getFirstWeightPrice().add(
                        comboProduct.getAdditionalWeightPrice().multiply(new BigDecimal(quantity))
                    );
                }
                break;
        }

        BigDecimal otherFees = purchase.getOtherFees() != null ? purchase.getOtherFees() : BigDecimal.ZERO;
        BigDecimal totalPurchaseAmount = comboProduct.getPurchasePrice()
                .multiply(new BigDecimal(vo.getProductQuantity()))
                .add(shippingFee)
                .add(otherFees);

        vo.setShippingFee(shippingFee);
        vo.setTotalPurchaseAmount(totalPurchaseAmount);
    }

    // 计算销售总额
    private void calculateSaleAmount(ErpDistributionRespVO vo, ErpDistributionPurchaseESDO purchase,
                                ErpDistributionSaleESDO sale) {
        // 从ES查询销售价格
        Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                purchase.getComboProductId(), sale.getCustomerName());

        if (salePriceOpt.isPresent()) {
            ErpSalePriceESDO salePrice = salePriceOpt.get();
            BigDecimal salePriceValue = salePrice.getDistributionPrice();
            BigDecimal saleShippingFee = sale.getShippingFee() != null ? sale.getShippingFee() : BigDecimal.ZERO;
            BigDecimal saleOtherFees = sale.getOtherFees() != null ? sale.getOtherFees() : BigDecimal.ZERO;

            BigDecimal totalSaleAmount = salePriceValue
                    .multiply(new BigDecimal(vo.getProductQuantity()))
                    .add(saleShippingFee)
                    .add(saleOtherFees);

            vo.setSalePrice(salePriceValue);
            vo.setTotalSaleAmount(totalSaleAmount);
        }
    }




    @Override
    public List<ErpDistributionRespVO> getDistributionVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpDistributionBaseDO> list = distributionMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpDistributionRespVO.class);
    }

    @Override
    public Map<Long, ErpDistributionRespVO> getDistributionVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getDistributionVOList(ids), ErpDistributionRespVO::getId);
    }

    @Override
    public List<ErpDistributionBaseDO> getDistributionList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return distributionMapper.selectBatchIds(ids);
    }

    private void validateDistributionForCreateOrUpdate(Long id, ErpDistributionSaveReqVO reqVO) {
        // 1. 校验订单号唯一
        ErpDistributionBaseDO distribution = distributionMapper.selectByNo(reqVO.getNo());
        if (distribution != null && !distribution.getId().equals(id)) {
            throw exception(DISTRIBUTION_NO_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDistributionStatus(Long id, Integer status, BigDecimal otherFees) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpDistributionBaseDO distribution = validateDistribution(id);
        // 1.2 校验状态
        if (distribution.getStatus().equals(status)) {
            throw exception(approve ? DISTRIBUTION_APPROVE_FAIL : DISTRIBUTION_PROCESS_FAIL);
        }

        // 2. 更新状态
        int updateCount = distributionMapper.updateByIdAndStatus(id, distribution.getStatus(),
                new ErpDistributionBaseDO().setStatus(status));

        // 3. 更新采购信息的其他费用
        if (otherFees != null) {
            ErpDistributionPurchaseDO purchase = new ErpDistributionPurchaseDO()
                    .setBaseId(id)
                    .setOtherFees(otherFees);
            purchaseMapper.update(purchase, new LambdaUpdateWrapper<ErpDistributionPurchaseDO>()
                    .eq(ErpDistributionPurchaseDO::getBaseId, id));
        }

        if (updateCount == 0) {
            throw exception(approve ? DISTRIBUTION_APPROVE_FAIL : DISTRIBUTION_PROCESS_FAIL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseAfterSales(ErpDistributionPurchaseAfterSalesUpdateReqVO reqVO) {
        // 1. 校验存在
        ErpDistributionBaseDO distribution = validateDistribution(reqVO.getId());

        // 2. 解析时间，兼容多种格式
        LocalDateTime purchaseAfterSalesTime = parseDateTime(reqVO.getPurchaseAfterSalesTime());
        LocalDateTime AfterSalesTime = parseDateTime(reqVO.getAfterSalesTime());


            // 3. 更新基础表的售后信息
            distributionMapper.updateById(new ErpDistributionBaseDO()
            .setId(reqVO.getId())
            .setAfterSalesStatus(reqVO.getAfterSalesStatus())
            .setAfterSalesTime(AfterSalesTime));
        // 4. 更新采购售后信息
        ErpDistributionPurchaseDO updateObj = new ErpDistributionPurchaseDO()
                .setPurchaseAfterSalesStatus(reqVO.getPurchaseAfterSalesStatus())
//                .setPurchaseAfterSalesSituation(reqVO.getPurchaseAfterSalesSituation())
                .setPurchaseAfterSalesAmount(reqVO.getPurchaseAfterSalesAmount())
                .setPurchaseAfterSalesTime(purchaseAfterSalesTime);
        purchaseMapper.update(updateObj, new LambdaUpdateWrapper<ErpDistributionPurchaseDO>()
                .eq(ErpDistributionPurchaseDO::getBaseId, reqVO.getId()));
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleAfterSales(ErpDistributionSaleAfterSalesUpdateReqVO reqVO) {
        // 1. 校验存在
        ErpDistributionBaseDO distribution = validateDistribution(reqVO.getId());

        // 2. 解析时间，兼容多种格式
        LocalDateTime purchaseAfterSalesTime = parseDateTime(reqVO.getSaleAfterSalesTime());
        LocalDateTime AfterSalesTime = parseDateTime(reqVO.getAfterSalesTime());

        // 3. 更新基础表的售后信息
        distributionMapper.updateById(new ErpDistributionBaseDO()
                .setId(reqVO.getId())
                .setAfterSalesStatus(reqVO.getAfterSalesStatus())
                .setAfterSalesTime(AfterSalesTime));

        // 4. 更新销售售后信息
        ErpDistributionSaleDO updateObj = new ErpDistributionSaleDO()
                .setSaleAfterSalesStatus(reqVO.getSaleAfterSalesStatus())
//                .setSaleAfterSalesSituation(reqVO.getSaleAfterSalesSituation())
                .setSaleAfterSalesAmount(reqVO.getSaleAfterSalesAmount())
                .setSaleAfterSalesTime(purchaseAfterSalesTime);
        saleMapper.update(updateObj, new LambdaUpdateWrapper<ErpDistributionSaleDO>()
                .eq(ErpDistributionSaleDO::getBaseId, reqVO.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseAuditStatus(Long id, Integer purchaseAuditStatus, BigDecimal otherFees) {
        // 1. 校验存在
        ErpDistributionBaseDO distribution = validateDistribution(id);

        // 2. 获取当前采购审核状态
        ErpDistributionPurchaseDO purchase = purchaseMapper.selectByBaseId(id);
        if (purchase == null) {
            throw exception(DISTRIBUTION_NOT_EXISTS);
        }

        // 3. 校验状态是否重复
        if (purchase.getPurchaseAuditStatus() != null && purchase.getPurchaseAuditStatus().equals(purchaseAuditStatus)) {
            throw exception(DISTRIBUTION_PROCESS_FAIL);
        }

        // 4. 更新采购审核状态
        ErpDistributionPurchaseDO updateObj = new ErpDistributionPurchaseDO()
                .setPurchaseAuditStatus(purchaseAuditStatus)
                .setOtherFees(otherFees);

        // 根据审核状态设置相应时间
        if (purchaseAuditStatus == 20) { // 审核通过
            updateObj.setPurchaseApprovalTime(LocalDateTime.now());
        } else if (purchaseAuditStatus == 10) { // 反审核
            updateObj.setPurchaseUnapproveTime(LocalDateTime.now());
        }

        purchaseMapper.update(updateObj, new LambdaUpdateWrapper<ErpDistributionPurchaseDO>()
                .eq(ErpDistributionPurchaseDO::getBaseId, id));
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleAuditStatus(Long id, Integer saleAuditStatus, BigDecimal otherFees) {
        // 1. 校验存在
        ErpDistributionBaseDO distribution = validateDistribution(id);

        // 2. 获取当前销售审核状态
        ErpDistributionSaleDO sale = saleMapper.selectByBaseId(id);
        if (sale == null) {
            throw exception(DISTRIBUTION_NOT_EXISTS);
        }

        // 3. 校验状态是否重复
        if (sale.getSaleAuditStatus() != null && sale.getSaleAuditStatus().equals(saleAuditStatus)) {
            throw exception(DISTRIBUTION_PROCESS_FAIL);
        }

        // 4. 更新销售审核状态
        ErpDistributionSaleDO updateObj = new ErpDistributionSaleDO()
                .setSaleAuditStatus(saleAuditStatus)
                .setOtherFees(otherFees);

        // 根据审核状态设置相应时间
        if (saleAuditStatus == 20) { // 审核通过
            updateObj.setSaleApprovalTime(LocalDateTime.now());
        } else if (saleAuditStatus == 10) { // 反审核
            updateObj.setSaleUnapproveTime(LocalDateTime.now());
        }

        saleMapper.update(updateObj, new LambdaUpdateWrapper<ErpDistributionSaleDO>()
                .eq(ErpDistributionSaleDO::getBaseId, id));
    }

//    private LocalDateTime parseDateTime(String dateTimeStr) {
//        try {
//            // 尝试解析第一种格式：yyyy-MM-dd'T'HH:mm
//            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
//        } catch (DateTimeParseException e1) {
//            try {
//                // 尝试解析第二种格式：yyyy-MM-dd'T'HH:mm:ss
//                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
//            } catch (DateTimeParseException e2) {
//                try {
//                    // 尝试解析第三种格式：yyyy-MM-dd HH:mm:ss
//                    return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND));
//                } catch (DateTimeParseException e3) {
//                    try {
//                        // 尝试解析第四种格式：带时区的ISO 8601格式（如2025-05-21T05:52:26.000Z）
//                        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//                        return offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(); // 转换为本地时间
//                    } catch (DateTimeParseException e4) {
//                        throw new IllegalArgumentException("无法解析时间格式: " + dateTimeStr);
//                    }
//                }
//            }
//        }
//    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        // 先检查是否为null或空
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        // 尝试解析为时间戳格式
        try {
            long timestamp = Long.parseLong(dateTimeStr);
            // 判断是秒级还是毫秒级时间戳
            if (dateTimeStr.length() <= 10) { // 秒级
                return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
            } else { // 毫秒级
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            }
        } catch (NumberFormatException e) {
            // 如果不是时间戳，继续原有解析逻辑
        }

        try {
            // 尝试解析第一种格式：yyyy-MM-dd'T'HH:mm
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (DateTimeParseException e1) {
            try {
                // 尝试解析第二种格式：yyyy-MM-dd'T'HH:mm:ss
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } catch (DateTimeParseException e2) {
                try {
                    // 尝试解析第三种格式：yyyy-MM-dd HH:mm:ss
                    return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND));
                } catch (DateTimeParseException e3) {
                    try {
                        // 尝试解析第四种格式：带时区的ISO 8601格式（如2025-05-21T05:52:26.000Z）
                        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        return offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                    } catch (DateTimeParseException e4) {
                        throw new IllegalArgumentException("无法解析时间格式: " + dateTimeStr);
                    }
                }
            }
        }
    }
}
