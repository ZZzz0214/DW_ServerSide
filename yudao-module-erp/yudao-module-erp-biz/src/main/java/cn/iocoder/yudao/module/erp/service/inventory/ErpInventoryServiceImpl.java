package cn.iocoder.yudao.module.erp.service.inventory;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventoryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventoryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventorySaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.inventory.ErpInventoryDO;
import cn.iocoder.yudao.module.erp.dal.mysql.inventory.ErpInventoryMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
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
public class ErpInventoryServiceImpl implements ErpInventoryService {

    @Resource
    private ErpInventoryMapper inventoryMapper;
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInventory(ErpInventorySaveReqVO createReqVO) {
        // 1. 校验数据
        validateInventoryForCreateOrUpdate(null, createReqVO);

        // 2. 生成库存编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.INVENTORY_NO_PREFIX);
        if (inventoryMapper.selectByNo(no) != null) {
            throw exception(INVENTORY_NO_EXISTS);
        }

        // 3. 插入库存记录
        ErpInventoryDO inventory = BeanUtils.toBean(createReqVO, ErpInventoryDO.class)
                .setNo(no);
        inventoryMapper.insert(inventory);

        return inventory.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInventory(ErpInventorySaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateInventory(updateReqVO.getId());
        // 1.2 校验数据
        validateInventoryForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新库存记录
        ErpInventoryDO updateObj = BeanUtils.toBean(updateReqVO, ErpInventoryDO.class);
        inventoryMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInventory(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpInventoryDO> inventories = inventoryMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(inventories)) {
            throw exception(INVENTORY_NOT_EXISTS);
        }
        // 2. 删除库存记录
        inventoryMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpInventoryDO getInventory(Long id) {
        return inventoryMapper.selectById(id);
    }

    @Override
    public ErpInventoryDO validateInventory(Long id) {
        ErpInventoryDO inventory = inventoryMapper.selectById(id);
        if (inventory == null) {
            throw exception(INVENTORY_NOT_EXISTS);
        }
        return inventory;
    }

    @Override
    public PageResult<ErpInventoryRespVO> getInventoryVOPage(ErpInventoryPageReqVO pageReqVO) {
        return inventoryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpInventoryRespVO> getInventoryVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpInventoryDO> list = inventoryMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpInventoryRespVO.class);
    }

    @Override
    public Map<Long, ErpInventoryRespVO> getInventoryVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getInventoryVOList(ids), ErpInventoryRespVO::getId);
    }

    @Override
    public List<ErpInventoryDO> getInventoryList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return inventoryMapper.selectBatchIds(ids);
    }

    private void validateInventoryForCreateOrUpdate(Long id, ErpInventorySaveReqVO reqVO) {
        // 1. 校验库存编号唯一
        ErpInventoryDO inventory = inventoryMapper.selectByNo(reqVO.getNo());
        if (inventory != null && !inventory.getId().equals(id)) {
            throw exception(INVENTORY_NO_EXISTS);
        }
    }
}
