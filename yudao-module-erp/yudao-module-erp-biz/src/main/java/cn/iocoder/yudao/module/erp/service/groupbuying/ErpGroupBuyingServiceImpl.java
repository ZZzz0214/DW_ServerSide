package cn.iocoder.yudao.module.erp.service.groupbuying;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
}