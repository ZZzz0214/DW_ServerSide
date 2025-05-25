package cn.iocoder.yudao.module.erp.dal.mysql.dropship;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.dropship.vo.ErpDropshipAssistPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.dropship.vo.ErpDropshipAssistRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.dropship.ErpDropshipAssistDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpDropshipAssistMapper extends BaseMapperX<ErpDropshipAssistDO> {

    default PageResult<ErpDropshipAssistRespVO> selectPage(ErpDropshipAssistPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpDropshipAssistDO> query = new MPJLambdaWrapperX<ErpDropshipAssistDO>()
                .likeIfPresent(ErpDropshipAssistDO::getNo, reqVO.getNo())
//                .eqIfPresent(ErpDropshipAssistDO::getTenantId, reqVO.getTenantId())
                .orderByDesc(ErpDropshipAssistDO::getId)
                // 字段映射
                .selectAs(ErpDropshipAssistDO::getId, ErpDropshipAssistRespVO::getId)
                .selectAs(ErpDropshipAssistDO::getNo, ErpDropshipAssistRespVO::getNo)
                .selectAs(ErpDropshipAssistDO::getOriginalProduct, ErpDropshipAssistRespVO::getOriginalProduct)
                .selectAs(ErpDropshipAssistDO::getOriginalSpec, ErpDropshipAssistRespVO::getOriginalSpec)
                .selectAs(ErpDropshipAssistDO::getOriginalQuantity, ErpDropshipAssistRespVO::getOriginalQuantity)
                .selectAs(ErpDropshipAssistDO::getComboProductId, ErpDropshipAssistRespVO::getComboProductId)
                .selectAs(ErpDropshipAssistDO::getProductSpec, ErpDropshipAssistRespVO::getProductSpec)
                .selectAs(ErpDropshipAssistDO::getProductQuantity, ErpDropshipAssistRespVO::getProductQuantity)
                .selectAs(ErpDropshipAssistDO::getCreateTime, ErpDropshipAssistRespVO::getCreateTime);
                query.leftJoin(ErpComboProductDO.class, ErpComboProductDO::getId, ErpDropshipAssistDO::getComboProductId)
               .selectAs(ErpComboProductDO::getName, ErpDropshipAssistRespVO::getProductName)
               .selectAs(ErpComboProductDO::getShortName, ErpDropshipAssistRespVO::getProductShortName)
               .selectAs(ErpComboProductDO::getShippingCode, ErpDropshipAssistRespVO::getShippingCode)
               .selectAs(ErpComboProductDO::getName, ErpDropshipAssistRespVO::getProductName);


        return selectJoinPage(reqVO, ErpDropshipAssistRespVO.class, query);
    }

    default ErpDropshipAssistDO selectByNo(String no) {
        return selectOne(ErpDropshipAssistDO::getNo, no);
    }
}
