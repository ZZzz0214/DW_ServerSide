package cn.iocoder.yudao.module.erp.service.distribution;


import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionCombinedESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpDistributionCombinedESRepository extends ElasticsearchRepository<ErpDistributionCombinedESDO, Long> {

    /**
    /**
     * 根据订单编号查询
     * @param no 订单编号
     * @return 代发合并记录
     */
    ErpDistributionCombinedESDO findByNo(String no);


    /**
     * 根据订单编号删除
     * @param no 订单编号
     */
    void deleteByNo(String no);

}
