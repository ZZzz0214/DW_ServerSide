package cn.iocoder.yudao.module.erp.dal.mysql.distribution;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.distribution.vo.ErpDistributionCombinedPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionCombinedDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpDistributionCombinedMapper extends BaseMapperX<ErpDistributionCombinedDO> {

    default PageResult<ErpDistributionCombinedDO> selectPage(ErpDistributionCombinedPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpDistributionCombinedDO> query = new MPJLambdaWrapperX<ErpDistributionCombinedDO>()
                .likeIfPresent(ErpDistributionCombinedDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpDistributionCombinedDO::getOrderNumber, reqVO.getOrderNumber())
                .likeIfPresent(ErpDistributionCombinedDO::getTrackingNumber, reqVO.getTrackingNumber())
                .likeIfPresent(ErpDistributionCombinedDO::getReceiverName, reqVO.getReceiverName())
                .likeIfPresent(ErpDistributionCombinedDO::getReceiverPhone, reqVO.getReceiverPhone())
                .eqIfPresent(ErpDistributionCombinedDO::getPurchaseAuditStatus, reqVO.getPurchaseAuditStatus())
                .eqIfPresent(ErpDistributionCombinedDO::getSaleAuditStatus, reqVO.getSaleAuditStatus())
                .orderByDesc(ErpDistributionCombinedDO::getId);

        return selectPage(reqVO, query);
    }

    default List<ErpDistributionCombinedDO> selectListByNoIn(Collection<String> nos) {
        if (CollUtil.isEmpty(nos)) {
            return Collections.emptyList();
        }
        return selectList(ErpDistributionCombinedDO::getNo, nos);
    }

    default ErpDistributionCombinedDO selectByNo(String no) {
        return selectOne(ErpDistributionCombinedDO::getNo, no);
    }

    default List<ErpDistributionCombinedDO> selectCombinedList(ErpDistributionCombinedPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpDistributionCombinedDO> query = new MPJLambdaWrapperX<ErpDistributionCombinedDO>()
                .orderByDesc(ErpDistributionCombinedDO::getId);
        return selectList(query);
    }
}
