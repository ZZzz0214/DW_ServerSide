package cn.iocoder.yudao.module.erp.dal.mysql.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePricePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceRespVO;

import java.math.BigDecimal;

/**
 * ERP 销售价格 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpSalePriceMapper extends BaseMapperX<ErpSalePriceDO> {

    default PageResult<ErpSalePriceDO> selectPage(ErpSalePricePageReqVO reqVO) {
        MPJLambdaWrapperX<ErpSalePriceDO> query = new MPJLambdaWrapperX<ErpSalePriceDO>()
                .likeIfPresent(ErpSalePriceDO::getCustomerName, reqVO.getCustomerName())
                .betweenIfPresent(ErpSalePriceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpSalePriceDO::getId);

        // 代发单价范围
        if (reqVO.getDistributionPriceRange() != null && reqVO.getDistributionPriceRange().length == 2) {
            query.ge(ErpSalePriceDO::getDistributionPrice, reqVO.getDistributionPriceRange()[0])
                    .le(ErpSalePriceDO::getDistributionPrice, reqVO.getDistributionPriceRange()[1]);
        }

        // 批发单价范围
        if (reqVO.getWholesalePriceRange() != null && reqVO.getWholesalePriceRange().length == 2) {
            query.ge(ErpSalePriceDO::getWholesalePrice, reqVO.getWholesalePriceRange()[0])
                    .le(ErpSalePriceDO::getWholesalePrice, reqVO.getWholesalePriceRange()[1]);
        }

        return selectJoinPage(reqVO, ErpSalePriceDO.class, query);
    }

    default int updateById(ErpSalePriceDO updateObj) {
        return update(updateObj, new MPJLambdaWrapperX<ErpSalePriceDO>()
                .eq(ErpSalePriceDO::getId, updateObj.getId()));
    }
    default ErpSalePriceDO selectById(Long id) {
        if (id == null) {
            return null;
        }
        return selectOne(ErpSalePriceDO::getId, id);
    }

    default ErpSalePriceDO selectByNo(String no) {
        if (no == null) {
            return null;
        }
        return selectOne(ErpSalePriceDO::getNo, no);
    }

    default ErpSalePriceRespVO selectByGroupProductIdAndCustomerName(Long groupProductId, String customerName) {
        MPJLambdaWrapperX<ErpSalePriceDO> query = new MPJLambdaWrapperX<ErpSalePriceDO>()
                .eq(ErpSalePriceDO::getGroupProductId, groupProductId)
                .eqIfPresent(ErpSalePriceDO::getCustomerName, customerName);

        return selectJoinOne(ErpSalePriceRespVO.class, query);
    }
}
