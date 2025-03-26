package cn.iocoder.yudao.module.erp.dal.mysql.product;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.price.ErpSalesPricePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpSalesPriceDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import java.util.List;

/**
 * ERP 销售价格 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpSalesPriceMapper extends BaseMapperX<ErpSalesPriceDO> {

    /**
     * 分页查询销售价格
     *
     * @param reqVO 分页查询条件
     * @return 分页结果
     */
    default PageResult<ErpSalesPriceDO> selectPage(ErpSalesPricePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpSalesPriceDO>()
                .eqIfPresent(ErpSalesPriceDO::getType, reqVO.getType()) // 根据类型查询
                .eqIfPresent(ErpSalesPriceDO::getProductId, reqVO.getProductId()) // 根据产品编号查询
                .eqIfPresent(ErpSalesPriceDO::getComboProductId, reqVO.getComboProductId()) // 根据组合产品编号查询
                .likeIfPresent(ErpSalesPriceDO::getCustomerName, reqVO.getCustomerName()) // 根据客户名称模糊查询
//                .betweenIfPresent(ErpSalesPriceDO::getCreateTime, reqVO.getCreateTime()) // 根据创建时间范围查询
                .orderByDesc(ErpSalesPriceDO::getId)); // 按 ID 降序排序
    }

    /**
     * 根据产品编号查询销售价格数量
     *
     * @param productId 产品编号
     * @return 数量
     */
    default Long selectCountByProductId(Long productId) {
        return selectCount(ErpSalesPriceDO::getProductId, productId);
    }

    /**
     * 根据组合产品编号查询销售价格数量
     *
     * @param comboProductId 组合产品编号
     * @return 数量
     */
    default Long selectCountByComboProductId(Long comboProductId) {
        return selectCount(ErpSalesPriceDO::getComboProductId, comboProductId);
    }

    /**
     * 根据类型查询销售价格列表
     *
     * @param type 类型
     * @return 销售价格列表
     */
    default List<ErpSalesPriceDO> selectListByType(Integer type) {
        return selectList(ErpSalesPriceDO::getType, type);
    }
}
