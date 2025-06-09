package cn.iocoder.yudao.module.erp.dal.mysql.sale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePricePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionPurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionSaleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesalePurchaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleSaleDO;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.github.yulichang.interfaces.MPJBaseJoin;
import com.github.yulichang.toolkit.Constant;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.ErpSalePriceRespVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.*;

/**
 * ERP 销售价格 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpSalePriceMapper extends BaseMapperX<ErpSalePriceDO> {

    <DTO, W> List<DTO> selectJoinList(@Param(Constant.CLAZZ) Class<DTO> clazz,
                                    @Param(Constants.WRAPPER) W wrapper);

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
    default List<ErpSalePriceRespVO> selectMissingPrices() {
        // 1. 查询代发表中客户名称不为空但代发单价为空的数据
        MPJLambdaWrapperX<ErpDistributionBaseDO> distributionQuery = new MPJLambdaWrapperX<ErpDistributionBaseDO>();
        distributionQuery.leftJoin(ErpDistributionSaleDO.class, ErpDistributionSaleDO::getBaseId, ErpDistributionBaseDO::getId)
                .leftJoin(ErpDistributionPurchaseDO.class, ErpDistributionPurchaseDO::getBaseId, ErpDistributionBaseDO::getId)
                .leftJoin(ErpComboProductDO.class, ErpComboProductDO::getId, ErpDistributionPurchaseDO::getComboProductId)
                .isNotNull(ErpDistributionSaleDO::getCustomerName)
                .isNull(ErpDistributionSaleDO::getSalePrice)
                .selectAs(ErpComboProductDO::getId, ErpSalePriceRespVO::getGroupProductId)
                .selectAs(ErpDistributionSaleDO::getCustomerName, ErpSalePriceRespVO::getCustomerName)
                .selectAs(ErpComboProductDO::getName, ErpSalePriceRespVO::getProductName)
                .selectAs(ErpComboProductDO::getShortName, ErpSalePriceRespVO::getProductShortName)
                .selectAs(ErpComboProductDO::getImage, ErpSalePriceRespVO::getProductImage);
        List<ErpSalePriceRespVO> distributionList = selectJoinList(ErpSalePriceRespVO.class, distributionQuery);

        // 2. 查询批发表中客户名称不为空但批发单价为空的数据
        MPJLambdaWrapperX<ErpWholesaleBaseDO> wholesaleQuery = new MPJLambdaWrapperX<ErpWholesaleBaseDO>();
        wholesaleQuery.leftJoin(ErpWholesaleSaleDO.class, ErpWholesaleSaleDO::getBaseId, ErpWholesaleBaseDO::getId)
                .leftJoin(ErpWholesalePurchaseDO.class, ErpWholesalePurchaseDO::getBaseId, ErpWholesaleBaseDO::getId)
                .leftJoin(ErpComboProductDO.class, ErpComboProductDO::getId, ErpWholesalePurchaseDO::getComboProductId)
                .isNotNull(ErpWholesaleSaleDO::getCustomerName)
                .isNull(ErpWholesaleSaleDO::getSalePrice)
                .selectAs(ErpComboProductDO::getId, ErpSalePriceRespVO::getGroupProductId)
                .selectAs(ErpWholesaleSaleDO::getCustomerName, ErpSalePriceRespVO::getCustomerName)
                .selectAs(ErpComboProductDO::getName, ErpSalePriceRespVO::getProductName)
                .selectAs(ErpComboProductDO::getShortName, ErpSalePriceRespVO::getProductShortName)
                .selectAs(ErpComboProductDO::getImage, ErpSalePriceRespVO::getProductImage);
        List<ErpSalePriceRespVO> wholesaleList = selectJoinList(ErpSalePriceRespVO.class, wholesaleQuery);

        // 3. 合并结果并去重
        Map<String, ErpSalePriceRespVO> resultMap = new LinkedHashMap<>();
        distributionList.forEach(item -> {
            String key = item.getGroupProductId() + "_" + item.getCustomerName();
            resultMap.putIfAbsent(key, item);
        });
        wholesaleList.forEach(item -> {
            String key = item.getGroupProductId() + "_" + item.getCustomerName();
            resultMap.putIfAbsent(key, item);
        });

        return new ArrayList<>(resultMap.values());
    }

    default List<ErpSalePriceDO> selectListByNoIn(Collection<String> nos) {
        if (CollUtil.isEmpty(nos)) {
            return Collections.emptyList();
        }
        return selectList(ErpSalePriceDO::getNo, nos);
    }

}
