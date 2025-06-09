package cn.iocoder.yudao.module.erp.service.transitsale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.transitsale.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpTransitSaleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.transitsale.ErpTransitSaleMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
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
public class ErpTransitSaleServiceImpl implements ErpTransitSaleService {

    @Resource
    private ErpTransitSaleMapper transitSaleMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpComboProductService erpComboProductService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransitSale(ErpTransitSaleSaveReqVO createReqVO) {
        // 1. 校验数据
        validateTransitSaleForCreateOrUpdate(null, createReqVO);

        // 2. 生成中转销售记录编号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.TRANSIT_SALE_NO_PREFIX);
        if (transitSaleMapper.selectByNo(no) != null) {
            throw exception(TRANSIT_SALE_NO_EXISTS);
        }

        // 3. 插入中转销售记录
        ErpTransitSaleDO transitSale = BeanUtils.toBean(createReqVO, ErpTransitSaleDO.class)
                .setNo(no);
        transitSaleMapper.insert(transitSale);

        return transitSale.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransitSale(ErpTransitSaleSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateTransitSale(updateReqVO.getId());
        // 1.2 校验数据
        validateTransitSaleForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新中转销售记录
        ErpTransitSaleDO updateObj = BeanUtils.toBean(updateReqVO, ErpTransitSaleDO.class);
        transitSaleMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransitSale(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 校验存在
        List<ErpTransitSaleDO> transitSales = transitSaleMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(transitSales)) {
            throw exception(TRANSIT_SALE_NOT_EXISTS);
        }
        // 2. 删除中转销售记录
        transitSaleMapper.deleteBatchIds(ids);
    }

    @Override
    public ErpTransitSaleRespVO getTransitSale(Long id) {
        ErpTransitSaleDO transitSale = transitSaleMapper.selectById(id);
        if (transitSale == null) {
            return null;
        }

        // 转换基础字段
        ErpTransitSaleRespVO respVO = BeanUtils.toBean(transitSale, ErpTransitSaleRespVO.class);

        // 获取组品信息并填充
        if (transitSale.getGroupProductId() != null) {
            ErpComboRespVO comboRespVO = erpComboProductService.getComboWithItems(transitSale.getGroupProductId());
            //ErpComboProductDO comboProduct = erpComboProductService.getCombo(transitSale.getGroupProductId());
            if (comboRespVO != null) {
                respVO.setComboList(Collections.singletonList(comboRespVO));
                respVO.setGroupProductId(comboRespVO.getId());
                respVO.setProductName(comboRespVO.getName());
                respVO.setProductShortName(comboRespVO.getShortName());
                respVO.setGroupProductNo(comboRespVO.getNo());
            }
        }

        return respVO;
    }

    @Override
    public ErpTransitSaleDO validateTransitSale(Long id) {
        ErpTransitSaleDO transitSale = transitSaleMapper.selectById(id);
        if (transitSale == null) {
            throw exception(TRANSIT_SALE_NOT_EXISTS);
        }
        return transitSale;
    }

    @Override
    public PageResult<ErpTransitSaleRespVO> getTransitSaleVOPage(ErpTransitSalePageReqVO pageReqVO) {
        return transitSaleMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpTransitSaleRespVO> getTransitSaleVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpTransitSaleDO> list = transitSaleMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpTransitSaleRespVO.class);
    }

    @Override
    public Map<Long, ErpTransitSaleRespVO> getTransitSaleVOMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(getTransitSaleVOList(ids), ErpTransitSaleRespVO::getId);
    }

    @Override
    public List<ErpTransitSaleDO> getTransitSaleList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return transitSaleMapper.selectBatchIds(ids);
    }

    private void validateTransitSaleForCreateOrUpdate(Long id, ErpTransitSaleSaveReqVO reqVO) {
        // 1. 校验编号唯一
        ErpTransitSaleDO transitSale = transitSaleMapper.selectByNo(reqVO.getNo());
        if (transitSale != null && !ObjectUtil.equal(transitSale.getId(), id)) {
            throw exception(TRANSIT_SALE_NO_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpTransitSaleImportRespVO importTransitSaleList(List<ErpTransitSaleImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(TRANSIT_SALE_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpTransitSaleImportRespVO respVO = ErpTransitSaleImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 查询已存在的中转销售记录
        Set<String> noSet = importList.stream()
                .map(ErpTransitSaleImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        List<ErpTransitSaleDO> existList = transitSaleMapper.selectListByNoIn(noSet);
        Map<String, ErpTransitSaleDO> noTransitSaleMap = convertMap(existList, ErpTransitSaleDO::getNo);

        // 遍历处理每个导入项
        for (int i = 0; i < importList.size(); i++) {
            ErpTransitSaleImportExcelVO importVO = importList.get(i);
            try {
                // 判断是否支持更新
                ErpTransitSaleDO existTransitSale = noTransitSaleMap.get(importVO.getNo());
                if (existTransitSale == null) {
                    // 创建
                    ErpTransitSaleDO transitSale = BeanUtils.toBean(importVO, ErpTransitSaleDO.class);
                    if (StrUtil.isEmpty(transitSale.getNo())) {
                        transitSale.setNo(noRedisDAO.generate(ErpNoRedisDAO.TRANSIT_SALE_NO_PREFIX));
                    }
                    transitSaleMapper.insert(transitSale);
                    respVO.getCreateNames().add(transitSale.getNo());
                } else if (isUpdateSupport) {
                    // 更新
                    ErpTransitSaleDO updateTransitSale = BeanUtils.toBean(importVO, ErpTransitSaleDO.class);
                    updateTransitSale.setId(existTransitSale.getId());
                    transitSaleMapper.updateById(updateTransitSale);
                    respVO.getUpdateNames().add(updateTransitSale.getNo());
                } else {
                    throw exception(TRANSIT_SALE_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
                }
            } catch (ServiceException ex) {
                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知中转销售";
                respVO.getFailureNames().put(errorKey, ex.getMessage());
            } catch (Exception ex) {
                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知中转销售";
                respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
            }
        }

        return respVO;
    }
}
