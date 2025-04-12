package cn.iocoder.yudao.module.erp.dal.mysql.purchase;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.wholesale_purchase.ErpWholesalePurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_purchase.ErpWholesalePurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_purchase.ErpWholesalePurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Objects;

@Mapper
public interface ErpWholesalePurchaseOrderMapper extends BaseMapperX<ErpWholesalePurchaseOrderDO> {

    default PageResult<ErpWholesalePurchaseOrderDO> selectPage(ErpWholesalePurchaseOrderPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpWholesalePurchaseOrderDO> query = new MPJLambdaWrapperX<ErpWholesalePurchaseOrderDO>()
                .likeIfPresent(ErpWholesalePurchaseOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpWholesalePurchaseOrderDO::getSupplierId, reqVO.getSupplierId())
//                .betweenIfPresent(ErpWholesalePurchaseOrderDO::getOrderTime, reqVO.getOrderTime())
                .eqIfPresent(ErpWholesalePurchaseOrderDO::getStatus, reqVO.getStatus())
//                .likeIfPresent(ErpWholesalePurchaseOrderDO::getRemark, reqVO.getRemark())
//                .eqIfPresent(ErpWholesalePurchaseOrderDO::getCreator, reqVO.getCreator())
                .orderByDesc(ErpWholesalePurchaseOrderDO::getId);

        // 入库状态
        if (Objects.equals(reqVO.getInStatus(), ErpWholesalePurchaseOrderPageReqVO.IN_STATUS_NONE)) {
            query.eq(ErpWholesalePurchaseOrderDO::getInCount, 0);
        } else if (Objects.equals(reqVO.getInStatus(), ErpWholesalePurchaseOrderPageReqVO.IN_STATUS_PART)) {
            query.gt(ErpWholesalePurchaseOrderDO::getInCount, 0).apply("t.in_count < t.total_count");
        } else if (Objects.equals(reqVO.getInStatus(), ErpWholesalePurchaseOrderPageReqVO.IN_STATUS_ALL)) {
            query.apply("t.in_count = t.total_count");
        }

        // 退货状态
        if (Objects.equals(reqVO.getReturnStatus(), ErpWholesalePurchaseOrderPageReqVO.RETURN_STATUS_NONE)) {
            query.eq(ErpWholesalePurchaseOrderDO::getReturnCount, 0);
        } else if (Objects.equals(reqVO.getReturnStatus(), ErpWholesalePurchaseOrderPageReqVO.RETURN_STATUS_PART)) {
            query.gt(ErpWholesalePurchaseOrderDO::getReturnCount, 0).apply("t.return_count < t.total_count");
        } else if (Objects.equals(reqVO.getReturnStatus(), ErpWholesalePurchaseOrderPageReqVO.RETURN_STATUS_ALL)) {
            query.apply("t.return_count = t.total_count");
        }

        // 可采购入库
        if (Boolean.TRUE.equals(reqVO.getInEnable())) {
            query.eq(ErpWholesalePurchaseOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.in_count < t.total_count");
        }

        // 可采购退货
        if (Boolean.TRUE.equals(reqVO.getReturnEnable())) {
            query.eq(ErpWholesalePurchaseOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.return_count < t.in_count");
        }

        if (reqVO.getProductId() != null) {
            query.leftJoin(ErpWholesalePurchaseOrderItemDO.class, ErpWholesalePurchaseOrderItemDO::getOrderId, ErpWholesalePurchaseOrderDO::getId)
                    .eq(reqVO.getProductId() != null, ErpWholesalePurchaseOrderItemDO::getProductId, reqVO.getProductId())
                    .groupBy(ErpWholesalePurchaseOrderDO::getId);
        }
        return selectJoinPage(reqVO, ErpWholesalePurchaseOrderDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpWholesalePurchaseOrderDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpWholesalePurchaseOrderDO>()
                .eq(ErpWholesalePurchaseOrderDO::getId, id)
                .eq(ErpWholesalePurchaseOrderDO::getStatus, status));
    }

    default ErpWholesalePurchaseOrderDO selectByNo(String no) {
        return selectOne(ErpWholesalePurchaseOrderDO::getNo, no);
    }
}
