package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpCustomerMapper;

import org.checkerframework.checker.units.qual.s;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.CUSTOMER_NOT_ENABLE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.CUSTOMER_NOT_EXISTS;

/**
 * ERP 客户 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpCustomerServiceImpl implements ErpCustomerService {

    @Resource
    private ErpCustomerMapper customerMapper;

    @Override
    public Long createCustomer(ErpCustomerSaveReqVO createReqVO) {
        // 插入
        ErpCustomerDO customer = BeanUtils.toBean(createReqVO, ErpCustomerDO.class);
        customerMapper.insert(customer);
        // 返回
        return customer.getId();
    }

    @Override
    public void updateCustomer(ErpCustomerSaveReqVO updateReqVO) {
        // 校验存在
        validateCustomerExists(updateReqVO.getId());
        // 更新
        ErpCustomerDO updateObj = BeanUtils.toBean(updateReqVO, ErpCustomerDO.class);
        customerMapper.updateById(updateObj);
    }

    @Override
    public void deleteCustomer(Long id) {
        // 校验存在
        validateCustomerExists(id);
        // 删除
        customerMapper.deleteById(id);
    }

    private void validateCustomerExists(Long id) {
        if (customerMapper.selectById(id) == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
    }

    @Override
    public ErpCustomerDO getCustomer(Long id) {
        return customerMapper.selectById(id);
    }

    @Override
    public ErpCustomerDO validateCustomer(Long id) {
        ErpCustomerDO customer = customerMapper.selectById(id);
        if (customer == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(customer.getStatus())) {
            throw exception(CUSTOMER_NOT_ENABLE, customer.getName());
        }
        return customer;
    }

    @Override
    public List<ErpCustomerDO> getCustomerList(Collection<Long> ids) {
        return customerMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ErpCustomerDO> getCustomerPage(ErpCustomerPageReqVO pageReqVO) {
        return customerMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpCustomerDO> getCustomerListByStatus(Integer status) {
        return customerMapper.selectListByStatus(status);
    }

//    @Override
//    public List<ErpCustomerSaveReqVO> searchCustomers(ErpCustomerPageReqVO searchReqVO) {
//        // 直接使用searchReqVO作为查询条件
//        System.out.println(searchReqVO);
//        List<ErpCustomerDO> customerDOList = customerMapper.selectList(new LambdaQueryWrapper<ErpCustomerDO>()
//                .eq(searchReqVO.getName() != null, ErpCustomerDO::getName, searchReqVO.getName())
//                .eq(searchReqVO.getMobile() != null, ErpCustomerDO::getMobile, searchReqVO.getMobile())
//                .eq(searchReqVO.getTelephone() != null, ErpCustomerDO::getTelephone, searchReqVO.getTelephone()));
//        System.out.println(customerDOList);
//        // 转换为响应对象
//        return BeanUtils.toBean(customerDOList, ErpCustomerSaveReqVO.class);
//    }

    @Override
    public List<ErpCustomerSaveReqVO> searchCustomers(ErpCustomerPageReqVO searchReqVO) {
        List<ErpCustomerDO> customerDOList = customerMapper.selectList(new LambdaQueryWrapper<ErpCustomerDO>()
                .eq(StrUtil.isNotBlank(searchReqVO.getName()), ErpCustomerDO::getName, searchReqVO.getName())
                .like(StrUtil.isNotBlank(searchReqVO.getMobile()), ErpCustomerDO::getMobile, searchReqVO.getMobile())
                .like(StrUtil.isNotBlank(searchReqVO.getTelephone()), ErpCustomerDO::getTelephone, searchReqVO.getTelephone()));

        return BeanUtils.toBean(customerDOList, ErpCustomerSaveReqVO.class);
    }
}
