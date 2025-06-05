package cn.iocoder.yudao.module.erp.service.distribution;

import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionSaleESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpDistributionSaleESRepository extends ElasticsearchRepository<ErpDistributionSaleESDO, Long> {
    Optional<ErpDistributionSaleESDO> findByBaseId(Long baseId);

    Optional<ErpDistributionSaleESDO> findByBaseIdAndSaleAuditStatus(Long baseId, Integer saleAuditStatus);
    
    void deleteByBaseId(Long baseId);
}
