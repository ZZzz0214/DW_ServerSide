package cn.iocoder.yudao.module.erp.dal.mysql.sale;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.wholesaleorder.ErpWholesaleSaleOrderPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_sale.ErpWholesaleSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale_sale.ErpWholesaleSaleOrderItemDO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Objects;

/**
 * ERP 批发销售订单 Mapper
 */
@Mapper
public interface ErpWholesaleSaleOrderMapper extends BaseMapperX<ErpWholesaleSaleOrderDO> {

    default PageResult<ErpWholesaleSaleOrderDO> selectPage(ErpWholesaleSaleOrderPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpWholesaleSaleOrderDO> query = new MPJLambdaWrapperX<ErpWholesaleSaleOrderDO>()
                .likeIfPresent(ErpWholesaleSaleOrderDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpWholesaleSaleOrderDO::getOrderNumber, reqVO.getOrderNumber())
                .eqIfPresent(ErpWholesaleSaleOrderDO::getCustomerId, reqVO.getCustomerId())
                .betweenIfPresent(ErpWholesaleSaleOrderDO::getOrderTime, reqVO.getOrderTime())
                .eqIfPresent(ErpWholesaleSaleOrderDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpWholesaleSaleOrderDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ErpWholesaleSaleOrderDO::getCreator, reqVO.getCreator())
                .orderByDesc(ErpWholesaleSaleOrderDO::getId);

        // 出库状态
        if (Objects.equals(reqVO.getOutStatus(), ErpWholesaleSaleOrderPageReqVO.OUT_STATUS_NONE)) {
            query.eq(ErpWholesaleSaleOrderDO::getOutCount, 0);
        } else if (Objects.equals(reqVO.getOutStatus(), ErpWholesaleSaleOrderPageReqVO.OUT_STATUS_PART)) {
            query.gt(ErpWholesaleSaleOrderDO::getOutCount, 0).apply("t.out_count < t.total_count");
        } else if (Objects.equals(reqVO.getOutStatus(), ErpWholesaleSaleOrderPageReqVO.OUT_STATUS_ALL)) {
            query.apply("t.out_count = t.total_count");
        }

        // 退货状态
        if (Objects.equals(reqVO.getReturnStatus(), ErpWholesaleSaleOrderPageReqVO.RETURN_STATUS_NONE)) {
            query.eq(ErpWholesaleSaleOrderDO::getReturnCount, 0);
        } else if (Objects.equals(reqVO.getReturnStatus(), ErpWholesaleSaleOrderPageReqVO.RETURN_STATUS_PART)) {
            query.gt(ErpWholesaleSaleOrderDO::getReturnCount, 0).apply("t.return_count < t.total_count");
        } else if (Objects.equals(reqVO.getReturnStatus(), ErpWholesaleSaleOrderPageReqVO.RETURN_STATUS_ALL)) {
            query.apply("t.return_count = t.total_count");
        }

        // 可销售出库
        if (Boolean.TRUE.equals(reqVO.getOutEnable())) {
            query.eq(ErpWholesaleSaleOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.out_count < t.total_count");
        }

        // 可销售退货
        if (Boolean.TRUE.equals(reqVO.getReturnEnable())) {
            query.eq(ErpWholesaleSaleOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.return_count < t.out_count");
        }

        if (reqVO.getProductId() != null) {
            query.leftJoin(ErpWholesaleSaleOrderItemDO.class, ErpWholesaleSaleOrderItemDO::getOrderId, ErpWholesaleSaleOrderDO::getId)
                    .eq(reqVO.getProductId() != null, ErpWholesaleSaleOrderItemDO::getProductId, reqVO.getProductId())
                    .groupBy(ErpWholesaleSaleOrderDO::getId);
        }

        return selectJoinPage(reqVO, ErpWholesaleSaleOrderDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpWholesaleSaleOrderDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpWholesaleSaleOrderDO>()
                .eq(ErpWholesaleSaleOrderDO::getId, id)
                .eq(ErpWholesaleSaleOrderDO::getStatus, status));
    }

    default ErpWholesaleSaleOrderDO selectByNo(String no) {
        return selectOne(ErpWholesaleSaleOrderDO::getNo, no);
    }
}
