package cn.iocoder.yudao.module.erp.dal.mysql.distribution;



import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionPurchaseDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;

import java.util.Collection;

@Mapper
public interface ErpDistributionPurchaseMapper extends BaseMapperX<ErpDistributionPurchaseDO> {

    default ErpDistributionPurchaseDO selectByBaseId(Long baseId) {
        return selectOne(ErpDistributionPurchaseDO::getBaseId, baseId);
    }

//    default void deleteByBaseIds(Collection<Long> baseIds) {
//        delete(new MPJLambdaWrapperX<ErpDistributionPurchaseDO>()
//                .in(ErpDistributionPurchaseDO::getBaseId, baseIds));
//    }
default void deleteByBaseIds(Collection<Long> baseIds) {
    delete(new QueryWrapper<ErpDistributionPurchaseDO>()
            .in("base_id", baseIds));
}
}
