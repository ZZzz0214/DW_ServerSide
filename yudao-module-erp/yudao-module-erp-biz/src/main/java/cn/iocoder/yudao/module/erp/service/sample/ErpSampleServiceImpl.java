package cn.iocoder.yudao.module.erp.service.sample;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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

        // 初始化返回结果
        ErpSampleImportRespVO respVO = ErpSampleImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理
        List<ErpSampleDO> createList = new ArrayList<>();
        List<ErpSampleDO> updateList = new ArrayList<>();

        try {
            // 批量查询组品信息
            Set<String> comboProductNos = importList.stream()
                    .map(ErpSampleImportExcelVO::getComboProductId)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> comboProductIdMap = comboProductNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(erpComboMapper.selectListByNoIn(comboProductNos), ErpComboProductDO::getNo, ErpComboProductDO::getId);

            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpSampleImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpSampleDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(sampleMapper.selectListByNoIn(noSet), ErpSampleDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();

            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpSampleImportExcelVO importVO = importList.get(i);
                try {
                    // 校验必填字段
                    if (StrUtil.isBlank(importVO.getLogisticsCompany())) {
                        throw exception(SAMPLE_IMPORT_LOGISTICS_COMPANY_EMPTY, i + 1);
                    }
                    if (StrUtil.isBlank(importVO.getLogisticsNo())) {
                        throw exception(SAMPLE_IMPORT_LOGISTICS_NO_EMPTY, i + 1);
                    }
                    if (StrUtil.isBlank(importVO.getReceiverName())) {
                        throw exception(SAMPLE_IMPORT_RECEIVER_NAME_EMPTY, i + 1);
                    }
                    if (StrUtil.isBlank(importVO.getContactPhone())) {
                        throw exception(SAMPLE_IMPORT_CONTACT_PHONE_EMPTY, i + 1);
                    }
                    if (StrUtil.isBlank(importVO.getAddress())) {
                        throw exception(SAMPLE_IMPORT_ADDRESS_EMPTY, i + 1);
                    }
                    if (StrUtil.isBlank(importVO.getComboProductId())) {
                        throw exception(SAMPLE_IMPORT_COMBO_PRODUCT_ID_EMPTY, i + 1);
                    }
                    if (StrUtil.isBlank(importVO.getProductSpec())) {
                        throw exception(SAMPLE_IMPORT_PRODUCT_SPEC_EMPTY, i + 1);
                    }
                    if (importVO.getProductQuantity() == null || importVO.getProductQuantity() <= 0) {
                        throw exception(SAMPLE_IMPORT_PRODUCT_QUANTITY_INVALID, i + 1);
                    }
                    if (StrUtil.isBlank(importVO.getCustomerName())) {
                        throw exception(SAMPLE_IMPORT_CUSTOMER_NAME_EMPTY, i + 1);
                    }
                    if (importVO.getSampleStatus() == null) {
                        throw exception(SAMPLE_IMPORT_SAMPLE_STATUS_INVALID, i + 1);
                    }

                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(SAMPLE_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 校验客户是否存在
                    if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                        List<ErpCustomerSaveReqVO> customers = customerService.searchCustomers(
                                new ErpCustomerPageReqVO().setName(importVO.getCustomerName()));
                        if (CollUtil.isEmpty(customers)) {
                            throw exception(SAMPLE_IMPORT_CUSTOMER_NOT_EXISTS, i + 1, importVO.getCustomerName());
                        }
                    }

                    // 将组品业务编号转换为组品ID
                    if (StrUtil.isNotBlank(importVO.getComboProductId())) {
                        Long comboProductId = comboProductIdMap.get(importVO.getComboProductId());
                        if (comboProductId == null) {
                            throw exception(SAMPLE_IMPORT_COMBO_PRODUCT_NOT_EXISTS, i + 1, importVO.getComboProductId());
                        }
                        importVO.setComboProductId(comboProductId.toString());
                    }

                    // 判断是否支持更新
                    ErpSampleDO existSample = existMap.get(importVO.getNo());
                    if (existSample == null) {
                        // 创建 - 自动生成新的no编号
                        ErpSampleDO sample = BeanUtils.toBean(importVO, ErpSampleDO.class);
                        sample.setNo(noRedisDAO.generate(ErpNoRedisDAO.SAMPLE_NO_PREFIX));
                        createList.add(sample);
                        respVO.getCreateNames().add(sample.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpSampleDO updateSample = BeanUtils.toBean(importVO, ErpSampleDO.class);
                        updateSample.setId(existSample.getId());
                        updateList.add(updateSample);
                        respVO.getUpdateNames().add(updateSample.getNo());
                    } else {
                        throw exception(SAMPLE_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
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
                sampleMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(sampleMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }

    private void validateSampleForCreateOrUpdate(Long id, ErpSampleSaveReqVO reqVO) {
        // 1. 校验样品编号唯一
        ErpSampleDO sample = sampleMapper.selectByNo(reqVO.getNo());
        if (sample != null && !ObjectUtil.equal(sample.getId(), id)) {
            throw exception(SAMPLE_NO_EXISTS);
        }
    }
}
