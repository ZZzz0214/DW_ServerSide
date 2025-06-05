package cn.iocoder.yudao.module.erp.service.sale;


import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpSalePriceESRepository extends ElasticsearchRepository<ErpSalePriceESDO, Long> {
    Optional<ErpSalePriceESDO> findByGroupProductIdAndCustomerName(Long groupProductId, String customerName);
}