package cn.iocoder.yudao.module.erp.service.privatebroadcastingreview;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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
    public Long createPrivateBroadcastingReview(ErpPrivateBroadcastingReviewSaveReqVO createReqVO) {
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
    public void updatePrivateBroadcastingReview(ErpPrivateBroadcastingReviewSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validatePrivateBroadcastingReview(updateReqVO.getId());
        // 1.2 校验数据
        validatePrivateBroadcastingReviewForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新私播复盘记录
        ErpPrivateBroadcastingReviewDO updateObj = BeanUtils.toBean(updateReqVO, ErpPrivateBroadcastingReviewDO.class);
        privateBroadcastingReviewMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePrivateBroadcastingReview(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpPrivateBroadcastingReviewDO> privateBroadcastingReviews = privateBroadcastingReviewMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(privateBroadcastingReviews)) {
            throw exception(PRIVATE_BROADCASTING_REVIEW_NOT_EXISTS);
        }
        // 2. 删除私播复盘记录
        privateBroadcastingReviewMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpPrivateBroadcastingReviewDO getPrivateBroadcastingReview(Long id) {
        return privateBroadcastingReviewMapper.selectById(id);
    }

    @Override
    public ErpPrivateBroadcastingReviewDO validatePrivateBroadcastingReview(Long id) {
        ErpPrivateBroadcastingReviewDO privateBroadcastingReview = privateBroadcastingReviewMapper.selectById(id);
        if (privateBroadcastingReview == null) {
            throw exception(PRIVATE_BROADCASTING_REVIEW_NOT_EXISTS);
        }
        return privateBroadcastingReview;
    }

    @Override
    public PageResult<ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOPage(ErpPrivateBroadcastingReviewPageReqVO pageReqVO) {
        PageResult<ErpPrivateBroadcastingReviewRespVO> pageResult = privateBroadcastingReviewMapper.selectPage(pageReqVO);

        // 填充关联信息
        fillRelatedInfo(pageResult.getList());

        return pageResult;
    }

    @Override
    public List<ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }

        // 1. 查询私播复盘记录
        List<ErpPrivateBroadcastingReviewDO> list = privateBroadcastingReviewMapper.selectBatchIds(ids);
        List<ErpPrivateBroadcastingReviewRespVO> respVOList = BeanUtils.toBean(list, ErpPrivateBroadcastingReviewRespVO.class);

        // 2. 获取私播货盘ID集合
        List<Long> privateBroadcastingIds = list.stream()
                .map(ErpPrivateBroadcastingReviewDO::getPrivateBroadcastingId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        // 3. 获取客户ID集合
        List<Long> customerIds = list.stream()
                .map(ErpPrivateBroadcastingReviewDO::getCustomerId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        // 4. 批量查询私播货盘信息
        Map<Long, ErpPrivateBroadcastingDO> privateBroadcastingMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(privateBroadcastingIds)) {
            List<ErpPrivateBroadcastingDO> privateBroadcastingList = privateBroadcastingMapper.selectBatchIds(privateBroadcastingIds);
            privateBroadcastingMap = convertMap(privateBroadcastingList, ErpPrivateBroadcastingDO::getId);
        }

        // 5. 批量查询客户信息
        Map<Long, ErpCustomerDO> customerMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(customerIds)) {
            List<ErpCustomerDO> customerList = customerMapper.selectBatchIds(customerIds);
            customerMap = convertMap(customerList, ErpCustomerDO::getId);
        }

        // 6. 填充关联信息
        for (int i = 0; i < respVOList.size(); i++) {
            ErpPrivateBroadcastingReviewRespVO respVO = respVOList.get(i);
            ErpPrivateBroadcastingReviewDO reviewDO = list.get(i);

            // 填充私播货盘信息
            if (reviewDO.getPrivateBroadcastingId() != null) {
                ErpPrivateBroadcastingDO privateBroadcastingDO = privateBroadcastingMap.get(reviewDO.getPrivateBroadcastingId());
                if (privateBroadcastingDO != null) {
                    respVO.setPrivateBroadcastingNo(privateBroadcastingDO.getNo());
                    respVO.setProductName(privateBroadcastingDO.getProductName());
                    respVO.setProductSpec(privateBroadcastingDO.getProductSpec());
                    respVO.setProductSku(privateBroadcastingDO.getProductSku());
                    respVO.setLivePrice(privateBroadcastingDO.getLivePrice());
                    respVO.setPrivateStatus(privateBroadcastingDO.getPrivateStatus());
                    // 设置品牌ID（用于导出时的字典转换）
                    if (privateBroadcastingDO.getBrandName() != null) {
                        respVO.setBrandName(privateBroadcastingDO.getBrandName());
                    }
                }
            }

            // 填充客户信息
            if (reviewDO.getCustomerId() != null) {
                ErpCustomerDO customerDO = customerMap.get(reviewDO.getCustomerId());
                if (customerDO != null) {
                    respVO.setCustomerName(customerDO.getName());
                }
            }
        }

        return respVOList;
    }

    @Override
    public Map<Long, ErpPrivateBroadcastingReviewRespVO> getPrivateBroadcastingReviewVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPrivateBroadcastingReviewVOList(ids), ErpPrivateBroadcastingReviewRespVO::getId);
    }

    @Override
    public List<ErpPrivateBroadcastingReviewDO> getPrivateBroadcastingReviewList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return privateBroadcastingReviewMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpPrivateBroadcastingReviewDO> getPrivateBroadcastingReviewMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPrivateBroadcastingReviewList(ids), ErpPrivateBroadcastingReviewDO::getId);
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
    public ErpPrivateBroadcastingReviewImportRespVO importPrivateBroadcastingReviewList(List<ErpPrivateBroadcastingReviewImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(PRIVATE_BROADCASTING_REVIEW_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpPrivateBroadcastingReviewImportRespVO respVO = ErpPrivateBroadcastingReviewImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理数据转换
        List<ErpPrivateBroadcastingReviewDO> createList = new ArrayList<>();
        List<ErpPrivateBroadcastingReviewDO> updateList = new ArrayList<>();

        try {
            // 批量查询私播货盘信息
            Set<String> privateBroadcastingNos = importList.stream()
                    .map(ErpPrivateBroadcastingReviewImportExcelVO::getPrivateBroadcastingNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> privateBroadcastingIdMap = privateBroadcastingNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(privateBroadcastingMapper.selectListByNoIn(privateBroadcastingNos), ErpPrivateBroadcastingDO::getNo, ErpPrivateBroadcastingDO::getId);

            // 批量查询客户信息
            Set<String> customerNames = importList.stream()
                    .map(ErpPrivateBroadcastingReviewImportExcelVO::getCustomerName)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> customerIdMap = customerNames.isEmpty() ? Collections.emptyMap() :
                    convertMap(customerMapper.selectListByNameIn(customerNames), ErpCustomerDO::getName, ErpCustomerDO::getId);

            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpPrivateBroadcastingReviewImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpPrivateBroadcastingReviewDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(privateBroadcastingReviewMapper.selectListByNoIn(noSet), ErpPrivateBroadcastingReviewDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();

            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpPrivateBroadcastingReviewImportExcelVO importVO = importList.get(i);
                try {
                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(PRIVATE_BROADCASTING_REVIEW_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 转换私播货盘编号为ID
                    Long privateBroadcastingId = null;
                    if (StrUtil.isNotBlank(importVO.getPrivateBroadcastingNo())) {
                        privateBroadcastingId = privateBroadcastingIdMap.get(importVO.getPrivateBroadcastingNo());
                        if (privateBroadcastingId == null) {
                            throw exception(PRIVATE_BROADCASTING_REVIEW_IMPORT_PRIVATE_BROADCASTING_NOT_EXISTS, i + 1, importVO.getPrivateBroadcastingNo());
                        }
                    }

                    // 转换客户名称为ID
                    Long customerId = null;
                    if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                        customerId = customerIdMap.get(importVO.getCustomerName());
                        if (customerId == null) {
                            throw exception(PRIVATE_BROADCASTING_REVIEW_IMPORT_CUSTOMER_NOT_EXISTS, i + 1, importVO.getCustomerName());
                        }
                    }

                    // 判断是否支持更新
                    ErpPrivateBroadcastingReviewDO existReview = existMap.get(importVO.getNo());
                    if (existReview == null) {
                        // 创建 - 自动生成新的no编号
                        ErpPrivateBroadcastingReviewDO review = BeanUtils.toBean(importVO, ErpPrivateBroadcastingReviewDO.class);
                        review.setNo(noRedisDAO.generate(ErpNoRedisDAO.PRIVATE_BROADCASTING_REVIEW_NO_PREFIX));
                        review.setPrivateBroadcastingId(privateBroadcastingId);
                        review.setCustomerId(customerId);
                        createList.add(review);
                        respVO.getCreateNames().add(review.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpPrivateBroadcastingReviewDO updateReview = BeanUtils.toBean(importVO, ErpPrivateBroadcastingReviewDO.class);
                        updateReview.setId(existReview.getId());
                        updateReview.setPrivateBroadcastingId(privateBroadcastingId);
                        updateReview.setCustomerId(customerId);
                        updateList.add(updateReview);
                        respVO.getUpdateNames().add(updateReview.getNo());
                    } else {
                        throw exception(PRIVATE_BROADCASTING_REVIEW_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
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
                privateBroadcastingReviewMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(privateBroadcastingReviewMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }
}
