package cn.iocoder.yudao.module.erp.service.product;

import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductItemES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// 项表 Repository
@Repository
public interface ErpComboProductItemESRepository
        extends ElasticsearchRepository<ErpComboProductItemES, Long> {
    Iterable<ErpComboProductItemES> findAllByComboProductIdIn(List<Long> comboProductIds);
    
    /**
     * 根据产品ID查询所有包含该产品的组品明细
     * @param itemProductId 产品ID
     * @return 组品明细列表
     */
    List<ErpComboProductItemES> findAllByItemProductId(Long itemProductId);
}
