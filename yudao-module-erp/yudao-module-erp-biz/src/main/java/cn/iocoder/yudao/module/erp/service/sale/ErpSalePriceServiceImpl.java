package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePricePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSalePriceMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_PRICE_NOT_EXISTS;

@Service
@Validated
public class ErpSalePriceServiceImpl implements ErpSalePriceService {

    @Resource
    private ErpSalePriceMapper erpSalePriceMapper;

    @Resource
    private ErpComboProductService erpComboProductService;

    @Resource
    private ErpComboMapper erpComboMapper;
    @Override
    public Long createSalePrice(@Valid ErpSalePriceSaveReqVO createReqVO) {
        // 根据groupProductId获取组品信息
        Long groupProductId = createReqVO.getGroupProductId();
        ErpComboRespVO comboInfo = null;
        if (groupProductId != null) {
            comboInfo = erpComboProductService.getComboWithItems(groupProductId);
        }

        // 保存销售价格信息
        ErpSalePriceDO salePriceDO = BeanUtils.toBean(createReqVO, ErpSalePriceDO.class);

        // 设置从组品获取的信息
        if (comboInfo != null) {
            salePriceDO.setProductName(comboInfo.getName());
            salePriceDO.setProductShortName(comboInfo.getShortName());
            salePriceDO.setProductImage(comboInfo.getImage());
        }

        erpSalePriceMapper.insert(salePriceDO);
        return salePriceDO.getId();
    }

    @Override
    public void updateSalePrice(@Valid ErpSalePriceSaveReqVO updateReqVO) {
        validateSalePriceExists(updateReqVO.getId());

        // 根据groupProductId获取组品信息
        Long groupProductId = updateReqVO.getGroupProductId();
        ErpComboRespVO comboInfo = null;
        if (groupProductId != null) {
            comboInfo = erpComboProductService.getComboWithItems(groupProductId);
        }

        ErpSalePriceDO updateObj = BeanUtils.toBean(updateReqVO, ErpSalePriceDO.class);

        // 设置从组品获取的信息
        if (comboInfo != null) {
            updateObj.setProductName(comboInfo.getName());
            updateObj.setProductShortName(comboInfo.getShortName());
            updateObj.setProductImage(comboInfo.getImage());
        }

        erpSalePriceMapper.updateById(updateObj);
    }

    @Override
    public void deleteSalePrice(Long id) {
        validateSalePriceExists(id);
        erpSalePriceMapper.deleteById(id);
    }

    @Override
    public List<ErpSalePriceDO> validSalePriceList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpSalePriceMapper.selectBatchIds(ids);
    }

    private void validateSalePriceExists(Long id) {
        if (erpSalePriceMapper.selectById(id) == null) {
            throw exception(SALE_PRICE_NOT_EXISTS);
        }
    }

    @Override
    public ErpSalePriceDO getSalePrice(Long id) {
        return erpSalePriceMapper.selectById(id);
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpSalePriceDO> list = erpSalePriceMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpSalePriceRespVO.class);
    }

    @Override
    public PageResult<ErpSalePriceRespVO> getSalePriceVOPage(ErpSalePricePageReqVO pageReqVO) {
        PageResult<ErpSalePriceDO> pageResult = erpSalePriceMapper.selectPage(pageReqVO);
        return new PageResult<>(BeanUtils.toBean(pageResult.getList(), ErpSalePriceRespVO.class), pageResult.getTotal());
    }

//    @Override
//    public ErpSalePriceRespVO getSalePriceWithItems(Long id) {
//        // 查询销售价格基本信息
//        ErpSalePriceDO salePrice = erpSalePriceMapper.selectById(id);
//        if (salePrice == null) {
//            return null;
//        }
//
//        // 组装响应对象
//        ErpSalePriceRespVO respVO = BeanUtils.toBean(salePrice, ErpSalePriceRespVO.class);
//
//        // 如果需要关联其他数据，可以在这里查询并设置
//        // 例如：respVO.setComboList(...);
//
//        return respVO;
//    }
@Override
public ErpSalePriceRespVO getSalePriceWithItems(Long id) {
    // 查询销售价格基本信息
    ErpSalePriceDO salePrice = erpSalePriceMapper.selectById(id);
    if (salePrice == null) {
        return null;
    }

    // 组装响应对象
    ErpSalePriceRespVO respVO = BeanUtils.toBean(salePrice, ErpSalePriceRespVO.class);

    // 根据 groupProductId 查询组合产品信息
    if (salePrice.getGroupProductId() != null) {
        // 调用组合产品服务层查询组合产品信息
        ErpComboRespVO comboRespVO = erpComboProductService.getComboWithItems(salePrice.getGroupProductId());
        if (comboRespVO != null) {
            // 将组合产品信息赋值给 comboList
            respVO.setComboList(Collections.singletonList(comboRespVO));
        }
    }

    return respVO;
}

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOListByGroupProductId(Long groupProductId) {
//        List<ErpSalePriceDO> list = erpSalePriceMapper.selectListByGroupProductId(groupProductId);
//        return BeanUtils.toBean(list, ErpSalePriceRespVO.class);
        return  null;
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOListByCustomerName(String customerName) {
//        List<ErpSalePriceDO> list = erpSalePriceMapper.selectListByCustomerName(customerName);
//        return BeanUtils.toBean(list, ErpSalePriceRespVO.class);
        return  null;
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOListByStatus(Integer status) {
//        List<ErpSalePriceDO> list = erpSalePriceMapper.selectListByStatus(status);
//        return BeanUtils.toBean(list, ErpSalePriceRespVO.class);
        return null;
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOListByComboStatus() {
        // 1. 查询所有销售价格记录
        List<ErpSalePriceDO> salePrices = erpSalePriceMapper.selectList();

        // 2. 过滤出组合品状态符合条件的记录
        return salePrices.stream()
                .filter(salePrice -> {
                    ErpComboProductDO comboProduct = erpComboMapper.selectById(salePrice.getGroupProductId());
                    return comboProduct != null && comboProduct.getStatus().equals(CommonStatusEnum.ENABLE.getStatus());
                })
                .map(salePrice -> BeanUtils.toBean(salePrice, ErpSalePriceRespVO.class))
                .collect(Collectors.toList());
    }
}
