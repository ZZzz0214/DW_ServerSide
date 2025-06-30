package cn.iocoder.yudao.module.erp.dal.mysql.livebroadcastingreview;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
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

    default PageResult<ErpLiveBroadcastingReviewRespVO> selectPage(ErpLiveBroadcastingReviewPageReqVO reqVO, String currentUsername) {
        MPJLambdaWrapperX<ErpLiveBroadcastingReviewDO> query = new MPJLambdaWrapperX<ErpLiveBroadcastingReviewDO>()
                .likeIfPresent(ErpLiveBroadcastingReviewDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpLiveBroadcastingReviewDO::getLiveCommission, reqVO.getLiveCommission())
                .likeIfPresent(ErpLiveBroadcastingReviewDO::getPublicCommission, reqVO.getPublicCommission())
                .betweenIfPresent(ErpLiveBroadcastingReviewDO::getSampleSendDate, reqVO.getSampleSendDate())
                .betweenIfPresent(ErpLiveBroadcastingReviewDO::getLiveStartDate, reqVO.getLiveStartDate())
                .likeIfPresent(ErpLiveBroadcastingReviewDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(ErpLiveBroadcastingReviewDO::getCreateTime, reqVO.getCreateTime());
        
        // 权限控制：admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"admin".equals(currentUsername)) {
            query.eq(ErpLiveBroadcastingReviewDO::getCreator, currentUsername);
        }
        
        query.orderByDesc(ErpLiveBroadcastingReviewDO::getId)
                // 直播复盘表字段映射
                .selectAs(ErpLiveBroadcastingReviewDO::getId, ErpLiveBroadcastingReviewRespVO::getId)
                .selectAs(ErpLiveBroadcastingReviewDO::getNo, ErpLiveBroadcastingReviewRespVO::getNo)
                .selectAs(ErpLiveBroadcastingReviewDO::getLiveBroadcastingId, ErpLiveBroadcastingReviewRespVO::getLiveBroadcastingId)
                .selectAs(ErpLiveBroadcastingReviewDO::getRemark, ErpLiveBroadcastingReviewRespVO::getRemark)
                .selectAs(ErpLiveBroadcastingReviewDO::getCustomerName, ErpLiveBroadcastingReviewRespVO::getCustomerName)
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
        query.leftJoin(ErpLiveBroadcastingDO.class, ErpLiveBroadcastingDO::getId, ErpLiveBroadcastingReviewDO::getLiveBroadcastingId);
        
        // 添加联表查询条件（需要在leftJoin之后单独处理）
        if (reqVO.getProductName() != null && !reqVO.getProductName().isEmpty()) {
            query.like(ErpLiveBroadcastingDO::getProductName, reqVO.getProductName());
        }
        if (reqVO.getProductSpec() != null && !reqVO.getProductSpec().isEmpty()) {
            query.like(ErpLiveBroadcastingDO::getProductSpec, reqVO.getProductSpec());
        }
        if (reqVO.getLiveStatus() != null && !reqVO.getLiveStatus().isEmpty()) {
            query.eq(ErpLiveBroadcastingDO::getLiveStatus, reqVO.getLiveStatus());
        }
        if (reqVO.getBrandName() != null && !reqVO.getBrandName().isEmpty()) {
            query.like(ErpLiveBroadcastingDO::getBrandName, reqVO.getBrandName());
        }
        
        query.selectAs(ErpLiveBroadcastingDO::getNo, ErpLiveBroadcastingReviewRespVO::getLiveBroadcastingNo)
                .selectAs(ErpLiveBroadcastingDO::getBrandName, ErpLiveBroadcastingReviewRespVO::getBrandName)
                .selectAs(ErpLiveBroadcastingDO::getProductName, ErpLiveBroadcastingReviewRespVO::getProductName)
                .selectAs(ErpLiveBroadcastingDO::getProductSpec, ErpLiveBroadcastingReviewRespVO::getProductSpec)
                .selectAs(ErpLiveBroadcastingDO::getProductSku, ErpLiveBroadcastingReviewRespVO::getProductSku)
                .selectAs(ErpLiveBroadcastingDO::getLivePrice, ErpLiveBroadcastingReviewRespVO::getLivePrice)
                .selectAs(ErpLiveBroadcastingDO::getLiveCommission, ErpLiveBroadcastingReviewRespVO::getLiveCommission)
                .selectAs(ErpLiveBroadcastingDO::getPublicCommission, ErpLiveBroadcastingReviewRespVO::getPublicCommission)
                .selectAs(ErpLiveBroadcastingDO::getLiveStatus, ErpLiveBroadcastingReviewRespVO::getLiveStatus);

        // 客户名称直接使用，不再联表查询客户信息
        if (reqVO.getCustomerName() != null && !reqVO.getCustomerName().isEmpty()) {
            query.like(ErpLiveBroadcastingReviewDO::getCustomerName, reqVO.getCustomerName());
        }
        
        query.selectAs(ErpLiveBroadcastingReviewDO::getCustomerName, ErpLiveBroadcastingReviewRespVO::getCustomerName);

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
                .selectAs(ErpLiveBroadcastingReviewDO::getCustomerName, ErpLiveBroadcastingReviewRespVO::getCustomerName)
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
                .selectAs(ErpLiveBroadcastingDO::getBrandName, ErpLiveBroadcastingReviewRespVO::getBrandName)
                .selectAs(ErpLiveBroadcastingDO::getProductName, ErpLiveBroadcastingReviewRespVO::getProductName)
                .selectAs(ErpLiveBroadcastingDO::getProductSpec, ErpLiveBroadcastingReviewRespVO::getProductSpec)
                .selectAs(ErpLiveBroadcastingDO::getProductSku, ErpLiveBroadcastingReviewRespVO::getProductSku)
                .selectAs(ErpLiveBroadcastingDO::getLivePrice, ErpLiveBroadcastingReviewRespVO::getLivePrice)
                .selectAs(ErpLiveBroadcastingDO::getLiveStatus, ErpLiveBroadcastingReviewRespVO::getLiveStatus);

        // 客户名称直接使用，不再联表查询客户信息
        query.selectAs(ErpLiveBroadcastingReviewDO::getCustomerName, ErpLiveBroadcastingReviewRespVO::getCustomerName);

        return selectJoinList(ErpLiveBroadcastingReviewRespVO.class, query);
    }

    default List<ErpLiveBroadcastingReviewDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpLiveBroadcastingReviewDO::getNo, nos);
    }

    default void insertBatch(List<ErpLiveBroadcastingReviewDO> list) {
        list.forEach(this::insert);
    }

    /**
     * 根据直播货盘ID和客户名称查询记录（用于校验组合唯一性）
     */
    default ErpLiveBroadcastingReviewDO selectByLiveBroadcastingIdAndCustomerName(Long liveBroadcastingId, String customerName, Long excludeId) {
        return selectOne(new LambdaQueryWrapperX<ErpLiveBroadcastingReviewDO>()
                .eq(ErpLiveBroadcastingReviewDO::getLiveBroadcastingId, liveBroadcastingId)
                .eq(ErpLiveBroadcastingReviewDO::getCustomerName, customerName)
                .neIfPresent(ErpLiveBroadcastingReviewDO::getId, excludeId));
    }
}
