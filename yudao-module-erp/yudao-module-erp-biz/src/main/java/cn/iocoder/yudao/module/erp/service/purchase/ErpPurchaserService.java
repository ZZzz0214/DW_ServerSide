package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaserDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ERP 采购人员 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpPurchaserService {

    /**
     * 创建采购人员
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchaser(@Valid ErpPurchaserSaveReqVO createReqVO);

    /**
     * 更新采购人员
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaser(@Valid ErpPurchaserSaveReqVO updateReqVO);

    /**
     * 删除采购人员
     *
     * @param ids 编号数组
     */
    void deletePurchaser(List<Long> ids);

    /**
     * 获得采购人员
     *
     * @param id 编号
     * @return 采购人员
     */
    ErpPurchaserDO getPurchaser(Long id);

    /**
     * 获得采购人员列表
     *
     * @param ids 编号数组
     * @return 采购人员列表
     */
    List<ErpPurchaserDO> getPurchaserList(Collection<Long> ids);

    /**
     * 校验采购人员是否有效
     *
     * @param id 编号
     * @return 采购人员
     */
    ErpPurchaserDO validatePurchaser(Long id);

    /**
     * 获得采购人员 VO 列表
     *
     * @param ids 编号数组
     * @return 采购人员 VO 列表
     */
    List<ErpPurchaserRespVO> getPurchaserVOList(Collection<Long> ids);

    /**
     * 获得采购人员 VO Map
     *
     * @param ids 编号数组
     * @return 采购人员 VO Map
     */
    Map<Long, ErpPurchaserRespVO> getPurchaserVOMap(Collection<Long> ids);

    /**
     * 获得采购人员分页
     *
     * @param pageReqVO 分页查询
     * @return 采购人员分页
     */
    PageResult<ErpPurchaserRespVO> getPurchaserVOPage(ErpPurchaserPageReqVO pageReqVO);

    /**
     * 搜索采购人员
     *
     * @param searchReqVO 搜索条件
     * @return 采购人员列表
     */
    List<ErpPurchaserRespVO> searchPurchasers(ErpPurchaserPageReqVO searchReqVO);
}