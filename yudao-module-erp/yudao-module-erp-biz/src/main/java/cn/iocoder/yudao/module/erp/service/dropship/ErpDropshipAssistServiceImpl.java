package cn.iocoder.yudao.module.erp.service.dropship;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.dropship.vo.ErpDropshipAssistPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.dropship.vo.ErpDropshipAssistRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.dropship.vo.ErpDropshipAssistSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.dropship.ErpDropshipAssistDO;
import cn.iocoder.yudao.module.erp.dal.mysql.dropship.ErpDropshipAssistMapper;
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
public class ErpDropshipAssistServiceImpl implements ErpDropshipAssistService {

    @Resource
    private ErpDropshipAssistMapper dropshipAssistMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDropshipAssist(ErpDropshipAssistSaveReqVO createReqVO) {
        // 1. 校验数据
        validateDropshipAssistForCreateOrUpdate(null, createReqVO);

        // 2. 生成代发辅助记录编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.DROPSHIP_ASSIST_NO_PREFIX);
        if (dropshipAssistMapper.selectByNo(no) != null) {
            throw exception(DROPSHIP_ASSIST_NO_EXISTS);
        }

        // 3. 插入代发辅助记录
        ErpDropshipAssistDO dropshipAssist = BeanUtils.toBean(createReqVO, ErpDropshipAssistDO.class)
                .setNo(no);
        dropshipAssistMapper.insert(dropshipAssist);

        return dropshipAssist.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDropshipAssist(ErpDropshipAssistSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateDropshipAssist(updateReqVO.getId());
        // 1.2 校验数据
        validateDropshipAssistForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新代发辅助记录
        ErpDropshipAssistDO updateObj = BeanUtils.toBean(updateReqVO, ErpDropshipAssistDO.class);
        dropshipAssistMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDropshipAssist(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpDropshipAssistDO> dropshipAssists = dropshipAssistMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(dropshipAssists)) {
            throw exception(DROPSHIP_ASSIST_NOT_EXISTS);
        }
        // 2. 删除代发辅助记录
        dropshipAssistMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpDropshipAssistDO getDropshipAssist(Long id) {
        return dropshipAssistMapper.selectById(id);
    }

    @Override
    public ErpDropshipAssistDO validateDropshipAssist(Long id) {
        ErpDropshipAssistDO dropshipAssist = dropshipAssistMapper.selectById(id);
        if (dropshipAssist == null) {
            throw exception(DROPSHIP_ASSIST_NOT_EXISTS);
        }
        return dropshipAssist;
    }

    @Override
    public PageResult<ErpDropshipAssistRespVO> getDropshipAssistVOPage(ErpDropshipAssistPageReqVO pageReqVO) {
        return dropshipAssistMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpDropshipAssistRespVO> getDropshipAssistVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpDropshipAssistDO> list = dropshipAssistMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpDropshipAssistRespVO.class);
    }

    @Override
    public Map<Long, ErpDropshipAssistRespVO> getDropshipAssistVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getDropshipAssistVOList(ids), ErpDropshipAssistRespVO::getId);
    }

    @Override
    public List<ErpDropshipAssistDO> getDropshipAssistList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return dropshipAssistMapper.selectBatchIds(ids);
    }

    private void validateDropshipAssistForCreateOrUpdate(Long id, ErpDropshipAssistSaveReqVO reqVO) {
        // 1. 校验编号唯一
        ErpDropshipAssistDO dropshipAssist = dropshipAssistMapper.selectByNo(reqVO.getNo());
        if (dropshipAssist != null && !ObjectUtil.equal(dropshipAssist.getId(), id)) {
            throw exception(DROPSHIP_ASSIST_NO_EXISTS);
        }
    }
}
