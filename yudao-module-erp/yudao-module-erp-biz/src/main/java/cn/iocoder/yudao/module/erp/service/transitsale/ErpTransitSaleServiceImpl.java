package cn.iocoder.yudao.module.erp.service.transitsale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.transitsale.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpTransitSaleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.transitsale.ErpTransitSaleMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.system.enums.DictTypeConstants.ERP_TRANSIT_PERSON;

@Service
@Validated
public class ErpTransitSaleServiceImpl implements ErpTransitSaleService {

    @Resource
    private ErpTransitSaleMapper transitSaleMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpComboProductService erpComboProductService;

    @Resource
    private ErpComboMapper erpComboMapper;

    @Resource
    private DictDataApi dictDataApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransitSale(ErpTransitSaleSaveReqVO createReqVO) {
        // 1. 校验数据
        validateTransitSaleForCreateOrUpdate(null, createReqVO);

        // 2. 生成中转销售记录编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.TRANSIT_SALE_NO_PREFIX);
        if (transitSaleMapper.selectByNo(no) != null) {
            throw exception(TRANSIT_SALE_NO_EXISTS);
        }

        // 3. 插入中转销售记录
        ErpTransitSaleDO transitSale = BeanUtils.toBean(createReqVO, ErpTransitSaleDO.class)
                .setNo(no);

        transitSaleMapper.insert(transitSale);

        return transitSale.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransitSale(ErpTransitSaleSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateTransitSale(updateReqVO.getId());
        // 1.2 校验数据
        validateTransitSaleForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新中转销售记录
        ErpTransitSaleDO updateObj = BeanUtils.toBean(updateReqVO, ErpTransitSaleDO.class);
        transitSaleMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransitSale(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpTransitSaleDO> transitSales = transitSaleMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(transitSales)) {
            throw exception(TRANSIT_SALE_NOT_EXISTS);
        }
        // 2. 删除中转销售记录
        transitSaleMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpTransitSaleRespVO getTransitSale(Long id) {
        ErpTransitSaleDO transitSale = transitSaleMapper.selectById(id);
        if (transitSale == null) {
            return null;
        }

        // 转换基础字段
        ErpTransitSaleRespVO respVO = BeanUtils.toBean(transitSale, ErpTransitSaleRespVO.class);

        // 获取组品信息并填充
        if (transitSale.getGroupProductId() != null) {
            ErpComboRespVO comboRespVO = erpComboProductService.getComboWithItems(transitSale.getGroupProductId());
            //ErpComboProductDO comboProduct = erpComboProductService.getCombo(transitSale.getGroupProductId());
            if (comboRespVO != null) {
                respVO.setComboList(Collections.singletonList(comboRespVO));
                respVO.setGroupProductId(comboRespVO.getId());
                respVO.setProductName(comboRespVO.getName());
                respVO.setProductShortName(comboRespVO.getShortName());
                respVO.setGroupProductNo(comboRespVO.getNo());
            }
        }

        return respVO;
    }

    @Override
    public ErpTransitSaleDO validateTransitSale(Long id) {
        ErpTransitSaleDO transitSale = transitSaleMapper.selectById(id);
        if (transitSale == null) {
            throw exception(TRANSIT_SALE_NOT_EXISTS);
        }
        return transitSale;
    }

    @Override
    public PageResult<ErpTransitSaleRespVO> getTransitSaleVOPage(ErpTransitSalePageReqVO pageReqVO) {
        return transitSaleMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpTransitSaleRespVO> getTransitSaleVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpTransitSaleDO> list = transitSaleMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpTransitSaleRespVO.class);
    }

    @Override
    public Map<Long, ErpTransitSaleRespVO> getTransitSaleVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getTransitSaleVOList(ids), ErpTransitSaleRespVO::getId);
    }

    @Override
    public List<ErpTransitSaleDO> getTransitSaleList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return transitSaleMapper.selectBatchIds(ids);
    }

    private void validateTransitSaleForCreateOrUpdate(Long id, ErpTransitSaleSaveReqVO reqVO) {
        // 1. 校验编号唯一
        ErpTransitSaleDO transitSale = transitSaleMapper.selectByNo(reqVO.getNo());
        if (transitSale != null && !ObjectUtil.equal(transitSale.getId(), id)) {
            throw exception(TRANSIT_SALE_NO_EXISTS);
        }

        // 2. 校验中转人员和组品组合唯一性
        if (StrUtil.isNotBlank(reqVO.getTransitPerson()) && reqVO.getGroupProductId() != null) {
            // 数据库中存储的是字典值，直接使用进行校验
            Long count = transitSaleMapper.selectCountByTransitPersonAndGroupProductId(
                    reqVO.getTransitPerson(), reqVO.getGroupProductId(), id);
            if (count > 0) {
                throw exception(TRANSIT_SALE_PERSON_PRODUCT_DUPLICATE);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpTransitSaleImportRespVO importTransitSaleList(List<ErpTransitSaleImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(TRANSIT_SALE_IMPORT_LIST_IS_EMPTY);
        }

        System.out.println("开始导入中转销售数据，共" + importList.size() + "条记录");

        // 初始化返回结果
        ErpTransitSaleImportRespVO respVO = ErpTransitSaleImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String username = WebFrameworkUtils.getUsernameById(userId);
        LocalDateTime now = LocalDateTime.now();

        try {
            // 1. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateAllImportData(importList, isUpdateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 2. 批量预加载组品信息到缓存
            Set<String> groupProductNos = importList.stream()
                    .map(ErpTransitSaleImportExcelVO::getGroupProductNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());

            Map<String, ErpComboProductDO> groupProductMap = preloadComboProducts(groupProductNos);

            // 3. 批量查询已存在的中转销售记录
            Set<String> noSet = importList.stream()
                    .map(ErpTransitSaleImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());

            Map<String, ErpTransitSaleDO> existingTransitSaleMap = Collections.emptyMap();
            if (CollUtil.isNotEmpty(noSet)) {
                List<ErpTransitSaleDO> existList = transitSaleMapper.selectListByNoIn(noSet);
                existingTransitSaleMap = convertMap(existList, ErpTransitSaleDO::getNo);
            }

            // 4. 批量处理数据
            List<ErpTransitSaleDO> toCreateList = new ArrayList<>();
            List<ErpTransitSaleDO> toUpdateList = new ArrayList<>();

            for (int i = 0; i < importList.size(); i++) {
                ErpTransitSaleImportExcelVO importVO = importList.get(i);
                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "第" + (i + 1) + "行";

                try {
                    // 验证组品编号
                    ErpComboProductDO comboProduct = groupProductMap.get(importVO.getGroupProductNo());
                    if (comboProduct == null) {
                        throw exception(TRANSIT_SALE_GROUP_PRODUCT_ID_REQUIRED, "组品编号不存在: " + importVO.getGroupProductNo());
                    }

                    // 判断是否支持更新
                    ErpTransitSaleDO existTransitSale = existingTransitSaleMap.get(importVO.getNo());

                    if (existTransitSale == null) {
                        // 创建新记录
                        ErpTransitSaleDO transitSale = BeanUtils.toBean(importVO, ErpTransitSaleDO.class);
                        transitSale.setGroupProductId(comboProduct.getId())
                                .setCreator(username)
                                .setCreateTime(now);


                            String newNo = noRedisDAO.generate(ErpNoRedisDAO.TRANSIT_SALE_NO_PREFIX);
                            transitSale.setNo(newNo);


                        toCreateList.add(transitSale);
                        respVO.getCreateNames().add(transitSale.getNo());

                    } else if (isUpdateSupport) {
                        // 更新记录 - 只更新导入文件中提供的字段
                        ErpTransitSaleDO updateTransitSale = new ErpTransitSaleDO();
                        updateTransitSale.setId(existTransitSale.getId());
                        updateTransitSale.setGroupProductId(comboProduct.getId())
                                .setCreator(username)
                                .setCreateTime(now);

                        // 只更新导入文件中提供的字段
                        if (importVO.getTransitPerson() != null) {
                            updateTransitSale.setTransitPerson(importVO.getTransitPerson());
                        }
                        if (importVO.getDistributionPrice() != null) {
                            updateTransitSale.setDistributionPrice(importVO.getDistributionPrice());
                        }
                        if (importVO.getWholesalePrice() != null) {
                            updateTransitSale.setWholesalePrice(importVO.getWholesalePrice());
                        }
                        if (importVO.getShippingFeeType() != null) {
                            updateTransitSale.setShippingFeeType(importVO.getShippingFeeType());
                        }
                        if (importVO.getFixedShippingFee() != null) {
                            updateTransitSale.setFixedShippingFee(importVO.getFixedShippingFee());
                        }
                        if (importVO.getAdditionalItemQuantity() != null) {
                            updateTransitSale.setAdditionalItemQuantity(importVO.getAdditionalItemQuantity());
                        }
                        if (importVO.getAdditionalItemPrice() != null) {
                            updateTransitSale.setAdditionalItemPrice(importVO.getAdditionalItemPrice());
                        }
                        if (importVO.getFirstWeight() != null) {
                            updateTransitSale.setFirstWeight(importVO.getFirstWeight());
                        }
                        if (importVO.getFirstWeightPrice() != null) {
                            updateTransitSale.setFirstWeightPrice(importVO.getFirstWeightPrice());
                        }
                        if (importVO.getAdditionalWeight() != null) {
                            updateTransitSale.setAdditionalWeight(importVO.getAdditionalWeight());
                        }
                        if (importVO.getAdditionalWeightPrice() != null) {
                            updateTransitSale.setAdditionalWeightPrice(importVO.getAdditionalWeightPrice());
                        }
                        if (importVO.getRemark() != null) {
                            updateTransitSale.setRemark(importVO.getRemark());
                        }

                        toUpdateList.add(updateTransitSale);
                        respVO.getUpdateNames().add(existTransitSale.getNo());

                    } else {
                        throw exception(TRANSIT_SALE_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
                    }

                } catch (ServiceException ex) {
                    respVO.getFailureNames().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    System.err.println("导入第" + (i + 1) + "行数据异常: " + ex.getMessage());
                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 5. 批量执行数据库操作
            try {
                // 批量插入
                if (CollUtil.isNotEmpty(toCreateList)) {
                    batchInsertTransitSales(toCreateList);
                }

                // 批量更新
                if (CollUtil.isNotEmpty(toUpdateList)) {
                    batchUpdateTransitSales(toUpdateList);
                }

            } catch (Exception e) {
                System.err.println("批量操作数据库失败: " + e.getMessage());
                throw new RuntimeException("批量导入失败: " + e.getMessage(), e);
            }

        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        } finally {
            // 清除转换错误
            ConversionErrorHolder.clearErrors();
        }

        System.out.println("导入完成，成功创建：" + respVO.getCreateNames().size() +
                          "，成功更新：" + respVO.getUpdateNames().size() +
                          "，失败：" + respVO.getFailureNames().size());
        return respVO;
    }

    /**
     * 统一校验所有导入数据（包括数据类型校验和业务逻辑校验）
     * 如果出现任何错误信息都记录下来并返回，后续操作就不进行了
     */
    private Map<String, String> validateAllImportData(List<ErpTransitSaleImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量预加载组品信息到缓存
        Set<String> groupProductNos = importList.stream()
                .map(ErpTransitSaleImportExcelVO::getGroupProductNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, ErpComboProductDO> groupProductMap = preloadComboProducts(groupProductNos);

        // 3. 批量查询已存在的中转销售记录
        Set<String> noSet = importList.stream()
                .map(ErpTransitSaleImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, ErpTransitSaleDO> existingTransitSaleMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(noSet)) {
            List<ErpTransitSaleDO> existList = transitSaleMapper.selectListByNoIn(noSet);
            existingTransitSaleMap = convertMap(existList, ErpTransitSaleDO::getNo);
        }

        // 4. 批量查询已存在的中转人员+组品ID组合
        List<ErpTransitSaleDO> allExistingTransitSales = transitSaleMapper.selectList(null);
        Map<String, Set<Long>> transitPersonProductMap = new HashMap<>();
        for (ErpTransitSaleDO transitSale : allExistingTransitSales) {
            if (StrUtil.isNotBlank(transitSale.getTransitPerson()) && transitSale.getGroupProductId() != null) {
                // 数据库中存储的是字典值，直接使用
                transitPersonProductMap.computeIfAbsent(transitSale.getTransitPerson(), k -> new HashSet<>())
                        .add(transitSale.getGroupProductId());
            }
        }

        // 5. 批量查询所有中转人员名称，验证中转人员是否存在
        Set<String> transitPersonNames = importList.stream()
                .map(ErpTransitSaleImportExcelVO::getTransitPerson)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, Boolean> transitPersonExistsMap = new HashMap<>();
        for (String transitPersonName : transitPersonNames) {
            try {
                // 使用字典API校验中转人员是否存在
                dictDataApi.validateDictDataList(ERP_TRANSIT_PERSON, Collections.singletonList(transitPersonName));
                transitPersonExistsMap.put(transitPersonName, true);
            } catch (Exception e) {
                transitPersonExistsMap.put(transitPersonName, false);
            }
        }

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();

        // 6. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpTransitSaleImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行组品编号" + "(" + importVO.getGroupProductNo() + ")";

            try {
                // 6.1 基础数据校验
                if (StrUtil.isEmpty(importVO.getGroupProductNo())) {
                    allErrors.put(errorKey, "组品编号不能为空");
                    continue;
                }

                // 6.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "中转销售编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 6.3 校验组品编号是否存在
                ErpComboProductDO comboProduct = groupProductMap.get(importVO.getGroupProductNo());
                if (comboProduct == null) {
                    allErrors.put(errorKey, "组品编号不存在: " + importVO.getGroupProductNo());
                    continue;
                }

                // 6.4 校验中转人员是否存在
                Boolean transitPersonExists = transitPersonExistsMap.get(importVO.getTransitPerson());
                if (transitPersonExists == null || !transitPersonExists) {
                    allErrors.put(errorKey, "中转人员不存在,组品编号是: " + importVO.getGroupProductNo());
                    continue;
                }

                // 6.5 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpTransitSaleDO transitSale = convertImportVOToDO(importVO, comboProduct);
                    if (transitSale == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 6.6 判断是新增还是更新，并进行相应校验
                ErpTransitSaleDO existTransitSale = existingTransitSaleMap.get(importVO.getNo());
                if (existTransitSale == null) {
                    // 新增校验：校验中转人员+组品ID的唯一性
                    try {
                        validateTransitPersonProductUniqueWithCache(importVO.getTransitPerson(), comboProduct.getId(), transitPersonProductMap, null);
                    } catch (ServiceException ex) {
                        allErrors.put(errorKey, ex.getMessage());
                    }
                } else if (isUpdateSupport) {
                    // 更新校验：校验中转人员+组品ID的唯一性（排除自身）
                    try {
                        validateTransitPersonProductUniqueWithCache(importVO.getTransitPerson(), comboProduct.getId(), transitPersonProductMap, existTransitSale.getId());
                    } catch (ServiceException ex) {
                        allErrors.put(errorKey, ex.getMessage());
                    }
                } else {
                    allErrors.put(errorKey, "中转销售编号不存在且不支持更新: " + importVO.getNo());
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
    private Map<String, String> validateDataTypeErrors(List<ErpTransitSaleImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取中转人员名称 - 修复行号索引问题
                String transitPerson = "未知组品编号";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpTransitSaleImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getGroupProductNo())) {
                        transitPerson = importVO.getGroupProductNo();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + transitPerson + ")";
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
     * 特别注意处理字段类型转换
     */
    private ErpTransitSaleDO convertImportVOToDO(ErpTransitSaleImportExcelVO importVO, ErpComboProductDO comboProduct) {
        if (importVO == null) {
            return null;
        }

        // 添加调试信息
        System.out.println("=== 中转销售转换调试信息 ===");
        System.out.println("中转人员: " + importVO.getTransitPerson());
        System.out.println("组品编号: " + importVO.getGroupProductNo());
        System.out.println("代发单价: " + importVO.getDistributionPrice() + " (类型: " + (importVO.getDistributionPrice() != null ? importVO.getDistributionPrice().getClass().getSimpleName() : "null") + ")");
        System.out.println("批发单价: " + importVO.getWholesalePrice() + " (类型: " + (importVO.getWholesalePrice() != null ? importVO.getWholesalePrice().getClass().getSimpleName() : "null") + ")");
        System.out.println("固定运费: " + importVO.getFixedShippingFee() + " (类型: " + (importVO.getFixedShippingFee() != null ? importVO.getFixedShippingFee().getClass().getSimpleName() : "null") + ")");
        System.out.println("==================");

        // 使用BeanUtils进行基础转换
        ErpTransitSaleDO transitSale = BeanUtils.toBean(importVO, ErpTransitSaleDO.class);

        // 手动设置转换器处理的字段，确保数据正确传递
        transitSale.setDistributionPrice(importVO.getDistributionPrice());
        transitSale.setWholesalePrice(importVO.getWholesalePrice());
        transitSale.setShippingFeeType(importVO.getShippingFeeType());
        transitSale.setFixedShippingFee(importVO.getFixedShippingFee());
        transitSale.setAdditionalItemQuantity(importVO.getAdditionalItemQuantity());
        transitSale.setAdditionalItemPrice(importVO.getAdditionalItemPrice());
        transitSale.setFirstWeight(importVO.getFirstWeight());
        transitSale.setFirstWeightPrice(importVO.getFirstWeightPrice());
        transitSale.setAdditionalWeight(importVO.getAdditionalWeight());
        transitSale.setAdditionalWeightPrice(importVO.getAdditionalWeightPrice());

        // 设置组品ID
        if (comboProduct != null) {
            transitSale.setGroupProductId(comboProduct.getId());
        }

        // 添加转换后的调试信息
        System.out.println("=== 转换后调试信息 ===");
        System.out.println("中转人员: " + transitSale.getTransitPerson());
        System.out.println("组品ID: " + transitSale.getGroupProductId());
        System.out.println("代发单价: " + transitSale.getDistributionPrice() + " (类型: " + (transitSale.getDistributionPrice() != null ? transitSale.getDistributionPrice().getClass().getSimpleName() : "null") + ")");
        System.out.println("批发单价: " + transitSale.getWholesalePrice() + " (类型: " + (transitSale.getWholesalePrice() != null ? transitSale.getWholesalePrice().getClass().getSimpleName() : "null") + ")");
        System.out.println("固定运费: " + transitSale.getFixedShippingFee() + " (类型: " + (transitSale.getFixedShippingFee() != null ? transitSale.getFixedShippingFee().getClass().getSimpleName() : "null") + ")");
        System.out.println("==================");

        return transitSale;
    }

    /**
     * 预加载组品信息到缓存
     */
    private Map<String, ErpComboProductDO> preloadComboProducts(Set<String> groupProductNos) {
        if (CollUtil.isEmpty(groupProductNos)) {
            return Collections.emptyMap();
        }

        List<ErpComboProductDO> comboProducts = erpComboMapper.selectList(
            new LambdaQueryWrapper<ErpComboProductDO>()
                .in(ErpComboProductDO::getNo, groupProductNos)
        );

        return convertMap(comboProducts, ErpComboProductDO::getNo);
    }

    /**
     * 使用预加载的数据校验中转人员+组品ID的唯一性
     */
    private void validateTransitPersonProductUniqueWithCache(String transitPerson, Long groupProductId,
            Map<String, Set<Long>> transitPersonProductMap, Long excludeId) {
        if (StrUtil.isBlank(transitPerson) || groupProductId == null) {
            return;
        }

        Set<Long> productIds = transitPersonProductMap.get(transitPerson);
        if (productIds != null && productIds.contains(groupProductId)) {
            // 如果是更新操作，需要进一步检查是否是同一条记录
            if (excludeId != null) {
                Long count = transitSaleMapper.selectCountByTransitPersonAndGroupProductId(
                        transitPerson, groupProductId, excludeId);
                if (count > 0) {
                    throw exception(TRANSIT_SALE_PERSON_PRODUCT_DUPLICATE,
                            "中转人员(" + transitPerson + ")和组品编号的组合已存在");
                }
            } else {
                throw exception(TRANSIT_SALE_PERSON_PRODUCT_DUPLICATE,
                        "中转人员(" + transitPerson + ")和组品编号的组合已存在");
            }
        }
    }

    /**
     * 批量插入中转销售
     */
    private void batchInsertTransitSales(List<ErpTransitSaleDO> transitSales) {
        // 分批插入，避免SQL过长
        int batchSize = 500;
        for (int i = 0; i < transitSales.size(); i += batchSize) {
            int end = Math.min(i + batchSize, transitSales.size());
            List<ErpTransitSaleDO> batch = transitSales.subList(i, end);

            for (ErpTransitSaleDO transitSale : batch) {
                transitSaleMapper.insert(transitSale);
            }
        }
    }

    /**
     * 批量更新中转销售
     */
    private void batchUpdateTransitSales(List<ErpTransitSaleDO> transitSales) {
        for (ErpTransitSaleDO transitSale : transitSales) {
            // 使用 LambdaUpdateWrapper 进行选择性更新
            LambdaUpdateWrapper<ErpTransitSaleDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ErpTransitSaleDO::getId, transitSale.getId());

            // 只更新非null的字段
            if (transitSale.getGroupProductId() != null) {
                updateWrapper.set(ErpTransitSaleDO::getGroupProductId, transitSale.getGroupProductId());
            }
            if (transitSale.getTransitPerson() != null) {
                updateWrapper.set(ErpTransitSaleDO::getTransitPerson, transitSale.getTransitPerson());
            }
            if (transitSale.getDistributionPrice() != null) {
                updateWrapper.set(ErpTransitSaleDO::getDistributionPrice, transitSale.getDistributionPrice());
            }
            if (transitSale.getWholesalePrice() != null) {
                updateWrapper.set(ErpTransitSaleDO::getWholesalePrice, transitSale.getWholesalePrice());
            }
            if (transitSale.getShippingFeeType() != null) {
                updateWrapper.set(ErpTransitSaleDO::getShippingFeeType, transitSale.getShippingFeeType());
            }
            if (transitSale.getFixedShippingFee() != null) {
                updateWrapper.set(ErpTransitSaleDO::getFixedShippingFee, transitSale.getFixedShippingFee());
            }
            if (transitSale.getAdditionalItemQuantity() != null) {
                updateWrapper.set(ErpTransitSaleDO::getAdditionalItemQuantity, transitSale.getAdditionalItemQuantity());
            }
            if (transitSale.getAdditionalItemPrice() != null) {
                updateWrapper.set(ErpTransitSaleDO::getAdditionalItemPrice, transitSale.getAdditionalItemPrice());
            }
            if (transitSale.getFirstWeight() != null) {
                updateWrapper.set(ErpTransitSaleDO::getFirstWeight, transitSale.getFirstWeight());
            }
            if (transitSale.getFirstWeightPrice() != null) {
                updateWrapper.set(ErpTransitSaleDO::getFirstWeightPrice, transitSale.getFirstWeightPrice());
            }
            if (transitSale.getAdditionalWeight() != null) {
                updateWrapper.set(ErpTransitSaleDO::getAdditionalWeight, transitSale.getAdditionalWeight());
            }
            if (transitSale.getAdditionalWeightPrice() != null) {
                updateWrapper.set(ErpTransitSaleDO::getAdditionalWeightPrice, transitSale.getAdditionalWeightPrice());
            }
            if (transitSale.getRemark() != null) {
                updateWrapper.set(ErpTransitSaleDO::getRemark, transitSale.getRemark());
            }

            transitSaleMapper.update(null, updateWrapper);
        }
    }
}
