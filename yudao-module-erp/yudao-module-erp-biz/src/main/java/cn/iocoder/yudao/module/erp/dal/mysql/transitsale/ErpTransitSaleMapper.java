package cn.iocoder.yudao.module.erp.dal.mysql.transitsale;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.transitsale.vo.ErpTransitSalePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.transitsale.vo.ErpTransitSaleRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpTransitSaleDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpTransitSaleMapper extends BaseMapperX<ErpTransitSaleDO> {

    default PageResult<ErpTransitSaleRespVO> selectPage(ErpTransitSalePageReqVO reqVO) {
        MPJLambdaWrapperX<ErpTransitSaleDO> query = new MPJLambdaWrapperX<ErpTransitSaleDO>()
        .likeIfPresent(ErpTransitSaleDO::getNo, reqVO.getNo())
        .eqIfPresent(ErpTransitSaleDO::getGroupProductId, reqVO.getGroupProductId())
        .likeIfPresent(ErpTransitSaleDO::getTransitPerson, reqVO.getTransitPerson())
        .eqIfPresent(ErpTransitSaleDO::getDistributionPrice, reqVO.getDistributionPrice())
        .eqIfPresent(ErpTransitSaleDO::getWholesalePrice, reqVO.getWholesalePrice())
        .likeIfPresent(ErpTransitSaleDO::getCreator, reqVO.getCreator())
        .betweenIfPresent(ErpTransitSaleDO::getCreateTime, reqVO.getCreateTime())
        .orderByDesc(ErpTransitSaleDO::getId)
        // 中转销售表字段映射
        .selectAs(ErpTransitSaleDO::getId, ErpTransitSaleRespVO::getId)
        .selectAs(ErpTransitSaleDO::getNo, ErpTransitSaleRespVO::getNo)
        .selectAs(ErpTransitSaleDO::getGroupProductId, ErpTransitSaleRespVO::getGroupProductId)
        .selectAs(ErpTransitSaleDO::getTransitPerson, ErpTransitSaleRespVO::getTransitPerson)
        .selectAs(ErpTransitSaleDO::getDistributionPrice, ErpTransitSaleRespVO::getDistributionPrice)
        .selectAs(ErpTransitSaleDO::getWholesalePrice, ErpTransitSaleRespVO::getWholesalePrice)
        .selectAs(ErpTransitSaleDO::getRemark, ErpTransitSaleRespVO::getRemark)
        .selectAs(ErpTransitSaleDO::getShippingFeeType, ErpTransitSaleRespVO::getShippingFeeType)
        .selectAs(ErpTransitSaleDO::getFixedShippingFee, ErpTransitSaleRespVO::getFixedShippingFee)
//        .selectAs(ErpTransitSaleDO::getFirstItemQuantity, ErpTransitSaleRespVO::getFirstItemQuantity)
//        .selectAs(ErpTransitSaleDO::getFirstItemPrice, ErpTransitSaleRespVO::getFirstItemPrice)
        .selectAs(ErpTransitSaleDO::getAdditionalItemQuantity, ErpTransitSaleRespVO::getAdditionalItemQuantity)
        .selectAs(ErpTransitSaleDO::getAdditionalItemPrice, ErpTransitSaleRespVO::getAdditionalItemPrice)
        .selectAs(ErpTransitSaleDO::getFirstWeight, ErpTransitSaleRespVO::getFirstWeight)
        .selectAs(ErpTransitSaleDO::getFirstWeightPrice, ErpTransitSaleRespVO::getFirstWeightPrice)
        .selectAs(ErpTransitSaleDO::getAdditionalWeight, ErpTransitSaleRespVO::getAdditionalWeight)
        .selectAs(ErpTransitSaleDO::getAdditionalWeightPrice, ErpTransitSaleRespVO::getAdditionalWeightPrice)
        .selectAs(ErpTransitSaleDO::getCreator, ErpTransitSaleRespVO::getCreator)
        .selectAs(ErpTransitSaleDO::getCreateTime, ErpTransitSaleRespVO::getCreateTime);
        // 关联组品表查询
        query.leftJoin(ErpComboProductDO.class, ErpComboProductDO::getId, ErpTransitSaleDO::getGroupProductId)
        .selectAs(ErpComboProductDO::getName, ErpTransitSaleRespVO::getProductName)
        .selectAs(ErpComboProductDO::getShortName, ErpTransitSaleRespVO::getProductShortName)
        .selectAs(ErpComboProductDO::getNo, ErpTransitSaleRespVO::getGroupProductNo);

        // 添加关联表字段的搜索条件
        if (reqVO.getProductName() != null) {
            query.like(ErpComboProductDO::getName, reqVO.getProductName());
        }
        if (reqVO.getProductShortName() != null) {
            query.like(ErpComboProductDO::getShortName, reqVO.getProductShortName());
        }

        return selectJoinPage(reqVO, ErpTransitSaleRespVO.class, query);
    }
    default List<ErpTransitSaleDO> selectListByNoIn(Collection<String> nos) {
        if (CollUtil.isEmpty(nos)) {
            return Collections.emptyList();
        }
        return selectList(ErpTransitSaleDO::getNo, nos);
    }

    default ErpTransitSaleDO selectByNo(String no) {
        return selectOne(ErpTransitSaleDO::getNo, no);
    }

    default ErpTransitSaleDO selectByTransitPersonAndGroupProductId(String transitPerson, Long groupProductId) {
        return selectOne(new LambdaQueryWrapper<ErpTransitSaleDO>()
                .eq(ErpTransitSaleDO::getTransitPerson, transitPerson)
                .eq(ErpTransitSaleDO::getGroupProductId, groupProductId));
    }

    default Long selectCountByTransitPersonAndGroupProductId(String transitPerson, Long groupProductId, Long excludeId) {
        LambdaQueryWrapper<ErpTransitSaleDO> queryWrapper = new LambdaQueryWrapper<ErpTransitSaleDO>()
                .eq(ErpTransitSaleDO::getTransitPerson, transitPerson)
                .eq(ErpTransitSaleDO::getGroupProductId, groupProductId);
        if (excludeId != null) {
            queryWrapper.ne(ErpTransitSaleDO::getId, excludeId);
        }
        return selectCount(queryWrapper);
    }
}
