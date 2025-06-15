package cn.iocoder.yudao.module.erp.dal.mysql.inventory;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventoryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventoryRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.inventory.ErpInventoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpInventoryMapper extends BaseMapperX<ErpInventoryDO> {

    default PageResult<ErpInventoryRespVO> selectPage(ErpInventoryPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpInventoryDO> query = new MPJLambdaWrapperX<ErpInventoryDO>()
                .likeIfPresent(ErpInventoryDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpInventoryDO::getProductId, reqVO.getProductId())
                .betweenIfPresent(ErpInventoryDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpInventoryDO::getId)
                // 库存表字段映射
                .selectAs(ErpInventoryDO::getId, ErpInventoryRespVO::getId)
                .selectAs(ErpInventoryDO::getNo, ErpInventoryRespVO::getNo)
                .selectAs(ErpInventoryDO::getProductId, ErpInventoryRespVO::getProductId)
                .selectAs(ErpInventoryDO::getSpotInventory, ErpInventoryRespVO::getSpotInventory)
                .selectAs(ErpInventoryDO::getRemainingInventory, ErpInventoryRespVO::getRemainingInventory)
                .selectAs(ErpInventoryDO::getRemark, ErpInventoryRespVO::getRemark)
                .selectAs(ErpInventoryDO::getCreator, ErpInventoryRespVO::getCreator)
                .selectAs(ErpInventoryDO::getCreateTime, ErpInventoryRespVO::getCreateTime);

        // 联表查询产品信息
        query.leftJoin(ErpProductDO.class, ErpProductDO::getId, ErpInventoryDO::getProductId)
                .selectAs(ErpProductDO::getName, ErpInventoryRespVO::getProductName)
                .selectAs(ErpProductDO::getNo, ErpInventoryRespVO::getProductNo)
                .selectAs(ErpProductDO::getProductShortName, ErpInventoryRespVO::getProductShortName)
                .selectAs(ErpProductDO::getBrand, ErpInventoryRespVO::getBrand)
                .selectAs(ErpProductDO::getCategoryId, ErpInventoryRespVO::getCategory);

        return selectJoinPage(reqVO, ErpInventoryRespVO.class, query);
    }

    default ErpInventoryDO selectByNo(String no) {
        return selectOne(ErpInventoryDO::getNo, no);
    }

    default List<ErpInventoryDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpInventoryDO::getNo, nos);
    }

    default ErpInventoryRespVO selectVOById(Long id) {
        MPJLambdaWrapperX<ErpInventoryDO> query = new MPJLambdaWrapperX<ErpInventoryDO>()
                .eq(ErpInventoryDO::getId, id)
                // 库存表字段映射
                .selectAs(ErpInventoryDO::getId, ErpInventoryRespVO::getId)
                .selectAs(ErpInventoryDO::getNo, ErpInventoryRespVO::getNo)
                .selectAs(ErpInventoryDO::getProductId, ErpInventoryRespVO::getProductId)
                .selectAs(ErpInventoryDO::getSpotInventory, ErpInventoryRespVO::getSpotInventory)
                .selectAs(ErpInventoryDO::getRemainingInventory, ErpInventoryRespVO::getRemainingInventory)
                .selectAs(ErpInventoryDO::getRemark, ErpInventoryRespVO::getRemark)
                .selectAs(ErpInventoryDO::getCreator, ErpInventoryRespVO::getCreator)
                .selectAs(ErpInventoryDO::getCreateTime, ErpInventoryRespVO::getCreateTime);

        // 联表查询产品信息
        query.leftJoin(ErpProductDO.class, ErpProductDO::getId, ErpInventoryDO::getProductId)
                .selectAs(ErpProductDO::getNo, ErpInventoryRespVO::getProductNo)
                .selectAs(ErpProductDO::getName, ErpInventoryRespVO::getProductName)
                .selectAs(ErpProductDO::getProductShortName, ErpInventoryRespVO::getProductShortName)
                .selectAs(ErpProductDO::getBrand, ErpInventoryRespVO::getBrand)
                .selectAs(ErpProductDO::getCategoryId, ErpInventoryRespVO::getCategory);

        return selectJoinOne(ErpInventoryRespVO.class, query);
    }

    default List<ErpInventoryRespVO> selectVOListByIds(Collection<Long> ids) {
        MPJLambdaWrapperX<ErpInventoryDO> query = new MPJLambdaWrapperX<ErpInventoryDO>()
                .in(ErpInventoryDO::getId, ids)
                .orderByDesc(ErpInventoryDO::getId)
                // 库存表字段映射
                .selectAs(ErpInventoryDO::getId, ErpInventoryRespVO::getId)
                .selectAs(ErpInventoryDO::getNo, ErpInventoryRespVO::getNo)
                .selectAs(ErpInventoryDO::getProductId, ErpInventoryRespVO::getProductId)
                .selectAs(ErpInventoryDO::getSpotInventory, ErpInventoryRespVO::getSpotInventory)
                .selectAs(ErpInventoryDO::getRemainingInventory, ErpInventoryRespVO::getRemainingInventory)
                .selectAs(ErpInventoryDO::getRemark, ErpInventoryRespVO::getRemark)
                .selectAs(ErpInventoryDO::getCreator, ErpInventoryRespVO::getCreator)
                .selectAs(ErpInventoryDO::getCreateTime, ErpInventoryRespVO::getCreateTime);

        // 联表查询产品信息
        query.leftJoin(ErpProductDO.class, ErpProductDO::getId, ErpInventoryDO::getProductId)
                .selectAs(ErpProductDO::getNo, ErpInventoryRespVO::getProductNo)
                .selectAs(ErpProductDO::getName, ErpInventoryRespVO::getProductName)
                .selectAs(ErpProductDO::getProductShortName, ErpInventoryRespVO::getProductShortName)
                .selectAs(ErpProductDO::getBrand, ErpInventoryRespVO::getBrand)
                .selectAs(ErpProductDO::getCategoryId, ErpInventoryRespVO::getCategory);

        return selectJoinList(ErpInventoryRespVO.class, query);
    }
}
