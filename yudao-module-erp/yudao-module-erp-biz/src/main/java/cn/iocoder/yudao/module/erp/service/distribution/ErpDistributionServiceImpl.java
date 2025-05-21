package cn.iocoder.yudao.module.erp.service.distribution;



import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionPurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionSaleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionPurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.distribution.ErpDistributionSaleMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDistribution(ErpDistributionSaveReqVO createReqVO) {
        // 1. 校验数据
        validateDistributionForCreateOrUpdate(null, createReqVO);
        System.out.println(createReqVO);

        // 2. 生成代发单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.DISTRIBUTION_NO_PREFIX);
        if (distributionMapper.selectByNo(no) != null) {
            throw exception(DISTRIBUTION_NO_EXISTS);
        }

        // 3. 插入代发记录
        ErpDistributionBaseDO distribution = BeanUtils.toBean(createReqVO, ErpDistributionBaseDO.class)
                .setNo(no)
                .setStatus(ErpAuditStatus.PROCESS.getStatus());
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
                .setOtherFees(createReqVO.getSaleOtherFees());
        saleMapper.insert(sale);

        return distribution.getId();
    }

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

        // 3. 更新采购信息
//        if (updateReqVO.getComboProductId() != null) {
            ErpDistributionPurchaseDO purchase = BeanUtils.toBean(updateReqVO, ErpDistributionPurchaseDO.class)
                    .setBaseId(updateReqVO.getId());
                    purchaseMapper.update(purchase, 
                    new LambdaUpdateWrapper<ErpDistributionPurchaseDO>()
                        .eq(ErpDistributionPurchaseDO::getBaseId, updateReqVO.getId()));
//        }

        // 4. 更新销售信息
//        if (updateReqVO.getSalePriceId() !=null) {
            ErpDistributionSaleDO sale = BeanUtils.toBean(updateReqVO, ErpDistributionSaleDO.class)
                    .setBaseId(updateReqVO.getId())
                    .setOtherFees(updateReqVO.getSaleOtherFees());
                    saleMapper.update(sale,
                    new LambdaUpdateWrapper<ErpDistributionSaleDO>()
                        .eq(ErpDistributionSaleDO::getBaseId, updateReqVO.getId()));
//        }
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

    @Override
    public PageResult<ErpDistributionRespVO> getDistributionVOPage(ErpDistributionPageReqVO pageReqVO) {
        return distributionMapper.selectPage(pageReqVO);
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

        // 3. 更新采购售后信息
        ErpDistributionPurchaseDO updateObj = new ErpDistributionPurchaseDO()
                .setPurchaseAfterSalesStatus(reqVO.getPurchaseAfterSalesStatus())
                .setPurchaseAfterSalesSituation(reqVO.getPurchaseAfterSalesSituation())
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
        // 2. 更新销售售后信息
        ErpDistributionSaleDO updateObj = new ErpDistributionSaleDO()
                .setSaleAfterSalesStatus(reqVO.getSaleAfterSalesStatus())
                .setSaleAfterSalesSituation(reqVO.getSaleAfterSalesSituation())
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
        saleMapper.update(updateObj, new LambdaUpdateWrapper<ErpDistributionSaleDO>()
                .eq(ErpDistributionSaleDO::getBaseId, id));
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
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
                        return offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(); // 转换为本地时间
                    } catch (DateTimeParseException e4) {
                        throw new IllegalArgumentException("无法解析时间格式: " + dateTimeStr);
                    }
                }
            }
        }
    }
}
