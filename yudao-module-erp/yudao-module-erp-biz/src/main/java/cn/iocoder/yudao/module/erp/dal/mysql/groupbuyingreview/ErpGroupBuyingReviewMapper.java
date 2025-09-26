package cn.iocoder.yudao.module.erp.dal.mysql.groupbuyingreview;


import cn.hutool.core.collection.CollUtil;
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
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpGroupBuyingReviewMapper extends BaseMapperX<ErpGroupBuyingReviewDO> {

    default PageResult<ErpGroupBuyingReviewRespVO> selectPage(ErpGroupBuyingReviewPageReqVO reqVO, String currentUsername) {
        MPJLambdaWrapperX<ErpGroupBuyingReviewDO> query = new MPJLambdaWrapperX<ErpGroupBuyingReviewDO>()
                .likeIfPresent(ErpGroupBuyingReviewDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpGroupBuyingReviewDO::getSupplyGroupPrice, reqVO.getSupplyGroupPrice())
                .likeIfPresent(ErpGroupBuyingReviewDO::getExpressFee, reqVO.getExpressFee())
                .likeIfPresent(ErpGroupBuyingReviewDO::getGroupPrice, reqVO.getGroupPrice())
                .betweenIfPresent(ErpGroupBuyingReviewDO::getSampleSendDate, reqVO.getSampleSendDate())
                .betweenIfPresent(ErpGroupBuyingReviewDO::getGroupStartDate, reqVO.getGroupStartDate())
                .likeIfPresent(ErpGroupBuyingReviewDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(ErpGroupBuyingReviewDO::getCreateTime, reqVO.getCreateTime());

        // 权限控制：admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"ahao".equals(currentUsername) &&!"caiwu".equals(currentUsername) && !"admin".equals(currentUsername)) {
            query.eq(ErpGroupBuyingReviewDO::getCreator, currentUsername);
        }

        query.orderByDesc(ErpGroupBuyingReviewDO::getId)
                // 团购复盘表字段映射
                .selectAs(ErpGroupBuyingReviewDO::getId, ErpGroupBuyingReviewRespVO::getId)
                .selectAs(ErpGroupBuyingReviewDO::getNo, ErpGroupBuyingReviewRespVO::getNo)
                .selectAs(ErpGroupBuyingReviewDO::getRemark, ErpGroupBuyingReviewRespVO::getRemark)
                .selectAs(ErpGroupBuyingReviewDO::getSupplyGroupPrice, ErpGroupBuyingReviewRespVO::getSupplyGroupPrice)
                .selectAs(ErpGroupBuyingReviewDO::getExpressFee, ErpGroupBuyingReviewRespVO::getExpressFee)
                .selectAs(ErpGroupBuyingReviewDO::getGroupPrice, ErpGroupBuyingReviewRespVO::getGroupPrice)
                .selectAs(ErpGroupBuyingReviewDO::getSampleSendDate, ErpGroupBuyingReviewRespVO::getSampleSendDate)
                .selectAs(ErpGroupBuyingReviewDO::getGroupStartDate, ErpGroupBuyingReviewRespVO::getGroupStartDate)
                .selectAs(ErpGroupBuyingReviewDO::getGroupSales, ErpGroupBuyingReviewRespVO::getGroupSales)
                .selectAs(ErpGroupBuyingReviewDO::getRepeatGroupDate, ErpGroupBuyingReviewRespVO::getRepeatGroupDate)
                .selectAs(ErpGroupBuyingReviewDO::getRepeatGroupSales, ErpGroupBuyingReviewRespVO::getRepeatGroupSales)
                .selectAs(ErpGroupBuyingReviewDO::getGroupBuyingId, ErpGroupBuyingReviewRespVO::getGroupBuyingId)
                .selectAs(ErpGroupBuyingReviewDO::getCustomerId, ErpGroupBuyingReviewRespVO::getCustomerId)
                .selectAs(ErpGroupBuyingReviewDO::getCreator, ErpGroupBuyingReviewRespVO::getCreator)
                .selectAs(ErpGroupBuyingReviewDO::getCreateTime, ErpGroupBuyingReviewRespVO::getCreateTime);

        // 联表查询团购货盘信息
        query.leftJoin(ErpGroupBuyingDO.class, ErpGroupBuyingDO::getNo, ErpGroupBuyingReviewDO::getGroupBuyingId);

        // 团购货盘搜索条件
        if (reqVO.getGroupBuyingNo() != null && !reqVO.getGroupBuyingNo().isEmpty()) {
            query.like(ErpGroupBuyingDO::getNo, reqVO.getGroupBuyingNo());
        }
        if (reqVO.getBrandName() != null && !reqVO.getBrandName().isEmpty()) {
            query.like(ErpGroupBuyingDO::getBrandName, reqVO.getBrandName());
        }
        if (reqVO.getProductName() != null && !reqVO.getProductName().isEmpty()) {
            query.like(ErpGroupBuyingDO::getProductName, reqVO.getProductName());
        }
        if (reqVO.getProductSpec() != null && !reqVO.getProductSpec().isEmpty()) {
            query.like(ErpGroupBuyingDO::getProductSpec, reqVO.getProductSpec());
        }
        if (reqVO.getProductSku() != null && !reqVO.getProductSku().isEmpty()) {
            query.like(ErpGroupBuyingDO::getProductSku, reqVO.getProductSku());
        }
        if (reqVO.getStatus() != null && !reqVO.getStatus().isEmpty()) {
            query.eq(ErpGroupBuyingDO::getStatus, reqVO.getStatus());
        }

        query.selectAs(ErpGroupBuyingDO::getBrandName, ErpGroupBuyingReviewRespVO::getBrandName)
                .selectAs(ErpGroupBuyingDO::getNo, ErpGroupBuyingReviewRespVO::getGroupBuyingNo)
                .selectAs(ErpGroupBuyingDO::getProductName, ErpGroupBuyingReviewRespVO::getProductName)
                .selectAs(ErpGroupBuyingDO::getProductSpec, ErpGroupBuyingReviewRespVO::getProductSpec)
                .selectAs(ErpGroupBuyingDO::getProductSku, ErpGroupBuyingReviewRespVO::getProductSku)
                .selectAs(ErpGroupBuyingDO::getGroupMechanism, ErpGroupBuyingReviewRespVO::getGroupMechanism)
                .selectAs(ErpGroupBuyingDO::getStatus, ErpGroupBuyingReviewRespVO::getStatus);

        // 联表查询客户信息
        query.leftJoin(ErpCustomerDO.class, ErpCustomerDO::getName, ErpGroupBuyingReviewDO::getCustomerId);

        // 客户名称搜索条件
        if (reqVO.getCustomerName() != null && !reqVO.getCustomerName().isEmpty()) {
            query.like(ErpCustomerDO::getName, reqVO.getCustomerName());
        }

        query.selectAs(ErpCustomerDO::getName, ErpGroupBuyingReviewRespVO::getCustomerName);

        return selectJoinPage(reqVO, ErpGroupBuyingReviewRespVO.class, query);
    }

    default ErpGroupBuyingReviewDO selectByNo(String no) {
        return selectOne(ErpGroupBuyingReviewDO::getNo, no);
    }

    default List<ErpGroupBuyingReviewDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpGroupBuyingReviewDO::getNo, nos);
    }

    default List<ErpGroupBuyingReviewRespVO> selectListByIds(Collection<Long> ids) {
        if (cn.hutool.core.collection.CollUtil.isEmpty(ids)) {
            return java.util.Collections.emptyList();
        }

        MPJLambdaWrapperX<ErpGroupBuyingReviewDO> query = new MPJLambdaWrapperX<ErpGroupBuyingReviewDO>()
                .in(ErpGroupBuyingReviewDO::getId, ids)
                .orderByDesc(ErpGroupBuyingReviewDO::getId)
                // 团购复盘表字段映射
                .selectAs(ErpGroupBuyingReviewDO::getId, ErpGroupBuyingReviewRespVO::getId)
                .selectAs(ErpGroupBuyingReviewDO::getNo, ErpGroupBuyingReviewRespVO::getNo)
                .selectAs(ErpGroupBuyingReviewDO::getRemark, ErpGroupBuyingReviewRespVO::getRemark)
                .selectAs(ErpGroupBuyingReviewDO::getSupplyGroupPrice, ErpGroupBuyingReviewRespVO::getSupplyGroupPrice)
                .selectAs(ErpGroupBuyingReviewDO::getExpressFee, ErpGroupBuyingReviewRespVO::getExpressFee)
                .selectAs(ErpGroupBuyingReviewDO::getGroupPrice, ErpGroupBuyingReviewRespVO::getGroupPrice)
                .selectAs(ErpGroupBuyingReviewDO::getSampleSendDate, ErpGroupBuyingReviewRespVO::getSampleSendDate)
                .selectAs(ErpGroupBuyingReviewDO::getGroupStartDate, ErpGroupBuyingReviewRespVO::getGroupStartDate)
                .selectAs(ErpGroupBuyingReviewDO::getGroupSales, ErpGroupBuyingReviewRespVO::getGroupSales)
                .selectAs(ErpGroupBuyingReviewDO::getRepeatGroupDate, ErpGroupBuyingReviewRespVO::getRepeatGroupDate)
                .selectAs(ErpGroupBuyingReviewDO::getRepeatGroupSales, ErpGroupBuyingReviewRespVO::getRepeatGroupSales)
                .selectAs(ErpGroupBuyingReviewDO::getGroupBuyingId, ErpGroupBuyingReviewRespVO::getGroupBuyingId)
                .selectAs(ErpGroupBuyingReviewDO::getCustomerId, ErpGroupBuyingReviewRespVO::getCustomerId)
                .selectAs(ErpGroupBuyingReviewDO::getCreator, ErpGroupBuyingReviewRespVO::getCreator)
                .selectAs(ErpGroupBuyingReviewDO::getCreateTime, ErpGroupBuyingReviewRespVO::getCreateTime);

        // 联表查询团购货盘信息
        query.leftJoin(ErpGroupBuyingDO.class, ErpGroupBuyingDO::getNo, ErpGroupBuyingReviewDO::getGroupBuyingId)
                .selectAs(ErpGroupBuyingDO::getBrandName, ErpGroupBuyingReviewRespVO::getBrandName)
                .selectAs(ErpGroupBuyingDO::getNo, ErpGroupBuyingReviewRespVO::getGroupBuyingNo)
                .selectAs(ErpGroupBuyingDO::getProductName, ErpGroupBuyingReviewRespVO::getProductName)
                .selectAs(ErpGroupBuyingDO::getProductSpec, ErpGroupBuyingReviewRespVO::getProductSpec)
                .selectAs(ErpGroupBuyingDO::getProductSku, ErpGroupBuyingReviewRespVO::getProductSku)
                .selectAs(ErpGroupBuyingDO::getGroupMechanism, ErpGroupBuyingReviewRespVO::getGroupMechanism)
                .selectAs(ErpGroupBuyingDO::getStatus, ErpGroupBuyingReviewRespVO::getStatus);

        // 联表查询客户信息
        query.leftJoin(ErpCustomerDO.class, ErpCustomerDO::getName, ErpGroupBuyingReviewDO::getCustomerId)
                .selectAs(ErpCustomerDO::getName, ErpGroupBuyingReviewRespVO::getCustomerName);

        return selectJoinList(ErpGroupBuyingReviewRespVO.class, query);
    }

    default void insertBatch(List<ErpGroupBuyingReviewDO> list) {
        list.forEach(this::insert);
    }

    default ErpGroupBuyingReviewDO selectByGroupBuyingIdAndCustomerId(String groupBuyingId, String customerId) {
        return selectOne(new MPJLambdaWrapperX<ErpGroupBuyingReviewDO>()
                .eq(ErpGroupBuyingReviewDO::getGroupBuyingId, groupBuyingId)
                .eq(ErpGroupBuyingReviewDO::getCustomerId, customerId));
    }

    /**
     * 根据团购货盘ID和客户ID查询记录，排除指定ID的记录（用于更新时的重复校验）
     */
    default ErpGroupBuyingReviewDO selectByGroupBuyingIdAndCustomerIdExcludeId(String groupBuyingId, String customerId, Long excludeId) {
//        return selectOne(new MPJLambdaWrapperX<ErpGroupBuyingReviewDO>()
//                .eq(ErpGroupBuyingReviewDO::getGroupBuyingId, groupBuyingId)
//                .eq(ErpGroupBuyingReviewDO::getCustomerId, customerId)
//                .ne(ErpGroupBuyingReviewDO::getId, excludeId));
        // 构建查询条件
        MPJLambdaWrapperX<ErpGroupBuyingReviewDO> wrapper = new MPJLambdaWrapperX<>();
        wrapper.eq(ErpGroupBuyingReviewDO::getGroupBuyingId, groupBuyingId)
                .eq(ErpGroupBuyingReviewDO::getCustomerId, customerId)
                .ne(ErpGroupBuyingReviewDO::getId, excludeId);

        // 打印查询参数
        System.out.println("[DEBUG] 查询拼团评价 - 参数: 拼团ID=" + groupBuyingId +
                ", 客户ID=" + customerId +
                ", 排除ID=" + excludeId);

        // 执行查询
        ErpGroupBuyingReviewDO result = selectOne(wrapper);

        // 打印查询结果
        if (result == null) {
            System.out.println("[INFO] 未找到匹配数据 - 参数: 拼团ID=" + groupBuyingId +
                    ", 客户ID=" + customerId +
                    ", 排除ID=" + excludeId);
        } else {
            System.out.println("[DEBUG] 找到数据 - ID=" + result.getId());
        }

        return result;
    }

    default List<ErpGroupBuyingReviewDO> selectListByGroupBuyingIdAndCustomerIdIn(Collection<String> groupBuyingIds, Collection<String> customerIds) {
        if (CollUtil.isEmpty(groupBuyingIds) || CollUtil.isEmpty(customerIds)) {
            return Collections.emptyList();
        }
        return selectList(new MPJLambdaWrapperX<ErpGroupBuyingReviewDO>()
                .in(ErpGroupBuyingReviewDO::getGroupBuyingId, groupBuyingIds)
                .in(ErpGroupBuyingReviewDO::getCustomerId, customerIds));
    }
}
