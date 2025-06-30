package cn.iocoder.yudao.module.erp.service.inventory;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.inventory.ErpInventoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductItemES;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionCombinedESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleCombinedESDO;
import cn.iocoder.yudao.module.erp.dal.mysql.inventory.ErpInventoryMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductItemESRepository;
import cn.iocoder.yudao.module.erp.service.distribution.ErpDistributionCombinedESRepository;
import cn.iocoder.yudao.module.erp.service.wholesale.ErpWholesaleCombinedESRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

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

    @Resource
    private ErpComboProductItemESRepository comboProductItemESRepository;

    @Resource
    private ErpDistributionCombinedESRepository distributionCombinedESRepository;

    @Resource
    private ErpWholesaleCombinedESRepository wholesaleCombinedESRepository;

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

        // 3. 插入库存记录（剩余库存不存储，仅在查询时动态计算）
        ErpInventoryDO inventory = BeanUtils.toBean(createReqVO, ErpInventoryDO.class)
                .setNo(no)
                .setRemainingInventory(null); // 不存储剩余库存，仅在查询时计算

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

        // 2. 更新库存记录（剩余库存不存储，仅在查询时动态计算）
        ErpInventoryDO updateObj = BeanUtils.toBean(updateReqVO, ErpInventoryDO.class);
        updateObj.setRemainingInventory(null); // 不存储剩余库存，仅在查询时计算

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
        ErpInventoryRespVO respVO = inventoryMapper.selectVOById(id);
        if (respVO != null) {
            // 动态计算剩余库存，传入库存创建时间
            Integer remainingInventory = calculateRemainingInventory(respVO.getProductId(), respVO.getSpotInventory(), respVO.getCreateTime());
            respVO.setRemainingInventory(remainingInventory);
        }
        return respVO;
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
        PageResult<ErpInventoryRespVO> pageResult = inventoryMapper.selectPage(pageReqVO);

        // 为每个库存记录动态计算剩余库存，传入库存创建时间
        for (ErpInventoryRespVO respVO : pageResult.getList()) {
            Integer remainingInventory = calculateRemainingInventory(respVO.getProductId(), respVO.getSpotInventory(), respVO.getCreateTime());
            respVO.setRemainingInventory(remainingInventory);
        }

        return pageResult;
    }

    @Override
    public List<ErpInventoryRespVO> getInventoryVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpInventoryRespVO> list = inventoryMapper.selectVOListByIds(ids);

        // 为每个库存记录动态计算剩余库存，传入库存创建时间
        for (ErpInventoryRespVO respVO : list) {
            Integer remainingInventory = calculateRemainingInventory(respVO.getProductId(), respVO.getSpotInventory(), respVO.getCreateTime());
            respVO.setRemainingInventory(remainingInventory);
        }

        return list;
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

    /**
     * 动态计算剩余库存
     * 剩余库存 = 现货库存 - (代发订单中该产品的总数量 + 批发订单中该产品的总数量)
     * 注意：只计算在库存创建时间之后的代发订单和批发订单
     *
     * @param productId 产品ID
     * @param spotInventory 现货库存
     * @param inventoryCreateTime 库存创建时间
     * @return 剩余库存
     */
    private Integer calculateRemainingInventory(Long productId, Integer spotInventory, LocalDateTime inventoryCreateTime) {
        if (productId == null || spotInventory == null) {
            return 0;
        }

        try {
            // 1. 获取该产品在所有组品中的数量信息
            List<ErpComboProductItemES> comboItems = (List<ErpComboProductItemES>)
                comboProductItemESRepository.findAllByItemProductId(productId);

            if (CollUtil.isEmpty(comboItems)) {
                // 如果该产品不在任何组品中，剩余库存等于现货库存
                return spotInventory;
            }

            // 2. 计算代发订单中该产品的总数量（只计算库存创建时间之后的订单）
            int distributionTotalQuantity = calculateDistributionProductQuantity(productId, comboItems, inventoryCreateTime);

            // 3. 计算批发订单中该产品的总数量（只计算库存创建时间之后的订单）
            int wholesaleTotalQuantity = calculateWholesaleProductQuantity(productId, comboItems, inventoryCreateTime);

            // 4. 计算剩余库存（允许为负值）
            int remainingInventory = spotInventory - distributionTotalQuantity - wholesaleTotalQuantity;

            return remainingInventory;

        } catch (Exception e) {
            // 如果计算出错，返回现货库存作为默认值
            System.err.println("计算剩余库存时出错: " + e.getMessage());
            return spotInventory;
        }
    }

    /**
     * 计算代发订单中该产品的总数量
     * 代发订单中该产品的总数量 = SUM(代发订单的订单数量 * 组品中该产品的数量)
     * 注意：只计算在库存创建时间之后的代发订单
     *
     * @param productId 产品ID
     * @param comboItems 组品项目列表
     * @param inventoryCreateTime 库存创建时间
     * @return 代发订单中该产品的总数量
     */
    private int calculateDistributionProductQuantity(Long productId, List<ErpComboProductItemES> comboItems, LocalDateTime inventoryCreateTime) {
        try {
            // 获取包含该产品的组品ID列表
            List<Long> comboProductIds = comboItems.stream()
                .map(ErpComboProductItemES::getComboProductId)
                .distinct()
                .collect(Collectors.toList());

            if (CollUtil.isEmpty(comboProductIds)) {
                return 0;
            }

            // 查询这些组品的所有代发订单
            List<ErpDistributionCombinedESDO> distributionOrders =
                distributionCombinedESRepository.findAllByComboProductIdIn(comboProductIds);

            if (CollUtil.isEmpty(distributionOrders)) {
                return 0;
            }

            // 创建组品ID到该产品数量的映射
            Map<Long, Integer> comboProductQuantityMap = comboItems.stream()
                .collect(Collectors.toMap(
                    ErpComboProductItemES::getComboProductId,
                    ErpComboProductItemES::getItemQuantity,
                    (existing, replacement) -> existing // 如果有重复的组品ID，保留第一个
                ));

            // 计算总数量，只计算库存创建时间之后的订单
            int totalQuantity = 0;
            for (ErpDistributionCombinedESDO order : distributionOrders) {
                // 时间过滤：只计算在库存创建时间之后的订单
                if (inventoryCreateTime != null && order.getCreateTime() != null &&
                    order.getCreateTime().isBefore(inventoryCreateTime)) {
                    continue; // 跳过库存创建时间之前的订单
                }

                Integer productQuantityInCombo = comboProductQuantityMap.get(order.getComboProductId());
                if (productQuantityInCombo != null && order.getProductQuantity() != null) {
                    totalQuantity += order.getProductQuantity() * productQuantityInCombo;
                }
            }

            return totalQuantity;
        } catch (Exception e) {
            System.err.println("计算代发订单中产品数量时出错: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 计算批发订单中该产品的总数量
     * 批发订单中该产品的总数量 = SUM(批发订单的订单数量 * 组品中该产品的数量)
     * 注意：只计算在库存创建时间之后的批发订单
     *
     * @param productId 产品ID
     * @param comboItems 组品项目列表
     * @param inventoryCreateTime 库存创建时间
     * @return 批发订单中该产品的总数量
     */
    private int calculateWholesaleProductQuantity(Long productId, List<ErpComboProductItemES> comboItems, LocalDateTime inventoryCreateTime) {
        try {
            // 获取包含该产品的组品ID列表
            List<Long> comboProductIds = comboItems.stream()
                .map(ErpComboProductItemES::getComboProductId)
                .distinct()
                .collect(Collectors.toList());

            if (CollUtil.isEmpty(comboProductIds)) {
                return 0;
            }

            // 查询这些组品的所有批发订单
            List<ErpWholesaleCombinedESDO> wholesaleOrders =
                wholesaleCombinedESRepository.findAllByComboProductIdIn(comboProductIds);

            if (CollUtil.isEmpty(wholesaleOrders)) {
                return 0;
            }

            // 创建组品ID到该产品数量的映射
            Map<Long, Integer> comboProductQuantityMap = comboItems.stream()
                .collect(Collectors.toMap(
                    ErpComboProductItemES::getComboProductId,
                    ErpComboProductItemES::getItemQuantity,
                    (existing, replacement) -> existing // 如果有重复的组品ID，保留第一个
                ));

            // 计算总数量，只计算库存创建时间之后的订单
            int totalQuantity = 0;
            for (ErpWholesaleCombinedESDO order : wholesaleOrders) {
                // 时间过滤：只计算在库存创建时间之后的订单
                if (inventoryCreateTime != null && order.getCreateTime() != null &&
                    order.getCreateTime().isBefore(inventoryCreateTime)) {
                    continue; // 跳过库存创建时间之前的订单
                }

                Integer productQuantityInCombo = comboProductQuantityMap.get(order.getComboProductId());
                if (productQuantityInCombo != null && order.getProductQuantity() != null) {
                    totalQuantity += order.getProductQuantity() * productQuantityInCombo;
                }
            }

            return totalQuantity;
        } catch (Exception e) {
            System.err.println("计算批发订单中产品数量时出错: " + e.getMessage());
            return 0;
        }
    }

    private void validateInventoryForCreateOrUpdate(Long id, ErpInventorySaveReqVO reqVO) {
        // 1. 校验库存编号唯一
        ErpInventoryDO inventory = inventoryMapper.selectByNo(reqVO.getNo());
        if (inventory != null && !inventory.getId().equals(id)) {
            throw exception(INVENTORY_NO_EXISTS);
        }

        // 2. 校验产品唯一性
        ErpInventoryDO existingInventory = inventoryMapper.selectByProductId(reqVO.getProductId());
        if (existingInventory != null && !existingInventory.getId().equals(id)) {
            throw exception(INVENTORY_PRODUCT_EXISTS);
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

        try {
            // 1. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateAllImportData(importList, isUpdateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 2. 批量处理列表
            List<ErpInventoryDO> createList = new ArrayList<>();
            List<ErpInventoryDO> updateList = new ArrayList<>();

            // 3. 批量查询产品信息
            Set<String> productNos = importList.stream()
                    .map(ErpInventoryImportExcelVO::getProductNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpProductDO> productMap = productNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(productMapper.selectListByNoIn(productNos), ErpProductDO::getNo);

            // 4. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpInventoryImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpInventoryDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(inventoryMapper.selectListByNoIn(noSet), ErpInventoryDO::getNo);

            // 5. 批量转换和保存数据
            for (int i = 0; i < importList.size(); i++) {
                ErpInventoryImportExcelVO importVO = importList.get(i);

                // 数据转换
                ErpInventoryDO inventory = convertImportVOToDO(importVO, productMap);

                // 判断是新增还是更新
                ErpInventoryDO existInventory = existMap.get(importVO.getNo());
                if (existInventory == null) {
                    // 创建库存
                    inventory.setNo(noRedisDAO.generate(ErpNoRedisDAO.INVENTORY_NO_PREFIX));
                    inventory.setRemainingInventory(null); // 不存储剩余库存，仅在查询时计算
                    createList.add(inventory);
                    respVO.getCreateNames().add(inventory.getNo());
                } else if (isUpdateSupport) {
                    // 更新库存
                    inventory.setId(existInventory.getId());
                    inventory.setRemainingInventory(null); // 不存储剩余库存，仅在查询时计算
                    updateList.add(inventory);
                    respVO.getUpdateNames().add(inventory.getNo());
                }
            }

            // 6. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                inventoryMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(inventoryMapper::updateById);
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
    private Map<String, String> validateAllImportData(List<ErpInventoryImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询产品信息
        Set<String> productNos = importList.stream()
                .map(ErpInventoryImportExcelVO::getProductNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpProductDO> productMap = productNos.isEmpty() ? Collections.emptyMap() :
                convertMap(productMapper.selectListByNoIn(productNos), ErpProductDO::getNo);

        // 3. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpInventoryImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpInventoryDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(inventoryMapper.selectListByNoIn(noSet), ErpInventoryDO::getNo);

        // 4. 批量查询产品是否已有库存记录
        Set<Long> productIds = importList.stream()
                .map(ErpInventoryImportExcelVO::getProductNo)
                .filter(StrUtil::isNotBlank)
                .map(productMap::get)
                .filter(Objects::nonNull)
                .map(ErpProductDO::getId)
                .collect(Collectors.toSet());
        Map<Long, ErpInventoryDO> productInventoryMap = new HashMap<>();
        for (Long productId : productIds) {
            ErpInventoryDO existingInventory = inventoryMapper.selectByProductId(productId);
            if (existingInventory != null) {
                productInventoryMap.put(productId, existingInventory);
            }
        }

        // 用于跟踪Excel内部重复的编号和产品
        Set<String> processedNos = new HashSet<>();
        Set<String> processedProductNos = new HashSet<>();

        // 5. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpInventoryImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");

            try {
                // 5.1 基础数据校验
                // 5.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "库存编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 5.3 检查Excel内部产品重复
                if (StrUtil.isNotBlank(importVO.getProductNo())) {
                    if (processedProductNos.contains(importVO.getProductNo())) {
                        allErrors.put(errorKey, "产品编号重复: " + importVO.getProductNo());
                        continue;
                    }
                    processedProductNos.add(importVO.getProductNo());
                }

                // 5.4 校验产品编号存在
                ErpProductDO product = productMap.get(importVO.getProductNo());
                if (product == null) {
                    allErrors.put(errorKey, "产品编号不存在: " + importVO.getProductNo());
                    continue;
                }

                // 5.5 判断是新增还是更新，并进行相应校验
                ErpInventoryDO existInventory = existMap.get(importVO.getNo());
                if (existInventory == null) {
                    // 新增校验：校验产品是否已有库存记录
                    ErpInventoryDO existingProductInventory = productInventoryMap.get(product.getId());
                    if (existingProductInventory != null) {
                        allErrors.put(errorKey, "产品已存在库存记录: " + importVO.getProductNo());
                        continue;
                    }
                } else if (isUpdateSupport) {
                    // 更新校验：检查是否与现有记录冲突
                    if (!existInventory.getProductId().equals(product.getId())) {
                        allErrors.put(errorKey, "库存编号已存在但产品不匹配: " + importVO.getNo());
                        continue;
                    }
                } else {
                    allErrors.put(errorKey, "库存编号不存在且不支持更新: " + importVO.getNo());
                    continue;
                }

                // 5.6 校验库存数量
                if (importVO.getSpotInventory() != null && importVO.getSpotInventory() < 0) {
                    allErrors.put(errorKey, "现货库存必须大于等于0");
                    continue;
                }

                // 5.7 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpInventoryDO inventory = convertImportVOToDO(importVO, productMap);
                    if (inventory == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
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
    private Map<String, String> validateDataTypeErrors(List<ErpInventoryImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取库存编号 - 修复行号索引问题
                String inventoryNo = "未知库存";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpInventoryImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        inventoryNo = importVO.getNo();
                    } else if (StrUtil.isNotBlank(importVO.getProductNo())) {
                        inventoryNo = importVO.getProductNo();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + inventoryNo + ")";
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
     * 将导入VO转换为DO
     */
    private ErpInventoryDO convertImportVOToDO(ErpInventoryImportExcelVO importVO, Map<String, ErpProductDO> productMap) {
        if (importVO == null) {
            return null;
        }

        try {
            // 使用BeanUtils进行基础转换
            ErpInventoryDO inventory = BeanUtils.toBean(importVO, ErpInventoryDO.class);

            // 设置产品ID
            if (StrUtil.isNotBlank(importVO.getProductNo())) {
                ErpProductDO product = productMap.get(importVO.getProductNo());
                if (product != null) {
                    inventory.setProductId(product.getId());
                }
            }

            return inventory;
        } catch (Exception e) {
            System.err.println("转换库存导入VO到DO失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean checkProductExists(Long productId) {
        if (productId == null) {
            return false;
        }
        ErpInventoryDO inventory = inventoryMapper.selectByProductId(productId);
        return inventory != null;
    }
}
