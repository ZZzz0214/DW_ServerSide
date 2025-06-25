package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpNotebookStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpNotebookStatisticsRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.notebook.ErpNotebookDO;
import cn.iocoder.yudao.module.erp.dal.mysql.statistics.ErpNotebookStatisticsMapper;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ERP 记事本统计 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpNotebookStatisticsServiceImpl implements ErpNotebookStatisticsService {

    @Resource
    private ErpNotebookStatisticsMapper notebookStatisticsMapper;

    @Resource
    private DictDataApi dictDataApi;

    // 字典类型常量
    private static final String DICT_TYPE_NOTEBOOK_STATUS = "erp_notebook_status";
    private static final String DICT_TYPE_SYSTEM_USER_LIST = "system_user_list";

    @Override
    public ErpNotebookStatisticsRespVO getNotebookStatistics(ErpNotebookStatisticsReqVO reqVO) {
        ErpNotebookStatisticsRespVO respVO = new ErpNotebookStatisticsRespVO();

        // 1. 获取记事本数据列表
        List<ErpNotebookDO> notebookList = notebookStatisticsMapper.selectListForStatistics(reqVO);

        // 2. 构建总体统计
        ErpNotebookStatisticsRespVO.TotalStatistics totalStatistics = buildTotalStatistics(notebookList);
        respVO.setTotalStatistics(totalStatistics);

        // 3. 构建人员统计
        List<ErpNotebookStatisticsRespVO.PersonStatistics> personStatisticsList = buildPersonStatistics(notebookList);
        respVO.setPersonStatisticsList(personStatisticsList);

        return respVO;
    }

    @Override
    public List<String> getTaskPersonList(String keyword) {
        // 从字典中获取所有用户选项，然后根据keyword过滤
        List<DictDataRespDTO> dictDataList = dictDataApi.getDictDataList(DICT_TYPE_SYSTEM_USER_LIST);
        if (dictDataList == null) {
            return new ArrayList<>();
        }
        
        return dictDataList.stream()
                .map(DictDataRespDTO::getLabel)
                .filter(label -> cn.hutool.core.util.StrUtil.isBlank(keyword) || label.contains(keyword))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<ErpNotebookStatisticsRespVO.TaskPersonOption> getTaskPersonOptions(String keyword) {
        // 从字典中获取所有用户选项
        List<DictDataRespDTO> dictDataList = dictDataApi.getDictDataList(DICT_TYPE_SYSTEM_USER_LIST);
        if (dictDataList == null) {
            return new ArrayList<>();
        }

        return dictDataList.stream()
                .map(dictData -> {
                    ErpNotebookStatisticsRespVO.TaskPersonOption option = new ErpNotebookStatisticsRespVO.TaskPersonOption();
                    option.setValue(dictData.getValue());
                    option.setLabel(dictData.getLabel());
                    return option;
                })
                .filter(option -> cn.hutool.core.util.StrUtil.isBlank(keyword) || option.getLabel().contains(keyword))
                .distinct()
                .sorted(Comparator.comparing(ErpNotebookStatisticsRespVO.TaskPersonOption::getLabel))
                .collect(Collectors.toList());
    }

    /**
     * 构建总体统计 - 动态统计所有任务状态
     */
    private ErpNotebookStatisticsRespVO.TotalStatistics buildTotalStatistics(List<ErpNotebookDO> notebookList) {
        ErpNotebookStatisticsRespVO.TotalStatistics statistics = new ErpNotebookStatisticsRespVO.TotalStatistics();
        
        long totalTaskCount = notebookList.size();
        
        // 动态统计各个状态的任务数量
        Map<Integer, Long> statusCountMap = notebookList.stream()
                .collect(Collectors.groupingBy(ErpNotebookDO::getTaskStatus, Collectors.counting()));
        
        // 设置基础统计数据
        statistics.setTotalTaskCount(totalTaskCount);
        statistics.setPendingTaskCount(statusCountMap.getOrDefault(0, 0L));
        statistics.setInProgressTaskCount(statusCountMap.getOrDefault(1, 0L));
        statistics.setCompletedTaskCount(statusCountMap.getOrDefault(2, 0L));
        
        // 统计人员数量
        long totalPersonCount = notebookList.stream().map(ErpNotebookDO::getTaskPerson).distinct().count();
        statistics.setTotalPersonCount(totalPersonCount);
        
        return statistics;
    }

    /**
     * 构建人员统计列表
     */
    private List<ErpNotebookStatisticsRespVO.PersonStatistics> buildPersonStatistics(List<ErpNotebookDO> notebookList) {
        // 按人员分组
        Map<String, List<ErpNotebookDO>> personGroupMap = notebookList.stream()
                .collect(Collectors.groupingBy(ErpNotebookDO::getTaskPerson));

        List<ErpNotebookStatisticsRespVO.PersonStatistics> result = new ArrayList<>();
        
        for (Map.Entry<String, List<ErpNotebookDO>> entry : personGroupMap.entrySet()) {
            String taskPersonValue = entry.getKey();
            List<ErpNotebookDO> personTasks = entry.getValue();
            
            // 将字典值转换为字典标签
            String taskPersonLabel = DictFrameworkUtils.getDictDataLabel(DICT_TYPE_SYSTEM_USER_LIST, taskPersonValue);
            if (taskPersonLabel == null || taskPersonLabel.isEmpty()) {
                taskPersonLabel = taskPersonValue; // 如果找不到字典标签，使用原值
            }
            
            ErpNotebookStatisticsRespVO.PersonStatistics personStatistics = buildSinglePersonStatistics(taskPersonLabel, personTasks);
            result.add(personStatistics);
        }
        
        // 按任务人员名称排序
        result.sort(Comparator.comparing(ErpNotebookStatisticsRespVO.PersonStatistics::getTaskPerson));
        
        return result;
    }

    /**
     * 构建单个人员统计 - 动态处理所有任务状态
     */
    private ErpNotebookStatisticsRespVO.PersonStatistics buildSinglePersonStatistics(String taskPerson, List<ErpNotebookDO> personTasks) {
        ErpNotebookStatisticsRespVO.PersonStatistics personStatistics = new ErpNotebookStatisticsRespVO.PersonStatistics();
        personStatistics.setTaskPerson(taskPerson);
        
        long totalTaskCount = personTasks.size();
        
        // 动态按状态分组统计
        Map<Integer, Long> statusCountMap = personTasks.stream()
                .collect(Collectors.groupingBy(ErpNotebookDO::getTaskStatus, Collectors.counting()));
        
        // 设置基础统计数据（保持向后兼容）
        personStatistics.setTotalTaskCount(totalTaskCount);
        personStatistics.setPendingTaskCount(statusCountMap.getOrDefault(0, 0L));
        personStatistics.setInProgressTaskCount(statusCountMap.getOrDefault(1, 0L));
        personStatistics.setCompletedTaskCount(statusCountMap.getOrDefault(2, 0L));
        
        // 构建状态分布 - 包含所有存在的状态
        List<ErpNotebookStatisticsRespVO.StatusDistribution> statusDistributions = new ArrayList<>();
        
        for (Map.Entry<Integer, Long> entry : statusCountMap.entrySet()) {
            Integer taskStatus = entry.getKey();
            Long taskCount = entry.getValue();
            
            ErpNotebookStatisticsRespVO.StatusDistribution distribution = new ErpNotebookStatisticsRespVO.StatusDistribution();
            distribution.setTaskStatus(taskStatus);
            
            // 从字典获取状态名称
            String statusName = DictFrameworkUtils.getDictDataLabel(DICT_TYPE_NOTEBOOK_STATUS, String.valueOf(taskStatus));
            if (statusName == null || statusName.isEmpty()) {
                // 如果字典中找不到，使用默认名称
                statusName = getDefaultStatusName(taskStatus);
            }
            distribution.setStatusName(statusName);
            distribution.setTaskCount(taskCount);
            
            // 计算百分比
            if (totalTaskCount > 0) {
                double percentage = (taskCount * 100.0) / totalTaskCount;
                distribution.setPercentage(BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP).doubleValue());
            } else {
                distribution.setPercentage(0.0);
            }
            
            statusDistributions.add(distribution);
        }
        
        // 按状态排序
        statusDistributions.sort(Comparator.comparing(ErpNotebookStatisticsRespVO.StatusDistribution::getTaskStatus));
        personStatistics.setStatusDistributions(statusDistributions);
        
        // 计算完成率（状态2为已完成）
        if (totalTaskCount > 0) {
            long completedCount = statusCountMap.getOrDefault(2, 0L);
            double completionRate = (completedCount * 100.0) / totalTaskCount;
            personStatistics.setCompletionRate(BigDecimal.valueOf(completionRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
        } else {
            personStatistics.setCompletionRate(0.0);
        }
        
        return personStatistics;
    }

    /**
     * 获取默认状态名称
     */
    private String getDefaultStatusName(Integer status) {
        switch (status) {
            case 0: return "未完成";
            case 1: return "正在做";
            case 2: return "已完成";
            default: return "状态" + status;
        }
    }

}
