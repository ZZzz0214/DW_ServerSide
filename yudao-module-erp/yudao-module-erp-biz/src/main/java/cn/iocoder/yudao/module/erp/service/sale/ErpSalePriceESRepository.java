package cn.iocoder.yudao.module.erp.service.sale;


import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceESDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErpSalePriceESRepository extends ElasticsearchRepository<ErpSalePriceESDO, Long> {

    /**
     * 根据组品ID和客户名称查询销售价格
     */
    Optional<ErpSalePriceESDO> findByGroupProductIdAndCustomerName(Long groupProductId, String customerName);

    /**
     * 根据组品ID查询销售价格列表
     */
    List<ErpSalePriceESDO> findByGroupProductId(Long groupProductId);

    /**
     * 根据客户名称查询销售价格列表
     */
    List<ErpSalePriceESDO> findByCustomerName(String customerName);

    /**
     * 根据状态查询销售价格列表
     */
    //List<ErpSalePriceESDO> findByStatus(Integer status);

}
