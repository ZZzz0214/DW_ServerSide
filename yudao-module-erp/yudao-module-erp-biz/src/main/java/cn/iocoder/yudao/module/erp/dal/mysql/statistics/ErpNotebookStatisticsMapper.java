package cn.iocoder.yudao.module.erp.dal.mysql.statistics;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpNotebookStatisticsReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.notebook.ErpNotebookDO;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;
import cn.hutool.core.util.StrUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ERP 记事本统计 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpNotebookStatisticsMapper extends BaseMapperX<ErpNotebookDO> {

    /**
     * 获得任务人员列表
     */
    default List<String> selectTaskPersonList(String keyword) {
        // 获取所有记录，然后在Java中过滤、去重和排序
        List<ErpNotebookDO> allRecords = selectList(new LambdaQueryWrapperX<ErpNotebookDO>()
                .isNotNull(ErpNotebookDO::getTaskPerson)
                .ne(ErpNotebookDO::getTaskPerson, ""));
        
        return allRecords.stream()
                .map(ErpNotebookDO::getTaskPerson)
                .filter(taskPerson -> cn.hutool.core.util.StrUtil.isBlank(keyword) || 
                        taskPerson.contains(keyword))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获得记事本列表（用于统计）
     */
    default List<ErpNotebookDO> selectListForStatistics(ErpNotebookStatisticsReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ErpNotebookDO>()
                .eqIfPresent(ErpNotebookDO::getTaskPerson, reqVO.getTaskPerson())
                .geIfPresent(ErpNotebookDO::getCreateTime, reqVO.getBeginTime())
                .leIfPresent(ErpNotebookDO::getCreateTime, reqVO.getEndTime())
                .orderByAsc(ErpNotebookDO::getTaskPerson, ErpNotebookDO::getTaskStatus));
    }
} 