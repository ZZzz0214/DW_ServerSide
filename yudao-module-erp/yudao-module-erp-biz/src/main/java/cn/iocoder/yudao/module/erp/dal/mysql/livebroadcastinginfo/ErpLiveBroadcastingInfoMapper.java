package cn.iocoder.yudao.module.erp.dal.mysql.livebroadcastinginfo;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastinginfo.ErpLiveBroadcastingInfoDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpLiveBroadcastingInfoMapper extends BaseMapperX<ErpLiveBroadcastingInfoDO> {

    default PageResult<ErpLiveBroadcastingInfoRespVO> selectPage(ErpLiveBroadcastingInfoPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpLiveBroadcastingInfoDO> query = new MPJLambdaWrapperX<ErpLiveBroadcastingInfoDO>()
                .likeIfPresent(ErpLiveBroadcastingInfoDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpLiveBroadcastingInfoDO::getCustomerId, reqVO.getCustomerId())
                .likeIfPresent(ErpLiveBroadcastingInfoDO::getPlatformName, reqVO.getPlatformName())
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
                .selectAs(ErpLiveBroadcastingInfoDO::getCreateTime, ErpLiveBroadcastingInfoRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpLiveBroadcastingInfoRespVO.class, query);
    }

    default ErpLiveBroadcastingInfoDO selectByNo(String no) {
        return selectOne(ErpLiveBroadcastingInfoDO::getNo, no);
    }
}