


package cn.iocoder.yudao.module.erp.dal.mysql.wholesale;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleSaleDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;

import java.util.Collection;

@Mapper
public interface ErpWholesaleSaleMapper extends BaseMapperX<ErpWholesaleSaleDO> {

    default ErpWholesaleSaleDO selectByBaseId(Long baseId) {
        return selectOne(ErpWholesaleSaleDO::getBaseId, baseId);
    }

//    default void deleteByBaseIds(Collection<Long> baseIds) {
//        delete(new MPJLambdaWrapperX<ErpWholesaleSaleDO>()
//                .in(ErpWholesaleSaleDO::getBaseId, baseIds));
//    }
    default void deleteByBaseIds(Collection<Long> baseIds) {
        delete(new QueryWrapper<ErpWholesaleSaleDO>()
                .in("base_id", baseIds));
    }
}
