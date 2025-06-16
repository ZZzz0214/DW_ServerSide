package cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcastinginfo;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo.ErpPrivateBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastinginfo.ErpPrivateBroadcastingInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpPrivateBroadcastingInfoMapper extends BaseMapperX<ErpPrivateBroadcastingInfoDO> {

    default PageResult<ErpPrivateBroadcastingInfoRespVO> selectPage(ErpPrivateBroadcastingInfoPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpPrivateBroadcastingInfoDO> query = new MPJLambdaWrapperX<ErpPrivateBroadcastingInfoDO>()
                .likeIfPresent(ErpPrivateBroadcastingInfoDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpPrivateBroadcastingInfoDO::getCustomerName, reqVO.getCustomerName())
                .likeIfPresent(ErpPrivateBroadcastingInfoDO::getPlatformName, reqVO.getPlatformName())
                .likeIfPresent(ErpPrivateBroadcastingInfoDO::getCustomerCity, reqVO.getCustomerCity())
                .betweenIfPresent(ErpPrivateBroadcastingInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpPrivateBroadcastingInfoDO::getId)
                // 私播信息表字段映射
                .selectAs(ErpPrivateBroadcastingInfoDO::getId, ErpPrivateBroadcastingInfoRespVO::getId)
                .selectAs(ErpPrivateBroadcastingInfoDO::getNo, ErpPrivateBroadcastingInfoRespVO::getNo)
                .selectAs(ErpPrivateBroadcastingInfoDO::getCustomerName, ErpPrivateBroadcastingInfoRespVO::getCustomerName)
                .selectAs(ErpPrivateBroadcastingInfoDO::getCustomerPosition, ErpPrivateBroadcastingInfoRespVO::getCustomerPosition)
                .selectAs(ErpPrivateBroadcastingInfoDO::getCustomerWechat, ErpPrivateBroadcastingInfoRespVO::getCustomerWechat)
                .selectAs(ErpPrivateBroadcastingInfoDO::getPlatformName, ErpPrivateBroadcastingInfoRespVO::getPlatformName)
                .selectAs(ErpPrivateBroadcastingInfoDO::getCustomerAttribute, ErpPrivateBroadcastingInfoRespVO::getCustomerAttribute)
                .selectAs(ErpPrivateBroadcastingInfoDO::getCustomerCity, ErpPrivateBroadcastingInfoRespVO::getCustomerCity)
                .selectAs(ErpPrivateBroadcastingInfoDO::getCustomerDistrict, ErpPrivateBroadcastingInfoRespVO::getCustomerDistrict)
                .selectAs(ErpPrivateBroadcastingInfoDO::getUserPortrait, ErpPrivateBroadcastingInfoRespVO::getUserPortrait)
                .selectAs(ErpPrivateBroadcastingInfoDO::getRecruitmentCategory, ErpPrivateBroadcastingInfoRespVO::getRecruitmentCategory)
                .selectAs(ErpPrivateBroadcastingInfoDO::getSelectionCriteria, ErpPrivateBroadcastingInfoRespVO::getSelectionCriteria)
                .selectAs(ErpPrivateBroadcastingInfoDO::getRemark, ErpPrivateBroadcastingInfoRespVO::getRemark)
                .selectAs(ErpPrivateBroadcastingInfoDO::getCreator, ErpPrivateBroadcastingInfoRespVO::getCreator)
                .selectAs(ErpPrivateBroadcastingInfoDO::getCreateTime, ErpPrivateBroadcastingInfoRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpPrivateBroadcastingInfoRespVO.class, query);
    }

    default ErpPrivateBroadcastingInfoDO selectByNo(String no) {
        return selectOne(ErpPrivateBroadcastingInfoDO::getNo, no);
    }

    default List<ErpPrivateBroadcastingInfoDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpPrivateBroadcastingInfoDO::getNo, nos);
    }
}
