package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierRespVO;
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

    default PageResult<ErpSupplierRespVO> selectPage(ErpSupplierPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpSupplierDO> query = new MPJLambdaWrapperX<ErpSupplierDO>()
                .likeIfPresent(ErpSupplierDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpSupplierDO::getName, reqVO.getName())
                .likeIfPresent(ErpSupplierDO::getReceiverName, reqVO.getReceiverName())
                .likeIfPresent(ErpSupplierDO::getTelephone, reqVO.getTelephone())
                .likeIfPresent(ErpSupplierDO::getAddress, reqVO.getAddress())
                .likeIfPresent(ErpSupplierDO::getWechatAccount, reqVO.getWechatAccount())
                .likeIfPresent(ErpSupplierDO::getAlipayAccount, reqVO.getAlipayAccount())
                .likeIfPresent(ErpSupplierDO::getBankAccount, reqVO.getBankAccount())
                //.betweenIfPresent(ErpSupplierDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpSupplierDO::getId)
                .selectAs(ErpSupplierDO::getId, ErpSupplierRespVO::getId)
                .selectAs(ErpSupplierDO::getNo, ErpSupplierRespVO::getNo)
                .selectAs(ErpSupplierDO::getName, ErpSupplierRespVO::getName)
                .selectAs(ErpSupplierDO::getReceiverName, ErpSupplierRespVO::getReceiverName)
                .selectAs(ErpSupplierDO::getTelephone, ErpSupplierRespVO::getTelephone)
                .selectAs(ErpSupplierDO::getAddress, ErpSupplierRespVO::getAddress)
                .selectAs(ErpSupplierDO::getWechatAccount, ErpSupplierRespVO::getWechatAccount)
                .selectAs(ErpSupplierDO::getAlipayAccount, ErpSupplierRespVO::getAlipayAccount)
                .selectAs(ErpSupplierDO::getBankAccount, ErpSupplierRespVO::getBankAccount)
                .selectAs(ErpSupplierDO::getRemark, ErpSupplierRespVO::getRemark)
                .selectAs(ErpSupplierDO::getCreateTime, ErpSupplierRespVO::getCreateTime);
        return selectJoinPage(reqVO, ErpSupplierRespVO.class, query);
    }

    default List<ErpSupplierDO> selectListByStatus(Integer status) {
        return selectList();
    }

    default ErpSupplierDO selectByNo(String no) {
        return selectOne(new LambdaQueryWrapperX<ErpSupplierDO>()
                .eq(ErpSupplierDO::getNo, no));
    }

    default List<ErpSupplierRespVO> searchSuppliers(ErpSupplierPageReqVO searchReqVO) {
        MPJLambdaWrapperX<ErpSupplierDO> query = new MPJLambdaWrapperX<ErpSupplierDO>()
                .likeIfPresent(ErpSupplierDO::getNo, searchReqVO.getNo())
                .likeIfPresent(ErpSupplierDO::getName, searchReqVO.getName())
                .likeIfPresent(ErpSupplierDO::getReceiverName, searchReqVO.getReceiverName())
                .likeIfPresent(ErpSupplierDO::getTelephone, searchReqVO.getTelephone())
                .likeIfPresent(ErpSupplierDO::getAddress, searchReqVO.getAddress())
                .likeIfPresent(ErpSupplierDO::getWechatAccount, searchReqVO.getWechatAccount())
                .likeIfPresent(ErpSupplierDO::getAlipayAccount, searchReqVO.getAlipayAccount())
                .likeIfPresent(ErpSupplierDO::getBankAccount, searchReqVO.getBankAccount())
                //.betweenIfPresent(ErpSupplierDO::getCreateTime, searchReqVO.getCreateTime())
                .orderByDesc(ErpSupplierDO::getId)
                .selectAs(ErpSupplierDO::getId, ErpSupplierRespVO::getId)
                .selectAs(ErpSupplierDO::getNo, ErpSupplierRespVO::getNo)
                .selectAs(ErpSupplierDO::getName, ErpSupplierRespVO::getName)
                .selectAs(ErpSupplierDO::getReceiverName, ErpSupplierRespVO::getReceiverName)
                .selectAs(ErpSupplierDO::getTelephone, ErpSupplierRespVO::getTelephone)
                .selectAs(ErpSupplierDO::getAddress, ErpSupplierRespVO::getAddress)
                .selectAs(ErpSupplierDO::getWechatAccount, ErpSupplierRespVO::getWechatAccount)
                .selectAs(ErpSupplierDO::getAlipayAccount, ErpSupplierRespVO::getAlipayAccount)
                .selectAs(ErpSupplierDO::getBankAccount, ErpSupplierRespVO::getBankAccount)
                .selectAs(ErpSupplierDO::getRemark, ErpSupplierRespVO::getRemark)
                .selectAs(ErpSupplierDO::getCreateTime, ErpSupplierRespVO::getCreateTime);
        return selectJoinList(ErpSupplierRespVO.class, query);
    }

    default ErpSupplierDO selectByName(String name) {
        return selectOne(ErpSupplierDO::getName, name);
    }

    default List<ErpSupplierDO> selectListByNameIn(Collection<String> names) {
        return selectList(ErpSupplierDO::getName, names);
    }

}
