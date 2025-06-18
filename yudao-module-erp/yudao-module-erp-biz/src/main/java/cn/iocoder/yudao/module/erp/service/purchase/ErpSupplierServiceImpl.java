package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpSupplierMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_NOT_ENABLE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_NO_DUPLICATE;

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
        if (CommonStatusEnum.isDisable(supplier.getStatus())) {
            throw exception(SUPPLIER_NOT_ENABLE, supplier.getName());
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
        return supplierMapper.selectListByStatus(status);
    }
    @Override
    public List<ErpSupplierDO> searchSuppliers(ErpSupplierPageReqVO searchReqVO) {
        return supplierMapper.selectList(new LambdaQueryWrapper<ErpSupplierDO>()
                .like(searchReqVO.getNo() != null, ErpSupplierDO::getNo, searchReqVO.getNo())
                .like(searchReqVO.getName() != null, ErpSupplierDO::getName, searchReqVO.getName())
                .like(searchReqVO.getMobile() != null, ErpSupplierDO::getMobile, searchReqVO.getMobile())
                .like(searchReqVO.getTelephone() != null, ErpSupplierDO::getTelephone, searchReqVO.getTelephone()));
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

}
