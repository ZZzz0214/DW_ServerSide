package cn.iocoder.yudao.module.erp.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.ErpComboImport.ErpComboImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.ErpComboImport.ErpComboImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboProductCreateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboSearchReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 组合产品 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpComboProductService {

    /**
     * 创建组合产品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCombo(@Valid ErpComboSaveReqVO createReqVO);

    /**
     * 更新组合产品
     *
     * @param updateReqVO 更新信息
     */
    void updateCombo(@Valid ErpComboSaveReqVO updateReqVO);

    /**
     * 删除组合产品
     *
     * @param id 编号
     */
    void deleteCombo(Long id);

    /**
     * 校验组合产品们的有效性
     *
     * @param ids 编号数组
     * @return 组合产品列表
     */
    List<ErpComboProductDO> validComboList(Collection<Long> ids);

    /**
     * 获得组合产品
     *
     * @param id 编号
     * @return 组合产品
     */
    ErpComboProductDO getCombo(Long id);

    /**
     * 获得组合产品 VO 列表
     *
     * @param ids 编号数组
     * @return 组合产品 VO 列表
     */
    List<ErpComboRespVO> getComboVOList(Collection<Long> ids);

    /**
     * 获得组合产品 VO Map
     *
     * @param ids 编号数组
     * @return 组合产品 VO Map
     */
    default Map<Long, ErpComboRespVO> getComboVOMap(Collection<Long> ids) {
        return convertMap(getComboVOList(ids), ErpComboRespVO::getId);
    }

    /**
     * 获得组合产品 VO 分页
     *
     * @param pageReqVO 分页查询
     * @return 组合产品分页
     */
    PageResult<ErpComboRespVO> getComboVOPage(ErpComboPageReqVO pageReqVO);

    /**
     * 根据状态获取组合产品 VO 列表
     *
     * @param status 状态
     * @return 组合产品 VO 列表
     */
    List<ErpComboRespVO> getComboProductVOListByStatus(Integer status);

    /**
     * 新增组品和单品关联
     *
     * @param comboProduct 组品信息
     */
    void createComboWithItems(ErpComboProductCreateReqVO comboProduct);

    /**
     * 通过组品 ID 查询组品及其关联的单品数据
     *
     * @param id 组品 ID
     * @return 包含组品和单品数据的响应对象
     */
    ErpComboRespVO getComboWithItems(Long id);

    /**
     * 搜索功能
     *
     * @param searchReqVO 搜索结果请求
     * @return 搜索结果列表
     */
    List<ErpComboRespVO> searchCombos(ErpComboSearchReqVO searchReqVO);


    /**
     * 批量导入组合产品
     *
     * @param importCombos     导入组合产品列表
     * @param isUpdateSupport 是否支持更新
     * @return 导入结果
     */
    ErpComboImportRespVO importComboList(List<ErpComboImportExcelVO> importCombos, boolean isUpdateSupport);

    /**
     * 手动同步单个组合产品到ES（包括主表和关联项）
     *
     * @param comboId 组合产品ID
     */
    void manualSyncComboToES(Long comboId);

    /**
     * 手动全量同步所有组合产品到ES
     */
    void fullSyncToES();

}
