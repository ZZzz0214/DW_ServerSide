package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 供应商 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpSupplierMapper extends BaseMapperX<ErpSupplierDO> {

    default PageResult<ErpSupplierDO> selectPage(ErpSupplierPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpSupplierDO>()
                .likeIfPresent(ErpSupplierDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpSupplierDO::getName, reqVO.getName())
                .likeIfPresent(ErpSupplierDO::getMobile, reqVO.getMobile())
                .likeIfPresent(ErpSupplierDO::getTelephone, reqVO.getTelephone())
                .orderByDesc(ErpSupplierDO::getId));
    }

    default List<ErpSupplierDO> selectListByStatus(Integer status) {
        return selectList(ErpSupplierDO::getStatus, status);
    }
    
    default ErpSupplierDO selectByNo(String no) {
        return selectOne(new LambdaQueryWrapperX<ErpSupplierDO>()
                .eq(ErpSupplierDO::getNo, no));
    }

    default List<ErpSupplierDO> searchSuppliers(ErpSupplierPageReqVO searchReqVO) {
        return selectList(new LambdaQueryWrapperX<ErpSupplierDO>()
                .likeIfPresent(ErpSupplierDO::getNo, searchReqVO.getNo())
                .likeIfPresent(ErpSupplierDO::getMobile, searchReqVO.getMobile())
                .likeIfPresent(ErpSupplierDO::getTelephone, searchReqVO.getTelephone())
                .orderByDesc(ErpSupplierDO::getId));
    }

    default ErpSupplierDO selectByName(String name) {
        return selectOne(ErpSupplierDO::getName, name);
    }

    default List<ErpSupplierDO> selectListByNameIn(Collection<String> names) {
        return selectList(ErpSupplierDO::getName, names);
    }

}