package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.sample.ErpSampleSummaryRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sample.ErpSampleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sample.ErpSampleMapper;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.module.system.enums.DictTypeConstants.ERP_SAMPLE_STATUS;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 样品统计 Service 实现类
 */
@Service
@Slf4j
public class ErpSampleStatisticsServiceImpl implements ErpSampleStatisticsService {

    @Resource
    private ErpSampleMapper sampleMapper;

    @Resource
    private DictDataApi dictDataApi;

    @Override
    public ErpSampleSummaryRespVO getSampleSummary(LocalDateTime beginTime, LocalDateTime endTime, String customerName) {
        log.info("开始获取样品统计，时间范围：{} 到 {}，客户名称：{}", beginTime, endTime, customerName);

        // 1. 查询指定时间范围内的所有样品
        LambdaQueryWrapperX<ErpSampleDO> queryWrapper = new LambdaQueryWrapperX<ErpSampleDO>();
        queryWrapper.between(ErpSampleDO::getCreateTime, beginTime, endTime)
                .orderByDesc(ErpSampleDO::getCreateTime);
        
        // 如果指定了客户名称，添加客户名称搜索条件
        if (customerName != null && !customerName.trim().isEmpty()) {
            queryWrapper.like(ErpSampleDO::getCustomerName, customerName.trim());
        }
        
        List<ErpSampleDO> samples = sampleMapper.selectList(queryWrapper);

        log.info("查询到样品数量：{}", samples.size());

        // 2. 获取样品状态字典数据
        Map<String, String> statusDictMap = getSampleStatusDictMap();

        // 3. 按样品状态统计
        Map<String, Integer> statusCount = new HashMap<>();
        Map<String, Map<String, Integer>> customerStatusCount = new HashMap<>();

        for (ErpSampleDO sample : samples) {
            // 获取样品状态名称
            String sampleStatusName = getSampleStatusName(sample.getSampleStatus(), statusDictMap);
            
            // 统计总体状态
            statusCount.put(sampleStatusName, statusCount.getOrDefault(sampleStatusName, 0) + 1);
            
            // 按客户统计
            String sampleCustomerName = sample.getCustomerName() != null ? sample.getCustomerName() : "未知客户";
            customerStatusCount.computeIfAbsent(sampleCustomerName, k -> new HashMap<>());
            
            Map<String, Integer> customerStatus = customerStatusCount.get(sampleCustomerName);
            customerStatus.put(sampleStatusName, customerStatus.getOrDefault(sampleStatusName, 0) + 1);
        }

        // 4. 构建客户统计列表
        List<ErpSampleSummaryRespVO.CustomerSampleStat> customerStats = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : customerStatusCount.entrySet()) {
            String entryCustomerName = entry.getKey();
            Map<String, Integer> customerStatus = entry.getValue();
            
            // 计算该客户总数量
            int totalCount = customerStatus.values().stream().mapToInt(Integer::intValue).sum();
            
            ErpSampleSummaryRespVO.CustomerSampleStat customerStat = new ErpSampleSummaryRespVO.CustomerSampleStat();
            customerStat.setCustomerName(entryCustomerName);
            customerStat.setStatusCount(customerStatus);
            customerStat.setTotalCount(totalCount);
            
            customerStats.add(customerStat);
        }

        // 5. 按客户总数量排序
        customerStats.sort((a, b) -> Integer.compare(b.getTotalCount(), a.getTotalCount()));

        // 6. 构建客户选项列表
        List<ErpSampleSummaryRespVO.CustomerOption> customerOptions = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : customerStatusCount.entrySet()) {
            String optionCustomerName = entry.getKey();
            Map<String, Integer> customerStatus = entry.getValue();
            int totalCount = customerStatus.values().stream().mapToInt(Integer::intValue).sum();
            
            ErpSampleSummaryRespVO.CustomerOption customerOption = new ErpSampleSummaryRespVO.CustomerOption();
            customerOption.setCustomerName(optionCustomerName);
            customerOption.setSampleCount(totalCount);
            customerOptions.add(customerOption);
        }
        
        // 按样品数量排序
        customerOptions.sort((a, b) -> Integer.compare(b.getSampleCount(), a.getSampleCount()));

        // 7. 构建返回结果
        ErpSampleSummaryRespVO result = new ErpSampleSummaryRespVO();
        result.setStatusCount(statusCount);
        result.setCustomerStats(customerStats);
        result.setTotalCount(samples.size());
        result.setCustomerOptions(customerOptions);

        // 添加调试信息
        log.info("样品统计完成，总数量：{}，客户数量：{}", result.getTotalCount(), customerStats.size());
        log.info("状态统计详情：{}", statusCount);
        
        // 验证总数计算
        int calculatedTotal = statusCount.values().stream().mapToInt(Integer::intValue).sum();
        log.info("状态统计总数：{}，查询样品总数：{}", calculatedTotal, samples.size());
        
        return result;
    }

    /**
     * 获取样品状态字典映射
     */
    private Map<String, String> getSampleStatusDictMap() {
        try {
            // 获取样品状态字典数据
            List<DictDataRespDTO> dictDataList = 
                dictDataApi.getDictDataList(ERP_SAMPLE_STATUS);
            
            if (dictDataList != null && !dictDataList.isEmpty()) {
                return dictDataList.stream()
                        .collect(Collectors.toMap(
                                DictDataRespDTO::getValue,
                                DictDataRespDTO::getLabel,
                                (existing, replacement) -> existing
                        ));
            }
        } catch (Exception e) {
            log.warn("获取样品状态字典数据失败", e);
        }
        
        // 如果获取字典失败，返回空映射
        return new HashMap<>();
    }

    /**
     * 获取样品状态名称
     */
    private String getSampleStatusName(Integer status, Map<String, String> statusDictMap) {
        if (status == null) {
            // 如果状态为空，返回字典中的第一个状态作为默认值
            return statusDictMap.values().iterator().hasNext() ? 
                   statusDictMap.values().iterator().next() : "未知状态";
        }
        
        String statusStr = String.valueOf(status);
        String statusName = statusDictMap.get(statusStr);
        
        if (statusName != null) {
            return statusName;
        }
        
        // 如果字典中没有找到，返回未知状态
        return "未知状态";
    }
} 