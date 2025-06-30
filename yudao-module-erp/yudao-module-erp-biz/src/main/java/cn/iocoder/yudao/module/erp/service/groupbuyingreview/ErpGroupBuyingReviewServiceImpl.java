package cn.iocoder.yudao.module.erp.service.groupbuyingreview;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyingreview.vo.ErpGroupBuyingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingReviewDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.groupbuying.ErpGroupBuyingMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.groupbuyingreview.ErpGroupBuyingReviewMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpCustomerMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpGroupBuyingReviewServiceImpl implements ErpGroupBuyingReviewService {

    @Resource
    private ErpGroupBuyingReviewMapper groupBuyingReviewMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpGroupBuyingMapper groupBuyingMapper;

    @Resource
    private ErpCustomerMapper customerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroupBuyingReview(ErpGroupBuyingReviewSaveReqVO createReqVO, String currentUsername) {
        // 1. 校验数据
        validateGroupBuyingReviewForCreateOrUpdate(null, createReqVO);

        // 2. 生成团购复盘编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.GROUP_BUYING_REVIEW_NO_PREFIX);
        if (groupBuyingReviewMapper.selectByNo(no) != null) {
            throw exception(GROUP_BUYING_REVIEW_NO_EXISTS);
        }

        // 3. 插入团购复盘记录
        ErpGroupBuyingReviewDO groupBuyingReview = BeanUtils.toBean(createReqVO, ErpGroupBuyingReviewDO.class)
                .setNo(no);
        groupBuyingReviewMapper.insert(groupBuyingReview);

        return groupBuyingReview.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupBuyingReview(ErpGroupBuyingReviewSaveReqVO updateReqVO, String currentUsername) {
        // 1.1 校验存在
        validateGroupBuyingReview(updateReqVO.getId(), currentUsername);
        // 1.2 校验数据
        validateGroupBuyingReviewForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新团购复盘记录
        ErpGroupBuyingReviewDO updateObj = BeanUtils.toBean(updateReqVO, ErpGroupBuyingReviewDO.class);
        groupBuyingReviewMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroupBuyingReview(List<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在且属于当前用户
        for (Long id : ids) {
            validateGroupBuyingReview(id, currentUsername);
        }
        // 2. 删除团购复盘记录
        groupBuyingReviewMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpGroupBuyingReviewDO getGroupBuyingReview(Long id, String currentUsername) {
        ErpGroupBuyingReviewDO groupBuyingReview = groupBuyingReviewMapper.selectById(id);
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (groupBuyingReview != null && !"admin".equals(currentUsername) && !ObjectUtil.equal(groupBuyingReview.getCreator(), currentUsername)) {
            return null; // 不是当前用户的数据且不是admin，返回null
        }
        return groupBuyingReview;
    }

    @Override
    public ErpGroupBuyingReviewDO validateGroupBuyingReview(Long id, String currentUsername) {
        ErpGroupBuyingReviewDO groupBuyingReview = groupBuyingReviewMapper.selectById(id);
        if (groupBuyingReview == null) {
            throw exception(GROUP_BUYING_REVIEW_NOT_EXISTS);
        }
        // admin用户可以操作全部数据，其他用户只能操作自己的数据
        if (!"admin".equals(currentUsername) && !ObjectUtil.equal(groupBuyingReview.getCreator(), currentUsername)) {
            throw exception(GROUP_BUYING_REVIEW_NOT_EXISTS); // 不是当前用户的数据且不是admin
        }
        return groupBuyingReview;
    }

    @Override
    public PageResult<ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOPage(ErpGroupBuyingReviewPageReqVO pageReqVO, String currentUsername) {
        return groupBuyingReviewMapper.selectPage(pageReqVO, currentUsername);
    }

    @Override
    public List<ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOList(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        
        // 使用Mapper方法获取完整信息，但需要过滤权限
        List<ErpGroupBuyingReviewRespVO> list = groupBuyingReviewMapper.selectListByIds(ids);
        
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"admin".equals(currentUsername)) {
            list = list.stream()
                    .filter(item -> ObjectUtil.equal(item.getCreator(), currentUsername))
                    .collect(ArrayList::new, (l, item) -> l.add(item), ArrayList::addAll);
        }
        
        return list;
    }

    @Override
    public Map<Long, ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOMap(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingReviewVOList(ids, currentUsername), ErpGroupBuyingReviewRespVO::getId);
    }

    @Override
    public List<ErpGroupBuyingReviewDO> getGroupBuyingReviewList(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpGroupBuyingReviewDO> list = groupBuyingReviewMapper.selectBatchIds(ids);
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"admin".equals(currentUsername)) {
            list = list.stream()
                    .filter(item -> ObjectUtil.equal(item.getCreator(), currentUsername))
                    .collect(ArrayList::new, (l, item) -> l.add(item), ArrayList::addAll);
        }
        return list;
    }

    @Override
    public Map<Long, ErpGroupBuyingReviewDO> getGroupBuyingReviewMap(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingReviewList(ids, currentUsername), ErpGroupBuyingReviewDO::getId);
    }

    private void validateGroupBuyingReviewForCreateOrUpdate(Long id, ErpGroupBuyingReviewSaveReqVO reqVO) {
        // 1. 校验团购复盘编号唯一
        ErpGroupBuyingReviewDO groupBuyingReview = groupBuyingReviewMapper.selectByNo(reqVO.getNo());
        if (groupBuyingReview != null && !groupBuyingReview.getId().equals(id)) {
            throw exception(GROUP_BUYING_REVIEW_NO_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpGroupBuyingReviewImportRespVO importGroupBuyingReviewList(List<ErpGroupBuyingReviewImportExcelVO> importList, boolean isUpdateSupport, String currentUsername) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(GROUP_BUYING_REVIEW_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpGroupBuyingReviewImportRespVO respVO = ErpGroupBuyingReviewImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        try {
            // 1. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateAllImportData(importList, isUpdateSupport, currentUsername);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 2. 批量处理
            List<ErpGroupBuyingReviewDO> createList = new ArrayList<>();
            List<ErpGroupBuyingReviewDO> updateList = new ArrayList<>();

            // 3. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpGroupBuyingReviewImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpGroupBuyingReviewDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(groupBuyingReviewMapper.selectListByNoIn(noSet), ErpGroupBuyingReviewDO::getNo);

            // 4. 批量转换和保存数据
            for (int i = 0; i < importList.size(); i++) {
                ErpGroupBuyingReviewImportExcelVO importVO = importList.get(i);

                // 数据转换
                ErpGroupBuyingReviewDO groupBuyingReview = BeanUtils.toBean(importVO, ErpGroupBuyingReviewDO.class);

                // 判断是新增还是更新
                ErpGroupBuyingReviewDO existGroupBuyingReview = existMap.get(importVO.getNo());
                if (existGroupBuyingReview == null) {
                    // 创建 - 自动生成新的no编号
                    groupBuyingReview.setNo(noRedisDAO.generate(ErpNoRedisDAO.GROUP_BUYING_REVIEW_NO_PREFIX));
                    createList.add(groupBuyingReview);
                    respVO.getCreateNames().add(groupBuyingReview.getNo());
                } else if (isUpdateSupport) {
                    // 更新
                    groupBuyingReview.setId(existGroupBuyingReview.getId());
                    updateList.add(groupBuyingReview);
                    respVO.getUpdateNames().add(groupBuyingReview.getNo());
                }
            }

            // 5. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                groupBuyingReviewMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(groupBuyingReviewMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        } finally {
            // 清除转换错误
            ConversionErrorHolder.clearErrors();
        }

        return respVO;
    }

    /**
     * 统一校验所有导入数据（包括数据类型校验和业务逻辑校验）
     * 如果出现任何错误信息都记录下来并返回，后续操作就不进行了
     */
    private Map<String, String> validateAllImportData(List<ErpGroupBuyingReviewImportExcelVO> importList, boolean isUpdateSupport, String currentUsername) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpGroupBuyingReviewImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpGroupBuyingReviewDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(groupBuyingReviewMapper.selectListByNoIn(noSet), ErpGroupBuyingReviewDO::getNo);

        // 3. 批量查询团购货盘信息
        Set<String> groupBuyingNos = importList.stream()
                .map(ErpGroupBuyingReviewImportExcelVO::getGroupBuyingId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, Long> groupBuyingIdMap = groupBuyingNos.isEmpty() ? Collections.emptyMap() :
                convertMap(groupBuyingMapper.selectListByNoIn(groupBuyingNos), ErpGroupBuyingDO::getNo, ErpGroupBuyingDO::getId);

        // 4. 批量查询客户信息
        Set<String> customerNames = importList.stream()
                .map(ErpGroupBuyingReviewImportExcelVO::getCustomerName)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, Long> customerIdMap = customerNames.isEmpty() ? Collections.emptyMap() :
                convertMap(customerMapper.selectListByNameIn(customerNames), ErpCustomerDO::getName, ErpCustomerDO::getId);

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();

        // 5. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpGroupBuyingReviewImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");

            try {
                // 5.1 基础数据校验
                if (StrUtil.isBlank(importVO.getGroupBuyingId())) {
                    allErrors.put(errorKey, "团购货盘编号不能为空");
                    continue;
                }

                // 5.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "团购复盘编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 5.3 校验团购货盘是否存在
                if (StrUtil.isNotBlank(importVO.getGroupBuyingId())) {
                    Long groupBuyingId = groupBuyingIdMap.get(importVO.getGroupBuyingId());
                    if (groupBuyingId == null) {
                        allErrors.put(errorKey, "团购货盘不存在: " + importVO.getGroupBuyingId());
                        continue;
                    }
                }

                // 5.4 校验客户是否存在
                if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                    Long customerId = customerIdMap.get(importVO.getCustomerName());
                    if (customerId == null) {
                        allErrors.put(errorKey, "客户不存在: " + importVO.getCustomerName());
                        continue;
                    }
                }

                // 5.5 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpGroupBuyingReviewDO groupBuyingReview = BeanUtils.toBean(importVO, ErpGroupBuyingReviewDO.class);
                    if (groupBuyingReview == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 5.6 判断是新增还是更新，并进行相应校验
                ErpGroupBuyingReviewDO existGroupBuyingReview = existMap.get(importVO.getNo());
                if (existGroupBuyingReview == null) {
                    // 新增校验：可以添加其他业务校验逻辑
                } else if (!isUpdateSupport) {
                    // 更新校验：如果不支持更新，记录错误
                    allErrors.put(errorKey, "团购复盘编号已存在且不支持更新: " + importVO.getNo());
                    continue;
                } else {
                    // 更新权限校验
                    if (!"admin".equals(currentUsername) && !ObjectUtil.equal(existGroupBuyingReview.getCreator(), currentUsername)) {
                        allErrors.put(errorKey, "无权限更新此记录: " + importVO.getNo());
                        continue;
                    }
                }
            } catch (Exception ex) {
                allErrors.put(errorKey, "校验异常: " + ex.getMessage());
            }
        }

        return allErrors;
    }

    /**
     * 校验数据类型错误
     */
    private Map<String, String> validateDataTypeErrors(List<ErpGroupBuyingReviewImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取团购复盘编号 - 修复行号索引问题
                String groupBuyingReviewNo = "未知编号";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpGroupBuyingReviewImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        groupBuyingReviewNo = importVO.getNo();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + groupBuyingReviewNo + ")";
                List<String> errorMessages = new ArrayList<>();

                for (ConversionErrorHolder.ConversionError error : errors) {
                    errorMessages.add(error.getErrorMessage());
                }

                String errorMsg = String.join("; ", errorMessages);
                dataTypeErrors.put(errorKey, "数据类型错误: " + errorMsg);
            }
        }

        return dataTypeErrors;
    }
}
