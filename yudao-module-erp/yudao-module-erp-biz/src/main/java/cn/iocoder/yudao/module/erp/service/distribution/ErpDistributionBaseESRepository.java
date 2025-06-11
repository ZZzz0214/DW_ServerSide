package cn.iocoder.yudao.module.erp.service.distribution;

import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionBaseESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErpDistributionBaseESRepository extends ElasticsearchRepository<ErpDistributionBaseESDO, Long> {

    // 根据编号列表查询
    List<ErpDistributionBaseESDO> findByNoIn(List<String> nos);
    // 根据编号查询
    ErpDistributionBaseESDO findByNo(String no);
}
