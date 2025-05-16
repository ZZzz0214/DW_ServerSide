package cn.iocoder.yudao.module.erp.service.purchase;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaserDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpPurchaserServiceImpl implements ErpPurchaserService {

    @Resource
    private ErpPurchaserMapper purchaserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaser(@Valid ErpPurchaserSaveReqVO createReqVO) {
        // 插入采购人员
        ErpPurchaserDO purchaser = BeanUtils.toBean(createReqVO, ErpPurchaserDO.class);
        purchaserMapper.insert(purchaser);
        return purchaser.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaser(@Valid ErpPurchaserSaveReqVO updateReqVO) {
        // 校验存在
        validatePurchaserExists(updateReqVO.getId());
        // 更新采购人员
        ErpPurchaserDO updateObj = BeanUtils.toBean(updateReqVO, ErpPurchaserDO.class);
        purchaserMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaser(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 校验存在
        List<ErpPurchaserDO> purchasers = purchaserMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(purchasers)) {
            throw exception(PURCHASER_NOT_EXISTS);
        }
        // 删除采购人员
        purchaserMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpPurchaserDO getPurchaser(Long id) {
        return purchaserMapper.selectById(id);
    }

    @Override
    public List<ErpPurchaserDO> getPurchaserList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return purchaserMapper.selectBatchIds(ids);
    }

    @Override
    public ErpPurchaserDO validatePurchaser(Long id) {
        ErpPurchaserDO purchaser = purchaserMapper.selectById(id);
        if (purchaser == null) {
            throw exception(PURCHASER_NOT_EXISTS);
        }
        return purchaser;
    }

    @Override
    public PageResult<ErpPurchaserRespVO> getPurchaserVOPage(ErpPurchaserPageReqVO pageReqVO) {
        return purchaserMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpPurchaserRespVO> getPurchaserVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpPurchaserDO> list = purchaserMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpPurchaserRespVO.class);
    }

    @Override
    public Map<Long, ErpPurchaserRespVO> getPurchaserVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPurchaserVOList(ids), ErpPurchaserRespVO::getId);
    }

    private void validatePurchaserExists(Long id) {
        if (id == null) {
            return;
        }
        ErpPurchaserDO purchaser = purchaserMapper.selectById(id);
        if (purchaser == null) {
            throw exception(PURCHASER_NOT_EXISTS);
        }
    }

    @Override
    public List<ErpPurchaserRespVO> searchPurchasers(ErpPurchaserPageReqVO searchReqVO) {
        // 执行查询
        List<ErpPurchaserDO> list = purchaserMapper.selectList(new LambdaQueryWrapper<ErpPurchaserDO>()
                .like(searchReqVO.getPurchaserName() != null, ErpPurchaserDO::getPurchaserName, searchReqVO.getPurchaserName())
                .like(searchReqVO.getContactPhone() != null, ErpPurchaserDO::getContactPhone, searchReqVO.getContactPhone())
                .between(searchReqVO.getCreateTime() != null, ErpPurchaserDO::getCreateTime,
                        searchReqVO.getCreateTime()[0], searchReqVO.getCreateTime()[1]));

        // 转换为VO列表
        return BeanUtils.toBean(list, ErpPurchaserRespVO.class);
    }
}
