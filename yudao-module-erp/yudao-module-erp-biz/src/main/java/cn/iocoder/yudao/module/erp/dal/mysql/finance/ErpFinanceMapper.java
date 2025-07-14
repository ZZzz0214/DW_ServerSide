package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.ErpFinancePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.ErpFinanceRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        if (!"ahao".equals(currentUsername) &&!"caiwu".equals(currentUsername) && !"admin".equals(currentUsername)) {
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

    /**
     * 根据审核状态和时间范围统计数量
     *
     * @param auditStatus 审核状态（10：未审核，20：已审核）
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 数量
     */
    default Long selectCountByAuditStatusAndTimeRange(Integer auditStatus, LocalDateTime beginTime, LocalDateTime endTime) {
        return selectCount(new MPJLambdaWrapperX<ErpFinanceDO>()
                .eq(ErpFinanceDO::getAuditStatus, auditStatus)
                .between(ErpFinanceDO::getOrderDate, beginTime.toLocalDate(), endTime.toLocalDate()));
    }

    /**
     * 根据审核状态、收入支出类型和时间范围统计金额
     *
     * @param auditStatus 审核状态（10：未审核，20：已审核）
     * @param incomeExpense 收入支出类型（1：收入，2：支出）
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 金额
     */
    default BigDecimal selectSumByAuditStatusAndIncomeExpenseAndTimeRange(Integer auditStatus, Integer incomeExpense, LocalDateTime beginTime, LocalDateTime endTime) {
        return selectJoinOne(BigDecimal.class, new MPJLambdaWrapperX<ErpFinanceDO>()
                .eq(ErpFinanceDO::getAuditStatus, auditStatus)
                .eq(ErpFinanceDO::getIncomeExpense, incomeExpense)
                .between(ErpFinanceDO::getOrderDate, beginTime.toLocalDate(), endTime.toLocalDate())
                .selectSum(ErpFinanceDO::getAmount));
    }
}
