package cn.iocoder.yudao.module.erp.service.dropship;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.dropship.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.dropship.ErpDropshipAssistDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.dropship.ErpDropshipAssistESDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpComboProductES;
import cn.iocoder.yudao.module.erp.dal.mysql.dropship.ErpDropshipAssistMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpComboMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.service.product.ErpComboProductESRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.context.event.EventListener;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    @Resource
    private ErpComboMapper erpComboMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private ErpDropshipAssistESRepository dropshipAssistESRepository;

    @Resource
    private ErpComboProductESRepository comboProductESRepository;

    // 初始化ES索引
    @EventListener(ApplicationReadyEvent.class)
    public void initESIndex() {
        System.out.println("开始初始化代发辅助表ES索引...");
        try {
            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpDropshipAssistESDO.class);
            if (!indexOps.exists()) {
                indexOps.create();
                indexOps.putMapping(indexOps.createMapping(ErpDropshipAssistESDO.class));
                System.out.println("代发辅助表索引创建成功");
            }
        } catch (Exception e) {
            System.err.println("代发辅助表索引初始化失败: " + e.getMessage());
        }
    }

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

//@Override
//public PageResult<ErpDropshipAssistRespVO> getDropshipAssistVOPage(ErpDropshipAssistPageReqVO pageReqVO) {
//    try {
//        // 检查索引是否存在
//        IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpDropshipAssistESDO.class);
//        if (!indexOps.exists()) {
//            return PageResult.empty();
//        }
//
//        // 构建ES查询条件
//        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.boolQuery()
//                        .must(QueryBuilders.matchAllQuery())
//                        .must(StrUtil.isNotBlank(pageReqVO.getNo()) ?
//                              QueryBuilders.wildcardQuery("no", "*" + pageReqVO.getNo() + "*") :
//                              QueryBuilders.matchAllQuery()))
//                .withPageable(PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize()))
//                .withSort(Sort.by(Sort.Direction.DESC, "id"));
//
//        // 执行ES查询
//        SearchHits<ErpDropshipAssistESDO> searchHits = elasticsearchRestTemplate.search(
//                queryBuilder.build(),
//                ErpDropshipAssistESDO.class
//        );
//
//        // 转换结果
//        List<ErpDropshipAssistRespVO> list = searchHits.getSearchHits().stream()
//                .map(hit -> {
//                    ErpDropshipAssistESDO esDO = hit.getContent();
//                    ErpDropshipAssistRespVO respVO = BeanUtils.toBean(esDO, ErpDropshipAssistRespVO.class);
//
//                    // 查询关联的组品信息
//                    if (esDO.getComboProductId() != null) {
//                        try {
//                            Long comboProductId = Long.parseLong(esDO.getComboProductId());
//                            Optional<ErpComboProductES> comboProduct = comboProductESRepository.findById(comboProductId);
//                            comboProduct.ifPresent(cp -> {
//                                respVO.setProductName(cp.getName());
//                                respVO.setComboProductNo(cp.getNo());
//                                respVO.setProductShortName(cp.getShortName());
//                                respVO.setShippingCode(cp.getShippingCode());
//                            });
//                        } catch (NumberFormatException e) {
//                            // 处理转换异常
//                        }
//                    }
//                    return respVO;
//                })
//                .collect(Collectors.toList());
//
//        return new PageResult<>(list, searchHits.getTotalHits());
//    } catch (Exception e) {
//        // 记录错误日志
//        System.err.println("ES查询失败: " + e.getMessage());
//        return PageResult.empty();
//    }
//}

//@Override
//public PageResult<ErpDropshipAssistRespVO> getDropshipAssistVOPage(ErpDropshipAssistPageReqVO pageReqVO) {
//    try {
//        // 检查索引是否存在
//        IndexOperations indexOps = elasticsearchRestTemplate.indexOps(ErpDropshipAssistESDO.class);
//        if (!indexOps.exists()) {
//            try {
//                indexOps.create();
//                indexOps.putMapping(indexOps.createMapping(ErpDropshipAssistESDO.class));
//                System.out.println("代发辅助表索引创建成功");
//            } catch (Exception e) {
//                System.err.println("代发辅助表索引创建失败: " + e.getMessage());
//                return PageResult.empty();
//            }
//        }
//
//        // 构建查询条件 - 移除分页参数，设置足够大的size
//        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.boolQuery()
//                        .must(QueryBuilders.matchAllQuery())
//                        .must(StrUtil.isNotBlank(pageReqVO.getNo()) ?
//                              QueryBuilders.wildcardQuery("no", "*" + pageReqVO.getNo().toLowerCase() + "*") :
//                              QueryBuilders.matchAllQuery()))
//                .withPageable(PageRequest.of(0, 10000)) // 设置足够大的size获取全部数据
//                .withSort(Sort.by(Sort.Direction.DESC, "_id"));
//
//        // 执行ES查询
//        SearchHits<ErpDropshipAssistESDO> searchHits = elasticsearchRestTemplate.search(
//                queryBuilder.build(),
//                ErpDropshipAssistESDO.class
//        );
//
//        // 转换结果
//        List<ErpDropshipAssistRespVO> list = searchHits.getSearchHits().stream()
//                .map(hit -> {
//                    ErpDropshipAssistESDO esDO = hit.getContent();
//                    ErpDropshipAssistRespVO respVO = BeanUtils.toBean(esDO, ErpDropshipAssistRespVO.class);
//
//                    // 查询关联的组品信息
//                    if (esDO.getComboProductId() != null) {
//                        try {
//                            Long comboProductId = Long.parseLong(esDO.getComboProductId());
//                            Optional<ErpComboProductES> comboProduct = comboProductESRepository.findById(comboProductId);
//                            comboProduct.ifPresent(cp -> {
//                                respVO.setProductName(cp.getName());
//                                respVO.setComboProductNo(cp.getNo());
//                                respVO.setProductShortName(cp.getShortName());
//                                respVO.setShippingCode(cp.getShippingCode());
//                            });
//                        } catch (NumberFormatException e) {
//                            // 处理转换异常
//                        }
//                    }
//                    return respVO;
//                })
//                .collect(Collectors.toList());
//
//        return new PageResult<>(list, searchHits.getTotalHits());
//    } catch (Exception e) {
//        System.err.println("ES查询失败: " + e.getClass().getName() + " - " + e.getMessage());
//        e.printStackTrace();
//        return PageResult.empty();
//    }
//}

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
        //return convertMap(getDropshipAssistVOList(ids), ErpDropshipAssistRespVO::getId);
        return null;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpDropshipAssistImportRespVO importDropshipAssistList(List<ErpDropshipAssistImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(DROPSHIP_ASSIST_IMPORT_LIST_IS_EMPTY);
        }

        // 初始化返回结果
        ErpDropshipAssistImportRespVO respVO = ErpDropshipAssistImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        // 查询已存在的代发辅助记录
        Set<String> noSet = importList.stream()
                .map(ErpDropshipAssistImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        List<ErpDropshipAssistDO> existList = dropshipAssistMapper.selectListByNoIn(noSet);
        Map<String, ErpDropshipAssistDO> noDropshipAssistMap = convertMap(existList, ErpDropshipAssistDO::getNo);

        // 遍历处理每个导入项
        for (int i = 0; i < importList.size(); i++) {
            ErpDropshipAssistImportExcelVO importVO = importList.get(i);
            try {
                // 将组品业务编号转换为组品ID
                if (StrUtil.isNotBlank(importVO.getComboProductId())) {
                    ErpComboProductDO comboProduct = erpComboMapper.selectByNo(importVO.getComboProductId());
                    if (comboProduct == null) {
                        throw exception(COMBO_PRODUCT_NOT_EXISTS);
                    }
                    importVO.setComboProductId(comboProduct.getId().toString());
                }
                // 判断是否支持更新
                ErpDropshipAssistDO existDropshipAssist = noDropshipAssistMap.get(importVO.getNo());
                if (existDropshipAssist == null) {
                    // 创建
                    ErpDropshipAssistDO dropshipAssist = BeanUtils.toBean(importVO, ErpDropshipAssistDO.class);
                    if (StrUtil.isEmpty(dropshipAssist.getNo())) {
                        dropshipAssist.setNo(noRedisDAO.generate(ErpNoRedisDAO.DROPSHIP_ASSIST_NO_PREFIX));
                    }
                    dropshipAssistMapper.insert(dropshipAssist);
                    respVO.getCreateNames().add(dropshipAssist.getNo());
                } else if (isUpdateSupport) {
                    // 更新
                    ErpDropshipAssistDO updateDropshipAssist = BeanUtils.toBean(importVO, ErpDropshipAssistDO.class);
                    updateDropshipAssist.setId(existDropshipAssist.getId());
                    dropshipAssistMapper.updateById(updateDropshipAssist);
                    respVO.getUpdateNames().add(updateDropshipAssist.getNo());
                } else {
                    throw exception(DROPSHIP_ASSIST_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
                }
            } catch (ServiceException ex) {
                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知代发辅助";
                respVO.getFailureNames().put(errorKey, ex.getMessage());
            } catch (Exception ex) {
                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知代发辅助";
                respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
            }
        }

        return respVO;
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public ErpDropshipAssistImportRespVO importDropshipAssistList(List<ErpDropshipAssistImportExcelVO> importList, boolean isUpdateSupport) {
//        if (CollUtil.isEmpty(importList)) {
//            throw exception(DROPSHIP_ASSIST_IMPORT_LIST_IS_EMPTY);
//        }
//
//        // 初始化返回结果
//        ErpDropshipAssistImportRespVO respVO = ErpDropshipAssistImportRespVO.builder()
//                .createNames(new ArrayList<>())
//                .updateNames(new ArrayList<>())
//                .failureNames(new LinkedHashMap<>())
//                .build();
//
//        // 批量处理组品ID转换
//        List<ErpDropshipAssistESDO> esCreateList = new ArrayList<>();
//        List<ErpDropshipAssistESDO> esUpdateList = new ArrayList<>();
//
//        try {
//            // 批量查询组品信息
//            Set<String> comboProductNos = importList.stream()
//                    .map(ErpDropshipAssistImportExcelVO::getComboProductId)
//                    .filter(StrUtil::isNotBlank)
//                    .collect(Collectors.toSet());
//            Map<String, Long> comboProductIdMap = comboProductNos.isEmpty() ? Collections.emptyMap() :
//                    convertMap(erpComboMapper.selectListByNoIn(comboProductNos), ErpComboProductDO::getNo, ErpComboProductDO::getId);
//
//            // 批量查询已存在的记录
//            Set<String> noSet = importList.stream()
//                    .map(ErpDropshipAssistImportExcelVO::getNo)
//                    .filter(StrUtil::isNotBlank)
//                    .collect(Collectors.toSet());
//            Map<String, ErpDropshipAssistESDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
//                    convertMap(dropshipAssistESRepository.findByNoIn(noSet), ErpDropshipAssistESDO::getNo);
//
//            // 批量转换数据
//            for (ErpDropshipAssistImportExcelVO importVO : importList) {
//                try {
//                    ErpDropshipAssistESDO esDO = BeanUtils.toBean(importVO, ErpDropshipAssistESDO.class);
//
//                    // 设置组品ID
//                    if (StrUtil.isNotBlank(importVO.getComboProductId())) {
//                        Long comboProductId = comboProductIdMap.get(importVO.getComboProductId());
//                        if (comboProductId == null) {
//                            throw exception(COMBO_PRODUCT_NOT_EXISTS);
//                        }
//                        esDO.setComboProductId(comboProductId.toString());
//                    }
//
//                    // 生成编号
//                    if (StrUtil.isEmpty(esDO.getNo())) {
//                        esDO.setNo(noRedisDAO.generate(ErpNoRedisDAO.DROPSHIP_ASSIST_NO_PREFIX));
//                    }
//
//                    // 判断是否已存在
//                    ErpDropshipAssistESDO existDO = existMap.get(esDO.getNo());
//                    if (existDO == null) {
//                        esCreateList.add(esDO);
//                        respVO.getCreateNames().add(esDO.getNo());
//                    } else if (isUpdateSupport) {
//                        esDO.setId(existDO.getId()); // 保留原ID
//                        esUpdateList.add(esDO);
//                        respVO.getUpdateNames().add(esDO.getNo());
//                    } else {
//                        throw exception(DROPSHIP_ASSIST_IMPORT_NO_EXISTS, esDO.getNo());
//                    }
//                } catch (ServiceException ex) {
//                    String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知代发辅助";
//                    respVO.getFailureNames().put(errorKey, ex.getMessage());
//                }
//            }
//
//            // 批量保存到ES
//            if (CollUtil.isNotEmpty(esCreateList)) {
//                dropshipAssistESRepository.saveAll(esCreateList);
//            }
//            if (CollUtil.isNotEmpty(esUpdateList)) {
//                dropshipAssistESRepository.saveAll(esUpdateList);
//            }
//        } catch (Exception ex) {
//            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
//        }
//
//        return respVO;
//    }


//@Override
//@Transactional(rollbackFor = Exception.class)
//public ErpDropshipAssistImportRespVO importDropshipAssistList(List<ErpDropshipAssistImportExcelVO> importList, boolean isUpdateSupport) {
//    if (CollUtil.isEmpty(importList)) {
//        throw exception(DROPSHIP_ASSIST_IMPORT_LIST_IS_EMPTY);
//    }
//
//    // 初始化返回结果
//    ErpDropshipAssistImportRespVO respVO = ErpDropshipAssistImportRespVO.builder()
//            .createNames(new ArrayList<>())
//            .updateNames(new ArrayList<>())
//            .failureNames(new LinkedHashMap<>())
//            .build();
//
//    // 批量处理组品ID转换
//    List<ErpDropshipAssistDO> createList = new ArrayList<>();
//    List<ErpDropshipAssistDO> updateList = new ArrayList<>();
//
//    try {
//        // 批量查询组品信息
//        Set<String> comboProductNos = importList.stream()
//                .map(ErpDropshipAssistImportExcelVO::getComboProductId)
//                .filter(StrUtil::isNotBlank)
//                .collect(Collectors.toSet());
//        Map<String, Long> comboProductIdMap = comboProductNos.isEmpty() ? Collections.emptyMap() :
//                convertMap(erpComboMapper.selectListByNoIn(comboProductNos), ErpComboProductDO::getNo, ErpComboProductDO::getId);
//
//        // 批量查询已存在的记录
//        Set<String> noSet = importList.stream()
//                .map(ErpDropshipAssistImportExcelVO::getNo)
//                .filter(StrUtil::isNotBlank)
//                .collect(Collectors.toSet());
//        Map<String, ErpDropshipAssistDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
//                convertMap(dropshipAssistMapper.selectListByNoIn(noSet), ErpDropshipAssistDO::getNo);
//
//        // 批量转换数据
//        for (int i = 0; i < importList.size(); i++) {
//            ErpDropshipAssistImportExcelVO importVO = importList.get(i);
//            try {
//                // 将组品业务编号转换为组品ID
//                if (StrUtil.isNotBlank(importVO.getComboProductId())) {
//                    Long comboProductId = comboProductIdMap.get(importVO.getComboProductId());
//                    if (comboProductId == null) {
//                        throw exception(COMBO_PRODUCT_NOT_EXISTS);
//                    }
//                    importVO.setComboProductId(comboProductId.toString());
//                }
//
//                // 判断是否支持更新
//                ErpDropshipAssistDO existDropshipAssist = existMap.get(importVO.getNo());
//                if (existDropshipAssist == null) {
//                   // 创建 - 自动生成新的no编号
//                   ErpDropshipAssistDO dropshipAssist = BeanUtils.toBean(importVO, ErpDropshipAssistDO.class);
//                   dropshipAssist.setNo(noRedisDAO.generate(ErpNoRedisDAO.DROPSHIP_ASSIST_NO_PREFIX));
//                    if (StrUtil.isEmpty(dropshipAssist.getNo())) {
//                        dropshipAssist.setNo(noRedisDAO.generate(ErpNoRedisDAO.DROPSHIP_ASSIST_NO_PREFIX));
//                    }
//                    createList.add(dropshipAssist);
//                    respVO.getCreateNames().add(dropshipAssist.getNo());
//                } else if (isUpdateSupport) {
//                    // 更新
//                    ErpDropshipAssistDO updateDropshipAssist = BeanUtils.toBean(importVO, ErpDropshipAssistDO.class);
//                    updateDropshipAssist.setId(existDropshipAssist.getId());
//                    updateList.add(updateDropshipAssist);
//                    respVO.getUpdateNames().add(updateDropshipAssist.getNo());
//                } else {
//                    throw exception(DROPSHIP_ASSIST_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
//                }
//            } catch (ServiceException ex) {
//                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知代发辅助";
//                respVO.getFailureNames().put(errorKey, ex.getMessage());
//            } catch (Exception ex) {
//                String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知代发辅助";
//                respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
//            }
//        }
//
//        // 批量保存到数据库
//        if (CollUtil.isNotEmpty(createList)) {
//            dropshipAssistMapper.insertBatch(createList);
//        }
//        if (CollUtil.isNotEmpty(updateList)) {
//            updateList.forEach(dropshipAssistMapper::updateById);
//        }
//    } catch (Exception ex) {
//        respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
//    }
//
//    return respVO;
//}

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public ErpDropshipAssistImportRespVO importDropshipAssistList(List<ErpDropshipAssistImportExcelVO> importList, boolean isUpdateSupport) {
//        if (CollUtil.isEmpty(importList)) {
//            throw exception(DROPSHIP_ASSIST_IMPORT_LIST_IS_EMPTY);
//        }
//
//        // 初始化返回结果
//        ErpDropshipAssistImportRespVO respVO = ErpDropshipAssistImportRespVO.builder()
//                .createNames(new ArrayList<>())
//                .updateNames(new ArrayList<>())
//                .failureNames(new LinkedHashMap<>())
//                .build();
//
//        // 批量处理组品ID转换
//        List<ErpDropshipAssistDO> createList = new ArrayList<>();
//        List<ErpDropshipAssistDO> updateList = new ArrayList<>();
//        List<ErpDropshipAssistESDO> esCreateList = new ArrayList<>();
//        List<ErpDropshipAssistESDO> esUpdateList = new ArrayList<>();
//
//        try {
//            // 批量查询组品信息
//            Set<String> comboProductNos = importList.stream()
//                    .map(ErpDropshipAssistImportExcelVO::getComboProductId)
//                    .filter(StrUtil::isNotBlank)
//                    .collect(Collectors.toSet());
//            Map<String, Long> comboProductIdMap = comboProductNos.isEmpty() ? Collections.emptyMap() :
//                    convertMap(erpComboMapper.selectListByNoIn(comboProductNos), ErpComboProductDO::getNo, ErpComboProductDO::getId);
//
//            // 批量查询已存在的记录
//            Set<String> noSet = importList.stream()
//                    .map(ErpDropshipAssistImportExcelVO::getNo)
//                    .filter(StrUtil::isNotBlank)
//                    .collect(Collectors.toSet());
//            Map<String, ErpDropshipAssistDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
//                    convertMap(dropshipAssistMapper.selectListByNoIn(noSet), ErpDropshipAssistDO::getNo);
//
//            // 批量转换数据
//            for (int i = 0; i < importList.size(); i++) {
//                ErpDropshipAssistImportExcelVO importVO = importList.get(i);
//                try {
//                    // 将组品业务编号转换为组品ID
//                    if (StrUtil.isNotBlank(importVO.getComboProductId())) {
//                        Long comboProductId = comboProductIdMap.get(importVO.getComboProductId());
//                        if (comboProductId == null) {
//                            throw exception(COMBO_PRODUCT_NOT_EXISTS);
//                        }
//                        importVO.setComboProductId(comboProductId.toString());
//                    }
//
//                    // 判断是否支持更新
//                    ErpDropshipAssistDO existDropshipAssist = existMap.get(importVO.getNo());
//                    if (existDropshipAssist == null) {
//                        // 创建 - 自动生成新的no编号
//                        ErpDropshipAssistDO dropshipAssist = BeanUtils.toBean(importVO, ErpDropshipAssistDO.class);
//                        dropshipAssist.setNo(noRedisDAO.generate(ErpNoRedisDAO.DROPSHIP_ASSIST_NO_PREFIX));
//                        createList.add(dropshipAssist);
//                        esCreateList.add(BeanUtils.toBean(dropshipAssist, ErpDropshipAssistESDO.class));
//                        respVO.getCreateNames().add(dropshipAssist.getNo());
//                    } else if (isUpdateSupport) {
//                        // 更新
//                        ErpDropshipAssistDO updateDropshipAssist = BeanUtils.toBean(importVO, ErpDropshipAssistDO.class);
//                        updateDropshipAssist.setId(existDropshipAssist.getId());
//                        updateList.add(updateDropshipAssist);
//                        esUpdateList.add(BeanUtils.toBean(updateDropshipAssist, ErpDropshipAssistESDO.class));
//                        respVO.getUpdateNames().add(updateDropshipAssist.getNo());
//                    } else {
//                        throw exception(DROPSHIP_ASSIST_IMPORT_NO_EXISTS, i + 1, importVO.getNo());
//                    }
//                } catch (ServiceException ex) {
//                    String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知代发辅助";
//                    respVO.getFailureNames().put(errorKey, ex.getMessage());
//                } catch (Exception ex) {
//                    String errorKey = StrUtil.isNotBlank(importVO.getNo()) ? importVO.getNo() : "未知代发辅助";
//                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
//                }
//            }
//
//            // 批量保存到数据库
//            if (CollUtil.isNotEmpty(createList)) {
//                dropshipAssistMapper.insertBatch(createList);
//            }
//            if (CollUtil.isNotEmpty(updateList)) {
//                updateList.forEach(dropshipAssistMapper::updateById);
//            }
//
//            // 批量保存到ES
//            if (CollUtil.isNotEmpty(esCreateList)) {
//                dropshipAssistESRepository.saveAll(esCreateList);
//            }
//            if (CollUtil.isNotEmpty(esUpdateList)) {
//                dropshipAssistESRepository.saveAll(esUpdateList);
//            }
//        } catch (Exception ex) {
//            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
//        }
//
//        return respVO;
//    }
}
