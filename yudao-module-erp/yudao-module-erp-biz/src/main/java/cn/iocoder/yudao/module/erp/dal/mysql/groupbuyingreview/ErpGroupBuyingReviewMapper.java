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
import java.util.List;

@Mapper
public interface ErpGroupBuyingReviewMapper extends BaseMapperX<ErpGroupBuyingReviewDO> {

    default PageResult<ErpGroupBuyingReviewRespVO> selectPage(ErpGroupBuyingReviewPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpGroupBuyingReviewDO> query = new MPJLambdaWrapperX<ErpGroupBuyingReviewDO>()
                .likeIfPresent(ErpGroupBuyingReviewDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpGroupBuyingReviewDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(ErpGroupBuyingReviewDO::getGroupBuyingId, reqVO.getGroupBuyingId())
                .betweenIfPresent(ErpGroupBuyingReviewDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpGroupBuyingReviewDO::getId)
                // 团购复盘表字段映射
                .selectAs(ErpGroupBuyingReviewDO::getId, ErpGroupBuyingReviewRespVO::getId)
                .selectAs(ErpGroupBuyingReviewDO::getNo, ErpGroupBuyingReviewRespVO::getNo)
                .selectAs(ErpGroupBuyingReviewDO::getRemark, ErpGroupBuyingReviewRespVO::getRemark)
                .selectAs(ErpGroupBuyingReviewDO::getSupplyGroupPrice, ErpGroupBuyingReviewRespVO::getSupplyGroupPrice)
                .selectAs(ErpGroupBuyingReviewDO::getSampleSendDate, ErpGroupBuyingReviewRespVO::getSampleSendDate)
                .selectAs(ErpGroupBuyingReviewDO::getGroupStartDate, ErpGroupBuyingReviewRespVO::getGroupStartDate)
                .selectAs(ErpGroupBuyingReviewDO::getGroupSales, ErpGroupBuyingReviewRespVO::getGroupSales)
                .selectAs(ErpGroupBuyingReviewDO::getRepeatGroupDate, ErpGroupBuyingReviewRespVO::getRepeatGroupDate)
                .selectAs(ErpGroupBuyingReviewDO::getRepeatGroupSales, ErpGroupBuyingReviewRespVO::getRepeatGroupSales)
                .selectAs(ErpGroupBuyingReviewDO::getGroupBuyingId, ErpGroupBuyingReviewRespVO::getGroupBuyingId)
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

        return selectJoinPage(reqVO, ErpGroupBuyingReviewRespVO.class, query);
    }

    default ErpGroupBuyingReviewDO selectByNo(String no) {
        return selectOne(ErpGroupBuyingReviewDO::getNo, no);
    }

    default List<ErpGroupBuyingReviewDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpGroupBuyingReviewDO::getNo, nos);
    }

    default void insertBatch(List<ErpGroupBuyingReviewDO> list) {
        list.forEach(this::insert);
    }
}
