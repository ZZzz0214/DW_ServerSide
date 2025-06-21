package cn.iocoder.yudao.module.erp.dal.mysql.groupbuyingreview;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingReviewDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpGroupBuyingReviewMapper extends BaseMapperX<ErpGroupBuyingReviewDO> {

    default PageResult<ErpGroupBuyingReviewRespVO> selectPage(ErpGroupBuyingReviewPageReqVO reqVO, String currentUsername) {
        MPJLambdaWrapperX<ErpGroupBuyingReviewDO> query = new MPJLambdaWrapperX<ErpGroupBuyingReviewDO>()
                .likeIfPresent(ErpGroupBuyingReviewDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpGroupBuyingReviewDO::getSupplyGroupPrice, reqVO.getSupplyGroupPrice())
                .likeIfPresent(ErpGroupBuyingReviewDO::getExpressFee, reqVO.getExpressFee())
                .betweenIfPresent(ErpGroupBuyingReviewDO::getSampleSendDate, reqVO.getSampleSendDate())
                .betweenIfPresent(ErpGroupBuyingReviewDO::getGroupStartDate, reqVO.getGroupStartDate())
                .likeIfPresent(ErpGroupBuyingReviewDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(ErpGroupBuyingReviewDO::getCreateTime, reqVO.getCreateTime());
        
        // 权限控制：admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"admin".equals(currentUsername)) {
            query.eq(ErpGroupBuyingReviewDO::getCreator, currentUsername);
        }
        
        query.orderByDesc(ErpGroupBuyingReviewDO::getId)
                // 团购复盘表字段映射
                .selectAs(ErpGroupBuyingReviewDO::getId, ErpGroupBuyingReviewRespVO::getId)
                .selectAs(ErpGroupBuyingReviewDO::getNo, ErpGroupBuyingReviewRespVO::getNo)
                .selectAs(ErpGroupBuyingReviewDO::getRemark, ErpGroupBuyingReviewRespVO::getRemark)
                .selectAs(ErpGroupBuyingReviewDO::getSupplyGroupPrice, ErpGroupBuyingReviewRespVO::getSupplyGroupPrice)
                .selectAs(ErpGroupBuyingReviewDO::getExpressFee, ErpGroupBuyingReviewRespVO::getExpressFee)
                .selectAs(ErpGroupBuyingReviewDO::getSampleSendDate, ErpGroupBuyingReviewRespVO::getSampleSendDate)
                .selectAs(ErpGroupBuyingReviewDO::getGroupStartDate, ErpGroupBuyingReviewRespVO::getGroupStartDate)
                .selectAs(ErpGroupBuyingReviewDO::getGroupSales, ErpGroupBuyingReviewRespVO::getGroupSales)
                .selectAs(ErpGroupBuyingReviewDO::getRepeatGroupDate, ErpGroupBuyingReviewRespVO::getRepeatGroupDate)
                .selectAs(ErpGroupBuyingReviewDO::getRepeatGroupSales, ErpGroupBuyingReviewRespVO::getRepeatGroupSales)
                .selectAs(ErpGroupBuyingReviewDO::getGroupBuyingId, ErpGroupBuyingReviewRespVO::getGroupBuyingId)
                .selectAs(ErpGroupBuyingReviewDO::getCustomerId, ErpGroupBuyingReviewRespVO::getCustomerId)
                .selectAs(ErpGroupBuyingReviewDO::getCreator, ErpGroupBuyingReviewRespVO::getCreator)
                .selectAs(ErpGroupBuyingReviewDO::getCreateTime, ErpGroupBuyingReviewRespVO::getCreateTime);

        // 联表查询团购货盘信息
        query.leftJoin(ErpGroupBuyingDO.class, ErpGroupBuyingDO::getId, ErpGroupBuyingReviewDO::getGroupBuyingId);
        
        // 团购货盘搜索条件
        if (reqVO.getBrandName() != null && !reqVO.getBrandName().isEmpty()) {
            query.like(ErpGroupBuyingDO::getBrandName, reqVO.getBrandName());
        }
        if (reqVO.getProductName() != null && !reqVO.getProductName().isEmpty()) {
            query.like(ErpGroupBuyingDO::getProductName, reqVO.getProductName());
        }
        if (reqVO.getProductSpec() != null && !reqVO.getProductSpec().isEmpty()) {
            query.like(ErpGroupBuyingDO::getProductSpec, reqVO.getProductSpec());
        }
        if (reqVO.getProductSku() != null && !reqVO.getProductSku().isEmpty()) {
            query.like(ErpGroupBuyingDO::getProductSku, reqVO.getProductSku());
        }
        if (reqVO.getStatus() != null && !reqVO.getStatus().isEmpty()) {
            query.eq(ErpGroupBuyingDO::getStatus, reqVO.getStatus());
        }
        
        query.selectAs(ErpGroupBuyingDO::getBrandName, ErpGroupBuyingReviewRespVO::getBrandName)
                .selectAs(ErpGroupBuyingDO::getNo, ErpGroupBuyingReviewRespVO::getGroupBuyingNo)
                .selectAs(ErpGroupBuyingDO::getProductName, ErpGroupBuyingReviewRespVO::getProductName)
                .selectAs(ErpGroupBuyingDO::getProductSpec, ErpGroupBuyingReviewRespVO::getProductSpec)
                .selectAs(ErpGroupBuyingDO::getProductSku, ErpGroupBuyingReviewRespVO::getProductSku)
                .selectAs(ErpGroupBuyingDO::getSupplyGroupPrice, ErpGroupBuyingReviewRespVO::getSupplyGroupPrice)
                .selectAs(ErpGroupBuyingDO::getGroupMechanism, ErpGroupBuyingReviewRespVO::getGroupMechanism)
                .selectAs(ErpGroupBuyingDO::getStatus, ErpGroupBuyingReviewRespVO::getStatus);

        // 联表查询客户信息
        query.leftJoin(ErpCustomerDO.class, ErpCustomerDO::getId, ErpGroupBuyingReviewDO::getCustomerId);
        
        // 客户名称搜索条件
        if (reqVO.getCustomerName() != null && !reqVO.getCustomerName().isEmpty()) {
            query.like(ErpCustomerDO::getName, reqVO.getCustomerName());
        }
        
        query.selectAs(ErpCustomerDO::getName, ErpGroupBuyingReviewRespVO::getCustomerName);

        return selectJoinPage(reqVO, ErpGroupBuyingReviewRespVO.class, query);
    }

    default ErpGroupBuyingReviewDO selectByNo(String no) {
        return selectOne(ErpGroupBuyingReviewDO::getNo, no);
    }

    default List<ErpGroupBuyingReviewDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpGroupBuyingReviewDO::getNo, nos);
    }

    default List<ErpGroupBuyingReviewRespVO> selectListByIds(Collection<Long> ids) {
        if (cn.hutool.core.collection.CollUtil.isEmpty(ids)) {
            return java.util.Collections.emptyList();
        }

        MPJLambdaWrapperX<ErpGroupBuyingReviewDO> query = new MPJLambdaWrapperX<ErpGroupBuyingReviewDO>()
                .in(ErpGroupBuyingReviewDO::getId, ids)
                .orderByDesc(ErpGroupBuyingReviewDO::getId)
                // 团购复盘表字段映射
                .selectAs(ErpGroupBuyingReviewDO::getId, ErpGroupBuyingReviewRespVO::getId)
                .selectAs(ErpGroupBuyingReviewDO::getNo, ErpGroupBuyingReviewRespVO::getNo)
                .selectAs(ErpGroupBuyingReviewDO::getRemark, ErpGroupBuyingReviewRespVO::getRemark)
                .selectAs(ErpGroupBuyingReviewDO::getSupplyGroupPrice, ErpGroupBuyingReviewRespVO::getSupplyGroupPrice)
                .selectAs(ErpGroupBuyingReviewDO::getExpressFee, ErpGroupBuyingReviewRespVO::getExpressFee)
                .selectAs(ErpGroupBuyingReviewDO::getSampleSendDate, ErpGroupBuyingReviewRespVO::getSampleSendDate)
                .selectAs(ErpGroupBuyingReviewDO::getGroupStartDate, ErpGroupBuyingReviewRespVO::getGroupStartDate)
                .selectAs(ErpGroupBuyingReviewDO::getGroupSales, ErpGroupBuyingReviewRespVO::getGroupSales)
                .selectAs(ErpGroupBuyingReviewDO::getRepeatGroupDate, ErpGroupBuyingReviewRespVO::getRepeatGroupDate)
                .selectAs(ErpGroupBuyingReviewDO::getRepeatGroupSales, ErpGroupBuyingReviewRespVO::getRepeatGroupSales)
                .selectAs(ErpGroupBuyingReviewDO::getGroupBuyingId, ErpGroupBuyingReviewRespVO::getGroupBuyingId)
                .selectAs(ErpGroupBuyingReviewDO::getCustomerId, ErpGroupBuyingReviewRespVO::getCustomerId)
                .selectAs(ErpGroupBuyingReviewDO::getCreator, ErpGroupBuyingReviewRespVO::getCreator)
                .selectAs(ErpGroupBuyingReviewDO::getCreateTime, ErpGroupBuyingReviewRespVO::getCreateTime);

        // 联表查询团购货盘信息
        query.leftJoin(ErpGroupBuyingDO.class, ErpGroupBuyingDO::getId, ErpGroupBuyingReviewDO::getGroupBuyingId)
                .selectAs(ErpGroupBuyingDO::getBrandName, ErpGroupBuyingReviewRespVO::getBrandName)
                .selectAs(ErpGroupBuyingDO::getNo, ErpGroupBuyingReviewRespVO::getGroupBuyingNo)
                .selectAs(ErpGroupBuyingDO::getProductName, ErpGroupBuyingReviewRespVO::getProductName)
                .selectAs(ErpGroupBuyingDO::getProductSpec, ErpGroupBuyingReviewRespVO::getProductSpec)
                .selectAs(ErpGroupBuyingDO::getProductSku, ErpGroupBuyingReviewRespVO::getProductSku)
                .selectAs(ErpGroupBuyingDO::getGroupMechanism, ErpGroupBuyingReviewRespVO::getGroupMechanism)
                .selectAs(ErpGroupBuyingDO::getStatus, ErpGroupBuyingReviewRespVO::getStatus)
                .selectAs(ErpGroupBuyingDO::getExpressFee, ErpGroupBuyingReviewRespVO::getExpressFee);

        // 联表查询客户信息
        query.leftJoin(ErpCustomerDO.class, ErpCustomerDO::getId, ErpGroupBuyingReviewDO::getCustomerId)
                .selectAs(ErpCustomerDO::getName, ErpGroupBuyingReviewRespVO::getCustomerName);

        return selectJoinList(ErpGroupBuyingReviewRespVO.class, query);
    }

    default void insertBatch(List<ErpGroupBuyingReviewDO> list) {
        list.forEach(this::insert);
    }
}
