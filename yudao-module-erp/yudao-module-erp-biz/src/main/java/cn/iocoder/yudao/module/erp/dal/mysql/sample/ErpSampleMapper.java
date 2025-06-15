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
                .likeIfPresent(ErpSampleDO::getCustomerName, reqVO.getCustomerName())
                .eqIfPresent(ErpSampleDO::getSampleStatus, reqVO.getSampleStatus())
                .betweenIfPresent(ErpSampleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpSampleDO::getId)
                // 样品表字段映射
                .selectAs(ErpSampleDO::getId, ErpSampleRespVO::getId)
                .selectAs(ErpSampleDO::getNo, ErpSampleRespVO::getNo)
                .selectAs(ErpSampleDO::getLogisticsCompany, ErpSampleRespVO::getLogisticsCompany)
                .selectAs(ErpSampleDO::getLogisticsNo, ErpSampleRespVO::getLogisticsNo)
                .selectAs(ErpSampleDO::getReceiverName, ErpSampleRespVO::getReceiverName)
                .selectAs(ErpSampleDO::getContactPhone, ErpSampleRespVO::getContactPhone)
                .selectAs(ErpSampleDO::getAddress, ErpSampleRespVO::getAddress)
                .selectAs(ErpSampleDO::getRemark, ErpSampleRespVO::getRemark)
                //.selectAs(ErpSampleDO::getComboProductId, ErpSampleRespVO::getComboProductId)
                .selectAs(ErpSampleDO::getProductSpec, ErpSampleRespVO::getProductSpec)
                .selectAs(ErpSampleDO::getProductQuantity, ErpSampleRespVO::getProductQuantity)
                .selectAs(ErpSampleDO::getCustomerName, ErpSampleRespVO::getCustomerName)
                .selectAs(ErpSampleDO::getSampleStatus, ErpSampleRespVO::getSampleStatus)
                .selectAs(ErpSampleDO::getReference, ErpSampleRespVO::getReference)
                .selectAs(ErpSampleDO::getCreator, ErpSampleRespVO::getCreator)
                .selectAs(ErpSampleDO::getCreateTime, ErpSampleRespVO::getCreateTime);
        query.leftJoin(ErpComboProductDO.class, ErpComboProductDO::getId,ErpSampleDO::getComboProductId) // 左连接组合产品表
                .selectAs(ErpComboProductDO::getShippingCode, ErpSampleRespVO::getShippingCode) // 选择组合产品表的发货编码字段
                .selectAs(ErpComboProductDO::getNo, ErpSampleRespVO::getComboProductId) // 选择组合产品表的发货编码字段
                .selectAs(ErpComboProductDO::getName, ErpSampleRespVO::getComboProductName); // 选择组合产品表的名称字段作为组合产品名称字段的映射值。
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
