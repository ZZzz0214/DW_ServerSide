package cn.iocoder.yudao.module.erp.service.distribution;


import cn.iocoder.yudao.module.erp.dal.dataobject.distribution.ErpDistributionCombinedESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * 根据订单编号列表查询
     * @param noList 订单编号列表
     * @return 代发合并记录列表
     */
    List<ErpDistributionCombinedESDO> findByNoIn(List<String> noList);

    /**
     * 根据组品ID列表查询所有代发订单
     * @param comboProductIds 组品ID列表
     * @return 代发订单列表
     */
    List<ErpDistributionCombinedESDO> findAllByComboProductIdIn(List<Long> comboProductIds);

}
