package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.ErpFinancePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.ErpFinanceRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpFinanceMapper extends BaseMapperX<ErpFinanceDO> {

    default PageResult<ErpFinanceRespVO> selectPage(ErpFinancePageReqVO reqVO, String currentUsername) {
        MPJLambdaWrapperX<ErpFinanceDO> query = new MPJLambdaWrapperX<ErpFinanceDO>()
                .likeIfPresent(ErpFinanceDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpFinanceDO::getBillName, reqVO.getBillName())
                .eqIfPresent(ErpFinanceDO::getIncomeExpense, reqVO.getIncomeExpense())
                .likeIfPresent(ErpFinanceDO::getCategory, reqVO.getCategory())
                .likeIfPresent(ErpFinanceDO::getAccount, reqVO.getAccount())
                .eqIfPresent(ErpFinanceDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ErpFinanceDO::getAuditStatus, reqVO.getAuditStatus())
                .likeIfPresent(ErpFinanceDO::getCreator, reqVO.getCreator())
                .likeIfPresent(ErpFinanceDO::getAuditor, reqVO.getAuditor())
                .betweenIfPresent(ErpFinanceDO::getOrderDate, reqVO.getOrderDate())
                .betweenIfPresent(ErpFinanceDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(ErpFinanceDO::getAuditTime, reqVO.getAuditTime());
        
        // 权限控制：admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"admin".equals(currentUsername)) {
            query.eq(ErpFinanceDO::getCreator, currentUsername);
        }
        
        query.orderByDesc(ErpFinanceDO::getId)
                // 字段映射
                .selectAs(ErpFinanceDO::getId, ErpFinanceRespVO::getId)
                .selectAs(ErpFinanceDO::getNo, ErpFinanceRespVO::getNo)
                .selectAs(ErpFinanceDO::getCarouselImages, ErpFinanceRespVO::getCarouselImages)
                .selectAs(ErpFinanceDO::getBillName, ErpFinanceRespVO::getBillName)
                .selectAs(ErpFinanceDO::getAmount, ErpFinanceRespVO::getAmount)
                .selectAs(ErpFinanceDO::getIncomeExpense, ErpFinanceRespVO::getIncomeExpense)
                .selectAs(ErpFinanceDO::getCategory, ErpFinanceRespVO::getCategory)
                .selectAs(ErpFinanceDO::getAccount, ErpFinanceRespVO::getAccount)
                .selectAs(ErpFinanceDO::getStatus, ErpFinanceRespVO::getStatus)
                .selectAs(ErpFinanceDO::getRemark, ErpFinanceRespVO::getRemark)
                .selectAs(ErpFinanceDO::getOrderDate, ErpFinanceRespVO::getOrderDate)
                .selectAs(ErpFinanceDO::getAuditStatus, ErpFinanceRespVO::getAuditStatus)
                .selectAs(ErpFinanceDO::getAuditor, ErpFinanceRespVO::getAuditor)
                .selectAs(ErpFinanceDO::getAuditTime, ErpFinanceRespVO::getAuditTime)
                .selectAs(ErpFinanceDO::getAuditRemark, ErpFinanceRespVO::getAuditRemark)
                .selectAs(ErpFinanceDO::getCreator, ErpFinanceRespVO::getCreator)
                .selectAs(ErpFinanceDO::getCreateTime, ErpFinanceRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpFinanceRespVO.class, query);
    }

    default List<ErpFinanceDO> selectListByNoIn(Collection<String> nos) {
        if (CollUtil.isEmpty(nos)) {
            return Collections.emptyList();
        }
        return selectList(ErpFinanceDO::getNo, nos);
    }

    default ErpFinanceDO selectByNo(String no) {
        return selectOne(ErpFinanceDO::getNo, no);
    }
} 