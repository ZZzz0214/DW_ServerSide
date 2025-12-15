package cn.iocoder.yudao.module.erp.service.livebroadcastingreview;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastingreview.vo.ErpLiveBroadcastingReviewImportRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcasting.ErpLiveBroadcastingDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastingreview.ErpLiveBroadcastingReviewDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.livebroadcasting.ErpLiveBroadcastingMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.livebroadcastingreview.ErpLiveBroadcastingReviewMapper;
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
public class ErpLiveBroadcastingReviewServiceImpl implements ErpLiveBroadcastingReviewService {

    @Resource
    private ErpLiveBroadcastingReviewMapper liveBroadcastingReviewMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpLiveBroadcastingMapper liveBroadcastingMapper;

    @Resource
    private ErpCustomerMapper customerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLiveBroadcastingReview(ErpLiveBroadcastingReviewSaveReqVO createReqVO, String currentUsername) {
        // 1. 校验数据
        validateLiveBroadcastingReviewForCreateOrUpdate(null, createReqVO);

        // 2. 生成直播复盘编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.LIVE_BROADCASTING_REVIEW_NO_PREFIX);
        if (liveBroadcastingReviewMapper.selectByNo(no) != null) {
            throw exception(LIVE_BROADCASTING_REVIEW_NO_EXISTS);
        }

        // 3. 插入直播复盘记录
        ErpLiveBroadcastingReviewDO liveBroadcastingReview = BeanUtils.toBean(createReqVO, ErpLiveBroadcastingReviewDO.class)
                .setNo(no);
        liveBroadcastingReviewMapper.insert(liveBroadcastingReview);

        return liveBroadcastingReview.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLiveBroadcastingReview(ErpLiveBroadcastingReviewSaveReqVO updateReqVO, String currentUsername) {
        // 1.1 校验存在
        validateLiveBroadcastingReview(updateReqVO.getId(), currentUsername);
        // 1.2 校验数据
        validateLiveBroadcastingReviewForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新直播复盘记录
        ErpLiveBroadcastingReviewDO updateObj = BeanUtils.toBean(updateReqVO, ErpLiveBroadcastingReviewDO.class);
        liveBroadcastingReviewMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLiveBroadcastingReview(List<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在且属于当前用户
        for (Long id : ids) {
            validateLiveBroadcastingReview(id, currentUsername);
        }
        // 2. 删除直播复盘记录
        liveBroadcastingReviewMapper.deleteBatchIds(ids);
    }

    @Override
    public PageResult<ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReviewVOPage(ErpLiveBroadcastingReviewPageReqVO pageReqVO, String currentUsername) {
        return liveBroadcastingReviewMapper.selectPage(pageReqVO, currentUsername);
    }

    @Override
    public List<ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReviewVOList(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }

        // 使用Mapper方法获取完整信息，但需要过滤权限
        List<ErpLiveBroadcastingReviewRespVO> list = liveBroadcastingReviewMapper.selectListByIds(ids);

        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"ahao".equals(currentUsername) &&!"caiwu".equals(currentUsername) && !"admin".equals(currentUsername)) {
            list = list.stream()
                    .filter(item -> ObjectUtil.equal(item.getCreator(), currentUsername))
                    .collect(ArrayList::new, (l, item) -> l.add(item), ArrayList::addAll);
        }

        return list;
    }

    @Override
    public Map<Long, ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReviewVOMap(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingReviewVOList(ids, currentUsername), ErpLiveBroadcastingReviewRespVO::getId);
    }

    @Override
    public ErpLiveBroadcastingReviewDO getLiveBroadcastingReview(Long id, String currentUsername) {
        ErpLiveBroadcastingReviewDO liveBroadcastingReview = liveBroadcastingReviewMapper.selectById(id);
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (liveBroadcastingReview != null && !"admin".equals(currentUsername) &&!"ahao".equals(currentUsername) &&!"caiwu".equals(currentUsername) &&  !ObjectUtil.equal(liveBroadcastingReview.getCreator(), currentUsername)) {
            return null; // 不是当前用户的数据且不是admin，返回null
        }
        return liveBroadcastingReview;
    }

    @Override
    public ErpLiveBroadcastingReviewDO validateLiveBroadcastingReview(Long id, String currentUsername) {
        ErpLiveBroadcastingReviewDO liveBroadcastingReview = liveBroadcastingReviewMapper.selectById(id);
        if (liveBroadcastingReview == null) {
            throw exception(LIVE_BROADCASTING_REVIEW_NOT_EXISTS);
        }
        // admin用户可以操作全部数据，其他用户只能操作自己的数据
        if (!"ahao".equals(currentUsername) &&!"caiwu".equals(currentUsername) && !"admin".equals(currentUsername) && !ObjectUtil.equal(liveBroadcastingReview.getCreator(), currentUsername)) {
            throw exception(LIVE_BROADCASTING_REVIEW_NOT_EXISTS); // 不是当前用户的数据且不是admin
        }
        return liveBroadcastingReview;
    }

    @Override
    public List<ErpLiveBroadcastingReviewDO> getLiveBroadcastingReviewList(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpLiveBroadcastingReviewDO> list = liveBroadcastingReviewMapper.selectBatchIds(ids);
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"admin".equals(currentUsername)) {
            list = list.stream()
                    .filter(item -> ObjectUtil.equal(item.getCreator(), currentUsername))
                    .collect(ArrayList::new, (l, item) -> l.add(item), ArrayList::addAll);
        }
        return list;
    }

    @Override
    public Map<Long, ErpLiveBroadcastingReviewDO> getLiveBroadcastingReviewMap(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingReviewList(ids, currentUsername), ErpLiveBroadcastingReviewDO::getId);
    }

    private void validateLiveBroadcastingReviewForCreateOrUpdate(Long id, ErpLiveBroadcastingReviewSaveReqVO reqVO) {
        // 1. 校验直播复盘编号唯一
        ErpLiveBroadcastingReviewDO liveBroadcastingReview = liveBroadcastingReviewMapper.selectByNo(reqVO.getNo());
        if (liveBroadcastingReview != null && !liveBroadcastingReview.getId().equals(id)) {
            throw exception(LIVE_BROADCASTING_REVIEW_NO_EXISTS);
        }

        // 2. 校验直播货盘编号和客户名称组合唯一性
        if (reqVO.getLiveBroadcastingId() != null && StrUtil.isNotBlank(reqVO.getCustomerName())) {
            ErpLiveBroadcastingReviewDO existReview = liveBroadcastingReviewMapper.selectByLiveBroadcastingIdAndCustomerName(
                    reqVO.getLiveBroadcastingId(), reqVO.getCustomerName(), id);
            if (existReview != null) {
                throw exception(LIVE_BROADCASTING_REVIEW_LIVE_BROADCASTING_ID_CUSTOMER_NAME_EXISTS_WITH_CREATOR, existReview.getCreator());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpLiveBroadcastingReviewImportRespVO importLiveBroadcastingReviewList(List<ErpLiveBroadcastingReviewImportExcelVO> importList, boolean isUpdateSupport, String currentUsername) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(LIVE_BROADCASTING_REVIEW_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpLiveBroadcastingReviewImportRespVO respVO = ErpLiveBroadcastingReviewImportRespVO.builder()
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

            // 2. 批量处理数据
            List<ErpLiveBroadcastingReviewDO> createList = new ArrayList<>();
            List<ErpLiveBroadcastingReviewDO> updateList = new ArrayList<>();

            // 3. 客户名称直接使用，不再查询客户表

            // 4. 批量查询直播货盘信息
            Set<String> liveBroadcastingNos = importList.stream()
                    .map(ErpLiveBroadcastingReviewImportExcelVO::getLiveBroadcastingNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> liveBroadcastingIdMap = liveBroadcastingNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(liveBroadcastingMapper.selectListByNoIn(liveBroadcastingNos), ErpLiveBroadcastingDO::getNo, ErpLiveBroadcastingDO::getId);

            // 5. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpLiveBroadcastingReviewImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpLiveBroadcastingReviewDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(liveBroadcastingReviewMapper.selectListByNoIn(noSet), ErpLiveBroadcastingReviewDO::getNo);

            // 6. 批量转换和保存数据
            for (int i = 0; i < importList.size(); i++) {
                ErpLiveBroadcastingReviewImportExcelVO importVO = importList.get(i);

                // 客户名称直接使用，不再需要客户ID

                // 转换货盘编号为货盘ID
                Long liveBroadcastingId = null;
                if (StrUtil.isNotBlank(importVO.getLiveBroadcastingNo())) {
                    liveBroadcastingId = liveBroadcastingIdMap.get(importVO.getLiveBroadcastingNo());
                    // 确保货盘编号存在
                    if (liveBroadcastingId == null) {
                        respVO.getFailureNames().put("第" + (i + 1) + "行", "直播货盘编号不存在: " + importVO.getLiveBroadcastingNo());
                        continue;
                    }
                } else {
                    // 货盘编号不能为空
                    respVO.getFailureNames().put("第" + (i + 1) + "行", "直播货盘编号不能为空");
                    continue;
                }

                // 判断是否支持更新
                ErpLiveBroadcastingReviewDO existReview = existMap.get(importVO.getNo());
                if (existReview == null) {
                    // 创建 - 自动生成新的no编号
                    ErpLiveBroadcastingReviewDO review = BeanUtils.toBean(importVO, ErpLiveBroadcastingReviewDO.class);
                    review.setNo(noRedisDAO.generate(ErpNoRedisDAO.LIVE_BROADCASTING_REVIEW_NO_PREFIX));
                    review.setLiveBroadcastingId(liveBroadcastingId);
                    createList.add(review);
                    respVO.getCreateNames().add(review.getNo());
                } else if (isUpdateSupport) {
                    // 更新 - 只更新导入文件中提供的非空字段，保留数据库中其他字段的原有值
                    // 更新直播货盘ID（如果提供）
                    if (liveBroadcastingId != null) {
                        existReview.setLiveBroadcastingId(liveBroadcastingId);
                    }
                    // 更新其他字段（需要根据ErpLiveBroadcastingReviewImportExcelVO的实际字段来补充）
                    updateList.add(existReview);
                    respVO.getUpdateNames().add(existReview.getNo());
                }
            }

            // 7. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                liveBroadcastingReviewMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(liveBroadcastingReviewMapper::updateById);
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
    private Map<String, String> validateAllImportData(List<ErpLiveBroadcastingReviewImportExcelVO> importList, boolean isUpdateSupport, String currentUsername) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 客户名称直接使用，不再查询客户表

        // 3. 批量查询直播货盘信息
        Set<String> liveBroadcastingNos = importList.stream()
                .map(ErpLiveBroadcastingReviewImportExcelVO::getLiveBroadcastingNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, Long> liveBroadcastingIdMap = liveBroadcastingNos.isEmpty() ? Collections.emptyMap() :
                convertMap(liveBroadcastingMapper.selectListByNoIn(liveBroadcastingNos), ErpLiveBroadcastingDO::getNo, ErpLiveBroadcastingDO::getId);

        // 4. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpLiveBroadcastingReviewImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpLiveBroadcastingReviewDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(liveBroadcastingReviewMapper.selectListByNoIn(noSet), ErpLiveBroadcastingReviewDO::getNo);

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();

        // 5. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpLiveBroadcastingReviewImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");

            try {
                // 5.1 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "直播复盘编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 5.2 客户名称直接使用，不再校验

                // 5.3 校验货盘编号是否存在
                Long liveBroadcastingId = null;
                if (StrUtil.isNotBlank(importVO.getLiveBroadcastingNo())) {
                    liveBroadcastingId = liveBroadcastingIdMap.get(importVO.getLiveBroadcastingNo());
                    if (liveBroadcastingId == null) {
                        allErrors.put(errorKey, "直播货盘编号不存在: " + importVO.getLiveBroadcastingNo());
                        continue;
                    }
                }

                // 5.4 校验直播货盘编号和客户名称组合唯一性
                if (liveBroadcastingId != null && StrUtil.isNotBlank(importVO.getCustomerName())) {
                    ErpLiveBroadcastingReviewDO existReview = existMap.get(importVO.getNo());
                    Long excludeId = existReview != null ? existReview.getId() : null;

                    ErpLiveBroadcastingReviewDO duplicateReview = liveBroadcastingReviewMapper.selectByLiveBroadcastingIdAndCustomerName(
                            liveBroadcastingId, importVO.getCustomerName(), excludeId);
                    if (duplicateReview != null) {
                        allErrors.put(errorKey, "直播货盘编号和客户名称组合已存在，创建人：" + duplicateReview.getCreator());
                        continue;
                    }
                }

                // 5.5 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpLiveBroadcastingReviewDO review = BeanUtils.toBean(importVO, ErpLiveBroadcastingReviewDO.class);
                    if (review == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 5.6 判断是新增还是更新，并进行相应校验
                ErpLiveBroadcastingReviewDO existReview = existMap.get(importVO.getNo());
                if (existReview == null) {
                    // 新增校验：无需额外校验
                } else if (isUpdateSupport) {
                    // 更新校验：检查权限
                    if (!"admin".equals(currentUsername) && !ObjectUtil.equal(existReview.getCreator(), currentUsername)) {
                        allErrors.put(errorKey, "无权限修改该记录");
                        continue;
                    }
                } else {
                    allErrors.put(errorKey, "直播复盘编号不存在且不支持更新: " + importVO.getNo());
                }
            } catch (Exception ex) {
                allErrors.put(errorKey, "系统异常: " + ex.getMessage());
            }
        }

        return allErrors;
    }

    /**
     * 数据类型校验前置检查
     * 检查所有转换错误，如果有错误则返回错误信息，不进行后续导入
     */
    private Map<String, String> validateDataTypeErrors(List<ErpLiveBroadcastingReviewImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取编号 - 修复行号索引问题
                String no = "未知编号";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpLiveBroadcastingReviewImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        no = importVO.getNo();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + no + ")";
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
