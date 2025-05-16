package cn.iocoder.yudao.module.erp.service.sample;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSamplePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sample.vo.ErpSampleSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sample.ErpSampleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sample.ErpSampleMapper;
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
public class ErpSampleServiceImpl implements ErpSampleService {

    @Resource
    private ErpSampleMapper sampleMapper;
    
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSample(ErpSampleSaveReqVO createReqVO) {
        // 1. 校验数据
        validateSampleForCreateOrUpdate(null, createReqVO);

        // 2. 生成样品编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.SAMPLE_NO_PREFIX);
        if (sampleMapper.selectByNo(no) != null) {
            throw exception(SAMPLE_NO_EXISTS);
        }

        // 3. 插入样品记录
        ErpSampleDO sample = BeanUtils.toBean(createReqVO, ErpSampleDO.class)
                .setNo(no);
        sampleMapper.insert(sample);

        return sample.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSample(ErpSampleSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateSample(updateReqVO.getId());
        // 1.2 校验数据
        validateSampleForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新样品记录
        ErpSampleDO updateObj = BeanUtils.toBean(updateReqVO, ErpSampleDO.class);
        sampleMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSample(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpSampleDO> samples = sampleMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(samples)) {
            throw exception(SAMPLE_NOT_EXISTS);
        }
        // 2. 删除样品记录
        sampleMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpSampleDO getSample(Long id) {
        return sampleMapper.selectById(id);
    }

    @Override
    public ErpSampleDO validateSample(Long id) {
        ErpSampleDO sample = sampleMapper.selectById(id);
        if (sample == null) {
            throw exception(SAMPLE_NOT_EXISTS);
        }
        return sample;
    }

    @Override
    public PageResult<ErpSampleRespVO> getSampleVOPage(ErpSamplePageReqVO pageReqVO) {
        return sampleMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpSampleRespVO> getSampleVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpSampleDO> list = sampleMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpSampleRespVO.class);
    }

    @Override
    public Map<Long, ErpSampleRespVO> getSampleVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getSampleVOList(ids), ErpSampleRespVO::getId);
    }

    @Override
    public List<ErpSampleDO> getSampleList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return sampleMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, ErpSampleDO> getSampleMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getSampleList(ids), ErpSampleDO::getId);
    }

    private void validateSampleForCreateOrUpdate(Long id, ErpSampleSaveReqVO reqVO) {
        // 1. 校验样品编号唯一
        ErpSampleDO sample = sampleMapper.selectByNo(reqVO.getNo());
        if (sample != null && !sample.getId().equals(id)) {
            throw exception(SAMPLE_NO_EXISTS);
        }
    }
}