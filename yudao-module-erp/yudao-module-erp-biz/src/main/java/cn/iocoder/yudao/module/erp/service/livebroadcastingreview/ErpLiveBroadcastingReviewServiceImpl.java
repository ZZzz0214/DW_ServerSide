package cn.iocoder.yudao.module.erp.service.livebroadcastingreview;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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
    public Long createLiveBroadcastingReview(ErpLiveBroadcastingReviewSaveReqVO createReqVO) {
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
    public void updateLiveBroadcastingReview(ErpLiveBroadcastingReviewSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateLiveBroadcastingReview(updateReqVO.getId());
        // 1.2 校验数据
        validateLiveBroadcastingReviewForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新直播复盘记录
        ErpLiveBroadcastingReviewDO updateObj = BeanUtils.toBean(updateReqVO, ErpLiveBroadcastingReviewDO.class);
        liveBroadcastingReviewMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLiveBroadcastingReview(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpLiveBroadcastingReviewDO> liveBroadcastingReviews = liveBroadcastingReviewMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(liveBroadcastingReviews)) {
            throw exception(LIVE_BROADCASTING_REVIEW_NOT_EXISTS);
        }
        // 2. 删除直播复盘记录
        liveBroadcastingReviewMapper.deleteBatchIds(ids);
    }

    @Override
    public PageResult<ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReviewVOPage(ErpLiveBroadcastingReviewPageReqVO pageReqVO) {
        return liveBroadcastingReviewMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReviewVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        
        // 使用Mapper方法获取完整信息
        return liveBroadcastingReviewMapper.selectListByIds(ids);
    }

    @Override
    public Map<Long, ErpLiveBroadcastingReviewRespVO> getLiveBroadcastingReviewVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingReviewVOList(ids), ErpLiveBroadcastingReviewRespVO::getId);
    }

    @Override
    public ErpLiveBroadcastingReviewDO getLiveBroadcastingReview(Long id) {
        return liveBroadcastingReviewMapper.selectById(id);
    }

    @Override
    public ErpLiveBroadcastingReviewDO validateLiveBroadcastingReview(Long id) {
        ErpLiveBroadcastingReviewDO liveBroadcastingReview = liveBroadcastingReviewMapper.selectById(id);
        if (liveBroadcastingReview == null) {
            throw exception(LIVE_BROADCASTING_REVIEW_NOT_EXISTS);
        }
        return liveBroadcastingReview;
    }

    @Override
    public List<ErpLiveBroadcastingReviewDO> getLiveBroadcastingReviewList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return liveBroadcastingReviewMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpLiveBroadcastingReviewDO> getLiveBroadcastingReviewMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingReviewList(ids), ErpLiveBroadcastingReviewDO::getId);
    }

    private void validateLiveBroadcastingReviewForCreateOrUpdate(Long id, ErpLiveBroadcastingReviewSaveReqVO reqVO) {
        // 1. 校验直播复盘编号唯一
        ErpLiveBroadcastingReviewDO liveBroadcastingReview = liveBroadcastingReviewMapper.selectByNo(reqVO.getNo());
        if (liveBroadcastingReview != null && !liveBroadcastingReview.getId().equals(id)) {
            throw exception(LIVE_BROADCASTING_REVIEW_NO_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpLiveBroadcastingReviewImportRespVO importLiveBroadcastingReviewList(List<ErpLiveBroadcastingReviewImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(LIVE_BROADCASTING_REVIEW_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpLiveBroadcastingReviewImportRespVO respVO = ErpLiveBroadcastingReviewImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理数据
        List<ErpLiveBroadcastingReviewDO> createList = new ArrayList<>();
        List<ErpLiveBroadcastingReviewDO> updateList = new ArrayList<>();

        try {
            // 批量查询客户信息
            Set<String> customerNames = importList.stream()
                    .map(ErpLiveBroadcastingReviewImportExcelVO::getCustomerName)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> customerIdMap = customerNames.isEmpty() ? Collections.emptyMap() :
                    convertMap(customerMapper.selectListByNameIn(customerNames), ErpCustomerDO::getName, ErpCustomerDO::getId);

            // 批量查询直播货盘信息
            Set<String> liveBroadcastingNos = importList.stream()
                    .map(ErpLiveBroadcastingReviewImportExcelVO::getLiveBroadcastingNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> liveBroadcastingIdMap = liveBroadcastingNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(liveBroadcastingMapper.selectListByNoIn(liveBroadcastingNos), ErpLiveBroadcastingDO::getNo, ErpLiveBroadcastingDO::getId);

            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpLiveBroadcastingReviewImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpLiveBroadcastingReviewDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(liveBroadcastingReviewMapper.selectListByNoIn(noSet), ErpLiveBroadcastingReviewDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();
            
            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpLiveBroadcastingReviewImportExcelVO importVO = importList.get(i);
                try {
                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(LIVE_BROADCASTING_REVIEW_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }
                    
                    // 转换客户名称为客户ID
                    Long customerId = null;
                    if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                        customerId = customerIdMap.get(importVO.getCustomerName());
                        if (customerId == null) {
                            throw exception(LIVE_BROADCASTING_REVIEW_IMPORT_CUSTOMER_NOT_EXISTS, i + 1, importVO.getCustomerName());
                        }
                    }

                    // 转换货盘编号为货盘ID
                    Long liveBroadcastingId = null;
                    if (StrUtil.isNotBlank(importVO.getLiveBroadcastingNo())) {
                        liveBroadcastingId = liveBroadcastingIdMap.get(importVO.getLiveBroadcastingNo());
                        if (liveBroadcastingId == null) {
                            throw exception(LIVE_BROADCASTING_REVIEW_IMPORT_LIVE_BROADCASTING_NOT_EXISTS, i + 1, importVO.getLiveBroadcastingNo());
                        }
                    }

                    // 判断是否支持更新
                    ErpLiveBroadcastingReviewDO existReview = existMap.get(importVO.getNo());
                    if (existReview == null) {
                        // 创建 - 自动生成新的no编号
                        ErpLiveBroadcastingReviewDO review = BeanUtils.toBean(importVO, ErpLiveBroadcastingReviewDO.class);
                        review.setNo(noRedisDAO.generate(ErpNoRedisDAO.LIVE_BROADCASTING_REVIEW_NO_PREFIX));
                        review.setCustomerId(customerId);
                        review.setLiveBroadcastingId(liveBroadcastingId);
                        createList.add(review);
                        respVO.getCreateNames().add(review.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpLiveBroadcastingReviewDO updateReview = BeanUtils.toBean(importVO, ErpLiveBroadcastingReviewDO.class);
                        updateReview.setId(existReview.getId());
                        updateReview.setCustomerId(customerId);
                        updateReview.setLiveBroadcastingId(liveBroadcastingId);
                        updateList.add(updateReview);
                        respVO.getUpdateNames().add(updateReview.getNo());
                    } else {
                        throw exception(LIVE_BROADCASTING_REVIEW_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
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
                liveBroadcastingReviewMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(liveBroadcastingReviewMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }
}
