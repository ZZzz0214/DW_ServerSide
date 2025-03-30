package cn.iocoder.yudao.module.erp.dal.mysql.product;

import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductItemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ErpComboProductItemMapper extends BaseMapper<ErpComboProductItemDO> {
    void deleteByComboProductId(Long comboProductId);
}