package cn.iocoder.yudao.module.erp.service.distribution;

import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionBaseESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErpDistributionBaseESRepository extends ElasticsearchRepository<ErpDistributionBaseESDO, Long> {
}
