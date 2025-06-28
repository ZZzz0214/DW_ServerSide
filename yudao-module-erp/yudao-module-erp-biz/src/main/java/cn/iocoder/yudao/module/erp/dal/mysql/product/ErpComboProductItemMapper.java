package cn.iocoder.yudao.module.erp.dal.mysql.product;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductItemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


@Mapper
public interface ErpComboProductItemMapper extends BaseMapper<ErpComboProductItemDO> {
    default List<ErpComboProductItemDO> selectByComboProductId(Long comboProductId) {
        return selectList(new LambdaQueryWrapperX<ErpComboProductItemDO>()
                .eq(ErpComboProductItemDO::getComboProductId, comboProductId));
    }

    default List<ErpComboProductItemDO> selectByComboProductIds(Collection<Long> comboProductIds) {
        if (CollUtil.isEmpty(comboProductIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpComboProductItemDO>()
                .in(ErpComboProductItemDO::getComboProductId, comboProductIds));
    }

    default void deleteByComboProductId(Long comboProductId) {
        delete(new LambdaQueryWrapperX<ErpComboProductItemDO>()
                .eq(ErpComboProductItemDO::getComboProductId, comboProductId));
    }

    default void insertBatch(List<ErpComboProductItemDO> list) {
        list.forEach(this::insert);
    }
}
