package cn.iocoder.yudao.module.erp.service.purchase;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaserDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalespersonDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaserMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
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
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaser(@Valid ErpPurchaserSaveReqVO createReqVO) {
        // 校验编号唯一性
        if (createReqVO.getNo() != null && !createReqVO.getNo().trim().isEmpty()) {
            validatePurchaserNoUnique(null, createReqVO.getNo());
        }
        
        // 插入采购人员
        ErpPurchaserDO purchaser = BeanUtils.toBean(createReqVO, ErpPurchaserDO.class);
        // 如果没有提供编号，自动生成
        if (purchaser.getNo() == null || purchaser.getNo().trim().isEmpty()) {
            purchaser.setNo(generatePurchaserNo());
        }
        purchaserMapper.insert(purchaser);
        return purchaser.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaser(@Valid ErpPurchaserSaveReqVO updateReqVO) {
        // 校验存在
        validatePurchaserExists(updateReqVO.getId());
        // 校验编号唯一性
        if (updateReqVO.getNo() != null && !updateReqVO.getNo().trim().isEmpty()) {
            validatePurchaserNoUnique(updateReqVO.getId(), updateReqVO.getNo());
        }
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
        LambdaQueryWrapper<ErpPurchaserDO> queryWrapper = new LambdaQueryWrapper<ErpPurchaserDO>()
                .like(searchReqVO.getNo() != null, ErpPurchaserDO::getNo, searchReqVO.getNo())
                .like(searchReqVO.getPurchaserName() != null, ErpPurchaserDO::getPurchaserName, searchReqVO.getPurchaserName())
                .like(searchReqVO.getContactPhone() != null, ErpPurchaserDO::getContactPhone, searchReqVO.getContactPhone())
                .like(searchReqVO.getAddress() != null, ErpPurchaserDO::getAddress, searchReqVO.getAddress());
    
        // 添加创建时间范围查询条件
        if (searchReqVO.getCreateTime() != null && searchReqVO.getCreateTime().length == 2 
                && searchReqVO.getCreateTime()[0] != null && searchReqVO.getCreateTime()[1] != null) {
            queryWrapper.between(ErpPurchaserDO::getCreateTime,
                    searchReqVO.getCreateTime()[0], searchReqVO.getCreateTime()[1]);
        }
    
        List<ErpPurchaserDO> list = purchaserMapper.selectList(queryWrapper);
    
        // 转换为VO列表
        return BeanUtils.toBean(list, ErpPurchaserRespVO.class);
    }

    /**
     * 生成采购人员编号
     */
    private String generatePurchaserNo() {
        String no;
        do {
            // 使用Redis生成唯一编号
            no = noRedisDAO.generate(ErpNoRedisDAO.PURCHASER_NO_PREFIX);
        } while (purchaserMapper.selectByNo(no) != null); // 确保编号唯一
        return no;
    }
    
    /**
     * 校验采购人员编号的唯一性
     */
    private void validatePurchaserNoUnique(Long id, String no) {
        ErpPurchaserDO purchaser = purchaserMapper.selectByNo(no);
        if (purchaser == null) {
            return;
        }
        // 如果 id 为空，说明不允许存在；否则，如果 id 不相等，说明冲突
        if (id == null || !id.equals(purchaser.getId())) {
            throw exception(PURCHASER_NO_DUPLICATE, no);
        }
    }
}
