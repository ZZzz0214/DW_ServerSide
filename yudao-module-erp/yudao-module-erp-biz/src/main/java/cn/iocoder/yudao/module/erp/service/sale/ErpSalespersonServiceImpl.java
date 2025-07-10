package cn.iocoder.yudao.module.erp.service.sale;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.salesperson.ErpSalespersonSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalespersonDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSalespersonMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpSalespersonServiceImpl implements ErpSalespersonService {

    @Resource
    private ErpSalespersonMapper salespersonMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSalesperson(@Valid ErpSalespersonSaveReqVO createReqVO) {
        // 插入销售人员
        ErpSalespersonDO salesperson = BeanUtils.toBean(createReqVO, ErpSalespersonDO.class);
        salespersonMapper.insert(salesperson);
        return salesperson.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSalesperson(@Valid ErpSalespersonSaveReqVO updateReqVO) {
        // 校验存在
        validateSalespersonExists(updateReqVO.getId());
        // 更新销售人员
        ErpSalespersonDO updateObj = BeanUtils.toBean(updateReqVO, ErpSalespersonDO.class);
        salespersonMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSalesperson(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 校验存在
        List<ErpSalespersonDO> salespersons = salespersonMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(salespersons)) {
            throw exception(SALESPERSON_NOT_EXISTS);
        }
        // 删除销售人员
        salespersonMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpSalespersonDO getSalesperson(Long id) {
        return salespersonMapper.selectById(id);
    }

    @Override
    public List<ErpSalespersonDO> getSalespersonList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return salespersonMapper.selectBatchIds(ids);
    }

    @Override
    public ErpSalespersonDO validateSalesperson(Long id) {
        ErpSalespersonDO salesperson = salespersonMapper.selectById(id);
        if (salesperson == null) {
            throw exception(SALESPERSON_NOT_EXISTS);
        }
        return salesperson;
    }

    @Override
    public PageResult<ErpSalespersonRespVO> getSalespersonVOPage(ErpSalespersonPageReqVO pageReqVO) {
        return salespersonMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpSalespersonRespVO> getSalespersonVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpSalespersonDO> list = salespersonMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpSalespersonRespVO.class);
    }

    @Override
    public Map<Long, ErpSalespersonRespVO> getSalespersonVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getSalespersonVOList(ids), ErpSalespersonRespVO::getId);
    }

    @Override
    public List<ErpSalespersonRespVO> searchSalespersons(ErpSalespersonPageReqVO searchReqVO) {
        return salespersonMapper.selectPage(searchReqVO, new LambdaQueryWrapperX<ErpSalespersonDO>()
                .eqIfPresent(ErpSalespersonDO::getSalespersonName, searchReqVO.getSalespersonName())
                .likeIfPresent(ErpSalespersonDO::getContactPhone, searchReqVO.getContactPhone())
                .betweenIfPresent(ErpSalespersonDO::getCreateTime, searchReqVO.getCreateTime())
                .orderByDesc(ErpSalespersonDO::getId))
                .getList()
                .stream()
                .map(salesperson -> BeanUtils.toBean(salesperson, ErpSalespersonRespVO.class))
                .collect(Collectors.toList());
    }

    private void validateSalespersonExists(Long id) {
        if (id == null) {
            return;
        }
        ErpSalespersonDO salesperson = salespersonMapper.selectById(id);
        if (salesperson == null) {
            throw exception(SALESPERSON_NOT_EXISTS);
        }
    }
}
