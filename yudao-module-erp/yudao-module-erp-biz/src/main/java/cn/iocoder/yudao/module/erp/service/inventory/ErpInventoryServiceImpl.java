package cn.iocoder.yudao.module.erp.service.inventory;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.inventory.ErpInventoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.inventory.ErpInventoryMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    
    @Resource
    private ErpProductMapper productMapper;

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
    public ErpInventoryRespVO getInventoryVO(Long id) {
        return inventoryMapper.selectVOById(id);
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
        return inventoryMapper.selectVOListByIds(ids);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpInventoryImportRespVO importInventoryList(List<ErpInventoryImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(INVENTORY_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpInventoryImportRespVO respVO = ErpInventoryImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理
        List<ErpInventoryDO> createList = new ArrayList<>();
        List<ErpInventoryDO> updateList = new ArrayList<>();

        try {
            // 批量查询产品信息
            Set<String> productNos = importList.stream()
                    .map(ErpInventoryImportExcelVO::getProductNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpProductDO> productMap = productNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(productMapper.selectListByNoIn(productNos), ErpProductDO::getNo);

            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpInventoryImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpInventoryDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(inventoryMapper.selectListByNoIn(noSet), ErpInventoryDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();

            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpInventoryImportExcelVO importVO = importList.get(i);
                try {
                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(INVENTORY_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 校验产品编号存在并转换为产品ID
                    Long productId = null;
                    if (StrUtil.isNotBlank(importVO.getProductNo())) {
                        ErpProductDO product = productMap.get(importVO.getProductNo());
                        if (product == null) {
                            throw exception(INVENTORY_IMPORT_PRODUCT_NOT_EXISTS, i + 1, importVO.getProductNo());
                        }
                        productId = product.getId();
                    }

                    // 校验库存数量
                    if (importVO.getSpotInventory() != null && importVO.getSpotInventory() < 0) {
                        throw exception(INVENTORY_IMPORT_SPOT_INVENTORY_INVALID, i + 1);
                    }
                    if (importVO.getRemainingInventory() != null && importVO.getRemainingInventory() < 0) {
                        throw exception(INVENTORY_IMPORT_REMAINING_INVENTORY_INVALID, i + 1);
                    }

                    // 判断是否支持更新
                    ErpInventoryDO existInventory = existMap.get(importVO.getNo());
                    if (existInventory == null) {
                        // 创建 - 自动生成新的no编号
                        ErpInventoryDO inventory = BeanUtils.toBean(importVO, ErpInventoryDO.class);
                        inventory.setNo(noRedisDAO.generate(ErpNoRedisDAO.INVENTORY_NO_PREFIX));
                        inventory.setProductId(productId); // 设置转换后的产品ID
                        createList.add(inventory);
                        respVO.getCreateNames().add(inventory.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpInventoryDO updateInventory = BeanUtils.toBean(importVO, ErpInventoryDO.class);
                        updateInventory.setId(existInventory.getId());
                        updateInventory.setProductId(productId); // 设置转换后的产品ID
                        updateList.add(updateInventory);
                        respVO.getUpdateNames().add(updateInventory.getNo());
                    } else {
                        throw exception(INVENTORY_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
                    }
                } catch (ServiceException ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");
                    respVO.getFailureNames().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");
                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                inventoryMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(inventoryMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }
}
