package cn.iocoder.yudao.module.erp.service.privatebroadcasting;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingImportExcelVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingImportRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.privatebroadcasting.vo.ErpPrivateBroadcastingSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcasting.ErpPrivateBroadcastingDO;
import cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcasting.ErpPrivateBroadcastingMapper;
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
public class ErpPrivateBroadcastingServiceImpl implements ErpPrivateBroadcastingService {

    @Resource
    private ErpPrivateBroadcastingMapper privateBroadcastingMapper;
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPrivateBroadcasting(ErpPrivateBroadcastingSaveReqVO createReqVO) {
        // 1. 校验数据
        validatePrivateBroadcastingForCreateOrUpdate(null, createReqVO);

        // 2. 生成私播货盘编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.PRIVATE_BROADCASTING_NO_PREFIX);
        if (privateBroadcastingMapper.selectByNo(no) != null) {
            throw exception(PRIVATE_BROADCASTING_NO_EXISTS);
        }

        // 3. 插入私播货盘记录
        ErpPrivateBroadcastingDO privateBroadcasting = BeanUtils.toBean(createReqVO, ErpPrivateBroadcastingDO.class)
                .setNo(no);
        privateBroadcastingMapper.insert(privateBroadcasting);

        return privateBroadcasting.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivateBroadcasting(ErpPrivateBroadcastingSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validatePrivateBroadcasting(updateReqVO.getId());
        // 1.2 校验数据
        validatePrivateBroadcastingForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新私播货盘记录
        ErpPrivateBroadcastingDO updateObj = BeanUtils.toBean(updateReqVO, ErpPrivateBroadcastingDO.class);
        privateBroadcastingMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePrivateBroadcasting(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpPrivateBroadcastingDO> privateBroadcastings = privateBroadcastingMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(privateBroadcastings)) {
            throw exception(PRIVATE_BROADCASTING_NOT_EXISTS);
        }
        // 2. 删除私播货盘记录
        privateBroadcastingMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpPrivateBroadcastingDO getPrivateBroadcasting(Long id) {
        return privateBroadcastingMapper.selectById(id);
    }

    @Override
    public ErpPrivateBroadcastingDO validatePrivateBroadcasting(Long id) {
        ErpPrivateBroadcastingDO privateBroadcasting = privateBroadcastingMapper.selectById(id);
        if (privateBroadcasting == null) {
            throw exception(PRIVATE_BROADCASTING_NOT_EXISTS);
        }
        return privateBroadcasting;
    }

    @Override
    public PageResult<ErpPrivateBroadcastingRespVO> getPrivateBroadcastingVOPage(ErpPrivateBroadcastingPageReqVO pageReqVO) {
        return privateBroadcastingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpPrivateBroadcastingRespVO> getPrivateBroadcastingVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpPrivateBroadcastingDO> list = privateBroadcastingMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpPrivateBroadcastingRespVO.class);
    }

    @Override
    public Map<Long, ErpPrivateBroadcastingRespVO> getPrivateBroadcastingVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPrivateBroadcastingVOList(ids), ErpPrivateBroadcastingRespVO::getId);
    }

    @Override
    public List<ErpPrivateBroadcastingDO> getPrivateBroadcastingList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return privateBroadcastingMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpPrivateBroadcastingDO> getPrivateBroadcastingMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getPrivateBroadcastingList(ids), ErpPrivateBroadcastingDO::getId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpPrivateBroadcastingImportRespVO importPrivateBroadcastingList(List<ErpPrivateBroadcastingImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(PRIVATE_BROADCASTING_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpPrivateBroadcastingImportRespVO respVO = ErpPrivateBroadcastingImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 批量处理数据
        List<ErpPrivateBroadcastingDO> createList = new ArrayList<>();
        List<ErpPrivateBroadcastingDO> updateList = new ArrayList<>();

        try {
            // 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpPrivateBroadcastingImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpPrivateBroadcastingDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(privateBroadcastingMapper.selectListByNoIn(noSet), ErpPrivateBroadcastingDO::getNo);

            // 用于跟踪Excel内部重复的编号
            Set<String> processedNos = new HashSet<>();
            
            // 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpPrivateBroadcastingImportExcelVO importVO = importList.get(i);
                try {
                    // 检查Excel内部编号重复
                    if (StrUtil.isNotBlank(importVO.getNo())) {
                        if (processedNos.contains(importVO.getNo())) {
                            throw exception(PRIVATE_BROADCASTING_IMPORT_NO_DUPLICATE, i + 1, importVO.getNo());
                        }
                        processedNos.add(importVO.getNo());
                    }

                    // 判断是否支持更新
                    ErpPrivateBroadcastingDO existPrivateBroadcasting = existMap.get(importVO.getNo());
                    if (existPrivateBroadcasting == null) {
                        // 创建 - 自动生成新的no编号
                        ErpPrivateBroadcastingDO privateBroadcasting = BeanUtils.toBean(importVO, ErpPrivateBroadcastingDO.class);
                        privateBroadcasting.setNo(noRedisDAO.generate(ErpNoRedisDAO.PRIVATE_BROADCASTING_NO_PREFIX));
                        // 设置默认状态
                        if (StrUtil.isBlank(privateBroadcasting.getPrivateStatus())) {
                            privateBroadcasting.setPrivateStatus("未设置");
                        }
                        createList.add(privateBroadcasting);
                        respVO.getCreateNames().add(privateBroadcasting.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpPrivateBroadcastingDO updatePrivateBroadcasting = BeanUtils.toBean(importVO, ErpPrivateBroadcastingDO.class);
                        updatePrivateBroadcasting.setId(existPrivateBroadcasting.getId());
                        // 如果状态为空，保持原状态
                        if (StrUtil.isBlank(updatePrivateBroadcasting.getPrivateStatus())) {
                            updatePrivateBroadcasting.setPrivateStatus(existPrivateBroadcasting.getPrivateStatus());
                        }
                        updateList.add(updatePrivateBroadcasting);
                        respVO.getUpdateNames().add(updatePrivateBroadcasting.getNo());
                    } else {
                        throw exception(PRIVATE_BROADCASTING_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
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
                createList.forEach(privateBroadcastingMapper::insert);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                updateList.forEach(privateBroadcastingMapper::updateById);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        }

        return respVO;
    }

    private void validatePrivateBroadcastingForCreateOrUpdate(Long id, ErpPrivateBroadcastingSaveReqVO reqVO) {
        // 1. 校验私播货盘编号唯一
        ErpPrivateBroadcastingDO privateBroadcasting = privateBroadcastingMapper.selectByNo(reqVO.getNo());
        if (privateBroadcasting != null && !ObjectUtil.equal(privateBroadcasting.getId(), id)) {
            throw exception(PRIVATE_BROADCASTING_NO_EXISTS);
        }
    }
}