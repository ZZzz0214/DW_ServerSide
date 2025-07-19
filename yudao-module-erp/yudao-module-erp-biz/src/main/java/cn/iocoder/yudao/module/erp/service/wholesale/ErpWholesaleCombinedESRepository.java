package cn.iocoder.yudao.module.erp.service.wholesale;


import cn.iocoder.yudao.module.erp.dal.dataobject.wholesale.ErpWholesaleCombinedESDO;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 根据组品ID列表查询所有批发订单
     * @param comboProductIds 组品ID列表
     * @return 批发订单列表
     */
    List<ErpWholesaleCombinedESDO> findAllByComboProductIdIn(List<Long> comboProductIds);

    /**
     * 根据创建时间范围查询
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 批发订单列表
     */
    List<ErpWholesaleCombinedESDO> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据创建时间范围查询（字符串格式）
     * @param startTime 开始时间字符串
     * @param endTime 结束时间字符串
     * @return 批发订单列表
     */
    // 使用简单的between查询，避免复杂条件
    @Query("{ \"range\": { \"create_time\": { \"gte\": \"?0\", \"lte\": \"?1\" }}}")
    List<ErpWholesaleCombinedESDO> findByCreateTimeStringBetween(String startTime, String endTime);
}
