package cn.iocoder.yudao.module.erp.service.privatebroadcasting;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    private void validatePrivateBroadcastingForCreateOrUpdate(Long id, ErpPrivateBroadcastingSaveReqVO reqVO) {
        // 1. 校验私播货盘编号唯一
        ErpPrivateBroadcastingDO privateBroadcasting = privateBroadcastingMapper.selectByNo(reqVO.getNo());
        if (privateBroadcasting != null && !privateBroadcasting.getId().equals(id)) {
            throw exception(PRIVATE_BROADCASTING_NO_EXISTS);
        }
    }
}