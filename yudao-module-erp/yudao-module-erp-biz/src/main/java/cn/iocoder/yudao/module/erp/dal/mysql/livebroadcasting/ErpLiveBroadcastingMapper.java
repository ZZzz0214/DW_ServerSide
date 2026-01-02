package cn.iocoder.yudao.module.erp.dal.mysql.livebroadcasting;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcasting.ErpLiveBroadcastingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpLiveBroadcastingMapper extends BaseMapperX<ErpLiveBroadcastingDO> {

    default PageResult<ErpLiveBroadcastingRespVO> selectPage(ErpLiveBroadcastingPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpLiveBroadcastingDO> query = new MPJLambdaWrapperX<ErpLiveBroadcastingDO>()
                .likeIfPresent(ErpLiveBroadcastingDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpLiveBroadcastingDO::getCategoryId, reqVO.getCategoryId())
                .likeIfPresent(ErpLiveBroadcastingDO::getProductName, reqVO.getProductName());
        
        // 品牌名称筛选：支持多选和为空筛选（可以同时选择多个值和为空）
        if (CollUtil.isNotEmpty(reqVO.getBrandNames()) || Boolean.TRUE.equals(reqVO.getBrandNameEmpty())) {
            query.and(w -> {
                boolean hasCondition = false;
                if (CollUtil.isNotEmpty(reqVO.getBrandNames())) {
                    w.in(ErpLiveBroadcastingDO::getBrandName, reqVO.getBrandNames());
                    hasCondition = true;
                }
                if (Boolean.TRUE.equals(reqVO.getBrandNameEmpty())) {
                    if (hasCondition) {
                        w.or();
                    }
                    w.and(empty -> empty.isNull(ErpLiveBroadcastingDO::getBrandName).or().eq(ErpLiveBroadcastingDO::getBrandName, ""));
                }
            });
        } else {
            query.eqIfPresent(ErpLiveBroadcastingDO::getBrandName, reqVO.getBrandName());
        }
        
        query.likeIfPresent(ErpLiveBroadcastingDO::getProductSpec, reqVO.getProductSpec())
                .betweenIfPresent(ErpLiveBroadcastingDO::getShelfLife, reqVO.getShelfLife())
                .likeIfPresent(ErpLiveBroadcastingDO::getLivePrice, reqVO.getLivePrice())
                .likeIfPresent(ErpLiveBroadcastingDO::getLiveCommission, reqVO.getLiveCommission())
                .likeIfPresent(ErpLiveBroadcastingDO::getPublicCommission, reqVO.getPublicCommission());
        
        // 货盘状态筛选：支持多选和为空筛选（可以同时选择多个值和为空）
        if (CollUtil.isNotEmpty(reqVO.getLiveStatuses()) || Boolean.TRUE.equals(reqVO.getLiveStatusEmpty())) {
            query.and(w -> {
                boolean hasCondition = false;
                if (CollUtil.isNotEmpty(reqVO.getLiveStatuses())) {
                    w.in(ErpLiveBroadcastingDO::getLiveStatus, reqVO.getLiveStatuses());
                    hasCondition = true;
                }
                if (Boolean.TRUE.equals(reqVO.getLiveStatusEmpty())) {
                    if (hasCondition) {
                        w.or();
                    }
                    w.and(empty -> empty.isNull(ErpLiveBroadcastingDO::getLiveStatus).or().eq(ErpLiveBroadcastingDO::getLiveStatus, ""));
                }
            });
        } else {
            query.eqIfPresent(ErpLiveBroadcastingDO::getLiveStatus, reqVO.getLiveStatus());
        }
        
        query.likeIfPresent(ErpLiveBroadcastingDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(ErpLiveBroadcastingDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpLiveBroadcastingDO::getId)
                // 直播货盘表字段映射
                .selectAs(ErpLiveBroadcastingDO::getId, ErpLiveBroadcastingRespVO::getId)
                .selectAs(ErpLiveBroadcastingDO::getNo, ErpLiveBroadcastingRespVO::getNo)
                .selectAs(ErpLiveBroadcastingDO::getProductImage, ErpLiveBroadcastingRespVO::getProductImage)
                .selectAs(ErpLiveBroadcastingDO::getBrandName, ErpLiveBroadcastingRespVO::getBrandName)
                .selectAs(ErpLiveBroadcastingDO::getCategoryId, ErpLiveBroadcastingRespVO::getCategoryId)
                .selectAs(ErpLiveBroadcastingDO::getProductName, ErpLiveBroadcastingRespVO::getProductName)
                .selectAs(ErpLiveBroadcastingDO::getProductSpec, ErpLiveBroadcastingRespVO::getProductSpec)
                .selectAs(ErpLiveBroadcastingDO::getProductSku, ErpLiveBroadcastingRespVO::getProductSku)
                .selectAs(ErpLiveBroadcastingDO::getMarketPrice, ErpLiveBroadcastingRespVO::getMarketPrice)
                .selectAs(ErpLiveBroadcastingDO::getShelfLife, ErpLiveBroadcastingRespVO::getShelfLife)
                .selectAs(ErpLiveBroadcastingDO::getProductStock, ErpLiveBroadcastingRespVO::getProductStock)
                .selectAs(ErpLiveBroadcastingDO::getCoreSellingPoint, ErpLiveBroadcastingRespVO::getCoreSellingPoint)
                .selectAs(ErpLiveBroadcastingDO::getRemark, ErpLiveBroadcastingRespVO::getRemark)
                .selectAs(ErpLiveBroadcastingDO::getLivePrice, ErpLiveBroadcastingRespVO::getLivePrice)
                .selectAs(ErpLiveBroadcastingDO::getLiveCommission, ErpLiveBroadcastingRespVO::getLiveCommission)
                .selectAs(ErpLiveBroadcastingDO::getPublicCommission, ErpLiveBroadcastingRespVO::getPublicCommission)
                .selectAs(ErpLiveBroadcastingDO::getRebateCommission, ErpLiveBroadcastingRespVO::getRebateCommission)
                .selectAs(ErpLiveBroadcastingDO::getExpressCompany, ErpLiveBroadcastingRespVO::getExpressCompany)
                .selectAs(ErpLiveBroadcastingDO::getShippingTime, ErpLiveBroadcastingRespVO::getShippingTime)
                .selectAs(ErpLiveBroadcastingDO::getShippingArea, ErpLiveBroadcastingRespVO::getShippingArea)
                .selectAs(ErpLiveBroadcastingDO::getLiveStatus, ErpLiveBroadcastingRespVO::getLiveStatus)
                .selectAs(ErpLiveBroadcastingDO::getCreator, ErpLiveBroadcastingRespVO::getCreator)
                .selectAs(ErpLiveBroadcastingDO::getCreateTime, ErpLiveBroadcastingRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpLiveBroadcastingRespVO.class, query);
    }

    default ErpLiveBroadcastingDO selectByNo(String no) {
        return selectOne(ErpLiveBroadcastingDO::getNo, no);
    }

    default List<ErpLiveBroadcastingDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpLiveBroadcastingDO::getNo, nos);
    }

    default ErpLiveBroadcastingDO selectByProductName(String productName) {
        return selectOne(ErpLiveBroadcastingDO::getProductName, productName);
    }

    default List<ErpLiveBroadcastingDO> selectListByProductNameIn(Collection<String> productNames) {
        return selectList(ErpLiveBroadcastingDO::getProductName, productNames);
    }
}
