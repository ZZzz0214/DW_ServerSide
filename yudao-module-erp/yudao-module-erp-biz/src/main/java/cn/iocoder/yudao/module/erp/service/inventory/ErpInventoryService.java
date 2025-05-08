package cn.iocoder.yudao.module.erp.service.inventory;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventoryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventoryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventorySaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.inventory.ErpInventoryDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpInventoryService {

    // 新增库存
    Long createInventory(@Valid ErpInventorySaveReqVO createReqVO);

    // 修改库存
    void updateInventory(@Valid ErpInventorySaveReqVO updateReqVO);

    // 删除库存
    void deleteInventory(List<Long> ids);

    // 根据id查询库存
    ErpInventoryDO getInventory(Long id);

    // 根据id列表查询库存
    List<ErpInventoryDO> getInventoryList(Collection<Long> ids);

    // 校验库存有效性
    ErpInventoryDO validateInventory(Long id);

    // 获取库存VO列表
    List<ErpInventoryRespVO> getInventoryVOList(Collection<Long> ids);

    // 获取库存VO Map
    Map<Long, ErpInventoryRespVO> getInventoryVOMap(Collection<Long> ids);

    // 获取库存VO分页
    PageResult<ErpInventoryRespVO> getInventoryVOPage(ErpInventoryPageReqVO pageReqVO);
}