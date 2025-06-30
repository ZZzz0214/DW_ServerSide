package cn.iocoder.yudao.module.erp.service.privatebroadcastingreview;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastingreview.vo.ErpPrivateBroadcastingReviewSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcasting.ErpPrivateBroadcastingDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastingreview.ErpPrivateBroadcastingReviewDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcasting.ErpPrivateBroadcastingMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcastingreview.ErpPrivateBroadcastingReviewMapper;
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
public class ErpPrivateBroadcastingReviewServiceImpl implements ErpPrivateBroadcastingReviewService {

    @Resource
    private ErpPrivateBroadcastingReviewMapper privateBroadcastingReviewMapper;
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpPrivateBroadcastingMapper privateBroadcastingMapper;

    @Resource
    private ErpCustomerMapper customerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPrivateBroadcastingReview(ErpPrivateBroadcastingReviewSaveReqVO createReqVO, String currentUsername) {
        // 1. 校验数据
        validatePrivateBroadcastingReviewForCreateOrUpdate(null, createReqVO);

        // 2. 生成私播复盘编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.PRIVATE_BROADCASTING_REVIEW_NO_PREFIX);
        if (privateBroadcastingReviewMapper.selectByNo(no) != null) {
            throw exception(PRIVATE_BROADCASTING_REVIEW_NO_EXISTS);
        }

        // 3. 插入私播复盘记录
        ErpPrivateBroadcastingReviewDO privateBroadcastingReview = BeanUtils.toBean(createReqVO, ErpPrivateBroadcastingReviewDO.class)
                .setNo(no);
        privateBroadcastingReviewMapper.insert(privateBroadcastingReview);

        return privateBroadcastingReview.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivateBroadcastingReview(ErpPrivateBroadcastingReviewSaveReqVO updateReqVO, String currentUsername) {
        // 1.1 校验存在
        validatePrivateBroadcastingReview(updateReqVO.getId(), currentUsername);
        // 1.2 校验数据
        validatePrivateBroadcastingReviewForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新私播复盘记录
        ErpPrivateBroadcastingReviewDO updateObj = BeanUtils.toBean(updateReqVO, ErpPrivateBroadcastingReviewDO.class);
        privateBroadcastingReviewMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePrivateBroadcastingReview(List<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在且属于当前用户
        for (Long id : ids) {
            validatePrivateBroadcastingReview(id, currentUsername);
        }
        // 2. 删除私播复盘记录
        privateBroadcastingReviewMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpPrivateBroadcastingReviewDO getPrivateBroadcastingReview(Long id, String currentUsername) {
        ErpPrivateBroadcastingReviewDO privateBroadcastingReview = privateBroadcastingReviewMapper.selectById(id);
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (privateBroadcastingReview != null && !"admin".equals(currentUsername) && !ObjectUtil.equal(privateBroadcastingReview.getCreator(), currentUsername)) {
            return null; // 不是当前用户的数据且不是admin，返回null
        }
        return privateBroadcastingReview;
    }

    /**
     * 获取私播复盘详情（带关联信息）
     */
    @Override
    public ErpPrivateBroadcastingReviewRespVO getPrivateBroadcastingReviewVO(Long id, String currentUsername) {
        ErpPrivateBroadcastingReviewDO privateBroadcastingReview = getPrivateBroadcastingReview(id, currentUsername);
        if (privateBroadcastingReview == null) {
            return null;
        }
        
        // 转换为VO并填充关联信息
        ErpPrivateBroadcastingReviewRespVO respVO = BeanUtils.toBean(privateBroadcastingReview, ErpPrivateBroadcastingReviewRespVO.class);
        
        // 填充关联信息
        if (privateBroadcastingReview.getPrivateBroadcastingId() != null) {
            ErpPrivateBroadcastingDO privateBroadcastingDO = privateBroadcastingMapper.selectById(privateBroadcastingReview.getPrivateBroadcastingId());
            if (privateBroadcastingDO != null) {
                respVO.setPrivateBroadcastingNo(privateBroadcastingDO.getNo());
                respVO.setProductName(privateBroadcastingDO.getProductName());
                respVO.setProductSpec(privateBroadcastingDO.getProductSpec());
                respVO.setProductSku(privateBroadcastingDO.getProductSku());
                respVO.setLivePrice(privateBroadcastingDO.getLivePrice());
                respVO.setPrivateStatus(privateBroadcastingDO.getPrivateStatus());
                if (privateBroadcastingDO.getBrandName() != null) {
                    respVO.setBrandName(privateBroadcastingDO.getBrandName());
                }
            }
        }
        
        // 填充客户信息
        if (privateBroadcastingReview.getCustomerId() != null) {
            ErpCustomerDO customerDO = customerMapper.selectById(privateBroadcastingReview.getCustomerId());
            if (customerDO != null) {
                respVO.setCustomerName(customerDO.getName());
            }
        }
        
        return respVO;
    }

    @Override
    public ErpPrivateBroadcastingReviewDO validatePrivateBroadcastingReview(Long id, String currentUsername) {
        ErpPrivateBroadcastingReviewDO privateBroadcastingReview = privateBroadcastingReviewMapper.selectById(id);
        if (privateBroadcastingReview == null) {
            throw exception(PRIVATE_BROADCASTING_REVIEW_NOT_EXISTS);
        }
        // admin用户可以操作全部数据，其他用户只能操作自己的数据
        if (!"admin".equals(currentUsername) && !ObjectUtil.equal(privateBroadcastingReview.getCreator(), currentUsername)) {
            throw exception(PRIVATE_BROADCASTING_REVIEW_NOT_EXISTS); // 不是当前用户的数据且不是admin
        }
        return privateBroadcastingReview;
    }

    @Override
    public PageResult<ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOPage(ErpPrivateBroadcastingReviewPageReqVO pageReqVO, String currentUsername) {
        PageResult<ErpPrivateBroadcastingReviewRespVO> pageResult = privateBroadcastingReviewMapper.selectPage(pageReqVO, currentUsername);

        // 填充关联信息
        fillRelatedInfo(pageResult.getList());

        return pageResult;
    }

    @Override
    public List<ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOList(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        
        // 使用Mapper方法获取完整信息，但需要过滤权限
        List<ErpPrivateBroadcastingReviewRespVO> list = privateBroadcastingReviewMapper.selectListByIds(ids);
        
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"admin".equals(currentUsername)) {
            list = list.stream()
                    .filter(item -> ObjectUtil.equal(item.getCreator(), currentUsername))
                    .collect(ArrayList::new, (l, item) -> l.add(item), ArrayList::addAll);
        }
        
        return list;
    }

    @Override
    public Map<Long, ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOMap(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPrivateBroadcastingReviewVOList(ids, currentUsername), ErpPrivateBroadcastingReviewRespVO::getId);
    }

    @Override
    public List<ErpPrivateBroadcastingReviewDO> getPrivateBroadcastingReviewList(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpPrivateBroadcastingReviewDO> list = privateBroadcastingReviewMapper.selectBatchIds(ids);
        // admin用户可以查看全部数据，其他用户只能查看自己的数据
        if (!"admin".equals(currentUsername)) {
            list = list.stream()
                    .filter(item -> ObjectUtil.equal(item.getCreator(), currentUsername))
                    .collect(ArrayList::new, (l, item) -> l.add(item), ArrayList::addAll);
        }
        return list;
    }

    @Override
    public Map<Long, ErpPrivateBroadcastingReviewDO> getPrivateBroadcastingReviewMap(Collection<Long> ids, String currentUsername) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPrivateBroadcastingReviewList(ids, currentUsername), ErpPrivateBroadcastingReviewDO::getId);
    }

    private void validatePrivateBroadcastingReviewForCreateOrUpdate(Long id, ErpPrivateBroadcastingReviewSaveReqVO reqVO) {
        // 1. 校验私播复盘编号唯一
        ErpPrivateBroadcastingReviewDO privateBroadcastingReview = privateBroadcastingReviewMapper.selectByNo(reqVO.getNo());
        if (privateBroadcastingReview != null && !privateBroadcastingReview.getId().equals(id)) {
            throw exception(PRIVATE_BROADCASTING_REVIEW_NO_EXISTS);
        }
    }

    /**
     * 填充关联信息（分页查询用，品牌ID不进行字典转换，前端处理）
     */
    private void fillRelatedInfo(List<ErpPrivateBroadcastingReviewRespVO> respVOList) {
        if (CollUtil.isEmpty(respVOList)) {
            return;
        }

        // 获取所有私播货盘ID
        List<Long> privateBroadcastingIds = respVOList.stream()
                .map(ErpPrivateBroadcastingReviewRespVO::getPrivateBroadcastingId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(privateBroadcastingIds)) {
            return;
        }

        // 查询私播货盘信息
        List<ErpPrivateBroadcastingDO> privateBroadcastingList = privateBroadcastingMapper.selectBatchIds(privateBroadcastingIds);
        Map<Long, ErpPrivateBroadcastingDO> privateBroadcastingMap = convertMap(privateBroadcastingList, ErpPrivateBroadcastingDO::getId);

        // 填充关联信息
        for (ErpPrivateBroadcastingReviewRespVO respVO : respVOList) {
            if (respVO.getPrivateBroadcastingId() != null) {
                ErpPrivateBroadcastingDO privateBroadcastingDO = privateBroadcastingMap.get(respVO.getPrivateBroadcastingId());
                if (privateBroadcastingDO != null) {
                    // 填充私播货盘信息
                    respVO.setPrivateBroadcastingNo(privateBroadcastingDO.getNo());
                    respVO.setProductName(privateBroadcastingDO.getProductName());
                    respVO.setProductSpec(privateBroadcastingDO.getProductSpec());
                    respVO.setProductSku(privateBroadcastingDO.getProductSku());
                    respVO.setLivePrice(privateBroadcastingDO.getLivePrice());
                    respVO.setPrivateStatus(privateBroadcastingDO.getPrivateStatus());
                    // 直接设置品牌ID，不进行字典转换（前端处理）
                    if (privateBroadcastingDO.getBrandName() != null) {
                        respVO.setBrandName(privateBroadcastingDO.getBrandName());
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpPrivateBroadcastingReviewImportRespVO importPrivateBroadcastingReviewList(List<ErpPrivateBroadcastingReviewImportExcelVO> importList, boolean isUpdateSupport, String currentUsername) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(PRIVATE_BROADCASTING_REVIEW_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpPrivateBroadcastingReviewImportRespVO respVO = ErpPrivateBroadcastingReviewImportRespVO.builder()
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
            List<ErpPrivateBroadcastingReviewDO> createList = new ArrayList<>();
            List<ErpPrivateBroadcastingReviewDO> updateList = new ArrayList<>();

            // 3. 批量查询客户信息
            Set<String> customerNames = importList.stream()
                    .map(ErpPrivateBroadcastingReviewImportExcelVO::getCustomerName)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> customerIdMap = customerNames.isEmpty() ? Collections.emptyMap() :
                    convertMap(customerMapper.selectListByNameIn(customerNames), ErpCustomerDO::getName, ErpCustomerDO::getId);

            // 4. 批量查询私播货盘信息
            Set<String> privateBroadcastingNos = importList.stream()
                    .map(ErpPrivateBroadcastingReviewImportExcelVO::getPrivateBroadcastingNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> privateBroadcastingIdMap = privateBroadcastingNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(privateBroadcastingMapper.selectListByNoIn(privateBroadcastingNos), ErpPrivateBroadcastingDO::getNo, ErpPrivateBroadcastingDO::getId);

            // 5. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpPrivateBroadcastingReviewImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpPrivateBroadcastingReviewDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(privateBroadcastingReviewMapper.selectListByNoIn(noSet), ErpPrivateBroadcastingReviewDO::getNo);

            // 6. 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpPrivateBroadcastingReviewImportExcelVO importVO = importList.get(i);

                // 转换客户名称为客户ID
                Long customerId = null;
                if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                    customerId = customerIdMap.get(importVO.getCustomerName());
                }

                // 转换货盘编号为货盘ID
                Long privateBroadcastingId = null;
                if (StrUtil.isNotBlank(importVO.getPrivateBroadcastingNo())) {
                    privateBroadcastingId = privateBroadcastingIdMap.get(importVO.getPrivateBroadcastingNo());
                }

                // 判断是否支持更新
                ErpPrivateBroadcastingReviewDO existReview = existMap.get(importVO.getNo());
                if (existReview == null) {
                    // 创建 - 自动生成新的no编号
                    ErpPrivateBroadcastingReviewDO review = BeanUtils.toBean(importVO, ErpPrivateBroadcastingReviewDO.class);
                    review.setNo(noRedisDAO.generate(ErpNoRedisDAO.PRIVATE_BROADCASTING_REVIEW_NO_PREFIX));
                    review.setCustomerId(customerId);
                    review.setPrivateBroadcastingId(privateBroadcastingId);
                    createList.add(review);
                    respVO.getCreateNames().add(review.getNo());
                } else if (isUpdateSupport) {
                    // 更新
                    ErpPrivateBroadcastingReviewDO updateReview = BeanUtils.toBean(importVO, ErpPrivateBroadcastingReviewDO.class);
                    updateReview.setId(existReview.getId());
                    updateReview.setCustomerId(customerId);
                    updateReview.setPrivateBroadcastingId(privateBroadcastingId);
                    updateList.add(updateReview);
                    respVO.getUpdateNames().add(updateReview.getNo());
                }
            }

            // 7. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                privateBroadcastingReviewMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(privateBroadcastingReviewMapper::updateById);
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
    private Map<String, String> validateAllImportData(List<ErpPrivateBroadcastingReviewImportExcelVO> importList, boolean isUpdateSupport, String currentUsername) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询客户信息
        Set<String> customerNames = importList.stream()
                .map(ErpPrivateBroadcastingReviewImportExcelVO::getCustomerName)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, Long> customerIdMap = customerNames.isEmpty() ? Collections.emptyMap() :
                convertMap(customerMapper.selectListByNameIn(customerNames), ErpCustomerDO::getName, ErpCustomerDO::getId);

        // 3. 批量查询私播货盘信息
        Set<String> privateBroadcastingNos = importList.stream()
                .map(ErpPrivateBroadcastingReviewImportExcelVO::getPrivateBroadcastingNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, Long> privateBroadcastingIdMap = privateBroadcastingNos.isEmpty() ? Collections.emptyMap() :
                convertMap(privateBroadcastingMapper.selectListByNoIn(privateBroadcastingNos), ErpPrivateBroadcastingDO::getNo, ErpPrivateBroadcastingDO::getId);

        // 4. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpPrivateBroadcastingReviewImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpPrivateBroadcastingReviewDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(privateBroadcastingReviewMapper.selectListByNoIn(noSet), ErpPrivateBroadcastingReviewDO::getNo);

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();

        // 5. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpPrivateBroadcastingReviewImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getNo()) ? "(" + importVO.getNo() + ")" : "");

            try {
                // 5.1 基础数据校验
                if (StrUtil.isEmpty(importVO.getCustomerName())) {
                    allErrors.put(errorKey, "客户名称不能为空");
                    continue;
                }

                // 5.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "私播复盘编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 5.3 校验客户名称是否存在
                if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                    Long customerId = customerIdMap.get(importVO.getCustomerName());
                    if (customerId == null) {
                        allErrors.put(errorKey, "客户不存在: " + importVO.getCustomerName());
                        continue;
                    }
                }

                // 5.4 校验私播货盘编号是否存在
                if (StrUtil.isNotBlank(importVO.getPrivateBroadcastingNo())) {
                    Long privateBroadcastingId = privateBroadcastingIdMap.get(importVO.getPrivateBroadcastingNo());
                    if (privateBroadcastingId == null) {
                        allErrors.put(errorKey, "私播货盘不存在: " + importVO.getPrivateBroadcastingNo());
                        continue;
                    }
                }

                // 5.5 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpPrivateBroadcastingReviewDO review = convertImportVOToDO(importVO);
                    if (review == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 5.6 判断是新增还是更新，并进行相应校验
                ErpPrivateBroadcastingReviewDO existReview = existMap.get(importVO.getNo());
                if (existReview == null) {
                    // 新增校验：可以添加特定的校验逻辑
                } else if (isUpdateSupport) {
                    // 更新校验：检查权限
                    if (!"admin".equals(currentUsername) && !ObjectUtil.equal(existReview.getCreator(), currentUsername)) {
                        allErrors.put(errorKey, "无权限更新该记录: " + importVO.getNo());
                        continue;
                    }
                } else {
                    allErrors.put(errorKey, "私播复盘编号不存在且不支持更新: " + importVO.getNo());
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
    private Map<String, String> validateDataTypeErrors(List<ErpPrivateBroadcastingReviewImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取私播复盘编号 - 修复行号索引问题
                String privateBroadcastingReviewNo = "未知编号";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpPrivateBroadcastingReviewImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        privateBroadcastingReviewNo = importVO.getNo();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + privateBroadcastingReviewNo + ")";
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

    /**
     * 将导入VO转换为DO
     * 特别注意处理字段类型转换
     */
    private ErpPrivateBroadcastingReviewDO convertImportVOToDO(ErpPrivateBroadcastingReviewImportExcelVO importVO) {
        if (importVO == null) {
            return null;
        }

        // 使用BeanUtils进行基础转换
        ErpPrivateBroadcastingReviewDO review = BeanUtils.toBean(importVO, ErpPrivateBroadcastingReviewDO.class);

        // 手动设置转换器处理的字段，确保数据正确传递
        // 根据实际需要添加字段转换逻辑

        return review;
    }
}
