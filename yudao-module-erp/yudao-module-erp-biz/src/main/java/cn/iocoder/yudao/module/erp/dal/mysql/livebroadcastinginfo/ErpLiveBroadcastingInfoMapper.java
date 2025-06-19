package cn.iocoder.yudao.module.erp.dal.mysql.livebroadcastinginfo;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastinginfo.ErpLiveBroadcastingInfoDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import org.apache.ibatis.annotations.Mapper;
import cn.hutool.core.collection.CollUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpLiveBroadcastingInfoMapper extends BaseMapperX<ErpLiveBroadcastingInfoDO> {

    default PageResult<ErpLiveBroadcastingInfoRespVO> selectPage(ErpLiveBroadcastingInfoPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpLiveBroadcastingInfoDO> query = new MPJLambdaWrapperX<ErpLiveBroadcastingInfoDO>()
                .likeIfPresent(ErpLiveBroadcastingInfoDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpLiveBroadcastingInfoDO::getCustomerPosition, reqVO.getCustomerPosition())
                .likeIfPresent(ErpLiveBroadcastingInfoDO::getPlatformName, reqVO.getPlatformName())
                .likeIfPresent(ErpLiveBroadcastingInfoDO::getCustomerAttribute, reqVO.getCustomerAttribute())
                .likeIfPresent(ErpLiveBroadcastingInfoDO::getCustomerCity, reqVO.getCustomerCity())
                .likeIfPresent(ErpLiveBroadcastingInfoDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(ErpLiveBroadcastingInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpLiveBroadcastingInfoDO::getId)
                // 直播信息表字段映射
                .selectAs(ErpLiveBroadcastingInfoDO::getId, ErpLiveBroadcastingInfoRespVO::getId)
                .selectAs(ErpLiveBroadcastingInfoDO::getNo, ErpLiveBroadcastingInfoRespVO::getNo)
                .selectAs(ErpLiveBroadcastingInfoDO::getCustomerId, ErpLiveBroadcastingInfoRespVO::getCustomerId)
                .selectAs(ErpLiveBroadcastingInfoDO::getCustomerPosition, ErpLiveBroadcastingInfoRespVO::getCustomerPosition)
                .selectAs(ErpLiveBroadcastingInfoDO::getCustomerWechat, ErpLiveBroadcastingInfoRespVO::getCustomerWechat)
                .selectAs(ErpLiveBroadcastingInfoDO::getPlatformName, ErpLiveBroadcastingInfoRespVO::getPlatformName)
                .selectAs(ErpLiveBroadcastingInfoDO::getCustomerAttribute, ErpLiveBroadcastingInfoRespVO::getCustomerAttribute)
                .selectAs(ErpLiveBroadcastingInfoDO::getCustomerCity, ErpLiveBroadcastingInfoRespVO::getCustomerCity)
                .selectAs(ErpLiveBroadcastingInfoDO::getCustomerDistrict, ErpLiveBroadcastingInfoRespVO::getCustomerDistrict)
                .selectAs(ErpLiveBroadcastingInfoDO::getUserPortrait, ErpLiveBroadcastingInfoRespVO::getUserPortrait)
                .selectAs(ErpLiveBroadcastingInfoDO::getRecruitmentCategory, ErpLiveBroadcastingInfoRespVO::getRecruitmentCategory)
                .selectAs(ErpLiveBroadcastingInfoDO::getSelectionCriteria, ErpLiveBroadcastingInfoRespVO::getSelectionCriteria)
                .selectAs(ErpLiveBroadcastingInfoDO::getRemark, ErpLiveBroadcastingInfoRespVO::getRemark)
                .selectAs(ErpLiveBroadcastingInfoDO::getCreator, ErpLiveBroadcastingInfoRespVO::getCreator)
                .selectAs(ErpLiveBroadcastingInfoDO::getCreateTime, ErpLiveBroadcastingInfoRespVO::getCreateTime);

        // 联表查询客户信息
        query.leftJoin(ErpCustomerDO.class, ErpCustomerDO::getId, ErpLiveBroadcastingInfoDO::getCustomerId);
        
        // 客户名称搜索条件
        if (reqVO.getCustomerName() != null && !reqVO.getCustomerName().isEmpty()) {
            query.like(ErpCustomerDO::getName, reqVO.getCustomerName());
        }
        
        query.selectAs(ErpCustomerDO::getName, ErpLiveBroadcastingInfoRespVO::getCustomerName);

        return selectJoinPage(reqVO, ErpLiveBroadcastingInfoRespVO.class, query);
    }

    default List<ErpLiveBroadcastingInfoRespVO> selectListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }

        MPJLambdaWrapperX<ErpLiveBroadcastingInfoDO> query = new MPJLambdaWrapperX<ErpLiveBroadcastingInfoDO>()
                .in(ErpLiveBroadcastingInfoDO::getId, ids)
                .orderByDesc(ErpLiveBroadcastingInfoDO::getId)
                // 直播信息表字段映射
                .selectAs(ErpLiveBroadcastingInfoDO::getId, ErpLiveBroadcastingInfoRespVO::getId)
                .selectAs(ErpLiveBroadcastingInfoDO::getNo, ErpLiveBroadcastingInfoRespVO::getNo)
                .selectAs(ErpLiveBroadcastingInfoDO::getCustomerId, ErpLiveBroadcastingInfoRespVO::getCustomerId)
                .selectAs(ErpLiveBroadcastingInfoDO::getCustomerPosition, ErpLiveBroadcastingInfoRespVO::getCustomerPosition)
                .selectAs(ErpLiveBroadcastingInfoDO::getCustomerWechat, ErpLiveBroadcastingInfoRespVO::getCustomerWechat)
                .selectAs(ErpLiveBroadcastingInfoDO::getPlatformName, ErpLiveBroadcastingInfoRespVO::getPlatformName)
                .selectAs(ErpLiveBroadcastingInfoDO::getCustomerAttribute, ErpLiveBroadcastingInfoRespVO::getCustomerAttribute)
                .selectAs(ErpLiveBroadcastingInfoDO::getCustomerCity, ErpLiveBroadcastingInfoRespVO::getCustomerCity)
                .selectAs(ErpLiveBroadcastingInfoDO::getCustomerDistrict, ErpLiveBroadcastingInfoRespVO::getCustomerDistrict)
                .selectAs(ErpLiveBroadcastingInfoDO::getUserPortrait, ErpLiveBroadcastingInfoRespVO::getUserPortrait)
                .selectAs(ErpLiveBroadcastingInfoDO::getRecruitmentCategory, ErpLiveBroadcastingInfoRespVO::getRecruitmentCategory)
                .selectAs(ErpLiveBroadcastingInfoDO::getSelectionCriteria, ErpLiveBroadcastingInfoRespVO::getSelectionCriteria)
                .selectAs(ErpLiveBroadcastingInfoDO::getRemark, ErpLiveBroadcastingInfoRespVO::getRemark)
                .selectAs(ErpLiveBroadcastingInfoDO::getCreateTime, ErpLiveBroadcastingInfoRespVO::getCreateTime);

        // 联表查询客户信息
        query.leftJoin(ErpCustomerDO.class, ErpCustomerDO::getId, ErpLiveBroadcastingInfoDO::getCustomerId)
                .selectAs(ErpCustomerDO::getName, ErpLiveBroadcastingInfoRespVO::getCustomerName);

        return selectJoinList(ErpLiveBroadcastingInfoRespVO.class, query);
    }

    default ErpLiveBroadcastingInfoDO selectByNo(String no) {
        return selectOne(ErpLiveBroadcastingInfoDO::getNo, no);
    }

    default List<ErpLiveBroadcastingInfoDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpLiveBroadcastingInfoDO::getNo, nos);
    }
}
