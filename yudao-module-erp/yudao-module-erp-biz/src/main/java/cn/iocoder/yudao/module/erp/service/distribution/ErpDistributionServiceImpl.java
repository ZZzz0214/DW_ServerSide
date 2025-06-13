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

//     // 初始化ES索引
//     @EventListener(ApplicationReadyEvent.class)
//     public void initESIndex() {
//         System.out.println("开始初始化代发订单ES索引...");
//         try {
//             // 初始化基础表索引
//             IndexOperations baseIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionBaseESDO.class);
//             if (!baseIndexOps.exists()) {
//                 baseIndexOps.create();
//                 baseIndexOps.putMapping(baseIndexOps.createMapping(ErpDistributionBaseESDO.class));
//                 System.out.println("代发基础表索引创建成功");
//             }
//
//             // 初始化采购表索引
//             IndexOperations purchaseIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionPurchaseESDO.class);
//             if (!purchaseIndexOps.exists()) {
//                 purchaseIndexOps.create();
//                 purchaseIndexOps.putMapping(purchaseIndexOps.createMapping(ErpDistributionPurchaseESDO.class));
//                 System.out.println("代发采购表索引创建成功");
//             }
//
//             // 初始化销售表索引
//             IndexOperations saleIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionSaleESDO.class);
//             if (!saleIndexOps.exists()) {
//                 saleIndexOps.create();
//                 saleIndexOps.putMapping(saleIndexOps.createMapping(ErpDistributionSaleESDO.class));
//                 System.out.println("代发销售表索引创建成功");
//             }
//         } catch (Exception e) {
//             System.err.println("代发订单索引初始化失败: " + e.getMessage());
//         }
//     }
//
//       // 同步基础表数据到ES
//    private void syncBaseToES(Long baseId) {
//        ErpDistributionBaseDO base = distributionMapper.selectById(baseId);
//        if (base == null) {
//            distributionBaseESRepository.deleteById(baseId);
//        } else {
//            ErpDistributionBaseESDO es = convertBaseToES(base);
//            distributionBaseESRepository.save(es);
//
//        }
//    }
//
//    // 同步采购表数据到ES
//    private void syncPurchaseToES(Long purchaseId) {
//        ErpDistributionPurchaseDO purchase = purchaseMapper.selectById(purchaseId);
//        if (purchase == null) {
//            distributionPurchaseESRepository.deleteByBaseId(purchaseId);
//        } else {
//            ErpDistributionPurchaseESDO es = convertPurchaseToES(purchase);
//            distributionPurchaseESRepository.save(es);
//        }
//    }
//
//    // 同步销售表数据到ES
//    private void syncSaleToES(Long saleId) {
//        ErpDistributionSaleDO sale = saleMapper.selectById(saleId);
//        if (sale == null) {
//            distributionSaleESRepository.deleteByBaseId(saleId);
//        } else {
//            ErpDistributionSaleESDO es = convertSaleToES(sale);
//            distributionSaleESRepository.save(es);
//        }
//    }
//
//    // 转换方法
//    private ErpDistributionBaseESDO convertBaseToES(ErpDistributionBaseDO base) {
//        ErpDistributionBaseESDO es = new ErpDistributionBaseESDO();
//        BeanUtils.copyProperties(base, es);
//        return es;
//    }
//
//    private ErpDistributionPurchaseESDO convertPurchaseToES(ErpDistributionPurchaseDO purchase) {
//        ErpDistributionPurchaseESDO es = new ErpDistributionPurchaseESDO();
//        BeanUtils.copyProperties(purchase, es);
//        return es;
//    }
//
//    private ErpDistributionSaleESDO convertSaleToES(ErpDistributionSaleDO sale) {
//        ErpDistributionSaleESDO es = new ErpDistributionSaleESDO();
//        BeanUtils.copyProperties(sale, es);
//        return es;
//    }

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


    // 全量同步方法
//    @Async
//    public void fullSyncToES() {
//        try {
//            // 同步基础表
//            List<ErpDistributionBaseDO> bases = distributionMapper.selectList(null);
//            if (CollUtil.isNotEmpty(bases)) {
//                List<ErpDistributionBaseESDO> baseESList = bases.stream()
//                        .map(this::convertBaseToES)
//                        .collect(Collectors.toList());
//                distributionBaseESRepository.saveAll(baseESList);
//            }
//
//            // 同步采购表
//            List<ErpDistributionPurchaseDO> purchases = purchaseMapper.selectList(null);
//            if (CollUtil.isNotEmpty(purchases)) {
//                List<ErpDistributionPurchaseESDO> purchaseESList = purchases.stream()
//                        .map(this::convertPurchaseToES)
//                        .collect(Collectors.toList());
//                distributionPurchaseESRepository.saveAll(purchaseESList);
//            }
//
//            // 同步销售表
//            List<ErpDistributionSaleDO> sales = saleMapper.selectList(null);
//            if (CollUtil.isNotEmpty(sales)) {
//                List<ErpDistributionSaleESDO> saleESList = sales.stream()
//                        .map(this::convertSaleToES)
//                        .collect(Collectors.toList());
//                distributionSaleESRepository.saveAll(saleESList);
//            }
//
//            System.out.println("代发订单全量同步ES数据完成");
//        } catch (Exception e) {
//            System.err.println("代发订单全量同步ES数据失败: " + e.getMessage());
//        }
//    }
    // 全量同步方法 - 修改为只同步合并后的代发表
    @Async
    public void fullSyncToES() {
        try {
            // 同步合并后的代发表
            List<ErpDistributionCombinedDO> combinedList = distributionCombinedMapper.selectCombinedList(null);
            if (CollUtil.isNotEmpty(combinedList)) {
                List<ErpDistributionCombinedESDO> combinedESList = combinedList.stream()
                        .map(this::convertCombinedToES)
                        .collect(Collectors.toList());
                elasticsearchRestTemplate.save(combinedESList);
            }
            System.out.println("代发订单全量同步ES数据完成");
        } catch (Exception e) {
            System.err.println("代发订单全量同步ES数据失败: " + e.getMessage());
        }
    }
    // 新增转换方法
    private ErpDistributionCombinedESDO convertCombinedToES(ErpDistributionCombinedDO combinedDO) {
        ErpDistributionCombinedESDO esDO = new ErpDistributionCombinedESDO();
        BeanUtils.copyProperties(combinedDO, esDO);
        return esDO;
    }




//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public Long createDistribution(ErpDistributionSaveReqVO createReqVO) {
//        // 1. 校验数据
//        validateDistributionForCreateOrUpdate(null, createReqVO);
//
//        // 2. 生成代发单号，并校验唯一性
//        String no = noRedisDAO.generate(ErpNoRedisDAO.DISTRIBUTION_NO_PREFIX);
//        ErpDistributionBaseESDO existing = distributionBaseESRepository.findByNo(no);
//        if (existing != null) {
//            throw exception(DISTRIBUTION_NO_EXISTS);
//        }
//        LocalDateTime afterSalesTime = parseDateTime(createReqVO.getAfterSalesTime());
//
//        // 3. 生成ID
//        Long id = IdUtil.getSnowflakeNextId();
//
//        // 4. 保存基础信息到ES
//        ErpDistributionBaseESDO baseES = BeanUtils.toBean(createReqVO, ErpDistributionBaseESDO.class)
//                .setId(id)
//                .setNo(no)
//                .setStatus(ErpAuditStatus.PROCESS.getStatus())
//                .setAfterSalesTime(afterSalesTime);
//        distributionBaseESRepository.save(baseES);
//
//        Long id2 = IdUtil.getSnowflakeNextId();
//        // 5. 保存采购信息到ES
//        ErpDistributionPurchaseESDO purchaseES = BeanUtils.toBean(createReqVO, ErpDistributionPurchaseESDO.class)
//                .setId(id2)
//                .setBaseId(baseES.getId())
//                .setPurchaseAuditStatus(ErpAuditStatus.PROCESS.getStatus())
//                .setPurchaseAfterSalesStatus(30);
//        distributionPurchaseESRepository.save(purchaseES);
//        Long id3 = IdUtil.getSnowflakeNextId();
//
//        // 6. 保存销售信息到ES
//        ErpDistributionSaleESDO saleES = BeanUtils.toBean(createReqVO, ErpDistributionSaleESDO.class)
//                .setId(id3)
//                .setBaseId(baseES.getId())
//                .setSaleAuditStatus(ErpAuditStatus.PROCESS.getStatus())
//                .setSaleAfterSalesStatus(30)
//                .setShippingFee(createReqVO.getSaleShippingFee())
//                .setOtherFees(createReqVO.getSaleOtherFees());
//        distributionSaleESRepository.save(saleES);
//
//        return id;
//    }

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

        return id;
    }





//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateDistribution(ErpDistributionSaveReqVO updateReqVO) {
//        // 1.1 校验存在 - 从ES查询
//        Optional<ErpDistributionBaseESDO> baseOpt = distributionBaseESRepository.findById(updateReqVO.getId());
//        if (!baseOpt.isPresent()) {
//            throw exception(DISTRIBUTION_NOT_EXISTS);
//        }
//        ErpDistributionBaseESDO distribution = baseOpt.get();
//
//        if (ErpAuditStatus.APPROVE.getStatus().equals(distribution.getStatus())) {
//            throw exception(DISTRIBUTION_UPDATE_FAIL_APPROVE, distribution.getNo());
//        }
//
//        // 1.2 校验数据
//        validateDistributionForCreateOrUpdate(updateReqVO.getId(), updateReqVO);
//
//        // 2. 更新基础信息到ES
//        ErpDistributionBaseESDO baseES = BeanUtils.toBean(updateReqVO, ErpDistributionBaseESDO.class)
//                .setId(distribution.getId())
//                .setNo(distribution.getNo())
//                .setStatus(distribution.getStatus());
//        distributionBaseESRepository.save(baseES);
//
//        // 3. 更新采购信息到ES（独立检查审核状态）
//        Optional<ErpDistributionPurchaseESDO> purchaseOpt = distributionPurchaseESRepository.findByBaseId(updateReqVO.getId());
//        if (purchaseOpt.isPresent()) {
//            ErpDistributionPurchaseESDO purchase = purchaseOpt.get();
//            if (!ErpAuditStatus.APPROVE.getStatus().equals(purchase.getPurchaseAuditStatus())) {
//                ErpDistributionPurchaseESDO purchaseES = BeanUtils.toBean(updateReqVO, ErpDistributionPurchaseESDO.class)
//                        .setId(purchase.getId())
//                        .setBaseId(updateReqVO.getId());
//                distributionPurchaseESRepository.save(purchaseES);
//            }
//        }
//
//        // 4. 更新销售信息到ES（独立检查审核状态）
//        Optional<ErpDistributionSaleESDO> saleOpt = distributionSaleESRepository.findByBaseId(updateReqVO.getId());
//        if (saleOpt.isPresent()) {
//            ErpDistributionSaleESDO sale = saleOpt.get();
//            if (!ErpAuditStatus.APPROVE.getStatus().equals(sale.getSaleAuditStatus())) {
//                ErpDistributionSaleESDO saleES = BeanUtils.toBean(updateReqVO, ErpDistributionSaleESDO.class)
//                        .setId(sale.getId())
//                        .setBaseId(updateReqVO.getId())
//                        .setShippingFee(updateReqVO.getSaleShippingFee())
//                        .setOtherFees(updateReqVO.getSaleOtherFees())
//                        .setSaleAuditStatus(sale.getSaleAuditStatus());
//                distributionSaleESRepository.save(saleES);
//            }
//        }
//    }

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
    }


//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void deleteDistribution(List<Long> ids) {
//        if (CollUtil.isEmpty(ids)) {
//            return;
//        }
//        // 1. 校验存在 - 从ES查询
//        Iterable<ErpDistributionBaseESDO> distributions = distributionBaseESRepository.findAllById(ids);
//        if (!distributions.iterator().hasNext()) {
//            throw exception(DISTRIBUTION_NOT_EXISTS);
//        }
//
//        // 2. 从ES删除
//
//        // 2. 批量从ES删除
//        distributionBaseESRepository.deleteAllById(ids);
//        distributionPurchaseESRepository.deleteAllByBaseIdIn(ids);
//        distributionSaleESRepository.deleteAllByBaseIdIn(ids);
//    }

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

//    @Override
//    public ErpDistributionBaseDO getDistribution(Long id) {
//        Optional<ErpDistributionBaseESDO> esDO = distributionBaseESRepository.findById(id);
//        return esDO.map(this::convertESToBaseDO).orElse(null);
//    }

    @Override
    public ErpDistributionRespVO getDistribution(Long id) {
        // 1. 从合并ES表查询数据
        Optional<ErpDistributionCombinedESDO> combinedOpt = distributionCombinedESRepository.findById(id);
        if (!combinedOpt.isPresent()) {
            return null;
        }
        ErpDistributionCombinedESDO combined = combinedOpt.get();

        // 2. 转换为RespVO
        ErpDistributionRespVO respVO = BeanUtils.toBean(combined, ErpDistributionRespVO.class).setOtherFees(combined.getPurchaseOtherFees());

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

    // 新增转换方法
    private ErpDistributionCombinedDO convertESToCombinedDO(ErpDistributionCombinedESDO esDO) {
        ErpDistributionCombinedDO combinedDO = new ErpDistributionCombinedDO();
        BeanUtils.copyProperties(esDO, combinedDO);
        return combinedDO;
    }


//    @Override
//    public PageResult<ErpDistributionRespVO> getDistributionVOPage(ErpDistributionPageReqVO pageReqVO) {
//        try {
//        // 1. 检查ES索引是否存在，不存在则创建
//        IndexOperations baseIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionBaseESDO.class);
//        if (!baseIndexOps.exists()) {
//            baseIndexOps.create();
//            baseIndexOps.putMapping(baseIndexOps.createMapping(ErpDistributionBaseESDO.class));
//        }
//
//        IndexOperations purchaseIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionPurchaseESDO.class);
//        if (!purchaseIndexOps.exists()) {
//            purchaseIndexOps.create();
//            purchaseIndexOps.putMapping(purchaseIndexOps.createMapping(ErpDistributionPurchaseESDO.class));
//        }
//
//        IndexOperations saleIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionSaleESDO.class);
//        if (!saleIndexOps.exists()) {
//            saleIndexOps.create();
//            saleIndexOps.putMapping(saleIndexOps.createMapping(ErpDistributionSaleESDO.class));
//        }
//
//            // 1. 构建基础查询条件
//            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
//                    .withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()))
//                    .withTrackTotalHits(true)
//                    .withSort(Sort.by(Sort.Direction.DESC, "id")); // 按照 ID 降序排序
//
//
//            // 2. 如果是深度分页(超过10000条)，使用search_after
//            if (pageReqVO.getPageNo() > 1) {
//                return handleDeepPagination(pageReqVO, queryBuilder);
//            }
//
//             // 3. 普通分页处理
//             SearchHits<ErpDistributionBaseESDO> searchHits = elasticsearchRestTemplate.search(
//                queryBuilder.build(),
//                ErpDistributionBaseESDO.class,
//                IndexCoordinates.of("erp_distribution_base"));
//                System.out.println("查询结果总数: " + searchHits.getTotalHits());
//                System.out.println("查询到的文档数量: " + searchHits.getSearchHits().size());
//
//        List<ErpDistributionRespVO> voList = searchHits.stream()
//                .map(SearchHit::getContent)
//                .peek(esDO -> {
//                    System.out.println("处理基础记录 - ID: " + esDO.getId() + " (类型: " +
//                        (esDO.getId() != null ? esDO.getId().getClass().getName() : "null") + ")");
//                    System.out.println("基础记录完整内容: " + esDO);
//                })
//                .map(esDO -> {
//                    ErpDistributionRespVO vo = BeanUtils.toBean(esDO, ErpDistributionRespVO.class);
//                    System.out.println("转换后的VO基础信息: " + vo);
//                    System.out.println("准备查询采购信息，baseId=" + esDO.getId() + " (类型: " +
//                (esDO.getId() != null ? esDO.getId().getClass().getName() : "null") + ")");
//                    // 从ES查询采购信息（通过baseId匹配）
//                    Optional<ErpDistributionPurchaseESDO> purchaseOpt = pageReqVO.getPurchaseAuditStatus() != null
//                    ? distributionPurchaseESRepository.findByBaseIdAndPurchaseAuditStatus(esDO.getId(), pageReqVO.getPurchaseAuditStatus())
//                    : distributionPurchaseESRepository.findByBaseId(esDO.getId());
//
//
//                    // ... existing code ...
//
//                    System.out.println("采购信息查询结果: " + purchaseOpt);
//                    if (purchaseOpt.isPresent()) {
//                        System.out.println("采购记录完整内容: " + purchaseOpt.get());
//                    }
//                     // 如果有采购审核状态条件但找不到匹配记录，则返回null
//                    if (pageReqVO.getPurchaseAuditStatus() != null && !purchaseOpt.isPresent()) {
//                        return null;
//                    }
//                    if (purchaseOpt.isPresent()) {
//                        ErpDistributionPurchaseESDO purchase = purchaseOpt.get();
//                        vo.setPurchaseAuditStatus(purchase.getPurchaseAuditStatus());
//                        vo.setOtherFees(purchase.getOtherFees());
//                        vo.setPurchaseRemark(purchase.getPurchaseRemark());
//                        vo.setPurchaseAfterSalesSituation(purchase.getPurchaseAfterSalesSituation());
//                        vo.setPurchaseAfterSalesStatus(purchase.getPurchaseAfterSalesStatus());
//                        vo.setPurchaseAfterSalesAmount(purchase.getPurchaseAfterSalesAmount());
//
//                        // 从ES查询组品信息
//                        if (purchase.getComboProductId() != null) {
//                            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(purchase.getComboProductId());
//                            if (comboProductOpt.isPresent()) {
//                                ErpComboProductES comboProduct = comboProductOpt.get();
//                                vo.setProductName(comboProduct.getName());
//                                vo.setShippingCode(comboProduct.getShippingCode());
//                                vo.setPurchaser(comboProduct.getPurchaser());
//                                vo.setSupplier(comboProduct.getSupplier());
//                                vo.setPurchasePrice(comboProduct.getPurchasePrice());
//                                vo.setComboProductNo(comboProduct.getNo());
//
//                                // 计算运费和采购总额
//                                calculatePurchaseAmount(vo, comboProduct, purchase);
//                            }
//                        }
//                    }
//                    System.out.println("准备查询销售信息，baseId=" + esDO.getId() + " (类型: " +
//                    (esDO.getId() != null ? esDO.getId().getClass().getName() : "null") + ")");
//
//                    // 从ES查询销售信息（通过baseId匹配）
//                    Optional<ErpDistributionSaleESDO> saleOpt = pageReqVO.getSaleAuditStatus() != null
//                    ? distributionSaleESRepository.findByBaseIdAndSaleAuditStatus(esDO.getId(), pageReqVO.getSaleAuditStatus())
//                    : distributionSaleESRepository.findByBaseId(esDO.getId());
//                    System.out.println("销售信息查询结果: " + saleOpt);
//                    if (saleOpt.isPresent()) {
//                        System.out.println("销售记录完整内容: " + saleOpt.get());
//                    }
//                    // 如果有销售审核状态条件但找不到匹配记录，则返回null
//                    if (pageReqVO.getSaleAuditStatus() != null && !saleOpt.isPresent()) {
//                        return null;
//                    }
//                    if (saleOpt.isPresent()) {
//                        ErpDistributionSaleESDO sale = saleOpt.get();
//                        vo.setSalesperson(sale.getSalesperson());
//                        vo.setCustomerName(sale.getCustomerName());
//                        vo.setSaleOtherFees(sale.getOtherFees());
//                        vo.setTransferPerson(sale.getTransferPerson());
//                        vo.setSaleAfterSalesStatus(sale.getSaleAfterSalesStatus());
//                        vo.setSaleAfterSalesSituation(sale.getSaleAfterSalesSituation());
//                        vo.setSaleAfterSalesAmount(sale.getSaleAfterSalesAmount());
//                        vo.setSaleAfterSalesTime(sale.getSaleAfterSalesTime());
//                        vo.setSaleRemark(sale.getSaleRemark());
//                        vo.setSaleAuditStatus(sale.getSaleAuditStatus());
//
//                        // 计算销售总额
//                        if (purchaseOpt.isPresent() && sale.getCustomerName() != null) {
//                            calculateSaleAmount(vo, purchaseOpt.get(), sale);
//                        }
//                    }
//
//                    return vo;
//                })
//                .collect(Collectors.toList());
//
//        return new PageResult<>(voList, searchHits.getTotalHits());
//
//    } catch (Exception e) {
//        System.out.println("ES查询失败，回退到数据库查询: " + e.getMessage());
//        // 返回空的分页结果而不是null
//        return new PageResult<>(Collections.emptyList(), 0L);
//    }
//    }

    @Override
    public PageResult<ErpDistributionRespVO> getDistributionVOPage(ErpDistributionPageReqVO pageReqVO) {
        try {
            System.out.println("代发传入的参数"+pageReqVO);
            // 1. 检查数据库是否有数据
            long dbCount = distributionCombinedMapper.selectCount(null);

            // 2. 检查ES索引是否存在
            IndexOperations combinedIndexOps = elasticsearchRestTemplate.indexOps(ErpDistributionCombinedESDO.class);
            boolean indexExists = combinedIndexOps.exists();
            System.out.println("索引存不存在"+indexExists);

            // 3. 检查ES数据量
            long esCount = 0;
            if (indexExists) {
                esCount = elasticsearchRestTemplate.count(
                    new NativeSearchQueryBuilder().build(),
                    ErpDistributionCombinedESDO.class
                );
            }
            System.out.println("数据库数量"+dbCount);
            System.out.println("es数量"+esCount);

            // 4. 处理数据库和ES数据不一致的情况
            if (dbCount == 0) {
                if (indexExists && esCount > 0) {
                    // 数据库为空但ES有数据，清空ES
                    distributionCombinedESRepository.deleteAll();
                    System.out.println("检测到数据库为空但ES有数据，已清空ES索引");
                }
                return new PageResult<>(Collections.emptyList(), 0L);
            }

            // 5. 如果索引不存在或数据不一致，重建索引
            if (!indexExists || esCount != dbCount) {
                System.out.println("调用了！！！");
                combinedIndexOps.create();
                combinedIndexOps.putMapping(combinedIndexOps.createMapping(ErpDistributionCombinedESDO.class));
                // 这里可以添加全量同步逻辑
                fullSyncToES();
                System.out.println("检测到ES索引不存在或数据不一致，已重建索引");
            }

            // 2. 构建基础查询条件
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()))
                    .withTrackTotalHits(true)
                    .withSort(Sort.by(Sort.Direction.DESC, "id"));

            // 3. 添加查询条件
//            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//            if (StringUtils.isNotBlank(pageReqVO.getNo())) {
//                boolQuery.must(QueryBuilders.matchQuery("no", pageReqVO.getNo()));
//            }
//            if (StringUtils.isNotBlank(pageReqVO.getLogisticsCompany())) {
//                boolQuery.must(QueryBuilders.matchQuery("logisticsCompany", pageReqVO.getLogisticsCompany()));
//            }
//            if (StringUtils.isNotBlank(pageReqVO.getTrackingNumber())) {
//                boolQuery.must(QueryBuilders.matchQuery("trackingNumber", pageReqVO.getTrackingNumber()));
//            }
//            if (pageReqVO.getPurchaseAuditStatus() != null) {
//                boolQuery.must(QueryBuilders.termQuery("purchaseAuditStatus", pageReqVO.getPurchaseAuditStatus()));
//            }
//            if (pageReqVO.getSaleAuditStatus() != null) {
//                boolQuery.must(QueryBuilders.termQuery("saleAuditStatus", pageReqVO.getSaleAuditStatus()));
//            }
//            if (pageReqVO.getPurchaseAuditStatus() != null) {
//                boolQuery.must(QueryBuilders.termQuery("purchaseAuditStatus", pageReqVO.getPurchaseAuditStatus()));
//            }
//            if (pageReqVO.getSaleAuditStatus() != null) {
//                boolQuery.must(QueryBuilders.termQuery("saleAuditStatus", pageReqVO.getSaleAuditStatus()));
//            }
//            queryBuilder.withQuery(boolQuery);
            if (pageReqVO.getPageNo() > 1) {
                return handleDeepPagination(pageReqVO, queryBuilder);
            }


            // 4. 执行查询
            SearchHits<ErpDistributionCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpDistributionCombinedESDO.class,
                    IndexCoordinates.of("erp_distribution_combined"));
                    System.out.println("查询代发结果总数: " + searchHits.getTotalHits());
            // 5. 转换为VO并计算金额
            List<ErpDistributionRespVO> voList = searchHits.stream()
                    .map(SearchHit::getContent)
                    .map(combined -> {
                        ErpDistributionRespVO vo = BeanUtils.toBean(combined, ErpDistributionRespVO.class);
                        //System.out.println("转换后的VO基础信息: " + vo);
                        // 查询组品信息
                        if (combined.getComboProductId() != null) {
                           // System.out.println("准备查询组品信息，comboProductId=" + combined.getComboProductId());
                            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combined.getComboProductId());
                            if (comboProductOpt.isPresent()) {
                                ErpComboProductES comboProduct = comboProductOpt.get();
                             //   System.out.println("找到组品信息: " + comboProduct);
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
                               // System.out.println("计算采购运费: " + shippingFee);
//System.out.println("计算采购总额: " + totalPurchaseAmount);

                                // 计算销售运费和总额
                                if (combined.getCustomerName() != null) {
                                  ///  System.out.println("准备查询销售价格，comboProductId=" + combined.getComboProductId() +
                                  //          ", customerName=" + combined.getCustomerName());
                                    Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                                            combined.getComboProductId(), combined.getCustomerName());
                                    if (salePriceOpt.isPresent()) {
                                     //   System.out.println("找到销售价格记录: " + salePriceOpt.get());
                                        BigDecimal saleShippingFee = calculateSaleShippingFee(salePriceOpt.get(), vo.getProductQuantity(), combined.getComboProductId());
                                        BigDecimal totalSaleAmount = salePriceOpt.get().getDistributionPrice()
                                                .multiply(new BigDecimal(vo.getProductQuantity()))
                                                .add(saleShippingFee)
                                                .add(combined.getSaleOtherFees() != null ? combined.getSaleOtherFees() : BigDecimal.ZERO);
                                        vo.setSalePrice(salePriceOpt.get().getDistributionPrice());
                                        vo.setSaleShippingFee(saleShippingFee);
                                        vo.setTotalSaleAmount(totalSaleAmount);
                                      //  System.out.println("计算销售运费: " + saleShippingFee);
                                      //  System.out.println("计算销售总额: " + totalSaleAmount);
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


//    private PageResult<ErpDistributionRespVO> handleDeepPagination(ErpDistributionPageReqVO pageReqVO,
//                                                                   NativeSearchQueryBuilder queryBuilder) {
//        // 1. 计算需要跳过的记录数
//        int skip = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();
//
//        // 2. 使用search_after直接获取目标页
//        NativeSearchQuery query = queryBuilder.build();
//
//        // 设置分页参数
//        query.setPageable(PageRequest.of(0, pageReqVO.getPageSize()));
//
//        // 如果是深度分页，使用search_after
//        if (skip > 0) {
//            // 先获取前一页的最后一条记录
//            NativeSearchQuery prevQuery = queryBuilder.build();
//            prevQuery.setPageable(PageRequest.of(pageReqVO.getPageNo() - 2, 1));
//
//            SearchHits<ErpDistributionBaseESDO> prevHits = elasticsearchRestTemplate.search(
//                    prevQuery,
//                    ErpDistributionBaseESDO.class,
//                    IndexCoordinates.of("erp_distribution_base"));
//
//            if (prevHits.isEmpty()) {
//                return new PageResult<>(Collections.emptyList(), prevHits.getTotalHits());
//            }
//
//            // 设置search_after参数
//            SearchHit<ErpDistributionBaseESDO> lastHit = prevHits.getSearchHits().get(0);
//            query.setSearchAfter(lastHit.getSortValues());
//        }
//
//        // 3. 执行查询
//        SearchHits<ErpDistributionBaseESDO> searchHits = elasticsearchRestTemplate.search(
//                query,
//                ErpDistributionBaseESDO.class,
//                IndexCoordinates.of("erp_distribution_base"));
//
//               // 4. 转换为VO并补充关联数据
//        List<ErpDistributionRespVO> voList = searchHits.stream()
//        .map(SearchHit::getContent)
//        .map(esDO -> {
//            ErpDistributionRespVO vo = BeanUtils.toBean(esDO, ErpDistributionRespVO.class);
//
//            // 从ES查询采购信息（通过baseId匹配）
//            Optional<ErpDistributionPurchaseESDO> purchaseOpt = pageReqVO.getPurchaseAuditStatus() != null
//        ? distributionPurchaseESRepository.findByBaseIdAndPurchaseAuditStatus(esDO.getId(), pageReqVO.getPurchaseAuditStatus())
//        : distributionPurchaseESRepository.findByBaseId(esDO.getId());
//        if (pageReqVO.getPurchaseAuditStatus() != null && !purchaseOpt.isPresent()) {
//            return null;
//        }
//            if (purchaseOpt.isPresent()) {
//                ErpDistributionPurchaseESDO purchase = purchaseOpt.get();
//                BeanUtils.copyProperties(purchase, vo);
//
//                // 从ES查询组品信息
//                if (purchase.getComboProductId() != null) {
//                    Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(purchase.getComboProductId());
//                    if (comboProductOpt.isPresent()) {
//                        ErpComboProductES comboProduct = comboProductOpt.get();
//                        vo.setProductName(comboProduct.getName());
//                        vo.setShippingCode(comboProduct.getShippingCode());
//                        vo.setPurchaser(comboProduct.getPurchaser());
//                        vo.setSupplier(comboProduct.getSupplier());
//                        vo.setPurchasePrice(comboProduct.getPurchasePrice());
//                        vo.setComboProductNo(comboProduct.getNo());
//
//                        // 计算运费和采购总额
//                        calculatePurchaseAmount(vo, comboProduct, purchase);
//                    }
//                }
//            }
//
//            // 从ES查询销售信息（通过baseId匹配）
//            Optional<ErpDistributionSaleESDO> saleOpt = pageReqVO.getSaleAuditStatus() != null
//            ? distributionSaleESRepository.findByBaseIdAndSaleAuditStatus(esDO.getId(), pageReqVO.getSaleAuditStatus())
//            : distributionSaleESRepository.findByBaseId(esDO.getId());
//            // 如果有销售审核状态条件但找不到匹配记录，则返回null
//            if (pageReqVO.getSaleAuditStatus() != null && !saleOpt.isPresent()) {
//                return null;
//            }
//            if (saleOpt.isPresent()) {
//                ErpDistributionSaleESDO sale = saleOpt.get();
//                vo.setSalesperson(sale.getSalesperson());
//                vo.setCustomerName(sale.getCustomerName());
//                vo.setSaleOtherFees(sale.getOtherFees());
//                vo.setTransferPerson(sale.getTransferPerson());
//                vo.setSaleAfterSalesStatus(sale.getSaleAfterSalesStatus());
//                vo.setSaleAfterSalesSituation(sale.getSaleAfterSalesSituation());
//                vo.setSaleAfterSalesAmount(sale.getSaleAfterSalesAmount());
//                vo.setSaleAfterSalesTime(sale.getSaleAfterSalesTime());
//                vo.setSaleAuditStatus(sale.getSaleAuditStatus());
//                vo.setSaleRemark(sale.getSaleRemark());
//
//
//                // 计算销售总额
//                if (purchaseOpt.isPresent() && sale.getCustomerName() != null) {
//                    calculateSaleAmount(vo, purchaseOpt.get(), sale);
//                }
//            }
//
//            return vo;
//        })
//        .collect(Collectors.toList());
//
//        return new PageResult<>(voList, searchHits.getTotalHits());
//    }

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

//    private void validateDistributionForCreateOrUpdate(Long id, ErpDistributionSaveReqVO reqVO) {
//        // 1. 校验订单号唯一 - 使用ES查询
//        ErpDistributionBaseESDO distribution = distributionBaseESRepository.findByNo(reqVO.getNo());
//        if (distribution != null && !distribution.getId().equals(id)) {
//            throw exception(DISTRIBUTION_NO_EXISTS);
//        }
//    }
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



//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updatePurchaseAfterSales(ErpDistributionPurchaseAfterSalesUpdateReqVO reqVO) {
//        // 1. 校验存在 - 使用ES查询
//        Optional<ErpDistributionBaseESDO> baseOpt = distributionBaseESRepository.findById(reqVO.getId());
//        if (!baseOpt.isPresent()) {
//            throw exception(DISTRIBUTION_NOT_EXISTS);
//        }
//
//        // 2. 解析时间
//        LocalDateTime purchaseAfterSalesTime = parseDateTime(reqVO.getPurchaseAfterSalesTime());
//        LocalDateTime afterSalesTime = parseDateTime(reqVO.getAfterSalesTime());
//
//        // 3. 更新基础信息到ES
//        ErpDistributionBaseESDO baseES = baseOpt.get();
//        baseES.setAfterSalesStatus(reqVO.getAfterSalesStatus());
//        baseES.setAfterSalesTime(afterSalesTime);
//        distributionBaseESRepository.save(baseES);
//
//        // 4. 更新采购信息到ES
//        Optional<ErpDistributionPurchaseESDO> purchaseOpt = distributionPurchaseESRepository.findByBaseId(reqVO.getId());
//        if (purchaseOpt.isPresent()) {
//            ErpDistributionPurchaseESDO purchaseES = purchaseOpt.get();
//            purchaseES.setPurchaseAfterSalesStatus(reqVO.getPurchaseAfterSalesStatus())
//                    .setPurchaseAfterSalesAmount(reqVO.getPurchaseAfterSalesAmount())
//                    .setPurchaseAfterSalesTime(purchaseAfterSalesTime);
//            distributionPurchaseESRepository.save(purchaseES);
//        }
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateSaleAfterSales(ErpDistributionSaleAfterSalesUpdateReqVO reqVO) {
//        // 1. 校验存在 - 使用ES查询
//        Optional<ErpDistributionBaseESDO> baseOpt = distributionBaseESRepository.findById(reqVO.getId());
//        if (!baseOpt.isPresent()) {
//            throw exception(DISTRIBUTION_NOT_EXISTS);
//        }
//
//        // 2. 解析时间
//        LocalDateTime saleAfterSalesTime = parseDateTime(reqVO.getSaleAfterSalesTime());
//        LocalDateTime afterSalesTime = parseDateTime(reqVO.getAfterSalesTime());
//
//        // 3. 更新基础信息到ES
//        ErpDistributionBaseESDO baseES = baseOpt.get();
//        baseES.setAfterSalesStatus(reqVO.getAfterSalesStatus());
//        baseES.setAfterSalesTime(afterSalesTime);
//        distributionBaseESRepository.save(baseES);
//
//        // 4. 更新销售信息到ES
//        Optional<ErpDistributionSaleESDO> saleOpt = distributionSaleESRepository.findByBaseId(reqVO.getId());
//        if (saleOpt.isPresent()) {
//            ErpDistributionSaleESDO saleES = saleOpt.get();
//            saleES.setSaleAfterSalesStatus(reqVO.getSaleAfterSalesStatus())
//                    .setSaleAfterSalesAmount(reqVO.getSaleAfterSalesAmount())
//                    .setSaleAfterSalesTime(saleAfterSalesTime);
//            distributionSaleESRepository.save(saleES);
//        }
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updatePurchaseAuditStatus(Long id, Integer purchaseAuditStatus, BigDecimal otherFees) {
//        // 1. 校验存在 - 使用ES查询
//        Optional<ErpDistributionBaseESDO> baseOpt = distributionBaseESRepository.findById(id);
//        if (!baseOpt.isPresent()) {
//            throw exception(DISTRIBUTION_NOT_EXISTS);
//        }
//
//        // 2. 获取当前采购审核状态 - 使用ES查询
//        Optional<ErpDistributionPurchaseESDO> purchaseOpt = distributionPurchaseESRepository.findByBaseId(id);
//        if (!purchaseOpt.isPresent()) {
//            throw exception(DISTRIBUTION_NOT_EXISTS);
//        }
//
//        // 3. 校验状态是否重复
//        if (purchaseOpt.get().getPurchaseAuditStatus() != null &&
//            purchaseOpt.get().getPurchaseAuditStatus().equals(purchaseAuditStatus)) {
//            throw exception(DISTRIBUTION_PROCESS_FAIL);
//        }
//
//        // 4. 更新采购审核状态到ES
//        ErpDistributionPurchaseESDO purchaseES = purchaseOpt.get();
//        purchaseES.setPurchaseAuditStatus(purchaseAuditStatus)
//                .setOtherFees(otherFees);
//
//        // 设置时间
//        if (purchaseAuditStatus == 20) {
//            purchaseES.setPurchaseApprovalTime(LocalDateTime.now());
//        } else if (purchaseAuditStatus == 10) {
//            purchaseES.setPurchaseUnapproveTime(LocalDateTime.now());
//        }
//
//        distributionPurchaseESRepository.save(purchaseES);
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateSaleAuditStatus(Long id, Integer saleAuditStatus, BigDecimal otherFees) {
//        // 1. 校验存在 - 使用ES查询
//        Optional<ErpDistributionBaseESDO> baseOpt = distributionBaseESRepository.findById(id);
//        if (!baseOpt.isPresent()) {
//            throw exception(DISTRIBUTION_NOT_EXISTS);
//        }
//
//        // 2. 获取当前销售审核状态 - 使用ES查询
//        Optional<ErpDistributionSaleESDO> saleOpt = distributionSaleESRepository.findByBaseId(id);
//        if (!saleOpt.isPresent()) {
//            throw exception(DISTRIBUTION_NOT_EXISTS);
//        }
//
//        // 3. 校验状态是否重复
//        if (saleOpt.get().getSaleAuditStatus() != null &&
//            saleOpt.get().getSaleAuditStatus().equals(saleAuditStatus)) {
//            throw exception(DISTRIBUTION_PROCESS_FAIL);
//        }
//
//        // 4. 更新销售审核状态到ES
//        ErpDistributionSaleESDO saleES = saleOpt.get();
//        saleES.setSaleAuditStatus(saleAuditStatus)
//                .setOtherFees(otherFees);
//
//        // 设置时间
//        if (saleAuditStatus == 20) {
//            saleES.setSaleApprovalTime(LocalDateTime.now());
//        } else if (saleAuditStatus == 10) {
//            saleES.setSaleUnapproveTime(LocalDateTime.now());
//        }
//
//        distributionSaleESRepository.save(saleES);
//    }

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

        // 3. 更新合并表信息
        ErpDistributionCombinedESDO combined = combinedOpt.get();
        combined.setAfterSalesStatus(reqVO.getAfterSalesStatus())
                .setAfterSalesTime(afterSalesTime)
                .setPurchaseAfterSalesStatus(reqVO.getPurchaseAfterSalesStatus())
                .setPurchaseAfterSalesAmount(reqVO.getPurchaseAfterSalesAmount())
                .setPurchaseAfterSalesTime(purchaseAfterSalesTime);
        distributionCombinedESRepository.save(combined);
         // 4. 同步更新数据库
         distributionCombinedMapper.updateById(BeanUtils.toBean(combined, ErpDistributionCombinedDO.class));
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

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public ErpDistributionImportRespVO importDistributionList(List<ErpDistributionImportExcelVO> importList, boolean isUpdateSupport) {
//        if (CollUtil.isEmpty(importList)) {
//            throw exception(DISTRIBUTION_IMPORT_LIST_IS_EMPTY);
//        }
//
//        // 初始化返回结果
//        ErpDistributionImportRespVO respVO = ErpDistributionImportRespVO.builder()
//                .createNames(new ArrayList<>())
//                .updateNames(new ArrayList<>())
//                .failureNames(new LinkedHashMap<>())
//                .build();
//
//        // 查询已存在的代发订单记录
//        Set<String> noSet = importList.stream()
//                .map(ErpDistributionImportExcelVO::getNo)
//                .filter(StrUtil::isNotBlank)
//                .collect(Collectors.toSet());
//        List<ErpDistributionBaseESDO> existList = noSet.isEmpty()
//                ? Collections.emptyList()
//                : distributionBaseESRepository.findByNoIn(new ArrayList<>(noSet));
////        Map<String, ErpDistributionBaseDO> noDistributionMap = convertMap(existList,
////                esDO -> esDO.getNo(),
////                esDO -> BeanUtils.toBean(esDO, ErpDistributionBaseDO.class));
//
//        Map<String, ErpDistributionBaseDO> noDistributionMap = convertMap(existList,
//                ErpDistributionBaseESDO::getNo,
//                esDO -> BeanUtils.toBean(esDO, ErpDistributionBaseDO.class));
//
//        // 遍历处理每个导入项
//        for (int i = 0; i < importList.size(); i++) {
//            ErpDistributionImportExcelVO importVO = importList.get(i);
//            try {
//                // 校验必填字段
//                //validateImportData(importVO);
//                                // 校验销售人员是否存在
//                if (StrUtil.isNotBlank(importVO.getSalesperson())) {
//                    List<ErpSalespersonRespVO> salespersons = salespersonService.searchSalespersons(
//                            new ErpSalespersonPageReqVO().setSalespersonName(importVO.getSalesperson()));
//                    if (CollUtil.isEmpty(salespersons)) {
//                        throw exception(DISTRIBUTION_SALESPERSON_NOT_EXISTS, importVO.getSalesperson());
//                    }
//                }
//
//                // 校验客户是否存在
//                if (StrUtil.isNotBlank(importVO.getCustomerName())) {
//                    List<ErpCustomerSaveReqVO> customers = customerService.searchCustomers(
//                            new ErpCustomerPageReqVO().setName(importVO.getCustomerName()));
//                    if (CollUtil.isEmpty(customers)) {
//                        throw exception(DISTRIBUTION_CUSTOMER_NOT_EXISTS, importVO.getCustomerName());
//                    }
//                }
//
//                // 获取组品ID
//                Long comboProductId = null;
//                if (StrUtil.isNotBlank(importVO.getComboProductNo())) {
//                    Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findByNo(importVO.getComboProductNo());
//                    if (!comboProductOpt.isPresent()) {
//                        throw exception(DISTRIBUTION_COMBO_PRODUCT_NOT_EXISTS, importVO.getComboProductNo());
//                    }
//                    comboProductId = comboProductOpt.get().getId();
//                }
//
//                // 判断是否支持更新
//                ErpDistributionBaseDO existDistribution = noDistributionMap.get(importVO.getNo());
//                if (existDistribution == null) {
//                    // 创建逻辑
//                    ErpDistributionSaveReqVO createReqVO = BeanUtils.toBean(importVO, ErpDistributionSaveReqVO.class).setComboProductId(comboProductId);
//                    Long id = createDistribution(createReqVO);
//                    respVO.getCreateNames().add(createReqVO.getNo());
//                } else if (isUpdateSupport) {
//                    // 更新逻辑
//                    ErpDistributionSaveReqVO updateReqVO = BeanUtils.toBean(importVO, ErpDistributionSaveReqVO.class).setComboProductId(comboProductId);
//                    System.out.println("更新id"+existDistribution.getId());
//                    updateReqVO.setId(existDistribution.getId());
//                    updateDistribution(updateReqVO);
//                    respVO.getUpdateNames().add(updateReqVO.getNo());
//                } else {
//                    throw exception(DISTRIBUTION_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
//                }
//            } catch (ServiceException ex) {
//                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知代发订单";
//                respVO.getFailureNames().put(errorKey, ex.getMessage());
//            } catch (Exception ex) {
//                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知代发订单";
//                respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
//            }
//        }
//
//        return respVO;
//    }


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
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }


//    private void validateImportData(ErpDistributionImportExcelVO importVO) {
//        // 1. 校验必填字段
//        if (StringUtils.isEmpty(importVO.getNo())) {
//            throw new ServiceException(DISTRIBUTION_NO_EMPTY);
//        }
//        if (StringUtils.isEmpty(importVO.getReceiverName())) {
//            throw new ServiceException(DISTRIBUTION_RECEIVER_NAME_EMPTY);
//        }
//        if (StringUtils.isEmpty(importVO.getReceiverPhone())) {
//            throw new ServiceException(DISTRIBUTION_RECEIVER_PHONE_EMPTY);
//        }
//        if (StringUtils.isEmpty(importVO.getReceiverAddress())) {
//            throw new ServiceException(DISTRIBUTION_RECEIVER_ADDRESS_EMPTY);
//        }
//        if (importVO.getProductQuantity() == null || importVO.getProductQuantity() <= 0) {
//            throw new ServiceException(DISTRIBUTION_PRODUCT_QUANTITY_INVALID);
//        }
//
//        // 2. 校验组品信息
//        if (!StringUtils.isEmpty(importVO.getComboProductNo())) {
//            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findByNo(importVO.getComboProductNo());
//            if (!comboProductOpt.isPresent()) {
//                throw new ServiceException(DISTRIBUTION_COMBO_PRODUCT_NOT_EXISTS);
//            }
//        }
//    }
}
