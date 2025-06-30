package cn.iocoder.yudao.module.erp.service.livebroadcastinginfo;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo.ErpLiveBroadcastingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastinginfo.ErpLiveBroadcastingInfoDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.livebroadcastinginfo.ErpLiveBroadcastingInfoMapper;
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
public class ErpLiveBroadcastingInfoServiceImpl implements ErpLiveBroadcastingInfoService {

    @Resource
    private ErpLiveBroadcastingInfoMapper liveBroadcastingInfoMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpCustomerMapper customerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLiveBroadcastingInfo(ErpLiveBroadcastingInfoSaveReqVO createReqVO) {
        // 1. 校验数据
        validateLiveBroadcastingInfoForCreateOrUpdate(null, createReqVO);

        // 2. 生成直播信息编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.LIVE_BROADCASTING_INFO_NO_PREFIX);
        if (liveBroadcastingInfoMapper.selectByNo(no) != null) {
            throw exception(LIVE_BROADCASTING_INFO_NO_EXISTS);
        }

        // 3. 插入直播信息记录
        ErpLiveBroadcastingInfoDO liveBroadcastingInfo = BeanUtils.toBean(createReqVO, ErpLiveBroadcastingInfoDO.class)
                .setNo(no);
        liveBroadcastingInfoMapper.insert(liveBroadcastingInfo);

        return liveBroadcastingInfo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLiveBroadcastingInfo(ErpLiveBroadcastingInfoSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateLiveBroadcastingInfo(updateReqVO.getId());
        // 1.2 校验数据
        validateLiveBroadcastingInfoForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新直播信息记录
        ErpLiveBroadcastingInfoDO updateObj = BeanUtils.toBean(updateReqVO, ErpLiveBroadcastingInfoDO.class);
        liveBroadcastingInfoMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLiveBroadcastingInfo(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpLiveBroadcastingInfoDO> liveBroadcastingInfos = liveBroadcastingInfoMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(liveBroadcastingInfos)) {
            throw exception(LIVE_BROADCASTING_INFO_NOT_EXISTS);
        }
        // 2. 删除直播信息记录
        liveBroadcastingInfoMapper.deleteBatchIds(ids);
    }

    @Override
    public PageResult<ErpLiveBroadcastingInfoRespVO> getLiveBroadcastingInfoVOPage(ErpLiveBroadcastingInfoPageReqVO pageReqVO) {
        return liveBroadcastingInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpLiveBroadcastingInfoRespVO> getLiveBroadcastingInfoVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return liveBroadcastingInfoMapper.selectListByIds(ids);
    }

    @Override
    public Map<Long, ErpLiveBroadcastingInfoRespVO> getLiveBroadcastingInfoVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingInfoVOList(ids), ErpLiveBroadcastingInfoRespVO::getId);
    }

    @Override
    public ErpLiveBroadcastingInfoDO getLiveBroadcastingInfo(Long id) {
        return liveBroadcastingInfoMapper.selectById(id);
    }

    @Override
    public ErpLiveBroadcastingInfoDO validateLiveBroadcastingInfo(Long id) {
        ErpLiveBroadcastingInfoDO liveBroadcastingInfo = liveBroadcastingInfoMapper.selectById(id);
        if (liveBroadcastingInfo == null) {
            throw exception(LIVE_BROADCASTING_INFO_NOT_EXISTS);
        }
        return liveBroadcastingInfo;
    }

    @Override
    public List<ErpLiveBroadcastingInfoDO> getLiveBroadcastingInfoList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return liveBroadcastingInfoMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpLiveBroadcastingInfoDO> getLiveBroadcastingInfoMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getLiveBroadcastingInfoList(ids), ErpLiveBroadcastingInfoDO::getId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpLiveBroadcastingInfoImportRespVO importLiveBroadcastingInfoList(List<ErpLiveBroadcastingInfoImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(LIVE_BROADCASTING_INFO_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpLiveBroadcastingInfoImportRespVO respVO = ErpLiveBroadcastingInfoImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        try {
            // 1. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateAllImportData(importList, isUpdateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 2. 批量处理数据
            List<ErpLiveBroadcastingInfoDO> createList = new ArrayList<>();
            List<ErpLiveBroadcastingInfoDO> updateList = new ArrayList<>();

                    // 3. 客户名称直接使用，不再查询客户表

            // 4. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpLiveBroadcastingInfoImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpLiveBroadcastingInfoDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(liveBroadcastingInfoMapper.selectListByNoIn(noSet), ErpLiveBroadcastingInfoDO::getNo);

            // 5. 批量转换和保存数据
            for (int i = 0; i < importList.size(); i++) {
                ErpLiveBroadcastingInfoImportExcelVO importVO = importList.get(i);

                // 客户名称直接使用，不再需要客户ID

                // 判断是否支持更新
                ErpLiveBroadcastingInfoDO existLiveBroadcastingInfo = existMap.get(importVO.getNo());
                if (existLiveBroadcastingInfo == null) {
                    // 创建 - 自动生成新的no编号
                    ErpLiveBroadcastingInfoDO liveBroadcastingInfo = BeanUtils.toBean(importVO, ErpLiveBroadcastingInfoDO.class);
                    liveBroadcastingInfo.setNo(noRedisDAO.generate(ErpNoRedisDAO.LIVE_BROADCASTING_INFO_NO_PREFIX));
                    createList.add(liveBroadcastingInfo);
                    respVO.getCreateNames().add(liveBroadcastingInfo.getNo());
                } else if (isUpdateSupport) {
                    // 更新
                    ErpLiveBroadcastingInfoDO updateLiveBroadcastingInfo = BeanUtils.toBean(importVO, ErpLiveBroadcastingInfoDO.class);
                    updateLiveBroadcastingInfo.setId(existLiveBroadcastingInfo.getId());
                    updateList.add(updateLiveBroadcastingInfo);
                    respVO.getUpdateNames().add(updateLiveBroadcastingInfo.getNo());
                }
            }

            // 6. 批量保存到数据库
            if (CollUtil.isNotEmpty(createList)) {
                createList.forEach(liveBroadcastingInfoMapper::insert);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(liveBroadcastingInfoMapper::updateById);
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
    private Map<String, String> validateAllImportData(List<ErpLiveBroadcastingInfoImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 客户名称直接使用，不再查询客户表

        // 3. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpLiveBroadcastingInfoImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpLiveBroadcastingInfoDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(liveBroadcastingInfoMapper.selectListByNoIn(noSet), ErpLiveBroadcastingInfoDO::getNo);

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();

        // 4. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpLiveBroadcastingInfoImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getCustomerName()) ? "(" + importVO.getCustomerName() + ")" : "");

            try {

                // 4.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "直播信息编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 4.3 客户名称直接使用，不再校验

                // 4.4 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpLiveBroadcastingInfoDO liveBroadcastingInfo = BeanUtils.toBean(importVO, ErpLiveBroadcastingInfoDO.class);
                    if (liveBroadcastingInfo == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 4.5 判断是新增还是更新，并进行相应校验
                ErpLiveBroadcastingInfoDO existLiveBroadcastingInfo = existMap.get(importVO.getNo());
                if (existLiveBroadcastingInfo == null) {
                    // 新增校验：无需额外校验
                } else if (isUpdateSupport) {
                    // 更新校验：无需额外校验
                } else {
                    allErrors.put(errorKey, "直播信息编号不存在且不支持更新: " + importVO.getNo());
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
    private Map<String, String> validateDataTypeErrors(List<ErpLiveBroadcastingInfoImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取客户名称 - 修复行号索引问题
                String customerName = "未知客户";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpLiveBroadcastingInfoImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getCustomerName())) {
                        customerName = importVO.getCustomerName();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + customerName + ")";
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

    private void validateLiveBroadcastingInfoForCreateOrUpdate(Long id, ErpLiveBroadcastingInfoSaveReqVO reqVO) {
        // 1. 校验直播信息编号唯一
        ErpLiveBroadcastingInfoDO liveBroadcastingInfo = liveBroadcastingInfoMapper.selectByNo(reqVO.getNo());
        if (liveBroadcastingInfo != null && !liveBroadcastingInfo.getId().equals(id)) {
            throw exception(LIVE_BROADCASTING_INFO_NO_EXISTS);
        }
    }
}
