package cn.iocoder.yudao.module.erp.dal.mysql.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaserDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalespersonDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ERP 销售人员 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpSalespersonMapper extends BaseMapperX<ErpSalespersonDO> {

    default PageResult<ErpSalespersonRespVO> selectPage(ErpSalespersonPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpSalespersonDO> query = new MPJLambdaWrapperX<ErpSalespersonDO>()
                .likeIfPresent(ErpSalespersonDO::getSalespersonName, reqVO.getSalespersonName())
                .likeIfPresent(ErpSalespersonDO::getContactPhone, reqVO.getContactPhone())
                .betweenIfPresent(ErpSalespersonDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpSalespersonDO::getId)
                .selectAs(ErpSalespersonDO::getId, ErpSalespersonRespVO::getId)
                .selectAs(ErpSalespersonDO::getSalespersonName, ErpSalespersonRespVO::getSalespersonName)
                .selectAs(ErpSalespersonDO::getReceiverName, ErpSalespersonRespVO::getReceiverName)
                .selectAs(ErpSalespersonDO::getContactPhone, ErpSalespersonRespVO::getContactPhone)
                .selectAs(ErpSalespersonDO::getAddress, ErpSalespersonRespVO::getAddress)
                .selectAs(ErpSalespersonDO::getWechatAccount, ErpSalespersonRespVO::getWechatAccount)
                .selectAs(ErpSalespersonDO::getAlipayAccount, ErpSalespersonRespVO::getAlipayAccount)
                .selectAs(ErpSalespersonDO::getBankAccount, ErpSalespersonRespVO::getBankAccount)
                .selectAs(ErpSalespersonDO::getRemark, ErpSalespersonRespVO::getRemark)
                .selectAs(ErpPurchaserDO::getCreator, ErpPurchaserRespVO::getCreator)
                .selectAs(ErpSalespersonDO::getCreateTime, ErpSalespersonRespVO::getCreateTime);
        return selectJoinPage(reqVO, ErpSalespersonRespVO.class, query);
    }

    default List<ErpSalespersonDO> selectListBySalespersonName(String salespersonName) {
        return selectList(new LambdaQueryWrapperX<ErpSalespersonDO>()
                .likeIfPresent(ErpSalespersonDO::getSalespersonName, salespersonName)
                .orderByDesc(ErpSalespersonDO::getId));
    }
}
