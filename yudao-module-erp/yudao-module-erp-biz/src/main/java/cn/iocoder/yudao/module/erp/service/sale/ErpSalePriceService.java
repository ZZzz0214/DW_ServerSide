package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSearchReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceDO;

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
     * @param ids 编号数组
     */
    void deleteSalePrice(List<Long> ids);

    /**
     * 校验销售价格的有效性
     *
     * @param ids 编号数组
     * @return 销售价格列表
     */
    List<ErpSalePriceDO> validSalePriceList(Collection<Long> ids);

    /**
     * 获取销售价格
     *
     * @param id 编号
     * @return 销售价格
     */
    ErpSalePriceDO getSalePrice(Long id);

    /**
     * 获取销售价格 VO 列表
     *
     * @param ids 编号数组
     * @return 销售价格 VO 列表
     */
    List<ErpSalePriceRespVO> getSalePriceVOList(Collection<Long> ids);

    /**
     * 获取销售价格 VO Map
     *
     * @param ids 编号数组
     * @return 销售价格 VO Map
     */
    default Map<Long, ErpSalePriceRespVO> getSalePriceVOMap(Collection<Long> ids) {
        return convertMap(getSalePriceVOList(ids), ErpSalePriceRespVO::getId);
    }

    /**
     * 获取销售价格 VO 分页
     *
     * @param pageReqVO 分页查询
     * @return 销售价格分页
     */
    PageResult<ErpSalePriceRespVO> getSalePriceVOPage(ErpSalePricePageReqVO pageReqVO);

    /**
     * 通过组品 ID 查询组品及其关联的单品数据
     *
     * @param id 组品 ID
     * @return 包含组品和单品数据的响应对象
     */
    ErpSalePriceRespVO getSalePriceWithItems(Long id);

    /**
     * 获取销售价格 VO 列表（通过组品编号）
     *
     * @param groupProductId 组品编号
     * @return 销售价格 VO 列表
     */
    List<ErpSalePriceRespVO> getSalePriceVOListByGroupProductId(Long groupProductId);

    /**
     * 获取销售价格 VO 列表（通过客户名称）
     *
     * @param customerName 客户名称
     * @return 销售价格 VO 列表
     */
    List<ErpSalePriceRespVO> getSalePriceVOListByCustomerName(String customerName);

    /**
     * 获取销售价格 VO 列表（按状态筛选）
     *
     * @param status 状态
     * @return 销售价格 VO 列表
     */
    List<ErpSalePriceRespVO> getSalePriceVOListByStatus(Integer status);

    /**
     * 获取销售价格列表（通过组合品状态筛选）
     *
     * @return 销售价格列表
     */
    List<ErpSalePriceRespVO> getSalePriceVOListByComboStatus();
    /**
     * 搜索产品
     *
     * @param searchReqVO 搜索请求参数
     * @return 搜索到的产品列表
     */
    List<ErpSalePriceRespVO> searchProducts(ErpSalePriceSearchReqVO searchReqVO);

    /**
     * 根据组品编号和客户名称获取销售价格
     *
     * @param groupProductId 组品编号
     * @param customerName 客户名称
     * @return 销售价格信息
     */
    ErpSalePriceRespVO getSalePriceByGroupProductIdAndCustomerName(Long groupProductId, String customerName);

    /**
     * 获取缺失价格的销售记录
     */
    List<ErpSalePriceRespVO> getMissingPrices();

    /**
     * 获取代发缺失价格记录
     *
     * @param pageReqVO 分页查询参数
     * @return 代发缺失价格记录分页
     */
    PageResult<ErpDistributionMissingPriceVO> getDistributionMissingPrices(ErpSalePricePageReqVO pageReqVO);

    /**
     * 获取批发缺失价格记录
     *
     * @param pageReqVO 分页查询参数
     * @return 批发缺失价格记录分页
     */
    PageResult<ErpWholesaleMissingPriceVO> getWholesaleMissingPrices(ErpSalePricePageReqVO pageReqVO);

    /**
     * 批量设置代发价格
     *
     * @param reqList 价格设置请求列表
     */
    void batchSetDistributionPrices(List<ErpDistributionPriceSetReqVO> reqList);

    /**
     * 批量设置批发价格
     *
     * @param reqList 价格设置请求列表
     */
    void batchSetWholesalePrices(List<ErpWholesalePriceSetReqVO> reqList);

    /**
     * 导入销售价格
     *
     * @param importList 导入数据列表
     * @param isUpdateSupport 是否支持更新
     * @return 导入结果
     */
    ErpSalePriceImportRespVO importSalePriceList(List<ErpSalePriceImportExcelVO> importList, boolean isUpdateSupport);

    /**
     * 清理缓存
     */
    void clearCache();

    /**
     * 手动全量同步数据到ES
     */
    void manualFullSyncToES();

    /**
     * 获取统一缺失价格记录（代发+批发）
     *
     * @param pageReqVO 分页查询参数
     * @return 统一缺失价格记录分页
     */
    PageResult<ErpCombinedMissingPriceVO> getCombinedMissingPrices(ErpSalePricePageReqVO pageReqVO);

    /**
     * 批量设置统一价格（代发+批发）
     *
     * @param reqList 价格设置请求列表
     */
    void batchSetCombinedPrices(List<ErpCombinedPriceSetReqVO> reqList);
}
