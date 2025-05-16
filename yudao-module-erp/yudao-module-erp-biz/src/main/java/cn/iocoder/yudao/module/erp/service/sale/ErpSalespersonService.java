package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalespersonDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ERP 销售人员 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpSalespersonService {

    /**
     * 创建销售人员
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSalesperson(@Valid ErpSalespersonSaveReqVO createReqVO);

    /**
     * 更新销售人员
     *
     * @param updateReqVO 更新信息
     */
    void updateSalesperson(@Valid ErpSalespersonSaveReqVO updateReqVO);

    /**
     * 删除销售人员
     *
     * @param ids 编号数组
     */
    void deleteSalesperson(List<Long> ids);

    /**
     * 获得销售人员
     *
     * @param id 编号
     * @return 销售人员
     */
    ErpSalespersonDO getSalesperson(Long id);

    /**
     * 获得销售人员列表
     *
     * @param ids 编号数组
     * @return 销售人员列表
     */
    List<ErpSalespersonDO> getSalespersonList(Collection<Long> ids);

    /**
     * 校验销售人员是否有效
     *
     * @param id 编号
     * @return 销售人员
     */
    ErpSalespersonDO validateSalesperson(Long id);

    /**
     * 获得销售人员 VO 列表
     *
     * @param ids 编号数组
     * @return 销售人员 VO 列表
     */
    List<ErpSalespersonRespVO> getSalespersonVOList(Collection<Long> ids);

    /**
     * 获得销售人员 VO Map
     *
     * @param ids 编号数组
     * @return 销售人员 VO Map
     */
    Map<Long, ErpSalespersonRespVO> getSalespersonVOMap(Collection<Long> ids);

    /**
     * 获得销售人员分页
     *
     * @param pageReqVO 分页查询
     * @return 销售人员分页
     */
    PageResult<ErpSalespersonRespVO> getSalespersonVOPage(ErpSalespersonPageReqVO pageReqVO);

    /**
     * 搜索销售人员
     *
     * @param searchReqVO 搜索条件
     * @return 销售人员列表
     */
    List<ErpSalespersonRespVO> searchSalespersons(ErpSalespersonPageReqVO searchReqVO);
}
