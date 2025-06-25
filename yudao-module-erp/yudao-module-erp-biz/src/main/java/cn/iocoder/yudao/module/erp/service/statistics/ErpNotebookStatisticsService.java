package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpNotebookStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.ErpNotebookStatisticsRespVO;

import javax.validation.Valid;
import java.util.List;

/**
 * ERP 记事本统计 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpNotebookStatisticsService {

    /**
     * 获得记事本统计
     *
     * @param reqVO 请求参数
     * @return 统计结果
     */
    ErpNotebookStatisticsRespVO getNotebookStatistics(@Valid ErpNotebookStatisticsReqVO reqVO);

    /**
     * 获得任务人员列表
     *
     * @param keyword 关键字
     * @return 任务人员列表
     */
    List<String> getTaskPersonList(String keyword);

    /**
     * 获得任务人员选项列表
     *
     * @param keyword 搜索关键词
     * @return 任务人员选项列表
     */
    List<ErpNotebookStatisticsRespVO.TaskPersonOption> getTaskPersonOptions(String keyword);

} 