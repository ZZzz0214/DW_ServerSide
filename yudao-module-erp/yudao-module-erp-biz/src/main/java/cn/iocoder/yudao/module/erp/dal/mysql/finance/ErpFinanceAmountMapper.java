package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.ErpFinanceAmountPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.ErpFinanceAmountRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAmountDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpFinanceAmountMapper extends BaseMapperX<ErpFinanceAmountDO> {

    default PageResult<ErpFinanceAmountRespVO> selectPage(ErpFinanceAmountPageReqVO reqVO, String currentUsername) {
        MPJLambdaWrapperX<ErpFinanceAmountDO> query = new MPJLambdaWrapperX<ErpFinanceAmountDO>()
                .likeIfPresent(ErpFinanceAmountDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpFinanceAmountDO::getChannelType, reqVO.getChannelType())
                .eqIfPresent(ErpFinanceAmountDO::getOperationType, reqVO.getOperationType())
                .betweenIfPresent(ErpFinanceAmountDO::getOrderDate, reqVO.getOrderDate())
                .eqIfPresent(ErpFinanceAmountDO::getAuditStatus, reqVO.getAuditStatus())
                .likeIfPresent(ErpFinanceAmountDO::getCreator, reqVO.getCreator())
                .likeIfPresent(ErpFinanceAmountDO::getAuditor, reqVO.getAuditor())
                .betweenIfPresent(ErpFinanceAmountDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(ErpFinanceAmountDO::getAuditTime, reqVO.getAuditTime());
        
        // 权限控制：admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"admin".equals(currentUsername)) {
            query.eq(ErpFinanceAmountDO::getCreator, currentUsername);
        }
        
        // 处理旧查询逻辑转换为新的业务逻辑
        // 当有具体渠道充值查询条件时，使用OR条件组合
        boolean hasRechargeQuery = reqVO.getWechatRecharge() != null || reqVO.getAlipayRecharge() != null || reqVO.getBankCardRecharge() != null;
        boolean hasBalanceQuery = reqVO.getWechatBalance() != null || reqVO.getAlipayBalance() != null || reqVO.getBankCardBalance() != null;
        
        if (hasRechargeQuery || hasBalanceQuery) {
            query.and(wrapper -> {
                // 充值金额查询
                if (reqVO.getWechatRecharge() != null) {
                    wrapper.or(w -> w.eq(ErpFinanceAmountDO::getChannelType, "微信")
                                   .eq(ErpFinanceAmountDO::getOperationType, 1)
                                   .eq(ErpFinanceAmountDO::getAmount, reqVO.getWechatRecharge()));
                }
                if (reqVO.getAlipayRecharge() != null) {
                    wrapper.or(w -> w.eq(ErpFinanceAmountDO::getChannelType, "支付宝")
                                   .eq(ErpFinanceAmountDO::getOperationType, 1)
                                   .eq(ErpFinanceAmountDO::getAmount, reqVO.getAlipayRecharge()));
                }
                if (reqVO.getBankCardRecharge() != null) {
                    wrapper.or(w -> w.eq(ErpFinanceAmountDO::getChannelType, "银行卡")
                                   .eq(ErpFinanceAmountDO::getOperationType, 1)
                                   .eq(ErpFinanceAmountDO::getAmount, reqVO.getBankCardRecharge()));
                }
                // 余额查询
                if (reqVO.getWechatBalance() != null) {
                    wrapper.or(w -> w.eq(ErpFinanceAmountDO::getChannelType, "微信")
                                   .eq(ErpFinanceAmountDO::getAfterBalance, reqVO.getWechatBalance()));
                }
                if (reqVO.getAlipayBalance() != null) {
                    wrapper.or(w -> w.eq(ErpFinanceAmountDO::getChannelType, "支付宝")
                                   .eq(ErpFinanceAmountDO::getAfterBalance, reqVO.getAlipayBalance()));
                }
                if (reqVO.getBankCardBalance() != null) {
                    wrapper.or(w -> w.eq(ErpFinanceAmountDO::getChannelType, "银行卡")
                                   .eq(ErpFinanceAmountDO::getAfterBalance, reqVO.getBankCardBalance()));
                }
            });
        }
        
        query.orderByDesc(ErpFinanceAmountDO::getId)
                // 字段映射
                .selectAs(ErpFinanceAmountDO::getId, ErpFinanceAmountRespVO::getId)
                .selectAs(ErpFinanceAmountDO::getNo, ErpFinanceAmountRespVO::getNo)
                .selectAs(ErpFinanceAmountDO::getCarouselImages, ErpFinanceAmountRespVO::getCarouselImages)
                .selectAs(ErpFinanceAmountDO::getChannelType, ErpFinanceAmountRespVO::getChannelType)
                .selectAs(ErpFinanceAmountDO::getAmount, ErpFinanceAmountRespVO::getAmount)
                .selectAs(ErpFinanceAmountDO::getOperationType, ErpFinanceAmountRespVO::getOperationType)
                .selectAs(ErpFinanceAmountDO::getBeforeBalance, ErpFinanceAmountRespVO::getBeforeBalance)
                .selectAs(ErpFinanceAmountDO::getAfterBalance, ErpFinanceAmountRespVO::getAfterBalance)
                .selectAs(ErpFinanceAmountDO::getOrderDate, ErpFinanceAmountRespVO::getOrderDate)
                .selectAs(ErpFinanceAmountDO::getRemark, ErpFinanceAmountRespVO::getRemark)
                .selectAs(ErpFinanceAmountDO::getAuditStatus, ErpFinanceAmountRespVO::getAuditStatus)
                .selectAs(ErpFinanceAmountDO::getAuditor, ErpFinanceAmountRespVO::getAuditor)
                .selectAs(ErpFinanceAmountDO::getAuditTime, ErpFinanceAmountRespVO::getAuditTime)
                .selectAs(ErpFinanceAmountDO::getAuditRemark, ErpFinanceAmountRespVO::getAuditRemark)
                .selectAs(ErpFinanceAmountDO::getCreator, ErpFinanceAmountRespVO::getCreator)
                .selectAs(ErpFinanceAmountDO::getCreateTime, ErpFinanceAmountRespVO::getCreateTime)
                // 兼容字段 - 根据实际数据动态计算
                .selectAs(ErpFinanceAmountDO::getWechatRecharge, ErpFinanceAmountRespVO::getWechatRecharge)
                .selectAs(ErpFinanceAmountDO::getAlipayRecharge, ErpFinanceAmountRespVO::getAlipayRecharge)
                .selectAs(ErpFinanceAmountDO::getBankCardRecharge, ErpFinanceAmountRespVO::getBankCardRecharge)
                .selectAs(ErpFinanceAmountDO::getWechatBalance, ErpFinanceAmountRespVO::getWechatBalance)
                .selectAs(ErpFinanceAmountDO::getAlipayBalance, ErpFinanceAmountRespVO::getAlipayBalance)
                .selectAs(ErpFinanceAmountDO::getBankCardBalance, ErpFinanceAmountRespVO::getBankCardBalance);

        return selectJoinPage(reqVO, ErpFinanceAmountRespVO.class, query);
    }

    default List<ErpFinanceAmountDO> selectListByNoIn(Collection<String> nos) {
        if (CollUtil.isEmpty(nos)) {
            return Collections.emptyList();
        }
        return selectList(ErpFinanceAmountDO::getNo, nos);
    }

    default ErpFinanceAmountDO selectByNo(String no) {
        return selectOne(ErpFinanceAmountDO::getNo, no);
    }

    default ErpFinanceAmountDO selectByCreator(String creator) {
        List<ErpFinanceAmountDO> list = selectList(ErpFinanceAmountDO::getCreator, creator);
        // 如果有多条记录，返回最新的一条（按ID降序）
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        // 按ID降序排序，取最新的一条记录
        return list.stream()
                .max((a, b) -> Long.compare(a.getId(), b.getId()))
                .orElse(null);
    }

    default List<ErpFinanceAmountDO> selectListByCreator(String creator) {
        return selectList(ErpFinanceAmountDO::getCreator, creator);
    }
} 