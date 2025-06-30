package cn.iocoder.yudao.module.erp.service.sample;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSamplePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sample.ErpSampleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sample.ErpSampleMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
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
public class ErpSampleServiceImpl implements ErpSampleService {

    @Resource
    private ErpSampleMapper sampleMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpComboMapper erpComboMapper;

    @Resource
    private ErpCustomerService customerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSample(ErpSampleSaveReqVO createReqVO) {
        // 1. 校验数据
        validateSampleForCreateOrUpdate(null, createReqVO);

        // 2. 生成样品编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.SAMPLE_NO_PREFIX);
        if (sampleMapper.selectByNo(no) != null) {
            throw exception(SAMPLE_NO_EXISTS);
        }

        // 3. 转换组品编号为组品ID
        Long comboProductId = null;
        if (StrUtil.isNotBlank(createReqVO.getComboProductId())) {
            ErpComboProductDO comboProduct = erpComboMapper.selectByNo(createReqVO.getComboProductId());
            if (comboProduct == null) {
                throw exception(SAMPLE_COMBO_PRODUCT_NOT_EXISTS, createReqVO.getComboProductId());
            }
            comboProductId = comboProduct.getId();
        }

        // 4. 插入样品记录
        ErpSampleDO sample = BeanUtils.toBean(createReqVO, ErpSampleDO.class)
                .setNo(no)
                .setComboProductId(comboProductId != null ? comboProductId.toString() : null);
        sampleMapper.insert(sample);

        return sample.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSample(ErpSampleSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateSample(updateReqVO.getId());
        // 1.2 校验数据
        validateSampleForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 转换组品编号为组品ID
        Long comboProductId = null;
        if (StrUtil.isNotBlank(updateReqVO.getComboProductId())) {
            ErpComboProductDO comboProduct = erpComboMapper.selectByNo(updateReqVO.getComboProductId());
            if (comboProduct == null) {
                throw exception(SAMPLE_COMBO_PRODUCT_NOT_EXISTS, updateReqVO.getComboProductId());
            }
            comboProductId = comboProduct.getId();
        }

        // 3. 更新样品记录
        ErpSampleDO updateObj = BeanUtils.toBean(updateReqVO, ErpSampleDO.class)
                .setComboProductId(comboProductId != null ? comboProductId.toString() : null);
        sampleMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSample(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpSampleDO> samples = sampleMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(samples)) {
            throw exception(SAMPLE_NOT_EXISTS);
        }
        // 2. 删除样品记录
        sampleMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpSampleDO getSample(Long id) {
        return sampleMapper.selectById(id);
    }

    @Override
    public ErpSampleDO validateSample(Long id) {
        ErpSampleDO sample = sampleMapper.selectById(id);
        if (sample == null) {
            throw exception(SAMPLE_NOT_EXISTS);
        }
        return sample;
    }

    @Override
    public PageResult<ErpSampleRespVO> getSampleVOPage(ErpSamplePageReqVO pageReqVO) {
        return sampleMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpSampleRespVO> getSampleVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpSampleDO> list = sampleMapper.selectBatchIds(ids);
        List<ErpSampleRespVO> voList = BeanUtils.toBean(list, ErpSampleRespVO.class);

        // 批量查询组品信息并填充发货编码和产品名称
        Set<Long> comboProductIds = list.stream()
                .map(ErpSampleDO::getComboProductId)
                .filter(StrUtil::isNotBlank)
                .map(Long::valueOf)
                .collect(Collectors.toSet());

        if (CollUtil.isNotEmpty(comboProductIds)) {
            List<ErpComboProductDO> comboProducts = erpComboMapper.selectBatchIds(comboProductIds);
            Map<Long, ErpComboProductDO> comboProductMap = convertMap(comboProducts, ErpComboProductDO::getId);

            // 填充组品信息
            for (ErpSampleRespVO vo : voList) {
                if (StrUtil.isNotBlank(vo.getComboProductId())) {
                    Long comboProductId = Long.valueOf(vo.getComboProductId());
                    ErpComboProductDO comboProduct = comboProductMap.get(comboProductId);
                    if (comboProduct != null) {
                        vo.setComboProductId(comboProduct.getNo());
                        vo.setShippingCode(comboProduct.getShippingCode());
                        vo.setComboProductName(comboProduct.getName());
                    }
                }
            }
        }

        return voList;
    }

    @Override
    public Map<Long, ErpSampleRespVO> getSampleVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getSampleVOList(ids), ErpSampleRespVO::getId);
    }

    @Override
    public List<ErpSampleDO> getSampleList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return sampleMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpSampleDO> getSampleMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getSampleList(ids), ErpSampleDO::getId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpSampleImportRespVO importSampleList(List<ErpSampleImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(SAMPLE_IMPORT_LIST_IS_EMPTY);
        }

        // 1. 初始化返回结果
        ErpSampleImportRespVO respVO = ErpSampleImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        try {
            // 2. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateAllImportData(importList, isUpdateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 3. 批量处理列表
            List<ErpSampleDO> createList = new ArrayList<>();
            List<ErpSampleDO> updateList = new ArrayList<>();

            // 4. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpSampleImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpSampleDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(sampleMapper.selectListByNoIn(noSet), ErpSampleDO::getNo);

            // 5. 批量查询组品信息
            Set<String> comboProductNos = importList.stream()
                    .map(ErpSampleImportExcelVO::getComboProductId)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> comboProductIdMap = comboProductNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(erpComboMapper.selectListByNoIn(comboProductNos), ErpComboProductDO::getNo, ErpComboProductDO::getId);

            // 6. 批量查询客户信息
            Set<String> customerNames = importList.stream()
                    .map(ErpSampleImportExcelVO::getCustomerName)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());

            Map<String, Boolean> customerExistsMap = new HashMap<>();
            for (String customerName : customerNames) {
                List<ErpCustomerSaveReqVO> customers = customerService.searchCustomers(
                        new ErpCustomerPageReqVO().setName(customerName));
                customerExistsMap.put(customerName, CollUtil.isNotEmpty(customers));
            }

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();

            // 7. 逐行校验业务逻辑
            for (int i = 0; i < importList.size(); i++) {
                ErpSampleImportExcelVO importVO = importList.get(i);

                // 数据转换
                ErpSampleDO sample = convertImportVOToDO(importVO);

                // 判断是新增还是更新
                ErpSampleDO existSample = existMap.get(importVO.getNo());
                if (existSample == null) {
                    // 创建 - 自动生成新的no编号
                    sample.setNo(noRedisDAO.generate(ErpNoRedisDAO.SAMPLE_NO_PREFIX));
                    createList.add(sample);
                    respVO.getCreateNames().add(sample.getNo());
                } else if (isUpdateSupport) {
                    // 更新
                    sample.setId(existSample.getId());
                    updateList.add(sample);
                    respVO.getUpdateNames().add(sample.getNo());
                }
            }

            // 8. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                sampleMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(sampleMapper::updateById);
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
    private Map<String, String> validateAllImportData(List<ErpSampleImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpSampleImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpSampleDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(sampleMapper.selectListByNoIn(noSet), ErpSampleDO::getNo);

        // 3. 批量查询组品信息
        Set<String> comboProductNos = importList.stream()
                .map(ErpSampleImportExcelVO::getComboProductId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, Long> comboProductIdMap = comboProductNos.isEmpty() ? Collections.emptyMap() :
                convertMap(erpComboMapper.selectListByNoIn(comboProductNos), ErpComboProductDO::getNo, ErpComboProductDO::getId);

        // 4. 批量查询客户信息
        Set<String> customerNames = importList.stream()
                .map(ErpSampleImportExcelVO::getCustomerName)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, Boolean> customerExistsMap = new HashMap<>();
        for (String customerName : customerNames) {
            List<ErpCustomerSaveReqVO> customers = customerService.searchCustomers(
                    new ErpCustomerPageReqVO().setName(customerName));
            customerExistsMap.put(customerName, CollUtil.isNotEmpty(customers));
        }

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();

        // 5. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpSampleImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");

            try {

                // 5.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "样品编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 5.3 校验客户是否存在
                if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                    Boolean customerExists = customerExistsMap.get(importVO.getCustomerName());
                    if (customerExists == null || !customerExists) {
                        allErrors.put(errorKey, "客户不存在: " + importVO.getCustomerName());
                        continue;
                    }
                }

                // 5.4 校验组品是否存在
                if (StrUtil.isNotBlank(importVO.getComboProductId())) {
                    Long comboProductId = comboProductIdMap.get(importVO.getComboProductId());
                    if (comboProductId == null) {
                        allErrors.put(errorKey, "组品编号不存在: " + importVO.getComboProductId());
                        continue;
                    }
                }

                // 5.5 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpSampleDO sample = convertImportVOToDO(importVO);
                    if (sample == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 5.6 判断是新增还是更新，并进行相应校验
                ErpSampleDO existSample = existMap.get(importVO.getNo());
                if (existSample == null) {
                    // 新增校验：无需额外校验
                } else if (isUpdateSupport) {
                    // 更新校验：无需额外校验
                } else {
                    allErrors.put(errorKey, "样品编号已存在，且不支持更新: " + importVO.getNo());
                    continue;
                }

            } catch (Exception ex) {
                allErrors.put(errorKey, "校验异常: " + ex.getMessage());
            }
        }

        return allErrors;
    }

    /**
     * 校验数据类型错误
     */
    private Map<String, String> validateDataTypeErrors(List<ErpSampleImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取样品编号 - 修复行号索引问题
                String sampleNo = "未知样品";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpSampleImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        sampleNo = importVO.getNo();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + sampleNo + ")";
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
     * 转换导入VO为DO对象
     */
    private ErpSampleDO convertImportVOToDO(ErpSampleImportExcelVO importVO) {
        if (importVO == null) {
            return null;
        }

        try {
            ErpSampleDO sample = new ErpSampleDO();

            // 复制基础属性
            BeanUtils.copyProperties(importVO, sample);

            // 将组品业务编号转换为组品ID
            if (StrUtil.isNotBlank(importVO.getComboProductId())) {
                ErpComboProductDO comboProduct = erpComboMapper.selectByNo(importVO.getComboProductId());
                if (comboProduct != null) {
                    sample.setComboProductId(comboProduct.getId().toString());
                }
            }

            return sample;
        } catch (Exception e) {
            System.err.println("转换样品导入VO到DO对象失败，样品编号: " +
                    (importVO.getNo() != null ? importVO.getNo() : "null") + ", 错误: " + e.getMessage());
            return null;
        }
    }

    private void validateSampleForCreateOrUpdate(Long id, ErpSampleSaveReqVO reqVO) {
        // 1. 校验样品编号唯一
        ErpSampleDO sample = sampleMapper.selectByNo(reqVO.getNo());
        if (sample != null && !ObjectUtil.equal(sample.getId(), id)) {
            throw exception(SAMPLE_NO_EXISTS);
        }
    }
}
