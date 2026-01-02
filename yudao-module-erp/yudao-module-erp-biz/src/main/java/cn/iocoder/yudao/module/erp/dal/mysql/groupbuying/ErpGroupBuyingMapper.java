package cn.iocoder.yudao.module.erp.dal.mysql.groupbuying;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpGroupBuyingMapper extends BaseMapperX<ErpGroupBuyingDO> {

    default PageResult<ErpGroupBuyingRespVO> selectPage(ErpGroupBuyingPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpGroupBuyingDO> query = new MPJLambdaWrapperX<ErpGroupBuyingDO>()
                .likeIfPresent(ErpGroupBuyingDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpGroupBuyingDO::getCategoryId, reqVO.getCategoryId())
                .inIfPresent(ErpGroupBuyingDO::getCategoryId, reqVO.getCategoryIds()) // 产品分类多选
                .likeIfPresent(ErpGroupBuyingDO::getProductName, reqVO.getProductName());
        
        // 品牌名称筛选：支持多选和为空筛选（可以同时选择多个值和为空）
        if (CollUtil.isNotEmpty(reqVO.getBrandNames()) || Boolean.TRUE.equals(reqVO.getBrandNameEmpty())) {
            query.and(w -> {
                boolean hasCondition = false;
                if (CollUtil.isNotEmpty(reqVO.getBrandNames())) {
                    w.in(ErpGroupBuyingDO::getBrandName, reqVO.getBrandNames());
                    hasCondition = true;
                }
                if (Boolean.TRUE.equals(reqVO.getBrandNameEmpty())) {
                    if (hasCondition) {
                        w.or();
                    }
                    w.and(empty -> empty.isNull(ErpGroupBuyingDO::getBrandName).or().eq(ErpGroupBuyingDO::getBrandName, ""));
                }
            });
        } else {
            query.eqIfPresent(ErpGroupBuyingDO::getBrandName, reqVO.getBrandName());
        }
        
        query.likeIfPresent(ErpGroupBuyingDO::getProductSpec, reqVO.getProductSpec())
                .betweenIfPresent(ErpGroupBuyingDO::getShelfLife, reqVO.getShelfLife())
                .likeIfPresent(ErpGroupBuyingDO::getSupplyGroupPrice, reqVO.getSupplyGroupPrice())
                .likeIfPresent(ErpGroupBuyingDO::getSellingCommission, reqVO.getSellingCommission())
                .likeIfPresent(ErpGroupBuyingDO::getGroupPrice, reqVO.getGroupPrice());
        
        // 货盘状态筛选：支持多选和为空筛选（可以同时选择多个值和为空）
        // 注意：数据库中状态可能存储为逗号分隔的多个值（如 "上架,热卖"），需要使用 LIKE 查询
        if (CollUtil.isNotEmpty(reqVO.getStatuses()) || Boolean.TRUE.equals(reqVO.getStatusEmpty())) {
            query.and(w -> {
                boolean hasCondition = false;
                if (CollUtil.isNotEmpty(reqVO.getStatuses())) {
                    // 使用 OR 连接多个状态的 LIKE 查询
                    w.nested(nested -> {
                        for (int i = 0; i < reqVO.getStatuses().size(); i++) {
                            if (i > 0) {
                                nested.or();
                            }
                            String status = reqVO.getStatuses().get(i);
                            // 使用 LIKE 查询支持逗号分隔的多状态
                            nested.and(like -> like
                                .eq(ErpGroupBuyingDO::getStatus, status)  // 完全匹配单个状态
                                .or().like(ErpGroupBuyingDO::getStatus, status + ",%")  // 匹配开头
                                .or().like(ErpGroupBuyingDO::getStatus, "%," + status + ",%")  // 匹配中间
                                .or().like(ErpGroupBuyingDO::getStatus, "%," + status)  // 匹配结尾
                            );
                        }
                    });
                    hasCondition = true;
                }
                if (Boolean.TRUE.equals(reqVO.getStatusEmpty())) {
                    if (hasCondition) {
                        w.or();
                    }
                    w.and(empty -> empty.isNull(ErpGroupBuyingDO::getStatus).or().eq(ErpGroupBuyingDO::getStatus, ""));
                }
            });
        } else {
            query.eqIfPresent(ErpGroupBuyingDO::getStatus, reqVO.getStatus());
        }
        
        query.likeIfPresent(ErpGroupBuyingDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(ErpGroupBuyingDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpGroupBuyingDO::getId)
                // 团购货盘表字段映射
                .selectAs(ErpGroupBuyingDO::getId, ErpGroupBuyingRespVO::getId)
                .selectAs(ErpGroupBuyingDO::getNo, ErpGroupBuyingRespVO::getNo)
                .selectAs(ErpGroupBuyingDO::getProductImage, ErpGroupBuyingRespVO::getProductImage)
                .selectAs(ErpGroupBuyingDO::getBrandName, ErpGroupBuyingRespVO::getBrandName)
                .selectAs(ErpGroupBuyingDO::getCategoryId, ErpGroupBuyingRespVO::getCategoryId)
                .selectAs(ErpGroupBuyingDO::getProductName, ErpGroupBuyingRespVO::getProductName)
                .selectAs(ErpGroupBuyingDO::getProductSpec, ErpGroupBuyingRespVO::getProductSpec)
                .selectAs(ErpGroupBuyingDO::getProductSku, ErpGroupBuyingRespVO::getProductSku)
                .selectAs(ErpGroupBuyingDO::getMarketPrice, ErpGroupBuyingRespVO::getMarketPrice)
                .selectAs(ErpGroupBuyingDO::getShelfLife, ErpGroupBuyingRespVO::getShelfLife)
                .selectAs(ErpGroupBuyingDO::getProductStock, ErpGroupBuyingRespVO::getProductStock)
                .selectAs(ErpGroupBuyingDO::getRemark, ErpGroupBuyingRespVO::getRemark)
                .selectAs(ErpGroupBuyingDO::getCorePrice, ErpGroupBuyingRespVO::getCorePrice)
                .selectAs(ErpGroupBuyingDO::getDistributionPrice, ErpGroupBuyingRespVO::getDistributionPrice)
                .selectAs(ErpGroupBuyingDO::getSupplyGroupPrice, ErpGroupBuyingRespVO::getSupplyGroupPrice)
                .selectAs(ErpGroupBuyingDO::getSellingCommission, ErpGroupBuyingRespVO::getSellingCommission)
                .selectAs(ErpGroupBuyingDO::getGroupPrice, ErpGroupBuyingRespVO::getGroupPrice)
                .selectAs(ErpGroupBuyingDO::getChannelProfit, ErpGroupBuyingRespVO::getChannelProfit)
                .selectAs(ErpGroupBuyingDO::getGroupMechanism, ErpGroupBuyingRespVO::getGroupMechanism)
                .selectAs(ErpGroupBuyingDO::getExpressFee, ErpGroupBuyingRespVO::getExpressFee)
                .selectAs(ErpGroupBuyingDO::getTmallJd, ErpGroupBuyingRespVO::getTmallJd)
                .selectAs(ErpGroupBuyingDO::getPublicData, ErpGroupBuyingRespVO::getPublicData)
                .selectAs(ErpGroupBuyingDO::getPrivateData, ErpGroupBuyingRespVO::getPrivateData)
                .selectAs(ErpGroupBuyingDO::getBrandEndorsement, ErpGroupBuyingRespVO::getBrandEndorsement)
                .selectAs(ErpGroupBuyingDO::getCompetitiveAnalysis, ErpGroupBuyingRespVO::getCompetitiveAnalysis)
                .selectAs(ErpGroupBuyingDO::getExpressCompany, ErpGroupBuyingRespVO::getExpressCompany)
                .selectAs(ErpGroupBuyingDO::getShippingTime, ErpGroupBuyingRespVO::getShippingTime)
                .selectAs(ErpGroupBuyingDO::getShippingArea, ErpGroupBuyingRespVO::getShippingArea)
                .selectAs(ErpGroupBuyingDO::getStatus, ErpGroupBuyingRespVO::getStatus)
                .selectAs(ErpGroupBuyingDO::getCreator, ErpGroupBuyingRespVO::getCreator)
                .selectAs(ErpGroupBuyingDO::getCreateTime, ErpGroupBuyingRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpGroupBuyingRespVO.class, query);
    }

    default ErpGroupBuyingDO selectByNo(String no) {
        return selectOne(ErpGroupBuyingDO::getNo, no);
    }

    default List<ErpGroupBuyingDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpGroupBuyingDO::getNo, nos);
    }

    default ErpGroupBuyingDO selectByProductName(String productName) {
        return selectOne(ErpGroupBuyingDO::getProductName, productName);
    }

    default List<ErpGroupBuyingDO> selectListByProductNameIn(Collection<String> productNames) {
        return selectList(ErpGroupBuyingDO::getProductName, productNames);
    }

    default void insertBatch(List<ErpGroupBuyingDO> list) {
        list.forEach(this::insert);
    }
}
