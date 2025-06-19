package cn.iocoder.yudao.module.erp.dal.mysql.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.customer.ErpCustomerPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 客户 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpCustomerMapper extends BaseMapperX<ErpCustomerDO> {

    default PageResult<ErpCustomerDO> selectPage(ErpCustomerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpCustomerDO>()
                .likeIfPresent(ErpCustomerDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpCustomerDO::getName, reqVO.getName())
                .likeIfPresent(ErpCustomerDO::getReceiverName, reqVO.getReceiverName())
                .likeIfPresent(ErpCustomerDO::getTelephone, reqVO.getTelephone())
                .likeIfPresent(ErpCustomerDO::getAddress, reqVO.getAddress())
                .likeIfPresent(ErpCustomerDO::getWechatAccount, reqVO.getWechatAccount())
                .likeIfPresent(ErpCustomerDO::getAlipayAccount, reqVO.getAlipayAccount())
                .likeIfPresent(ErpCustomerDO::getBankAccount, reqVO.getBankAccount())
                .orderByDesc(ErpCustomerDO::getId));
    }

    default List<ErpCustomerDO> selectListByNameIn(Collection<String> names) {
        return selectList(ErpCustomerDO::getName, names);
    }

}