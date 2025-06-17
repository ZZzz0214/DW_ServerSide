package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpFinanceService {

    // 新增财务记录
    Long createFinance(@Valid ErpFinanceSaveReqVO createReqVO, String currentUsername);

    // 修改财务记录
    void updateFinance(@Valid ErpFinanceSaveReqVO updateReqVO, String currentUsername);

    // 删除财务记录
    void deleteFinance(List<Long> ids, String currentUsername);

    // 根据id查询财务记录
    ErpFinanceDO getFinance(Long id, String currentUsername);

    // 根据id列表查询财务记录
    List<ErpFinanceDO> getFinanceList(Collection<Long> ids, String currentUsername);

    // 校验财务记录有效性
    ErpFinanceDO validateFinance(Long id, String currentUsername);

    // 获取财务记录VO列表
    List<ErpFinanceRespVO> getFinanceVOList(Collection<Long> ids, String currentUsername);

    // 获取财务记录VO Map
    Map<Long, ErpFinanceRespVO> getFinanceVOMap(Collection<Long> ids, String currentUsername);

    // 获取财务记录VO分页
    PageResult<ErpFinanceRespVO> getFinanceVOPage(ErpFinancePageReqVO pageReqVO, String currentUsername);

    // 导入财务记录
    ErpFinanceImportRespVO importFinanceList(List<ErpFinanceImportExcelVO> importList, boolean isUpdateSupport);

    // 审核财务记录
    void auditFinance(ErpFinanceAuditReqVO auditReqVO, String currentUsername);

    // 反审核财务记录
    void unauditFinance(List<Long> ids, String currentUsername);
} 