package cn.iocoder.yudao.module.erp.service.wholesale;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.*;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO.ErpWholesaleImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO.ErpWholesaleImportRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionCombinedDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.*;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleCombinedMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesalePurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleSaleMapper;
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
import java.math.BigDecimal;

import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;


import javax.annotation.Resource;
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
public class ErpWholesaleServiceImpl implements ErpWholesaleService {

    @Resource
    private ErpWholesaleMapper wholesaleMapper;

    @Resource
    private ErpWholesalePurchaseMapper purchaseMapper;

    @Resource
    private ErpWholesaleSaleMapper saleMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Resource
    private ErpWholesaleBaseESRepository wholesaleBaseESRepository;
    @Resource
    private ErpWholesalePurchaseESRepository wholesalePurchaseESRepository;
    @Resource
    private ErpWholesaleSaleESRepository wholesaleSaleESRepository;

    @Resource
    private ErpComboProductESRepository comboProductESRepository;

    @Resource
    private ErpSalePriceESRepository salePriceESRepository;

    @Resource
    private ErpWholesaleCombinedESRepository wholesaleCombinedESRepository;

    @Resource
    private ErpWholesaleCombinedMapper wholesaleCombinedMapper;
    @Resource
    private ErpCustomerService customerService;

    @Resource
    private ErpSalespersonService salespersonService;

//     // 初始化ES索引
//     @EventListener(ApplicationReadyEvent.class)
//     public void initESIndex() {
//         System.out.println("开始初始化批发订单ES索引...");
//         try {
//             // 初始化基础表索引
//             IndexOperations baseIndexOps = elasticsearchRestTemplate.indexOps(ErpWholesaleBaseESDO.class);
//             if (!baseIndexOps.exists()) {
//                 baseIndexOps.create();
//                 baseIndexOps.putMapping(baseIndexOps.createMapping(ErpWholesaleBaseESDO.class));
//                 System.out.println("批发基础表索引创建成功");
//             }
//
//             // 初始化采购表索引
//             IndexOperations purchaseIndexOps = elasticsearchRestTemplate.indexOps(ErpWholesalePurchaseESDO.class);
//             if (!purchaseIndexOps.exists()) {
//                 purchaseIndexOps.create();
//                 purchaseIndexOps.putMapping(purchaseIndexOps.createMapping(ErpWholesalePurchaseESDO.class));
//                 System.out.println("批发采购表索引创建成功");
//             }
//
//             // 初始化销售表索引
//             IndexOperations saleIndexOps = elasticsearchRestTemplate.indexOps(ErpWholesaleSaleESDO.class);
//             if (!saleIndexOps.exists()) {
//                 saleIndexOps.create();
//                 saleIndexOps.putMapping(saleIndexOps.createMapping(ErpWholesaleSaleESDO.class));
//                 System.out.println("批发销售表索引创建成功");
//             }
//         } catch (Exception e) {
//             System.err.println("批发订单索引初始化失败: " + e.getMessage());
//         }
//     }
//
//     // 同步基础表数据到ES
//     private void syncBaseToES(Long baseId) {
//         ErpWholesaleBaseDO base = wholesaleMapper.selectById(baseId);
//         if (base == null) {
//             wholesaleBaseESRepository.deleteById(baseId);
//         } else {
//             ErpWholesaleBaseESDO es = convertBaseToES(base);
//             wholesaleBaseESRepository.save(es);
//         }
//     }
//
//     // 同步采购表数据到ES
//     private void syncPurchaseToES(Long purchaseId) {
//         ErpWholesalePurchaseDO purchase = purchaseMapper.selectById(purchaseId);
//         if (purchase == null) {
//             wholesalePurchaseESRepository.deleteByBaseId(purchaseId);
//         } else {
//             ErpWholesalePurchaseESDO es = convertPurchaseToES(purchase);
//             wholesalePurchaseESRepository.save(es);
//         }
//     }
//
//     // 同步销售表数据到ES
//     private void syncSaleToES(Long saleId) {
//         ErpWholesaleSaleDO sale = saleMapper.selectById(saleId);
//         if (sale == null) {
//             wholesaleSaleESRepository.deleteByBaseId(saleId);
//         } else {
//             ErpWholesaleSaleESDO es = convertSaleToES(sale);
//             wholesaleSaleESRepository.save(es);
//         }
//     }
//
//     // 转换方法
//     private ErpWholesaleBaseESDO convertBaseToES(ErpWholesaleBaseDO base) {
//         ErpWholesaleBaseESDO es = new ErpWholesaleBaseESDO();
//         BeanUtils.copyProperties(base, es);
//         return es;
//     }
//
//     private ErpWholesalePurchaseESDO convertPurchaseToES(ErpWholesalePurchaseDO purchase) {
//         ErpWholesalePurchaseESDO es = new ErpWholesalePurchaseESDO();
//         BeanUtils.copyProperties(purchase, es);
//         return es;
//     }
//
//     private ErpWholesaleSaleESDO convertSaleToES(ErpWholesaleSaleDO sale) {
//         ErpWholesaleSaleESDO es = new ErpWholesaleSaleESDO();
//         BeanUtils.copyProperties(sale, es);
//         return es;
//     }

         // 初始化ES索引
    @EventListener(ApplicationReadyEvent.class)
    public void initESIndex() {
        System.out.println("开始初始化批发合并表ES索引...");
        try {
            // 初始化合并表索引
            IndexOperations combinedIndexOps = elasticsearchRestTemplate.indexOps(ErpWholesaleCombinedESDO.class);
            if (!combinedIndexOps.exists()) {
                combinedIndexOps.create();
                combinedIndexOps.putMapping(combinedIndexOps.createMapping(ErpWholesaleCombinedESDO.class));
                System.out.println("批发合并表索引创建成功");
            }
        } catch (Exception e) {
            System.err.println("批发合并表索引初始化失败: " + e.getMessage());
        }
    }

    // 同步合并表数据到ES
    private void syncCombinedToES(Long id) {
        ErpWholesaleCombinedDO combined = wholesaleCombinedMapper.selectById(id);
        if (combined == null) {
            wholesaleCombinedESRepository.deleteById(id);
        } else {
            ErpWholesaleCombinedESDO es = convertCombinedToES(combined);
            wholesaleCombinedESRepository.save(es);
        }
    }

    // 转换方法
    private ErpWholesaleCombinedESDO convertCombinedToES(ErpWholesaleCombinedDO combined) {
        ErpWholesaleCombinedESDO es = new ErpWholesaleCombinedESDO();
        BeanUtils.copyProperties(combined, es);
        return es;
    }

//     // 全量同步方法
//     @Async
//     public void fullSyncToES() {
//         try {
//             // 同步基础表
//             List<ErpWholesaleBaseDO> bases = wholesaleMapper.selectList(null);
//             if (CollUtil.isNotEmpty(bases)) {
//                 List<ErpWholesaleBaseESDO> baseESList = bases.stream()
//                         .map(this::convertBaseToES)
//                         .collect(Collectors.toList());
//                 wholesaleBaseESRepository.saveAll(baseESList);
//             }
//
//             // 同步采购表
//             List<ErpWholesalePurchaseDO> purchases = purchaseMapper.selectList(null);
//             if (CollUtil.isNotEmpty(purchases)) {
//                 List<ErpWholesalePurchaseESDO> purchaseESList = purchases.stream()
//                         .map(this::convertPurchaseToES)
//                         .collect(Collectors.toList());
//                 wholesalePurchaseESRepository.saveAll(purchaseESList);
//             }
//
//             // 同步销售表
//             List<ErpWholesaleSaleDO> sales = saleMapper.selectList(null);
//             if (CollUtil.isNotEmpty(sales)) {
//                 List<ErpWholesaleSaleESDO> saleESList = sales.stream()
//                         .map(this::convertSaleToES)
//                         .collect(Collectors.toList());
//                 wholesaleSaleESRepository.saveAll(saleESList);
//             }
//
//             System.out.println("批发订单全量同步ES数据完成");
//         } catch (Exception e) {
//             System.err.println("批发订单全量同步ES数据失败: " + e.getMessage());
//         }
//     }

         // 全量同步方法
    @Async
    public void fullSyncToES() {
        try {
            // 同步合并表数据
            List<ErpWholesaleCombinedDO> combinedList = wholesaleCombinedMapper.selectList(null);
            if (CollUtil.isNotEmpty(combinedList)) {
                List<ErpWholesaleCombinedESDO> combinedESList = combinedList.stream()
                        .map(this::convertCombinedToES)
                        .collect(Collectors.toList());
                wholesaleCombinedESRepository.saveAll(combinedESList);
            }

            System.out.println("批发合并表全量同步ES数据完成");
        } catch (Exception e) {
            System.err.println("批发合并表全量同步ES数据失败: " + e.getMessage());
        }
    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public Long createWholesale(ErpWholesaleSaveReqVO createReqVO) {
//        // 1. 校验数据
//        validateWholesaleForCreateOrUpdate(null, createReqVO);
//
//        // 2. 生成批发单号，并校验唯一性
//        String no = noRedisDAO.generate(ErpNoRedisDAO.WHOLESALE_NO_PREFIX);
//        if (wholesaleMapper.selectByNo(no) != null) {
//            throw exception(WHOLESALE_NO_EXISTS);
//        }
//        LocalDateTime AfterSalesTime = parseDateTime(createReqVO.getAfterSalesTime());
//        // 3. 插入批发记录
//        ErpWholesaleBaseDO wholesale = BeanUtils.toBean(createReqVO, ErpWholesaleBaseDO.class)
//                .setNo(no)
//                .setStatus(ErpAuditStatus.PROCESS.getStatus())
//                .setAfterSalesTime(AfterSalesTime);
//        wholesaleMapper.insert(wholesale);
//
//        // 4. 插入采购信息
//        ErpWholesalePurchaseDO purchase = BeanUtils.toBean(createReqVO, ErpWholesalePurchaseDO.class)
//                .setBaseId(wholesale.getId())
//                .setPurchaseAuditStatus(ErpAuditStatus.PROCESS.getStatus())
//                .setPurchaseAfterSalesStatus(30)
//                .setOtherFees(createReqVO.getOtherFees());
//        purchaseMapper.insert(purchase);
//
//        // 5. 插入销售信息
//        ErpWholesaleSaleDO sale = BeanUtils.toBean(createReqVO, ErpWholesaleSaleDO.class)
//                .setBaseId(wholesale.getId())
//                .setSaleAuditStatus(ErpAuditStatus.PROCESS.getStatus())
//                .setSaleAfterSalesStatus(30)
//                .setLogisticsFee(createReqVO.getSaleLogisticsFee())
//                .setTruckFee(createReqVO.getSaleTruckFee())
//                .setOtherFees(createReqVO.getSaleOtherFees());
//        saleMapper.insert(sale);
//
//        // 6. 同步到ES
//        syncBaseToES(wholesale.getId());
//        syncPurchaseToES(purchase.getId());
//        syncSaleToES(sale.getId());
//
//        return wholesale.getId();
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateWholesale(ErpWholesaleSaveReqVO updateReqVO) {
//        // 1.1 校验存在
//        ErpWholesaleBaseDO wholesale = validateWholesale(updateReqVO.getId());
//        if (ErpAuditStatus.APPROVE.getStatus().equals(wholesale.getStatus())) {
//            throw exception(WHOLESALE_UPDATE_FAIL_APPROVE, wholesale.getNo());
//        }
//        // 1.2 校验数据
//        validateWholesaleForCreateOrUpdate(updateReqVO.getId(), updateReqVO);
//
//        // 2. 更新批发记录
//        ErpWholesaleBaseDO updateObj = BeanUtils.toBean(updateReqVO, ErpWholesaleBaseDO.class);
//        wholesaleMapper.updateById(updateObj);
//
//        // 3. 更新采购信息（独立检查审核状态）
//        ErpWholesalePurchaseDO purchase = purchaseMapper.selectByBaseId(updateReqVO.getId());
//        if (purchase != null) {
//            if (!ErpAuditStatus.APPROVE.getStatus().equals(purchase.getPurchaseAuditStatus())) {
//                Long originalId = purchase.getId();
//                purchase = BeanUtils.toBean(updateReqVO, ErpWholesalePurchaseDO.class)
//                        .setBaseId(updateReqVO.getId())
//                        .setId(originalId);
//                purchaseMapper.update(purchase,
//                    new LambdaUpdateWrapper<ErpWholesalePurchaseDO>()
//                        .eq(ErpWholesalePurchaseDO::getBaseId, updateReqVO.getId()));
//            }
//        }
//
//        // 4. 更新销售信息（独立检查审核状态）
//        ErpWholesaleSaleDO sale = saleMapper.selectByBaseId(updateReqVO.getId());
//        if (sale != null) {
//            if (!ErpAuditStatus.APPROVE.getStatus().equals(sale.getSaleAuditStatus())) {
//                Long originalId = sale.getId();
//                sale = BeanUtils.toBean(updateReqVO, ErpWholesaleSaleDO.class)
//                        .setBaseId(updateReqVO.getId())
//                        .setOtherFees(updateReqVO.getSaleOtherFees())
//                        .setTruckFee(updateReqVO.getSaleTruckFee())
//                        .setId(originalId); // 恢复原始ID
//                saleMapper.update(sale,
//                    new LambdaUpdateWrapper<ErpWholesaleSaleDO>()
//                        .eq(ErpWholesaleSaleDO::getBaseId, updateReqVO.getId()));
//            }
//        }
//        // 5. 同步到ES
//        syncBaseToES(updateReqVO.getId());
//        if (purchase != null) {
//            syncPurchaseToES(purchase.getId());
//        }
//        if (sale != null) {
//            syncSaleToES(sale.getId());
//        }
//
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void deleteWholesale(List<Long> ids) {
//        if (CollUtil.isEmpty(ids)) {
//            return;
//        }
//        // 1. 校验存在
//        List<ErpWholesaleBaseDO> wholesales = wholesaleMapper.selectBatchIds(ids);
//        if (CollUtil.isEmpty(wholesales)) {
//            throw exception(WHOLESALE_NOT_EXISTS);
//        }
//        // 2. 删除批发记录
//        wholesaleMapper.deleteBatchIds(ids);
//
//        // 3. 删除采购信息
//        purchaseMapper.deleteByBaseIds(ids);
//
//        // 4. 删除销售信息
//        saleMapper.deleteByBaseIds(ids);
//
//        // 从ES删除
//        ids.forEach(id -> {
//            wholesaleBaseESRepository.deleteById(id);
//            wholesalePurchaseESRepository.deleteByBaseId(id);
//            wholesaleSaleESRepository.deleteByBaseId(id);
//        });
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWholesale(ErpWholesaleSaveReqVO createReqVO) {
        // 1. 校验数据
        validateWholesaleForCreateOrUpdate(null, createReqVO);

        // 2. 生成批发单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.WHOLESALE_NO_PREFIX);
        if (wholesaleCombinedMapper.selectByNo(no) != null) {
            throw exception(WHOLESALE_NO_EXISTS);
        }
        LocalDateTime afterSalesTime = parseDateTime(createReqVO.getAfterSalesTime());

        // 3. 生成ID
        Long id = IdUtil.getSnowflakeNextId();

        // 4. 保存到数据库
        ErpWholesaleCombinedDO combinedDO = BeanUtils.toBean(createReqVO, ErpWholesaleCombinedDO.class)
                .setId(id)
                .setNo(no)
                .setAfterSalesTime(afterSalesTime)
                .setPurchaseAuditStatus(ErpAuditStatus.PROCESS.getStatus())
                .setPurchaseAfterSalesStatus(30)
                .setSaleAuditStatus(ErpAuditStatus.PROCESS.getStatus())
                .setPurchaseOtherFees(createReqVO.getOtherFees())
                .setPurchaseTruckFee(createReqVO.getTruckFee())
                .setPurchaseLogisticsFee(createReqVO.getLogisticsFee())
                .setSaleAfterSalesStatus(30);
        wholesaleCombinedMapper.insert(combinedDO);

        // 5. 保存到ES
        ErpWholesaleCombinedESDO combinedESDO = convertCombinedToES(combinedDO);
        wholesaleCombinedESRepository.save(combinedESDO);

        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWholesale(ErpWholesaleSaveReqVO updateReqVO) {
        System.out.println("查看更新的数据"+updateReqVO);
        // 1.1 校验存在 - 通过订单号查询
        ErpWholesaleCombinedESDO combined = wholesaleCombinedESRepository.findByNo(updateReqVO.getNo());
        if (combined == null) {
            throw exception(WHOLESALE_NOT_EXISTS);
        }

        // 1.2 校验采购审核状态
        if (ErpAuditStatus.APPROVE.getStatus().equals(combined.getPurchaseAuditStatus())) {
            throw exception(WHOLESALE_UPDATE_FAIL_PURCHASE_APPROVE, combined.getNo());
        }

        // 1.3 校验销售审核状态
        if (ErpAuditStatus.APPROVE.getStatus().equals(combined.getSaleAuditStatus())) {
            throw exception(WHOLESALE_UPDATE_FAIL_SALE_APPROVE, combined.getNo());
        }

        // 1.4 校验数据
        validateWholesaleForCreateOrUpdate(combined.getId(), updateReqVO);

        // 2. 更新数据库记录
        ErpWholesaleCombinedDO updateDO = BeanUtils.toBean(updateReqVO, ErpWholesaleCombinedDO.class)
                .setId(combined.getId())
                .setPurchaseOtherFees(updateReqVO.getOtherFees())
                .setPurchaseTruckFee(updateReqVO.getTruckFee())
                .setPurchaseLogisticsFee(updateReqVO.getLogisticsFee())
                .setNo(combined.getNo())
                .setPurchaseAuditStatus(combined.getPurchaseAuditStatus())
                .setSaleAuditStatus(combined.getSaleAuditStatus());

        wholesaleCombinedMapper.updateById(updateDO);
        ErpWholesaleCombinedDO dbCombined = wholesaleCombinedMapper.selectById(updateDO.getId());

        // 3. 更新ES记录
        ErpWholesaleCombinedESDO combinedESDO = convertCombinedToES(updateDO);
        combinedESDO.setCreator(dbCombined.getCreator());
        combinedESDO.setCreateTime(dbCombined.getCreateTime());
        wholesaleCombinedESRepository.save(combinedESDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWholesale(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在 - 从数据库查询
        List<ErpWholesaleCombinedDO> wholesales = wholesaleCombinedMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(wholesales)) {
            throw exception(WHOLESALE_NOT_EXISTS);
        }

        // 2. 从数据库删除
        wholesaleCombinedMapper.deleteBatchIds(ids);

        // 3. 从ES删除 - 使用相同的ID集合
        wholesaleCombinedESRepository.deleteAllById(ids);
    }

//    @Override
//    public ErpWholesaleBaseDO getWholesale(Long id) {
//        return wholesaleMapper.selectById(id);
//    }

    @Override
    public ErpWholesaleRespVO getWholesale(Long id) {
        // 1. 从合并ES表查询数据
        Optional<ErpWholesaleCombinedESDO> combinedOpt = wholesaleCombinedESRepository.findById(id);
        if (!combinedOpt.isPresent()) {
            return null;
        }
        ErpWholesaleCombinedESDO combined = combinedOpt.get();

        // 2. 转换为RespVO
        ErpWholesaleRespVO respVO = BeanUtils.toBean(combined, ErpWholesaleRespVO.class)
                .setTruckFee(combined.getPurchaseTruckFee())
                .setLogisticsFee(combined.getPurchaseLogisticsFee())
                .setOtherFees(combined.getPurchaseOtherFees());

        // 3. 查询组品信息并设置到respVO
        if (combined.getComboProductId() != null) {
            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combined.getComboProductId());
            if (comboProductOpt.isPresent()) {
                ErpComboProductES comboProduct = comboProductOpt.get();
                respVO.setShippingCode(comboProduct.getShippingCode())
                      .setProductName(comboProduct.getName())
                      .setPurchaser(comboProduct.getPurchaser())
                      .setSupplier(comboProduct.getSupplier())
                      .setPurchasePrice(comboProduct.getWholesalePrice())  // 采购价使用组品的批发价
                      .setComboProductNo(comboProduct.getNo());
                // 查询销售价格
                Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                        combined.getComboProductId(),
                        combined.getCustomerName());
                if (salePriceOpt.isPresent()) {
                    respVO.setSalePrice(salePriceOpt.get().getWholesalePrice());
                }
                       // 计算采购总额
        BigDecimal totalPurchaseAmount = comboProduct.getPurchasePrice()
                .multiply(BigDecimal.valueOf(combined.getProductQuantity()))
                .add(combined.getPurchaseTruckFee() != null ? combined.getPurchaseTruckFee() : BigDecimal.ZERO)
                .add(combined.getPurchaseLogisticsFee() != null ? combined.getPurchaseLogisticsFee() : BigDecimal.ZERO)
                .add(combined.getPurchaseOtherFees() != null ? combined.getPurchaseOtherFees() : BigDecimal.ZERO);
        respVO.setTotalPurchaseAmount(totalPurchaseAmount);

        // 计算销售总额
        BigDecimal salePrice = respVO.getSalePrice() != null ? respVO.getSalePrice() : BigDecimal.ZERO;
        BigDecimal totalSaleAmount = salePrice
                .multiply(BigDecimal.valueOf(combined.getProductQuantity()))
                .add(combined.getSaleTruckFee() != null ? combined.getSaleTruckFee() : BigDecimal.ZERO)
                .add(combined.getSaleLogisticsFee() != null ? combined.getSaleLogisticsFee() : BigDecimal.ZERO)
                .add(combined.getSaleOtherFees() != null ? combined.getSaleOtherFees() : BigDecimal.ZERO);
        respVO.setTotalSaleAmount(totalSaleAmount);
            }
        }

        return respVO;
    }

//    @Override
//    public ErpWholesaleBaseDO validateWholesale(Long id) {
//        ErpWholesaleBaseDO wholesale = wholesaleMapper.selectById(id);
//        if (wholesale == null) {
//            throw exception(WHOLESALE_NOT_EXISTS);
//        }
//        return wholesale;
//    }

    @Override
    public ErpWholesaleCombinedDO validateWholesale(Long id) {
        // 1. 从数据库查询合并表记录
        ErpWholesaleCombinedDO wholesale = wholesaleCombinedMapper.selectById(id);
        if (wholesale == null) {
            // 2. 如果数据库没有，再从ES查询
            Optional<ErpWholesaleCombinedESDO> combinedOpt = wholesaleCombinedESRepository.findById(id);
            if (!combinedOpt.isPresent()) {
                throw exception(WHOLESALE_NOT_EXISTS);
            }
            // 将ESDO转换为DO返回
            return BeanUtils.toBean(combinedOpt.get(), ErpWholesaleCombinedDO.class);
        }
        return wholesale;
    }

//    @Override
//    public PageResult<ErpWholesaleRespVO> getWholesaleVOPage(ErpWholesalePageReqVO pageReqVO) {
//        return wholesaleMapper.selectPage(pageReqVO);
//    }

//    @Override
//    public PageResult<ErpWholesaleRespVO> getWholesaleVOPage(ErpWholesalePageReqVO pageReqVO) {
//        try {
//            // 1. 检查数据库是否有数据
//            long dbCount = wholesaleMapper.selectCount(null);
//            if (dbCount == 0) {
//                return new PageResult<>(Collections.emptyList(), 0L);
//            }
//
//            // 2. 检查ES索引是否存在
//            IndexOperations baseIndexOps = elasticsearchRestTemplate.indexOps(ErpWholesaleBaseESDO.class);
//            if (!baseIndexOps.exists()) {
//                initESIndex(); // 如果索引不存在则创建
//                fullSyncToES(); // 全量同步数据
//                return wholesaleMapper.selectPage(pageReqVO); // 首次查询使用数据库
//            }
//
//            // 3. 检查ES是否有数据
//            long esCount = elasticsearchRestTemplate.count(new NativeSearchQueryBuilder().build(), ErpWholesaleBaseESDO.class);
//            if (esCount == 0) {
//                fullSyncToES(); // 同步数据到ES
//                return wholesaleMapper.selectPage(pageReqVO); // 首次查询使用数据库
//            }
//
//            // 1. 构建基础查询条件
//            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
//                    .withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()))
//                    .withTrackTotalHits(true)
//                    .withSort(Sort.by(Sort.Direction.DESC, "id")); // 按照 ID 降序排序
//
//            // 添加查询条件
//            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//            if (StringUtils.isNotBlank(pageReqVO.getNo())) {
//                boolQuery.must(QueryBuilders.matchQuery("no", pageReqVO.getNo()));
//                System.out.println("添加查询条件 - no: " + pageReqVO.getNo());
//            }
//            if (StringUtils.isNotBlank(pageReqVO.getReceiverName())) {
//                boolQuery.must(QueryBuilders.matchQuery("receiverName", pageReqVO.getReceiverName()));
//                System.out.println("添加查询条件 - receiverName: " + pageReqVO.getReceiverName());
//            }
//            if (pageReqVO.getCreateTime() != null && pageReqVO.getCreateTime().length == 2) {
//                boolQuery.must(QueryBuilders.rangeQuery("createTime")
//                        .gte(pageReqVO.getCreateTime()[0])
//                        .lte(pageReqVO.getCreateTime()[1]));
//                        System.out.println("添加查询条件 - createTime范围: " +
//                        pageReqVO.getCreateTime()[0] + " - " + pageReqVO.getCreateTime()[1]);
//            }
//
//            queryBuilder.withQuery(boolQuery);
//
//            // 2. 如果是深度分页(超过10000条)，使用search_after
//            if (pageReqVO.getPageNo() > 1) {
//                return handleDeepPagination(pageReqVO, queryBuilder);
//            }
//
//            // 3. 普通分页处理
//            SearchHits<ErpWholesaleBaseESDO> searchHits = elasticsearchRestTemplate.search(
//                    queryBuilder.build(),
//                    ErpWholesaleBaseESDO.class,
//                    IndexCoordinates.of("erp_wholesale_base"));
//                    System.out.println("查询结果总数: " + searchHits.getTotalHits());
//                    System.out.println("查询到的文档数量: " + searchHits.getSearchHits().size());
//
//            List<ErpWholesaleRespVO> voList = searchHits.stream()
//                    .map(SearchHit::getContent)
//                    .map(esDO -> {
//                        //ErpWholesaleRespVO vo = BeanUtils.toBean(esDO, ErpWholesaleRespVO.class);
//                        ErpWholesaleRespVO vo = new ErpWholesaleRespVO();
//                        // 设置基础表字段
//                        vo.setId(esDO.getId());
//                        vo.setNo(esDO.getNo());
//                        vo.setOrderNumber(esDO.getOrderNumber());
//                        vo.setLogisticsNumber(esDO.getLogisticsNumber());
//                        vo.setReceiverName(esDO.getReceiverName());
//                        vo.setReceiverPhone(esDO.getReceiverPhone());
//                        vo.setReceiverAddress(esDO.getReceiverAddress());
//                        vo.setProductQuantity(esDO.getProductQuantity());
//                        vo.setProductSpecification(esDO.getProductSpecification());
//                        vo.setRemark(esDO.getRemark());
//                        vo.setCreator(esDO.getCreator());
//                        vo.setCreateTime(esDO.getCreateTime());
//                        vo.setAfterSalesStatus(esDO.getAfterSalesStatus());
//                        vo.setAfterSalesTime(esDO.getAfterSalesTime());
//                        // 从ES查询采购信息（通过baseId匹配）
//                        Optional<ErpWholesalePurchaseESDO> purchaseOpt = pageReqVO.getPurchaseAuditStatus() != null
//                                ? wholesalePurchaseESRepository.findByBaseIdAndPurchaseAuditStatus(esDO.getId(), pageReqVO.getPurchaseAuditStatus())
//                                : wholesalePurchaseESRepository.findByBaseId(esDO.getId());
//                        // 如果有采购审核状态条件但找不到匹配记录，则返回null
//                        if (pageReqVO.getPurchaseAuditStatus() != null && !purchaseOpt.isPresent()) {
//                            return null;
//                        }
//                        if (purchaseOpt.isPresent()) {
//                            ErpWholesalePurchaseESDO purchase = purchaseOpt.get();
//                            //BeanUtils.copyProperties(purchase, vo);
//                            vo.setTruckFee(purchase.getTruckFee());
//                            vo.setOtherFees(purchase.getOtherFees());
//                            vo.setPurchaseAfterSalesStatus(purchase.getPurchaseAfterSalesStatus());
//                            vo.setPurchaseAfterSalesSituation(purchase.getPurchaseAfterSalesSituation());
//                            vo.setPurchaseAfterSalesAmount(purchase.getPurchaseAfterSalesAmount());
//                            vo.setPurchaseAuditStatus(purchase.getPurchaseAuditStatus());
//                            vo.setPurchaseRemark(purchase.getPurchaseRemark());
//                            // 从ES查询组品信息
//                            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(purchase.getComboProductId());
//                            if (comboProductOpt.isPresent()) {
//                                ErpComboProductES comboProduct = comboProductOpt.get();
//                                vo.setProductName(comboProduct.getName());
//                                vo.setShippingCode(comboProduct.getShippingCode());
//                                vo.setPurchaser(comboProduct.getPurchaser());
//                                vo.setSupplier(comboProduct.getSupplier());
//                                vo.setPurchasePrice(comboProduct.getWholesalePrice());
//                                vo.setComboProductNo(comboProduct.getNo());
//
//                                // 直接使用采购记录中的运费值
//                                vo.setLogisticsFee(purchase.getLogisticsFee());
//                                // 计算采购运费
//
//                                // 计算采购总额 = 采购单价*数量 + 货拉拉费 + 物流费用 + 其他费用
//                                BigDecimal totalPurchaseAmount = comboProduct.getWholesalePrice()
//                                        .multiply(BigDecimal.valueOf(esDO.getProductQuantity()))
//                                        .add(purchase.getTruckFee() != null ? purchase.getTruckFee() : BigDecimal.ZERO)
//                                        .add(purchase.getLogisticsFee()!= null? purchase.getLogisticsFee() : BigDecimal.ZERO)
//                                        .add(purchase.getOtherFees() != null ? purchase.getOtherFees() : BigDecimal.ZERO);
//                                vo.setTotalPurchaseAmount(totalPurchaseAmount);
//                            }
//                        }
//
//                        // 从ES查询销售信息（通过baseId匹配）
//                        Optional<ErpWholesaleSaleESDO> saleOpt = pageReqVO.getSaleAuditStatus() != null
//                                ? wholesaleSaleESRepository.findByBaseIdAndSaleAuditStatus(esDO.getId(), pageReqVO.getSaleAuditStatus())
//                                : wholesaleSaleESRepository.findByBaseId(esDO.getId());
//                        // 如果有销售审核状态条件但找不到匹配记录，则返回null
//                        if (pageReqVO.getSaleAuditStatus() != null && !saleOpt.isPresent()) {
//                            return null;
//                        }
//                        if (saleOpt.isPresent()) {
//                            ErpWholesaleSaleESDO sale = saleOpt.get();
//                            //BeanUtils.copyProperties(sale, vo);
//                             // 设置销售表字段
//                             vo.setSalesperson(sale.getSalesperson());
//                             vo.setCustomerName(sale.getCustomerName());
//                             vo.setSaleTruckFee(sale.getTruckFee());
//                             vo.setSaleLogisticsFee(sale.getLogisticsFee());
//                             vo.setSaleOtherFees(sale.getOtherFees());
//                             vo.setSaleAfterSalesStatus(sale.getSaleAfterSalesStatus());
//                             vo.setSaleAfterSalesSituation(sale.getSaleAfterSalesSituation());
//                             vo.setSaleAfterSalesAmount(sale.getSaleAfterSalesAmount());
//                             // 设置销售售后时间（将LocalDateTime转换为String）
//                             vo.setSaleAfterSalesTime(sale.getSaleAfterSalesTime() != null ?
//                             sale.getSaleAfterSalesTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
//                             null);
//                             vo.setTransferPerson(sale.getTransferPerson());
//                             vo.setSaleAuditStatus(sale.getSaleAuditStatus());
//                            vo.setSaleRemark(sale.getSaleRemark());
//                            // 从ES查询销售价格信息
//                            Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
//                                    purchaseOpt.map(ErpWholesalePurchaseESDO::getComboProductId).orElse(null),
//                                    sale.getCustomerName());
//                            if (salePriceOpt.isPresent()) {
//                                vo.setSalePrice(salePriceOpt.get().getWholesalePrice());
//
//                                // 计算销售总额 = 销售单价*数量 + 销售货拉拉费 + 销售物流费用 + 销售其他费用
//                                if (purchaseOpt.isPresent()) {
//                                    BigDecimal totalSaleAmount = salePriceOpt.get().getWholesalePrice()
//                                            .multiply(BigDecimal.valueOf(esDO.getProductQuantity()))
//                                            .add(sale.getTruckFee() != null ? sale.getTruckFee() : BigDecimal.ZERO)
//                                            .add(sale.getLogisticsFee() != null ? sale.getLogisticsFee() : BigDecimal.ZERO)
//                                            .add(sale.getOtherFees() != null ? sale.getOtherFees() : BigDecimal.ZERO);
//                                    vo.setTotalSaleAmount(totalSaleAmount);
//                                }
//                            }
//                        }
//
//                        return vo;
//                    })
//                    .collect(Collectors.toList());
//
//            return new PageResult<>(voList, searchHits.getTotalHits());
//
//        } catch (Exception e) {
//            System.out.println("ES查询失败，回退到数据库查询: " + e.getMessage());
//            return wholesaleMapper.selectPage(pageReqVO);
//        }
//    }


    @Override
    public PageResult<ErpWholesaleRespVO> getWholesaleVOPage(ErpWholesalePageReqVO pageReqVO) {
        try {
            System.out.println("批发传入的参数"+pageReqVO);
            // 1. 检查数据库是否有数据
            long dbCount = wholesaleCombinedMapper.selectCount(null);

            // 2. 检查ES索引是否存在
            IndexOperations combinedIndexOps = elasticsearchRestTemplate.indexOps(ErpWholesaleCombinedESDO.class);
            boolean indexExists = combinedIndexOps.exists();

            // 3. 检查ES数据量
            long esCount = 0;
            if (indexExists) {
                esCount = elasticsearchRestTemplate.count(
                    new NativeSearchQueryBuilder().build(),
                    ErpWholesaleCombinedESDO.class
                );
            }

            // 4. 处理数据库和ES数据不一致的情况
            if (dbCount == 0) {
                if (indexExists && esCount > 0) {
                    // 数据库为空但ES有数据，清空ES
                    wholesaleCombinedESRepository.deleteAll();
                    System.out.println("检测到数据库为空但ES有数据，已清空ES索引");
                }
                return new PageResult<>(Collections.emptyList(), 0L);
            }

            // 5. 如果索引不存在或数据不一致，重建索引
            if (!indexExists || esCount != dbCount) {
                combinedIndexOps.create();
                combinedIndexOps.putMapping(combinedIndexOps.createMapping(ErpWholesaleCombinedESDO.class));
                fullSyncToES();
                System.out.println("检测到ES索引不存在或数据不一致，已重建索引");
            }

            // 构建查询条件
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()))
                    .withTrackTotalHits(true)
                    .withSort(Sort.by(Sort.Direction.DESC, "id"));

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            if (StringUtils.isNotBlank(pageReqVO.getNo())) {
                boolQuery.must(QueryBuilders.matchQuery("no", pageReqVO.getNo()));
            }
            if (StringUtils.isNotBlank(pageReqVO.getReceiverName())) {
                boolQuery.must(QueryBuilders.matchQuery("receiverName", pageReqVO.getReceiverName()));
            }
            if (pageReqVO.getCreateTime() != null && pageReqVO.getCreateTime().length == 2) {
                boolQuery.must(QueryBuilders.rangeQuery("createTime")
                        .gte(pageReqVO.getCreateTime()[0])
                        .lte(pageReqVO.getCreateTime()[1]));
            }

            queryBuilder.withQuery(boolQuery);

            if (pageReqVO.getPageNo() > 1) {
                return handleDeepPagination(pageReqVO, queryBuilder);
            }

            // 执行查询
            SearchHits<ErpWholesaleCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpWholesaleCombinedESDO.class,
                    IndexCoordinates.of("erp_wholesale_combined"));
            System.out.println("查询批发结果总数: " + searchHits.getTotalHits());

            List<ErpWholesaleRespVO> voList = searchHits.stream()
                    .map(SearchHit::getContent)
                    .map(combined -> {
                        ErpWholesaleRespVO vo = BeanUtils.toBean(combined, ErpWholesaleRespVO.class)
                                .setOtherFees(combined.getPurchaseOtherFees());

                        // 查询组品信息并设置到respVO
                        if (combined.getComboProductId() != null) {
                            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combined.getComboProductId());
                            if (comboProductOpt.isPresent()) {
                                ErpComboProductES comboProduct = comboProductOpt.get();
                                vo.setShippingCode(comboProduct.getShippingCode())
                                  .setProductName(comboProduct.getName())
                                  .setPurchaser(comboProduct.getPurchaser())
                                  .setSupplier(comboProduct.getSupplier())
                                  .setPurchasePrice(comboProduct.getWholesalePrice())
                                  .setComboProductNo(comboProduct.getNo());

                                // 查询销售价格
                                Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                                        combined.getComboProductId(),
                                        combined.getCustomerName());
                                if (salePriceOpt.isPresent()) {
                                    vo.setSalePrice(salePriceOpt.get().getWholesalePrice());
                                }

                                // 计算采购总额
                                BigDecimal totalPurchaseAmount = comboProduct.getWholesalePrice()
                                        .multiply(BigDecimal.valueOf(combined.getProductQuantity()))
                                        .add(combined.getPurchaseTruckFee() != null ? combined.getPurchaseTruckFee() : BigDecimal.ZERO)
                                        .add(combined.getPurchaseLogisticsFee() != null ? combined.getPurchaseLogisticsFee() : BigDecimal.ZERO)
                                        .add(combined.getPurchaseOtherFees() != null ? combined.getPurchaseOtherFees() : BigDecimal.ZERO);
                                vo.setTotalPurchaseAmount(totalPurchaseAmount);

                                // 计算销售总额
                                BigDecimal salePrice = vo.getSalePrice() != null ? vo.getSalePrice() : BigDecimal.ZERO;
                                BigDecimal totalSaleAmount = salePrice
                                        .multiply(BigDecimal.valueOf(combined.getProductQuantity()))
                                        .add(combined.getSaleTruckFee() != null ? combined.getSaleTruckFee() : BigDecimal.ZERO)
                                        .add(combined.getSaleLogisticsFee() != null ? combined.getSaleLogisticsFee() : BigDecimal.ZERO)
                                        .add(combined.getSaleOtherFees() != null ? combined.getSaleOtherFees() : BigDecimal.ZERO);
                                vo.setTotalSaleAmount(totalSaleAmount);
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

//    private PageResult<ErpWholesaleRespVO> handleDeepPagination(ErpWholesalePageReqVO pageReqVO,
//                                                                NativeSearchQueryBuilder queryBuilder) {
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
//            SearchHits<ErpWholesaleBaseESDO> prevHits = elasticsearchRestTemplate.search(
//                    prevQuery,
//                    ErpWholesaleBaseESDO.class,
//                    IndexCoordinates.of("erp_wholesale_base"));
//
//            if (prevHits.isEmpty()) {
//                return new PageResult<>(Collections.emptyList(), prevHits.getTotalHits());
//            }
//
//            // 设置search_after参数
//            SearchHit<ErpWholesaleBaseESDO> lastHit = prevHits.getSearchHits().get(0);
//            query.setSearchAfter(lastHit.getSortValues());
//        }
//
//        // 3. 执行查询
//        SearchHits<ErpWholesaleBaseESDO> searchHits = elasticsearchRestTemplate.search(
//                query,
//                ErpWholesaleBaseESDO.class,
//                IndexCoordinates.of("erp_wholesale_base"));
//
//        // 4. 转换为VO并补充关联数据
//        List<ErpWholesaleRespVO> voList = searchHits.stream()
//        .map(SearchHit::getContent)
//        .map(esDO -> {
//            //ErpWholesaleRespVO vo = BeanUtils.toBean(esDO, ErpWholesaleRespVO.class);
//            ErpWholesaleRespVO vo = new ErpWholesaleRespVO();
//            // 设置基础表字段
//            vo.setId(esDO.getId());
//            vo.setNo(esDO.getNo());
//            vo.setOrderNumber(esDO.getOrderNumber());
//            vo.setLogisticsNumber(esDO.getLogisticsNumber());
//            vo.setReceiverName(esDO.getReceiverName());
//            vo.setReceiverPhone(esDO.getReceiverPhone());
//            vo.setReceiverAddress(esDO.getReceiverAddress());
//            vo.setProductQuantity(esDO.getProductQuantity());
//            vo.setProductSpecification(esDO.getProductSpecification());
//            vo.setRemark(esDO.getRemark());
//            vo.setCreator(esDO.getCreator());
//            vo.setCreateTime(esDO.getCreateTime());
//            vo.setAfterSalesStatus(esDO.getAfterSalesStatus());
//            vo.setAfterSalesTime(esDO.getAfterSalesTime());
//            // 从ES查询采购信息（通过baseId匹配）
//            Optional<ErpWholesalePurchaseESDO> purchaseOpt = pageReqVO.getPurchaseAuditStatus() != null
//                    ? wholesalePurchaseESRepository.findByBaseIdAndPurchaseAuditStatus(esDO.getId(), pageReqVO.getPurchaseAuditStatus())
//                    : wholesalePurchaseESRepository.findByBaseId(esDO.getId());
//            // 如果有采购审核状态条件但找不到匹配记录，则返回null
//            if (pageReqVO.getPurchaseAuditStatus() != null && !purchaseOpt.isPresent()) {
//                return null;
//            }
//            if (purchaseOpt.isPresent()) {
//                ErpWholesalePurchaseESDO purchase = purchaseOpt.get();
//                //BeanUtils.copyProperties(purchase, vo);
//                vo.setTruckFee(purchase.getTruckFee());
//                vo.setOtherFees(purchase.getOtherFees());
//                vo.setPurchaseAfterSalesStatus(purchase.getPurchaseAfterSalesStatus());
//                vo.setPurchaseAfterSalesSituation(purchase.getPurchaseAfterSalesSituation());
//                vo.setPurchaseAfterSalesAmount(purchase.getPurchaseAfterSalesAmount());
//                vo.setPurchaseAuditStatus(purchase.getPurchaseAuditStatus());
//                vo.setPurchaseRemark(purchase.getPurchaseRemark());
//                // 从ES查询组品信息
//                Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(purchase.getComboProductId());
//                if (comboProductOpt.isPresent()) {
//                    ErpComboProductES comboProduct = comboProductOpt.get();
//                    vo.setProductName(comboProduct.getName());
//                    vo.setShippingCode(comboProduct.getShippingCode());
//                    vo.setPurchaser(comboProduct.getPurchaser());
//                    vo.setSupplier(comboProduct.getSupplier());
//                    vo.setPurchasePrice(comboProduct.getWholesalePrice());
//                    vo.setComboProductNo(comboProduct.getNo());
//
//                    // 直接使用采购记录中的运费值
//                    vo.setLogisticsFee(purchase.getLogisticsFee());
//                    // 计算采购运费
//
//                    // 计算采购总额 = 采购单价*数量 + 货拉拉费 + 物流费用 + 其他费用
//                    BigDecimal totalPurchaseAmount = comboProduct.getWholesalePrice()
//                            .multiply(BigDecimal.valueOf(esDO.getProductQuantity()))
//                            .add(purchase.getTruckFee() != null ? purchase.getTruckFee() : BigDecimal.ZERO)
//                            .add(purchase.getLogisticsFee()!= null? purchase.getLogisticsFee() : BigDecimal.ZERO)
//                            .add(purchase.getOtherFees() != null ? purchase.getOtherFees() : BigDecimal.ZERO);
//                    vo.setTotalPurchaseAmount(totalPurchaseAmount);
//                }
//            }
//
//            // 从ES查询销售信息（通过baseId匹配）
//            Optional<ErpWholesaleSaleESDO> saleOpt = pageReqVO.getSaleAuditStatus() != null
//                    ? wholesaleSaleESRepository.findByBaseIdAndSaleAuditStatus(esDO.getId(), pageReqVO.getSaleAuditStatus())
//                    : wholesaleSaleESRepository.findByBaseId(esDO.getId());
//            // 如果有销售审核状态条件但找不到匹配记录，则返回null
//            if (pageReqVO.getSaleAuditStatus() != null && !saleOpt.isPresent()) {
//                return null;
//            }
//            if (saleOpt.isPresent()) {
//                ErpWholesaleSaleESDO sale = saleOpt.get();
//                //BeanUtils.copyProperties(sale, vo);
//                 // 设置销售表字段
//                 vo.setSalesperson(sale.getSalesperson());
//                 vo.setCustomerName(sale.getCustomerName());
//                 vo.setSaleTruckFee(sale.getTruckFee());
//                 vo.setSaleLogisticsFee(sale.getLogisticsFee());
//                 vo.setSaleOtherFees(sale.getOtherFees());
//                 vo.setSaleAfterSalesStatus(sale.getSaleAfterSalesStatus());
//                 vo.setSaleAfterSalesSituation(sale.getSaleAfterSalesSituation());
//                 vo.setSaleAfterSalesAmount(sale.getSaleAfterSalesAmount());
//                 // 设置销售售后时间（将LocalDateTime转换为String）
//                 vo.setSaleAfterSalesTime(sale.getSaleAfterSalesTime() != null ?
//                 sale.getSaleAfterSalesTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
//                 null);
//                 vo.setTransferPerson(sale.getTransferPerson());
//                 vo.setSaleAuditStatus(sale.getSaleAuditStatus());
//                vo.setSaleRemark(sale.getSaleRemark());
//                // 从ES查询销售价格信息
//                Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
//                        purchaseOpt.map(ErpWholesalePurchaseESDO::getComboProductId).orElse(null),
//                        sale.getCustomerName());
//                if (salePriceOpt.isPresent()) {
//                    vo.setSalePrice(salePriceOpt.get().getWholesalePrice());
//
//                    // 计算销售总额 = 销售单价*数量 + 销售货拉拉费 + 销售物流费用 + 销售其他费用
//                    if (purchaseOpt.isPresent()) {
//                        BigDecimal totalSaleAmount = salePriceOpt.get().getWholesalePrice()
//                                .multiply(BigDecimal.valueOf(esDO.getProductQuantity()))
//                                .add(sale.getTruckFee() != null ? sale.getTruckFee() : BigDecimal.ZERO)
//                                .add(sale.getLogisticsFee() != null ? sale.getLogisticsFee() : BigDecimal.ZERO)
//                                .add(sale.getOtherFees() != null ? sale.getOtherFees() : BigDecimal.ZERO);
//                        vo.setTotalSaleAmount(totalSaleAmount);
//                    }
//                }
//            }
//
//            return vo;
//        })
//        .collect(Collectors.toList());
//
//        return new PageResult<>(voList, searchHits.getTotalHits());
//    }

    private PageResult<ErpWholesaleRespVO> handleDeepPagination(ErpWholesalePageReqVO pageReqVO,
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

            SearchHits<ErpWholesaleCombinedESDO> prevHits = elasticsearchRestTemplate.search(
                    prevQuery,
                    ErpWholesaleCombinedESDO.class,
                    IndexCoordinates.of("erp_wholesale_combined"));

            if (prevHits.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), prevHits.getTotalHits());
            }

            // 设置search_after参数
            SearchHit<ErpWholesaleCombinedESDO> lastHit = prevHits.getSearchHits().get(0);
            query.setSearchAfter(lastHit.getSortValues());
        }

        // 3. 执行查询
        SearchHits<ErpWholesaleCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                query,
                ErpWholesaleCombinedESDO.class,
                IndexCoordinates.of("erp_wholesale_combined"));

        // 4. 转换为VO并补充关联数据
        List<ErpWholesaleRespVO> voList = searchHits.stream()
                .map(SearchHit::getContent)
                .map(combined -> {
                    ErpWholesaleRespVO vo = BeanUtils.toBean(combined, ErpWholesaleRespVO.class)
                            .setOtherFees(combined.getPurchaseOtherFees());

                    // 查询组品信息并设置到respVO
                    if (combined.getComboProductId() != null) {
                        Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combined.getComboProductId());
                        if (comboProductOpt.isPresent()) {
                            ErpComboProductES comboProduct = comboProductOpt.get();
                            vo.setShippingCode(comboProduct.getShippingCode())
                              .setProductName(comboProduct.getName())
                              .setPurchaser(comboProduct.getPurchaser())
                              .setSupplier(comboProduct.getSupplier())
                              .setPurchasePrice(comboProduct.getWholesalePrice())
                              .setComboProductNo(comboProduct.getNo());

                            // 查询销售价格
                            Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                                    combined.getComboProductId(),
                                    combined.getCustomerName());
                            if (salePriceOpt.isPresent()) {
                                vo.setSalePrice(salePriceOpt.get().getWholesalePrice());
                            }

                            // 计算采购总额
                            BigDecimal totalPurchaseAmount = comboProduct.getWholesalePrice()
                                    .multiply(BigDecimal.valueOf(combined.getProductQuantity()))
                                    .add(combined.getPurchaseTruckFee() != null ? combined.getPurchaseTruckFee() : BigDecimal.ZERO)
                                    .add(combined.getPurchaseLogisticsFee() != null ? combined.getPurchaseLogisticsFee() : BigDecimal.ZERO)
                                    .add(combined.getPurchaseOtherFees() != null ? combined.getPurchaseOtherFees() : BigDecimal.ZERO);
                            vo.setTotalPurchaseAmount(totalPurchaseAmount);

                            // 计算销售总额
                            BigDecimal salePrice = vo.getSalePrice() != null ? vo.getSalePrice() : BigDecimal.ZERO;
                            BigDecimal totalSaleAmount = salePrice
                                    .multiply(BigDecimal.valueOf(combined.getProductQuantity()))
                                    .add(combined.getSaleTruckFee() != null ? combined.getSaleTruckFee() : BigDecimal.ZERO)
                                    .add(combined.getSaleLogisticsFee() != null ? combined.getSaleLogisticsFee() : BigDecimal.ZERO)
                                    .add(combined.getSaleOtherFees() != null ? combined.getSaleOtherFees() : BigDecimal.ZERO);
                            vo.setTotalSaleAmount(totalSaleAmount);
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(voList, searchHits.getTotalHits());
    }

     // 计算采购运费
     private BigDecimal calculatePurchaseLogisticsFee(ErpComboProductES comboProduct, Integer productQuantity, BigDecimal truckFee) {
        if (comboProduct.getShippingFeeType() == 0) {
            return comboProduct.getFixedShippingFee();
        } else if (comboProduct.getShippingFeeType() == 1) {
            if (comboProduct.getAdditionalItemQuantity() > 0) {
                return comboProduct.getAdditionalItemPrice()
                        .multiply(BigDecimal.valueOf(Math.ceil(productQuantity / (double) comboProduct.getAdditionalItemQuantity())));
            }
        } else if (comboProduct.getShippingFeeType() == 2) {
            BigDecimal totalWeight = comboProduct.getWeight().multiply(BigDecimal.valueOf(productQuantity));
            if (totalWeight.compareTo(comboProduct.getFirstWeight()) <= 0) {
                return comboProduct.getFirstWeightPrice();
            } else {
                return comboProduct.getFirstWeightPrice().add(
                        comboProduct.getAdditionalWeightPrice().multiply(
                                BigDecimal.valueOf(Math.ceil(
                                        (totalWeight.subtract(comboProduct.getFirstWeight()))
                                                .divide(comboProduct.getAdditionalWeight(), 0, RoundingMode.UP).doubleValue()
                                ))
                        )
                );
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<ErpWholesaleRespVO> getWholesaleVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpWholesaleBaseDO> list = wholesaleMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpWholesaleRespVO.class);
    }

    @Override
    public Map<Long, ErpWholesaleRespVO> getWholesaleVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getWholesaleVOList(ids), ErpWholesaleRespVO::getId);
    }

    @Override
    public List<ErpWholesaleBaseDO> getWholesaleList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return wholesaleMapper.selectBatchIds(ids);
    }

//    private void validateWholesaleForCreateOrUpdate(Long id, ErpWholesaleSaveReqVO reqVO) {
//        // 1. 校验订单号唯一
//        ErpWholesaleBaseDO wholesale = wholesaleMapper.selectByNo(reqVO.getNo());
//        if (wholesale != null && !wholesale.getId().equals(id)) {
//            throw exception(WHOLESALE_NO_EXISTS);
//        }
//    }
    private void validateWholesaleForCreateOrUpdate(Long id, ErpWholesaleSaveReqVO reqVO) {
        // 1. 校验订单号唯一
        ErpWholesaleCombinedDO wholesale = wholesaleCombinedMapper.selectByNo(reqVO.getNo());
        if (wholesale != null && !wholesale.getId().equals(id)) {
            throw exception(WHOLESALE_NO_EXISTS);
        }
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updatePurchaseAuditStatus(Long id, Integer purchaseAuditStatus, BigDecimal otherFees) {
//        // 1. 校验存在 - 使用ES查询
//        Optional<ErpWholesaleBaseESDO> baseOpt = wholesaleBaseESRepository.findById(id);
//        if (!baseOpt.isPresent()) {
//            throw exception(WHOLESALE_NOT_EXISTS);
//        }
//
//        // 2. 获取当前采购审核状态 - 使用ES查询
//        Optional<ErpWholesalePurchaseESDO> purchaseOpt = wholesalePurchaseESRepository.findByBaseId(id);
//        if (!purchaseOpt.isPresent()) {
//            throw exception(WHOLESALE_NOT_EXISTS);
//        }
//
//        // 3. 校验状态是否重复
//        if (purchaseOpt.get().getPurchaseAuditStatus() != null &&
//            purchaseOpt.get().getPurchaseAuditStatus().equals(purchaseAuditStatus)) {
//            throw exception(WHOLESALE_PROCESS_FAIL);
//        }
//
//        // 4. 更新采购审核状态
//        ErpWholesalePurchaseDO updateObj = new ErpWholesalePurchaseDO()
//                .setPurchaseAuditStatus(purchaseAuditStatus)
//                .setOtherFees(otherFees);
//
//        // 根据审核状态设置相应时间
//        if (purchaseAuditStatus == 20) { // 审核通过
//            updateObj.setPurchaseApprovalTime(LocalDateTime.now());
//        } else if (purchaseAuditStatus == 10) { // 反审核
//            updateObj.setPurchaseUnapproveTime(LocalDateTime.now());
//        }
//
//        purchaseMapper.update(updateObj, new LambdaUpdateWrapper<ErpWholesalePurchaseDO>()
//                .eq(ErpWholesalePurchaseDO::getBaseId, id));
//
//        // 5. 同步到ES
//        syncPurchaseToES(purchaseOpt.get().getId());
//        syncBaseToES(id);
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateSaleAuditStatus(Long id, Integer saleAuditStatus, BigDecimal otherFees) {
//        // 1. 校验存在 - 使用ES查询
//        Optional<ErpWholesaleBaseESDO> baseOpt = wholesaleBaseESRepository.findById(id);
//        if (!baseOpt.isPresent()) {
//            throw exception(WHOLESALE_NOT_EXISTS);
//        }
//
//        // 2. 获取当前销售审核状态 - 使用ES查询
//        Optional<ErpWholesaleSaleESDO> saleOpt = wholesaleSaleESRepository.findByBaseId(id);
//        if (!saleOpt.isPresent()) {
//            throw exception(WHOLESALE_NOT_EXISTS);
//        }
//
//        // 3. 校验状态是否重复
//        if (saleOpt.get().getSaleAuditStatus() != null &&
//            saleOpt.get().getSaleAuditStatus().equals(saleAuditStatus)) {
//            throw exception(WHOLESALE_PROCESS_FAIL);
//        }
//
//        // 4. 更新销售审核状态
//        ErpWholesaleSaleDO updateObj = new ErpWholesaleSaleDO()
//                .setSaleAuditStatus(saleAuditStatus)
//                .setOtherFees(otherFees);
//
//        // 根据审核状态设置相应时间
//        if (saleAuditStatus == 20) { // 审核通过
//            updateObj.setSaleApprovalTime(LocalDateTime.now());
//        } else if (saleAuditStatus == 10) { // 反审核
//            updateObj.setSaleUnapproveTime(LocalDateTime.now());
//        }
//
//        saleMapper.update(updateObj, new LambdaUpdateWrapper<ErpWholesaleSaleDO>()
//                .eq(ErpWholesaleSaleDO::getBaseId, id));
//
//        // 5. 同步到ES
//        syncSaleToES(saleOpt.get().getId());
//        syncBaseToES(id);
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updatePurchaseAfterSales(ErpWholesalePurchaseAfterSalesUpdateReqVO reqVO) {
//        // 1. 校验存在 - 使用ES查询
//        Optional<ErpWholesaleBaseESDO> baseOpt = wholesaleBaseESRepository.findById(reqVO.getId());
//        if (!baseOpt.isPresent()) {
//            throw exception(WHOLESALE_NOT_EXISTS);
//        }
//
//        // 2. 获取采购记录 - 使用ES查询
//        Optional<ErpWholesalePurchaseESDO> purchaseOpt = wholesalePurchaseESRepository.findByBaseId(reqVO.getId());
//        if (!purchaseOpt.isPresent()) {
//            throw exception(WHOLESALE_NOT_EXISTS);
//        }
//
//        LocalDateTime purchaseAfterSalesTime = parseDateTime(reqVO.getPurchaseAfterSalesTime());
//        LocalDateTime afterSalesTime = parseDateTime(reqVO.getAfterSalesTime());
//
//        // 3. 更新基础表售后信息
//        ErpWholesaleBaseDO baseUpdateObj = new ErpWholesaleBaseDO()
//                .setId(reqVO.getId())
//                .setAfterSalesStatus(reqVO.getAfterSalesStatus())
//                .setAfterSalesTime(afterSalesTime);
//        wholesaleMapper.updateById(baseUpdateObj);
//
//        // 4. 更新采购售后信息
//        ErpWholesalePurchaseDO purchaseUpdateObj = new ErpWholesalePurchaseDO()
//                .setPurchaseAfterSalesStatus(reqVO.getPurchaseAfterSalesStatus())
//                .setPurchaseAfterSalesAmount(reqVO.getPurchaseAfterSalesAmount())
//                .setPurchaseAfterSalesTime(purchaseAfterSalesTime);
//
//        purchaseMapper.update(purchaseUpdateObj, new LambdaUpdateWrapper<ErpWholesalePurchaseDO>()
//                .eq(ErpWholesalePurchaseDO::getBaseId, reqVO.getId()));
//
//        // 5. 同步到ES
//        syncBaseToES(reqVO.getId());
//        syncPurchaseToES(purchaseOpt.get().getId());
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateSaleAfterSales(ErpWholesaleSaleAfterSalesUpdateReqVO reqVO) {
//        // 1. 校验存在 - 使用ES查询
//        Optional<ErpWholesaleBaseESDO> baseOpt = wholesaleBaseESRepository.findById(reqVO.getId());
//        if (!baseOpt.isPresent()) {
//            throw exception(WHOLESALE_NOT_EXISTS);
//        }
//
//        // 2. 获取销售记录 - 使用ES查询
//        Optional<ErpWholesaleSaleESDO> saleOpt = wholesaleSaleESRepository.findByBaseId(reqVO.getId());
//        if (!saleOpt.isPresent()) {
//            throw exception(WHOLESALE_NOT_EXISTS);
//        }
//        LocalDateTime saleAfterSalesTime = parseDateTime(reqVO.getSaleAfterSalesTime());
//        LocalDateTime afterSalesTime = parseDateTime(reqVO.getAfterSalesTime());
//
//        // 3. 更新基础表售后信息
//        ErpWholesaleBaseDO baseUpdateObj = new ErpWholesaleBaseDO()
//                .setId(reqVO.getId())
//                .setAfterSalesStatus(reqVO.getAfterSalesStatus())
//                .setAfterSalesTime(afterSalesTime);
//        wholesaleMapper.updateById(baseUpdateObj);
//
//        // 4. 更新销售售后信息
//        ErpWholesaleSaleDO saleUpdateObj = new ErpWholesaleSaleDO()
//                .setSaleAfterSalesStatus(reqVO.getSaleAfterSalesStatus())
//                .setSaleAfterSalesAmount(reqVO.getSaleAfterSalesAmount())
//                .setSaleAfterSalesTime(saleAfterSalesTime);
//
//        saleMapper.update(saleUpdateObj, new LambdaUpdateWrapper<ErpWholesaleSaleDO>()
//                .eq(ErpWholesaleSaleDO::getBaseId, reqVO.getId()));
//
//        // 5. 同步到ES
//        syncBaseToES(reqVO.getId());
//        syncSaleToES(saleOpt.get().getId());
//
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseAuditStatus(Long id, Integer purchaseAuditStatus, BigDecimal otherFees) {
        // 1. 校验存在 - 使用合并表查询
        Optional<ErpWholesaleCombinedESDO> combinedOpt = wholesaleCombinedESRepository.findById(id);
        if (!combinedOpt.isPresent()) {
            throw exception(WHOLESALE_NOT_EXISTS);
        }

        // 2. 校验状态是否重复
        if (combinedOpt.get().getPurchaseAuditStatus() != null &&
            combinedOpt.get().getPurchaseAuditStatus().equals(purchaseAuditStatus)) {
            throw exception(WHOLESALE_PROCESS_FAIL);
        }

        // 3. 更新采购审核状态
        ErpWholesaleCombinedDO updateObj = new ErpWholesaleCombinedDO()
                .setId(id)
                .setPurchaseAuditStatus(purchaseAuditStatus)
                .setPurchaseOtherFees(otherFees);

        // 根据审核状态设置相应时间
        if (purchaseAuditStatus == 20) { // 审核通过
            updateObj.setPurchaseApprovalTime(LocalDateTime.now());
        } else if (purchaseAuditStatus == 10) { // 反审核
            updateObj.setPurchaseUnapproveTime(LocalDateTime.now());
        }

        wholesaleCombinedMapper.updateById(updateObj);

        // 4. 同步到ES
        syncCombinedToES(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleAuditStatus(Long id, Integer saleAuditStatus, BigDecimal otherFees) {
        // 1. 校验存在 - 使用合并表查询
        Optional<ErpWholesaleCombinedESDO> combinedOpt = wholesaleCombinedESRepository.findById(id);
        if (!combinedOpt.isPresent()) {
            throw exception(WHOLESALE_NOT_EXISTS);
        }

        // 2. 校验状态是否重复
        if (combinedOpt.get().getSaleAuditStatus() != null &&
            combinedOpt.get().getSaleAuditStatus().equals(saleAuditStatus)) {
            throw exception(WHOLESALE_PROCESS_FAIL);
        }

        // 3. 更新销售审核状态
        ErpWholesaleCombinedDO updateObj = new ErpWholesaleCombinedDO()
                .setId(id)
                .setSaleAuditStatus(saleAuditStatus)
                .setSaleOtherFees(otherFees);

        // 根据审核状态设置相应时间
        if (saleAuditStatus == 20) { // 审核通过
            updateObj.setSaleApprovalTime(LocalDateTime.now());
        } else if (saleAuditStatus == 10) { // 反审核
            updateObj.setSaleUnapproveTime(LocalDateTime.now());
        }

        wholesaleCombinedMapper.updateById(updateObj);

        // 4. 同步到ES
        syncCombinedToES(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseAfterSales(ErpWholesalePurchaseAfterSalesUpdateReqVO reqVO) {
        // 1. 校验存在 - 使用合并表查询
        Optional<ErpWholesaleCombinedESDO> combinedOpt = wholesaleCombinedESRepository.findById(reqVO.getId());
        if (!combinedOpt.isPresent()) {
            throw exception(WHOLESALE_NOT_EXISTS);
        }

        LocalDateTime purchaseAfterSalesTime = parseDateTime(reqVO.getPurchaseAfterSalesTime());
        LocalDateTime afterSalesTime = parseDateTime(reqVO.getAfterSalesTime());

        // 2. 更新售后信息
        ErpWholesaleCombinedDO updateObj = new ErpWholesaleCombinedDO()
                .setId(reqVO.getId())
                .setAfterSalesStatus(reqVO.getAfterSalesStatus())
                .setAfterSalesTime(afterSalesTime)
                .setPurchaseAfterSalesStatus(reqVO.getPurchaseAfterSalesStatus())
                .setPurchaseAfterSalesAmount(reqVO.getPurchaseAfterSalesAmount())
                .setPurchaseAfterSalesTime(purchaseAfterSalesTime);

        wholesaleCombinedMapper.updateById(updateObj);

        // 3. 同步到ES
        syncCombinedToES(reqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleAfterSales(ErpWholesaleSaleAfterSalesUpdateReqVO reqVO) {
        // 1. 校验存在 - 使用合并表查询
        Optional<ErpWholesaleCombinedESDO> combinedOpt = wholesaleCombinedESRepository.findById(reqVO.getId());
        if (!combinedOpt.isPresent()) {
            throw exception(WHOLESALE_NOT_EXISTS);
        }

        LocalDateTime saleAfterSalesTime = parseDateTime(reqVO.getSaleAfterSalesTime());
        LocalDateTime afterSalesTime = parseDateTime(reqVO.getAfterSalesTime());

        // 2. 更新售后信息
        ErpWholesaleCombinedDO updateObj = new ErpWholesaleCombinedDO()
                .setId(reqVO.getId())
                .setAfterSalesStatus(reqVO.getAfterSalesStatus())
                .setAfterSalesTime(afterSalesTime)
                .setSaleAfterSalesStatus(reqVO.getSaleAfterSalesStatus())
                .setSaleAfterSalesAmount(reqVO.getSaleAfterSalesAmount())
                .setSaleAfterSalesTime(saleAfterSalesTime);

        wholesaleCombinedMapper.updateById(updateObj);

        // 3. 同步到ES
        syncCombinedToES(reqVO.getId());
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
    public ErpWholesaleImportRespVO importWholesaleList(List<ErpWholesaleImportExcelVO> list, Boolean updateSupport) {
        if (CollUtil.isEmpty(list)) {
            throw exception(WHOLESALE_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpWholesaleImportRespVO respVO = ErpWholesaleImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理数据
        List<ErpWholesaleCombinedDO> createList = new ArrayList<>();
        List<ErpWholesaleCombinedDO> updateList = new ArrayList<>();
        List<ErpWholesaleCombinedESDO> esCreateList = new ArrayList<>();
        List<ErpWholesaleCombinedESDO> esUpdateList = new ArrayList<>();

        try {
            // 批量查询组品信息
            Set<String> comboProductNos = list.stream()
                    .map(ErpWholesaleImportExcelVO::getComboProductNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> comboProductIdMap = comboProductNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(comboProductESRepository.findByNoIn(new ArrayList<>(comboProductNos)),
                            ErpComboProductES::getNo, ErpComboProductES::getId);

            // 批量查询已存在的记录
            Set<String> noSet = list.stream()
                    .map(ErpWholesaleImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpWholesaleCombinedDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(wholesaleCombinedMapper.selectListByNoIn(noSet), ErpWholesaleCombinedDO::getNo);

            // 批量转换数据
            for (int i = 0; i < list.size(); i++) {
                ErpWholesaleImportExcelVO importVO = list.get(i);
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
                            throw exception(WHOLESALE_CUSTOMER_NOT_EXISTS, importVO.getCustomerName());
                        }
                    }

                    // 获取组品ID
                    Long comboProductId = null;
                    if (StrUtil.isNotBlank(importVO.getComboProductNo())) {
                        comboProductId = comboProductIdMap.get(importVO.getComboProductNo());
                        if (comboProductId == null) {
                            throw exception(WHOLESALE_COMBO_PRODUCT_NOT_EXISTS, importVO.getComboProductNo());
                        }
                    }

                    // 判断是否支持更新
                    ErpWholesaleCombinedDO existDistribution = existMap.get(importVO.getNo());
                    if (existDistribution == null) {
                        // 创建逻辑
                        ErpWholesaleCombinedDO combined = BeanUtils.toBean(importVO, ErpWholesaleCombinedDO.class)
                                .setId(IdUtil.getSnowflakeNextId()).setPurchaseAuditStatus(ErpAuditStatus.PROCESS.getStatus())  // 设置采购审核状态
                                .setSaleAuditStatus(ErpAuditStatus.PROCESS.getStatus()).setPurchaseAfterSalesStatus(30).setSaleAfterSalesStatus(30);;
                        combined.setComboProductId(comboProductId);
                        if (StrUtil.isEmpty(combined.getNo())) {
                            combined.setNo(noRedisDAO.generate(ErpNoRedisDAO.WHOLESALE_NO_PREFIX));
                        }
                        createList.add(combined);
                        esCreateList.add(BeanUtils.toBean(combined, ErpWholesaleCombinedESDO.class).setCreator(username).setCreateTime(now));
                        respVO.getCreateNames().add(combined.getNo());
                    } else if (updateSupport) {
                        // 更新逻辑
                        ErpWholesaleCombinedDO combined = BeanUtils.toBean(importVO, ErpWholesaleCombinedDO.class);
                        combined.setId(existDistribution.getId());
                        combined.setComboProductId(comboProductId);
                        updateList.add(combined);
                        esUpdateList.add(BeanUtils.toBean(combined, ErpWholesaleCombinedESDO.class));
                        respVO.getUpdateNames().add(combined.getNo());
                    }
                    else {
                        throw exception(WHOLESALE_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
                    }
                } catch (ServiceException ex) {
                    String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知批发订单";
                    respVO.getFailureNames().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知批发订单";
                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                wholesaleCombinedMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(wholesaleCombinedMapper::updateById);
            }

            // 批量保存到ES
            if (CollUtil.isNotEmpty(esCreateList)) {
                wholesaleCombinedESRepository.saveAll(esCreateList);
            }
            if (CollUtil.isNotEmpty(esUpdateList)) {
                wholesaleCombinedESRepository.saveAll(esUpdateList);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }
}
