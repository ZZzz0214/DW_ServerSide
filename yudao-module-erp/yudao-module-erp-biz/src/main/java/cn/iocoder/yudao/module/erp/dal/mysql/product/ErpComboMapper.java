package cn.iocoder.yudao.module.erp.dal.mysql.product;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ERP 组合产品 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpComboMapper extends BaseMapperX<ErpComboProductDO> {

    default PageResult<ErpComboProductDO> selectPage(ErpComboPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpComboProductDO>()
                .likeIfPresent(ErpComboProductDO::getName, reqVO.getName())
                .likeIfPresent(ErpComboProductDO::getShortName, reqVO.getShortName())
                .eqIfPresent(ErpComboProductDO::getShippingCode, reqVO.getShippingCode())
                .betweenIfPresent(ErpComboProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpComboProductDO::getId));
    }

//    default Long selectCountByCategoryId(Long categoryId) {
//        return selectCount(ErpComboProductDO::getCategoryId, categoryId);
//    }
    default Long selectCountByStatus(Integer status) {
        return selectCount(ErpComboProductDO::getStatus, status);
    }

    default List<ErpComboProductDO> selectListByStatus(Integer status) {
        return selectList(ErpComboProductDO::getStatus, status);
    }

    default ErpComboProductDO selectByNo(String no) {
        return selectOne(ErpComboProductDO::getNo, no);
    }


    default List<ErpComboProductDO> selectListByNoIn(Collection<String> nos) {
        if (CollUtil.isEmpty(nos)) {
            return Collections.emptyList();
        }
        return selectList(ErpComboProductDO::getNo, nos);
    }
}
