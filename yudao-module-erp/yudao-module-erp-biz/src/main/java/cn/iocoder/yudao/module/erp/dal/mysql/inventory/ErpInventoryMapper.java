package cn.iocoder.yudao.module.erp.dal.mysql.inventory;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventoryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.inventory.vo.ErpInventoryRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.inventory.ErpInventoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import org.apache.ibatis.annotations.Mapper;

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
                .selectAs(ErpInventoryDO::getCreateTime, ErpInventoryRespVO::getCreateTime);

        // 联表查询产品信息
        query.leftJoin(ErpProductDO.class, ErpProductDO::getId, ErpInventoryDO::getProductId)
                .selectAs(ErpProductDO::getName, ErpInventoryRespVO::getProductName)
                .selectAs(ErpProductDO::getImage, ErpInventoryRespVO::getProductImage)
                .selectAs(ErpProductDO::getProductShortName, ErpInventoryRespVO::getProductShortName)
                .selectAs(ErpProductDO::getBrand, ErpInventoryRespVO::getBrand)
                .selectAs(ErpProductDO::getCategoryId, ErpInventoryRespVO::getCategory);

        return selectJoinPage(reqVO, ErpInventoryRespVO.class, query);
    }

    default ErpInventoryDO selectByNo(String no) {
        return selectOne(ErpInventoryDO::getNo, no);
    }
}