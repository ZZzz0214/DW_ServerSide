package cn.iocoder.yudao.module.erp.service.product;

import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErpComboProductESRepository
        extends ElasticsearchRepository<ErpComboProductES, Long> {

    // 添加按编号查询的方法
    Optional<ErpComboProductES> findByNo(String no);


    // 添加按编号列表批量查询的方法
    List<ErpComboProductES> findByNoIn(List<String> nos);

    // 添加按产品名称模糊查询的方法
    List<ErpComboProductES> findByNameContaining(String name);

}
