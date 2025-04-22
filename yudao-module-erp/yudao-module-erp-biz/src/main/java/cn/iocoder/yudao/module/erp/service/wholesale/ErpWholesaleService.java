package cn.iocoder.yudao.module.erp.service.wholesale;



import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesalePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesaleRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesaleSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleBaseDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpWholesaleService {

    // 新增
    Long createWholesale(@Valid ErpWholesaleSaveReqVO createReqVO);

    // 修改
    void updateWholesale(@Valid ErpWholesaleSaveReqVO updateReqVO);

    // 删除
    void deleteWholesale(List<Long> ids);

    // 根据id查询
    ErpWholesaleBaseDO getWholesale(Long id);

    // 根据id列表查询
    List<ErpWholesaleBaseDO> getWholesaleList(Collection<Long> ids);

    // 校验有效性
    ErpWholesaleBaseDO validateWholesale(Long id);

    // 获取 VO 列表
    List<ErpWholesaleRespVO> getWholesaleVOList(Collection<Long> ids);

    // 获取 VO Map
    Map<Long, ErpWholesaleRespVO> getWholesaleVOMap(Collection<Long> ids);

    // 获取 VO 分页
    PageResult<ErpWholesaleRespVO> getWholesaleVOPage(ErpWholesalePageReqVO pageReqVO);
}