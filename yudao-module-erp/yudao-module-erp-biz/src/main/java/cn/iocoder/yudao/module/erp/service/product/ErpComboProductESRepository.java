package cn.iocoder.yudao.module.erp.service.product;

import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErpComboProductESRepository
        extends ElasticsearchRepository<ErpComboProductES, Long> {
}
