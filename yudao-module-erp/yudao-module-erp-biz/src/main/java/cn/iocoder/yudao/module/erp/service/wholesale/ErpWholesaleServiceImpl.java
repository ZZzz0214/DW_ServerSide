package cn.iocoder.yudao.module.erp.service.wholesale;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesalePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesaleRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesaleSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesalePurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleSaleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesalePurchaseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.wholesale.ErpWholesaleSaleMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWholesale(ErpWholesaleSaveReqVO createReqVO) {
        // 1. 校验数据
        validateWholesaleForCreateOrUpdate(null, createReqVO);

        // 2. 插入批发记录
        ErpWholesaleBaseDO wholesale = BeanUtils.toBean(createReqVO, ErpWholesaleBaseDO.class)
                .setStatus(ErpAuditStatus.PROCESS.getStatus());
        wholesaleMapper.insert(wholesale);

        // 3. 插入采购信息
        if (createReqVO.getComboProductId() != null) {
            ErpWholesalePurchaseDO purchase = BeanUtils.toBean(createReqVO, ErpWholesalePurchaseDO.class)
                    .setBaseId(wholesale.getId());
            purchaseMapper.insert(purchase);
        }

        // 4. 插入销售信息
        if (createReqVO.getSalePriceId() != null) {
            ErpWholesaleSaleDO sale = BeanUtils.toBean(createReqVO, ErpWholesaleSaleDO.class)
                    .setBaseId(wholesale.getId());
            saleMapper.insert(sale);
        }

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

        // 3. 更新采购信息
        if (updateReqVO.getComboProductId() != null) {
            ErpWholesalePurchaseDO purchase = BeanUtils.toBean(updateReqVO, ErpWholesalePurchaseDO.class)
                    .setBaseId(updateReqVO.getId());
            purchaseMapper.updateById(purchase);
        }

        // 4. 更新销售信息
        if (updateReqVO.getSalePriceId() != null) {
            ErpWholesaleSaleDO sale = BeanUtils.toBean(updateReqVO, ErpWholesaleSaleDO.class)
                    .setBaseId(updateReqVO.getId());
            saleMapper.updateById(sale);
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
        if (ObjectUtil.notEqual(wholesale.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(WHOLESALE_NOT_APPROVE);
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
}