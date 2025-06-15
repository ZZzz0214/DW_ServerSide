package cn.iocoder.yudao.module.erp.dal.mysql.groupbuyinginfo;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuyinginfo.ErpGroupBuyingInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpGroupBuyingInfoMapper extends BaseMapperX<ErpGroupBuyingInfoDO> {

    default PageResult<ErpGroupBuyingInfoRespVO> selectPage(ErpGroupBuyingInfoPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpGroupBuyingInfoDO> query = new MPJLambdaWrapperX<ErpGroupBuyingInfoDO>()
                .likeIfPresent(ErpGroupBuyingInfoDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpGroupBuyingInfoDO::getCustomerName, reqVO.getCustomerName())
                .likeIfPresent(ErpGroupBuyingInfoDO::getPlatformName, reqVO.getPlatformName())
                .likeIfPresent(ErpGroupBuyingInfoDO::getCustomerCity, reqVO.getCustomerCity())
                .betweenIfPresent(ErpGroupBuyingInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpGroupBuyingInfoDO::getId)
                // 团购信息表字段映射
                .selectAs(ErpGroupBuyingInfoDO::getId, ErpGroupBuyingInfoRespVO::getId)
                .selectAs(ErpGroupBuyingInfoDO::getNo, ErpGroupBuyingInfoRespVO::getNo)
                .selectAs(ErpGroupBuyingInfoDO::getCustomerName, ErpGroupBuyingInfoRespVO::getCustomerName)
                .selectAs(ErpGroupBuyingInfoDO::getCustomerPosition, ErpGroupBuyingInfoRespVO::getCustomerPosition)
                .selectAs(ErpGroupBuyingInfoDO::getCustomerWechat, ErpGroupBuyingInfoRespVO::getCustomerWechat)
                .selectAs(ErpGroupBuyingInfoDO::getPlatformName, ErpGroupBuyingInfoRespVO::getPlatformName)
                .selectAs(ErpGroupBuyingInfoDO::getCustomerAttribute, ErpGroupBuyingInfoRespVO::getCustomerAttribute)
                .selectAs(ErpGroupBuyingInfoDO::getCustomerCity, ErpGroupBuyingInfoRespVO::getCustomerCity)
                .selectAs(ErpGroupBuyingInfoDO::getCustomerDistrict, ErpGroupBuyingInfoRespVO::getCustomerDistrict)
                .selectAs(ErpGroupBuyingInfoDO::getUserPortrait, ErpGroupBuyingInfoRespVO::getUserPortrait)
                .selectAs(ErpGroupBuyingInfoDO::getRecruitmentCategory, ErpGroupBuyingInfoRespVO::getRecruitmentCategory)
                .selectAs(ErpGroupBuyingInfoDO::getSelectionCriteria, ErpGroupBuyingInfoRespVO::getSelectionCriteria)
                .selectAs(ErpGroupBuyingInfoDO::getRemark, ErpGroupBuyingInfoRespVO::getRemark)
                .selectAs(ErpGroupBuyingInfoDO::getCreator, ErpGroupBuyingInfoRespVO::getCreator)
                .selectAs(ErpGroupBuyingInfoDO::getCreateTime, ErpGroupBuyingInfoRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpGroupBuyingInfoRespVO.class, query);
    }

    default ErpGroupBuyingInfoDO selectByNo(String no) {
        return selectOne(ErpGroupBuyingInfoDO::getNo, no);
    }

    default List<ErpGroupBuyingInfoDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpGroupBuyingInfoDO::getNo, nos);
    }

    default void insertBatch(List<ErpGroupBuyingInfoDO> list) {
        list.forEach(this::insert);
    }
}
