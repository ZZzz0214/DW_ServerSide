package cn.iocoder.yudao.module.erp.dal.mysql.distribution;



import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionSaleDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;

import java.util.Collection;

@Mapper
public interface ErpDistributionSaleMapper extends BaseMapperX<ErpDistributionSaleDO> {

    default ErpDistributionSaleDO selectByBaseId(Long baseId) {
        return selectOne(ErpDistributionSaleDO::getBaseId, baseId);
    }

//    default void deleteByBaseIds(Collection<Long> baseIds) {
//        delete(new MPJLambdaWrapperX<ErpDistributionSaleDO>()
//                .in(ErpDistributionSaleDO::getBaseId, baseIds));
//    }
default void deleteByBaseIds(Collection<Long> baseIds) {
    delete(new QueryWrapper<ErpDistributionSaleDO>()
            .in("base_id", baseIds));
}
}
