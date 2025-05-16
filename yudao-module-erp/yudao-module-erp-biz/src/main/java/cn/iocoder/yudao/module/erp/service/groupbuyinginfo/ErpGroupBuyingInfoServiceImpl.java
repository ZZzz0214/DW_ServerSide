package cn.iocoder.yudao.module.erp.service.groupbuyinginfo;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo.ErpGroupBuyingInfoSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuyinginfo.ErpGroupBuyingInfoDO;
import cn.iocoder.yudao.module.erp.dal.mysql.groupbuyinginfo.ErpGroupBuyingInfoMapper;
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
public class ErpGroupBuyingInfoServiceImpl implements ErpGroupBuyingInfoService {

    @Resource
    private ErpGroupBuyingInfoMapper groupBuyingInfoMapper;
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroupBuyingInfo(ErpGroupBuyingInfoSaveReqVO createReqVO) {
        // 1. 校验数据
        validateGroupBuyingInfoForCreateOrUpdate(null, createReqVO);

        // 2. 生成团购信息编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.GROUP_BUYING_INFO_NO_PREFIX);
        if (groupBuyingInfoMapper.selectByNo(no) != null) {
            throw exception(GROUP_BUYING_INFO_NO_EXISTS);
        }

        // 3. 插入团购信息记录
        ErpGroupBuyingInfoDO groupBuyingInfo = BeanUtils.toBean(createReqVO, ErpGroupBuyingInfoDO.class)
                .setNo(no);
        groupBuyingInfoMapper.insert(groupBuyingInfo);

        return groupBuyingInfo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupBuyingInfo(ErpGroupBuyingInfoSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateGroupBuyingInfo(updateReqVO.getId());
        // 1.2 校验数据
        validateGroupBuyingInfoForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新团购信息记录
        ErpGroupBuyingInfoDO updateObj = BeanUtils.toBean(updateReqVO, ErpGroupBuyingInfoDO.class);
        groupBuyingInfoMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroupBuyingInfo(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpGroupBuyingInfoDO> groupBuyingInfos = groupBuyingInfoMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(groupBuyingInfos)) {
            throw exception(GROUP_BUYING_INFO_NOT_EXISTS);
        }
        // 2. 删除团购信息记录
        groupBuyingInfoMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpGroupBuyingInfoDO getGroupBuyingInfo(Long id) {
        return groupBuyingInfoMapper.selectById(id);
    }

    @Override
    public ErpGroupBuyingInfoDO validateGroupBuyingInfo(Long id) {
        ErpGroupBuyingInfoDO groupBuyingInfo = groupBuyingInfoMapper.selectById(id);
        if (groupBuyingInfo == null) {
            throw exception(GROUP_BUYING_INFO_NOT_EXISTS);
        }
        return groupBuyingInfo;
    }

    @Override
    public PageResult<ErpGroupBuyingInfoRespVO> getGroupBuyingInfoVOPage(ErpGroupBuyingInfoPageReqVO pageReqVO) {
        return groupBuyingInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpGroupBuyingInfoRespVO> getGroupBuyingInfoVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpGroupBuyingInfoDO> list = groupBuyingInfoMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpGroupBuyingInfoRespVO.class);
    }

    @Override
    public Map<Long, ErpGroupBuyingInfoRespVO> getGroupBuyingInfoVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingInfoVOList(ids), ErpGroupBuyingInfoRespVO::getId);
    }

    @Override
    public List<ErpGroupBuyingInfoDO> getGroupBuyingInfoList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return groupBuyingInfoMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpGroupBuyingInfoDO> getGroupBuyingInfoMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getGroupBuyingInfoList(ids), ErpGroupBuyingInfoDO::getId);
    }

    private void validateGroupBuyingInfoForCreateOrUpdate(Long id, ErpGroupBuyingInfoSaveReqVO reqVO) {
        // 1. 校验团购信息编号唯一
        ErpGroupBuyingInfoDO groupBuyingInfo = groupBuyingInfoMapper.selectByNo(reqVO.getNo());
        if (groupBuyingInfo != null && !groupBuyingInfo.getId().equals(id)) {
            throw exception(GROUP_BUYING_INFO_NO_EXISTS);
        }
    }
}