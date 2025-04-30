package cn.iocoder.yudao.module.erp.dal.mysql.wholesale;


import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesalePurchaseDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;

import java.util.Collection;

@Mapper
public interface ErpWholesalePurchaseMapper extends BaseMapperX<ErpWholesalePurchaseDO> {

    default ErpWholesalePurchaseDO selectByBaseId(Long baseId) {
        return selectOne(ErpWholesalePurchaseDO::getBaseId, baseId);
    }

//    default void deleteByBaseIds(Collection<Long> baseIds) {
//        delete(new MPJLambdaWrapperX<ErpWholesalePurchaseDO>()
//                .in(ErpWholesalePurchaseDO::getBaseId, baseIds));
//    }
    default void deleteByBaseIds(Collection<Long> baseIds) {
        delete(new QueryWrapper<ErpWholesalePurchaseDO>()
                .in("base_id", baseIds));
    }
}
