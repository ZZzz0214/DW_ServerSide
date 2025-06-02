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
}
