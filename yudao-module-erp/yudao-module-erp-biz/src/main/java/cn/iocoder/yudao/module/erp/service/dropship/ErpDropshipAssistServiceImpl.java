package cn.iocoder.yudao.module.erp.service.dropship;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
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
import java.time.LocalDateTime;
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
    public ErpDropshipAssistRespVO getDropshipAssistDetail(Long id) {
        // 校验存在
        validateDropshipAssist(id);
        // 查询详情（包含组品信息）
        return dropshipAssistMapper.selectDetailById(id);
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

        // 2. 校验字段组合唯一性
        ErpDropshipAssistDO existingRecord = dropshipAssistMapper.selectByUniqueFields(
                reqVO.getOriginalProduct(),
                reqVO.getOriginalSpec(),
                reqVO.getOriginalQuantity(),
                reqVO.getComboProductId(),
                reqVO.getProductSpec(),
                reqVO.getProductQuantity(),
                id // 排除当前记录ID（更新时使用）
        );
        if (existingRecord != null) {
            throw exception(DROPSHIP_ASSIST_FIELDS_DUPLICATE);
        }
    }




    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpDropshipAssistImportRespVO importDropshipAssistList(List<ErpDropshipAssistImportExcelVO> importList, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importList)) {
            throw exception(DROPSHIP_ASSIST_IMPORT_LIST_IS_EMPTY);
        }

        System.out.println("开始导入代发辅助数据，共" + importList.size() + "条记录");

        // 初始化返回结果
        ErpDropshipAssistImportRespVO respVO = ErpDropshipAssistImportRespVO.builder()
                .createNames(new ArrayList<>())
                .updateNames(new ArrayList<>())
                .failureNames(new LinkedHashMap<>())
                .build();

        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String username = WebFrameworkUtils.getUsernameById(userId);
        LocalDateTime now = LocalDateTime.now();

        try {
            // 1. 统一校验所有数据（包括数据类型校验和业务逻辑校验）
            Map<String, String> allErrors = validateAllImportData(importList, isUpdateSupport);
            if (!allErrors.isEmpty()) {
                // 如果有任何错误，直接返回错误信息，不进行后续导入
                respVO.getFailureNames().putAll(allErrors);
                return respVO;
            }

            // 2. 批量处理组品ID转换
            List<ErpDropshipAssistDO> createList = new ArrayList<>();
            List<ErpDropshipAssistDO> updateList = new ArrayList<>();

            // 3. 批量查询组品信息
            Set<String> comboProductNos = importList.stream()
                    .map(ErpDropshipAssistImportExcelVO::getComboProductId)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, Long> comboProductIdMap = comboProductNos.isEmpty() ? Collections.emptyMap() :
                    convertMap(erpComboMapper.selectListByNoIn(comboProductNos), ErpComboProductDO::getNo, ErpComboProductDO::getId);

            // 4. 批量查询已存在的记录
            Set<String> noSet = importList.stream()
                    .map(ErpDropshipAssistImportExcelVO::getNo)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Map<String, ErpDropshipAssistDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                    convertMap(dropshipAssistMapper.selectListByNoIn(noSet), ErpDropshipAssistDO::getNo);

            // 5. 批量转换数据
            for (int i = 0; i < importList.size(); i++) {
                ErpDropshipAssistImportExcelVO importVO = importList.get(i);
                String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getOriginalProduct()) ? "(" + importVO.getOriginalProduct() + ")" : "");

                try {
                    // 将组品业务编号转换为组品ID
                    String comboProductNo = importVO.getComboProductId();
                    Long comboProductIdLong = null;
                    if (StrUtil.isNotBlank(comboProductNo)) {
                        comboProductIdLong = comboProductIdMap.get(comboProductNo);
                        if (comboProductIdLong == null) {
                            throw exception(DROPSHIP_ASSIST_IMPORT_COMBO_PRODUCT_NOT_EXISTS, i + 1, comboProductNo);
                        }
                    }

                    // 判断是否支持更新
                    ErpDropshipAssistDO existDropshipAssist = existMap.get(importVO.getNo());
                    if (existDropshipAssist == null) {
                       // 创建 - 自动生成新的no编号
                       ErpDropshipAssistDO dropshipAssist = BeanUtils.toBean(importVO, ErpDropshipAssistDO.class);
                       dropshipAssist.setComboProductId(comboProductIdLong != null ? comboProductIdLong.toString() : null).setNo(noRedisDAO.generate(ErpNoRedisDAO.DROPSHIP_ASSIST_NO_PREFIX))
                               .setCreator(username)
                               .setCreateTime(now);

                       createList.add(dropshipAssist);
                       respVO.getCreateNames().add(dropshipAssist.getNo());
                    } else if (isUpdateSupport) {
                        // 更新
                        ErpDropshipAssistDO updateDropshipAssist = BeanUtils.toBean(importVO, ErpDropshipAssistDO.class);
                        updateDropshipAssist.setId(existDropshipAssist.getId())
                                .setComboProductId(comboProductIdLong != null ? comboProductIdLong.toString() : null)
                                .setCreator(existDropshipAssist.getCreator())        // 保留原始创建人
                                .setCreateTime(existDropshipAssist.getCreateTime());  // 保留原始创建时间

                        updateList.add(updateDropshipAssist);
                        respVO.getUpdateNames().add(updateDropshipAssist.getNo());
                    } else {
                        throw exception(DROPSHIP_ASSIST_IMPORT_NO_EXISTS_UPDATE_NOT_SUPPORT, i + 1, importVO.getNo());
                    }
                } catch (ServiceException ex) {
                    respVO.getFailureNames().put(errorKey, ex.getMessage());
                } catch (Exception ex) {
                    System.err.println("导入第" + (i + 1) + "行数据异常: " + ex.getMessage());
                    respVO.getFailureNames().put(errorKey, "系统异常: " + ex.getMessage());
                }
            }

            // 6. 批量保存到数据库
            try {
                if (CollUtil.isNotEmpty(createList)) {
                    dropshipAssistMapper.insertBatch(createList);
                }
                if (CollUtil.isNotEmpty(updateList)) {
                    updateList.forEach(dropshipAssistMapper::updateById);
                }
            } catch (Exception e) {
                System.err.println("批量操作数据库失败: " + e.getMessage());
                throw new RuntimeException("批量导入失败: " + e.getMessage(), e);
            }
        } catch (Exception ex) {
            respVO.getFailureNames().put("批量导入", "系统异常: " + ex.getMessage());
        } finally {
            // 清除转换错误
            ConversionErrorHolder.clearErrors();
        }

        System.out.println("导入完成，成功创建：" + respVO.getCreateNames().size() +
                          "，成功更新：" + respVO.getUpdateNames().size() +
                          "，失败：" + respVO.getFailureNames().size());
        return respVO;
    }

    /**
     * 统一校验所有导入数据（包括数据类型校验和业务逻辑校验）
     * 如果出现任何错误信息都记录下来并返回，后续操作就不进行了
     */
    private Map<String, String> validateAllImportData(List<ErpDropshipAssistImportExcelVO> importList, boolean isUpdateSupport) {
        Map<String, String> allErrors = new LinkedHashMap<>();

        // 1. 数据类型校验前置检查
        Map<String, String> dataTypeErrors = validateDataTypeErrors(importList);
        if (!dataTypeErrors.isEmpty()) {
            allErrors.putAll(dataTypeErrors);
            return allErrors; // 如果有数据类型错误，直接返回，不进行后续校验
        }

        // 2. 批量查询组品信息
        Set<String> comboProductNos = importList.stream()
                .map(ErpDropshipAssistImportExcelVO::getComboProductId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, Long> comboProductIdMap = comboProductNos.isEmpty() ? Collections.emptyMap() :
                convertMap(erpComboMapper.selectListByNoIn(comboProductNos), ErpComboProductDO::getNo, ErpComboProductDO::getId);

        // 3. 批量查询已存在的记录
        Set<String> noSet = importList.stream()
                .map(ErpDropshipAssistImportExcelVO::getNo)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ErpDropshipAssistDO> existMap = noSet.isEmpty() ? Collections.emptyMap() :
                convertMap(dropshipAssistMapper.selectListByNoIn(noSet), ErpDropshipAssistDO::getNo);

        // 用于跟踪Excel内部重复的编号
        Set<String> processedNos = new HashSet<>();
        // 用于跟踪Excel内部重复的组合字段
        Set<String> processedCombinations = new HashSet<>();

        // 4. 逐行校验业务逻辑
        for (int i = 0; i < importList.size(); i++) {
            ErpDropshipAssistImportExcelVO importVO = importList.get(i);
            String errorKey = "第" + (i + 1) + "行" + (StrUtil.isNotBlank(importVO.getOriginalProduct()) ? "(" + importVO.getOriginalProduct() + ")" : "");

            try {

                // 4.2 检查Excel内部编号重复
                if (StrUtil.isNotBlank(importVO.getNo())) {
                    if (processedNos.contains(importVO.getNo())) {
                        allErrors.put(errorKey, "代发辅助编号重复: " + importVO.getNo());
                        continue;
                    }
                    processedNos.add(importVO.getNo());
                }

                // 4.3 校验组品编号是否存在
                String comboProductId = importVO.getComboProductId();
                if (StrUtil.isNotBlank(comboProductId)) {
                    Long comboProductIdLong = comboProductIdMap.get(comboProductId);
                    if (comboProductIdLong == null) {
                        allErrors.put(errorKey, "组品编号不存在: " + comboProductId);
                        continue;
                    }
                }

                // 4.4 检查Excel内部组合字段重复
                String combination = buildCombinationKey(importVO.getOriginalProduct(), importVO.getOriginalSpec(),
                        importVO.getOriginalQuantity() != null ? importVO.getOriginalQuantity().toString() : "",
                        comboProductId, importVO.getProductSpec(),
                        importVO.getProductQuantity() != null ? importVO.getProductQuantity().toString() : "");
                if (processedCombinations.contains(combination)) {
                    allErrors.put(errorKey, "组合字段重复: " + combination);
                    continue;
                }
                processedCombinations.add(combination);

                // 4.5 数据转换校验（如果转换失败，记录错误并跳过）
                try {
                    ErpDropshipAssistDO dropshipAssist = convertImportVOToDO(importVO);
                    if (dropshipAssist == null) {
                        allErrors.put(errorKey, "数据转换失败");
                        continue;
                    }
                } catch (Exception ex) {
                    allErrors.put(errorKey, "数据转换异常: " + ex.getMessage());
                    continue;
                }

                // 4.6 判断是新增还是更新，并进行相应校验
                ErpDropshipAssistDO existDropshipAssist = existMap.get(importVO.getNo());
                if (existDropshipAssist == null) {
                    // 新增校验：校验组合字段唯一性
                    try {
                        validateCombinationUnique(importVO, null);
                    } catch (ServiceException ex) {
                        allErrors.put(errorKey, ex.getMessage());
                    }
                } else if (isUpdateSupport) {
                    // 更新校验：校验组合字段唯一性（排除自身）
                    try {
                        validateCombinationUnique(importVO, existDropshipAssist.getId());
                    } catch (ServiceException ex) {
                        allErrors.put(errorKey, ex.getMessage());
                    }
                } else {
                    allErrors.put(errorKey, "代发辅助编号不存在且不支持更新: " + importVO.getNo());
                }
            } catch (Exception ex) {
                allErrors.put(errorKey, "系统异常: " + ex.getMessage());
            }
        }

        return allErrors;
    }

    /**
     * 数据类型校验前置检查
     * 检查所有转换错误，如果有错误则返回错误信息，不进行后续导入
     */
    private Map<String, String> validateDataTypeErrors(List<ErpDropshipAssistImportExcelVO> importList) {
        Map<String, String> dataTypeErrors = new LinkedHashMap<>();

        // 检查是否有转换错误
        Map<Integer, List<ConversionErrorHolder.ConversionError>> allErrors = ConversionErrorHolder.getAllErrors();

        if (!allErrors.isEmpty()) {
            // 收集所有转换错误
            for (Map.Entry<Integer, List<ConversionErrorHolder.ConversionError>> entry : allErrors.entrySet()) {
                int rowIndex = entry.getKey();
                List<ConversionErrorHolder.ConversionError> errors = entry.getValue();

                // 获取原产品名称 - 修复行号索引问题
                String originalProduct = "未知原表产品+原表规格";
                // ConversionErrorHolder中的行号是从1开始的，数组索引是从0开始的
                // 所以需要减1来访问数组，但要确保索引有效
                int arrayIndex = rowIndex - 1;
                if (arrayIndex >= 0 && arrayIndex < importList.size()) {
                    ErpDropshipAssistImportExcelVO importVO = importList.get(arrayIndex);
                    if (StrUtil.isNotBlank(importVO.getOriginalProduct())) {
                        originalProduct = importVO.getOriginalProduct() +importVO.getOriginalSpec();
                    }
                }

                // 行号显示，RowIndexListener已经设置为从1开始，直接使用
                String errorKey = "第" + rowIndex + "行(" + originalProduct + ")";
                List<String> errorMessages = new ArrayList<>();

                for (ConversionErrorHolder.ConversionError error : errors) {
                    errorMessages.add(error.getErrorMessage());
                }

                String errorMsg = String.join("; ", errorMessages);
                dataTypeErrors.put(errorKey, "数据类型错误: " + errorMsg);
            }
        }

        return dataTypeErrors;
    }

    /**
     * 将导入VO转换为DO
     * 特别注意处理字段类型转换
     * 注意：此方法仅用于校验，不处理组品ID转换（组品ID转换在importDropshipAssistList方法中处理）
     */
    private ErpDropshipAssistDO convertImportVOToDO(ErpDropshipAssistImportExcelVO importVO) {
        if (importVO == null) {
            return null;
        }

        // 添加调试信息
        System.out.println("=== 代发辅助转换调试信息 ===");
        System.out.println("原产品: " + importVO.getOriginalProduct());
        System.out.println("组品编号: " + importVO.getComboProductId());
        System.out.println("原产品数量: " + importVO.getOriginalQuantity() + " (类型: " + (importVO.getOriginalQuantity() != null ? importVO.getOriginalQuantity().getClass().getSimpleName() : "null") + ")");
        System.out.println("产品数量: " + importVO.getProductQuantity() + " (类型: " + (importVO.getProductQuantity() != null ? importVO.getProductQuantity().getClass().getSimpleName() : "null") + ")");
        System.out.println("==================");

        // 使用BeanUtils进行基础转换
        ErpDropshipAssistDO dropshipAssist = BeanUtils.toBean(importVO, ErpDropshipAssistDO.class);

        // 手动设置转换器处理的字段，确保数据正确传递
        dropshipAssist.setOriginalQuantity(importVO.getOriginalQuantity());
        dropshipAssist.setProductQuantity(importVO.getProductQuantity());

        // 注意：组品ID转换在importDropshipAssistList方法中处理，这里保持原样用于校验

        // 添加转换后的调试信息
        System.out.println("=== 转换后调试信息 ===");
        System.out.println("原产品: " + dropshipAssist.getOriginalProduct());
        System.out.println("组品ID: " + dropshipAssist.getComboProductId());
        System.out.println("原产品数量: " + dropshipAssist.getOriginalQuantity() + " (类型: " + (dropshipAssist.getOriginalQuantity() != null ? dropshipAssist.getOriginalQuantity().getClass().getSimpleName() : "null") + ")");
        System.out.println("产品数量: " + dropshipAssist.getProductQuantity() + " (类型: " + (dropshipAssist.getProductQuantity() != null ? dropshipAssist.getProductQuantity().getClass().getSimpleName() : "null") + ")");
        System.out.println("==================");

        return dropshipAssist;
    }

    /**
     * 校验组合字段唯一性（通用方法，支持创建和更新）
     */
    private void validateCombinationUnique(ErpDropshipAssistImportExcelVO importVO, Long excludeId) {
        ErpDropshipAssistDO existingRecord = dropshipAssistMapper.selectByUniqueFields(
                importVO.getOriginalProduct(),
                importVO.getOriginalSpec(),
                importVO.getOriginalQuantity(),
                importVO.getComboProductId(), // 直接使用String类型
                importVO.getProductSpec(),
                importVO.getProductQuantity(),
                excludeId // 创建时为null，更新时为当前记录ID
        );
        if (existingRecord != null) {
            throw exception(DROPSHIP_ASSIST_FIELDS_DUPLICATE);
        }
    }

    /**
     * 构建组合字段的唯一键
     */
    private String buildCombinationKey(String originalProduct, String originalSpec, String originalQuantity,
                                     String comboProductId, String productSpec, String productQuantity) {
        return String.format("%s|%s|%s|%s|%s|%s",
                originalProduct, originalSpec, originalQuantity,
                comboProductId != null ? comboProductId : "", productSpec, productQuantity);
    }

}
