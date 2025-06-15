package cn.iocoder.yudao.module.erp.service.groupbuying;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuying.vo.ErpGroupBuyingSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingDO;
import cn.iocoder.yudao.module.erp.dal.mysql.groupbuying.ErpGroupBuyingMapper;
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
public class ErpGroupBuyingServiceImpl implements ErpGroupBuyingService {

    @Resource
    private ErpGroupBuyingMapper groupBuyingMapper;
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroupBuying(ErpGroupBuyingSaveReqVO createReqVO) {
        // 1. 校验数据
        validateGroupBuyingForCreateOrUpdate(null, createReqVO);

        // 2. 生成团购货盘编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.GROUP_BUYING_NO_PREFIX);
        if (groupBuyingMapper.selectByNo(no) != null) {
            throw exception(GROUP_BUYING_NO_EXISTS);
        }

        // 3. 插入团购货盘记录
        ErpGroupBuyingDO groupBuying = BeanUtils.toBean(createReqVO, ErpGroupBuyingDO.class)
                .setNo(no);
        groupBuyingMapper.insert(groupBuying);

        return groupBuying.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupBuying(ErpGroupBuyingSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateGroupBuying(updateReqVO.getId());
        // 1.2 校验数据
        validateGroupBuyingForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新团购货盘记录
        ErpGroupBuyingDO updateObj = BeanUtils.toBean(updateReqVO, ErpGroupBuyingDO.class);
        groupBuyingMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroupBuying(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpGroupBuyingDO> groupBuyings = groupBuyingMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(groupBuyings)) {
            throw exception(GROUP_BUYING_NOT_EXISTS);
        }
        // 2. 删除团购货盘记录
        groupBuyingMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpGroupBuyingDO getGroupBuying(Long id) {
        return groupBuyingMapper.selectById(id);
    }

    @Override
    public ErpGroupBuyingDO validateGroupBuying(Long id) {
        ErpGroupBuyingDO groupBuying = groupBuyingMapper.selectById(id);
        if (groupBuying == null) {
            throw exception(GROUP_BUYING_NOT_EXISTS);
        }
        return groupBuying;
    }

    @Override
    public PageResult<ErpGroupBuyingRespVO> getGroupBuyingVOPage(ErpGroupBuyingPageReqVO pageReqVO) {
        return groupBuyingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpGroupBuyingRespVO> getGroupBuyingVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpGroupBuyingDO> list = groupBuyingMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpGroupBuyingRespVO.class);
    }

    @Override
    public Map<Long, ErpGroupBuyingRespVO> getGroupBuyingVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingVOList(ids), ErpGroupBuyingRespVO::getId);
    }

    @Override
    public List<ErpGroupBuyingDO> getGroupBuyingList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return groupBuyingMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpGroupBuyingDO> getGroupBuyingMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingList(ids), ErpGroupBuyingDO::getId);
    }

    private void validateGroupBuyingForCreateOrUpdate(Long id, ErpGroupBuyingSaveReqVO reqVO) {
        // 1. 校验团购货盘编号唯一
        ErpGroupBuyingDO groupBuying = groupBuyingMapper.selectByNo(reqVO.getNo());
        if (groupBuying != null && !groupBuying.getId().equals(id)) {
            throw exception(GROUP_BUYING_NO_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpGroupBuyingImportRespVO importGroupBuyingList(List<ErpGroupBuyingImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(GROUP_BUYING_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpGroupBuyingImportRespVO respVO = ErpGroupBuyingImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理
        List<ErpGroupBuyingDO> createList = new ArrayList<>();
        List<ErpGroupBuyingDO> updateList = new ArrayList<>();

        try {
            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpGroupBuyingImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpGroupBuyingDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(groupBuyingMapper.selectListByNoIn(noSet), ErpGroupBuyingDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();
            
            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpGroupBuyingImportExcelVO importVO = importList.get(i);
                try {
                    // 校验必填字段
                    if (StrUtil.isBlank(importVO.getProductName())) {
                        throw exception(GROUP_BUYING_IMPORT_PRODUCT_NAME_EMPTY, i + 1);
                    }

                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(GROUP_BUYING_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 判断是否支持更新
                    ErpGroupBuyingDO existGroupBuying = existMap.get(importVO.getNo());
                    if (existGroupBuying == null) {
                        // 创建 - 自动生成新的no编号
                        ErpGroupBuyingDO groupBuying = BeanUtils.toBean(importVO, ErpGroupBuyingDO.class);
                        groupBuying.setNo(noRedisDAO.generate(ErpNoRedisDAO.GROUP_BUYING_NO_PREFIX));
                        createList.add(groupBuying);
                        respVO.getCreateNames().add(groupBuying.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpGroupBuyingDO updateGroupBuying = BeanUtils.toBean(importVO, ErpGroupBuyingDO.class);
                        updateGroupBuying.setId(existGroupBuying.getId());
                        updateList.add(updateGroupBuying);
                        respVO.getUpdateNames().add(updateGroupBuying.getNo());
                    } else {
                        throw exception(GROUP_BUYING_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
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
                groupBuyingMapper.insertBatch(createList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(groupBuyingMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }
}