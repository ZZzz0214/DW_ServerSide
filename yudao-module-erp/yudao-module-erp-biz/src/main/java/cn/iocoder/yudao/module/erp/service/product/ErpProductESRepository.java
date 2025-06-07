package cn.iocoder.yudao.module.erp.service.product;


import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Collection;
import java.util.List;

public interface ErpProductESRepository extends ElasticsearchRepository<ErpProductESDO, Long> {

        // 添加根据编号列表查询的方法
        List<ErpProductESDO> findAllByNoIn(Collection<String> nos);
}
