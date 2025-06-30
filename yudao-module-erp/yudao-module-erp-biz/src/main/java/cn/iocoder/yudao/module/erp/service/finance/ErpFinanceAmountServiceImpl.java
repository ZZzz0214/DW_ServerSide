package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAmountDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceAmountMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpFinanceAmountServiceImpl implements ErpFinanceAmountService {

    @Resource
    private ErpFinanceAmountMapper financeAmountMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;
    
    @Resource
    private cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceMapper financeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFinanceAmount(ErpFinanceAmountSaveReqVO createReqVO, String currentUsername) {
        // 1. 校验数据
        validateFinanceAmountForCreateOrUpdate(null, createReqVO, currentUsername);

        // 2. 生成财务金额记录编号
        String no = noRedisDAO.generate(ErpNoRedisDAO.FINANCE_AMOUNT_NO_PREFIX);
        if (financeAmountMapper.selectByNo(no) != null) {
            throw exception(FINANCE_AMOUNT_NO_EXISTS);
        }

        // 3. 插入财务金额记录
        ErpFinanceAmountDO financeAmount = BeanUtils.toBean(createReqVO, ErpFinanceAmountDO.class)
                .setNo(no)
                .setAuditStatus(10); // 默认待审核
        financeAmountMapper.insert(financeAmount);

        return financeAmount.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceAmount(ErpFinanceAmountSaveReqVO updateReqVO, String currentUsername) {
        // 1.1 校验存在
        ErpFinanceAmountDO oldFinanceAmount = validateFinanceAmount(updateReqVO.getId(), currentUsername);
        
        // 1.2 校验审核状态，已审核的记录不能修改
        if (oldFinanceAmount.getAuditStatus() != null && oldFinanceAmount.getAuditStatus() == 20) {
            throw exception(FINANCE_AMOUNT_AUDIT_STATUS_NOT_ALLOW_UPDATE);
        }
        
        // 1.3 校验数据
        validateFinanceAmountForCreateOrUpdate(updateReqVO.getId(), updateReqVO, currentUsername);

        // 2. 重新计算余额（如果金额、操作类型或渠道类型发生变化）
        ErpFinanceAmountDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinanceAmountDO.class);
        
        boolean needRecalculateBalance = false;
        // 检查是否需要重新计算余额
        if (!Objects.equals(oldFinanceAmount.getAmount(), updateReqVO.getAmount()) ||
            !Objects.equals(oldFinanceAmount.getOperationType(), updateReqVO.getOperationType()) ||
            !Objects.equals(oldFinanceAmount.getChannelType(), updateReqVO.getChannelType())) {
            needRecalculateBalance = true;
        }
        
        if (needRecalculateBalance) {
            // 重新计算操作前余额和操作后余额
            BigDecimal beforeBalance = calculateBeforeBalanceForUpdate(updateReqVO.getId(), updateReqVO.getChannelType(), currentUsername);
            BigDecimal afterBalance;
            
            if (updateReqVO.getOperationType() == 1) { // 充值
                afterBalance = beforeBalance.add(updateReqVO.getAmount());
            } else { // 消费
                afterBalance = beforeBalance.subtract(updateReqVO.getAmount());
                // 检查余额是否足够
                if (afterBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throw exception(FINANCE_AMOUNT_BALANCE_INSUFFICIENT);
                }
            }
            
            updateObj.setBeforeBalance(beforeBalance);
            updateObj.setAfterBalance(afterBalance);
        }

        // 3. 更新财务金额记录
        financeAmountMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinanceAmount(List<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在且属于当前用户
        for (Long id : ids) {
            ErpFinanceAmountDO financeAmount = validateFinanceAmount(id, currentUsername);
            // 校验审核状态，已审核的记录不能删除
            if (financeAmount.getAuditStatus() != null && financeAmount.getAuditStatus() == 20) {
                throw exception(FINANCE_AMOUNT_AUDIT_STATUS_NOT_ALLOW_DELETE);
            }
        }
        // 2. 删除财务金额记录
        financeAmountMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpFinanceAmountDO getFinanceAmount(Long id, String currentUsername) {
        ErpFinanceAmountDO financeAmount = financeAmountMapper.selectById(id);
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (financeAmount != null && !"admin".equals(currentUsername) && !ObjectUtil.equal(financeAmount.getCreator(), currentUsername)) {
            return null; // 不是当前用户的数据且不是admin，返回null
        }
        return financeAmount;
    }

    @Override
    public ErpFinanceAmountDO validateFinanceAmount(Long id, String currentUsername) {
        ErpFinanceAmountDO financeAmount = financeAmountMapper.selectById(id);
        if (financeAmount == null) {
            throw exception(FINANCE_AMOUNT_NOT_EXISTS);
        }
        // admin用户可以操作全部数据，其他用户只能操作自己的数据
        if (!"admin".equals(currentUsername) && !ObjectUtil.equal(financeAmount.getCreator(), currentUsername)) {
            throw exception(FINANCE_AMOUNT_NOT_EXISTS); // 不是当前用户的数据且不是admin
        }
        return financeAmount;
    }

    @Override
    public PageResult<ErpFinanceAmountRespVO> getFinanceAmountVOPage(ErpFinanceAmountPageReqVO pageReqVO, String currentUsername) {
        return financeAmountMapper.selectPage(pageReqVO, currentUsername);
    }

    @Override
    public List<ErpFinanceAmountRespVO> getFinanceAmountVOList(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpFinanceAmountDO> list = financeAmountMapper.selectBatchIds(ids);
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"admin".equals(currentUsername)) {
            list = list.stream()
                    .filter(item -> ObjectUtil.equal(item.getCreator(), currentUsername))
                    .collect(ArrayList::new, (l, item) -> l.add(item), ArrayList::addAll);
        }
        return BeanUtils.toBean(list, ErpFinanceAmountRespVO.class);
    }

    @Override
    public List<ErpFinanceAmountDO> getFinanceAmountList(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpFinanceAmountDO> list = financeAmountMapper.selectBatchIds(ids);
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"admin".equals(currentUsername)) {
            list = list.stream()
                    .filter(item -> ObjectUtil.equal(item.getCreator(), currentUsername))
                    .collect(ArrayList::new, (l, item) -> l.add(item), ArrayList::addAll);
        }
        return list;
    }

    @Override
    public ErpFinanceAmountDO getFinanceAmountByCreator(String creator) {
        return financeAmountMapper.selectByCreator(creator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpFinanceAmountDO initUserFinanceAmount(String creator) {
        // 注释：财务金额表现在使用新的记录模式，不需要初始化兼容字段
        // 检查用户是否已有财务金额记录（查询最新的一条记录即可）
        List<ErpFinanceAmountDO> existingRecords = financeAmountMapper.selectListByCreator(creator);
        if (!existingRecords.isEmpty()) {
            // 返回最新的记录
            return existingRecords.stream()
                    .max((a, b) -> Long.compare(a.getId(), b.getId()))
                    .orElse(null);
        }

        // 如果没有任何记录，创建一个初始化记录（仅用于标识用户已初始化）
        String no = noRedisDAO.generate(ErpNoRedisDAO.FINANCE_AMOUNT_NO_PREFIX);
        ErpFinanceAmountDO financeAmount = ErpFinanceAmountDO.builder()
                .no(no)
                .channelType("系统")
                .amount(BigDecimal.ZERO)
                .operationType(1) // 充值类型
                .beforeBalance(BigDecimal.ZERO)
                .afterBalance(BigDecimal.ZERO)
                .remark("系统初始化记录")
                .build();
        
        financeAmountMapper.insert(financeAmount);
        return financeAmount;
    }

    // 注释：此方法已废弃，新的记录模式下不需要清理重复记录
    // 每个用户可以有多条充值/消费记录
    // /**
    //  * 清理重复的财务金额记录，确保每个用户只保留最新的一条记录
    //  */
    // @Transactional(rollbackFor = Exception.class)
    // private void cleanupDuplicateFinanceAmountRecords(String creator) {
    //     List<ErpFinanceAmountDO> list = financeAmountMapper.selectListByCreator(creator);
    //     if (list.size() <= 1) {
    //         return; // 没有重复记录
    //     }
    //     
    //     // 按ID降序排序，保留最新的记录
    //     list.sort((a, b) -> Long.compare(b.getId(), a.getId()));
    //     ErpFinanceAmountDO latestRecord = list.get(0);
    //     
    //     // 删除其他重复记录
    //     List<Long> idsToDelete = list.stream()
    //             .skip(1) // 跳过第一条（最新的）
    //             .map(ErpFinanceAmountDO::getId)
    //             .collect(ArrayList::new, (l, id) -> l.add(id), ArrayList::addAll);
    //     
    //     if (!idsToDelete.isEmpty()) {
    //         financeAmountMapper.deleteBatchIds(idsToDelete);
    //     }
    // }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBalance(String creator, String account, BigDecimal amount, Integer incomeExpense) {
        // 财务表操作不在财务金额表中创建记录，只影响余额的实时计算
        // 余额 = 财务金额表最新余额 + 财务表收支影响
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(String creator, String channelType, BigDecimal amount) {
        // 使用新的记录方式创建充值记录
        createRechargeRecord(creator, channelType, amount, "用户充值");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rechargeWithImages(String creator, String channelType, BigDecimal amount, String carouselImages, String remark, String orderDate) {
        // 使用新的记录方式创建充值记录，包含图片和备注
        createRechargeRecordWithImages(creator, channelType, amount, carouselImages, remark != null ? remark : "用户充值", orderDate);
    }

    @Override
    public ErpFinanceAmountRespVO getUserBalanceSummary(String creator) {
        // 计算各渠道余额
        BigDecimal wechatBalance = getChannelBalance(creator, "微信");
        BigDecimal alipayBalance = getChannelBalance(creator, "支付宝");
        BigDecimal bankCardBalance = getChannelBalance(creator, "银行卡");
        
        // 计算各渠道累计充值
        BigDecimal wechatRecharge = getTotalRechargeByChannel(creator, "微信");
        BigDecimal alipayRecharge = getTotalRechargeByChannel(creator, "支付宝");
        BigDecimal bankCardRecharge = getTotalRechargeByChannel(creator, "银行卡");
        
        // 构建响应对象
        ErpFinanceAmountRespVO summary = new ErpFinanceAmountRespVO();
        summary.setWechatBalance(wechatBalance);
        summary.setAlipayBalance(alipayBalance);
        summary.setBankCardBalance(bankCardBalance);
        summary.setWechatRecharge(wechatRecharge);
        summary.setAlipayRecharge(alipayRecharge);
        summary.setBankCardRecharge(bankCardRecharge);
        
        return summary;
    }

    @Override
    public BigDecimal getChannelBalance(String creator, String channelType) {
        // 1. 获取财务金额表的最新记录（只包含真正的充值记录）
        List<ErpFinanceAmountDO> amountRecords = financeAmountMapper.selectList(
            new MPJLambdaWrapperX<ErpFinanceAmountDO>()
                .eq(ErpFinanceAmountDO::getCreator, creator)
                .eq(ErpFinanceAmountDO::getChannelType, channelType)
                .orderByDesc(ErpFinanceAmountDO::getCreateTime)
        );
        
        BigDecimal amountBalance = BigDecimal.ZERO;
        if (!CollUtil.isEmpty(amountRecords)) {
            ErpFinanceAmountDO latestRecord = amountRecords.get(0);
            amountBalance = latestRecord.getAfterBalance() != null ? latestRecord.getAfterBalance() : BigDecimal.ZERO;
        }
        
        // 2. 获取财务表的收支影响
        BigDecimal financeBalance = getFinanceTableBalance(creator, channelType);
        
        // 3. 返回综合余额
        BigDecimal totalBalance = amountBalance.add(financeBalance);
        return totalBalance;
    }
    
    /**
     * 计算财务表对指定渠道余额的影响
     */
    private BigDecimal getFinanceTableBalance(String creator, String channelType) {
        // 查询财务表中该用户该渠道的所有记录
        List<cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO> financeRecords = financeMapper.selectList(
            new MPJLambdaWrapperX<cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO>()
                .eq(cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO::getCreator, creator)
                .eq(cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO::getAccount, channelType)
        );
        
        if (CollUtil.isEmpty(financeRecords)) {
            return BigDecimal.ZERO;
        }
        
        // 计算收支影响：收入为正，支出为负
        BigDecimal totalBalance = BigDecimal.ZERO;
        for (cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDO record : financeRecords) {
            if (record.getAmount() != null && record.getIncomeExpense() != null) {
                if (record.getIncomeExpense() == 1) {
                    // 收入，增加余额
                    totalBalance = totalBalance.add(record.getAmount());
                } else if (record.getIncomeExpense() == 2) {
                    // 支出，减少余额
                    totalBalance = totalBalance.subtract(record.getAmount());
                }
            }
        }
        
        return totalBalance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRechargeRecord(String creator, String channelType, BigDecimal amount, String remark) {
        return createFinanceRecord(creator, channelType, amount, 1, remark);
    }

    /**
     * 创建充值记录（带图片和备注）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRechargeRecordWithImages(String creator, String channelType, BigDecimal amount, String carouselImages, String remark, String orderDate) {
        return createFinanceRecordWithImages(creator, channelType, amount, 1, carouselImages, remark, orderDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConsumeRecord(String creator, String channelType, BigDecimal amount, String remark) {
        return createFinanceRecord(creator, channelType, amount, 2, remark);
    }

    /**
     * 创建财务记录（充值或消费）
     */
    @Transactional(rollbackFor = Exception.class)
    private Long createFinanceRecord(String creator, String channelType, BigDecimal amount, Integer operationType, String remark) {
        return createFinanceRecordWithImages(creator, channelType, amount, operationType, null, remark, null);
    }

    /**
     * 创建财务记录（充值或消费，带图片）
     */
    @Transactional(rollbackFor = Exception.class)
    private Long createFinanceRecordWithImages(String creator, String channelType, BigDecimal amount, Integer operationType, String carouselImages, String remark, String orderDate) {
        // 获取当前余额（包含财务表和财务金额表的综合余额）
        BigDecimal currentBalance = getCurrentBalanceForNewRecord(creator, channelType);
        
        // 计算操作后余额
        BigDecimal afterBalance;
        if (operationType == 1) { // 充值
            afterBalance = currentBalance.add(amount);
        } else { // 消费
            afterBalance = currentBalance.subtract(amount);
            // 检查余额是否足够
            if (afterBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw exception(FINANCE_AMOUNT_BALANCE_INSUFFICIENT);
            }
        }
        
        // 生成编号
        String no = noRedisDAO.generate(ErpNoRedisDAO.FINANCE_AMOUNT_NO_PREFIX);
        
        // 创建记录
        ErpFinanceAmountDO record = ErpFinanceAmountDO.builder()
                .no(no)
                .channelType(channelType)
                .amount(amount)
                .operationType(operationType)
                .beforeBalance(currentBalance)
                .afterBalance(afterBalance)
                .carouselImages(carouselImages)
                .remark(remark)
                .orderDate(orderDate != null ? LocalDate.parse(orderDate) : LocalDate.now())
                .auditStatus(10) // 默认待审核
                .build();
        
        financeAmountMapper.insert(record);
        return record.getId();
    }
    
    /**
     * 获取创建新记录时的当前余额（简化版）
     */
    private BigDecimal getCurrentBalanceForNewRecord(String creator, String channelType) {
        // 直接获取财务金额表的最新余额，不包含财务表影响
        // 因为充值记录本身就是在财务金额表中，不应该重复计算
        List<ErpFinanceAmountDO> amountRecords = financeAmountMapper.selectList(
            new MPJLambdaWrapperX<ErpFinanceAmountDO>()
                .eq(ErpFinanceAmountDO::getCreator, creator)
                .eq(ErpFinanceAmountDO::getChannelType, channelType)
                .orderByDesc(ErpFinanceAmountDO::getCreateTime)
        );
        
        if (!CollUtil.isEmpty(amountRecords)) {
            ErpFinanceAmountDO latestRecord = amountRecords.get(0);
            return latestRecord.getAfterBalance() != null ? latestRecord.getAfterBalance() : BigDecimal.ZERO;
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * 计算更新记录时的操作前余额
     * 需要排除当前正在更新的记录，获取该记录之前的最新余额
     */
    private BigDecimal calculateBeforeBalanceForUpdate(Long currentRecordId, String channelType, String creator) {
        // 查询该渠道的所有记录，按创建时间排序，排除当前正在更新的记录
        List<ErpFinanceAmountDO> amountRecords = financeAmountMapper.selectList(
            new MPJLambdaWrapperX<ErpFinanceAmountDO>()
                .eq(ErpFinanceAmountDO::getCreator, creator)
                .eq(ErpFinanceAmountDO::getChannelType, channelType)
                .ne(ErpFinanceAmountDO::getId, currentRecordId)
                .orderByDesc(ErpFinanceAmountDO::getCreateTime)
        );
        
        if (!CollUtil.isEmpty(amountRecords)) {
            // 获取最新的一条记录的操作后余额作为当前记录的操作前余额
            ErpFinanceAmountDO latestRecord = amountRecords.get(0);
            return latestRecord.getAfterBalance() != null ? latestRecord.getAfterBalance() : BigDecimal.ZERO;
        }
        
        // 如果没有其他记录，则操作前余额为0
        return BigDecimal.ZERO;
    }

    /**
     * 获取指定渠道的累计充值金额
     */
    private BigDecimal getTotalRechargeByChannel(String creator, String channelType) {
        List<ErpFinanceAmountDO> records = financeAmountMapper.selectList(
            new MPJLambdaWrapperX<ErpFinanceAmountDO>()
                .eq(ErpFinanceAmountDO::getCreator, creator)
                .eq(ErpFinanceAmountDO::getChannelType, channelType)
                .eq(ErpFinanceAmountDO::getOperationType, 1) // 只统计充值记录
        );
        
        BigDecimal totalRecharge = records.stream()
                .map(ErpFinanceAmountDO::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalRecharge;
    }

    private void validateFinanceAmountForCreateOrUpdate(Long id, ErpFinanceAmountSaveReqVO reqVO, String currentUsername) {
        // 1. 校验编号唯一
        ErpFinanceAmountDO financeAmount = financeAmountMapper.selectByNo(reqVO.getNo());
        if (financeAmount != null && !ObjectUtil.equal(financeAmount.getId(), id)) {
            throw exception(FINANCE_AMOUNT_NO_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditFinanceAmount(ErpFinanceAmountAuditReqVO auditReqVO, String currentUsername) {
        if (CollUtil.isEmpty(auditReqVO.getIds())) {
            return;
        }
        
        // 1. 校验记录存在且属于当前用户
        List<ErpFinanceAmountDO> financeAmounts = new ArrayList<>();
        for (Long id : auditReqVO.getIds()) {
            ErpFinanceAmountDO financeAmount = validateFinanceAmount(id, currentUsername);
            // 校验审核状态，只有待审核的记录才能审核
            if (financeAmount.getAuditStatus() != null && financeAmount.getAuditStatus() != 10) {
                throw exception(FINANCE_AMOUNT_AUDIT_STATUS_NOT_ALLOW_AUDIT);
            }
            financeAmounts.add(financeAmount);
        }
        
        // 2. 批量更新审核状态
        for (ErpFinanceAmountDO financeAmount : financeAmounts) {
            financeAmount.setAuditStatus(auditReqVO.getAuditStatus());
            financeAmount.setAuditor(currentUsername);
            financeAmount.setAuditTime(LocalDateTime.now());
            financeAmount.setAuditRemark(auditReqVO.getAuditRemark());
            financeAmountMapper.updateById(financeAmount);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unauditFinanceAmount(List<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        
        // 1. 校验记录存在且属于当前用户
        List<ErpFinanceAmountDO> financeAmounts = new ArrayList<>();
        for (Long id : ids) {
            ErpFinanceAmountDO financeAmount = validateFinanceAmount(id, currentUsername);
            // 校验审核状态，只有已审核的记录才能反审核
            if (financeAmount.getAuditStatus() == null || financeAmount.getAuditStatus() != 20) {
                throw exception(FINANCE_AMOUNT_AUDIT_STATUS_NOT_ALLOW_UNAUDIT);
            }
            financeAmounts.add(financeAmount);
        }
        
        // 2. 批量更新审核状态为待审核
        for (ErpFinanceAmountDO financeAmount : financeAmounts) {
            financeAmount.setAuditStatus(10);
            financeAmount.setAuditor(null);
            financeAmount.setAuditTime(null);
            financeAmount.setAuditRemark(null);
            financeAmountMapper.updateById(financeAmount);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpFinanceAmountImportRespVO importFinanceAmountList(List<ErpFinanceAmountImportExcelVO> importList, boolean isUpdateSupport, String currentUsername) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(FINANCE_AMOUNT_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpFinanceAmountImportRespVO respVO = ErpFinanceAmountImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理
        List<ErpFinanceAmountDO> createList = new ArrayList<>();
        List<ErpFinanceAmountDO> updateList = new ArrayList<>();

        try {
            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpFinanceAmountImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpFinanceAmountDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(financeAmountMapper.selectListByNoIn(noSet), ErpFinanceAmountDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();
            
            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpFinanceAmountImportExcelVO importVO = importList.get(i);
                try {
                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(FINANCE_AMOUNT_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 判断是否支持更新
                    ErpFinanceAmountDO existFinanceAmount = existMap.get(importVO.getNo());
                    if (existFinanceAmount == null) {
                       // 创建 - 自动生成新的no编号
                       ErpFinanceAmountDO financeAmount = BeanUtils.toBean(importVO, ErpFinanceAmountDO.class);
                       financeAmount.setNo(noRedisDAO.generate(ErpNoRedisDAO.FINANCE_AMOUNT_NO_PREFIX));
                       financeAmount.setAuditStatus(10); // 默认待审核
                        createList.add(financeAmount);
                        respVO.getCreateNames().add(financeAmount.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpFinanceAmountDO updateFinanceAmount = BeanUtils.toBean(importVO, ErpFinanceAmountDO.class);
                        updateFinanceAmount.setId(existFinanceAmount.getId());
                        updateList.add(updateFinanceAmount);
                        respVO.getUpdateNames().add(updateFinanceAmount.getNo());
                    } else {
                        throw exception(FINANCE_AMOUNT_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
                    }
                } catch (ServiceException ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");
                    respVO.getFailureNames().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");
                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                financeAmountMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(financeAmountMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }
} 