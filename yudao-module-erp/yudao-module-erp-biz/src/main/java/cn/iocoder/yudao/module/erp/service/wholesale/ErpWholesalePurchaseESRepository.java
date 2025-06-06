package cn.iocoder.yudao.module.erp.service.wholesale;


import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesalePurchaseESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpWholesalePurchaseESRepository extends ElasticsearchRepository<ErpWholesalePurchaseESDO, Long> {
    void deleteByBaseId(Long baseId);
    Optional<ErpWholesalePurchaseESDO> findByBaseId(Long baseId);

    Optional<ErpWholesalePurchaseESDO> findByBaseIdAndPurchaseAuditStatus(Long baseId, Integer purchaseAuditStatus);
}
