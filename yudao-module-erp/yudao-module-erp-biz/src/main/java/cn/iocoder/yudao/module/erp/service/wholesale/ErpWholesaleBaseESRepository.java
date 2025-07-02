package cn.iocoder.yudao.module.erp.service.wholesale;


import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleBaseESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ErpWholesaleBaseESRepository extends ElasticsearchRepository<ErpWholesaleBaseESDO, Long> {
    // 根据创建时间范围查询
    List<ErpWholesaleBaseESDO> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}