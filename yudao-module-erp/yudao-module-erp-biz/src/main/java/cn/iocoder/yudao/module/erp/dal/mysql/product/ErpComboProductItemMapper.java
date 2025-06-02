package cn.iocoder.yudao.module.erp.dal.mysql.product;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductItemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface ErpComboProductItemMapper extends BaseMapper<ErpComboProductItemDO> {
    default List<ErpComboProductItemDO> selectByComboProductId(Long comboProductId) {
        return selectList(new LambdaQueryWrapperX<ErpComboProductItemDO>()
                .eq(ErpComboProductItemDO::getComboProductId, comboProductId));
    }

    default void deleteByComboProductId(Long comboProductId) {
        delete(new LambdaQueryWrapperX<ErpComboProductItemDO>()
                .eq(ErpComboProductItemDO::getComboProductId, comboProductId));
    }

    default void insertBatch(List<ErpComboProductItemDO> list) {
        list.forEach(this::insert);
    }
}
