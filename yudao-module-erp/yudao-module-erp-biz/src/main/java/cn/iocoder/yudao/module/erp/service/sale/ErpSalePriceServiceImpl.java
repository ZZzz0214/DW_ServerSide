package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpComboRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.saleprice.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalePriceESDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSalePriceMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ErpSalePriceServiceImpl implements ErpSalePriceService {

    @Resource
    private ErpSalePriceMapper erpSalePriceMapper;

    @Resource
    private ErpComboProductService erpComboProductService;

    @Resource
    private ErpComboMapper erpComboMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;


    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private ErpSalePriceESRepository salePriceESRepository;

//    // ES索引初始化
//    @EventListener(ApplicationReadyEvent.class)
//    public void initESIndex() {
//        System.out.println("开始初始化销售价格ES索引...");
//        try {
//            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpSalePriceESDO.class);
//            if (!indexOps.exists()) {
//                indexOps.create();
//                indexOps.putMapping(indexOps.createMapping(ErpSalePriceESDO.class));
//                System.out.println("销售价格索引创建成功");
//            }
//        } catch (Exception e) {
//            System.err.println("销售价格索引初始化失败: " + e.getMessage());
//        }
//    }
//
//    // 同步数据到ES
//    private void syncToES(Long id) {
//        ErpSalePriceDO salePrice = erpSalePriceMapper.selectById(id);
//        if (salePrice == null) {
//            salePriceESRepository.deleteById(id);
//        } else {
//            ErpSalePriceESDO es = convertToES(salePrice);
//            salePriceESRepository.save(es);
//        }
//    }
//
//    // 转换方法
//    private ErpSalePriceESDO convertToES(ErpSalePriceDO salePrice) {
//        ErpSalePriceESDO es = new ErpSalePriceESDO();
//        BeanUtils.copyProperties(salePrice, es);
//        return es;
//    }
//
//    // 全量同步方法
//    @Async
//    public void fullSyncToES() {
//        try {
//            List<ErpSalePriceDO> salePrices = erpSalePriceMapper.selectList(null);
//            if (CollUtil.isNotEmpty(salePrices)) {
//                List<ErpSalePriceESDO> esList = salePrices.stream()
//                        .map(this::convertToES)
//                        .collect(Collectors.toList());
//                salePriceESRepository.saveAll(esList);
//            }
//            System.out.println("销售价格全量同步ES数据完成");
//        } catch (Exception e) {
//            System.err.println("销售价格全量同步ES数据失败: " + e.getMessage());
//        }
//    }

    @Override
    public Long createSalePrice(@Valid ErpSalePriceSaveReqVO createReqVO) {


        System.out.println("妈的 前端传递的是"+createReqVO.getGroupProductId());
        // 1. 生成销售价格表编号
        String no = noRedisDAO.generate(ErpNoRedisDAO.SALE_PRICE_NO_PREFIX);
        if (erpSalePriceMapper.selectByNo(no) != null) {
            throw exception(SALE_PRICE_NOT_EXISTS);
        }
        validateCustomerProductUnique(createReqVO.getCustomerName(), createReqVO.getGroupProductId(), null);
        // 2. 根据groupProductId获取组品信息
        Long groupProductId = createReqVO.getGroupProductId();
        ErpComboRespVO comboInfo = null;
        if (groupProductId != null) {
            comboInfo = erpComboProductService.getComboWithItems(groupProductId);
        }

        // 3. 保存销售价格信息
        ErpSalePriceDO salePriceDO = BeanUtils.toBean(createReqVO, ErpSalePriceDO.class)
                .setNo(no);

        // 设置从组品获取的信息
        if (comboInfo != null) {
            salePriceDO.setProductName(comboInfo.getName());
            salePriceDO.setProductShortName(comboInfo.getShortName());
            salePriceDO.setProductImage(comboInfo.getImage());
        }

        erpSalePriceMapper.insert(salePriceDO);
        //syncToES(salePriceDO.getId()); // 新增ES同步
        return salePriceDO.getId();
    }

    @Override
    public void updateSalePrice(@Valid ErpSalePriceSaveReqVO updateReqVO) {
        validateSalePriceExists(updateReqVO.getId());
        validateCustomerProductUnique(updateReqVO.getCustomerName(), updateReqVO.getGroupProductId(), updateReqVO.getId());
        // 根据groupProductId获取组品信息
        Long groupProductId = updateReqVO.getGroupProductId();
        ErpComboRespVO comboInfo = null;
        if (groupProductId != null) {
            comboInfo = erpComboProductService.getComboWithItems(groupProductId);
        }

        ErpSalePriceDO updateObj = BeanUtils.toBean(updateReqVO, ErpSalePriceDO.class);

        // 设置从组品获取的信息
        if (comboInfo != null) {
            updateObj.setProductName(comboInfo.getName());
            updateObj.setProductShortName(comboInfo.getShortName());
            updateObj.setProductImage(comboInfo.getImage());
        }

        erpSalePriceMapper.updateById(updateObj);
        //syncToES(updateReqVO.getId()); // 新增ES同步
    }

    @Override
    public void deleteSalePrice(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            validateSalePriceExists(id);
        }
        System.out.println(ids);
        erpSalePriceMapper.deleteBatchIds(ids);
        ids.forEach(id -> salePriceESRepository.deleteById(id));
    }

    @Override
    public List<ErpSalePriceDO> validSalePriceList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpSalePriceMapper.selectBatchIds(ids);
    }

    private void validateSalePriceExists(Long id) {
        if (erpSalePriceMapper.selectById(id) == null) {
            throw exception(SALE_PRICE_NOT_EXISTS);
        }
    }

    @Override
    public ErpSalePriceDO getSalePrice(Long id) {
        return erpSalePriceMapper.selectById(id);
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpSalePriceDO> list = erpSalePriceMapper.selectBatchIds(ids);
        return BeanUtils.toBean(list, ErpSalePriceRespVO.class);
    }

    @Override
    public PageResult<ErpSalePriceRespVO> getSalePriceVOPage(ErpSalePricePageReqVO pageReqVO) {
//        // 1. 查询销售价格分页数据
//
//            // 2. 检查ES索引是否存在
//        IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpSalePriceESDO.class);
//        if (!indexOps.exists()) {
//            initESIndex(); // 如果索引不存在则创建
//            fullSyncToES(); // 全量同步数据
//        }
//
//        // 3. 检查ES是否有数据
//        long esCount = elasticsearchRestTemplate.count(new NativeSearchQueryBuilder().build(), ErpSalePriceESDO.class);
//        if (esCount == 0) {
//            fullSyncToES(); // 同步数据到ES
//        }
//        // 1. 检查数据库是否有数据
//        long dbCount = erpSalePriceMapper.selectCount(null);
//
//        // 2. 检查ES索引是否存在
//        IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpSalePriceESDO.class);
//        boolean indexExists = indexOps.exists();
//
//        // 3. 检查ES数据量
//        long esCount = 0;
//        if (indexExists) {
//            esCount = elasticsearchRestTemplate.count(new NativeSearchQueryBuilder().build(), ErpSalePriceESDO.class);
//        }
//
//        // 4. 处理数据库和ES数据不一致的情况
//        if (dbCount == 0) {
//            if (indexExists && esCount > 0) {
//                // 数据库为空但ES有数据，清空ES
//                salePriceESRepository.deleteAll();
//                System.out.println("检测到数据库为空但ES有数据，已清空销售价格ES索引");
//            }
//            return new PageResult<>(Collections.emptyList(), 0L);
//        }
//
//        if (!indexExists) {
//            initESIndex();
//            fullSyncToES();
//            return getSalePriceVOPageFromDB(pageReqVO);
//        }
//
//        if (esCount == 0 || dbCount != esCount) {
//            fullSyncToES();
//            if (esCount == 0) {
//                return getSalePriceVOPageFromDB(pageReqVO);
//            }
//        }
        PageResult<ErpSalePriceDO> pageResult = erpSalePriceMapper.selectPage(pageReqVO);

        // 2. 转换为VO列表并设置组合产品信息
        List<ErpSalePriceRespVO> voList = pageResult.getList().stream()
            .map(doObj -> {
                ErpSalePriceRespVO vo = BeanUtils.toBean(doObj, ErpSalePriceRespVO.class);
                if (doObj.getGroupProductId() != null) {
                    ErpComboRespVO comboRespVO = erpComboProductService.getComboWithItems(doObj.getGroupProductId());
                    if (comboRespVO != null) {
                        vo.setComboList(Collections.singletonList(comboRespVO));
                        vo.setGroupProductId(comboRespVO.getId());
                        vo.setGroupProductNo(comboRespVO.getNo());

                    }
                }
                return vo;
            })
            .collect(Collectors.toList());

        return new PageResult<>(voList, pageResult.getTotal());
    }

//    private PageResult<ErpSalePriceRespVO> getSalePriceVOPageFromDB(ErpSalePricePageReqVO pageReqVO) {
//        PageResult<ErpSalePriceDO> pageResult = erpSalePriceMapper.selectPage(pageReqVO);
//
//        List<ErpSalePriceRespVO> voList = pageResult.getList().stream()
//                .map(doObj -> {
//                    ErpSalePriceRespVO vo = BeanUtils.toBean(doObj, ErpSalePriceRespVO.class);
//                    if (doObj.getGroupProductId() != null) {
//                        ErpComboRespVO comboRespVO = erpComboProductService.getComboWithItems(doObj.getGroupProductId());
//                        if (comboRespVO != null) {
//                            vo.setComboList(Collections.singletonList(comboRespVO));
//                            vo.setGroupProductId(comboRespVO.getId());
//                        }
//                    }
//                    return vo;
//                })
//                .collect(Collectors.toList());
//
//        return new PageResult<>(voList, pageResult.getTotal());
//    }
//    @Override
//    public ErpSalePriceRespVO getSalePriceWithItems(Long id) {
//        // 查询销售价格基本信息
//        ErpSalePriceDO salePrice = erpSalePriceMapper.selectById(id);
//        if (salePrice == null) {
//            return null;
//        }
//
//        // 组装响应对象
//        ErpSalePriceRespVO respVO = BeanUtils.toBean(salePrice, ErpSalePriceRespVO.class);
//
//        // 如果需要关联其他数据，可以在这里查询并设置
//        // 例如：respVO.setComboList(...);
//
//        return respVO;
//    }
@Override
public ErpSalePriceRespVO getSalePriceWithItems(Long id) {
    // 查询销售价格基本信息
    ErpSalePriceDO salePrice = erpSalePriceMapper.selectById(id);
    //System.out.println("销售价格表记录："+salePrice);
    if (salePrice == null) {
        return null;
    }

    // 组装响应对象
    ErpSalePriceRespVO respVO = BeanUtils.toBean(salePrice, ErpSalePriceRespVO.class);
    //System.out.println("销售价格表组装响应对象："+salePrice);
    // 根据 groupProductId 查询组合产品信息
    if (salePrice.getGroupProductId() != null) {
        // 调用组合产品服务层查询组合产品信息
        ErpComboRespVO comboRespVO = erpComboProductService.getComboWithItems(salePrice.getGroupProductId());
        if (comboRespVO != null) {
            // 将组合产品信息赋值给 comboList
            respVO.setComboList(Collections.singletonList(comboRespVO));
            respVO.setGroupProductId(comboRespVO.getId());
        }
    }
    //System.out.println("销售价格表组装将组合产品信息赋值给 comboList响应对象："+respVO);
    return respVO;
}

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOListByGroupProductId(Long groupProductId) {
//        List<ErpSalePriceDO> list = erpSalePriceMapper.selectListByGroupProductId(groupProductId);
//        return BeanUtils.toBean(list, ErpSalePriceRespVO.class);
        return  null;
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOListByCustomerName(String customerName) {
//        List<ErpSalePriceDO> list = erpSalePriceMapper.selectListByCustomerName(customerName);
//        return BeanUtils.toBean(list, ErpSalePriceRespVO.class);
        return  null;
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOListByStatus(Integer status) {
//        List<ErpSalePriceDO> list = erpSalePriceMapper.selectListByStatus(status);
//        return BeanUtils.toBean(list, ErpSalePriceRespVO.class);
        return null;
    }

    @Override
    public List<ErpSalePriceRespVO> getSalePriceVOListByComboStatus() {
        // 1. 查询所有销售价格记录
        List<ErpSalePriceDO> salePrices = erpSalePriceMapper.selectList();

        // 2. 过滤出组合品状态符合条件的记录
        return salePrices.stream()
                .filter(salePrice -> {
                    ErpComboProductDO comboProduct = erpComboMapper.selectById(salePrice.getGroupProductId());
                    return comboProduct != null && comboProduct.getStatus().equals(CommonStatusEnum.ENABLE.getStatus());
                })
                .map(salePrice -> BeanUtils.toBean(salePrice, ErpSalePriceRespVO.class))
                .collect(Collectors.toList());
    }

//    @Override
//    public List<ErpSalePriceRespVO> searchProducts(ErpSalePriceSearchReqVO searchReqVO) {
//        // 构造查询条件
//        ErpSalePriceDO salePriceDO = new ErpSalePriceDO();
//        if (searchReqVO.getGroupProductId() != null) {
//            salePriceDO.setGroupProductId(searchReqVO.getGroupProductId());
//        }
//        if (searchReqVO.getProductName() != null) {
//            salePriceDO.setProductName(searchReqVO.getProductName());
//        }
//        if (searchReqVO.getProductShortName() != null) {
//            salePriceDO.setProductShortName(searchReqVO.getProductShortName());
//        }
//        if (searchReqVO.getCreateTime() != null) {
//            salePriceDO.setCreateTime(searchReqVO.getCreateTime());
//        }
//        if (searchReqVO.getName() != null) {
//            salePriceDO.setCustomerName(searchReqVO.getName());
//        }
//
//        // 执行查询
//        List<ErpSalePriceDO> comboProductDOList = erpSalePriceMapper.selectList(new LambdaQueryWrapper<ErpSalePriceDO>()
//                .eq(salePriceDO.getGroupProductId() != null, ErpSalePriceDO::getGroupProductId, searchReqVO.getGroupProductId())
//                .like(salePriceDO.getProductName() != null, ErpSalePriceDO::getProductName, searchReqVO.getProductName())
//                .like(salePriceDO.getProductShortName() != null, ErpSalePriceDO::getProductShortName, searchReqVO.getProductShortName())
//                .eq(salePriceDO.getCreateTime() != null, ErpSalePriceDO::getCreateTime, searchReqVO.getCreateTime())
//                .like(salePriceDO.getCustomerName() != null, ErpSalePriceDO::getCustomerName, searchReqVO.getName()));
//
//
//        // 转换为响应对象
////        List<ErpSalePriceRespVO> respVOList = BeanUtils.toBean(comboProductDOList, ErpSalePriceRespVO.class);
//
////        // 如果有组品ID，查询组品数据并填充到 comboList
////        if (searchReqVO.getGroupProductId() != null) {
////            // 获取组品数据
////            List<ErpComboRespVO> comboList = erpComboProductService.getComboVOList(
////                    comboProductDOList.stream().map(ErpSalePriceDO::getGroupProductId).distinct().collect(Collectors.toList())
////            );
////
////            // 将组品数据填充到每个响应对象的 comboList 字段
////            respVOList.forEach(respVO -> {
////                respVO.setComboList(comboList);
////            });
////        }
//        // 转换为VO列表并设置组合产品信息
//        List<ErpSalePriceRespVO> respVOList = BeanUtils.toBean(comboProductDOList.stream()
//                .map(doObj -> {
//                    ErpSalePriceRespVO vo = BeanUtils.toBean(doObj, ErpSalePriceRespVO.class);
//                    if (doObj.getGroupProductId() != null) {
//                        ErpComboRespVO comboRespVO = erpComboProductService.getComboWithItems(doObj.getGroupProductId());
//                        if (comboRespVO != null) {
//                            vo.setComboList(Collections.singletonList(comboRespVO));
//                            vo.setGroupProductId(comboRespVO.getId());
//                            vo.setProductName(comboRespVO.getName());
//                            vo.setProductShortName(comboRespVO.getShortName());
//                            vo.setOriginalQuantity(comboRespVO.getTotalQuantity());
//                            vo.setShippingCode(comboRespVO.getShippingCode());
//                        }
//                    }
//                    return vo;
//                })
//                .collect(Collectors.toList()), ErpSalePriceRespVO.class);
//        // 转换为响应对象
//        return respVOList;
//    }
    @Override
    public List<ErpSalePriceRespVO> searchProducts(ErpSalePriceSearchReqVO searchReqVO) {
        // 1. 使用Mapper层方法查询
        List<ErpSalePriceDO> list = erpSalePriceMapper.selectList(new LambdaQueryWrapper<ErpSalePriceDO>()
                .eq(searchReqVO.getGroupProductId() != null, ErpSalePriceDO::getGroupProductId, searchReqVO.getGroupProductId())
                .eq(searchReqVO.getCustomerName() != null, ErpSalePriceDO::getCustomerName, searchReqVO.getCustomerName()));

        // 2. 转换为VO列表并设置组合产品信息
        return list.stream()
                .map(doObj -> {
                    ErpSalePriceRespVO vo = BeanUtils.toBean(doObj, ErpSalePriceRespVO.class);
                    if (doObj.getGroupProductId() != null) {
                        ErpComboRespVO comboRespVO = erpComboProductService.getComboWithItems(doObj.getGroupProductId());
                        if (comboRespVO != null) {
                            vo.setComboList(Collections.singletonList(comboRespVO));
                            vo.setGroupProductId(comboRespVO.getId());
                            vo.setProductName(comboRespVO.getName());
                            vo.setProductShortName(comboRespVO.getShortName());
                            vo.setOriginalQuantity(comboRespVO.getTotalQuantity());
                            vo.setShippingCode(comboRespVO.getShippingCode());
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }
    @Override
    public ErpSalePriceRespVO getSalePriceByGroupProductIdAndCustomerName(Long groupProductId, String customerName) {
        // 1. 查询销售价格基本信息
        ErpSalePriceRespVO respVO = erpSalePriceMapper.selectByGroupProductIdAndCustomerName(groupProductId, customerName);
        if (respVO == null) {
            return null;
        }

        // 2. 设置组合产品信息
        if (groupProductId != null) {
            ErpComboRespVO comboRespVO = erpComboProductService.getComboWithItems(groupProductId);
            if (comboRespVO != null) {
                respVO.setComboList(Collections.singletonList(comboRespVO));
                respVO.setGroupProductId(comboRespVO.getId());
                respVO.setProductName(comboRespVO.getName());
                respVO.setProductShortName(comboRespVO.getShortName());
                respVO.setOriginalQuantity(comboRespVO.getTotalQuantity());
                respVO.setShippingCode(comboRespVO.getShippingCode());
            }
        }

        return respVO;
    }
    @Override
    public List<ErpSalePriceRespVO> getMissingPrices() {
        return erpSalePriceMapper.selectMissingPrices();
    }


    private void validateCustomerProductUnique(String customerName, Long groupProductId, Long excludeId) {
        if (StrUtil.isBlank(customerName) || groupProductId == null) {
            return;
        }

        // 直接使用groupProductId和customerName进行校验
        Long count = erpSalePriceMapper.selectCount(new LambdaQueryWrapper<ErpSalePriceDO>()
                .eq(ErpSalePriceDO::getCustomerName, customerName)
                .eq(ErpSalePriceDO::getGroupProductId, groupProductId)
                .ne(excludeId != null, ErpSalePriceDO::getId, excludeId));
        if (count > 0) {
            throw exception(SALE_PRICE_CUSTOMER_PRODUCT_DUPLICATE);
        }
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public ErpSalePriceImportRespVO importSalePriceList(List<ErpSalePriceImportExcelVO> importList, boolean isUpdateSupport) {
//        if (CollUtil.isEmpty(importList)) {
//            throw exception(SALE_PRICE_IMPORT_LIST_IS_EMPTY);
//        }
//
//        // 初始化返回结果
//        ErpSalePriceImportRespVO respVO = ErpSalePriceImportRespVO.builder()
//                .createNames(new ArrayList<>())
//                .updateNames(new ArrayList<>())
//                .failureNames(new LinkedHashMap<>())
//                .build();
//
//        // 查询已存在的销售价格记录
//        Set<String> noSet = importList.stream()
//                .map(ErpSalePriceImportExcelVO::getNo)
//                .filter(StrUtil::isNotBlank)
//                .collect(Collectors.toSet());
//        List<ErpSalePriceDO> existList = erpSalePriceMapper.selectListByNoIn(noSet);
//        Map<String, ErpSalePriceDO> noSalePriceMap = convertMap(existList, ErpSalePriceDO::getNo);
//
//        // 遍历处理每个导入项
//        for (int i = 0; i < importList.size(); i++) {
//            ErpSalePriceImportExcelVO importVO = importList.get(i);
//            try {
//                ErpComboProductDO comboProduct = erpComboMapper.selectByNo(importVO.getGroupProductNo());
//                // 判断是否支持更新
//                ErpSalePriceDO existSalePrice = noSalePriceMap.get(importVO.getNo());
//
//                if (existSalePrice == null) {
//                    // 创建
//                    ErpSalePriceDO salePrice = BeanUtils.toBean(importVO, ErpSalePriceDO.class).setGroupProductId(comboProduct.getId());
//                    if (StrUtil.isEmpty(salePrice.getNo())) {
//                        salePrice.setNo(noRedisDAO.generate(ErpNoRedisDAO.SALE_PRICE_NO_PREFIX));
//                    }
//                    erpSalePriceMapper.insert(salePrice);
//                    respVO.getCreateNames().add(salePrice.getNo());
//                } else if (isUpdateSupport) {
//                    // 更新
//                    ErpSalePriceDO updateSalePrice = BeanUtils.toBean(importVO, ErpSalePriceDO.class);
//                    updateSalePrice.setId(existSalePrice.getId());
//                    erpSalePriceMapper.updateById(updateSalePrice);
//                    respVO.getUpdateNames().add(updateSalePrice.getNo());
//                } else {
//                    throw exception(SALE_PRICE_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
//                }
//            } catch (ServiceException ex) {
//                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知销售价格";
//                respVO.getFailureNames().put(errorKey, ex.getMessage());
//            } catch (Exception ex) {
//                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知销售价格";
//                respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
//            }
//        }
//
//        return respVO;
//    }

    @Override
@Transactional(rollbackFor = Exception.class)
public ErpSalePriceImportRespVO importSalePriceList(List<ErpSalePriceImportExcelVO> importList, boolean isUpdateSupport) {
    if (CollUtil.isEmpty(importList)) {
        throw exception(SALE_PRICE_IMPORT_LIST_IS_EMPTY);
    }

    System.out.println("开始导入销售价格数据，共" + importList.size() + "条记录");

    // 初始化返回结果
    ErpSalePriceImportRespVO respVO = ErpSalePriceImportRespVO.builder()
            .createNames(new ArrayList<>())
            .updateNames(new ArrayList<>())
            .failureNames(new LinkedHashMap<>())
            .build();

    // 查询已存在的销售价格记录
    Set<String> noSet = importList.stream()
            .map(ErpSalePriceImportExcelVO::getNo)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toSet());
    System.out.println("需要检查的销售价格编号列表：" + noSet);

    List<ErpSalePriceDO> existList = erpSalePriceMapper.selectListByNoIn(noSet);
    System.out.println("已存在的销售价格记录数量：" + existList.size());

    Map<String, ErpSalePriceDO> noSalePriceMap = convertMap(existList, ErpSalePriceDO::getNo);

    // 遍历处理每个导入项
    for (int i = 0; i < importList.size(); i++) {
        ErpSalePriceImportExcelVO importVO = importList.get(i);
        System.out.println("正在处理第" + (i+1) + "条记录，编号：" + importVO.getNo());

        try {
            System.out.println("查找组品编号：" + importVO.getGroupProductNo());
            ErpComboProductDO comboProduct = erpComboMapper.selectByNo(importVO.getGroupProductNo());
            if (comboProduct == null) {
                System.err.println("未找到组品编号：" + importVO.getGroupProductNo());
                throw exception(SALE_PRICE_GROUP_PRODUCT_ID_REQUIRED, "组品编号不存在: " + importVO.getGroupProductNo());
            }
            System.out.println("找到组品ID：" + comboProduct.getId());

            // 判断是否支持更新
            ErpSalePriceDO existSalePrice = noSalePriceMap.get(importVO.getNo());
            System.out.println("已存在记录检查结果：" + (existSalePrice != null));

            if (existSalePrice == null) {
                // 创建
                System.out.println("准备创建新记录");
                ErpSalePriceDO salePrice = BeanUtils.toBean(importVO, ErpSalePriceDO.class)
                        .setGroupProductId(comboProduct.getId());

                if (StrUtil.isEmpty(salePrice.getNo())) {
                    String newNo = noRedisDAO.generate(ErpNoRedisDAO.SALE_PRICE_NO_PREFIX);
                    System.out.println("生成了新编号：" + newNo);
                    salePrice.setNo(newNo);
                }

                System.out.println("准备插入记录：" + salePrice);
                erpSalePriceMapper.insert(salePrice);
                respVO.getCreateNames().add(salePrice.getNo());
                System.out.println("创建成功，ID：" + salePrice.getId());
            } else if (isUpdateSupport) {
                // 更新
                System.out.println("准备更新记录，ID：" + existSalePrice.getId());
                ErpSalePriceDO updateSalePrice = BeanUtils.toBean(importVO, ErpSalePriceDO.class)
                        .setId(existSalePrice.getId())
                        .setGroupProductId(comboProduct.getId());

                System.out.println("准备更新记录内容：" + updateSalePrice);
                erpSalePriceMapper.updateById(updateSalePrice);
                respVO.getUpdateNames().add(updateSalePrice.getNo());
                System.out.println("更新成功");
            } else {
                System.err.println("记录已存在且不允许更新，编号：" + importVO.getNo());
                throw exception(SALE_PRICE_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
            }
        } catch (ServiceException ex) {
            String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知销售价格";
            System.err.println("业务异常：" + ex.getMessage());
            respVO.getFailureNames().put(errorKey, ex.getMessage());
        } catch (Exception ex) {
            String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知销售价格";
            System.err.println("系统异常：" + ex.getMessage());
            ex.printStackTrace();
            respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
        }
    }

    System.out.println("导入完成，成功创建：" + respVO.getCreateNames().size() +
                      "，成功更新：" + respVO.getUpdateNames().size() +
                      "，失败：" + respVO.getFailureNames().size());
    return respVO;
}
}
