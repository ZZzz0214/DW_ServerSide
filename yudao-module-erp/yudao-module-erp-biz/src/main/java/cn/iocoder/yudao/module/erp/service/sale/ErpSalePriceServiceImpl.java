package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePricePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSalePriceMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * ERP 销售价格 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpSalePriceServiceImpl implements ErpSalePriceService {

    @Resource
    private ErpSalePriceMapper erpSalePriceMapper;
    @Resource
    private ErpComboProductService comboService;

    @Override
    public Long createSalePrice(ErpSalePriceSaveReqVO createReqVO) {
        // 校验请求参数
        validateSalePrice(createReqVO);
        // 插入
        ErpSalePriceDO salePrice = BeanUtils.toBean(createReqVO, ErpSalePriceDO.class);
        erpSalePriceMapper.insert(salePrice);
        // 返回
        return salePrice.getId();
    }

    @Override
    public void updateSalePrice(ErpSalePriceSaveReqVO updateReqVO) {
        // 校验存在
        validateSalePriceExists(updateReqVO.getId());
        // 校验请求参数
        validateSalePrice(updateReqVO);
        // 更新
        ErpSalePriceDO updateObj = BeanUtils.toBean(updateReqVO, ErpSalePriceDO.class);
        erpSalePriceMapper.updateById(updateObj);
    }

    @Override
    public void deleteSalePrice(Long id) {
        // 校验存在
        validateSalePriceExists(id);
        // 删除
        erpSalePriceMapper.deleteById(id);
    }

    @Override
    public List<ErpSalePriceDO> validSalePriceList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpSalePriceDO> list = erpSalePriceMapper.selectBatchIds(ids);
        // 校验是否存在
        for (Long id : ids) {
            if (!list.stream().anyMatch(item -> item.getId().equals(id))) {
                throw exception(SALE_PRICE_NOT_EXISTS);
            }
        }
        return list;
    }

    @Override
    public ErpSalePriceDO getSalePriceById(Long id) {
        return erpSalePriceMapper.selectById(id);
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpSalePriceDO> list = erpSalePriceMapper.selectBatchIds(ids);
        return buildSalePriceVOList(list);
    }

    @Override
    public PageResult<ErpSalePriceRespVO> getSalePriceVOPage(ErpSalePricePageReqVO pageReqVO) {
        PageResult<ErpSalePriceDO> pageResult = erpSalePriceMapper.selectPage(pageReqVO);
        return new PageResult<>(buildSalePriceVOList(pageResult.getList()), pageResult.getTotal());
    }

//    @Override
//    public List<ErpSalePriceRespVO> searchSalePrices(ErpSalePriceSearchReqVO searchReqVO) {
//        // 构造查询条件
//        LambdaQueryWrapper<ErpSalePriceDO> queryWrapper = new LambdaQueryWrapper<>();
//        if (searchReqVO.getGroupProductId() != null) {
//            queryWrapper.eq(ErpSalePriceDO::getGroupProductId, searchReqVO.getGroupProductId());
//        }
//        if (searchReqVO.getCustomerName() != null) {
//            queryWrapper.like(ErpSalePriceDO::getCustomerName, searchReqVO.getCustomerName());
//        }
//        if (searchReqVO.getDistributionPriceRange() != null && searchReqVO.getDistributionPriceRange().length == 2) {
//            queryWrapper.ge(ErpSalePriceDO::getDistributionPrice, searchReqVO.getDistributionPriceRange()[0])
//                    .le(ErpSalePriceDO::getDistributionPrice, searchReqVO.getDistributionPriceRange()[1]);
//        }
//        if (searchReqVO.getWholesalePriceRange() != null && searchReqVO.getWholesalePriceRange().length == 2) {
//            queryWrapper.ge(ErpSalePriceDO::getWholesalePrice, searchReqVO.getWholesalePriceRange()[0])
//                    .le(ErpSalePriceDO::getWholesalePrice, searchReqVO.getWholesalePriceRange()[1]);
//        }
//
//        // 执行查询
//        List<ErpSalePriceDO> salePriceList = erpSalePriceMapper.selectList(queryWrapper);
//        // 转换为响应对象
//        return convertList(salePriceList, salePrice -> BeanUtils.toBean(salePrice, ErpSalePriceRespVO.class));
//    }

    private List<ErpSalePriceRespVO> buildSalePriceVOList(List<ErpSalePriceDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return BeanUtils.toBean(list, ErpSalePriceRespVO.class);
    }

    private void validateSalePrice(ErpSalePriceSaveReqVO reqVO) {
        // 校验必填字段
        if (reqVO.getGroupProductId() == null) {
            throw exception(SALE_PRICE_GROUP_PRODUCT_ID_REQUIRED);
        }
        if (reqVO.getCustomerName() == null) {
            throw exception(SALE_PRICE_CUSTOMER_NAME_REQUIRED);
        }
        if (reqVO.getDistributionPrice() == null) {
            throw exception(SALE_PRICE_DISTRIBUTION_PRICE_REQUIRED);
        }
        if (reqVO.getWholesalePrice() == null) {
            throw exception(SALE_PRICE_WHOLESALE_PRICE_REQUIRED);
        }
        if (reqVO.getShippingFeeType() == null) {
            throw exception(SALE_PRICE_SHIPPING_FEE_TYPE_REQUIRED);
        }
    }

    private void validateSalePriceExists(Long id) {
        if (erpSalePriceMapper.selectById(id) == null) {
            throw exception(SALE_PRICE_NOT_EXISTS);
        }
    }
    @Override
    public ErpSalePriceRespVO getSalePriceWithCombo(Long id) {
        // 查询销售价格基本信息
        ErpSalePriceDO salePriceDO = erpSalePriceMapper.selectById(id);
        if (salePriceDO == null) {
            return null;
        }

        // 动态查询组品信息（假设通过 salePriceDO 的 id 查询组品）
        ErpComboRespVO combo = comboService.getComboWithItems(id); // 使用 salePriceDO 的 id 作为组品编号

        // 组装响应对象
        ErpSalePriceRespVO respVO = BeanUtils.toBean(salePriceDO, ErpSalePriceRespVO.class);
        respVO.setComboList(combo != null ? Collections.singletonList(combo) : Collections.emptyList());

        return respVO;
    }
}

