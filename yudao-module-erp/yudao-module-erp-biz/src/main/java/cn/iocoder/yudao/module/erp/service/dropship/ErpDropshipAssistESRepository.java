package cn.iocoder.yudao.module.erp.service.dropship;


import cn.iocoder.yudao.module.erp.dal.dataobject.dropship.ErpDropshipAssistESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ErpDropshipAssistESRepository extends ElasticsearchRepository<ErpDropshipAssistESDO, Long> {

    // 根据编号列表查询
    List<ErpDropshipAssistESDO> findByNoIn(Set<String> nos);

    // 根据编号查询
    ErpDropshipAssistESDO findByNo(String no);
}
