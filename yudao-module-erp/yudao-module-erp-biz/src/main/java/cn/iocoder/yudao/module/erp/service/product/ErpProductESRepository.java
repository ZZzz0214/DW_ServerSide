package cn.iocoder.yudao.module.erp.service.product;


import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ErpProductESRepository extends ElasticsearchRepository<ErpProductESDO, Long> {
}
