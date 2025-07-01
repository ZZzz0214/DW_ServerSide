package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierImportRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpSupplierMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_NOT_ENABLE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_NO_DUPLICATE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_NAME_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_IMPORT_LIST_IS_EMPTY;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_IMPORT_NAME_EMPTY;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_IMPORT_NAME_DUPLICATE;

/**
 * ERP 供应商 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpSupplierServiceImpl implements ErpSupplierService {

    @Resource
    private ErpSupplierMapper supplierMapper;
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    public Long createSupplier(ErpSupplierSaveReqVO createReqVO) {
        // 校验编号唯一性
        if (createReqVO.getNo() != null && !createReqVO.getNo().trim().isEmpty()) {
            validateSupplierNoUnique(null, createReqVO.getNo());
        }
        // 校验供应商名称不能重复
        validateSupplierNameUnique(null, createReqVO.getName());
        
        ErpSupplierDO supplier = BeanUtils.toBean(createReqVO, ErpSupplierDO.class);
        // 如果没有提供编号，自动生成
        if (supplier.getNo() == null || supplier.getNo().trim().isEmpty()) {
            supplier.setNo(generateSupplierNo());
        }
        supplierMapper.insert(supplier);
        return supplier.getId();
    }

    @Override
    public void updateSupplier(ErpSupplierSaveReqVO updateReqVO) {
        // 校验存在
        validateSupplierExists(updateReqVO.getId());
        // 校验编号唯一性
        if (updateReqVO.getNo() != null && !updateReqVO.getNo().trim().isEmpty()) {
            validateSupplierNoUnique(updateReqVO.getId(), updateReqVO.getNo());
        }
        // 校验供应商名称不能重复
        validateSupplierNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        ErpSupplierDO updateObj = BeanUtils.toBean(updateReqVO, ErpSupplierDO.class);
        supplierMapper.updateById(updateObj);
    }

    @Override
    public void deleteSupplier(Long id) {
        // 校验存在
        validateSupplierExists(id);
        // 删除
        supplierMapper.deleteById(id);
    }

    private void validateSupplierExists(Long id) {
        if (supplierMapper.selectById(id) == null) {
            throw exception(SUPPLIER_NOT_EXISTS);
        }
    }

    @Override
    public ErpSupplierDO getSupplier(Long id) {
        return supplierMapper.selectById(id);
    }

    @Override
    public ErpSupplierDO validateSupplier(Long id) {
        ErpSupplierDO supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw exception(SUPPLIER_NOT_EXISTS);
        }
        return supplier;
    }

    @Override
    public List<ErpSupplierDO> getSupplierList(Collection<Long> ids) {
        return supplierMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ErpSupplierDO> getSupplierPage(ErpSupplierPageReqVO pageReqVO) {
        return supplierMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpSupplierDO> getSupplierListByStatus(Integer status) {
        return supplierMapper.selectList();
    }
    @Override
    public List<ErpSupplierDO> searchSuppliers(ErpSupplierPageReqVO searchReqVO) {
        return supplierMapper.searchSuppliers(searchReqVO);
    }

    /**
     * 生成供应商编号
     */
    private String generateSupplierNo() {
        String no;
        do {
            // 使用Redis生成唯一编号
            no = noRedisDAO.generate(ErpNoRedisDAO.SUPPLIER_NO_PREFIX);
        } while (supplierMapper.selectByNo(no) != null); // 确保编号唯一
        return no;
    }
    
    /**
     * 校验供应商编号的唯一性
     */
    private void validateSupplierNoUnique(Long id, String no) {
        ErpSupplierDO supplier = supplierMapper.selectByNo(no);
        if (supplier == null) {
            return;
        }
        // 如果 id 为空，说明不允许存在；否则，如果 id 不相等，说明冲突
        if (id == null || !id.equals(supplier.getId())) {
            throw exception(SUPPLIER_NO_DUPLICATE, no);
        }
    }

    /**
     * 校验供应商名称的唯一性
     */
    private void validateSupplierNameUnique(Long id, String name) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        ErpSupplierDO supplier = supplierMapper.selectByName(name);
        if (supplier == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的供应商
        if (id == null) {
            throw exception(SUPPLIER_NAME_EXISTS);
        }
        if (!supplier.getId().equals(id)) {
            throw exception(SUPPLIER_NAME_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpSupplierImportRespVO importSuppliers(List<ErpSupplierImportExcelVO> importSuppliers) {
        if (CollUtil.isEmpty(importSuppliers)) {
            throw exception(SUPPLIER_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpSupplierImportRespVO respVO = ErpSupplierImportRespVO.builder()
                .createNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理
        List<ErpSupplierDO> createList = new ArrayList<>();

        try {
            // 批量查询已存在的供应商
            Set<String> nameSet = importSuppliers.stream()
                    .map(ErpSupplierImportExcelVO::getName)
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .collect(Collectors.toSet());
            Map<String, ErpSupplierDO> existMap = nameSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(supplierMapper.selectListByNameIn(nameSet), ErpSupplierDO::getName);

            // 用于跟踪Excel内部重复的供应商名称
            Set<String> processedNames = new HashSet<>();
            
            // 批量转换数据
            for (int i = 0; i < importSuppliers.size(); i++) {
                ErpSupplierImportExcelVO importSupplier = importSuppliers.get(i);
                try {
                    // 校验供应商名称
                    if (importSupplier.getName() == null || importSupplier.getName().trim().isEmpty()) {
                        throw exception(SUPPLIER_IMPORT_NAME_EMPTY, i + 1);
                    }
                    
                    // 检查Excel内部供应商名称重复
                    if (processedNames.contains(importSupplier.getName())) {
                        throw exception(SUPPLIER_IMPORT_NAME_DUPLICATE, i + 1, importSupplier.getName());
                    }
                    processedNames.add(importSupplier.getName());

                    // 检查供应商名称是否已存在
                    ErpSupplierDO existSupplier = existMap.get(importSupplier.getName());
                    if (existSupplier != null) {
                        throw exception(SUPPLIER_NAME_EXISTS, i + 1, importSupplier.getName());
                    }

                    // 创建
                    ErpSupplierDO supplier = BeanUtils.toBean(importSupplier, ErpSupplierDO.class);
                    // 生成供应商编号
                    supplier.setNo(generateSupplierNo());
                    createList.add(supplier);
                    respVO.getCreateNames().add(supplier.getName());
                } catch (ServiceException ex) {
                    String errorKey = "第" + (i + 1) + "行" + (importSupplier.getName() != null ? "(" + importSupplier.getName() + ")" : "");
                    respVO.getFailureNames().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    String errorKey = "第" + (i + 1) + "行" + (importSupplier.getName() != null ? "(" + importSupplier.getName() + ")" : "");
                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                supplierMapper.insertBatch(createList);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }

}
