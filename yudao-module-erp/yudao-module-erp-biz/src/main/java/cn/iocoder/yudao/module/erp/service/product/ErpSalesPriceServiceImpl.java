package cn.iocoder.yudao.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.price.ErpSalesPricePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.price.ErpSalesPriceRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.price.ErpSalesPriceSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpSalesPriceDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpSalesPriceMapper;
import cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;

/**
 * ERP 销售价格 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpSalesPriceServiceImpl implements ErpSalesPriceService {

    @Resource
    private ErpSalesPriceMapper salesPriceMapper;
    @Resource
    private ErpProductService productService;

    @Override
    public Long createPrice(ErpSalesPriceSaveReqVO createReqVO) {
        // 校验参数
        validateSalesPriceForCreateOrUpdate(null, createReqVO);
        // 插入
        ErpSalesPriceDO salesPrice = BeanUtils.toBean(createReqVO, ErpSalesPriceDO.class);
        salesPriceMapper.insert(salesPrice);
        // 返回
        return salesPrice.getId();
    }

    @Override
    public void updatePrice(ErpSalesPriceSaveReqVO updateReqVO) {
        // 校验存在
        validateSalesPriceExists(updateReqVO.getId());
        // 校验参数
        validateSalesPriceForCreateOrUpdate(updateReqVO.getId(), updateReqVO);
        // 更新
        ErpSalesPriceDO updateObj = BeanUtils.toBean(updateReqVO, ErpSalesPriceDO.class);
        salesPriceMapper.updateById(updateObj);
    }

    @Override
    public void deletePrice(Long id) {
        // 校验存在
        validateSalesPriceExists(id);
        // 删除
        salesPriceMapper.deleteById(id);
    }

    private void validateSalesPriceExists(Long id) {
        if (salesPriceMapper.selectById(id) == null) {
            throw new ServiceException(PRODUCT_NOT_EXISTS);
        }
    }

    private void validateSalesPriceForCreateOrUpdate(Long id, ErpSalesPriceSaveReqVO reqVO) {
        // 校验产品是否存在
        if (reqVO.getProductId() != null) {
            productService.validProductList(Collections.singleton(reqVO.getProductId()));
        }
        // 校验组合产品是否存在
        if (reqVO.getComboProductId() != null) {
            productService.validProductList(Collections.singleton(reqVO.getComboProductId()));
        }
        // 其他业务校验...
    }

    @Override
    public List<ErpSalesPriceDO> validSalesPriceList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpSalesPriceDO> list = salesPriceMapper.selectBatchIds(ids);
        if (list.size() != ids.size()) {
            throw new ServiceException(PRODUCT_NOT_EXISTS);
        }
        return list;
    }

    @Override
    public ErpSalesPriceDO getPrice(Long id) {
        return salesPriceMapper.selectById(id);
    }

    @Override
    public List<ErpSalesPriceDO> getPriceList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return salesPriceMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ErpSalesPriceRespVO> getSalesPricePage(ErpSalesPricePageReqVO pageReqVO) {
        PageResult<ErpSalesPriceDO> pageResult = salesPriceMapper.selectPage(pageReqVO);
        List<ErpSalesPriceRespVO> voList = BeanUtils.toBean(pageResult.getList(), ErpSalesPriceRespVO.class);
        return new PageResult<>(voList, pageResult.getTotal());
    }
}
