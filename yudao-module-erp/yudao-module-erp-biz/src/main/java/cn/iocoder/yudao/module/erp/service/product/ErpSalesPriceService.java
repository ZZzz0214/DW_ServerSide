package cn.iocoder.yudao.module.erp.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.price.ErpSalesPricePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.price.ErpSalesPriceRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.price.ErpSalesPriceSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpSalesPriceDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 销售价格 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpSalesPriceService {

    /**
     * 创建销售价格
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPrice(@Valid ErpSalesPriceSaveReqVO createReqVO);

    /**
     * 更新销售价格
     *
     * @param updateReqVO 更新信息
     */
    void updatePrice(@Valid ErpSalesPriceSaveReqVO updateReqVO);

    /**
     * 删除销售价格
     *
     * @param id 编号
     */
    void deletePrice(Long id);

    /**
     * 获得销售价格
     *
     * @param id 编号
     * @return 销售价格
     */
    ErpSalesPriceDO getPrice(Long id);

    /**
     * 获得销售价格列表
     *
     * @param ids 编号数组
     * @return 销售价格列表
     */
    List<ErpSalesPriceDO> getPriceList(Collection<Long> ids);

    /**
     * 获得销售价格分页
     *
     * @param pageReqVO 分页查询
     * @return 销售价格分页
     */
    PageResult<ErpSalesPriceRespVO> getSalesPricePage(ErpSalesPricePageReqVO pageReqVO);

    /**
     * 校验销售价格的有效性
     *
     * @param ids 编号数组
     * @return 销售价格列表
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException 如果销售价格不存在
     */
    List<ErpSalesPriceDO> validSalesPriceList(Collection<Long> ids);

    /**
     * 获得销售价格 Map
     *
     * @param ids 编号数组
     * @return 销售价格 Map
     */
    default Map<Long, ErpSalesPriceDO> getSalesPriceMap(Collection<Long> ids) {
        return convertMap(getPriceList(ids), ErpSalesPriceDO::getId);
    }
}
