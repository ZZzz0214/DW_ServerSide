package cn.iocoder.yudao.module.erp.dal.mysql.wholesale;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;

import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesalePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesaleRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesalePurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleSaleDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpWholesaleMapper extends BaseMapperX<ErpWholesaleBaseDO> {

    default PageResult<ErpWholesaleRespVO> selectPage(ErpWholesalePageReqVO reqVO) {
        MPJLambdaWrapperX<ErpWholesaleBaseDO> query = new MPJLambdaWrapperX<ErpWholesaleBaseDO>()
                .likeIfPresent(ErpWholesaleBaseDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpWholesaleBaseDO::getReceiverName, reqVO.getReceiverName())
                .betweenIfPresent(ErpWholesaleBaseDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpWholesaleBaseDO::getId);

        // 联表查询采购信息
        query.leftJoin(ErpWholesalePurchaseDO.class, ErpWholesalePurchaseDO::getBaseId, ErpWholesaleBaseDO::getId)
                .selectAs(ErpWholesalePurchaseDO::getPurchaser, ErpWholesaleRespVO::getPurchaser)
                .selectAs(ErpWholesalePurchaseDO::getSupplier, ErpWholesaleRespVO::getSupplier)
                .selectAs(ErpWholesalePurchaseDO::getPurchasePrice, ErpWholesaleRespVO::getPurchasePrice)
                .selectAs(ErpWholesalePurchaseDO::getTruckFee, ErpWholesaleRespVO::getTruckFee)
                .selectAs(ErpWholesalePurchaseDO::getLogisticsFee, ErpWholesaleRespVO::getLogisticsFee)
                .selectAs(ErpWholesalePurchaseDO::getOtherFees, ErpWholesaleRespVO::getOtherFees)
                .selectAs(ErpWholesalePurchaseDO::getTotalPurchaseAmount, ErpWholesaleRespVO::getTotalPurchaseAmount);

        // 联表查询销售信息
        query.leftJoin(ErpWholesaleSaleDO.class, ErpWholesaleSaleDO::getBaseId, ErpWholesaleBaseDO::getId)
                .selectAs(ErpWholesaleSaleDO::getSalesperson, ErpWholesaleRespVO::getSalesperson)
                .selectAs(ErpWholesaleSaleDO::getCustomerName, ErpWholesaleRespVO::getCustomerName)
                .selectAs(ErpWholesaleSaleDO::getSalePrice, ErpWholesaleRespVO::getSalePrice)
                .selectAs(ErpWholesaleSaleDO::getTruckFee, ErpWholesaleRespVO::getTruckFee)
                .selectAs(ErpWholesaleSaleDO::getLogisticsFee, ErpWholesaleRespVO::getLogisticsFee)
                .selectAs(ErpWholesaleSaleDO::getOtherFees, ErpWholesaleRespVO::getOtherFees)
                .selectAs(ErpWholesaleSaleDO::getTotalSaleAmount, ErpWholesaleRespVO::getTotalSaleAmount);

        return selectJoinPage(reqVO, ErpWholesaleRespVO.class, query);
    }

    default ErpWholesaleBaseDO selectByNo(String no) {
        return selectOne(ErpWholesaleBaseDO::getNo, no);
    }
}
