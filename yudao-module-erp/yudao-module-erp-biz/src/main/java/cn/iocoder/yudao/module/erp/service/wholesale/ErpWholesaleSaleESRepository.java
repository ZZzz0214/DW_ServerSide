package cn.iocoder.yudao.module.erp.service.wholesale;


import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleSaleESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpWholesaleSaleESRepository extends ElasticsearchRepository<ErpWholesaleSaleESDO, Long> {
    void deleteByBaseId(Long baseId);

    Optional<ErpWholesaleSaleESDO> findByBaseId(Long baseId);

    Optional<ErpWholesaleSaleESDO> findByBaseIdAndSaleAuditStatus(Long baseId, Integer saleAuditStatus);
}
