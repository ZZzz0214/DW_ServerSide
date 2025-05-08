package cn.iocoder.yudao.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboProductCreateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboSearchReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboProductItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.COMBO_PRODUCT_NOT_EXISTS;

@Service
@Validated
public class ErpComboProductServiceImpl implements ErpComboProductService {

    @Resource
    private ErpComboMapper erpComboMapper;

    @Resource
    private ErpComboProductItemMapper erpComboProductItemMapper;

    @Resource
    private ErpProductMapper erpProductMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    public Long createCombo(@Valid ErpComboSaveReqVO createReqVO) {
        // 生成组合产品编号
        String no = noRedisDAO.generate(ErpNoRedisDAO.COMBO_PRODUCT_NO_PREFIX);
        if (erpComboMapper.selectByNo(no) != null) {
            throw exception(COMBO_PRODUCT_NOT_EXISTS);
        }

        // 保存组品信息
        ErpComboProductDO comboProductDO = BeanUtils.toBean(createReqVO, ErpComboProductDO.class)
                .setNo(no);
        erpComboMapper.insert(comboProductDO);

        // 如果有单品关联信息，保存关联关系
//        if (createReqVO.getItems() != null) {
//            for (ErpComboSaveReqVO.ComboItem item : createReqVO.getItems()) {
//                ErpComboProductItemDO itemDO = new ErpComboProductItemDO();
//                itemDO.setComboProductId(comboProductDO.getId());
//                itemDO.setItemProductId(item.getProductId());
//                itemDO.setItemQuantity(item.getItemQuantity());
//                erpComboProductItemMapper.insert(itemDO);
//            }
//        }
        if (createReqVO.getItems() != null) {
            for (ErpProductRespVO item : createReqVO.getItems()) {
                ErpComboProductItemDO itemDO = new ErpComboProductItemDO();
                itemDO.setComboProductId(comboProductDO.getId());
                itemDO.setItemProductId(item.getId()); // 假设 ErpProductRespVO 中有 id 字段
                itemDO.setItemQuantity(item.getCount()); // 假设数量默认为 1，或者从其他字段获取
                erpComboProductItemMapper.insert(itemDO);
            }
        }
        return comboProductDO.getId();
    }

    @Override
    public void updateCombo(@Valid ErpComboSaveReqVO updateReqVO) {
        validateComboExists(updateReqVO.getId());
        ErpComboProductDO updateObj = BeanUtils.toBean(updateReqVO, ErpComboProductDO.class);
        erpComboMapper.updateById(updateObj);

        // 如果有单品关联信息，先删除旧的关联，再保存新的关联
        if (updateReqVO.getItems() != null) {
            // 删除旧的关联
            erpComboProductItemMapper.deleteByComboProductId(updateReqVO.getId());

//            // 插入新的关联
//            for (ErpComboSaveReqVO.ComboItem item : updateReqVO.getItems()) {
//                ErpComboProductItemDO itemDO = new ErpComboProductItemDO();
//                itemDO.setComboProductId(updateReqVO.getId());
//                itemDO.setItemProductId(item.getProductId());
//                itemDO.setItemQuantity(item.getQuantity());
//                erpComboProductItemMapper.insert(itemDO);
//            }
//        }
            // 插入新的关联
            for (ErpProductRespVO item : updateReqVO.getItems()) {
                ErpComboProductItemDO itemDO = new ErpComboProductItemDO();
                itemDO.setComboProductId(updateReqVO.getId());
                itemDO.setItemProductId(item.getId()); // 假设 ErpProductRespVO 中有 id 字段
                itemDO.setItemQuantity(item.getCount()); // 假设数量默认为 1，或者从其他字段获取
                erpComboProductItemMapper.insert(itemDO);
            }
        }
    }

    @Override
    public void deleteCombo(Long id) {
        validateComboExists(id);
        erpComboMapper.deleteById(id);

        // 删除关联的单品信息
        erpComboProductItemMapper.deleteByComboProductId(id);
    }

    @Override
    public List<ErpComboProductDO> validComboList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpComboMapper.selectBatchIds(ids);
    }

    private void validateComboExists(Long id) {
        if (erpComboMapper.selectById(id) == null) {
            throw exception(COMBO_PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    public ErpComboProductDO getCombo(Long id) {
        return erpComboMapper.selectById(id);
    }

    @Override
    public List<ErpComboRespVO> getComboVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpComboProductDO> list = erpComboMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpComboRespVO.class);
    }

//    @Override
//    public PageResult<ErpComboRespVO> getComboVOPage(ErpComboPageReqVO pageReqVO) {
//        PageResult<ErpComboProductDO> pageResult = erpComboMapper.selectPage(pageReqVO);
//        return new PageResult<>(BeanUtils.toBean(pageResult.getList(), ErpComboRespVO.class), pageResult.getTotal());
//    }
    @Override
    public PageResult<ErpComboRespVO> getComboVOPage(ErpComboPageReqVO pageReqVO) {
        PageResult<ErpComboProductDO> pageResult = erpComboMapper.selectPage(pageReqVO);

        // 转换结果并设置组合产品名称和重量
        List<ErpComboRespVO> voList = pageResult.getList().stream().map(combo -> {
            // 查询组合产品关联的单品
            List<ErpComboProductItemDO> comboItems = erpComboProductItemMapper.selectByComboProductId(combo.getId());
            List<Long> productIds = comboItems.stream()
                    .map(ErpComboProductItemDO::getItemProductId)
                    .collect(Collectors.toList());
            List<ErpProductDO> products = erpProductMapper.selectBatchIds(productIds);

            // 构建名称字符串
            StringBuilder nameBuilder = new StringBuilder();
            // 计算总重量
            BigDecimal totalWeight = BigDecimal.ZERO;
            for (int i = 0; i < products.size(); i++) {
                if (i > 0) {
                    nameBuilder.append("+");
                }
                nameBuilder.append(products.get(i).getName())
                        .append("*")
                        .append(comboItems.get(i).getItemQuantity());
                
                // 计算重量
                BigDecimal itemWeight = products.get(i).getWeight();
                if (itemWeight != null) {
                    BigDecimal quantity = new BigDecimal(comboItems.get(i).getItemQuantity());
                    totalWeight = totalWeight.add(itemWeight.multiply(quantity));
                }
            }

            // 转换VO并设置名称和重量
            ErpComboRespVO vo = BeanUtils.toBean(combo, ErpComboRespVO.class);
            vo.setName(nameBuilder.toString());
            vo.setWeight(totalWeight);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, pageResult.getTotal());
    }

    @Override
    public List<ErpComboRespVO> getComboProductVOListByStatus(Integer status) {
        List<ErpComboProductDO> comboProductList = erpComboMapper.selectListByStatus(status);
        return BeanUtils.toBean(comboProductList, ErpComboRespVO.class);
    }

    @Override
    public void createComboWithItems(ErpComboProductCreateReqVO createReqVO) {
        // 保存组品信息
        ErpComboProductDO comboProductDO = new ErpComboProductDO();
        comboProductDO.setName(createReqVO.getName());
        erpComboMapper.insert(comboProductDO);

        // 保存组品和单品的关联关系
        for (ErpComboProductCreateReqVO.ComboItem item : createReqVO.getItems()) {
            ErpComboProductItemDO itemDO = new ErpComboProductItemDO();
            itemDO.setComboProductId(comboProductDO.getId());
            itemDO.setItemProductId(item.getProductId());
            itemDO.setItemQuantity(item.getQuantity());
            erpComboProductItemMapper.insert(itemDO);
        }
    }

    @Override
    public ErpComboRespVO getComboWithItems(Long id) {
        // 查询组品基本信息
        ErpComboProductDO comboProduct = erpComboMapper.selectById(id);
        if (comboProduct == null) {
            return null;
        }

        // 查询组品关联的单品项
        List<ErpComboProductItemDO> comboItems = erpComboProductItemMapper.selectByComboProductId(id);

        // 提取单品ID列表
        List<Long> productIds = comboItems.stream()
                .map(ErpComboProductItemDO::getItemProductId)
                .collect(Collectors.toList());

        // 查询单品详细信息
        List<ErpProductDO> products = erpProductMapper.selectBatchIds(productIds);

        // 组装单品名称字符串 (单品A*数量+单品B*数量)
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) {
                nameBuilder.append("+");
            }
            nameBuilder.append(products.get(i).getName())
                      .append("*")
                      .append(comboItems.get(i).getItemQuantity());
        }
        // 计算总重量 (单品weight*数量)
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (int i = 0; i < products.size(); i++) {
            BigDecimal itemWeight = products.get(i).getWeight();
            if (itemWeight != null) {
                BigDecimal quantity = new BigDecimal(comboItems.get(i).getItemQuantity());
                totalWeight = totalWeight.add(itemWeight.multiply(quantity));
            }
        }
        // 组装响应对象
        ErpComboRespVO comboRespVO = BeanUtils.toBean(comboProduct, ErpComboRespVO.class);
        comboRespVO.setName(nameBuilder.toString());
        comboRespVO.setWeight(totalWeight);

        // 组装单品列表
        List<ErpProductRespVO> productVOs = products.stream()
                .map(product -> BeanUtils.toBean(product, ErpProductRespVO.class))
                .collect(Collectors.toList());

        // 将 itemQuantity 赋值给 count
        for (int i = 0; i < productVOs.size(); i++) {
            productVOs.get(i).setCount(comboItems.get(i).getItemQuantity());
        }
        comboRespVO.setItems(productVOs);
        return comboRespVO;
    }




    @Override
    public List<ErpComboRespVO> searchCombos(ErpComboSearchReqVO searchReqVO) {
        // 构造查询条件
        ErpComboProductDO comboProductDO = new ErpComboProductDO();
        if (searchReqVO.getId() != null) {
            comboProductDO.setId(searchReqVO.getId());
        }
        if (searchReqVO.getName() != null) {
            comboProductDO.setName(searchReqVO.getName());
        }
        if (searchReqVO.getCreateTime() != null) {
            comboProductDO.setCreateTime(searchReqVO.getCreateTime());
        }

        // 执行查询
        List<ErpComboProductDO> comboProductDOList = erpComboMapper.selectList(new LambdaQueryWrapper<ErpComboProductDO>()
                .eq(comboProductDO.getId() != null, ErpComboProductDO::getId, comboProductDO.getId())
                .like(comboProductDO.getName() != null, ErpComboProductDO::getName, comboProductDO.getName())
                .eq(comboProductDO.getCreateTime() != null, ErpComboProductDO::getCreateTime, comboProductDO.getCreateTime()));

        // 转换为响应对象
        return BeanUtils.toBean(comboProductDOList, ErpComboRespVO.class);
    }
}
