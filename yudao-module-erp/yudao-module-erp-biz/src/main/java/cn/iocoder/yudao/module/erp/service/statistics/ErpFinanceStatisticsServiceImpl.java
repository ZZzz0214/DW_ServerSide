package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.finance.ErpFinanceSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.finance.ErpFinanceAmountSummaryRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAmountDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceAmountMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 财务统计 Service 实现类
 */
@Service
@Slf4j
public class ErpFinanceStatisticsServiceImpl implements ErpFinanceStatisticsService {

    @Resource
    private ErpFinanceMapper financeMapper;

    @Resource
    private ErpFinanceAmountMapper financeAmountMapper;

    @Override
    public ErpFinanceSummaryRespVO getFinanceSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        log.info("开始获取财务表统计，时间范围：{} 到 {}", beginTime, endTime);
        
        ErpFinanceSummaryRespVO summary = new ErpFinanceSummaryRespVO();

        try {
            // 调试：先查询所有数据
            Long totalCount = financeMapper.selectCount();
            log.info("财务表总数据量：{}", totalCount);
            
            // 调试：查询所有审核状态的数据
            Long count10 = financeMapper.selectCount(ErpFinanceDO::getAuditStatus, 10);
            Long count20 = financeMapper.selectCount(ErpFinanceDO::getAuditStatus, 20);
            log.info("财务表审核状态统计 - 未审核(10): {}, 已审核(20): {}", count10, count20);
            
            // 统计未审核数量
            Long unauditedCount = financeMapper.selectCountByAuditStatusAndTimeRange(10, beginTime, endTime);
            summary.setUnauditedCount(unauditedCount != null ? unauditedCount : 0L);
            log.debug("未审核数量：{}", unauditedCount);
            
            // 统计已审核数量
            Long auditedCount = financeMapper.selectCountByAuditStatusAndTimeRange(20, beginTime, endTime);
            summary.setAuditedCount(auditedCount != null ? auditedCount : 0L);
            log.debug("已审核数量：{}", auditedCount);

            // 调试：查询所有收入支出类型的数据
            Long incomeCount = financeMapper.selectCount(ErpFinanceDO::getIncomeExpense, 1);
            Long expenseCount = financeMapper.selectCount(ErpFinanceDO::getIncomeExpense, 2);
            log.info("财务表收入支出统计 - 收入(1): {}, 支出(2): {}", incomeCount, expenseCount);

            // 统计未审核支出金额
            BigDecimal unauditedExpense = financeMapper.selectSumByAuditStatusAndIncomeExpenseAndTimeRange(10, 2, beginTime, endTime);
            summary.setUnauditedExpense(unauditedExpense != null ? unauditedExpense : BigDecimal.ZERO);
            log.debug("未审核支出：{}", unauditedExpense);

            // 统计未审核收入金额
            BigDecimal unauditedIncome = financeMapper.selectSumByAuditStatusAndIncomeExpenseAndTimeRange(10, 1, beginTime, endTime);
            summary.setUnauditedIncome(unauditedIncome != null ? unauditedIncome : BigDecimal.ZERO);
            log.debug("未审核收入：{}", unauditedIncome);

            // 统计已审核支出金额
            BigDecimal auditedExpense = financeMapper.selectSumByAuditStatusAndIncomeExpenseAndTimeRange(20, 2, beginTime, endTime);
            summary.setAuditedExpense(auditedExpense != null ? auditedExpense : BigDecimal.ZERO);
            log.debug("已审核支出：{}", auditedExpense);

            // 统计已审核收入金额
            BigDecimal auditedIncome = financeMapper.selectSumByAuditStatusAndIncomeExpenseAndTimeRange(20, 1, beginTime, endTime);
            summary.setAuditedIncome(auditedIncome != null ? auditedIncome : BigDecimal.ZERO);
            log.debug("已审核收入：{}", auditedIncome);

            // 计算总支出和总收入
            summary.setTotalExpense(summary.getUnauditedExpense().add(summary.getAuditedExpense()));
            summary.setTotalIncome(summary.getUnauditedIncome().add(summary.getAuditedIncome()));

            log.info("财务表统计获取完成：{}", summary);
            return summary;
        } catch (Exception e) {
            log.error("获取财务表统计失败", e);
            throw e;
        }
    }

    @Override
    public ErpFinanceAmountSummaryRespVO getFinanceAmountSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        log.info("开始获取财务金额表统计，时间范围：{} 到 {}", beginTime, endTime);
        
        ErpFinanceAmountSummaryRespVO summary = new ErpFinanceAmountSummaryRespVO();

        try {
            // 调试：先查询所有数据
            Long totalCount = financeAmountMapper.selectCount();
            log.info("财务金额表总数据量：{}", totalCount);
            
            // 调试：查询所有审核状态的数据
            Long count10 = financeAmountMapper.selectCount(ErpFinanceAmountDO::getAuditStatus, 10);
            Long count20 = financeAmountMapper.selectCount(ErpFinanceAmountDO::getAuditStatus, 20);
            log.info("财务金额表审核状态统计 - 未审核(10): {}, 已审核(20): {}", count10, count20);
            
            // 调试：查询所有渠道类型的数据
            Long wechatCount = financeAmountMapper.selectCount(ErpFinanceAmountDO::getChannelType, "微信");
            Long alipayCount = financeAmountMapper.selectCount(ErpFinanceAmountDO::getChannelType, "支付宝");
            Long bankCardCount = financeAmountMapper.selectCount(ErpFinanceAmountDO::getChannelType, "银行卡");
            log.info("财务金额表渠道统计 - 微信: {}, 支付宝: {}, 银行卡: {}", wechatCount, alipayCount, bankCardCount);
            
            // 统计未审核数量
            Long unauditedCount = financeAmountMapper.selectCountByAuditStatusAndTimeRange(10, beginTime, endTime);
            summary.setUnauditedCount(unauditedCount != null ? unauditedCount : 0L);
            log.debug("未审核数量：{}", unauditedCount);
            
            // 统计已审核数量
            Long auditedCount = financeAmountMapper.selectCountByAuditStatusAndTimeRange(20, beginTime, endTime);
            summary.setAuditedCount(auditedCount != null ? auditedCount : 0L);
            log.debug("已审核数量：{}", auditedCount);

            // 统计微信充值数量和总额
            Long wechatCount2 = financeAmountMapper.selectCountByChannelTypeAndTimeRange("微信", beginTime, endTime);
            summary.setWechatCount(wechatCount2 != null ? wechatCount2 : 0L);
            BigDecimal wechatTotal = financeAmountMapper.selectSumByChannelTypeAndTimeRange("微信", beginTime, endTime);
            summary.setWechatTotal(wechatTotal != null ? wechatTotal : BigDecimal.ZERO);
            log.debug("微信充值数量：{}，总额：{}", wechatCount2, wechatTotal);

            // 统计支付宝充值数量和总额
            Long alipayCount2 = financeAmountMapper.selectCountByChannelTypeAndTimeRange("支付宝", beginTime, endTime);
            summary.setAlipayCount(alipayCount2 != null ? alipayCount2 : 0L);
            BigDecimal alipayTotal = financeAmountMapper.selectSumByChannelTypeAndTimeRange("支付宝", beginTime, endTime);
            summary.setAlipayTotal(alipayTotal != null ? alipayTotal : BigDecimal.ZERO);
            log.debug("支付宝充值数量：{}，总额：{}", alipayCount2, alipayTotal);

            // 统计银行卡充值数量和总额
            Long bankCardCount2 = financeAmountMapper.selectCountByChannelTypeAndTimeRange("银行卡", beginTime, endTime);
            summary.setBankCardCount(bankCardCount2 != null ? bankCardCount2 : 0L);
            BigDecimal bankCardTotal = financeAmountMapper.selectSumByChannelTypeAndTimeRange("银行卡", beginTime, endTime);
            summary.setBankCardTotal(bankCardTotal != null ? bankCardTotal : BigDecimal.ZERO);
            log.debug("银行卡充值数量：{}，总额：{}", bankCardCount2, bankCardTotal);

            // 计算充值总金额
            summary.setTotalAmount(summary.getWechatTotal().add(summary.getAlipayTotal()).add(summary.getBankCardTotal()));

            log.info("财务金额表统计获取完成：{}", summary);
            return summary;
        } catch (Exception e) {
            log.error("获取财务金额表统计失败", e);
            throw e;
        }
    }

} 