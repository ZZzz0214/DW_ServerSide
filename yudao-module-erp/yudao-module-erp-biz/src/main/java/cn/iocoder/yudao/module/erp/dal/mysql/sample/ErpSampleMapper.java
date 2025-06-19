package cn.iocoder.yudao.module.erp.dal.mysql.sample;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSamplePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sample.ErpSampleDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpSampleMapper extends BaseMapperX<ErpSampleDO> {

    default PageResult<ErpSampleRespVO> selectPage(ErpSamplePageReqVO reqVO) {
        MPJLambdaWrapperX<ErpSampleDO> query = new MPJLambdaWrapperX<ErpSampleDO>()
                .likeIfPresent(ErpSampleDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpSampleDO::getLogisticsCompany, reqVO.getLogisticsCompany())
                .likeIfPresent(ErpSampleDO::getLogisticsNo, reqVO.getLogisticsNo())
                .likeIfPresent(ErpSampleDO::getReceiverName, reqVO.getReceiverName())
                .likeIfPresent(ErpSampleDO::getContactPhone, reqVO.getContactPhone())
                .likeIfPresent(ErpSampleDO::getProductSpec, reqVO.getProductSpec())
                .likeIfPresent(ErpSampleDO::getCustomerName, reqVO.getCustomerName())
                .eqIfPresent(ErpSampleDO::getSampleStatus, reqVO.getSampleStatus())
                .likeIfPresent(ErpSampleDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(ErpSampleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpSampleDO::getId);

        // 联表查询组品信息
        query.leftJoin(ErpComboProductDO.class, ErpComboProductDO::getId, ErpSampleDO::getComboProductId);

        // 添加组品相关的查询条件
        if (reqVO.getComboProductId() != null && !reqVO.getComboProductId().isEmpty()) {
            query.like(ErpComboProductDO::getNo, reqVO.getComboProductId());
        }
        if (reqVO.getShippingCode() != null && !reqVO.getShippingCode().isEmpty()) {
            query.like(ErpComboProductDO::getShippingCode, reqVO.getShippingCode());
        }
        if (reqVO.getProductName() != null && !reqVO.getProductName().isEmpty()) {
            query.like(ErpComboProductDO::getName, reqVO.getProductName());
        }

        // 字段映射
        query.selectAs(ErpSampleDO::getId, ErpSampleRespVO::getId)
                .selectAs(ErpSampleDO::getNo, ErpSampleRespVO::getNo)
                .selectAs(ErpSampleDO::getLogisticsCompany, ErpSampleRespVO::getLogisticsCompany)
                .selectAs(ErpSampleDO::getLogisticsNo, ErpSampleRespVO::getLogisticsNo)
                .selectAs(ErpSampleDO::getReceiverName, ErpSampleRespVO::getReceiverName)
                .selectAs(ErpSampleDO::getContactPhone, ErpSampleRespVO::getContactPhone)
                .selectAs(ErpSampleDO::getAddress, ErpSampleRespVO::getAddress)
                .selectAs(ErpSampleDO::getRemark, ErpSampleRespVO::getRemark)
                .selectAs(ErpSampleDO::getProductSpec, ErpSampleRespVO::getProductSpec)
                .selectAs(ErpSampleDO::getProductQuantity, ErpSampleRespVO::getProductQuantity)
                .selectAs(ErpSampleDO::getCustomerName, ErpSampleRespVO::getCustomerName)
                .selectAs(ErpSampleDO::getSampleStatus, ErpSampleRespVO::getSampleStatus)
                .selectAs(ErpSampleDO::getReference, ErpSampleRespVO::getReference)
                .selectAs(ErpSampleDO::getCreator, ErpSampleRespVO::getCreator)
                .selectAs(ErpSampleDO::getCreateTime, ErpSampleRespVO::getCreateTime)
                .selectAs(ErpComboProductDO::getShippingCode, ErpSampleRespVO::getShippingCode)
                .selectAs(ErpComboProductDO::getNo, ErpSampleRespVO::getComboProductId)
                .selectAs(ErpComboProductDO::getName, ErpSampleRespVO::getComboProductName);

        return selectJoinPage(reqVO, ErpSampleRespVO.class, query);
    }

    default ErpSampleDO selectByNo(String no) {
        return selectOne(ErpSampleDO::getNo, no);
    }

    default List<ErpSampleDO> selectListByNoIn(Collection<String> nos) {
        return selectList(ErpSampleDO::getNo, nos);
    }

    default void insertBatch(List<ErpSampleDO> list) {
        list.forEach(this::insert);
    }
}
