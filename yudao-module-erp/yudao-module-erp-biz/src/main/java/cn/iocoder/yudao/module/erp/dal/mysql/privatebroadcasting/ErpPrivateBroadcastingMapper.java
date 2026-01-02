package cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcasting;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcasting.ErpPrivateBroadcastingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpPrivateBroadcastingMapper extends BaseMapperX<ErpPrivateBroadcastingDO> {

    default PageResult<ErpPrivateBroadcastingRespVO> selectPage(ErpPrivateBroadcastingPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpPrivateBroadcastingDO> query = new MPJLambdaWrapperX<ErpPrivateBroadcastingDO>()
                .likeIfPresent(ErpPrivateBroadcastingDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpPrivateBroadcastingDO::getCategoryId, reqVO.getCategoryId())
                .likeIfPresent(ErpPrivateBroadcastingDO::getProductName, reqVO.getProductName());
        
        // 品牌名称筛选：支持多选和为空筛选（可以同时选择多个值和为空）
        if (CollUtil.isNotEmpty(reqVO.getBrandNames()) || Boolean.TRUE.equals(reqVO.getBrandNameEmpty())) {
            query.and(w -> {
                boolean hasCondition = false;
                if (CollUtil.isNotEmpty(reqVO.getBrandNames())) {
                    w.in(ErpPrivateBroadcastingDO::getBrandName, reqVO.getBrandNames());
                    hasCondition = true;
                }
                if (Boolean.TRUE.equals(reqVO.getBrandNameEmpty())) {
                    if (hasCondition) {
                        w.or();
                    }
                    w.and(empty -> empty.isNull(ErpPrivateBroadcastingDO::getBrandName).or().eq(ErpPrivateBroadcastingDO::getBrandName, ""));
                }
            });
        } else {
            query.likeIfPresent(ErpPrivateBroadcastingDO::getBrandName, reqVO.getBrandName());
        }
        
        query.likeIfPresent(ErpPrivateBroadcastingDO::getProductSpec, reqVO.getProductSpec())
                .betweenIfPresent(ErpPrivateBroadcastingDO::getShelfLife, reqVO.getShelfLife())
                .likeIfPresent(ErpPrivateBroadcastingDO::getLivePrice, reqVO.getLivePrice())
                .likeIfPresent(ErpPrivateBroadcastingDO::getProductNakedPrice, reqVO.getNakedPrice())
                .likeIfPresent(ErpPrivateBroadcastingDO::getExpressFee, reqVO.getExpressFee())
                .likeIfPresent(ErpPrivateBroadcastingDO::getDropshipPrice, reqVO.getDropshippingPrice());
        
        // 货盘状态筛选：支持多选和为空筛选（可以同时选择多个值和为空）
        // 注意：数据库中状态可能存储为逗号分隔的多个值（如 "上架,热卖"），需要使用 LIKE 查询
        if (CollUtil.isNotEmpty(reqVO.getPrivateStatuses()) || Boolean.TRUE.equals(reqVO.getPrivateStatusEmpty())) {
            query.and(w -> {
                boolean hasCondition = false;
                if (CollUtil.isNotEmpty(reqVO.getPrivateStatuses())) {
                    // 使用 OR 连接多个状态的 LIKE 查询
                    w.nested(nested -> {
                        for (int i = 0; i < reqVO.getPrivateStatuses().size(); i++) {
                            if (i > 0) {
                                nested.or();
                            }
                            String status = reqVO.getPrivateStatuses().get(i);
                            // 使用 LIKE 查询支持逗号分隔的多状态
                            nested.and(like -> like
                                .eq(ErpPrivateBroadcastingDO::getPrivateStatus, status)  // 完全匹配单个状态
                                .or().like(ErpPrivateBroadcastingDO::getPrivateStatus, status + ",%")  // 匹配开头
                                .or().like(ErpPrivateBroadcastingDO::getPrivateStatus, "%," + status + ",%")  // 匹配中间
                                .or().like(ErpPrivateBroadcastingDO::getPrivateStatus, "%," + status)  // 匹配结尾
                            );
                        }
                    });
                    hasCondition = true;
                }
                if (Boolean.TRUE.equals(reqVO.getPrivateStatusEmpty())) {
                    if (hasCondition) {
                        w.or();
                    }
                    w.and(empty -> empty.isNull(ErpPrivateBroadcastingDO::getPrivateStatus).or().eq(ErpPrivateBroadcastingDO::getPrivateStatus, ""));
                }
            });
        } else {
            query.eqIfPresent(ErpPrivateBroadcastingDO::getPrivateStatus, reqVO.getPrivateStatus());
        }
        
        query.likeIfPresent(ErpPrivateBroadcastingDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(ErpPrivateBroadcastingDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpPrivateBroadcastingDO::getId)
                // 私播货盘表字段映射
                .selectAs(ErpPrivateBroadcastingDO::getId, ErpPrivateBroadcastingRespVO::getId)
                .selectAs(ErpPrivateBroadcastingDO::getNo, ErpPrivateBroadcastingRespVO::getNo)
                .selectAs(ErpPrivateBroadcastingDO::getProductImage, ErpPrivateBroadcastingRespVO::getProductImage)
                .selectAs(ErpPrivateBroadcastingDO::getBrandName, ErpPrivateBroadcastingRespVO::getBrandName)
                .selectAs(ErpPrivateBroadcastingDO::getCategoryId, ErpPrivateBroadcastingRespVO::getCategoryId)
                .selectAs(ErpPrivateBroadcastingDO::getProductName, ErpPrivateBroadcastingRespVO::getProductName)
                .selectAs(ErpPrivateBroadcastingDO::getProductSpec, ErpPrivateBroadcastingRespVO::getProductSpec)
                .selectAs(ErpPrivateBroadcastingDO::getProductSku, ErpPrivateBroadcastingRespVO::getProductSku)
                .selectAs(ErpPrivateBroadcastingDO::getMarketPrice, ErpPrivateBroadcastingRespVO::getMarketPrice)
                .selectAs(ErpPrivateBroadcastingDO::getShelfLife, ErpPrivateBroadcastingRespVO::getShelfLife)
                .selectAs(ErpPrivateBroadcastingDO::getProductStock, ErpPrivateBroadcastingRespVO::getProductStock)
                .selectAs(ErpPrivateBroadcastingDO::getLivePrice, ErpPrivateBroadcastingRespVO::getLivePrice)
                .selectAs(ErpPrivateBroadcastingDO::getProductNakedPrice, ErpPrivateBroadcastingRespVO::getProductNakedPrice)
                .selectAs(ErpPrivateBroadcastingDO::getExpressFee, ErpPrivateBroadcastingRespVO::getExpressFee)
                .selectAs(ErpPrivateBroadcastingDO::getDropshipPrice, ErpPrivateBroadcastingRespVO::getDropshipPrice)
                .selectAs(ErpPrivateBroadcastingDO::getPublicLink, ErpPrivateBroadcastingRespVO::getPublicLink)
                .selectAs(ErpPrivateBroadcastingDO::getCoreSellingPoint, ErpPrivateBroadcastingRespVO::getCoreSellingPoint)
                .selectAs(ErpPrivateBroadcastingDO::getExpressCompany, ErpPrivateBroadcastingRespVO::getExpressCompany)
                .selectAs(ErpPrivateBroadcastingDO::getShippingTime, ErpPrivateBroadcastingRespVO::getShippingTime)
                .selectAs(ErpPrivateBroadcastingDO::getShippingArea, ErpPrivateBroadcastingRespVO::getShippingArea)
                .selectAs(ErpPrivateBroadcastingDO::getRemark, ErpPrivateBroadcastingRespVO::getRemark)
                .selectAs(ErpPrivateBroadcastingDO::getPrivateStatus, ErpPrivateBroadcastingRespVO::getPrivateStatus)
                .selectAs(ErpPrivateBroadcastingDO::getCreator, ErpPrivateBroadcastingRespVO::getCreator)
                .selectAs(ErpPrivateBroadcastingDO::getCreateTime, ErpPrivateBroadcastingRespVO::getCreateTime);

        return selectJoinPage(reqVO, ErpPrivateBroadcastingRespVO.class, query);
    }

    default ErpPrivateBroadcastingDO selectByNo(String no) {
        return selectOne(ErpPrivateBroadcastingDO::getNo, no);
    }

    default List<ErpPrivateBroadcastingDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpPrivateBroadcastingDO::getNo, nos);
    }

    default ErpPrivateBroadcastingDO selectByProductName(String productName) {
        return selectOne(ErpPrivateBroadcastingDO::getProductName, productName);
    }

    default List<ErpPrivateBroadcastingDO> selectListByProductNameIn(Collection<String> productNames) {
        return selectList(ErpPrivateBroadcastingDO::getProductName, productNames);
    }
}
