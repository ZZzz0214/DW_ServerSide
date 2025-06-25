package cn.iocoder.yudao.module.erp.dal.mysql.dropship;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.dropship.vo.ErpDropshipAssistPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.dropship.vo.ErpDropshipAssistRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.dropship.ErpDropshipAssistDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpDropshipAssistMapper extends BaseMapperX<ErpDropshipAssistDO> {

    default PageResult<ErpDropshipAssistRespVO> selectPage(ErpDropshipAssistPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpDropshipAssistDO> query = new MPJLambdaWrapperX<ErpDropshipAssistDO>()
                .likeIfPresent(ErpDropshipAssistDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpDropshipAssistDO::getOriginalProduct, reqVO.getOriginalProduct())
                .likeIfPresent(ErpDropshipAssistDO::getOriginalSpec, reqVO.getOriginalSpec())
                .likeIfPresent(ErpDropshipAssistDO::getProductSpec, reqVO.getProductSpec())
                .likeIfPresent(ErpDropshipAssistDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpDropshipAssistDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(ErpDropshipAssistDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpDropshipAssistDO::getId);

        // 联表查询组品信息 - 使用LEFT JOIN确保即使组品编号为空也能查询到代发辅助记录
        query.leftJoin(ErpComboProductDO.class, ErpComboProductDO::getId, ErpDropshipAssistDO::getComboProductId);

        // 添加组品相关的查询条件 - 只有当查询条件不为空时才添加
        if (reqVO.getComboProductId() != null && !reqVO.getComboProductId().trim().isEmpty()) {
            query.like(ErpComboProductDO::getNo, reqVO.getComboProductId());
        }
        if (reqVO.getShippingCode() != null && !reqVO.getShippingCode().trim().isEmpty()) {
            query.like(ErpComboProductDO::getShippingCode, reqVO.getShippingCode());
        }
        if (reqVO.getProductName() != null && !reqVO.getProductName().trim().isEmpty()) {
            query.like(ErpComboProductDO::getName, reqVO.getProductName());
        }

        // 字段映射
        query.selectAs(ErpDropshipAssistDO::getId, ErpDropshipAssistRespVO::getId)
                .selectAs(ErpDropshipAssistDO::getNo, ErpDropshipAssistRespVO::getNo)
                .selectAs(ErpDropshipAssistDO::getOriginalProduct, ErpDropshipAssistRespVO::getOriginalProduct)
                .selectAs(ErpDropshipAssistDO::getOriginalSpec, ErpDropshipAssistRespVO::getOriginalSpec)
                .selectAs(ErpDropshipAssistDO::getOriginalQuantity, ErpDropshipAssistRespVO::getOriginalQuantity)
                .selectAs(ErpDropshipAssistDO::getComboProductId, ErpDropshipAssistRespVO::getComboProductId)
                .selectAs(ErpDropshipAssistDO::getProductSpec, ErpDropshipAssistRespVO::getProductSpec)
                .selectAs(ErpDropshipAssistDO::getProductQuantity, ErpDropshipAssistRespVO::getProductQuantity)
                .selectAs(ErpDropshipAssistDO::getRemark, ErpDropshipAssistRespVO::getRemark)
                .selectAs(ErpDropshipAssistDO::getStatus, ErpDropshipAssistRespVO::getStatus)
                .selectAs(ErpDropshipAssistDO::getCreator, ErpDropshipAssistRespVO::getCreator)
                .selectAs(ErpDropshipAssistDO::getCreateTime, ErpDropshipAssistRespVO::getCreateTime)
                .selectAs(ErpComboProductDO::getName, ErpDropshipAssistRespVO::getProductName)
                .selectAs(ErpComboProductDO::getNo, ErpDropshipAssistRespVO::getComboProductNo)
                .selectAs(ErpComboProductDO::getShortName, ErpDropshipAssistRespVO::getProductShortName)
                .selectAs(ErpComboProductDO::getShippingCode, ErpDropshipAssistRespVO::getShippingCode);

        return selectJoinPage(reqVO, ErpDropshipAssistRespVO.class, query);
    }

    default List<ErpDropshipAssistDO> selectListByNoIn(Collection<String> nos) {
        if (CollUtil.isEmpty(nos)) {
            return Collections.emptyList();
        }
        return selectList(ErpDropshipAssistDO::getNo, nos);
    }
    
    default ErpDropshipAssistDO selectByNo(String no) {
        return selectOne(ErpDropshipAssistDO::getNo, no);
    }

    /**
     * 根据字段组合查询是否存在重复记录
     * @param originalProduct 原表商品
     * @param originalSpec 原表规格
     * @param originalQuantity 原表数量
     * @param comboProductId 组品编号
     * @param productSpec 产品规格
     * @param productQuantity 产品数量
     * @param excludeId 排除的ID（用于更新时排除自己）
     * @return 是否存在重复记录
     */
    default ErpDropshipAssistDO selectByUniqueFields(String originalProduct, String originalSpec, 
                                                     Integer originalQuantity, String comboProductId, 
                                                     String productSpec, Integer productQuantity, 
                                                     Long excludeId) {
        MPJLambdaWrapperX<ErpDropshipAssistDO> query = new MPJLambdaWrapperX<ErpDropshipAssistDO>()
                .eq(ErpDropshipAssistDO::getOriginalProduct, originalProduct)
                .eq(ErpDropshipAssistDO::getOriginalSpec, originalSpec)
                .eq(ErpDropshipAssistDO::getOriginalQuantity, originalQuantity)
                .eq(ErpDropshipAssistDO::getProductSpec, productSpec)
                .eq(ErpDropshipAssistDO::getProductQuantity, productQuantity);
        
        // 处理组品编号可能为空的情况
        if (comboProductId == null || comboProductId.trim().isEmpty()) {
            query.isNull(ErpDropshipAssistDO::getComboProductId);
        } else {
            query.eq(ErpDropshipAssistDO::getComboProductId, comboProductId);
        }
        
        // 如果是更新操作，排除当前记录
        if (excludeId != null) {
            query.ne(ErpDropshipAssistDO::getId, excludeId);
        }
        
        return selectOne(query);
    }
}
