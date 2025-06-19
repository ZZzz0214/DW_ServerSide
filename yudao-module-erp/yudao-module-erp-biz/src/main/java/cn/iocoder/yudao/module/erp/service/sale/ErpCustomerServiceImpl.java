package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerImportRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpCustomerMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

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
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    public Long createCustomer(ErpCustomerSaveReqVO createReqVO) {
        // 插入
        ErpCustomerDO customer = BeanUtils.toBean(createReqVO, ErpCustomerDO.class);
        // 生成客户编号
        customer.setNo(noRedisDAO.generate(ErpNoRedisDAO.CUSTOMER_NO_PREFIX));
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
        return customer;
    }

    @Override
    public List<ErpCustomerDO> getCustomerList(Collection<Long> ids) {
        return customerMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpCustomerDO> getCustomerMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<ErpCustomerDO> list = customerMapper.selectBatchIds(ids);
        return convertMap(list, ErpCustomerDO::getId);
    }

    @Override
    public PageResult<ErpCustomerDO> getCustomerPage(ErpCustomerPageReqVO pageReqVO) {
        return customerMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpCustomerSaveReqVO> searchCustomers(ErpCustomerPageReqVO searchReqVO) {
        List<ErpCustomerDO> customerDOList = customerMapper.selectList(new LambdaQueryWrapper<ErpCustomerDO>()
                .like(StrUtil.isNotBlank(searchReqVO.getNo()), ErpCustomerDO::getNo, searchReqVO.getNo())
                .like(StrUtil.isNotBlank(searchReqVO.getName()), ErpCustomerDO::getName, searchReqVO.getName())
                .like(StrUtil.isNotBlank(searchReqVO.getReceiverName()), ErpCustomerDO::getReceiverName, searchReqVO.getReceiverName())
                .like(StrUtil.isNotBlank(searchReqVO.getTelephone()), ErpCustomerDO::getTelephone, searchReqVO.getTelephone())
                .like(StrUtil.isNotBlank(searchReqVO.getAddress()), ErpCustomerDO::getAddress, searchReqVO.getAddress())
                .like(StrUtil.isNotBlank(searchReqVO.getWechatAccount()), ErpCustomerDO::getWechatAccount, searchReqVO.getWechatAccount())
                .like(StrUtil.isNotBlank(searchReqVO.getAlipayAccount()), ErpCustomerDO::getAlipayAccount, searchReqVO.getAlipayAccount())
                .like(StrUtil.isNotBlank(searchReqVO.getBankAccount()), ErpCustomerDO::getBankAccount, searchReqVO.getBankAccount()));

        return BeanUtils.toBean(customerDOList, ErpCustomerSaveReqVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpCustomerImportRespVO importCustomers(List<ErpCustomerImportExcelVO> importCustomers, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importCustomers)) {
            throw exception(CUSTOMER_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpCustomerImportRespVO respVO = ErpCustomerImportRespVO.builder()
                .createCustomers(new ArrayList<>())
                .updateCustomers(new ArrayList<>())
                .failureCustomers(new LinkedHashMap<>())
                .build();

        // 批量处理
        List<ErpCustomerDO> createList = new ArrayList<>();
        List<ErpCustomerDO> updateList = new ArrayList<>();

        try {
            // 批量查询已存在的客户
            Set<String> nameSet = importCustomers.stream()
                    .map(ErpCustomerImportExcelVO::getName)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpCustomerDO> existMap = nameSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(customerMapper.selectListByNameIn(nameSet), ErpCustomerDO::getName);

            // 用于跟踪Excel内部重复的客户名称
            Set<String> processedNames = new HashSet<>();
            
            // 批量转换数据
            for (int i = 0; i < importCustomers.size(); i++) {
                ErpCustomerImportExcelVO importCustomer = importCustomers.get(i);
                try {
                    // 校验客户名称
                    if (StrUtil.isBlank(importCustomer.getName())) {
                        throw exception(CUSTOMER_IMPORT_NAME_EMPTY, i + 1);
                    }
                    
                    // 检查Excel内部客户名称重复
                    if (processedNames.contains(importCustomer.getName())) {
                        throw exception(CUSTOMER_IMPORT_NAME_DUPLICATE, i + 1, importCustomer.getName());
                    }
                    processedNames.add(importCustomer.getName());

                    // 判断是否支持更新
                    ErpCustomerDO existCustomer = existMap.get(importCustomer.getName());
                    if (existCustomer == null) {
                        // 创建
                        ErpCustomerDO customer = BeanUtils.toBean(importCustomer, ErpCustomerDO.class);
                        // 生成客户编号
                        customer.setNo(noRedisDAO.generate(ErpNoRedisDAO.CUSTOMER_NO_PREFIX));
                        createList.add(customer);
                        respVO.getCreateCustomers().add(customer.getName());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpCustomerDO updateCustomer = BeanUtils.toBean(importCustomer, ErpCustomerDO.class);
                        updateCustomer.setId(existCustomer.getId());
                        updateList.add(updateCustomer);
                        respVO.getUpdateCustomers().add(updateCustomer.getName());
                    } else {
                        throw exception(CUSTOMER_IMPORT_NAME_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importCustomer.getName());
                    }
                } catch (ServiceException ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importCustomer.getName()) ? "(" + importCustomer.getName() + ")" : "");
                    respVO.getFailureCustomers().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importCustomer.getName()) ? "(" + importCustomer.getName() + ")" : "");
                    respVO.getFailureCustomers().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                customerMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(customerMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureCustomers().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }
}
