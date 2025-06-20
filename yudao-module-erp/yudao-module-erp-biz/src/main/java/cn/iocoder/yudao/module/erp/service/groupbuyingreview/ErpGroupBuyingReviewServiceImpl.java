package cn.iocoder.yudao.module.erp.service.groupbuyingreview;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
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
                        // 更新 - 检查权限
                        if (!"admin".equals(currentUsername) && !ObjectUtil.equal(existGroupBuyingReview.getCreator(), currentUsername)) {
                            throw exception(GROUP_BUYING_REVIEW_IMPORT_NO_PERMISSION, i + 1, importVO.getNo());
                        }
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
