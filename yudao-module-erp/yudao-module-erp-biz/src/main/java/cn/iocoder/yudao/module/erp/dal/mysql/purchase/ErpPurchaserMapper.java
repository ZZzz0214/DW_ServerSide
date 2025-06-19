package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.purchaser.ErpPurchaserRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaserDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ERP 采购人员 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpPurchaserMapper extends BaseMapperX<ErpPurchaserDO> {

    default PageResult<ErpPurchaserRespVO> selectPage(ErpPurchaserPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpPurchaserDO> query = new MPJLambdaWrapperX<ErpPurchaserDO>()
                .likeIfPresent(ErpPurchaserDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpPurchaserDO::getPurchaserName, reqVO.getPurchaserName())
                .likeIfPresent(ErpPurchaserDO::getContactPhone, reqVO.getContactPhone())
                .betweenIfPresent(ErpPurchaserDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpPurchaserDO::getId)
                .selectAs(ErpPurchaserDO::getId, ErpPurchaserRespVO::getId)
                .selectAs(ErpPurchaserDO::getNo, ErpPurchaserRespVO::getNo)
                .selectAs(ErpPurchaserDO::getPurchaserName, ErpPurchaserRespVO::getPurchaserName)
                .selectAs(ErpPurchaserDO::getReceiverName, ErpPurchaserRespVO::getReceiverName)
                .selectAs(ErpPurchaserDO::getContactPhone, ErpPurchaserRespVO::getContactPhone)
                .selectAs(ErpPurchaserDO::getAddress, ErpPurchaserRespVO::getAddress)
                .selectAs(ErpPurchaserDO::getWechatAccount, ErpPurchaserRespVO::getWechatAccount)
                .selectAs(ErpPurchaserDO::getAlipayAccount, ErpPurchaserRespVO::getAlipayAccount)
                .selectAs(ErpPurchaserDO::getBankAccount, ErpPurchaserRespVO::getBankAccount)
                .selectAs(ErpPurchaserDO::getRemark, ErpPurchaserRespVO::getRemark)
                .selectAs(ErpPurchaserDO::getCreateTime, ErpPurchaserRespVO::getCreateTime);
        return selectJoinPage(reqVO, ErpPurchaserRespVO.class, query);
    }

    default List<ErpPurchaserDO> selectListByPurchaserName(String purchaserName) {
        return selectList(new LambdaQueryWrapperX<ErpPurchaserDO>()
                .likeIfPresent(ErpPurchaserDO::getPurchaserName, purchaserName)
                .orderByDesc(ErpPurchaserDO::getId));
    }

    default ErpPurchaserDO selectByNo(String no) {
        return selectOne(new LambdaQueryWrapperX<ErpPurchaserDO>()
                .eq(ErpPurchaserDO::getNo, no));
    }
}
