package cn.iocoder.yudao.module.erp.service.groupbuyingreview;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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
    public Long createGroupBuyingReview(ErpGroupBuyingReviewSaveReqVO createReqVO) {
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
    public void updateGroupBuyingReview(ErpGroupBuyingReviewSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateGroupBuyingReview(updateReqVO.getId());
        // 1.2 校验数据
        validateGroupBuyingReviewForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新团购复盘记录
        ErpGroupBuyingReviewDO updateObj = BeanUtils.toBean(updateReqVO, ErpGroupBuyingReviewDO.class);
        groupBuyingReviewMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroupBuyingReview(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpGroupBuyingReviewDO> groupBuyingReviews = groupBuyingReviewMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(groupBuyingReviews)) {
            throw exception(GROUP_BUYING_REVIEW_NOT_EXISTS);
        }
        // 2. 删除团购复盘记录
        groupBuyingReviewMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpGroupBuyingReviewDO getGroupBuyingReview(Long id) {
        return groupBuyingReviewMapper.selectById(id);
    }

    @Override
    public ErpGroupBuyingReviewDO validateGroupBuyingReview(Long id) {
        ErpGroupBuyingReviewDO groupBuyingReview = groupBuyingReviewMapper.selectById(id);
        if (groupBuyingReview == null) {
            throw exception(GROUP_BUYING_REVIEW_NOT_EXISTS);
        }
        return groupBuyingReview;
    }

    @Override
    public PageResult<ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOPage(ErpGroupBuyingReviewPageReqVO pageReqVO) {
        return groupBuyingReviewMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }

        // 1. 查询团购复盘基础信息
        List<ErpGroupBuyingReviewDO> list = groupBuyingReviewMapper.selectBatchIds(ids);
        List<ErpGroupBuyingReviewRespVO> respVOList = BeanUtils.toBean(list, ErpGroupBuyingReviewRespVO.class);

        // 2. 批量查询团购货盘信息
        Set<Long> groupBuyingIds = list.stream()
                .map(ErpGroupBuyingReviewDO::getGroupBuyingId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, ErpGroupBuyingDO> groupBuyingMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(groupBuyingIds)) {
            List<ErpGroupBuyingDO> groupBuyingList = groupBuyingMapper.selectBatchIds(groupBuyingIds);
            groupBuyingMap = convertMap(groupBuyingList, ErpGroupBuyingDO::getId);
        }

        // 3. 批量查询客户信息
        Set<Long> customerIds = list.stream()
                .map(ErpGroupBuyingReviewDO::getCustomerId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, ErpCustomerDO> customerMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(customerIds)) {
            List<ErpCustomerDO> customerList = customerMapper.selectBatchIds(customerIds);
            customerMap = convertMap(customerList, ErpCustomerDO::getId);
        }

        // 4. 填充关联信息
        for (int i = 0; i < list.size(); i++) {
            ErpGroupBuyingReviewDO reviewDO = list.get(i);
            ErpGroupBuyingReviewRespVO respVO = respVOList.get(i);

            // 填充团购货盘信息
            if (reviewDO.getGroupBuyingId() != null) {
                ErpGroupBuyingDO groupBuying = groupBuyingMap.get(reviewDO.getGroupBuyingId());
                if (groupBuying != null) {
                    respVO.setBrandName(groupBuying.getBrandName());
                    respVO.setGroupBuyingNo(groupBuying.getNo());
                    respVO.setProductName(groupBuying.getProductName());
                    respVO.setProductSpec(groupBuying.getProductSpec());
                    respVO.setProductSku(groupBuying.getProductSku());
                    respVO.setGroupMechanism(groupBuying.getGroupMechanism());
                    respVO.setStatus(groupBuying.getStatus());
                    respVO.setExpressFee(groupBuying.getExpressFee());
                }
            }

            // 填充客户信息
            if (reviewDO.getCustomerId() != null) {
                ErpCustomerDO customer = customerMap.get(reviewDO.getCustomerId());
                if (customer != null) {
                    respVO.setCustomerName(customer.getName());
                }
            }
        }

        return respVOList;
    }

    @Override
    public Map<Long, ErpGroupBuyingReviewRespVO> getGroupBuyingReviewVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingReviewVOList(ids), ErpGroupBuyingReviewRespVO::getId);
    }

    @Override
    public List<ErpGroupBuyingReviewDO> getGroupBuyingReviewList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return groupBuyingReviewMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpGroupBuyingReviewDO> getGroupBuyingReviewMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingReviewList(ids), ErpGroupBuyingReviewDO::getId);
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
    public ErpGroupBuyingReviewImportRespVO importGroupBuyingReviewList(List<ErpGroupBuyingReviewImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(GROUP_BUYING_REVIEW_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpGroupBuyingReviewImportRespVO respVO = ErpGroupBuyingReviewImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理数据转换
        List<ErpGroupBuyingReviewDO> createList = new ArrayList<>();
        List<ErpGroupBuyingReviewDO> updateList = new ArrayList<>();

        try {
            // 批量查询团购货盘信息
            Set<String> groupBuyingNos = importList.stream()
                    .map(ErpGroupBuyingReviewImportExcelVO::getGroupBuyingId)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> groupBuyingIdMap = groupBuyingNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(groupBuyingMapper.selectListByNoIn(groupBuyingNos), ErpGroupBuyingDO::getNo, ErpGroupBuyingDO::getId);

            // 批量查询客户信息
            Set<String> customerNames = importList.stream()
                    .map(ErpGroupBuyingReviewImportExcelVO::getCustomerName)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> customerIdMap = customerNames.isEmpty() ? Collections.emptyMap() :
                    convertMap(customerMapper.selectListByNameIn(customerNames), ErpCustomerDO::getName, ErpCustomerDO::getId);

            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpGroupBuyingReviewImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpGroupBuyingReviewDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(groupBuyingReviewMapper.selectListByNoIn(noSet), ErpGroupBuyingReviewDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();

            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpGroupBuyingReviewImportExcelVO importVO = importList.get(i);
                try {
                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(GROUP_BUYING_REVIEW_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 将团购货盘编号转换为团购货盘ID
                    if (StrUtil.isNotBlank(importVO.getGroupBuyingId())) {
                        Long groupBuyingId = groupBuyingIdMap.get(importVO.getGroupBuyingId());
                        if (groupBuyingId == null) {
                            throw exception(GROUP_BUYING_REVIEW_IMPORT_GROUP_BUYING_NOT_EXISTS, i + 1, importVO.getGroupBuyingId());
                        }
                        importVO.setGroupBuyingId(groupBuyingId.toString());
                    }

                    // 将客户名称转换为客户ID
                    Long customerId = null;
                    if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                        customerId = customerIdMap.get(importVO.getCustomerName());
                        if (customerId == null) {
                            throw exception(GROUP_BUYING_REVIEW_IMPORT_CUSTOMER_NOT_EXISTS, i + 1, importVO.getCustomerName());
                        }
                    }

                    // 判断是否支持更新
                    ErpGroupBuyingReviewDO existGroupBuyingReview = existMap.get(importVO.getNo());
                    if (existGroupBuyingReview == null) {
                        // 创建 - 自动生成新的no编号
                        ErpGroupBuyingReviewDO groupBuyingReview = BeanUtils.toBean(importVO, ErpGroupBuyingReviewDO.class);
                        groupBuyingReview.setNo(noRedisDAO.generate(ErpNoRedisDAO.GROUP_BUYING_REVIEW_NO_PREFIX));
                        // 转换字段类型
                        if (StrUtil.isNotBlank(importVO.getGroupBuyingId())) {
                            groupBuyingReview.setGroupBuyingId(Long.parseLong(importVO.getGroupBuyingId()));
                        }
                        if (customerId != null) {
                            groupBuyingReview.setCustomerId(customerId);
                        }
                        createList.add(groupBuyingReview);
                        respVO.getCreateNames().add(groupBuyingReview.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpGroupBuyingReviewDO updateGroupBuyingReview = BeanUtils.toBean(importVO, ErpGroupBuyingReviewDO.class);
                        updateGroupBuyingReview.setId(existGroupBuyingReview.getId());
                        // 转换字段类型
                        if (StrUtil.isNotBlank(importVO.getGroupBuyingId())) {
                            updateGroupBuyingReview.setGroupBuyingId(Long.parseLong(importVO.getGroupBuyingId()));
                        }
                        if (customerId != null) {
                            updateGroupBuyingReview.setCustomerId(customerId);
                        }
                        updateList.add(updateGroupBuyingReview);
                        respVO.getUpdateNames().add(updateGroupBuyingReview.getNo());
                    } else {
                        throw exception(GROUP_BUYING_REVIEW_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
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
                groupBuyingReviewMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(groupBuyingReviewMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }
}
