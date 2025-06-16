package cn.iocoder.yudao.module.erp.service.livebroadcasting;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcasting.vo.ErpLiveBroadcastingSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcasting.ErpLiveBroadcastingDO;
import cn.iocoder.yudao.module.erp.dal.mysql.livebroadcasting.ErpLiveBroadcastingMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpLiveBroadcastingServiceImpl implements ErpLiveBroadcastingService {

    @Resource
    private ErpLiveBroadcastingMapper liveBroadcastingMapper;
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLiveBroadcasting(ErpLiveBroadcastingSaveReqVO createReqVO) {
        // 1. 校验数据
        validateLiveBroadcastingForCreateOrUpdate(null, createReqVO);

        // 2. 生成直播货盘编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.LIVE_BROADCASTING_NO_PREFIX);
        if (liveBroadcastingMapper.selectByNo(no) != null) {
            throw exception(LIVE_BROADCASTING_NO_EXISTS);
        }

        // 3. 插入直播货盘记录
        ErpLiveBroadcastingDO liveBroadcasting = BeanUtils.toBean(createReqVO, ErpLiveBroadcastingDO.class)
                .setNo(no);
        liveBroadcastingMapper.insert(liveBroadcasting);

        return liveBroadcasting.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLiveBroadcasting(ErpLiveBroadcastingSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateLiveBroadcasting(updateReqVO.getId());
        // 1.2 校验数据
        validateLiveBroadcastingForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新直播货盘记录
        ErpLiveBroadcastingDO updateObj = BeanUtils.toBean(updateReqVO, ErpLiveBroadcastingDO.class);
        liveBroadcastingMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLiveBroadcasting(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpLiveBroadcastingDO> liveBroadcastings = liveBroadcastingMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(liveBroadcastings)) {
            throw exception(LIVE_BROADCASTING_NOT_EXISTS);
        }
        // 2. 删除直播货盘记录
        liveBroadcastingMapper.deleteBatchIds(ids);
    }

    @Override
    public PageResult<ErpLiveBroadcastingRespVO> getLiveBroadcastingVOPage(ErpLiveBroadcastingPageReqVO pageReqVO) {
        return liveBroadcastingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpLiveBroadcastingRespVO> getLiveBroadcastingVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpLiveBroadcastingDO> list = liveBroadcastingMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpLiveBroadcastingRespVO.class);
    }

    @Override
    public Map<Long, ErpLiveBroadcastingRespVO> getLiveBroadcastingVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingVOList(ids), ErpLiveBroadcastingRespVO::getId);
    }

    @Override
    public ErpLiveBroadcastingDO getLiveBroadcasting(Long id) {
        return liveBroadcastingMapper.selectById(id);
    }

    @Override
    public ErpLiveBroadcastingDO validateLiveBroadcasting(Long id) {
        ErpLiveBroadcastingDO liveBroadcasting = liveBroadcastingMapper.selectById(id);
        if (liveBroadcasting == null) {
            throw exception(LIVE_BROADCASTING_NOT_EXISTS);
        }
        return liveBroadcasting;
    }

    @Override
    public List<ErpLiveBroadcastingDO> getLiveBroadcastingList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return liveBroadcastingMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpLiveBroadcastingDO> getLiveBroadcastingMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingList(ids), ErpLiveBroadcastingDO::getId);
    }

    private void validateLiveBroadcastingForCreateOrUpdate(Long id, ErpLiveBroadcastingSaveReqVO reqVO) {
        // 1. 校验直播货盘编号唯一
        ErpLiveBroadcastingDO liveBroadcasting = liveBroadcastingMapper.selectByNo(reqVO.getNo());
        if (liveBroadcasting != null && !liveBroadcasting.getId().equals(id)) {
            throw exception(LIVE_BROADCASTING_NO_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpLiveBroadcastingImportRespVO importLiveBroadcastingList(List<ErpLiveBroadcastingImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(LIVE_BROADCASTING_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpLiveBroadcastingImportRespVO respVO = ErpLiveBroadcastingImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理数据
        List<ErpLiveBroadcastingDO> createList = new ArrayList<>();
        List<ErpLiveBroadcastingDO> updateList = new ArrayList<>();

        try {
            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpLiveBroadcastingImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpLiveBroadcastingDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(liveBroadcastingMapper.selectListByNoIn(noSet), ErpLiveBroadcastingDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();
            
            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpLiveBroadcastingImportExcelVO importVO = importList.get(i);
                try {
                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(LIVE_BROADCASTING_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }
                    
                    // 数据校验
                    validateImportData(importVO, i + 1);

                    // 判断是否支持更新
                    ErpLiveBroadcastingDO existLiveBroadcasting = existMap.get(importVO.getNo());
                    if (existLiveBroadcasting == null) {
                       // 创建 - 自动生成新的no编号
                       ErpLiveBroadcastingDO liveBroadcasting = BeanUtils.toBean(importVO, ErpLiveBroadcastingDO.class);
                       liveBroadcasting.setNo(noRedisDAO.generate(ErpNoRedisDAO.LIVE_BROADCASTING_NO_PREFIX));
                        createList.add(liveBroadcasting);
                        respVO.getCreateNames().add(liveBroadcasting.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpLiveBroadcastingDO updateLiveBroadcasting = BeanUtils.toBean(importVO, ErpLiveBroadcastingDO.class);
                        updateLiveBroadcasting.setId(existLiveBroadcasting.getId());
                        updateList.add(updateLiveBroadcasting);
                        respVO.getUpdateNames().add(updateLiveBroadcasting.getNo());
                    } else {
                        throw exception(LIVE_BROADCASTING_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
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
                liveBroadcastingMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(liveBroadcastingMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }

    /**
     * 校验导入数据
     */
    private void validateImportData(ErpLiveBroadcastingImportExcelVO importVO, int rowIndex) {
        // 1. 校验产品名称
        if (StrUtil.isBlank(importVO.getProductName())) {
            throw exception(LIVE_BROADCASTING_IMPORT_PRODUCT_NAME_EMPTY, rowIndex);
        }

        // 2. 校验市场价格
        if (importVO.getMarketPrice() == null || importVO.getMarketPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(LIVE_BROADCASTING_IMPORT_MARKET_PRICE_INVALID, rowIndex);
        }

        // 3. 校验直播价格
        if (importVO.getLivePrice() == null || importVO.getLivePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(LIVE_BROADCASTING_IMPORT_LIVE_PRICE_INVALID, rowIndex);
        }

        // 4. 校验直播佣金
        if (importVO.getLiveCommission() == null || importVO.getLiveCommission().compareTo(BigDecimal.ZERO) < 0) {
            throw exception(LIVE_BROADCASTING_IMPORT_LIVE_COMMISSION_INVALID, rowIndex);
        }

        // 5. 校验产品库存
        if (importVO.getProductStock() == null || importVO.getProductStock() < 0) {
            throw exception(LIVE_BROADCASTING_IMPORT_PRODUCT_STOCK_INVALID, rowIndex);
        }
    }
}