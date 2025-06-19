package cn.iocoder.yudao.module.erp.service.distribution;



import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.*;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO.ErpDistributionImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO.ErpDistributionImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO.ErpDistributionPurchaseAuditImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ImportVO.ErpDistributionSaleAuditImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceESDO;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionCombinedMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionPurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionSaleMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductESRepository;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalePriceESRepository;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalespersonService;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
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

    @Resource
    private ErpSalespersonService salespersonService;

    @Resource
    private ErpCustomerService customerService;

    @Resource
    private ErpDistributionCombinedMapper distributionCombinedMapper;
    @Resource
    private ErpDistributionCombinedESRepository distributionCombinedESRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initESIndex() {
        System.out.println("开始初始化代发订单ES索引...");
        try {
            // 初始化合并后的代发表索引
            IndexOperations combinedIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionCombinedESDO.class);
            if (!combinedIndexOps.exists()) {
                combinedIndexOps.create();
                combinedIndexOps.putMapping(combinedIndexOps.createMapping(ErpDistributionCombinedESDO.class));
                System.out.println("代发合并表索引创建成功");
            }
        } catch (Exception e) {
            System.err.println("代发订单索引初始化失败: " + e.getMessage());
        }
    }

    @Async
    public void fullSyncToES() {
        try {
            System.out.println("开始同步代发数据到ES...");
            // 同步合并后的代发表
            List<ErpDistributionCombinedDO> combinedList = distributionCombinedMapper.selectCombinedList(null);
            System.out.println("从数据库查询到 " + (combinedList != null ? combinedList.size() : 0) + " 条代发记录");
            
            if (CollUtil.isNotEmpty(combinedList)) {
                List<ErpDistributionCombinedESDO> combinedESList = combinedList.stream()
                        .map(this::convertCombinedToES)
                        .collect(Collectors.toList());
                        
                elasticsearchRestTemplate.save(combinedESList);
                // 刷新ES索引
                elasticsearchRestTemplate.indexOps(ErpDistributionCombinedESDO.class).refresh();
            }
            System.out.println("代发订单全量同步ES数据完成");
        } catch (Exception e) {
            System.err.println("代发订单全量同步ES数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ErpDistributionCombinedESDO convertCombinedToES(ErpDistributionCombinedDO combinedDO) {
        ErpDistributionCombinedESDO esDO = new ErpDistributionCombinedESDO();
        
        // 先复制基础字段
        BeanUtils.copyProperties(combinedDO, esDO);
        
        // 填充keyword字段（与组品表保持一致）
        esDO.setNoKeyword(combinedDO.getNo());
        esDO.setOrderNumberKeyword(combinedDO.getOrderNumber());
        esDO.setLogisticsCompanyKeyword(combinedDO.getLogisticsCompany());
        esDO.setTrackingNumberKeyword(combinedDO.getTrackingNumber());
        esDO.setReceiverNameKeyword(combinedDO.getReceiverName());
        esDO.setReceiverPhoneKeyword(combinedDO.getReceiverPhone());
        esDO.setReceiverAddressKeyword(combinedDO.getReceiverAddress());
        esDO.setOriginalProductKeyword(combinedDO.getOriginalProductName());
        esDO.setOriginalSpecificationKeyword(combinedDO.getOriginalStandard());
        esDO.setAfterSalesStatusKeyword(combinedDO.getAfterSalesStatus());
        esDO.setSalespersonKeyword(combinedDO.getSalesperson());
        esDO.setCustomerNameKeyword(combinedDO.getCustomerName());
        esDO.setTransferPersonKeyword(combinedDO.getTransferPerson());
        esDO.setCreatorKeyword(combinedDO.getCreator());
        esDO.setUpdaterKeyword(combinedDO.getUpdater());
        
        // 字段名映射修正
        esDO.setOriginalProduct(combinedDO.getOriginalProductName());
        esDO.setOriginalSpecification(combinedDO.getOriginalStandard());
        
        // 如果有组品ID，从组品表获取相关信息并填充到ES对象中
        if (combinedDO.getComboProductId() != null) {
            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combinedDO.getComboProductId());
            if (comboProductOpt.isPresent()) {
                ErpComboProductES comboProduct = comboProductOpt.get();
                esDO.setComboProductNo(comboProduct.getNo());
                esDO.setComboProductNoKeyword(comboProduct.getNo());
                esDO.setShippingCode(comboProduct.getShippingCode());
                esDO.setShippingCodeKeyword(comboProduct.getShippingCode());
                esDO.setProductName(comboProduct.getName());
                esDO.setProductNameKeyword(comboProduct.getName());
                esDO.setPurchaser(comboProduct.getPurchaser());
                esDO.setPurchaserKeyword(comboProduct.getPurchaser());
                esDO.setSupplier(comboProduct.getSupplier());
                esDO.setSupplierKeyword(comboProduct.getSupplier());
            }
        }
        
        return esDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDistribution(ErpDistributionSaveReqVO createReqVO) {
        // 1. 校验数据
        validateDistributionForCreateOrUpdate(null, createReqVO);

        // 2. 生成代发单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.DISTRIBUTION_NO_PREFIX);
        if (distributionCombinedMapper.selectByNo(no) != null) {
            throw exception(DISTRIBUTION_NO_EXISTS);
        }
        LocalDateTime afterSalesTime = parseDateTime(createReqVO.getAfterSalesTime());

        // 3. 生成ID
        Long id = IdUtil.getSnowflakeNextId();

        // 4. 保存到数据库
        ErpDistributionCombinedDO combinedDO = BeanUtils.toBean(createReqVO, ErpDistributionCombinedDO.class)
                .setId(id)
                .setNo(no)
                .setAfterSalesTime(afterSalesTime)
                .setPurchaseAuditStatus(ErpAuditStatus.PROCESS.getStatus())
                .setPurchaseAfterSalesStatus(30)
                .setSaleAuditStatus(ErpAuditStatus.PROCESS.getStatus())
                .setPurchaseOtherFees(createReqVO.getOtherFees())
                .setSaleAfterSalesStatus(30);
        distributionCombinedMapper.insert(combinedDO);

        // 5. 保存到ES
        ErpDistributionCombinedESDO combinedESDO = convertCombinedToES(combinedDO);
        distributionCombinedESRepository.save(combinedESDO);

        // 6. 刷新ES索引
        elasticsearchRestTemplate.indexOps(ErpDistributionCombinedESDO.class).refresh();

        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDistribution(ErpDistributionSaveReqVO updateReqVO) {
        // 1.1 校验存在 - 通过订单号查询
        ErpDistributionCombinedESDO combined = distributionCombinedESRepository.findByNo(updateReqVO.getNo());
        if (combined == null) {
            throw exception(DISTRIBUTION_NOT_EXISTS);
        }

        // 1.2 校验采购审核状态
        if (ErpAuditStatus.APPROVE.getStatus().equals(combined.getPurchaseAuditStatus())) {
            throw exception(DISTRIBUTION_UPDATE_FAIL_PURCHASE_APPROVE, combined.getNo());
        }

        // 1.3 校验销售审核状态
        if (ErpAuditStatus.APPROVE.getStatus().equals(combined.getSaleAuditStatus())) {
            throw exception(DISTRIBUTION_UPDATE_FAIL_SALE_APPROVE, combined.getNo());
        }
        System.out.println("查看代发的组品编号"+updateReqVO.getComboProductId());

        // 1.4 校验数据
        validateDistributionForCreateOrUpdate(combined.getId(), updateReqVO);

        // 2. 更新数据库记录
        ErpDistributionCombinedDO updateDO = BeanUtils.toBean(updateReqVO, ErpDistributionCombinedDO.class)
                .setId(combined.getId())
                .setPurchaseOtherFees(updateReqVO.getOtherFees())
                .setNo(combined.getNo())
                .setPurchaseAuditStatus(combined.getPurchaseAuditStatus())
                .setSaleAuditStatus(combined.getSaleAuditStatus());
        distributionCombinedMapper.updateById(updateDO);
        ErpDistributionCombinedDO dbCombined = distributionCombinedMapper.selectById(updateDO.getId());
        // 3. 更新ES记录
        ErpDistributionCombinedESDO combinedESDO = convertCombinedToES(updateDO);
        combinedESDO.setCreator(dbCombined.getCreator());
         combinedESDO.setCreateTime(dbCombined.getCreateTime());
        distributionCombinedESRepository.save(combinedESDO);

        // 4. 刷新ES索引
        elasticsearchRestTemplate.indexOps(ErpDistributionCombinedESDO.class).refresh();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDistribution(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在 - 从数据库查询
        List<ErpDistributionCombinedDO> distributions = distributionCombinedMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(distributions)) {
            throw exception(DISTRIBUTION_NOT_EXISTS);
        }

        // 2. 从数据库删除
        distributionCombinedMapper.deleteBatchIds(ids);

        // 3. 从ES删除 - 使用相同的ID集合
        distributionCombinedESRepository.deleteAllById(ids);
    }

    @Override
    public ErpDistributionRespVO getDistribution(Long id) {
        // 1. 从合并ES表查询数据
        Optional<ErpDistributionCombinedESDO> combinedOpt = distributionCombinedESRepository.findById(id);
        if (!combinedOpt.isPresent()) {
            return null;
        }
        ErpDistributionCombinedESDO combined = combinedOpt.get();

        // 2. 转换为RespVO
        ErpDistributionRespVO respVO = BeanUtils.toBean(combined, ErpDistributionRespVO.class)
                .setOtherFees(combined.getPurchaseOtherFees())
                .setPurchaseAfterSalesTime(combined.getPurchaseAfterSalesTime());

        // 3. 查询组品信息并设置到respVO
        if (combined.getComboProductId() != null) {
           // System.out.println("组品编号"+combined.getComboProductId());
            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combined.getComboProductId());
           // System.out.println("搜索到数据"+comboProductOpt.isPresent());
            if (comboProductOpt.isPresent()) {
                ErpComboProductES comboProduct = comboProductOpt.get();
                //System.out.println("组品信息"+comboProduct);
                respVO.setShippingCode(comboProduct.getShippingCode());
                respVO.setProductName(comboProduct.getName());
                respVO.setPurchaser(comboProduct.getPurchaser());
                respVO.setSupplier(comboProduct.getSupplier());
                respVO.setPurchasePrice(comboProduct.getPurchasePrice());
                respVO.setComboProductNo(comboProduct.getNo());

                // 计算采购运费和总额
                BigDecimal shippingFee = calculatePurchaseShippingFee(comboProduct, respVO.getProductQuantity());
                BigDecimal totalPurchaseAmount = comboProduct.getPurchasePrice()
                        .multiply(new BigDecimal(respVO.getProductQuantity()))
                        .add(shippingFee)
                        .add(combined.getPurchaseOtherFees() != null ? combined.getPurchaseOtherFees() : BigDecimal.ZERO);

                respVO.setShippingFee(shippingFee);
                respVO.setTotalPurchaseAmount(totalPurchaseAmount);
                System.out.println(respVO.getShippingFee());
                System.out.println(respVO.getTotalPurchaseAmount());

                // 4. 根据组品ID和客户名称获取销售价格
                if (combined.getCustomerName() != null) {
                    Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                            combined.getComboProductId(), combined.getCustomerName());
                    if (salePriceOpt.isPresent()) {
                        ErpSalePriceESDO salePrice = salePriceOpt.get();
                        respVO.setSalePrice(salePrice.getDistributionPrice());

                        // 计算销售运费和总额
                        BigDecimal saleShippingFee = calculateSaleShippingFee(salePrice, respVO.getProductQuantity(), combined.getComboProductId());
                        BigDecimal totalSaleAmount = salePrice.getDistributionPrice()
                                .multiply(new BigDecimal(respVO.getProductQuantity()))
                                .add(saleShippingFee)
                                .add(combined.getSaleOtherFees() != null ? combined.getSaleOtherFees() : BigDecimal.ZERO);

                        respVO.setSaleShippingFee(saleShippingFee);
                        respVO.setTotalSaleAmount(totalSaleAmount);
                    }
                }
            }
        }

        return respVO;
    }

    private BigDecimal calculatePurchaseShippingFee(ErpComboProductES comboProduct, Integer quantity) {
        BigDecimal shippingFee = BigDecimal.ZERO;
        switch (comboProduct.getShippingFeeType()) {
            case 0: // 固定运费
                shippingFee = comboProduct.getFixedShippingFee();
                break;
            case 1: // 按件计费
                if (comboProduct.getAdditionalItemQuantity() > 0) {
                    int additionalUnits = (int) Math.ceil((double) quantity / comboProduct.getAdditionalItemQuantity());
                    shippingFee = comboProduct.getAdditionalItemPrice().multiply(new BigDecimal(additionalUnits));
                }
                break;
            case 2: // 按重量计费
                BigDecimal totalWeight = comboProduct.getWeight().multiply(new BigDecimal(quantity));
                if (totalWeight.compareTo(comboProduct.getFirstWeight()) <= 0) {
                    shippingFee = comboProduct.getFirstWeightPrice();
                } else {
                    BigDecimal additionalWeight = totalWeight.subtract(comboProduct.getFirstWeight());
                    BigDecimal additionalUnits = additionalWeight.divide(comboProduct.getAdditionalWeight(), 0, RoundingMode.UP);
                    shippingFee = comboProduct.getFirstWeightPrice().add(
                            comboProduct.getAdditionalWeightPrice().multiply(additionalUnits)
                    );
                }
                break;
        }
        return shippingFee;
    }

    private BigDecimal calculateSaleShippingFee(ErpSalePriceESDO salePrice, Integer quantity, Long comboProductId) {
        BigDecimal shippingFee = BigDecimal.ZERO;
        switch (salePrice.getShippingFeeType()) {
            case 0: // 固定运费
                shippingFee = salePrice.getFixedShippingFee();
                break;
            case 1: // 按件计费
                if (salePrice.getAdditionalItemQuantity() > 0) {
                    int additionalUnits = (int) Math.ceil((double) quantity / salePrice.getAdditionalItemQuantity());
                    shippingFee = salePrice.getAdditionalItemPrice().multiply(new BigDecimal(additionalUnits));
                }
                break;
            case 2: // 按重计费
                Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(comboProductId);
                if (comboProductOpt.isPresent()) {
                    BigDecimal productWeight = comboProductOpt.get().getWeight();
                    BigDecimal totalWeight = productWeight.multiply(new BigDecimal(quantity));

                    if (totalWeight.compareTo(salePrice.getFirstWeight()) <= 0) {
                        shippingFee = salePrice.getFirstWeightPrice();
                    } else {
                        BigDecimal additionalWeight = totalWeight.subtract(salePrice.getFirstWeight());
                        BigDecimal additionalUnits = additionalWeight.divide(salePrice.getAdditionalWeight(), 0, RoundingMode.UP);
                        shippingFee = salePrice.getFirstWeightPrice().add(
                                salePrice.getAdditionalWeightPrice().multiply(additionalUnits)
                        );
                    }
                }
                break;
        }
        return shippingFee;
    }

    @Override
    public ErpDistributionCombinedDO validateDistribution(Long id) {
        Optional<ErpDistributionCombinedESDO> esDO = distributionCombinedESRepository.findById(id);
        if (!esDO.isPresent()) {
            throw exception(DISTRIBUTION_NOT_EXISTS);
        }
        return convertESToCombinedDO(esDO.get());
    }

    private ErpDistributionCombinedDO convertESToCombinedDO(ErpDistributionCombinedESDO esDO) {
        ErpDistributionCombinedDO combinedDO = new ErpDistributionCombinedDO();
        BeanUtils.copyProperties(esDO, combinedDO);
        return combinedDO;
    }

    @Override
    public PageResult<ErpDistributionRespVO> getDistributionVOPage(ErpDistributionPageReqVO pageReqVO) {
        try {
            // 1. 检查数据库是否有数据
            long dbCount = distributionCombinedMapper.selectCount(null);

            // 2. 检查ES索引是否存在
            IndexOperations combinedIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionCombinedESDO.class);
            boolean indexExists = combinedIndexOps.exists();

            // 3. 检查ES数据量
            long esCount = 0;
            if (indexExists) {
                esCount = elasticsearchRestTemplate.count(
                    new NativeSearchQueryBuilder().build(),
                    ErpDistributionCombinedESDO.class
                );
            }

            // 4. 调试：检查ES中的实际数据内容
            if (indexExists && esCount > 0) {
                System.out.println("=== ES数据内容检查 ===");
                NativeSearchQuery debugQuery = new NativeSearchQueryBuilder()
                        .withPageable(PageRequest.of(0, 5))
                        .build();
                SearchHits<ErpDistributionCombinedESDO> debugHits = elasticsearchRestTemplate.search(
                        debugQuery,
                        ErpDistributionCombinedESDO.class,
                        IndexCoordinates.of("erp_distribution_combined"));
                
                for (SearchHit<ErpDistributionCombinedESDO> hit : debugHits) {
                    ErpDistributionCombinedESDO content = hit.getContent();
                    System.out.println("ES记录 - ID: " + content.getId() + ", no: '" + content.getNo() + "'");
                    System.out.println("  orderNumber: '" + content.getOrderNumber() + "'");
                    System.out.println("  logisticsCompany: '" + content.getLogisticsCompany() + "'");
                    System.out.println("  receiverName: '" + content.getReceiverName() + "'");
                }
                System.out.println("=== ES数据内容检查结束 ===");
            }

            // 5. 处理数据库和ES数据不一致的情况
            if (dbCount == 0) {
                if (indexExists && esCount > 0) {
                    // 数据库为空但ES有数据，清空ES
                    distributionCombinedESRepository.deleteAll();
                    System.out.println("检测到数据库为空但ES有数据，已清空ES索引");
                }
                return new PageResult<>(Collections.emptyList(), 0L);
            }

            // 6. 如果索引不存在或数据不一致，重建索引
            if (!indexExists || esCount != dbCount) {
                System.out.println("检测到ES索引不存在或数据不一致，开始重建索引...");
                System.out.println("数据库记录数: " + dbCount + ", ES记录数: " + esCount);
                
                // 删除现有索引（如果存在）
                if (indexExists) {
                    combinedIndexOps.delete();
                    System.out.println("已删除旧索引");
                }
                // 重新创建索引和映射
                combinedIndexOps.create();
                System.out.println("已创建新索引");
                combinedIndexOps.putMapping(combinedIndexOps.createMapping(ErpDistributionCombinedESDO.class));
                System.out.println("已设置字段映射");
                // 全量同步数据
                fullSyncToES();
                System.out.println("ES索引重建和数据同步完成");
            }

            // 2. 构建基础查询条件
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()))
                    .withTrackTotalHits(true)
                    .withSort(Sort.by(Sort.Direction.DESC, "id"));

            // 3. 添加查询条件 - 完全使用组品表搜索策略
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            
            // 订单编号搜索 - 超智能搜索策略，精确控制匹配范围
            if (StrUtil.isNotBlank(pageReqVO.getNo())) {
                BoolQueryBuilder noQuery = QueryBuilders.boolQuery();
                String no = pageReqVO.getNo().trim();

                // 添加调试信息
                System.out.println("=== 订单编号搜索调试 ===");
                System.out.println("查询关键词: '" + no + "', 长度: " + no.length());

                BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                
                // 第一优先级：完全精确匹配（权重最高，确保精确查询优先）
                multiMatchQuery.should(QueryBuilders.termQuery("no_keyword", no).boost(1000000.0f));
                System.out.println("添加精确匹配: no_keyword = '" + no + "', 权重: 1000000");
                
                // 第二优先级：前缀匹配（支持"DFJL"匹配"DFJL20250614..."）
                multiMatchQuery.should(QueryBuilders.prefixQuery("no_keyword", no).boost(100000.0f));
                System.out.println("添加前缀匹配: no_keyword 前缀 = '" + no + "', 权重: 100000");
                
                // 第三优先级：通配符包含匹配
                multiMatchQuery.should(QueryBuilders.wildcardQuery("no_keyword", "*" + no + "*").boost(10000.0f));
                System.out.println("添加通配符匹配: *" + no + "*, 权重: 10000");

                // 第四优先级：超智能子字符串匹配
                if (no.length() >= 2) {
                    if (no.length() <= 6) {
                        // 短查询词：优化子字符串匹配，避免过短子字符串的误匹配
                        System.out.println("使用短查询词策略（≤6字符）");
                        
                        // 后缀子字符串匹配 - 只保留长度>=4且不包含太多重复字符的有意义子字符串，避免误匹配
                        for (int i = 1; i < no.length(); i++) {
                            String substring = no.substring(i);
                            if (substring.length() >= 4 && !containsTooManyRepeatedChars(substring)) { 
                                multiMatchQuery.should(QueryBuilders.wildcardQuery("no_keyword", "*" + substring + "*").boost(5000.0f));
                                System.out.println("  添加后缀子字符串: *" + substring + "*, 权重: 5000");
                            } else if (substring.length() >= 4) {
                                System.out.println("  跳过重复字符过多的后缀: *" + substring + "*");
                            }
                        }
                        
                        // 前缀子字符串匹配 - 只保留长度>=4的有意义子字符串，避免误匹配
                        for (int i = 0; i < no.length() - 1; i++) {
                            String prefix = no.substring(0, no.length() - i);
                            if (prefix.length() >= 4) { // 进一步提高最小长度要求到4
                                multiMatchQuery.should(QueryBuilders.wildcardQuery("no_keyword", "*" + prefix + "*").boost(4000.0f));
                                System.out.println("  添加前缀子字符串: *" + prefix + "*, 权重: 4000");
                            }
                        }
                    } else if (no.length() <= 10) {
                        // 中等长度：主要使用前缀匹配，减少后缀匹配的误匹配
                        System.out.println("使用中等长度策略（7-10字符）");
                        
                        // 前缀匹配（去掉最后几位）- 这是相对安全的匹配方式
                        for (int i = 1; i <= 2 && i < no.length(); i++) { // 减少前缀匹配的数量
                            String prefix = no.substring(0, no.length() - i);
                            if (prefix.length() >= 6) { // 提高前缀最小长度要求
                                multiMatchQuery.should(QueryBuilders.prefixQuery("no_keyword", prefix).boost(4000.0f));
                                System.out.println("  添加部分前缀: " + prefix + ", 权重: 4000");
                            }
                        }
                        
                        // 后缀匹配 - 只在查询词较短且不包含太多重复数字时使用
                        if (no.length() <= 8 && !containsTooManyRepeatedDigits(no)) {
                            for (int i = Math.max(0, no.length() - 4); i < no.length() - 1; i++) { // 减少后缀匹配范围
                                String suffix = no.substring(i);
                                if (suffix.length() >= 5) { // 提高后缀最小长度要求
                                    multiMatchQuery.should(QueryBuilders.wildcardQuery("no_keyword", "*" + suffix).boost(2000.0f)); // 降低权重
                                    System.out.println("  添加后缀: *" + suffix + ", 权重: 2000");
                                }
                            }
                        } else {
                            System.out.println("  跳过后缀匹配（查询词过长或包含重复数字）");
                        }
                    } else if (no.length() <= 15) {
                        // 较长查询词：主要支持前缀匹配，极少的后缀匹配
                        System.out.println("使用较长查询词策略（11-15字符）");
                        multiMatchQuery.should(QueryBuilders.prefixQuery("no_keyword", no).boost(50000.0f));
                        System.out.println("  添加完整前缀: " + no + ", 权重: 50000");
                        
                        // 非常有限的后缀匹配（只匹配最后4位）
                        if (no.length() >= 4) {
                            String lastFour = no.substring(no.length() - 4);
                            multiMatchQuery.should(QueryBuilders.wildcardQuery("no_keyword", "*" + lastFour).boost(2000.0f));
                            System.out.println("  添加最后4位后缀: *" + lastFour + ", 权重: 2000");
                        }
                    } else {
                        // 很长的查询词（>15字符，可能是几乎完整的订单号）：几乎只使用前缀匹配
                        System.out.println("使用很长查询词策略（>15字符）");
                        multiMatchQuery.should(QueryBuilders.prefixQuery("no_keyword", no).boost(90000.0f));
                        System.out.println("  添加完整前缀: " + no + ", 权重: 90000");
                        
                        // 对于完整订单号长度（22字符），完全禁用后缀匹配
                        if (no.length() < 20) {
                            // 只对不完整的长查询词添加后缀匹配
                            String lastThree = no.substring(no.length() - 3);
                            multiMatchQuery.should(QueryBuilders.wildcardQuery("no_keyword", "*" + lastThree).boost(800.0f));
                            System.out.println("  添加最后3位后缀: *" + lastThree + ", 权重: 800");
                        } else {
                            System.out.println("  跳过后缀匹配（查询词过长，可能是完整订单号）");
                        }
                    }
                }

                // 第五优先级：智能分词匹配
                if (no.length() == 1) {
                    multiMatchQuery.should(QueryBuilders.matchQuery("no", no).operator(Operator.OR).boost(800.0f));
                    System.out.println("添加单字分词匹配, 权重: 800");
                } else if (no.length() == 2) {
                    multiMatchQuery.should(QueryBuilders.matchQuery("no", no).operator(Operator.AND).boost(600.0f));
                    multiMatchQuery.should(QueryBuilders.matchPhraseQuery("no", no).boost(1200.0f));
                    multiMatchQuery.should(QueryBuilders.matchQuery("no", no).operator(Operator.OR).boost(400.0f));
                    System.out.println("添加双字分词匹配, 权重: 600/1200/400");
                } else {
                    multiMatchQuery.should(QueryBuilders.matchQuery("no", no).operator(Operator.AND).boost(500.0f));
                    multiMatchQuery.should(QueryBuilders.matchPhraseQuery("no", no).boost(1000.0f));
                    System.out.println("添加多字分词匹配, 权重: 500/1000");
                }

                multiMatchQuery.minimumShouldMatch(1);
                noQuery.must(multiMatchQuery);
                boolQuery.must(noQuery);
                
                System.out.println("=== 订单编号搜索调试结束 ===");
            }
            
            // 订单号搜索 - 使用组品表策略并优化长字符串匹配
            if (StrUtil.isNotBlank(pageReqVO.getOrderNumber())) {
                BoolQueryBuilder orderNumberQuery = QueryBuilders.boolQuery();
                String orderNumber = pageReqVO.getOrderNumber().trim();

                BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                multiMatchQuery.should(QueryBuilders.termQuery("order_number_keyword", orderNumber).boost(1000000.0f));
                multiMatchQuery.should(QueryBuilders.prefixQuery("order_number_keyword", orderNumber).boost(100000.0f));
                multiMatchQuery.should(QueryBuilders.wildcardQuery("order_number_keyword", "*" + orderNumber + "*").boost(10000.0f));

                // 优化子字符串匹配策略
                if (orderNumber.length() >= 2 && orderNumber.length() <= 15) {
                    for (int i = 1; i < orderNumber.length(); i++) {
                        String substring = orderNumber.substring(i);
                        if (substring.length() >= 4 && !containsTooManyRepeatedChars(substring)) { // 避免重复字符过多的子字符串
                            multiMatchQuery.should(QueryBuilders.wildcardQuery("order_number_keyword", "*" + substring + "*").boost(3000.0f));
                        }
                    }
                } else if (orderNumber.length() > 15) {
                    for (int i = Math.max(1, orderNumber.length() - 10); i < orderNumber.length(); i++) {
                        String substring = orderNumber.substring(i);
                        if (substring.length() >= 4) {
                            multiMatchQuery.should(QueryBuilders.wildcardQuery("order_number_keyword", "*" + substring + "*").boost(2000.0f));
                        }
                    }
                }

                if (orderNumber.length() == 1) {
                    multiMatchQuery.should(QueryBuilders.matchQuery("order_number", orderNumber).operator(Operator.OR).boost(800.0f));
                } else if (orderNumber.length() == 2) {
                    multiMatchQuery.should(QueryBuilders.matchQuery("order_number", orderNumber).operator(Operator.AND).boost(600.0f));
                    multiMatchQuery.should(QueryBuilders.matchPhraseQuery("order_number", orderNumber).boost(1200.0f));
                    multiMatchQuery.should(QueryBuilders.matchQuery("order_number", orderNumber).operator(Operator.OR).boost(400.0f));
                } else {
                    multiMatchQuery.should(QueryBuilders.matchQuery("order_number", orderNumber).operator(Operator.AND).boost(500.0f));
                    multiMatchQuery.should(QueryBuilders.matchPhraseQuery("order_number", orderNumber).boost(1000.0f));
                }

                multiMatchQuery.minimumShouldMatch(1);
                orderNumberQuery.must(multiMatchQuery);
                boolQuery.must(orderNumberQuery);
            }
            
            // 物流公司搜索
            if (StrUtil.isNotBlank(pageReqVO.getLogisticsCompany())) {
                boolQuery.must(createComboStyleMatchQuery("logistics_company", "logistics_company_keyword", pageReqVO.getLogisticsCompany().trim()));
            }
            
            // 物流单号搜索 - 使用组品表策略并优化长字符串匹配
            if (StrUtil.isNotBlank(pageReqVO.getTrackingNumber())) {
                BoolQueryBuilder trackingNumberQuery = QueryBuilders.boolQuery();
                String trackingNumber = pageReqVO.getTrackingNumber().trim();

                BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                multiMatchQuery.should(QueryBuilders.termQuery("tracking_number_keyword", trackingNumber).boost(1000000.0f));
                multiMatchQuery.should(QueryBuilders.prefixQuery("tracking_number_keyword", trackingNumber).boost(100000.0f));
                multiMatchQuery.should(QueryBuilders.wildcardQuery("tracking_number_keyword", "*" + trackingNumber + "*").boost(10000.0f));

                // 优化子字符串匹配策略
                if (trackingNumber.length() >= 2 && trackingNumber.length() <= 15) {
                    for (int i = 1; i < trackingNumber.length(); i++) {
                        String substring = trackingNumber.substring(i);
                        if (substring.length() >= 4 && !containsTooManyRepeatedChars(substring)) { // 避免重复字符过多的子字符串
                            multiMatchQuery.should(QueryBuilders.wildcardQuery("tracking_number_keyword", "*" + substring + "*").boost(3000.0f));
                        }
                    }
                } else if (trackingNumber.length() > 15) {
                    for (int i = Math.max(1, trackingNumber.length() - 10); i < trackingNumber.length(); i++) {
                        String substring = trackingNumber.substring(i);
                        if (substring.length() >= 4) {
                            multiMatchQuery.should(QueryBuilders.wildcardQuery("tracking_number_keyword", "*" + substring + "*").boost(2000.0f));
                        }
                    }
                }

                if (trackingNumber.length() == 1) {
                    multiMatchQuery.should(QueryBuilders.matchQuery("tracking_number", trackingNumber).operator(Operator.OR).boost(800.0f));
                } else if (trackingNumber.length() == 2) {
                    multiMatchQuery.should(QueryBuilders.matchQuery("tracking_number", trackingNumber).operator(Operator.AND).boost(600.0f));
                    multiMatchQuery.should(QueryBuilders.matchPhraseQuery("tracking_number", trackingNumber).boost(1200.0f));
                    multiMatchQuery.should(QueryBuilders.matchQuery("tracking_number", trackingNumber).operator(Operator.OR).boost(400.0f));
                } else {
                    multiMatchQuery.should(QueryBuilders.matchQuery("tracking_number", trackingNumber).operator(Operator.AND).boost(500.0f));
                    multiMatchQuery.should(QueryBuilders.matchPhraseQuery("tracking_number", trackingNumber).boost(1000.0f));
                }

                multiMatchQuery.minimumShouldMatch(1);
                trackingNumberQuery.must(multiMatchQuery);
                boolQuery.must(trackingNumberQuery);
            }
            
            // 收件人姓名搜索
            if (StrUtil.isNotBlank(pageReqVO.getReceiverName())) {
                boolQuery.must(createComboStyleMatchQuery("receiver_name", "receiver_name_keyword", pageReqVO.getReceiverName().trim()));
            }
            
            // 联系电话搜索
            if (StrUtil.isNotBlank(pageReqVO.getReceiverPhone())) {
                boolQuery.must(createComboStyleMatchQuery("receiver_phone", "receiver_phone_keyword", pageReqVO.getReceiverPhone().trim()));
            }
            
            // 详细地址搜索
            if (StrUtil.isNotBlank(pageReqVO.getReceiverAddress())) {
                boolQuery.must(createComboStyleMatchQuery("receiver_address", "receiver_address_keyword", pageReqVO.getReceiverAddress().trim()));
            }
            
            // 原表商品搜索
            if (StrUtil.isNotBlank(pageReqVO.getOriginalProduct())) {
                boolQuery.must(createComboStyleMatchQuery("original_product", "original_product_keyword", pageReqVO.getOriginalProduct().trim()));
            }
            
            // 原表规格搜索
            if (StrUtil.isNotBlank(pageReqVO.getOriginalSpecification())) {
                boolQuery.must(createComboStyleMatchQuery("original_specification", "original_specification_keyword", pageReqVO.getOriginalSpecification().trim()));
            }
            
            // 组品编号搜索 - 使用智能编号搜索策略
            if (StrUtil.isNotBlank(pageReqVO.getComboProductNo())) {
                boolQuery.must(createIntelligentNumberMatchQuery("combo_product_no", "combo_product_no_keyword", pageReqVO.getComboProductNo().trim()));
            }
            
            // 发货编码搜索 - 使用智能编号搜索策略
            if (StrUtil.isNotBlank(pageReqVO.getShippingCode())) {
                boolQuery.must(createIntelligentNumberMatchQuery("shipping_code", "shipping_code_keyword", pageReqVO.getShippingCode().trim()));
            }
            
            // 产品名称搜索
            if (StrUtil.isNotBlank(pageReqVO.getProductName())) {
                boolQuery.must(createComboStyleMatchQuery("product_name", "product_name_keyword", pageReqVO.getProductName().trim()));
            }
            
            // 产品规格搜索
            if (StrUtil.isNotBlank(pageReqVO.getProductSpecification())) {
                boolQuery.must(createComboStyleMatchQuery("product_specification", "product_specification_keyword", pageReqVO.getProductSpecification().trim()));
            }
            
            // 售后状况搜索
            if (StrUtil.isNotBlank(pageReqVO.getAfterSalesStatus())) {
                boolQuery.must(createComboStyleMatchQuery("after_sales_status", "after_sales_status_keyword", pageReqVO.getAfterSalesStatus().trim()));
            }
            
            // 采购人员搜索
            if (StrUtil.isNotBlank(pageReqVO.getPurchaser())) {
                boolQuery.must(createComboStyleMatchQuery("purchaser", "purchaser_keyword", pageReqVO.getPurchaser().trim()));
            }
            
            // 供应商名搜索
            if (StrUtil.isNotBlank(pageReqVO.getSupplier())) {
                boolQuery.must(createComboStyleMatchQuery("supplier", "supplier_keyword", pageReqVO.getSupplier().trim()));
            }
            
            // 销售人员搜索
            if (StrUtil.isNotBlank(pageReqVO.getSalesperson())) {
                boolQuery.must(createComboStyleMatchQuery("salesperson", "salesperson_keyword", pageReqVO.getSalesperson().trim()));
            }
            
            // 客户名称搜索
            if (StrUtil.isNotBlank(pageReqVO.getCustomerName())) {
                boolQuery.must(createComboStyleMatchQuery("customer_name", "customer_name_keyword", pageReqVO.getCustomerName().trim()));
            }
            
            // 中转人员搜索
            if (StrUtil.isNotBlank(pageReqVO.getTransferPerson())) {
                boolQuery.must(createComboStyleMatchQuery("transfer_person", "transfer_person_keyword", pageReqVO.getTransferPerson().trim()));
            }
            
            // 创建人员搜索
            if (StrUtil.isNotBlank(pageReqVO.getCreator())) {
                boolQuery.must(createComboStyleMatchQuery("creator", "creator_keyword", pageReqVO.getCreator().trim()));
            }

            // 精确匹配字段
            if (pageReqVO.getStatus() != null) {
                boolQuery.must(QueryBuilders.termQuery("status", pageReqVO.getStatus()));
            }
            if (pageReqVO.getPurchaseAuditStatus() != null) {
                boolQuery.must(QueryBuilders.termQuery("purchase_audit_status", pageReqVO.getPurchaseAuditStatus()));
            }
            if (pageReqVO.getSaleAuditStatus() != null) {
                boolQuery.must(QueryBuilders.termQuery("sale_audit_status", pageReqVO.getSaleAuditStatus()));
            }

            // 时间范围查询
            if (pageReqVO.getCreateTime() != null && pageReqVO.getCreateTime().length == 2) {
                boolQuery.must(QueryBuilders.rangeQuery("create_time")
                        .gte(pageReqVO.getCreateTime()[0])
                        .lte(pageReqVO.getCreateTime()[1]));
            }

            // 售后时间范围查询
            if (pageReqVO.getAfterSalesTime() != null && pageReqVO.getAfterSalesTime().length == 2) {
                boolQuery.must(QueryBuilders.rangeQuery("after_sales_time")
                        .gte(pageReqVO.getAfterSalesTime()[0])
                        .lte(pageReqVO.getAfterSalesTime()[1]));
            }

            queryBuilder.withQuery(boolQuery);
            
            // 在执行主查询前，先测试精确匹配是否工作
            if (StrUtil.isNotBlank(pageReqVO.getNo())) {
                System.out.println("=== 测试精确匹配 ===");
                NativeSearchQuery exactTestQuery = new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.termQuery("no_keyword", pageReqVO.getNo().trim()))
                        .withPageable(PageRequest.of(0, 10))
                        .build();
                
                SearchHits<ErpDistributionCombinedESDO> exactHits = elasticsearchRestTemplate.search(
                        exactTestQuery,
                        ErpDistributionCombinedESDO.class,
                        IndexCoordinates.of("erp_distribution_combined"));
                
                System.out.println("精确匹配测试结果: " + exactHits.getTotalHits() + " 条记录");
                for (SearchHit<ErpDistributionCombinedESDO> hit : exactHits) {
                    System.out.println("  精确匹配到: ID=" + hit.getContent().getId() + ", no='" + hit.getContent().getNo() + "', 评分=" + hit.getScore());
                }
                System.out.println("=== 精确匹配测试结束 ===");
            }
            
            if (pageReqVO.getPageNo() > 1) {
                return handleDeepPagination(pageReqVO, queryBuilder);
            }

            // 4. 执行查询
            SearchHits<ErpDistributionCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpDistributionCombinedESDO.class,
                    IndexCoordinates.of("erp_distribution_combined"));
            
            // 5. 转换为VO并计算金额
            List<ErpDistributionRespVO> voList = searchHits.stream()
                    .map(SearchHit::getContent)
                    .map(combined -> {
                        ErpDistributionRespVO vo = BeanUtils.toBean(combined, ErpDistributionRespVO.class);
                        // 设置采购其他费用
                        vo.setOtherFees(combined.getPurchaseOtherFees());
                        // 设置销售相关的三个字段
                        vo.setSaleUnapproveTime(combined.getSaleUnapproveTime());
                        vo.setSaleAfterSalesAmount(combined.getSaleAfterSalesAmount());
                        vo.setSaleAfterSalesTime(combined.getSaleAfterSalesTime());
                        
                        // 查询组品信息
                        if (combined.getComboProductId() != null) {
                            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combined.getComboProductId());
                            if (comboProductOpt.isPresent()) {
                                ErpComboProductES comboProduct = comboProductOpt.get();
                                vo.setProductName(comboProduct.getName());
                                vo.setShippingCode(comboProduct.getShippingCode());
                                vo.setPurchaser(comboProduct.getPurchaser());
                                vo.setSupplier(comboProduct.getSupplier());
                                vo.setPurchasePrice(comboProduct.getPurchasePrice());
                                vo.setComboProductNo(comboProduct.getNo());

                                // 计算采购运费和总额
                                BigDecimal shippingFee = calculatePurchaseShippingFee(comboProduct, vo.getProductQuantity());
                                BigDecimal totalPurchaseAmount = comboProduct.getPurchasePrice()
                                        .multiply(new BigDecimal(vo.getProductQuantity()))
                                        .add(shippingFee)
                                        .add(combined.getPurchaseOtherFees() != null ? combined.getPurchaseOtherFees() : BigDecimal.ZERO);
                                vo.setShippingFee(shippingFee);
                                vo.setTotalPurchaseAmount(totalPurchaseAmount);

                                // 计算销售运费和总额
                                if (combined.getCustomerName() != null) {
                                    Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                                            combined.getComboProductId(), combined.getCustomerName());
                                    if (salePriceOpt.isPresent()) {
                                        BigDecimal saleShippingFee = calculateSaleShippingFee(salePriceOpt.get(), vo.getProductQuantity(), combined.getComboProductId());
                                        BigDecimal totalSaleAmount = salePriceOpt.get().getDistributionPrice()
                                                .multiply(new BigDecimal(vo.getProductQuantity()))
                                                .add(saleShippingFee)
                                                .add(combined.getSaleOtherFees() != null ? combined.getSaleOtherFees() : BigDecimal.ZERO);
                                        vo.setSalePrice(salePriceOpt.get().getDistributionPrice());
                                        vo.setSaleShippingFee(saleShippingFee);
                                        vo.setTotalSaleAmount(totalSaleAmount);
                                    }
                                }
                            }
                        }
                        return vo;
                    })
                    .collect(Collectors.toList());

            return new PageResult<>(voList, searchHits.getTotalHits());
        } catch (Exception e) {
            System.out.println("ES查询失败，回退到数据库查询: " + e.getMessage());
            return new PageResult<>(Collections.emptyList(), 0L);
        }
    }

    private PageResult<ErpDistributionRespVO> handleDeepPagination(ErpDistributionPageReqVO pageReqVO,
                                                                   NativeSearchQueryBuilder queryBuilder) {
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

            SearchHits<ErpDistributionCombinedESDO> prevHits = elasticsearchRestTemplate.search(
                    prevQuery,
                    ErpDistributionCombinedESDO.class,
                    IndexCoordinates.of("erp_distribution_combined"));

            if (prevHits.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), prevHits.getTotalHits());
            }

            // 设置search_after参数
            SearchHit<ErpDistributionCombinedESDO> lastHit = prevHits.getSearchHits().get(0);
            query.setSearchAfter(lastHit.getSortValues());
        }

        // 3. 执行查询
        SearchHits<ErpDistributionCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                query,
                ErpDistributionCombinedESDO.class,
                IndexCoordinates.of("erp_distribution_combined"));

        // 4. 转换为VO并计算金额
        List<ErpDistributionRespVO> voList = searchHits.stream()
                .map(SearchHit::getContent)
                .map(combined -> {
                    ErpDistributionRespVO vo = BeanUtils.toBean(combined, ErpDistributionRespVO.class);
                    // 设置采购其他费用
                    vo.setOtherFees(combined.getPurchaseOtherFees());
                    // 设置销售相关的三个字段
                    vo.setSaleUnapproveTime(combined.getSaleUnapproveTime());
                    vo.setSaleAfterSalesAmount(combined.getSaleAfterSalesAmount());
                    vo.setSaleAfterSalesTime(combined.getSaleAfterSalesTime());

                    // 查询组品信息
                    if (combined.getComboProductId() != null) {
                        Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combined.getComboProductId());
                        if (comboProductOpt.isPresent()) {
                            ErpComboProductES comboProduct = comboProductOpt.get();
                            vo.setProductName(comboProduct.getName());
                            vo.setShippingCode(comboProduct.getShippingCode());
                            vo.setPurchaser(comboProduct.getPurchaser());
                            vo.setSupplier(comboProduct.getSupplier());
                            vo.setPurchasePrice(comboProduct.getPurchasePrice());
                            vo.setComboProductNo(comboProduct.getNo());

                            // 计算采购运费和总额
                            BigDecimal shippingFee = calculatePurchaseShippingFee(comboProduct, vo.getProductQuantity());
                            BigDecimal totalPurchaseAmount = comboProduct.getPurchasePrice()
                                    .multiply(new BigDecimal(vo.getProductQuantity()))
                                    .add(shippingFee)
                                    .add(combined.getPurchaseOtherFees() != null ? combined.getPurchaseOtherFees() : BigDecimal.ZERO);
                            vo.setShippingFee(shippingFee);
                            vo.setTotalPurchaseAmount(totalPurchaseAmount);

                            // 计算销售运费和总额
                            if (combined.getCustomerName() != null) {
                                Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                                        combined.getComboProductId(), combined.getCustomerName());
                                if (salePriceOpt.isPresent()) {
                                    BigDecimal saleShippingFee = calculateSaleShippingFee(salePriceOpt.get(), vo.getProductQuantity(), combined.getComboProductId());
                                    BigDecimal totalSaleAmount = salePriceOpt.get().getDistributionPrice()
                                            .multiply(new BigDecimal(vo.getProductQuantity()))
                                            .add(saleShippingFee)
                                            .add(combined.getSaleOtherFees() != null ? combined.getSaleOtherFees() : BigDecimal.ZERO);
                                    vo.setSalePrice(salePriceOpt.get().getDistributionPrice());
                                    vo.setSaleShippingFee(saleShippingFee);
                                    vo.setTotalSaleAmount(totalSaleAmount);
                                }
                            }
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

    private void calculateSaleAmount(ErpDistributionRespVO vo, ErpDistributionPurchaseESDO purchase,
                            ErpDistributionSaleESDO sale) {
    // 从ES查询销售价格
    //System.out.println("开始查询销售价格 - 组品ID: " + purchase.getComboProductId() + ", 客户名称: " + sale.getCustomerName());
    Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
            purchase.getComboProductId(), sale.getCustomerName());

    if (salePriceOpt.isPresent()) {
        ErpSalePriceESDO salePrice = salePriceOpt.get();
        //System.out.println("找到销售价格记录: " + salePrice);

        BigDecimal salePriceValue = salePrice.getDistributionPrice();
        BigDecimal saleOtherFees = sale.getOtherFees() != null ? sale.getOtherFees() : BigDecimal.ZERO;

        //System.out.println("销售单价: " + salePriceValue + ", 其他费用: " + saleOtherFees);

        // 计算销售运费
        BigDecimal saleShippingFee = calculateSaleShippingFee(salePrice, vo.getProductQuantity(), purchase.getComboProductId());
        //System.out.println("计算出的销售运费: " + saleShippingFee);

        BigDecimal totalSaleAmount = salePriceValue
                .multiply(new BigDecimal(vo.getProductQuantity()))
                .add(saleShippingFee)
                .add(saleOtherFees);

        //System.out.println("销售总额计算: " + salePriceValue + " * " + vo.getProductQuantity()
              //  + " + " + saleShippingFee + " + " + saleOtherFees + " = " + totalSaleAmount);

        vo.setSalePrice(salePriceValue);
        vo.setSaleShippingFee(saleShippingFee);
        vo.setTotalSaleAmount(totalSaleAmount);
    } else {
        System.out.println("未找到匹配的销售价格记录");
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
        // 1. 校验订单号唯一 - 使用合并表ES查询
        ErpDistributionCombinedESDO distribution = distributionCombinedESRepository.findByNo(reqVO.getNo());
        if (distribution != null && !distribution.getId().equals(id)) {
            throw exception(DISTRIBUTION_NO_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDistributionStatus(Long id, Integer status, BigDecimal otherFees) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);

        // 1.1 校验存在 - 使用ES查询
        Optional<ErpDistributionBaseESDO> baseOpt = distributionBaseESRepository.findById(id);
        if (!baseOpt.isPresent()) {
            throw exception(DISTRIBUTION_NOT_EXISTS);
        }
        ErpDistributionBaseESDO distribution = baseOpt.get();

        // 1.2 校验状态
        if (distribution.getStatus().equals(status)) {
            throw exception(approve ? DISTRIBUTION_APPROVE_FAIL : DISTRIBUTION_PROCESS_FAIL);
        }

        // 2. 更新基础信息状态
        distribution.setStatus(status);
        distributionBaseESRepository.save(distribution);

        // 3. 更新采购信息的其他费用
        if (otherFees != null) {
            Optional<ErpDistributionPurchaseESDO> purchaseOpt = distributionPurchaseESRepository.findByBaseId(id);
            if (purchaseOpt.isPresent()) {
                ErpDistributionPurchaseESDO purchase = purchaseOpt.get();
                purchase.setOtherFees(otherFees);
                distributionPurchaseESRepository.save(purchase);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseAfterSales(ErpDistributionPurchaseAfterSalesUpdateReqVO reqVO) {
        // 1. 校验存在 - 使用合并表ES查询
        Optional<ErpDistributionCombinedESDO> combinedOpt = distributionCombinedESRepository.findById(reqVO.getId());
        if (!combinedOpt.isPresent()) {
            throw exception(DISTRIBUTION_NOT_EXISTS);
        }

        // 2. 解析时间
        LocalDateTime purchaseAfterSalesTime = parseDateTime(reqVO.getPurchaseAfterSalesTime());
        LocalDateTime afterSalesTime = parseDateTime(reqVO.getAfterSalesTime());

        System.out.println("更新采购售后信息 - 原始时间字符串: " + reqVO.getPurchaseAfterSalesTime());
        System.out.println("更新采购售后信息 - 解析后时间: " + purchaseAfterSalesTime);

        // 3. 更新合并表信息
        ErpDistributionCombinedESDO combined = combinedOpt.get();
        combined.setAfterSalesStatus(reqVO.getAfterSalesStatus())
                .setAfterSalesTime(afterSalesTime)
                .setPurchaseAfterSalesStatus(reqVO.getPurchaseAfterSalesStatus())
                .setPurchaseAfterSalesAmount(reqVO.getPurchaseAfterSalesAmount())
                .setPurchaseAfterSalesTime(purchaseAfterSalesTime);
        distributionCombinedESRepository.save(combined);
         // 4. 同步更新数据库
         ErpDistributionCombinedDO updateDO = BeanUtils.toBean(combined, ErpDistributionCombinedDO.class);
         System.out.println("更新数据库前的DO对象 - purchaseAfterSalesTime: " + updateDO.getPurchaseAfterSalesTime());
         distributionCombinedMapper.updateById(updateDO);

         // 5. 刷新ES索引
         elasticsearchRestTemplate.indexOps(ErpDistributionCombinedESDO.class).refresh();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleAfterSales(ErpDistributionSaleAfterSalesUpdateReqVO reqVO) {
        // 1. 校验存在 - 使用合并表ES查询
        Optional<ErpDistributionCombinedESDO> combinedOpt = distributionCombinedESRepository.findById(reqVO.getId());
        if (!combinedOpt.isPresent()) {
            throw exception(DISTRIBUTION_NOT_EXISTS);
        }

        // 2. 解析时间
        LocalDateTime saleAfterSalesTime = parseDateTime(reqVO.getSaleAfterSalesTime());
        LocalDateTime afterSalesTime = parseDateTime(reqVO.getAfterSalesTime());

        // 3. 更新合并表信息
        ErpDistributionCombinedESDO combined = combinedOpt.get();
        combined.setAfterSalesStatus(reqVO.getAfterSalesStatus())
                .setAfterSalesTime(afterSalesTime)
                .setSaleAfterSalesStatus(reqVO.getSaleAfterSalesStatus())
                .setSaleAfterSalesAmount(reqVO.getSaleAfterSalesAmount())
                .setSaleAfterSalesTime(saleAfterSalesTime);
        distributionCombinedESRepository.save(combined);
        // 4. 同步更新数据库
        distributionCombinedMapper.updateById(BeanUtils.toBean(combined, ErpDistributionCombinedDO.class));

        // 5. 刷新ES索引
        elasticsearchRestTemplate.indexOps(ErpDistributionCombinedESDO.class).refresh();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseAuditStatus(Long id, Integer purchaseAuditStatus, BigDecimal otherFees) {
        // 1. 校验存在 - 使用合并表ES查询
        Optional<ErpDistributionCombinedESDO> combinedOpt = distributionCombinedESRepository.findById(id);
        if (!combinedOpt.isPresent()) {
            throw exception(DISTRIBUTION_NOT_EXISTS);
        }

        // 2. 校验状态是否重复
        ErpDistributionCombinedESDO combined = combinedOpt.get();
        if (combined.getPurchaseAuditStatus() != null &&
            combined.getPurchaseAuditStatus().equals(purchaseAuditStatus)) {
            throw exception(DISTRIBUTION_PROCESS_FAIL);
        }

        // 3. 更新合并表审核状态
        combined.setPurchaseAuditStatus(purchaseAuditStatus)
                .setPurchaseOtherFees(otherFees);

        // 设置时间
        if (purchaseAuditStatus == 20) {
            combined.setPurchaseApprovalTime(LocalDateTime.now());
        } else if (purchaseAuditStatus == 10) {
            combined.setPurchaseUnapproveTime(LocalDateTime.now());
        }

        distributionCombinedESRepository.save(combined);
         // 4. 同步更新数据库
         distributionCombinedMapper.updateById(BeanUtils.toBean(combined, ErpDistributionCombinedDO.class));

         // 5. 刷新ES索引
         elasticsearchRestTemplate.indexOps(ErpDistributionCombinedESDO.class).refresh();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleAuditStatus(Long id, Integer saleAuditStatus, BigDecimal otherFees) {
        // 1. 校验存在 - 使用合并表ES查询
        Optional<ErpDistributionCombinedESDO> combinedOpt = distributionCombinedESRepository.findById(id);
        if (!combinedOpt.isPresent()) {
            throw exception(DISTRIBUTION_NOT_EXISTS);
        }

        // 2. 校验状态是否重复
        ErpDistributionCombinedESDO combined = combinedOpt.get();
        if (combined.getSaleAuditStatus() != null &&
            combined.getSaleAuditStatus().equals(saleAuditStatus)) {
            throw exception(DISTRIBUTION_PROCESS_FAIL);
        }

        // 3. 更新合并表审核状态
        combined.setSaleAuditStatus(saleAuditStatus)
                .setSaleOtherFees(otherFees);

        // 设置时间
        if (saleAuditStatus == 20) {
            combined.setSaleApprovalTime(LocalDateTime.now());
        } else if (saleAuditStatus == 10) {
            combined.setSaleUnapproveTime(LocalDateTime.now());
        }

        distributionCombinedESRepository.save(combined);
         // 4. 同步更新数据库
         distributionCombinedMapper.updateById(BeanUtils.toBean(combined, ErpDistributionCombinedDO.class));
    }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpDistributionImportRespVO importDistributionList(List<ErpDistributionImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(DISTRIBUTION_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpDistributionImportRespVO respVO = ErpDistributionImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理数据
        List<ErpDistributionCombinedDO> createList = new ArrayList<>();
        List<ErpDistributionCombinedDO> updateList = new ArrayList<>();
        List<ErpDistributionCombinedESDO> esCreateList = new ArrayList<>();
        List<ErpDistributionCombinedESDO> esUpdateList = new ArrayList<>();

        try {
            // 批量查询组品信息
            Set<String> comboProductNos = importList.stream()
                    .map(ErpDistributionImportExcelVO::getComboProductNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> comboProductIdMap = comboProductNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(comboProductESRepository.findByNoIn(new ArrayList<>(comboProductNos)),
                            ErpComboProductES::getNo, ErpComboProductES::getId);

            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpDistributionImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpDistributionCombinedDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(distributionCombinedMapper.selectListByNoIn(noSet), ErpDistributionCombinedDO::getNo);

            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpDistributionImportExcelVO importVO = importList.get(i);
                try {
                    String username = SecurityFrameworkUtils.getLoginUsername();
                    LocalDateTime now = LocalDateTime.now();
                                    // 校验销售人员是否存在
                if (StrUtil.isNotBlank(importVO.getSalesperson())) {
                    List<ErpSalespersonRespVO> salespersons = salespersonService.searchSalespersons(
                            new ErpSalespersonPageReqVO().setSalespersonName(importVO.getSalesperson()));
                    if (CollUtil.isEmpty(salespersons)) {
                        throw exception(DISTRIBUTION_SALESPERSON_NOT_EXISTS, importVO.getSalesperson());
                    }
                }

                // 校验客户是否存在
                if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                    List<ErpCustomerSaveReqVO> customers = customerService.searchCustomers(
                            new ErpCustomerPageReqVO().setName(importVO.getCustomerName()));
                    if (CollUtil.isEmpty(customers)) {
                        throw exception(DISTRIBUTION_CUSTOMER_NOT_EXISTS, importVO.getCustomerName());
                    }
                }

                    // 获取组品ID
                    Long comboProductId = null;
                    if (StrUtil.isNotBlank(importVO.getComboProductNo())) {
                        comboProductId = comboProductIdMap.get(importVO.getComboProductNo());
                        if (comboProductId == null) {
                            throw exception(DISTRIBUTION_COMBO_PRODUCT_NOT_EXISTS, importVO.getComboProductNo());
                        }
                    }

                    // 判断是否支持更新
                    ErpDistributionCombinedDO existDistribution = existMap.get(importVO.getNo());
                    if (existDistribution == null) {
                        // 创建逻辑
                        ErpDistributionCombinedDO combined = BeanUtils.toBean(importVO, ErpDistributionCombinedDO.class).setId(IdUtil.getSnowflakeNextId()).setPurchaseAuditStatus(ErpAuditStatus.PROCESS.getStatus())  // 设置采购审核状态
                                .setSaleAuditStatus(ErpAuditStatus.PROCESS.getStatus()).setPurchaseAfterSalesStatus(30).setSaleAfterSalesStatus(30);
                        combined.setComboProductId(comboProductId);
                        if (StrUtil.isEmpty(combined.getNo())) {
                            combined.setNo(noRedisDAO.generate(ErpNoRedisDAO.DISTRIBUTION_NO_PREFIX));
                        }
                        createList.add(combined);
                        esCreateList.add(BeanUtils.toBean(combined, ErpDistributionCombinedESDO.class).setCreator(username).setCreateTime(now));
                        respVO.getCreateNames().add(combined.getNo());
                    } else if (isUpdateSupport) {
                        // 更新逻辑
                        ErpDistributionCombinedDO combined = BeanUtils.toBean(importVO, ErpDistributionCombinedDO.class);
                        combined.setId(existDistribution.getId());
                        combined.setComboProductId(comboProductId);
                        updateList.add(combined);
                        esUpdateList.add(BeanUtils.toBean(combined, ErpDistributionCombinedESDO.class));
                        respVO.getUpdateNames().add(combined.getNo());
                    }
                    else {
                        throw exception(DISTRIBUTION_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
                    }
                } catch (ServiceException ex) {
                    String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知分销订单";
                    respVO.getFailureNames().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知分销订单";
                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                distributionCombinedMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(distributionCombinedMapper::updateById);
            }

            // 批量保存到ES
            if (CollUtil.isNotEmpty(esCreateList)) {
                distributionCombinedESRepository.saveAll(esCreateList);
            }
            if (CollUtil.isNotEmpty(esUpdateList)) {
                distributionCombinedESRepository.saveAll(esUpdateList);
            }

            // 刷新ES索引
            if (CollUtil.isNotEmpty(esCreateList) || CollUtil.isNotEmpty(esUpdateList)) {
                elasticsearchRestTemplate.indexOps(ErpDistributionCombinedESDO.class).refresh();
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpDistributionImportRespVO importPurchaseAuditList(List<ErpDistributionPurchaseAuditImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(DISTRIBUTION_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpDistributionImportRespVO respVO = ErpDistributionImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpDistributionPurchaseAuditImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        if (CollUtil.isEmpty(noSet)) {
            respVO.getFailureNames().put("全部", "订单编号不能为空");
            return respVO;
        }

        // 从ES查询已存在的记录
        List<ErpDistributionCombinedESDO> existList = distributionCombinedESRepository.findByNoIn(new ArrayList<>(noSet));
        Map<String, ErpDistributionCombinedESDO> existMap = convertMap(existList, ErpDistributionCombinedESDO::getNo);

        // 批量更新数据
        List<ErpDistributionCombinedDO> updateList = new ArrayList<>();
        List<ErpDistributionCombinedESDO> esUpdateList = new ArrayList<>();

        for (ErpDistributionPurchaseAuditImportExcelVO importVO : importList) {
            try {
                // 校验订单是否存在
                ErpDistributionCombinedESDO existDistribution = existMap.get(importVO.getNo());
                if (existDistribution == null) {
                    throw exception(DISTRIBUTION_NOT_EXISTS);
                }

                // 更新采购杂费、售后审核费用和售后状况
                existDistribution.setPurchaseOtherFees(importVO.getOtherFees());
                existDistribution.setPurchaseAfterSalesAmount(importVO.getPurchaseAfterSalesAmount());
                existDistribution.setAfterSalesStatus(importVO.getAfterSalesStatus());

                // 添加到更新列表
                ErpDistributionCombinedDO updateDO = convertESToCombinedDO(existDistribution);
                updateList.add(updateDO);
                esUpdateList.add(existDistribution);

                respVO.getUpdateNames().add(importVO.getNo());
            } catch (ServiceException ex) {
                respVO.getFailureNames().put(importVO.getNo(), ex.getMessage());
            } catch (Exception ex) {
                respVO.getFailureNames().put(importVO.getNo(), "系统异常: " + ex.getMessage());
            }
        }

        // 批量更新数据库
        if (CollUtil.isNotEmpty(updateList)) {
            updateList.forEach(distributionCombinedMapper::updateById);
        }

        // 批量更新ES
        if (CollUtil.isNotEmpty(esUpdateList)) {
            distributionCombinedESRepository.saveAll(esUpdateList);
            // 刷新ES索引
            elasticsearchRestTemplate.indexOps(ErpDistributionCombinedESDO.class).refresh();
        }

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpDistributionImportRespVO importSaleAuditList(List<ErpDistributionSaleAuditImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(DISTRIBUTION_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpDistributionImportRespVO respVO = ErpDistributionImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpDistributionSaleAuditImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        if (CollUtil.isEmpty(noSet)) {
            respVO.getFailureNames().put("全部", "订单编号不能为空");
            return respVO;
        }

        // 从ES查询已存在的记录
        List<ErpDistributionCombinedESDO> existList = distributionCombinedESRepository.findByNoIn(new ArrayList<>(noSet));
        Map<String, ErpDistributionCombinedESDO> existMap = convertMap(existList, ErpDistributionCombinedESDO::getNo);

        // 批量更新数据
        List<ErpDistributionCombinedDO> updateList = new ArrayList<>();
        List<ErpDistributionCombinedESDO> esUpdateList = new ArrayList<>();

        for (ErpDistributionSaleAuditImportExcelVO importVO : importList) {
            try {
                // 校验订单是否存在
                ErpDistributionCombinedESDO existDistribution = existMap.get(importVO.getNo());
                if (existDistribution == null) {
                    throw exception(DISTRIBUTION_NOT_EXISTS);
                }

                // 更新销售杂费、销售售后金额和售后状况
                existDistribution.setSaleOtherFees(importVO.getSaleOtherFees());
                existDistribution.setSaleAfterSalesAmount(importVO.getSaleAfterSalesAmount());
                existDistribution.setAfterSalesStatus(importVO.getAfterSalesStatus());

                // 添加到更新列表
                ErpDistributionCombinedDO updateDO = convertESToCombinedDO(existDistribution);
                updateList.add(updateDO);
                esUpdateList.add(existDistribution);

                respVO.getUpdateNames().add(importVO.getNo());
            } catch (ServiceException ex) {
                respVO.getFailureNames().put(importVO.getNo(), ex.getMessage());
            } catch (Exception ex) {
                respVO.getFailureNames().put(importVO.getNo(), "系统异常: " + ex.getMessage());
            }
        }

        // 批量更新数据库
        if (CollUtil.isNotEmpty(updateList)) {
            updateList.forEach(distributionCombinedMapper::updateById);
        }

        // 批量更新ES
        if (CollUtil.isNotEmpty(esUpdateList)) {
            distributionCombinedESRepository.saveAll(esUpdateList);
            // 刷新ES索引
            elasticsearchRestTemplate.indexOps(ErpDistributionCombinedESDO.class).refresh();
        }

        return respVO;
    }

    /**
     * 创建组品表风格的搜索查询 - 完全使用组品表的搜索策略和权重
     * 
     * @param fieldName 字段名（用于分词搜索）
     * @param keywordFieldName keyword字段名（用于精确匹配）
     * @param keyword 关键词
     * @return 组品表风格的搜索查询
     */
    private BoolQueryBuilder createComboStyleMatchQuery(String fieldName, String keywordFieldName, String keyword) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        
        BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
        // 第一优先级：完全精确匹配（权重最高）
        multiMatchQuery.should(QueryBuilders.termQuery(keywordFieldName, keyword).boost(1000000.0f));
        // 第二优先级：前缀匹配
        multiMatchQuery.should(QueryBuilders.prefixQuery(keywordFieldName, keyword).boost(100000.0f));
        // 第三优先级：通配符包含匹配
        multiMatchQuery.should(QueryBuilders.wildcardQuery(keywordFieldName, "*" + keyword + "*").boost(10000.0f));

        // 第四优先级：对于多字搜索，添加子字符串通配符匹配
        if (keyword.length() >= 2) {
            for (int i = 1; i < keyword.length(); i++) {
                String substring = keyword.substring(i);
                if (substring.length() >= 4 && !containsTooManyRepeatedChars(substring)) { // 避免重复字符过多的子字符串
                    multiMatchQuery.should(QueryBuilders.wildcardQuery(keywordFieldName, "*" + substring + "*").boost(3000.0f));
                }
            }
        }

        // 第五优先级：智能分词匹配
        if (keyword.length() == 1) {
            // 单字搜索
            multiMatchQuery.should(QueryBuilders.matchQuery(fieldName, keyword).operator(Operator.OR).boost(800.0f));
        } else if (keyword.length() == 2) {
            // 双字搜索，使用AND匹配避免误匹配，但也添加OR匹配作为兜底
            multiMatchQuery.should(QueryBuilders.matchQuery(fieldName, keyword).operator(Operator.AND).boost(600.0f));
            multiMatchQuery.should(QueryBuilders.matchPhraseQuery(fieldName, keyword).boost(1200.0f));
            // 添加OR匹配作为兜底，权重较低
            multiMatchQuery.should(QueryBuilders.matchQuery(fieldName, keyword).operator(Operator.OR).boost(400.0f));
        } else {
            // 多字搜索
            multiMatchQuery.should(QueryBuilders.matchQuery(fieldName, keyword).operator(Operator.AND).boost(500.0f));
            multiMatchQuery.should(QueryBuilders.matchPhraseQuery(fieldName, keyword).boost(1000.0f));
        }

        multiMatchQuery.minimumShouldMatch(1);
        query.must(multiMatchQuery);
        return query;
    }

    /**
     * 创建智能编号搜索查询 - 完全使用智能编号搜索策略
     * 
     * @param fieldName 字段名（用于分词搜索）
     * @param keywordFieldName keyword字段名（用于精确匹配）
     * @param keyword 关键词
     * @return 智能编号搜索查询
     */
    private BoolQueryBuilder createIntelligentNumberMatchQuery(String fieldName, String keywordFieldName, String keyword) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        
        BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
        // 第一优先级：完全精确匹配（权重最高）
        multiMatchQuery.should(QueryBuilders.termQuery(keywordFieldName, keyword).boost(1000000.0f));
        // 第二优先级：前缀匹配
        multiMatchQuery.should(QueryBuilders.prefixQuery(keywordFieldName, keyword).boost(100000.0f));
        // 第三优先级：通配符包含匹配
        multiMatchQuery.should(QueryBuilders.wildcardQuery(keywordFieldName, "*" + keyword + "*").boost(10000.0f));

        // 第四优先级：超智能子字符串匹配
        if (keyword.length() >= 2) {
            if (keyword.length() <= 6) {
                // 短查询词：优化子字符串匹配，避免过短子字符串的误匹配
                for (int i = 1; i < keyword.length(); i++) {
                    String substring = keyword.substring(i);
                    if (substring.length() >= 4 && !containsTooManyRepeatedChars(substring)) { // 避免重复字符过多的子字符串
                        multiMatchQuery.should(QueryBuilders.wildcardQuery(keywordFieldName, "*" + substring + "*").boost(5000.0f));
                    }
                }
                // 前缀子字符串匹配
                for (int i = 0; i < keyword.length() - 1; i++) {
                    String prefix = keyword.substring(0, keyword.length() - i);
                    if (prefix.length() >= 4) { // 进一步提高最小长度要求到4
                        multiMatchQuery.should(QueryBuilders.wildcardQuery(keywordFieldName, "*" + prefix + "*").boost(4000.0f));
                    }
                }
            } else if (keyword.length() <= 10) {
                // 中等长度：主要使用前缀匹配，减少后缀匹配的误匹配
                System.out.println("使用中等长度策略（7-10字符）");
                
                // 前缀匹配（去掉最后几位）- 这是相对安全的匹配方式
                for (int i = 1; i <= 2 && i < keyword.length(); i++) { // 减少前缀匹配的数量
                    String prefix = keyword.substring(0, keyword.length() - i);
                    if (prefix.length() >= 6) { // 提高前缀最小长度要求
                        multiMatchQuery.should(QueryBuilders.prefixQuery(keywordFieldName, prefix).boost(4000.0f));
                        System.out.println("  添加部分前缀: " + prefix + ", 权重: 4000");
                    }
                }
                
                // 后缀匹配 - 只在查询词较短且不包含太多重复数字时使用
                if (keyword.length() <= 8 && !containsTooManyRepeatedDigits(keyword)) {
                    for (int i = Math.max(0, keyword.length() - 4); i < keyword.length() - 1; i++) { // 减少后缀匹配范围
                        String suffix = keyword.substring(i);
                        if (suffix.length() >= 5) { // 提高后缀最小长度要求
                            multiMatchQuery.should(QueryBuilders.wildcardQuery(keywordFieldName, "*" + suffix).boost(2000.0f)); // 降低权重
                            System.out.println("  添加后缀: *" + suffix + ", 权重: 2000");
                        }
                    }
                } else {
                    System.out.println("  跳过后缀匹配（查询词过长或包含重复数字）");
                }
            } else if (keyword.length() <= 15) {
                // 较长查询词：主要支持前缀匹配，极少的后缀匹配
                multiMatchQuery.should(QueryBuilders.prefixQuery(keywordFieldName, keyword).boost(50000.0f));
                
                // 非常有限的后缀匹配（只匹配最后4位）
                if (keyword.length() >= 4) {
                    String lastFour = keyword.substring(keyword.length() - 4);
                    multiMatchQuery.should(QueryBuilders.wildcardQuery(keywordFieldName, "*" + lastFour).boost(2000.0f));
                }
            } else {
                // 很长的查询词（>15字符）：几乎只使用前缀匹配
                multiMatchQuery.should(QueryBuilders.prefixQuery(keywordFieldName, keyword).boost(90000.0f));
                
                // 对于完整编号长度，完全禁用后缀匹配
                if (keyword.length() < 20) {
                    // 只对不完整的长查询词添加后缀匹配
                    String lastThree = keyword.substring(keyword.length() - 3);
                    multiMatchQuery.should(QueryBuilders.wildcardQuery(keywordFieldName, "*" + lastThree).boost(800.0f));
                }
            }
        }

        // 第五优先级：智能分词匹配
        if (keyword.length() == 1) {
            // 单字搜索
            multiMatchQuery.should(QueryBuilders.matchQuery(fieldName, keyword).operator(Operator.OR).boost(800.0f));
        } else if (keyword.length() == 2) {
            // 双字搜索，使用AND匹配避免误匹配，但也添加OR匹配作为兜底
            multiMatchQuery.should(QueryBuilders.matchQuery(fieldName, keyword).operator(Operator.AND).boost(600.0f));
            multiMatchQuery.should(QueryBuilders.matchPhraseQuery(fieldName, keyword).boost(1200.0f));
            // 添加OR匹配作为兜底，权重较低
            multiMatchQuery.should(QueryBuilders.matchQuery(fieldName, keyword).operator(Operator.OR).boost(400.0f));
        } else {
            // 多字搜索
            multiMatchQuery.should(QueryBuilders.matchQuery(fieldName, keyword).operator(Operator.AND).boost(500.0f));
            multiMatchQuery.should(QueryBuilders.matchPhraseQuery(fieldName, keyword).boost(1000.0f));
        }

        multiMatchQuery.minimumShouldMatch(1);
        query.must(multiMatchQuery);
        return query;
    }

    private boolean containsTooManyRepeatedDigits(String str) {
        int digitCount = 0;
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                digitCount++;
                if (digitCount > 3) {
                    return true;
                }
            } else {
                digitCount = 0;
            }
        }
        return false;
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
}
