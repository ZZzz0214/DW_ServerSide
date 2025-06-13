package cn.iocoder.yudao.module.erp.dal.mysql.wholesale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.wholesale.vo.ErpWholesaleCombinedPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleCombinedDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpWholesaleCombinedMapper extends BaseMapperX<ErpWholesaleCombinedDO> {

    default PageResult<ErpWholesaleCombinedDO> selectPage(ErpWholesaleCombinedPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpWholesaleCombinedDO> query = new MPJLambdaWrapperX<ErpWholesaleCombinedDO>()
                .likeIfPresent(ErpWholesaleCombinedDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpWholesaleCombinedDO::getReceiverName, reqVO.getReceiverName())
                .likeIfPresent(ErpWholesaleCombinedDO::getReceiverPhone, reqVO.getReceiverPhone())
                .likeIfPresent(ErpWholesaleCombinedDO::getLogisticsNumber, reqVO.getLogisticsNumber())
                .eqIfPresent(ErpWholesaleCombinedDO::getPurchaseAuditStatus, reqVO.getPurchaseAuditStatus())
                .eqIfPresent(ErpWholesaleCombinedDO::getSaleAuditStatus, reqVO.getSaleAuditStatus())
                .orderByDesc(ErpWholesaleCombinedDO::getId);

        return selectPage(reqVO, query);
    }

    default List<ErpWholesaleCombinedDO> selectListByNoIn(Collection<String> nos) {
        if (CollUtil.isEmpty(nos)) {
            return Collections.emptyList();
        }
        return selectList(ErpWholesaleCombinedDO::getNo, nos);
    }

    default ErpWholesaleCombinedDO selectByNo(String no) {
        return selectOne(ErpWholesaleCombinedDO::getNo, no);
    }

    default List<ErpWholesaleCombinedDO> selectCombinedList(ErpWholesaleCombinedPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpWholesaleCombinedDO> query = new MPJLambdaWrapperX<ErpWholesaleCombinedDO>()
                .orderByDesc(ErpWholesaleCombinedDO::getId);
        return selectList(query);
    }
}
