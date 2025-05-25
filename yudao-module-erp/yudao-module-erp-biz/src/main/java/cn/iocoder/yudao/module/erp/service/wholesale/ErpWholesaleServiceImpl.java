package cn.iocoder.yudao.module.erp.service.wholesale;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesalePurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleSaleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesalePurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleSaleMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import java.math.BigDecimal;


import javax.annotation.Resource;
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
public class ErpWholesaleServiceImpl implements ErpWholesaleService {

    @Resource
    private ErpWholesaleMapper wholesaleMapper;

    @Resource
    private ErpWholesalePurchaseMapper purchaseMapper;

    @Resource
    private ErpWholesaleSaleMapper saleMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWholesale(ErpWholesaleSaveReqVO createReqVO) {
        // 1. 校验数据
        validateWholesaleForCreateOrUpdate(null, createReqVO);

        // 2. 生成批发单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.WHOLESALE_NO_PREFIX);
        if (wholesaleMapper.selectByNo(no) != null) {
            throw exception(WHOLESALE_NO_EXISTS);
        }

        // 3. 插入批发记录
        ErpWholesaleBaseDO wholesale = BeanUtils.toBean(createReqVO, ErpWholesaleBaseDO.class)
                .setNo(no)
                .setStatus(ErpAuditStatus.PROCESS.getStatus());
        wholesaleMapper.insert(wholesale);

        // 4. 插入采购信息
        ErpWholesalePurchaseDO purchase = BeanUtils.toBean(createReqVO, ErpWholesalePurchaseDO.class)
                .setBaseId(wholesale.getId())
                .setPurchaseAuditStatus(ErpAuditStatus.PROCESS.getStatus())
                .setPurchaseAfterSalesStatus(30)
                .setOtherFees(createReqVO.getOtherFees());
        purchaseMapper.insert(purchase);

        // 5. 插入销售信息
        ErpWholesaleSaleDO sale = BeanUtils.toBean(createReqVO, ErpWholesaleSaleDO.class)
                .setBaseId(wholesale.getId())
                .setSaleAuditStatus(ErpAuditStatus.PROCESS.getStatus())
                .setSaleAfterSalesStatus(30)
                .setTruckFee(createReqVO.getSaleTruckFee())
                .setOtherFees(createReqVO.getSaleOtherFees());
        saleMapper.insert(sale);

        return wholesale.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWholesale(ErpWholesaleSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpWholesaleBaseDO wholesale = validateWholesale(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(wholesale.getStatus())) {
            throw exception(WHOLESALE_UPDATE_FAIL_APPROVE, wholesale.getNo());
        }
        // 1.2 校验数据
        validateWholesaleForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新批发记录
        ErpWholesaleBaseDO updateObj = BeanUtils.toBean(updateReqVO, ErpWholesaleBaseDO.class);
        wholesaleMapper.updateById(updateObj);

        // 3. 更新采购信息（独立检查审核状态）
        ErpWholesalePurchaseDO purchase = purchaseMapper.selectByBaseId(updateReqVO.getId());
        if (purchase != null) {
            if (!ErpAuditStatus.APPROVE.getStatus().equals(purchase.getPurchaseAuditStatus())) {
                purchase = BeanUtils.toBean(updateReqVO, ErpWholesalePurchaseDO.class)
                        .setBaseId(updateReqVO.getId());
                purchaseMapper.update(purchase,
                    new LambdaUpdateWrapper<ErpWholesalePurchaseDO>()
                        .eq(ErpWholesalePurchaseDO::getBaseId, updateReqVO.getId()));
            }
        }

        // 4. 更新销售信息（独立检查审核状态）
        ErpWholesaleSaleDO sale = saleMapper.selectByBaseId(updateReqVO.getId());
        if (sale != null) {
            if (!ErpAuditStatus.APPROVE.getStatus().equals(sale.getSaleAuditStatus())) {
                sale = BeanUtils.toBean(updateReqVO, ErpWholesaleSaleDO.class)
                        .setBaseId(updateReqVO.getId())
                        .setOtherFees(updateReqVO.getSaleOtherFees())
                        .setTruckFee(updateReqVO.getSaleTruckFee());
                saleMapper.update(sale,
                    new LambdaUpdateWrapper<ErpWholesaleSaleDO>()
                        .eq(ErpWholesaleSaleDO::getBaseId, updateReqVO.getId()));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWholesale(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpWholesaleBaseDO> wholesales = wholesaleMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(wholesales)) {
            throw exception(WHOLESALE_NOT_EXISTS);
        }
        // 2. 删除批发记录
        wholesaleMapper.deleteBatchIds(ids);

        // 3. 删除采购信息
        purchaseMapper.deleteByBaseIds(ids);

        // 4. 删除销售信息
        saleMapper.deleteByBaseIds(ids);
    }

    @Override
    public ErpWholesaleBaseDO getWholesale(Long id) {
        return wholesaleMapper.selectById(id);
    }

    @Override
    public ErpWholesaleBaseDO validateWholesale(Long id) {
        ErpWholesaleBaseDO wholesale = wholesaleMapper.selectById(id);
        if (wholesale == null) {
            throw exception(WHOLESALE_NOT_EXISTS);
        }
        return wholesale;
    }

    @Override
    public PageResult<ErpWholesaleRespVO> getWholesaleVOPage(ErpWholesalePageReqVO pageReqVO) {
        return wholesaleMapper.selectPage(pageReqVO);
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
        ErpWholesaleBaseDO wholesale = wholesaleMapper.selectByNo(reqVO.getNo());
        if (wholesale != null && !wholesale.getId().equals(id)) {
            throw exception(WHOLESALE_NO_EXISTS);
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseAuditStatus(Long id, Integer purchaseAuditStatus, BigDecimal otherFees) {
        // 1. 校验存在
        ErpWholesaleBaseDO wholesale = validateWholesale(id);

        // 2. 获取当前采购审核状态
        ErpWholesalePurchaseDO purchase = purchaseMapper.selectByBaseId(id);
        if (purchase == null) {
            throw exception(WHOLESALE_NOT_EXISTS);
        }

        // 3. 校验状态是否重复
        if (purchase.getPurchaseAuditStatus() != null && purchase.getPurchaseAuditStatus().equals(purchaseAuditStatus)) {
            throw exception(WHOLESALE_PROCESS_FAIL);
        }

        // 4. 更新采购审核状态
        ErpWholesalePurchaseDO updateObj = new ErpWholesalePurchaseDO()
                .setPurchaseAuditStatus(purchaseAuditStatus)
                .setOtherFees(otherFees);
        
        // 根据审核状态设置相应时间
        if (purchaseAuditStatus == 20) { // 审核通过
            updateObj.setPurchaseApprovalTime(LocalDateTime.now());
        } else if (purchaseAuditStatus == 10) { // 反审核
            updateObj.setPurchaseUnapproveTime(LocalDateTime.now());
        }

        purchaseMapper.update(updateObj, new LambdaUpdateWrapper<ErpWholesalePurchaseDO>()
                .eq(ErpWholesalePurchaseDO::getBaseId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleAuditStatus(Long id, Integer saleAuditStatus, BigDecimal otherFees) {
        // 1. 校验存在
        ErpWholesaleBaseDO wholesale = validateWholesale(id);

        // 2. 获取当前销售审核状态
        ErpWholesaleSaleDO sale = saleMapper.selectByBaseId(id);
        if (sale == null) {
            throw exception(WHOLESALE_NOT_EXISTS);
        }

        // 3. 校验状态是否重复
        if (sale.getSaleAuditStatus() != null && sale.getSaleAuditStatus().equals(saleAuditStatus)) {
            throw exception(WHOLESALE_PROCESS_FAIL);
        }

        // 4. 更新销售审核状态
        ErpWholesaleSaleDO updateObj = new ErpWholesaleSaleDO()
                .setSaleAuditStatus(saleAuditStatus)
                .setOtherFees(otherFees);
        
        // 根据审核状态设置相应时间
        if (saleAuditStatus == 20) { // 审核通过
            updateObj.setSaleApprovalTime(LocalDateTime.now());
        } else if (saleAuditStatus == 10) { // 反审核
            updateObj.setSaleUnapproveTime(LocalDateTime.now());
        }

        saleMapper.update(updateObj, new LambdaUpdateWrapper<ErpWholesaleSaleDO>()
                .eq(ErpWholesaleSaleDO::getBaseId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseAfterSales(ErpWholesalePurchaseAfterSalesUpdateReqVO reqVO) {
        // 1. 校验存在
        ErpWholesaleBaseDO wholesale = validateWholesale(reqVO.getId());
        LocalDateTime purchaseAfterSalesTime = parseDateTime(reqVO.getPurchaseAfterSalesTime());
        // 2. 更新采购售后信息
        ErpWholesalePurchaseDO updateObj = new ErpWholesalePurchaseDO()
                .setPurchaseAfterSalesStatus(reqVO.getPurchaseAfterSalesStatus())
                .setPurchaseAfterSalesSituation(reqVO.getPurchaseAfterSalesSituation())
                .setPurchaseAfterSalesAmount(reqVO.getPurchaseAfterSalesAmount())
                .setPurchaseAfterSalesTime(purchaseAfterSalesTime);
        purchaseMapper.update(updateObj, new LambdaUpdateWrapper<ErpWholesalePurchaseDO>()
                .eq(ErpWholesalePurchaseDO::getBaseId, reqVO.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleAfterSales(ErpWholesaleSaleAfterSalesUpdateReqVO reqVO) {
        // 1. 校验存在
        ErpWholesaleBaseDO wholesale = validateWholesale(reqVO.getId());
        LocalDateTime purchaseAfterSalesTime = parseDateTime(reqVO.getSaleAfterSalesTime());
        // 2. 更新销售售后信息
        ErpWholesaleSaleDO updateObj = new ErpWholesaleSaleDO()
                .setSaleAfterSalesStatus(reqVO.getSaleAfterSalesStatus())
                .setSaleAfterSalesSituation(reqVO.getSaleAfterSalesSituation())
                .setSaleAfterSalesAmount(reqVO.getSaleAfterSalesAmount())
                .setSaleAfterSalesTime(purchaseAfterSalesTime);
        saleMapper.update(updateObj, new LambdaUpdateWrapper<ErpWholesaleSaleDO>()
                .eq(ErpWholesaleSaleDO::getBaseId, reqVO.getId()));
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
