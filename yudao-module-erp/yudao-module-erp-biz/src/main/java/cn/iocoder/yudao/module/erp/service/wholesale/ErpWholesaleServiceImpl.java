package cn.iocoder.yudao.module.erp.service.wholesale;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.*;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO.ErpWholesaleImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO.ErpWholesaleImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO.ErpWholesalePurchaseAuditImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ImportVO.ErpWholesaleSaleAuditImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePricePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpWholesaleMissingPriceVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionCombinedDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductItemES;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSalePriceMapper;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.*;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleCombinedMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesalePurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleSaleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpCustomerMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductESRepository;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalePriceESRepository;
import cn.iocoder.yudao.module.erp.service.sale.ErpSalespersonService;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
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

    @Resource
    private ErpSalePriceMapper salePriceMapper;

    @Resource
    private ErpCustomerMapper customerMapper;

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
        ErpWholesaleCombinedESDO esDO = new ErpWholesaleCombinedESDO();

        // 先复制基础字段
        BeanUtils.copyProperties(combined, esDO);

        // 填充keyword字段（与产品表保持一致）- 确保所有Text字段都有对应的keyword字段
        esDO.setNoKeyword(combined.getNo());
        esDO.setLogisticsNumberKeyword(combined.getLogisticsNumber());
        esDO.setReceiverNameKeyword(combined.getReceiverName());
        esDO.setReceiverPhoneKeyword(combined.getReceiverPhone());
        esDO.setReceiverAddressKeyword(combined.getReceiverAddress());
        esDO.setAfterSalesStatusKeyword(combined.getAfterSalesStatus());
        esDO.setSalespersonKeyword(combined.getSalesperson());
        esDO.setCustomerNameKeyword(combined.getCustomerName());
        esDO.setTransferPersonKeyword(combined.getTransferPerson());
        esDO.setCreatorKeyword(combined.getCreator());
        esDO.setUpdaterKeyword(combined.getUpdater());

        // 设置产品规格的keyword字段（从批发表本身的数据获取）
        esDO.setProductSpecificationKeyword(combined.getProductSpecification());

        // 如果有组品ID，从组品表获取相关信息并填充到ES对象中
        if (combined.getComboProductId() != null) {
            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combined.getComboProductId());
            if (comboProductOpt.isPresent()) {
                ErpComboProductES comboProduct = comboProductOpt.get();
                // 设置组品相关字段及其keyword字段
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

                // 添加调试信息
                System.out.println("转换批发表到ES: ID=" + combined.getId() +
                                 ", no='" + combined.getNo() + "'" +
                                 ", no_keyword='" + esDO.getNoKeyword() + "'" +
                                 ", combo_product_no='" + comboProduct.getNo() + "'" +
                                 ", combo_product_no_keyword='" + esDO.getComboProductNoKeyword() + "'");
            }
        }

        return esDO;
    }

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

        // 2. 更新数据库记录 - 将ES数据转换为DO，然后有选择性地更新
        ErpWholesaleCombinedDO updateDO = BeanUtils.toBean(combined, ErpWholesaleCombinedDO.class);

        // 有选择性地更新字段，只更新前端传入的有值字段
        if (updateReqVO.getLogisticsNumber() != null) {
            updateDO.setLogisticsNumber(updateReqVO.getLogisticsNumber());
        }
        if (updateReqVO.getReceiverName() != null) {
            updateDO.setReceiverName(updateReqVO.getReceiverName());
        }
        if (updateReqVO.getReceiverPhone() != null) {
            updateDO.setReceiverPhone(updateReqVO.getReceiverPhone());
        }
        if (updateReqVO.getReceiverAddress() != null) {
            updateDO.setReceiverAddress(updateReqVO.getReceiverAddress());
        }
        if (updateReqVO.getComboProductId() != null) {
            updateDO.setComboProductId(updateReqVO.getComboProductId());
        }
        if (updateReqVO.getProductSpecification() != null) {
            updateDO.setProductSpecification(updateReqVO.getProductSpecification());
        }
        if (updateReqVO.getProductQuantity() != null) {
            updateDO.setProductQuantity(updateReqVO.getProductQuantity());
        }
        if (updateReqVO.getAfterSalesStatus() != null) {
            updateDO.setAfterSalesStatus(updateReqVO.getAfterSalesStatus());
        }
        if (updateReqVO.getAfterSalesTime() != null) {
            updateDO.setAfterSalesTime(parseDateTime(updateReqVO.getAfterSalesTime()));
        }
        if (updateReqVO.getRemark() != null) {
            updateDO.setRemark(updateReqVO.getRemark());
        }
        if (updateReqVO.getPurchaseRemark() != null) {
            updateDO.setPurchaseRemark(updateReqVO.getPurchaseRemark());
        }
        if (updateReqVO.getSalesperson() != null) {
            updateDO.setSalesperson(updateReqVO.getSalesperson());
        }
        if (updateReqVO.getCustomerName() != null) {
            updateDO.setCustomerName(updateReqVO.getCustomerName());
        }
        if (updateReqVO.getTransferPerson() != null) {
            updateDO.setTransferPerson(updateReqVO.getTransferPerson());
        }
        if (updateReqVO.getSaleRemark() != null) {
            updateDO.setSaleRemark(updateReqVO.getSaleRemark());
        }
        if (updateReqVO.getOtherFees() != null) {
            updateDO.setPurchaseOtherFees(updateReqVO.getOtherFees());
        }
        if (updateReqVO.getTruckFee() != null) {
            updateDO.setPurchaseTruckFee(updateReqVO.getTruckFee());
        }
        if (updateReqVO.getLogisticsFee() != null) {
            updateDO.setPurchaseLogisticsFee(updateReqVO.getLogisticsFee());
        }
        if (updateReqVO.getSaleOtherFees() != null) {
            updateDO.setSaleOtherFees(updateReqVO.getSaleOtherFees());
        }
        if (updateReqVO.getSaleTruckFee() != null) {
            updateDO.setSaleTruckFee(updateReqVO.getSaleTruckFee());
        }
        if (updateReqVO.getSaleLogisticsFee() != null) {
            updateDO.setSaleLogisticsFee(updateReqVO.getSaleLogisticsFee());
        }

        wholesaleCombinedMapper.updateById(updateDO);

        // 3. 更新ES记录 - 直接使用更新后的DO转换为ES
        ErpWholesaleCombinedESDO combinedESDO = convertCombinedToES(updateDO);
        // 保留原有的创建者和创建时间
        combinedESDO.setCreator(combined.getCreator());
        combinedESDO.setCreateTime(combined.getCreateTime());
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

        // 3. 🔥 实时获取组品信息并计算相关字段
        if (combined.getComboProductId() != null) {
            try {
                // 3.1 从ES实时查询组品基本信息
                Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combined.getComboProductId());
                if (comboProductOpt.isPresent()) {
                    ErpComboProductES comboProduct = comboProductOpt.get();

                    // 设置基础信息
                    respVO.setShippingCode(comboProduct.getShippingCode());
                    respVO.setPurchaser(comboProduct.getPurchaser());
                    respVO.setSupplier(comboProduct.getSupplier());
                    respVO.setComboProductNo(comboProduct.getNo());

                    // 🔥 实时计算产品名称、采购单价等字段
                    String realTimeProductName = calculateRealTimeProductName(combined.getComboProductId());
                    BigDecimal realTimePurchasePrice = calculateRealTimePurchasePrice(combined.getComboProductId());

                    // 如果实时计算失败，使用ES中的缓存数据
                    respVO.setProductName(realTimeProductName != null ? realTimeProductName : comboProduct.getName());
                    respVO.setPurchasePrice(realTimePurchasePrice != null ? realTimePurchasePrice : comboProduct.getWholesalePrice());

                    // 查询销售价格
                    Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                            combined.getComboProductId(),
                            combined.getCustomerName());
                    if (salePriceOpt.isPresent()) {
                        respVO.setSalePrice(salePriceOpt.get().getWholesalePrice());
                    }

                    // 计算采购总额 - 使用实时计算的采购单价
                    BigDecimal finalPurchasePrice = realTimePurchasePrice != null ? realTimePurchasePrice : comboProduct.getWholesalePrice();
                    BigDecimal totalPurchaseAmount = finalPurchasePrice
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
            } catch (Exception e) {
                System.err.println("实时获取组品信息失败，回退到缓存数据: " + e.getMessage());
                // 回退到原有的缓存数据逻辑
                Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combined.getComboProductId());
                if (comboProductOpt.isPresent()) {
                    ErpComboProductES comboProduct = comboProductOpt.get();
                    respVO.setShippingCode(comboProduct.getShippingCode())
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
                        respVO.setSalePrice(salePriceOpt.get().getWholesalePrice());
                    }

                    // 计算采购总额
                    BigDecimal totalPurchaseAmount = comboProduct.getWholesalePrice()
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
        }

        return respVO;
    }

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

            // 4. 调试：检查ES中的实际数据内容
            if (indexExists && esCount > 0) {
                System.out.println("=== ES数据内容检查 ===");
                NativeSearchQuery debugQuery = new NativeSearchQueryBuilder()
                        .withPageable(PageRequest.of(0, 5))
                        .build();
                SearchHits<ErpWholesaleCombinedESDO> debugHits = elasticsearchRestTemplate.search(
                        debugQuery,
                        ErpWholesaleCombinedESDO.class,
                        IndexCoordinates.of("erp_wholesale_combined"));

                for (SearchHit<ErpWholesaleCombinedESDO> hit : debugHits) {
                    ErpWholesaleCombinedESDO content = hit.getContent();
                    System.out.println("ES记录 - ID: " + content.getId() + ", no: '" + content.getNo() + "'");
                    System.out.println("  logisticsNumber: '" + content.getLogisticsNumber() + "'");
                    System.out.println("  receiverName: '" + content.getReceiverName() + "'");
                }
                System.out.println("=== ES数据内容检查结束 ===");
            }

            // 5. 处理数据库和ES数据不一致的情况
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
                combinedIndexOps.putMapping(combinedIndexOps.createMapping(ErpWholesaleCombinedESDO.class));
                System.out.println("已设置字段映射");
                // 全量同步数据
                fullSyncToES();
                System.out.println("ES索引重建和数据同步完成");
            }

            // 构建查询条件
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()))
                    .withTrackTotalHits(true)
                    .withSort(Sort.by(Sort.Direction.DESC, "id"));

            // 3. 添加查询条件 - 完全使用代发表搜索策略
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 订单编号搜索 - 完全使用产品表的简化搜索策略
            if (StrUtil.isNotBlank(pageReqVO.getNo())) {
                BoolQueryBuilder noQuery = QueryBuilders.boolQuery();
                String no = pageReqVO.getNo().trim();

                // 添加调试信息
                System.out.println("=== 订单编号搜索调试 ===");
                System.out.println("查询关键词: '" + no + "', 长度: " + no.length());

                BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();

                // 🔥 简化的编号匹配策略：只保留核心匹配逻辑
                // 由于no字段现在是keyword类型，不会分词，可以大幅简化匹配策略

                System.out.println("使用简化的编号匹配策略，查询词长度: " + no.length());

                // 第一优先级：完全精确匹配（最高权重）
                multiMatchQuery.should(QueryBuilders.termQuery("no_keyword", no).boost(1000000.0f));
                System.out.println("添加精确匹配: no_keyword = '" + no + "', 权重: 1000000");

                // 第二优先级：前缀匹配（支持"PFJL2025"匹配"PFJL2025..."）
                multiMatchQuery.should(QueryBuilders.prefixQuery("no_keyword", no).boost(100000.0f));
                System.out.println("添加前缀匹配: no_keyword 前缀 = '" + no + "', 权重: 100000");

                // 第三优先级：包含匹配（支持任意位置的模糊匹配）
                multiMatchQuery.should(QueryBuilders.wildcardQuery("no_keyword", "*" + no + "*").boost(50000.0f));
                System.out.println("添加包含匹配: *" + no + "*, 权重: 50000");

                // 注意：移除复杂的智能子字符串匹配，因为keyword字段已经足够支持模糊匹配

                multiMatchQuery.minimumShouldMatch(1);
                noQuery.must(multiMatchQuery);
                boolQuery.must(noQuery);

                System.out.println("=== 订单编号搜索调试结束 ===");
            }

            // 物流单号搜索 - 使用代发表策略并优化长字符串匹配
            if (StrUtil.isNotBlank(pageReqVO.getLogisticsNumber())) {
                BoolQueryBuilder logisticsNumberQuery = QueryBuilders.boolQuery();
                String logisticsNumber = pageReqVO.getLogisticsNumber().trim();

                BoolQueryBuilder multiMatchQuery = QueryBuilders.boolQuery();
                multiMatchQuery.should(QueryBuilders.termQuery("logistics_number_keyword", logisticsNumber).boost(1000000.0f));
                multiMatchQuery.should(QueryBuilders.prefixQuery("logistics_number_keyword", logisticsNumber).boost(100000.0f));
                multiMatchQuery.should(QueryBuilders.wildcardQuery("logistics_number_keyword", "*" + logisticsNumber + "*").boost(10000.0f));

                // 优化子字符串匹配策略
                if (logisticsNumber.length() >= 2 && logisticsNumber.length() <= 15) {
                    for (int i = 1; i < logisticsNumber.length(); i++) {
                        String substring = logisticsNumber.substring(i);
                        if (substring.length() >= 4 && !containsTooManyRepeatedChars(substring)) { // 避免重复字符过多的子字符串
                            multiMatchQuery.should(QueryBuilders.wildcardQuery("logistics_number_keyword", "*" + substring + "*").boost(3000.0f));
                        }
                    }
                } else if (logisticsNumber.length() > 15) {
                    for (int i = Math.max(1, logisticsNumber.length() - 10); i < logisticsNumber.length(); i++) {
                        String substring = logisticsNumber.substring(i);
                        if (substring.length() >= 4) {
                            multiMatchQuery.should(QueryBuilders.wildcardQuery("logistics_number_keyword", "*" + substring + "*").boost(2000.0f));
                        }
                    }
                }

                if (logisticsNumber.length() == 1) {
                    multiMatchQuery.should(QueryBuilders.matchQuery("logistics_number", logisticsNumber).operator(Operator.OR).boost(800.0f));
                } else if (logisticsNumber.length() == 2) {
                    multiMatchQuery.should(QueryBuilders.matchQuery("logistics_number", logisticsNumber).operator(Operator.AND).boost(600.0f));
                    multiMatchQuery.should(QueryBuilders.matchPhraseQuery("logistics_number", logisticsNumber).boost(1200.0f));
                    multiMatchQuery.should(QueryBuilders.matchQuery("logistics_number", logisticsNumber).operator(Operator.OR).boost(400.0f));
                } else {
                    multiMatchQuery.should(QueryBuilders.matchQuery("logistics_number", logisticsNumber).operator(Operator.AND).boost(500.0f));
                    multiMatchQuery.should(QueryBuilders.matchPhraseQuery("logistics_number", logisticsNumber).boost(1000.0f));
                }

                multiMatchQuery.minimumShouldMatch(1);
                logisticsNumberQuery.must(multiMatchQuery);
                boolQuery.must(logisticsNumberQuery);
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
            if (pageReqVO.getPurchaseAuditStatus() != null) {
                boolQuery.must(QueryBuilders.termQuery("purchase_audit_status", pageReqVO.getPurchaseAuditStatus()));
            }
            if (pageReqVO.getSaleAuditStatus() != null) {
                boolQuery.must(QueryBuilders.termQuery("sale_audit_status", pageReqVO.getSaleAuditStatus()));
            }

            // 时间范围查询
            if (pageReqVO.getCreateTime() != null && pageReqVO.getCreateTime().length == 2) {
                // 前端传递的是字符串数组，直接使用字符串进行范围查询
                System.out.println("创建时间查询: [" + pageReqVO.getCreateTime()[0] + ", " + pageReqVO.getCreateTime()[1] + "]");
                boolQuery.must(QueryBuilders.rangeQuery("create_time")
                        .gte(pageReqVO.getCreateTime()[0])
                        .lte(pageReqVO.getCreateTime()[1]));
            }

            // 售后时间范围查询
            if (pageReqVO.getAfterSalesTime() != null && pageReqVO.getAfterSalesTime().length == 2) {
                // 前端传递的是字符串数组，直接使用字符串进行范围查询
                System.out.println("售后时间查询: [" + pageReqVO.getAfterSalesTime()[0] + ", " + pageReqVO.getAfterSalesTime()[1] + "]");
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

                SearchHits<ErpWholesaleCombinedESDO> exactHits = elasticsearchRestTemplate.search(
                        exactTestQuery,
                        ErpWholesaleCombinedESDO.class,
                        IndexCoordinates.of("erp_wholesale_combined"));

                System.out.println("精确匹配测试结果: " + exactHits.getTotalHits() + " 条记录");
                for (SearchHit<ErpWholesaleCombinedESDO> hit : exactHits) {
                    System.out.println("  精确匹配到: ID=" + hit.getContent().getId() + ", no='" + hit.getContent().getNo() + "', 评分=" + hit.getScore());
                }
                System.out.println("=== 精确匹配测试结束 ===");
            }

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
                                .setTruckFee(combined.getPurchaseTruckFee())
                                .setLogisticsFee(combined.getPurchaseLogisticsFee())
                                .setOtherFees(combined.getPurchaseOtherFees());

                        // 🔥 实时获取组品信息并计算相关字段
                        if (combined.getComboProductId() != null) {
                            try {
                                // 从ES实时查询组品基本信息
                                Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combined.getComboProductId());
                                if (comboProductOpt.isPresent()) {
                                    ErpComboProductES comboProduct = comboProductOpt.get();

                                    // 设置基础信息
                                    vo.setShippingCode(comboProduct.getShippingCode());
                                    vo.setPurchaser(comboProduct.getPurchaser());
                                    vo.setSupplier(comboProduct.getSupplier());
                                    vo.setComboProductNo(comboProduct.getNo());

                                    // 🔥 实时计算产品名称、采购单价等字段
                                    String realTimeProductName = calculateRealTimeProductName(combined.getComboProductId());
                                    BigDecimal realTimePurchasePrice = calculateRealTimePurchasePrice(combined.getComboProductId());

                                    // 如果实时计算失败，使用ES中的缓存数据
                                    vo.setProductName(realTimeProductName != null ? realTimeProductName : comboProduct.getName());
                                    vo.setPurchasePrice(realTimePurchasePrice != null ? realTimePurchasePrice : comboProduct.getWholesalePrice());

                                    // 查询销售价格
                                    Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                                            combined.getComboProductId(),
                                            combined.getCustomerName());
                                    if (salePriceOpt.isPresent()) {
                                        vo.setSalePrice(salePriceOpt.get().getWholesalePrice());
                                    }

                                    // 计算采购总额 - 使用实时计算的采购单价
                                    BigDecimal finalPurchasePrice = realTimePurchasePrice != null ? realTimePurchasePrice : comboProduct.getWholesalePrice();
                                    BigDecimal totalPurchaseAmount = finalPurchasePrice
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
                            } catch (Exception e) {
                                System.err.println("实时获取组品信息失败，回退到缓存数据: " + e.getMessage());
                                // 回退到原有的缓存数据逻辑
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

                    // 🔥 实时获取组品信息并计算相关字段
                    if (combined.getComboProductId() != null) {
                        try {
                            // 从ES实时查询组品基本信息
                            Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(combined.getComboProductId());
                            if (comboProductOpt.isPresent()) {
                                ErpComboProductES comboProduct = comboProductOpt.get();

                                // 设置基础信息
                                vo.setShippingCode(comboProduct.getShippingCode());
                                vo.setPurchaser(comboProduct.getPurchaser());
                                vo.setSupplier(comboProduct.getSupplier());
                                vo.setComboProductNo(comboProduct.getNo());

                                // 🔥 实时计算产品名称、采购单价等字段
                                String realTimeProductName = calculateRealTimeProductName(combined.getComboProductId());
                                BigDecimal realTimePurchasePrice = calculateRealTimePurchasePrice(combined.getComboProductId());

                                // 如果实时计算失败，使用ES中的缓存数据
                                vo.setProductName(realTimeProductName != null ? realTimeProductName : comboProduct.getName());
                                vo.setPurchasePrice(realTimePurchasePrice != null ? realTimePurchasePrice : comboProduct.getWholesalePrice());

                                // 查询销售价格
                                Optional<ErpSalePriceESDO> salePriceOpt = salePriceESRepository.findByGroupProductIdAndCustomerName(
                                        combined.getComboProductId(),
                                        combined.getCustomerName());
                                if (salePriceOpt.isPresent()) {
                                    vo.setSalePrice(salePriceOpt.get().getWholesalePrice());
                                }

                                // 计算采购总额 - 使用实时计算的采购单价
                                BigDecimal finalPurchasePrice = realTimePurchasePrice != null ? realTimePurchasePrice : comboProduct.getWholesalePrice();
                                BigDecimal totalPurchaseAmount = finalPurchasePrice
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
                        } catch (Exception e) {
                            System.err.println("实时获取组品信息失败，回退到缓存数据: " + e.getMessage());
                            // 回退到原有的缓存数据逻辑
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

    private void validateWholesaleForCreateOrUpdate(Long id, ErpWholesaleSaveReqVO reqVO) {
        // 1. 校验订单号唯一
        ErpWholesaleCombinedDO wholesale = wholesaleCombinedMapper.selectByNo(reqVO.getNo());
        if (wholesale != null && !wholesale.getId().equals(id)) {
            throw exception(WHOLESALE_NO_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseAuditStatus(Long id, Integer purchaseAuditStatus, BigDecimal otherFees, BigDecimal purchaseAuditTotalAmount) {
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
                .setPurchaseOtherFees(otherFees)
                .setPurchaseAuditTotalAmount(purchaseAuditTotalAmount);

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
    public void batchUpdatePurchaseAuditStatus(List<Long> ids, Integer purchaseAuditStatus) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Long id : ids) {
            // 1. 校验存在
            Optional<ErpWholesaleCombinedESDO> combinedOpt = wholesaleCombinedESRepository.findById(id);
            if (!combinedOpt.isPresent()) {
                continue; // 跳过不存在的记录
            }

            // 2. 校验状态是否重复
            if (combinedOpt.get().getPurchaseAuditStatus() != null &&
                combinedOpt.get().getPurchaseAuditStatus().equals(purchaseAuditStatus)) {
                continue; // 跳过状态相同的记录
            }

            // 3. 更新采购审核状态
            ErpWholesaleCombinedDO updateObj = new ErpWholesaleCombinedDO()
                    .setId(id)
                    .setPurchaseAuditStatus(purchaseAuditStatus);

            // 根据审核状态设置相应时间
            if (purchaseAuditStatus == 20) { // 审核通过
                updateObj.setPurchaseApprovalTime(now);
            } else if (purchaseAuditStatus == 10) { // 反审核
                updateObj.setPurchaseUnapproveTime(now);
            }

            wholesaleCombinedMapper.updateById(updateObj);

            // 4. 同步到ES
            syncCombinedToES(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleAuditStatus(Long id, Integer saleAuditStatus, BigDecimal otherFees, BigDecimal saleAuditTotalAmount) {
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
                .setSaleOtherFees(otherFees)
                .setSaleAuditTotalAmount(saleAuditTotalAmount);

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
    public void batchUpdateSaleAuditStatus(List<Long> ids, Integer saleAuditStatus) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Long id : ids) {
            // 1. 校验存在
            Optional<ErpWholesaleCombinedESDO> combinedOpt = wholesaleCombinedESRepository.findById(id);
            if (!combinedOpt.isPresent()) {
                continue; // 跳过不存在的记录
            }

            // 2. 校验状态是否重复
            if (combinedOpt.get().getSaleAuditStatus() != null &&
                combinedOpt.get().getSaleAuditStatus().equals(saleAuditStatus)) {
                continue; // 跳过状态相同的记录
            }

            // 3. 更新销售审核状态
            ErpWholesaleCombinedDO updateObj = new ErpWholesaleCombinedDO()
                    .setId(id)
                    .setSaleAuditStatus(saleAuditStatus);

            // 根据审核状态设置相应时间
            if (saleAuditStatus == 20) { // 审核通过
                updateObj.setSaleApprovalTime(now);
            } else if (saleAuditStatus == 10) { // 反审核
                updateObj.setSaleUnapproveTime(now);
            }

            wholesaleCombinedMapper.updateById(updateObj);

            // 4. 同步到ES
            syncCombinedToES(id);
        }
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdatePurchaseAfterSales(List<Long> ids, Integer purchaseAfterSalesStatus) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Long id : ids) {
            // 1. 校验存在
            ErpWholesaleCombinedDO combined = wholesaleCombinedMapper.selectById(id);
            if (combined == null) {
                continue; // 跳过不存在的记录
            }

            // 2. 更新采购售后状态
            ErpWholesaleCombinedDO updateObj = new ErpWholesaleCombinedDO()
                    .setId(id)
                    .setPurchaseAfterSalesStatus(purchaseAfterSalesStatus)
                    .setPurchaseAfterSalesTime(now);

            wholesaleCombinedMapper.updateById(updateObj);

            // 3. 同步到ES
            syncCombinedToES(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSaleAfterSales(List<Long> ids, Integer saleAfterSalesStatus) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Long id : ids) {
            // 1. 校验存在
            ErpWholesaleCombinedDO combined = wholesaleCombinedMapper.selectById(id);
            if (combined == null) {
                continue; // 跳过不存在的记录
            }

            // 2. 更新销售售后状态
            ErpWholesaleCombinedDO updateObj = new ErpWholesaleCombinedDO()
                    .setId(id)
                    .setSaleAfterSalesStatus(saleAfterSalesStatus)
                    .setSaleAfterSalesTime(now);

            wholesaleCombinedMapper.updateById(updateObj);

            // 3. 同步到ES
            syncCombinedToES(id);
        }
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

        try {
            // 1. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateAllImportData(list, updateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 2. 批量处理数据
            List<ErpWholesaleCombinedDO> createList = new ArrayList<>();
            List<ErpWholesaleCombinedDO> updateList = new ArrayList<>();
            List<ErpWholesaleCombinedESDO> esCreateList = new ArrayList<>();
            List<ErpWholesaleCombinedESDO> esUpdateList = new ArrayList<>();

            // 3. 批量查询组品信息
            Set<String> comboProductNos = list.stream()
                    .map(ErpWholesaleImportExcelVO::getComboProductNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> comboProductIdMap = comboProductNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(comboProductESRepository.findByNoIn(new ArrayList<>(comboProductNos)),
                            ErpComboProductES::getNo, ErpComboProductES::getId);

            // 4. 批量查询已存在的记录
            Set<String> noSet = list.stream()
                    .map(ErpWholesaleImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpWholesaleCombinedDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(wholesaleCombinedMapper.selectListByNoIn(noSet), ErpWholesaleCombinedDO::getNo);

            // 5. 批量转换数据
            for (int i = 0; i < list.size(); i++) {
                ErpWholesaleImportExcelVO importVO = list.get(i);
                Long userId = SecurityFrameworkUtils.getLoginUserId();
                String username = cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getUsernameById(userId);
                LocalDateTime now = LocalDateTime.now();

                // 判断是否支持更新
                ErpWholesaleCombinedDO existDistribution = existMap.get(importVO.getNo());
                if (existDistribution == null) {
                    // 创建逻辑
                    ErpWholesaleCombinedDO combined = BeanUtils.toBean(importVO, ErpWholesaleCombinedDO.class)
                            .setId(IdUtil.getSnowflakeNextId()).setPurchaseAuditStatus(ErpAuditStatus.PROCESS.getStatus())  // 设置采购审核状态
                            .setSaleAuditStatus(ErpAuditStatus.PROCESS.getStatus()).setPurchaseAfterSalesStatus(30).setSaleAfterSalesStatus(30);;
                    combined.setComboProductId(comboProductIdMap.get(importVO.getComboProductNo()));
                        combined.setNo(noRedisDAO.generate(ErpNoRedisDAO.WHOLESALE_NO_PREFIX));
                    createList.add(combined);
                    esCreateList.add(BeanUtils.toBean(combined, ErpWholesaleCombinedESDO.class).setCreator(username).setCreateTime(now));
                    respVO.getCreateNames().add(combined.getNo());
                } else if (updateSupport) {
                    // 更新逻辑 - 只更新导入的字段，保留其他字段的原有数据
                    // 1. 数据库更新：从现有数据复制，然后只更新导入的字段
                    ErpWholesaleCombinedDO combined = BeanUtils.toBean(existDistribution, ErpWholesaleCombinedDO.class);

                    // 只更新导入的字段，且只有当导入值不为空时才更新
                    if (StrUtil.isNotBlank(importVO.getLogisticsNumber())) {
                        combined.setLogisticsNumber(importVO.getLogisticsNumber());
                    }
                    if (StrUtil.isNotBlank(importVO.getReceiverName())) {
                        combined.setReceiverName(importVO.getReceiverName());
                    }
                    if (StrUtil.isNotBlank(importVO.getReceiverPhone())) {
                        combined.setReceiverPhone(importVO.getReceiverPhone());
                    }
                    if (StrUtil.isNotBlank(importVO.getReceiverAddress())) {
                        combined.setReceiverAddress(importVO.getReceiverAddress());
                    }
                    if (StrUtil.isNotBlank(importVO.getRemark())) {
                        combined.setRemark(importVO.getRemark());
                    }
                    if (comboProductIdMap.get(importVO.getComboProductNo()) != null) {
                        combined.setComboProductId(comboProductIdMap.get(importVO.getComboProductNo()));
                    }
                    if (StrUtil.isNotBlank(importVO.getProductSpecification())) {
                        combined.setProductSpecification(importVO.getProductSpecification());
                    }
                    if (importVO.getProductQuantity() != null) {
                        combined.setProductQuantity(importVO.getProductQuantity());
                    }
                    // 销售相关字段 - 只有当值不为null时才更新
                    if (StrUtil.isNotBlank(importVO.getSalesperson())) {
                        combined.setSalesperson(importVO.getSalesperson());
                    }
                    if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                        combined.setCustomerName(importVO.getCustomerName());
                    }
                    if (StrUtil.isNotBlank(importVO.getTransferPerson())) {
                        combined.setTransferPerson(importVO.getTransferPerson());
                    }

                    // 添加到批量更新列表
                    updateList.add(combined);

                    // 2. ES更新：从现有数据复制，然后只更新导入的字段
                    ErpWholesaleCombinedESDO esUpdateDO = BeanUtils.toBean(existDistribution, ErpWholesaleCombinedESDO.class);
                    // 更新导入的字段 - 只有当值不为空时才更新
                    if (StrUtil.isNotBlank(importVO.getLogisticsNumber())) {
                        esUpdateDO.setLogisticsNumber(importVO.getLogisticsNumber());
                    }
                    if (StrUtil.isNotBlank(importVO.getReceiverName())) {
                        esUpdateDO.setReceiverName(importVO.getReceiverName());
                    }
                    if (StrUtil.isNotBlank(importVO.getReceiverPhone())) {
                        esUpdateDO.setReceiverPhone(importVO.getReceiverPhone());
                    }
                    if (StrUtil.isNotBlank(importVO.getReceiverAddress())) {
                        esUpdateDO.setReceiverAddress(importVO.getReceiverAddress());
                    }
                    if (StrUtil.isNotBlank(importVO.getRemark())) {
                        esUpdateDO.setRemark(importVO.getRemark());
                    }
                    if (comboProductIdMap.get(importVO.getComboProductNo()) != null) {
                        esUpdateDO.setComboProductId(comboProductIdMap.get(importVO.getComboProductNo()));
                    }
                    if (StrUtil.isNotBlank(importVO.getProductSpecification())) {
                        esUpdateDO.setProductSpecification(importVO.getProductSpecification());
                    }
                    if (importVO.getProductQuantity() != null) {
                        esUpdateDO.setProductQuantity(importVO.getProductQuantity());
                    }
                    // 销售相关字段 - 只有当值不为null时才更新
                    if (StrUtil.isNotBlank(importVO.getSalesperson())) {
                        esUpdateDO.setSalesperson(importVO.getSalesperson());
                    }
                    if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                        esUpdateDO.setCustomerName(importVO.getCustomerName());
                    }
                    if (StrUtil.isNotBlank(importVO.getTransferPerson())) {
                        esUpdateDO.setTransferPerson(importVO.getTransferPerson());
                    }

                    esUpdateList.add(esUpdateDO);
                    respVO.getUpdateNames().add(existDistribution.getNo());
                }
            }

            // 6. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                wholesaleCombinedMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                // 批量更新 - 使用批量更新操作
                wholesaleCombinedMapper.updateBatch(updateList);
            }

            // 7. 批量保存到ES
            if (CollUtil.isNotEmpty(esCreateList)) {
                wholesaleCombinedESRepository.saveAll(esCreateList);
            }
            if (CollUtil.isNotEmpty(esUpdateList)) {
                wholesaleCombinedESRepository.saveAll(esUpdateList);
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
    private Map<String, String> validateAllImportData(List<ErpWholesaleImportExcelVO> importList, Boolean updateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的批发订单
        Set<String> noSet = importList.stream()
                .map(ErpWholesaleImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, ErpWholesaleCombinedDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(wholesaleCombinedMapper.selectListByNoIn(noSet), ErpWholesaleCombinedDO::getNo);

        // 3. 批量查询所有组品编号，验证组品是否存在
        Set<String> comboProductNos = importList.stream()
                .map(ErpWholesaleImportExcelVO::getComboProductNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, Long> comboProductIdMap = new HashMap<>();
        if (!comboProductNos.isEmpty()) {
            List<ErpComboProductES> comboProducts = comboProductESRepository.findByNoIn(new ArrayList<>(comboProductNos));
            comboProductIdMap = convertMap(comboProducts, ErpComboProductES::getNo, ErpComboProductES::getId);
        }

        // 4. 批量查询所有销售人员名称，验证销售人员是否存在
        Set<String> salespersonNames = importList.stream()
                .map(ErpWholesaleImportExcelVO::getSalesperson)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, Boolean> salespersonExistsMap = new HashMap<>();
        for (String salespersonName : salespersonNames) {
            List<ErpSalespersonRespVO> salespersons = salespersonService.searchSalespersons(
                    new ErpSalespersonPageReqVO().setSalespersonName(salespersonName));
            salespersonExistsMap.put(salespersonName, CollUtil.isNotEmpty(salespersons));
        }

        // 5. 批量查询所有客户名称，验证客户是否存在
        Set<String> customerNames = importList.stream()
                .map(ErpWholesaleImportExcelVO::getCustomerName)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, Boolean> customerExistsMap = new HashMap<>();
        if (!customerNames.isEmpty()) {
            // 使用精确查询验证客户是否存在
            List<ErpCustomerDO> customers = customerMapper.selectListByNameIn(customerNames);
            Set<String> existingCustomerNames = customers.stream()
                    .map(ErpCustomerDO::getName)
                    .collect(Collectors.toSet());

            for (String customerName : customerNames) {
                customerExistsMap.put(customerName, existingCustomerNames.contains(customerName));
            }
        }

        // 6. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpWholesaleImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行";

            try {
                // 6.1 基础数据校验

                // 6.2 校验组品编号是否存在
                if (StrUtil.isNotBlank(importVO.getComboProductNo())) {
                    if (!comboProductIdMap.containsKey(importVO.getComboProductNo())) {
                        allErrors.put(errorKey, "组品编号不存在: " + importVO.getComboProductNo());
                        continue;
                    }
                }

                // 6.3 校验销售人员是否存在
                if (StrUtil.isNotBlank(importVO.getSalesperson())) {
                    Boolean salespersonExists = salespersonExistsMap.get(importVO.getSalesperson());
                    if (salespersonExists == null || !salespersonExists) {
                        allErrors.put(errorKey, "销售人员不存在: " + importVO.getSalesperson());
                        continue;
                    }
                }

                // 6.4 校验客户是否存在
                if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                    Boolean customerExists = customerExistsMap.get(importVO.getCustomerName());
                    if (customerExists == null || !customerExists) {
                        allErrors.put(errorKey, "客户名称不存在: " + importVO.getCustomerName());
                        continue;
                    }
                }

                // 6.5 校验产品数量
                if (importVO.getProductQuantity() != null && importVO.getProductQuantity() <= 0) {
                    allErrors.put(errorKey, "产品数量必须大于0");
                    continue;
                }


                // 6.7 判断是新增还是更新，并进行相应校验
                ErpWholesaleCombinedDO existWholesale = existMap.get(importVO.getNo());
                if (existWholesale == null) {
                    // 新增校验：校验批发订单编号唯一性
                    ErpWholesaleCombinedDO wholesale = wholesaleCombinedMapper.selectByNo(importVO.getNo());
                    if (wholesale != null) {
                        allErrors.put(errorKey, "批发订单编号已存在: " + importVO.getNo());
                        continue;
                    }
                } else if (updateSupport) {
                    // 更新校验：检查是否支持更新
                    // 这里可以添加更多的更新校验逻辑，比如检查审核状态等
                } else {
                    allErrors.put(errorKey, "批发订单编号已存在，不支持更新: " + importVO.getNo());
                    continue;
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
    private Map<String, String> validateDataTypeErrors(List<ErpWholesaleImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取批发订单编号
                String wholesaleNo = "未知批发订单编号";
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpWholesaleImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        wholesaleNo = importVO.getNo();
                    }
                }

                String errorKey = "第" + rowIndex + "行(" + wholesaleNo + ")";
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
     * 统一校验采购审核导入数据（包括数据类型校验和业务逻辑校验）
     * 如果出现任何错误信息都记录下来并返回，后续操作就不进行了
     */
    private Map<String, String> validatePurchaseAuditImportData(List<ErpWholesalePurchaseAuditImportExcelVO> importList, Boolean updateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validatePurchaseAuditDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的批发订单
        Set<String> noSet = importList.stream()
                .map(ErpWholesalePurchaseAuditImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, ErpWholesaleCombinedDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(wholesaleCombinedMapper.selectListByNoIn(noSet), ErpWholesaleCombinedDO::getNo);

        // 3. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpWholesalePurchaseAuditImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行";

            try {
                // 3.1 基础数据校验
                if (StrUtil.isBlank(importVO.getNo())) {
                    allErrors.put(errorKey, "批发订单编号不能为空");
                    continue;
                }

                // 3.2 校验批发订单是否存在
                ErpWholesaleCombinedDO existWholesale = existMap.get(importVO.getNo());
                if (existWholesale == null) {
                    allErrors.put(errorKey, "批发订单不存在: " + importVO.getNo());
                    continue;
                }

                // 3.3 校验金额字段
                if (importVO.getPurchaseOtherFees() != null && importVO.getPurchaseOtherFees().compareTo(BigDecimal.ZERO) < 0) {
                    allErrors.put(errorKey, "采购其他费用不能为负数");
                    continue;
                }
                if (importVO.getPurchaseAfterSalesAmount() != null && importVO.getPurchaseAfterSalesAmount().compareTo(BigDecimal.ZERO) < 0) {
                    allErrors.put(errorKey, "采购售后金额不能为负数");
                    continue;
                }

            } catch (Exception ex) {
                allErrors.put(errorKey, "系统异常: " + ex.getMessage());
            }
        }

        return allErrors;
    }

    /**
     * 采购审核数据类型校验前置检查
     * 检查所有转换错误，如果有错误则返回错误信息，不进行后续导入
     */
    private Map<String, String> validatePurchaseAuditDataTypeErrors(List<ErpWholesalePurchaseAuditImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取批发订单编号
                String wholesaleNo = "未知批发订单编号";
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpWholesalePurchaseAuditImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        wholesaleNo = importVO.getNo();
                    }
                }

                String errorKey = "第" + rowIndex + "行(" + wholesaleNo + ")";
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
     * 统一校验销售审核导入数据（包括数据类型校验和业务逻辑校验）
     * 如果出现任何错误信息都记录下来并返回，后续操作就不进行了
     */
    private Map<String, String> validateSaleAuditImportData(List<ErpWholesaleSaleAuditImportExcelVO> importList, Boolean updateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateSaleAuditDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的批发订单
        Set<String> noSet = importList.stream()
                .map(ErpWholesaleSaleAuditImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, ErpWholesaleCombinedDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(wholesaleCombinedMapper.selectListByNoIn(noSet), ErpWholesaleCombinedDO::getNo);

        // 3. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpWholesaleSaleAuditImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行";

            try {
                // 3.1 基础数据校验
                if (StrUtil.isBlank(importVO.getNo())) {
                    allErrors.put(errorKey, "批发订单编号不能为空");
                    continue;
                }

                // 3.2 校验批发订单是否存在
                ErpWholesaleCombinedDO existWholesale = existMap.get(importVO.getNo());
                if (existWholesale == null) {
                    allErrors.put(errorKey, "批发订单不存在: " + importVO.getNo());
                    continue;
                }

                // 3.3 校验金额字段
                if (importVO.getSaleOtherFees() != null && importVO.getSaleOtherFees().compareTo(BigDecimal.ZERO) < 0) {
                    allErrors.put(errorKey, "销售其他费用不能为负数");
                    continue;
                }
                if (importVO.getSaleAfterSalesAmount() != null && importVO.getSaleAfterSalesAmount().compareTo(BigDecimal.ZERO) < 0) {
                    allErrors.put(errorKey, "销售售后金额不能为负数");
                    continue;
                }

            } catch (Exception ex) {
                allErrors.put(errorKey, "系统异常: " + ex.getMessage());
            }
        }

        return allErrors;
    }

    /**
     * 销售审核数据类型校验前置检查
     * 检查所有转换错误，如果有错误则返回错误信息，不进行后续导入
     */
    private Map<String, String> validateSaleAuditDataTypeErrors(List<ErpWholesaleSaleAuditImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取批发订单编号
                String wholesaleNo = "未知批发订单编号";
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpWholesaleSaleAuditImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        wholesaleNo = importVO.getNo();
                    }
                }

                String errorKey = "第" + rowIndex + "行(" + wholesaleNo + ")";
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



    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpWholesaleImportRespVO importWholesalePurchaseAuditList(List<ErpWholesalePurchaseAuditImportExcelVO> list, Boolean updateSupport) {
        if (CollUtil.isEmpty(list)) {
            throw exception(WHOLESALE_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpWholesaleImportRespVO respVO = ErpWholesaleImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        try {
            // 1. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validatePurchaseAuditImportData(list, updateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 2. 批量处理数据
            List<ErpWholesaleCombinedDO> updateList = new ArrayList<>();
            List<ErpWholesaleCombinedESDO> esUpdateList = new ArrayList<>();

            // 3. 批量查询已存在的记录
            Set<String> noSet = list.stream()
                    .map(ErpWholesalePurchaseAuditImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpWholesaleCombinedDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(wholesaleCombinedMapper.selectListByNoIn(noSet), ErpWholesaleCombinedDO::getNo);

            // 4. 批量转换数据
            for (int i = 0; i < list.size(); i++) {
                ErpWholesalePurchaseAuditImportExcelVO importVO = list.get(i);

                // 更新逻辑 - 只更新采购审核相关字段
                ErpWholesaleCombinedDO existRecord = existMap.get(importVO.getNo());

                // 从现有数据复制，然后只更新导入的字段
                ErpWholesaleCombinedDO combined = BeanUtils.toBean(existRecord, ErpWholesaleCombinedDO.class);

                // 只有当值不为null时才更新
                if (importVO.getPurchaseOtherFees() != null) {
                    combined.setPurchaseOtherFees(importVO.getPurchaseOtherFees());
                }
                if (StrUtil.isNotBlank(importVO.getAfterSalesStatus())) {
                    combined.setAfterSalesStatus(importVO.getAfterSalesStatus());
                }
                if (importVO.getPurchaseAfterSalesAmount() != null) {
                    combined.setPurchaseAfterSalesAmount(importVO.getPurchaseAfterSalesAmount());
                }
                LocalDateTime now = LocalDateTime.now();
                combined.setAfterSalesTime(now);

                updateList.add(combined);

                // ES更新数据
                ErpWholesaleCombinedESDO esUpdateDO = BeanUtils.toBean(existRecord, ErpWholesaleCombinedESDO.class);
                if (importVO.getPurchaseOtherFees() != null) {
                    esUpdateDO.setPurchaseOtherFees(importVO.getPurchaseOtherFees());
                }
                if (StrUtil.isNotBlank(importVO.getAfterSalesStatus())) {
                    esUpdateDO.setAfterSalesStatus(importVO.getAfterSalesStatus());
                }
                if (importVO.getPurchaseAfterSalesAmount() != null) {
                    esUpdateDO.setPurchaseAfterSalesAmount(importVO.getPurchaseAfterSalesAmount());
                }


                esUpdateDO.setAfterSalesTime(now);

                esUpdateList.add(esUpdateDO);
                respVO.getUpdateNames().add(existRecord.getNo());
            }

            // 5. 批量保存到数据库
            if (CollUtil.isNotEmpty(updateList)) {
                wholesaleCombinedMapper.updateBatch(updateList);
            }

            // 6. 批量保存到ES
            if (CollUtil.isNotEmpty(esUpdateList)) {
                wholesaleCombinedESRepository.saveAll(esUpdateList);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        } finally {
            // 清除转换错误
            ConversionErrorHolder.clearErrors();
        }

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpWholesaleImportRespVO importWholesaleSaleAuditList(List<ErpWholesaleSaleAuditImportExcelVO> list, Boolean updateSupport) {
        if (CollUtil.isEmpty(list)) {
            throw exception(WHOLESALE_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpWholesaleImportRespVO respVO = ErpWholesaleImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        try {
            // 1. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateSaleAuditImportData(list, updateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 2. 批量处理数据
            List<ErpWholesaleCombinedDO> updateList = new ArrayList<>();
            List<ErpWholesaleCombinedESDO> esUpdateList = new ArrayList<>();

            // 3. 批量查询已存在的记录
            Set<String> noSet = list.stream()
                    .map(ErpWholesaleSaleAuditImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpWholesaleCombinedDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(wholesaleCombinedMapper.selectListByNoIn(noSet), ErpWholesaleCombinedDO::getNo);

            // 4. 批量转换数据
            for (int i = 0; i < list.size(); i++) {
                ErpWholesaleSaleAuditImportExcelVO importVO = list.get(i);

                // 更新逻辑 - 只更新销售审核相关字段
                ErpWholesaleCombinedDO existRecord = existMap.get(importVO.getNo());

                // 从现有数据复制，然后只更新导入的字段
                ErpWholesaleCombinedDO combined = BeanUtils.toBean(existRecord, ErpWholesaleCombinedDO.class);

                // 只有当值不为null时才更新
                if (importVO.getSaleOtherFees() != null) {
                    combined.setSaleOtherFees(importVO.getSaleOtherFees());
                }
                if (StrUtil.isNotBlank(importVO.getAfterSalesStatus())) {
                    combined.setAfterSalesStatus(importVO.getAfterSalesStatus());
                }
                if (importVO.getSaleAfterSalesAmount() != null) {
                    combined.setSaleAfterSalesAmount(importVO.getSaleAfterSalesAmount());
                }

                LocalDateTime now = LocalDateTime.now();
                combined.setAfterSalesTime(now);
                updateList.add(combined);

                // ES更新数据
                ErpWholesaleCombinedESDO esUpdateDO = BeanUtils.toBean(existRecord, ErpWholesaleCombinedESDO.class);
                if (importVO.getSaleOtherFees() != null) {
                    esUpdateDO.setSaleOtherFees(importVO.getSaleOtherFees());
                }
                if (StrUtil.isNotBlank(importVO.getAfterSalesStatus())) {
                    esUpdateDO.setAfterSalesStatus(importVO.getAfterSalesStatus());
                }
                if (importVO.getSaleAfterSalesAmount() != null) {
                    esUpdateDO.setSaleAfterSalesAmount(importVO.getSaleAfterSalesAmount());
                }
                esUpdateDO.setAfterSalesTime(now);
                esUpdateList.add(esUpdateDO);
                respVO.getUpdateNames().add(existRecord.getNo());
            }

            // 5. 批量保存到数据库
            if (CollUtil.isNotEmpty(updateList)) {
                wholesaleCombinedMapper.updateBatch(updateList);
            }

            // 6. 批量保存到ES
            if (CollUtil.isNotEmpty(esUpdateList)) {
                wholesaleCombinedESRepository.saveAll(esUpdateList);
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

        // 🔥 简化的编号匹配策略：只保留核心匹配逻辑
        // 由于字段现在是keyword类型，不会分词，可以大幅简化匹配策略

        // 第一优先级：完全精确匹配（最高权重）
        multiMatchQuery.should(QueryBuilders.termQuery(keywordFieldName, keyword).boost(1000000.0f));

        // 第二优先级：前缀匹配
        multiMatchQuery.should(QueryBuilders.prefixQuery(keywordFieldName, keyword).boost(100000.0f));

        // 第三优先级：包含匹配（支持任意位置的模糊匹配）
        multiMatchQuery.should(QueryBuilders.wildcardQuery(keywordFieldName, "*" + keyword + "*").boost(50000.0f));

        // 注意：移除复杂的智能子字符串匹配，因为keyword字段已经足够支持模糊匹配

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

    @Override
    public PageResult<ErpWholesaleMissingPriceVO> getWholesaleMissingPrices(ErpSalePricePageReqVO pageReqVO) {
        try {
            // 构建ES查询 - 查询所有批发订单
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 添加搜索条件
            if (pageReqVO.getGroupProductId() != null) {
                boolQuery.must(QueryBuilders.termQuery("combo_product_id", pageReqVO.getGroupProductId()));
            }
            if (StrUtil.isNotBlank(pageReqVO.getCustomerName())) {
                boolQuery.must(QueryBuilders.wildcardQuery("customer_name.keyword", "*" + pageReqVO.getCustomerName() + "*"));
            }

            queryBuilder.withQuery(boolQuery);
            // 设置大的查询数量以获取所有数据进行分组
            queryBuilder.withPageable(PageRequest.of(0, 10000));
            queryBuilder.withSort(Sort.by(Sort.Direction.DESC, "create_time"));

            // 执行搜索 - 查询CombinedESDO
            SearchHits<ErpWholesaleCombinedESDO> searchHits = elasticsearchRestTemplate.search(
                    queryBuilder.build(),
                    ErpWholesaleCombinedESDO.class);

            // 按组品ID和客户名称分组
            Map<String, List<ErpWholesaleCombinedESDO>> groupedData = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .filter(esDO -> esDO.getComboProductId() != null && StrUtil.isNotBlank(esDO.getCustomerName()))
                .collect(Collectors.groupingBy(esDO ->
                    esDO.getComboProductId() + "_" + esDO.getCustomerName()));

            // 转换为VO并过滤出没有价格的记录
            List<ErpWholesaleMissingPriceVO> allVoList = groupedData.entrySet().stream()
                .map(entry -> {
                    List<ErpWholesaleCombinedESDO> orders = entry.getValue();
                    ErpWholesaleCombinedESDO firstOrder = orders.get(0);

                    ErpWholesaleMissingPriceVO vo = new ErpWholesaleMissingPriceVO();
                    vo.setComboProductId(firstOrder.getComboProductId());
                    vo.setComboProductNo(firstOrder.getComboProductNo());
                    vo.setProductName(firstOrder.getProductName());
                    vo.setCustomerName(firstOrder.getCustomerName());

                    // 统计信息
                    vo.setOrderCount(orders.size());
                    vo.setTotalProductQuantity(orders.stream()
                        .mapToInt(order -> order.getProductQuantity() != null ? order.getProductQuantity() : 0)
                        .sum());
                    vo.setOrderNumbers(orders.stream()
                        .map(ErpWholesaleCombinedESDO::getNo)
                        .collect(Collectors.toList()));
                    vo.setOrderIds(orders.stream()
                        .map(ErpWholesaleCombinedESDO::getId)
                        .collect(Collectors.toList()));

                    // 时间信息
                    List<LocalDateTime> createTimes = orders.stream()
                        .map(ErpWholesaleCombinedESDO::getCreateTime)
                        .filter(Objects::nonNull)
                        .sorted()
                        .collect(Collectors.toList());
                    if (!createTimes.isEmpty()) {
                        vo.setEarliestCreateTime(createTimes.get(0));
                        vo.setLatestCreateTime(createTimes.get(createTimes.size() - 1));
                    }

                    // 查询销售价格表，检查是否有批发单价
                    try {
                        LambdaQueryWrapper<ErpSalePriceDO> priceQuery = new LambdaQueryWrapper<>();
                        priceQuery.eq(ErpSalePriceDO::getGroupProductId, firstOrder.getComboProductId())
                                  .eq(ErpSalePriceDO::getCustomerName, firstOrder.getCustomerName());
                        ErpSalePriceDO salePrice = salePriceMapper.selectOne(priceQuery);
                        if (salePrice != null) {
                            vo.setWholesalePrice(salePrice.getWholesalePrice());
                        }
                    } catch (Exception e) {
                        System.err.println("查询销售价格失败: " + e.getMessage());
                    }

                    return vo;
                })
                .filter(vo -> vo.getWholesalePrice() == null || vo.getWholesalePrice().compareTo(BigDecimal.ZERO) == 0)
                .sorted(Comparator.comparing(ErpWholesaleMissingPriceVO::getLatestCreateTime,
                    Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

            // 手动分页
            int page = pageReqVO.getPageNo() != null ? pageReqVO.getPageNo() - 1 : 0;
            int size = pageReqVO.getPageSize() != null ? pageReqVO.getPageSize() : 10;
            int start = page * size;
            int end = Math.min(start + size, allVoList.size());

            List<ErpWholesaleMissingPriceVO> pagedVoList = start < allVoList.size() ?
                allVoList.subList(start, end) : Collections.emptyList();

            return new PageResult<>(pagedVoList, (long) allVoList.size());

        } catch (Exception e) {
            System.err.println("从ES查询批发缺失价格记录失败: " + e.getMessage());
            // 降级到数据库查询
            return getWholesaleMissingPricesFromDB(pageReqVO);
        }
    }

    /**
     * 从数据库查询批发缺失价格记录（降级方案）
     */
    private PageResult<ErpWholesaleMissingPriceVO> getWholesaleMissingPricesFromDB(ErpSalePricePageReqVO pageReqVO) {
        try {
            // 构建查询条件 - 查询所有批发订单
            LambdaQueryWrapper<ErpWholesaleCombinedDO> queryWrapper = new LambdaQueryWrapper<>();

            // 添加搜索条件
            if (pageReqVO.getGroupProductId() != null) {
                queryWrapper.eq(ErpWholesaleCombinedDO::getComboProductId, pageReqVO.getGroupProductId());
            }
            if (StrUtil.isNotBlank(pageReqVO.getCustomerName())) {
                queryWrapper.like(ErpWholesaleCombinedDO::getCustomerName, pageReqVO.getCustomerName());
            }

            // 排序
            queryWrapper.orderByDesc(ErpWholesaleCombinedDO::getCreateTime);

            // 查询所有数据进行分组
            List<ErpWholesaleCombinedDO> allRecords = wholesaleCombinedMapper.selectList(queryWrapper);

            // 按组品ID和客户名称分组
            Map<String, List<ErpWholesaleCombinedDO>> groupedData = allRecords.stream()
                .filter(combinedDO -> combinedDO.getComboProductId() != null && StrUtil.isNotBlank(combinedDO.getCustomerName()))
                .collect(Collectors.groupingBy(combinedDO ->
                    combinedDO.getComboProductId() + "_" + combinedDO.getCustomerName()));

            // 转换为VO并过滤出没有价格的记录
            List<ErpWholesaleMissingPriceVO> allVoList = groupedData.entrySet().stream()
                .map(entry -> {
                    List<ErpWholesaleCombinedDO> orders = entry.getValue();
                    ErpWholesaleCombinedDO firstOrder = orders.get(0);

                    ErpWholesaleMissingPriceVO vo = new ErpWholesaleMissingPriceVO();
                    vo.setComboProductId(firstOrder.getComboProductId());
                    vo.setCustomerName(firstOrder.getCustomerName());

                    // 统计信息
                    vo.setOrderCount(orders.size());
                    vo.setTotalProductQuantity(orders.stream()
                        .mapToInt(order -> order.getProductQuantity() != null ? order.getProductQuantity() : 0)
                        .sum());
                    vo.setOrderNumbers(orders.stream()
                        .map(ErpWholesaleCombinedDO::getNo)
                        .collect(Collectors.toList()));
                    vo.setOrderIds(orders.stream()
                        .map(ErpWholesaleCombinedDO::getId)
                        .collect(Collectors.toList()));

                    // 时间信息
                    List<LocalDateTime> createTimes = orders.stream()
                        .map(ErpWholesaleCombinedDO::getCreateTime)
                        .filter(Objects::nonNull)
                        .sorted()
                        .collect(Collectors.toList());
                    if (!createTimes.isEmpty()) {
                        vo.setEarliestCreateTime(createTimes.get(0));
                        vo.setLatestCreateTime(createTimes.get(createTimes.size() - 1));
                    }

                    // 从组品表获取组品编号和产品名称
                    if (firstOrder.getComboProductId() != null) {
                        Optional<ErpComboProductES> comboProductOpt = comboProductESRepository.findById(firstOrder.getComboProductId());
                        if (comboProductOpt.isPresent()) {
                            ErpComboProductES comboProduct = comboProductOpt.get();
                            vo.setComboProductNo(comboProduct.getNo());
                            vo.setProductName(comboProduct.getName());
                        }
                    }

                    // 查询销售价格表，检查是否有批发单价
                    try {
                        LambdaQueryWrapper<ErpSalePriceDO> priceQuery = new LambdaQueryWrapper<>();
                        priceQuery.eq(ErpSalePriceDO::getGroupProductId, firstOrder.getComboProductId())
                                  .eq(ErpSalePriceDO::getCustomerName, firstOrder.getCustomerName());
                        ErpSalePriceDO salePrice = salePriceMapper.selectOne(priceQuery);
                        if (salePrice != null) {
                            vo.setWholesalePrice(salePrice.getWholesalePrice());
                        }
                    } catch (Exception e) {
                        System.err.println("查询销售价格失败: " + e.getMessage());
                    }

                    return vo;
                })
                .filter(vo -> vo.getWholesalePrice() == null || vo.getWholesalePrice().compareTo(BigDecimal.ZERO) == 0)
                .sorted(Comparator.comparing(ErpWholesaleMissingPriceVO::getLatestCreateTime,
                    Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

            // 手动分页
            int page = pageReqVO.getPageNo() != null ? pageReqVO.getPageNo() - 1 : 0;
            int size = pageReqVO.getPageSize() != null ? pageReqVO.getPageSize() : 10;
            int start = page * size;
            int end = Math.min(start + size, allVoList.size());

            List<ErpWholesaleMissingPriceVO> pagedVoList = start < allVoList.size() ?
                allVoList.subList(start, end) : Collections.emptyList();

            return new PageResult<>(pagedVoList, (long) allVoList.size());

        } catch (Exception e) {
            System.err.println("从数据库查询批发缺失价格记录失败: " + e.getMessage());
            return new PageResult<>(Collections.emptyList(), 0L);
        }
    }

    /**
     * 🔥 实时计算产品名称 - 参考代发表的实时计算逻辑
     * 根据组品ID查询关联的单品，实时组装产品名称
     */
    private String calculateRealTimeProductName(Long comboProductId) {
        try {
            // 从ES查询组品关联的单品项
            NativeSearchQuery itemQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("combo_product_id", comboProductId))
                    .withSort(Sort.by(Sort.Direction.ASC, "id"))
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                    itemQuery,
                    ErpComboProductItemES.class,
                    IndexCoordinates.of("erp_combo_product_items"));

            if (itemHits.isEmpty()) {
                return null;
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

            // 组装单品名称字符串 (单品A×数量+单品B×数量)
            StringBuilder nameBuilder = new StringBuilder();
            List<ErpComboProductItemES> items = itemHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    nameBuilder.append("+");
                }
                ErpProductESDO product = productMap.get(items.get(i).getItemProductId());
                if (product != null) {
                    nameBuilder.append(product.getName())
                              .append("×")
                              .append(items.get(i).getItemQuantity());
                }
            }

            return nameBuilder.toString();
        } catch (Exception e) {
            System.err.println("实时计算产品名称失败，组品ID: " + comboProductId + ", 错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 🔥 实时计算采购单价 - 参考代发表的实时计算逻辑
     * 根据组品ID查询关联的单品，实时计算采购总价
     */
    private BigDecimal calculateRealTimePurchasePrice(Long comboProductId) {
        try {
            // 从ES查询组品关联的单品项
            NativeSearchQuery itemQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("combo_product_id", comboProductId))
                    .withSort(Sort.by(Sort.Direction.ASC, "id"))
                    .withPageable(PageRequest.of(0, 1000))
                    .build();

            SearchHits<ErpComboProductItemES> itemHits = elasticsearchRestTemplate.search(
                    itemQuery,
                    ErpComboProductItemES.class,
                    IndexCoordinates.of("erp_combo_product_items"));

            if (itemHits.isEmpty()) {
                return BigDecimal.ZERO;
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

            // 计算采购总价
            BigDecimal totalPurchasePrice = BigDecimal.ZERO;
            List<ErpComboProductItemES> items = itemHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            for (ErpComboProductItemES item : items) {
                ErpProductESDO product = productMap.get(item.getItemProductId());
                if (product != null && product.getWholesalePrice() != null) {
                    BigDecimal itemQuantity = new BigDecimal(item.getItemQuantity());
                    totalPurchasePrice = totalPurchasePrice.add(product.getWholesalePrice().multiply(itemQuantity));
                }
            }

            return totalPurchasePrice;
        } catch (Exception e) {
            System.err.println("实时计算采购单价失败，组品ID: " + comboProductId + ", 错误: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
