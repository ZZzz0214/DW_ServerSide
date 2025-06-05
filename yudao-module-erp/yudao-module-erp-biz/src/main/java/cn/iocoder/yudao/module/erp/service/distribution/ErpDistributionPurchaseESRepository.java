package cn.iocoder.yudao.module.erp.service.distribution;


import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionPurchaseESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpDistributionPurchaseESRepository extends ElasticsearchRepository<ErpDistributionPurchaseESDO, Long> {
    Optional<ErpDistributionPurchaseESDO> findByBaseId(Long baseId);

    Optional<ErpDistributionPurchaseESDO> findByBaseIdAndPurchaseAuditStatus(Long baseId, Integer purchaseAuditStatus);
}