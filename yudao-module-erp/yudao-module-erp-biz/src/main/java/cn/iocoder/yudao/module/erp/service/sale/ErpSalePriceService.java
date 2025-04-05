package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePricePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ERP 销售价格 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpSalePriceService {

    /**
     * 创建销售价格
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSalePrice(@Valid ErpSalePriceSaveReqVO createReqVO);

    /**
     * 更新销售价格
     *
     * @param updateReqVO 更新信息
     */
    void updateSalePrice(@Valid ErpSalePriceSaveReqVO updateReqVO);

    /**
     * 删除销售价格
     *
     * @param id 编号
     */
    void deleteSalePrice(Long id);

    /**
     * 校验销售价格的有效性
     *
     * @param ids 编号数组
     * @return 销售价格列表
     */
    List<ErpSalePriceDO> validSalePriceList(Collection<Long> ids);

    /**
     * 获得销售价格
     *
     * @param id 编号
     * @return 销售价格
     */
    ErpSalePriceDO getSalePriceById(Long id);

    /**
     * 获得销售价格 VO 列表
     *
     * @param ids 编号数组
     * @return 销售价格 VO 列表
     */
    List<ErpSalePriceRespVO> getSalePriceVOList(Collection<Long> ids);

    /**
     * 获得销售价格 VO Map
     *
     * @param ids 编号数组
     * @return 销售价格 VO Map
     */
    default Map<Long, ErpSalePriceRespVO> getSalePriceVOMap(Collection<Long> ids) {
        return cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap(getSalePriceVOList(ids), ErpSalePriceRespVO::getId);
    }

    /**
     * 获得销售价格 VO 分页
     *
     * @param pageReqVO 分页查询
     * @return 销售价格分页
     */
    PageResult<ErpSalePriceRespVO> getSalePriceVOPage(ErpSalePricePageReqVO pageReqVO);

    /**
     * 搜索功能
     *
     * @param searchReqVO 搜索功能请求参数
     * @return 搜索结果列表
     */
//    List<ErpSalePriceRespVO> searchSalePrices(ErpSalePriceSearchReqVO searchReqVO);

    public ErpSalePriceRespVO getSalePriceWithCombo(Long id);
}
