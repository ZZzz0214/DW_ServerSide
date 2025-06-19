package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAmountDO;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * ERP 财务金额 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpFinanceAmountService {

    /**
     * 创建财务金额
     *
     * @param createReqVO 创建信息
     * @param currentUsername 当前用户名
     * @return 编号
     */
    Long createFinanceAmount(@Valid ErpFinanceAmountSaveReqVO createReqVO, String currentUsername);

    /**
     * 更新财务金额
     *
     * @param updateReqVO 更新信息
     * @param currentUsername 当前用户名
     */
    void updateFinanceAmount(@Valid ErpFinanceAmountSaveReqVO updateReqVO, String currentUsername);

    /**
     * 删除财务金额
     *
     * @param ids 编号列表
     * @param currentUsername 当前用户名
     */
    void deleteFinanceAmount(List<Long> ids, String currentUsername);

    /**
     * 获得财务金额
     *
     * @param id 编号
     * @param currentUsername 当前用户名
     * @return 财务金额
     */
    ErpFinanceAmountDO getFinanceAmount(Long id, String currentUsername);

    /**
     * 校验财务金额是否存在
     *
     * @param id 编号
     * @param currentUsername 当前用户名
     * @return 财务金额
     */
    ErpFinanceAmountDO validateFinanceAmount(Long id, String currentUsername);

    /**
     * 获得财务金额分页
     *
     * @param pageReqVO 分页查询
     * @param currentUsername 当前用户名
     * @return 财务金额分页
     */
    PageResult<ErpFinanceAmountRespVO> getFinanceAmountVOPage(ErpFinanceAmountPageReqVO pageReqVO, String currentUsername);

    /**
     * 获得财务金额列表, 用于 Excel 导出
     *
     * @param ids 编号列表
     * @param currentUsername 当前用户名
     * @return 财务金额列表
     */
    List<ErpFinanceAmountRespVO> getFinanceAmountVOList(Collection<Long> ids, String currentUsername);

    /**
     * 获得财务金额列表
     *
     * @param ids 编号列表
     * @param currentUsername 当前用户名
     * @return 财务金额列表
     */
    List<ErpFinanceAmountDO> getFinanceAmountList(Collection<Long> ids, String currentUsername);

    /**
     * 根据创建者获取财务金额记录
     *
     * @param creator 创建者
     * @return 财务金额记录
     */
    ErpFinanceAmountDO getFinanceAmountByCreator(String creator);

    /**
     * 初始化用户财务金额记录
     *
     * @param creator 创建者
     * @return 财务金额记录
     */
    ErpFinanceAmountDO initUserFinanceAmount(String creator);

    /**
     * 更新余额
     *
     * @param creator 创建者
     * @param account 账户
     * @param amount 金额
     * @param incomeExpense 收支类型
     */
    void updateBalance(String creator, String account, BigDecimal amount, Integer incomeExpense);

    /**
     * 充值
     *
     * @param creator 创建者
     * @param channelType 渠道类型
     * @param amount 金额
     */
    void recharge(String creator, String channelType, BigDecimal amount);

    /**
     * 充值（带图片和备注）
     *
     * @param creator 创建者
     * @param channelType 渠道类型
     * @param amount 金额
     * @param carouselImages 轮播图片
     * @param remark 备注
     * @param orderDate 订单日期
     */
    void rechargeWithImages(String creator, String channelType, BigDecimal amount, String carouselImages, String remark, String orderDate);

    /**
     * 获取用户余额汇总
     *
     * @param creator 创建者
     * @return 余额汇总
     */
    ErpFinanceAmountRespVO getUserBalanceSummary(String creator);

    /**
     * 获取指定渠道的余额
     *
     * @param creator 创建者
     * @param channelType 渠道类型
     * @return 余额
     */
    BigDecimal getChannelBalance(String creator, String channelType);

    /**
     * 创建充值记录
     *
     * @param creator 创建者
     * @param channelType 渠道类型
     * @param amount 金额
     * @param remark 备注
     * @return 记录ID
     */
    Long createRechargeRecord(String creator, String channelType, BigDecimal amount, String remark);

    /**
     * 创建消费记录
     *
     * @param creator 创建者
     * @param channelType 渠道类型
     * @param amount 金额
     * @param remark 备注
     * @return 记录ID
     */
    Long createConsumeRecord(String creator, String channelType, BigDecimal amount, String remark);

    /**
     * 审核财务金额记录
     *
     * @param auditReqVO 审核信息
     * @param currentUsername 当前用户名
     */
    void auditFinanceAmount(ErpFinanceAmountAuditReqVO auditReqVO, String currentUsername);

    /**
     * 反审核财务金额记录
     *
     * @param ids 编号列表
     * @param currentUsername 当前用户名
     */
    void unauditFinanceAmount(List<Long> ids, String currentUsername);

    /**
     * 导入财务金额列表
     *
     * @param importList 导入列表
     * @param isUpdateSupport 是否支持更新
     * @param currentUsername 当前用户名
     * @return 导入结果
     */
    ErpFinanceAmountImportRespVO importFinanceAmountList(List<ErpFinanceAmountImportExcelVO> importList, boolean isUpdateSupport, String currentUsername);
} 