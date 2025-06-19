package cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcastingreview;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcasting.ErpPrivateBroadcastingDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastingreview.ErpPrivateBroadcastingReviewDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpPrivateBroadcastingReviewMapper extends BaseMapperX<ErpPrivateBroadcastingReviewDO> {

    default PageResult<ErpPrivateBroadcastingReviewRespVO> selectPage(ErpPrivateBroadcastingReviewPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpPrivateBroadcastingReviewDO> query = new MPJLambdaWrapperX<ErpPrivateBroadcastingReviewDO>()
                .likeIfPresent(ErpPrivateBroadcastingReviewDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpPrivateBroadcastingReviewDO::getProductNakedPrice, reqVO.getNakedPrice())
                .likeIfPresent(ErpPrivateBroadcastingReviewDO::getExpressFee, reqVO.getExpressFee())
                .likeIfPresent(ErpPrivateBroadcastingReviewDO::getDropshipPrice, reqVO.getDropshippingPrice())
                .betweenIfPresent(ErpPrivateBroadcastingReviewDO::getSampleSendDate, reqVO.getSampleSendDate())
                .betweenIfPresent(ErpPrivateBroadcastingReviewDO::getGroupStartDate, reqVO.getGroupStartDate())
                .likeIfPresent(ErpPrivateBroadcastingReviewDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(ErpPrivateBroadcastingReviewDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpPrivateBroadcastingReviewDO::getId)
                // 私播复盘表字段映射
                .selectAs(ErpPrivateBroadcastingReviewDO::getId, ErpPrivateBroadcastingReviewRespVO::getId)
                .selectAs(ErpPrivateBroadcastingReviewDO::getNo, ErpPrivateBroadcastingReviewRespVO::getNo)
                .selectAs(ErpPrivateBroadcastingReviewDO::getPrivateBroadcastingId, ErpPrivateBroadcastingReviewRespVO::getPrivateBroadcastingId)
                .selectAs(ErpPrivateBroadcastingReviewDO::getCustomerId, ErpPrivateBroadcastingReviewRespVO::getCustomerId)
                .selectAs(ErpPrivateBroadcastingReviewDO::getProductNakedPrice, ErpPrivateBroadcastingReviewRespVO::getProductNakedPrice)
                .selectAs(ErpPrivateBroadcastingReviewDO::getExpressFee, ErpPrivateBroadcastingReviewRespVO::getExpressFee)
                .selectAs(ErpPrivateBroadcastingReviewDO::getDropshipPrice, ErpPrivateBroadcastingReviewRespVO::getDropshipPrice)
                .selectAs(ErpPrivateBroadcastingReviewDO::getSampleSendDate, ErpPrivateBroadcastingReviewRespVO::getSampleSendDate)
                .selectAs(ErpPrivateBroadcastingReviewDO::getGroupStartDate, ErpPrivateBroadcastingReviewRespVO::getGroupStartDate)
                .selectAs(ErpPrivateBroadcastingReviewDO::getGroupSales, ErpPrivateBroadcastingReviewRespVO::getGroupSales)
                .selectAs(ErpPrivateBroadcastingReviewDO::getRepeatGroupDate, ErpPrivateBroadcastingReviewRespVO::getRepeatGroupDate)
                .selectAs(ErpPrivateBroadcastingReviewDO::getRepeatGroupSales, ErpPrivateBroadcastingReviewRespVO::getRepeatGroupSales)
                .selectAs(ErpPrivateBroadcastingReviewDO::getRemark, ErpPrivateBroadcastingReviewRespVO::getRemark)
                .selectAs(ErpPrivateBroadcastingReviewDO::getCreator, ErpPrivateBroadcastingReviewRespVO::getCreator)
                .selectAs(ErpPrivateBroadcastingReviewDO::getCreateTime, ErpPrivateBroadcastingReviewRespVO::getCreateTime);
                
        // 联表查询私播货盘信息
        query.leftJoin(ErpPrivateBroadcastingDO.class, ErpPrivateBroadcastingDO::getId, ErpPrivateBroadcastingReviewDO::getPrivateBroadcastingId);
        
        // 添加联表查询条件（需要在leftJoin之后单独处理）
        if (reqVO.getProductName() != null && !reqVO.getProductName().isEmpty()) {
            query.like(ErpPrivateBroadcastingDO::getProductName, reqVO.getProductName());
        }
        if (reqVO.getProductSpec() != null && !reqVO.getProductSpec().isEmpty()) {
            query.like(ErpPrivateBroadcastingDO::getProductSpec, reqVO.getProductSpec());
        }
        if (reqVO.getStatus() != null && !reqVO.getStatus().isEmpty()) {
            query.eq(ErpPrivateBroadcastingDO::getPrivateStatus, reqVO.getStatus());
        }
        if (reqVO.getBrandName() != null && !reqVO.getBrandName().isEmpty()) {
            query.like(ErpPrivateBroadcastingDO::getBrandName, reqVO.getBrandName());
        }
        
        query.selectAs(ErpPrivateBroadcastingDO::getNo, ErpPrivateBroadcastingReviewRespVO::getPrivateBroadcastingNo)
                .selectAs(ErpPrivateBroadcastingDO::getBrandName, ErpPrivateBroadcastingReviewRespVO::getBrandName)
                .selectAs(ErpPrivateBroadcastingDO::getProductName, ErpPrivateBroadcastingReviewRespVO::getProductName)
                .selectAs(ErpPrivateBroadcastingDO::getProductSpec, ErpPrivateBroadcastingReviewRespVO::getProductSpec)
                .selectAs(ErpPrivateBroadcastingDO::getProductSku, ErpPrivateBroadcastingReviewRespVO::getProductSku)
                .selectAs(ErpPrivateBroadcastingDO::getLivePrice, ErpPrivateBroadcastingReviewRespVO::getLivePrice)
                .selectAs(ErpPrivateBroadcastingDO::getPrivateStatus, ErpPrivateBroadcastingReviewRespVO::getPrivateStatus);
                
        // 联表查询客户信息
        query.leftJoin(ErpCustomerDO.class, ErpCustomerDO::getId, ErpPrivateBroadcastingReviewDO::getCustomerId);
        
        // 添加客户查询条件
        if (reqVO.getCustomerName() != null && !reqVO.getCustomerName().isEmpty()) {
            query.like(ErpCustomerDO::getName, reqVO.getCustomerName());
        }
        
        query.selectAs(ErpCustomerDO::getName, ErpPrivateBroadcastingReviewRespVO::getCustomerName);
                
        return selectJoinPage(reqVO, ErpPrivateBroadcastingReviewRespVO.class, query);
    }

    default ErpPrivateBroadcastingReviewDO selectByNo(String no) {
        return selectOne(ErpPrivateBroadcastingReviewDO::getNo, no);
    }

    default List<ErpPrivateBroadcastingReviewDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpPrivateBroadcastingReviewDO::getNo, nos);
    }

    default void insertBatch(List<ErpPrivateBroadcastingReviewDO> list) {
        list.forEach(this::insert);
    }
}
