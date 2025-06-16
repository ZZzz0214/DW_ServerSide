package cn.iocoder.yudao.module.erp.dal.mysql.livebroadcastingreview;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcasting.ErpLiveBroadcastingDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastingreview.ErpLiveBroadcastingReviewDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import org.apache.ibatis.annotations.Mapper;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import cn.hutool.core.collection.CollUtil;

@Mapper
public interface ErpLiveBroadcastingReviewMapper extends BaseMapperX<ErpLiveBroadcastingReviewDO> {

    default PageResult<ErpLiveBroadcastingReviewRespVO> selectPage(ErpLiveBroadcastingReviewPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpLiveBroadcastingReviewDO> query = new MPJLambdaWrapperX<ErpLiveBroadcastingReviewDO>()
                .likeIfPresent(ErpLiveBroadcastingReviewDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpLiveBroadcastingReviewDO::getLiveBroadcastingId, reqVO.getLiveBroadcastingId())
                .eqIfPresent(ErpLiveBroadcastingReviewDO::getCustomerId, reqVO.getCustomerId())
                .likeIfPresent(ErpLiveBroadcastingReviewDO::getLivePlatform, reqVO.getLivePlatform())
                .betweenIfPresent(ErpLiveBroadcastingReviewDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpLiveBroadcastingReviewDO::getId)
                // 直播复盘表字段映射
                .selectAs(ErpLiveBroadcastingReviewDO::getId, ErpLiveBroadcastingReviewRespVO::getId)
                .selectAs(ErpLiveBroadcastingReviewDO::getNo, ErpLiveBroadcastingReviewRespVO::getNo)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveBroadcastingId, ErpLiveBroadcastingReviewRespVO::getLiveBroadcastingId)
                .selectAs(ErpLiveBroadcastingReviewDO::getRemark, ErpLiveBroadcastingReviewRespVO::getRemark)
                .selectAs(ErpLiveBroadcastingReviewDO::getCustomerId, ErpLiveBroadcastingReviewRespVO::getCustomerId)
                .selectAs(ErpLiveBroadcastingReviewDO::getLivePlatform, ErpLiveBroadcastingReviewRespVO::getLivePlatform)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveCommission, ErpLiveBroadcastingReviewRespVO::getLiveCommission)
                .selectAs(ErpLiveBroadcastingReviewDO::getPublicCommission, ErpLiveBroadcastingReviewRespVO::getPublicCommission)
                .selectAs(ErpLiveBroadcastingReviewDO::getRebateCommission, ErpLiveBroadcastingReviewRespVO::getRebateCommission)
                .selectAs(ErpLiveBroadcastingReviewDO::getSampleSendDate, ErpLiveBroadcastingReviewRespVO::getSampleSendDate)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveStartDate, ErpLiveBroadcastingReviewRespVO::getLiveStartDate)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveSales, ErpLiveBroadcastingReviewRespVO::getLiveSales)
                .selectAs(ErpLiveBroadcastingReviewDO::getRepeatLiveDate, ErpLiveBroadcastingReviewRespVO::getRepeatLiveDate)
                .selectAs(ErpLiveBroadcastingReviewDO::getRepeatLiveSales, ErpLiveBroadcastingReviewRespVO::getRepeatLiveSales)
                .selectAs(ErpLiveBroadcastingReviewDO::getLivePlatform, ErpLiveBroadcastingReviewRespVO::getLivePlatform)
                .selectAs(ErpLiveBroadcastingReviewDO::getCreator, ErpLiveBroadcastingReviewRespVO::getCreator)
                .selectAs(ErpLiveBroadcastingReviewDO::getCreateTime, ErpLiveBroadcastingReviewRespVO::getCreateTime);

        // 联表查询直播货盘信息
        query.leftJoin(ErpLiveBroadcastingDO.class, ErpLiveBroadcastingDO::getId, ErpLiveBroadcastingReviewDO::getLiveBroadcastingId)
                .selectAs(ErpLiveBroadcastingDO::getNo, ErpLiveBroadcastingReviewRespVO::getLiveBroadcastingNo)
                .selectAs(ErpLiveBroadcastingDO::getProductImage, ErpLiveBroadcastingReviewRespVO::getProductImage)
                .selectAs(ErpLiveBroadcastingDO::getBrandId, ErpLiveBroadcastingReviewRespVO::getBrandId)  // 直接映射品牌ID到brandName字段
                .selectAs(ErpLiveBroadcastingDO::getProductName, ErpLiveBroadcastingReviewRespVO::getProductName)
                .selectAs(ErpLiveBroadcastingDO::getProductSpec, ErpLiveBroadcastingReviewRespVO::getProductSpec)
                .selectAs(ErpLiveBroadcastingDO::getProductSku, ErpLiveBroadcastingReviewRespVO::getProductSku)
                .selectAs(ErpLiveBroadcastingDO::getLivePrice, ErpLiveBroadcastingReviewRespVO::getLivePrice)
                .selectAs(ErpLiveBroadcastingDO::getLiveStatus, ErpLiveBroadcastingReviewRespVO::getLiveStatus);  // 直接映射货盘状态

        // 联表查询客户信息
        query.leftJoin(ErpCustomerDO.class, ErpCustomerDO::getId, ErpLiveBroadcastingReviewDO::getCustomerId)
                .selectAs(ErpCustomerDO::getName, ErpLiveBroadcastingReviewRespVO::getCustomerName);

        return selectJoinPage(reqVO, ErpLiveBroadcastingReviewRespVO.class, query);
    }

    default ErpLiveBroadcastingReviewDO selectByNo(String no) {
        return selectOne(ErpLiveBroadcastingReviewDO::getNo, no);
    }

    default List<ErpLiveBroadcastingReviewRespVO> selectListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }

        MPJLambdaWrapperX<ErpLiveBroadcastingReviewDO> query = new MPJLambdaWrapperX<ErpLiveBroadcastingReviewDO>()
                .in(ErpLiveBroadcastingReviewDO::getId, ids)
                .orderByDesc(ErpLiveBroadcastingReviewDO::getId)
                // 直播复盘表字段映射
                .selectAs(ErpLiveBroadcastingReviewDO::getId, ErpLiveBroadcastingReviewRespVO::getId)
                .selectAs(ErpLiveBroadcastingReviewDO::getNo, ErpLiveBroadcastingReviewRespVO::getNo)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveBroadcastingId, ErpLiveBroadcastingReviewRespVO::getLiveBroadcastingId)
                .selectAs(ErpLiveBroadcastingReviewDO::getRemark, ErpLiveBroadcastingReviewRespVO::getRemark)
                .selectAs(ErpLiveBroadcastingReviewDO::getCustomerId, ErpLiveBroadcastingReviewRespVO::getCustomerId)
                .selectAs(ErpLiveBroadcastingReviewDO::getLivePlatform, ErpLiveBroadcastingReviewRespVO::getLivePlatform)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveCommission, ErpLiveBroadcastingReviewRespVO::getLiveCommission)
                .selectAs(ErpLiveBroadcastingReviewDO::getPublicCommission, ErpLiveBroadcastingReviewRespVO::getPublicCommission)
                .selectAs(ErpLiveBroadcastingReviewDO::getRebateCommission, ErpLiveBroadcastingReviewRespVO::getRebateCommission)
                .selectAs(ErpLiveBroadcastingReviewDO::getSampleSendDate, ErpLiveBroadcastingReviewRespVO::getSampleSendDate)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveStartDate, ErpLiveBroadcastingReviewRespVO::getLiveStartDate)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveSales, ErpLiveBroadcastingReviewRespVO::getLiveSales)
                .selectAs(ErpLiveBroadcastingReviewDO::getRepeatLiveDate, ErpLiveBroadcastingReviewRespVO::getRepeatLiveDate)
                .selectAs(ErpLiveBroadcastingReviewDO::getRepeatLiveSales, ErpLiveBroadcastingReviewRespVO::getRepeatLiveSales)
                .selectAs(ErpLiveBroadcastingReviewDO::getCreateTime, ErpLiveBroadcastingReviewRespVO::getCreateTime);

        // 联表查询直播货盘信息
        query.leftJoin(ErpLiveBroadcastingDO.class, ErpLiveBroadcastingDO::getId, ErpLiveBroadcastingReviewDO::getLiveBroadcastingId)
                .selectAs(ErpLiveBroadcastingDO::getNo, ErpLiveBroadcastingReviewRespVO::getLiveBroadcastingNo)
                .selectAs(ErpLiveBroadcastingDO::getProductImage, ErpLiveBroadcastingReviewRespVO::getProductImage)
                .selectAs(ErpLiveBroadcastingDO::getBrandId, ErpLiveBroadcastingReviewRespVO::getBrandId)  // 直接映射品牌ID到brandName字段
                .selectAs(ErpLiveBroadcastingDO::getProductName, ErpLiveBroadcastingReviewRespVO::getProductName)
                .selectAs(ErpLiveBroadcastingDO::getProductSpec, ErpLiveBroadcastingReviewRespVO::getProductSpec)
                .selectAs(ErpLiveBroadcastingDO::getProductSku, ErpLiveBroadcastingReviewRespVO::getProductSku)
                .selectAs(ErpLiveBroadcastingDO::getLivePrice, ErpLiveBroadcastingReviewRespVO::getLivePrice)
                .selectAs(ErpLiveBroadcastingDO::getLiveStatus, ErpLiveBroadcastingReviewRespVO::getLiveStatus);

        // 联表查询客户信息
        query.leftJoin(ErpCustomerDO.class, ErpCustomerDO::getId, ErpLiveBroadcastingReviewDO::getCustomerId)
                .selectAs(ErpCustomerDO::getName, ErpLiveBroadcastingReviewRespVO::getCustomerName);

        return selectJoinList(ErpLiveBroadcastingReviewRespVO.class, query);
    }

    default List<ErpLiveBroadcastingReviewDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpLiveBroadcastingReviewDO::getNo, nos);
    }

    default void insertBatch(List<ErpLiveBroadcastingReviewDO> list) {
        list.forEach(this::insert);
    }
}
