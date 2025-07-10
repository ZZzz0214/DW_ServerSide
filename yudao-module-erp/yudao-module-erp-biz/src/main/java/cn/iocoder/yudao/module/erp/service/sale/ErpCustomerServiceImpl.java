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
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;

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
        // 校验客户名称不能重复
        validateCustomerNameUnique(null, createReqVO.getName());
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
        // 校验客户名称不能重复
        validateCustomerNameUnique(updateReqVO.getId(), updateReqVO.getName());
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

    private void validateCustomerNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        ErpCustomerDO customer = customerMapper.selectByName(name);
        if (customer == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的客户
        if (id == null) {
            throw exception(CUSTOMER_NAME_EXISTS);
        }
        if (!customer.getId().equals(id)) {
            throw exception(CUSTOMER_NAME_EXISTS);
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
        return customerMapper.selectPage(searchReqVO, new LambdaQueryWrapperX<ErpCustomerDO>()
                .likeIfPresent(ErpCustomerDO::getNo, searchReqVO.getNo())
                .eq(ErpCustomerDO::getName, searchReqVO.getName())
                .likeIfPresent(ErpCustomerDO::getReceiverName, searchReqVO.getReceiverName())
                .likeIfPresent(ErpCustomerDO::getTelephone, searchReqVO.getTelephone())
                .likeIfPresent(ErpCustomerDO::getAddress, searchReqVO.getAddress())
                .likeIfPresent(ErpCustomerDO::getWechatAccount, searchReqVO.getWechatAccount())
                .likeIfPresent(ErpCustomerDO::getAlipayAccount, searchReqVO.getAlipayAccount())
                .likeIfPresent(ErpCustomerDO::getBankAccount, searchReqVO.getBankAccount())
                .betweenIfPresent(ErpCustomerDO::getCreateTime, searchReqVO.getCreateTime())
                .orderByDesc(ErpCustomerDO::getId))
                .getList()
                .stream()
                .map(customer -> BeanUtils.toBean(customer, ErpCustomerSaveReqVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ErpCustomerSaveReqVO> searchCustomersPage(ErpCustomerPageReqVO searchReqVO) {
        // 使用客户Mapper中已有的selectPage方法进行分页查询
        PageResult<ErpCustomerDO> pageResult = customerMapper.selectPage(searchReqVO);

        // 转换为VO并返回
        List<ErpCustomerSaveReqVO> voList = BeanUtils.toBean(pageResult.getList(), ErpCustomerSaveReqVO.class);
        return new PageResult<>(voList, pageResult.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpCustomerImportRespVO importCustomers(List<ErpCustomerImportExcelVO> importCustomers) {
        if (CollUtil.isEmpty(importCustomers)) {
            throw exception(CUSTOMER_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpCustomerImportRespVO respVO = ErpCustomerImportRespVO.builder()
                .createNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理
        List<ErpCustomerDO> createList = new ArrayList<>();

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

                    // 检查客户名称是否已存在
                    ErpCustomerDO existCustomer = existMap.get(importCustomer.getName());
                    if (existCustomer != null) {
                        throw exception(CUSTOMER_NAME_EXISTS, i + 1, importCustomer.getName());
                    }

                    // 创建
                    ErpCustomerDO customer = BeanUtils.toBean(importCustomer, ErpCustomerDO.class);
                    // 生成客户编号
                    customer.setNo(noRedisDAO.generate(ErpNoRedisDAO.CUSTOMER_NO_PREFIX));
                    createList.add(customer);
                    respVO.getCreateNames().add(customer.getName());
                } catch (ServiceException ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importCustomer.getName()) ? "(" + importCustomer.getName() + ")" : "");
                    respVO.getFailureNames().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importCustomer.getName()) ? "(" + importCustomer.getName() + ")" : "");
                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                customerMapper.insertBatch(createList);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }
}
