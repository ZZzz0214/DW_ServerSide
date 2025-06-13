package cn.iocoder.yudao.module.erp.service.wholesale;


import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleCombinedESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErpWholesaleCombinedESRepository extends ElasticsearchRepository<ErpWholesaleCombinedESDO, Long> {

    /**
     * 根据订单编号查询
     * @param no 订单编号
     * @return 批发合并记录
     */
    ErpWholesaleCombinedESDO findByNo(String no);

    /**
     * 根据订单编号删除
     * @param no 订单编号
     */
    void deleteByNo(String no);
}
