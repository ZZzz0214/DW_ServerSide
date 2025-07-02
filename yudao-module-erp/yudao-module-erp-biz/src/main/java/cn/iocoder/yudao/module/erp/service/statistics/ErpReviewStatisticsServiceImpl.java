package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.review.ErpReviewStatisticsReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.review.ErpReviewStatisticsRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.groupbuying.ErpGroupBuyingReviewDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcasting.ErpLiveBroadcastingDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.livebroadcastingreview.ErpLiveBroadcastingReviewDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcasting.ErpPrivateBroadcastingDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.privatebroadcastingreview.ErpPrivateBroadcastingReviewDO;
import cn.iocoder.yudao.module.erp.dal.mysql.groupbuying.ErpGroupBuyingMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.groupbuyingreview.ErpGroupBuyingReviewMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.livebroadcasting.ErpLiveBroadcastingMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.livebroadcastingreview.ErpLiveBroadcastingReviewMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcasting.ErpPrivateBroadcastingMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.privatebroadcastingreview.ErpPrivateBroadcastingReviewMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ERP 复盘统计 Service 实现类
 */
@Service
@Validated
@Slf4j
public class ErpReviewStatisticsServiceImpl implements ErpReviewStatisticsService {

    @Resource
    private ErpGroupBuyingReviewMapper groupBuyingReviewMapper;

    @Resource
    private ErpGroupBuyingMapper groupBuyingMapper;

    @Resource
    private ErpPrivateBroadcastingReviewMapper privateBroadcastingReviewMapper;

    @Resource
    private ErpPrivateBroadcastingMapper privateBroadcastingMapper;

    @Resource
    private ErpLiveBroadcastingReviewMapper liveBroadcastingReviewMapper;

    @Resource
    private ErpLiveBroadcastingMapper liveBroadcastingMapper;

    @Override
    public ErpReviewStatisticsRespVO getReviewStatistics(ErpReviewStatisticsReqVO reqVO) {
        ErpReviewStatisticsRespVO respVO = new ErpReviewStatisticsRespVO();

        // 获取团购复盘统计
        respVO.setGroupBuyingStats(getGroupBuyingReviewStats(reqVO));

        // 获取私播复盘统计
        respVO.setPrivateBroadcastingStats(getPrivateBroadcastingReviewStats(reqVO));

        // 获取直播复盘统计
        respVO.setLiveBroadcastingStats(getLiveBroadcastingReviewStats(reqVO));

        // 获取产品选项
        respVO.setProductOptions(getProductOptions(reqVO));

        // 设置分页信息
        respVO.setPageNo(reqVO.getPageNo());
        respVO.setPageSize(reqVO.getPageSize());
        
        // 计算总条数
        long total = respVO.getGroupBuyingStats().size() + 
                    respVO.getPrivateBroadcastingStats().size() + 
                    respVO.getLiveBroadcastingStats().size();
        respVO.setTotal(total);

        return respVO;
    }

    /**
     * 获取团购复盘统计
     */
    private List<ErpReviewStatisticsRespVO.GroupBuyingReviewStat> getGroupBuyingReviewStats(ErpReviewStatisticsReqVO reqVO) {
        LambdaQueryWrapper<ErpGroupBuyingReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(ErpGroupBuyingReviewDO::getCreateTime, LocalDate.parse(reqVO.getBeginDate()).atStartOfDay(), LocalDate.parse(reqVO.getEndDate()).atTime(23, 59, 59));
        List<ErpGroupBuyingReviewDO> reviews = groupBuyingReviewMapper.selectList(wrapper);

        // 获取相关的团购货盘数据
        Set<String> groupBuyingIds = reviews.stream()
                .map(ErpGroupBuyingReviewDO::getGroupBuyingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 修复：直接初始化Map，避免重新赋值
        final Map<String, ErpGroupBuyingDO> groupBuyingMap;
        if (!groupBuyingIds.isEmpty()) {
            LambdaQueryWrapper<ErpGroupBuyingDO> gbWrapper = new LambdaQueryWrapper<>();
            gbWrapper.in(ErpGroupBuyingDO::getNo, groupBuyingIds);
            List<ErpGroupBuyingDO> groupBuyings = groupBuyingMapper.selectList(gbWrapper);
            groupBuyingMap = groupBuyings.stream()
                    .collect(Collectors.toMap(ErpGroupBuyingDO::getNo, gb -> gb));
        } else {
            groupBuyingMap = new HashMap<>();
        }

        // 按产品名称分组统计
        Map<String, List<ErpGroupBuyingReviewDO>> productGroups = reviews.stream()
                .collect(Collectors.groupingBy(review -> {
                    ErpGroupBuyingDO groupBuying = groupBuyingMap.get(review.getGroupBuyingId());
                    return groupBuying != null ? groupBuying.getProductName() : "未知产品";
                }));

        List<ErpReviewStatisticsRespVO.GroupBuyingReviewStat> stats = productGroups.entrySet().stream()
                .map(entry -> {
                    String productName = entry.getKey();
                    List<ErpGroupBuyingReviewDO> productReviews = entry.getValue();

                    ErpReviewStatisticsRespVO.GroupBuyingReviewStat stat = new ErpReviewStatisticsRespVO.GroupBuyingReviewStat();
                    stat.setProductName(productName);

                    // 寄样统计
                    long sampleSendCount = productReviews.stream()
                            .filter(review -> review.getSampleSendDate() != null)
                            .count();
                    stat.setSampleSendCount((int) sampleSendCount);
                    stat.setNotSampleSendCount(productReviews.size() - (int) sampleSendCount);

                    // 获取寄样日期（取最早的）
                    Optional<ErpGroupBuyingReviewDO> sampleSendReview = productReviews.stream()
                            .filter(review -> review.getSampleSendDate() != null)
                            .min(Comparator.comparing(ErpGroupBuyingReviewDO::getSampleSendDate));
                    sampleSendReview.ifPresent(review -> stat.setSampleSendDate(review.getSampleSendDate()));

                    // 开团统计
                    long groupStartCount = productReviews.stream()
                            .filter(review -> review.getGroupStartDate() != null)
                            .count();
                    stat.setGroupStartCount((int) groupStartCount);
                    stat.setNotGroupStartCount(productReviews.size() - (int) groupStartCount);

                    // 获取开团日期（取最早的）
                    Optional<ErpGroupBuyingReviewDO> groupStartReview = productReviews.stream()
                            .filter(review -> review.getGroupStartDate() != null)
                            .min(Comparator.comparing(ErpGroupBuyingReviewDO::getGroupStartDate));
                    groupStartReview.ifPresent(review -> stat.setGroupStartDate(review.getGroupStartDate()));

                    // 复团统计
                    long repeatGroupCount = productReviews.stream()
                            .filter(review -> review.getRepeatGroupDate() != null)
                            .count();
                    stat.setRepeatGroupCount((int) repeatGroupCount);
                    stat.setNotRepeatGroupCount(productReviews.size() - (int) repeatGroupCount);

                    // 获取复团日期（取最早的）
                    Optional<ErpGroupBuyingReviewDO> repeatGroupReview = productReviews.stream()
                            .filter(review -> review.getRepeatGroupDate() != null)
                            .min(Comparator.comparing(ErpGroupBuyingReviewDO::getRepeatGroupDate));
                    repeatGroupReview.ifPresent(review -> stat.setRepeatGroupDate(review.getRepeatGroupDate()));

                    return stat;
                })
                .collect(Collectors.toList());

        // 产品名称过滤
        if (reqVO.getProductName() != null && !reqVO.getProductName().trim().isEmpty()) {
            stats = stats.stream()
                    .filter(stat -> stat.getProductName().contains(reqVO.getProductName().trim()))
                    .collect(Collectors.toList());
        }

        // 分页处理
        int startIndex = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        int endIndex = Math.min(startIndex + reqVO.getPageSize(), stats.size());
        
        if (startIndex < stats.size()) {
            return stats.subList(startIndex, endIndex);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 获取私播复盘统计
     */
    private List<ErpReviewStatisticsRespVO.PrivateBroadcastingReviewStat> getPrivateBroadcastingReviewStats(ErpReviewStatisticsReqVO reqVO) {
        LambdaQueryWrapper<ErpPrivateBroadcastingReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(ErpPrivateBroadcastingReviewDO::getCreateTime, LocalDate.parse(reqVO.getBeginDate()).atStartOfDay(), LocalDate.parse(reqVO.getEndDate()).atTime(23, 59, 59));
        List<ErpPrivateBroadcastingReviewDO> reviews = privateBroadcastingReviewMapper.selectList(wrapper);

        // 获取相关的私播货盘数据
        Set<Long> privateBroadcastingIds = reviews.stream()
                .map(ErpPrivateBroadcastingReviewDO::getPrivateBroadcastingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 修复：直接初始化Map，避免重新赋值
        final Map<Long, ErpPrivateBroadcastingDO> privateBroadcastingMap;
        if (!privateBroadcastingIds.isEmpty()) {
            LambdaQueryWrapper<ErpPrivateBroadcastingDO> pbWrapper = new LambdaQueryWrapper<>();
            pbWrapper.in(ErpPrivateBroadcastingDO::getId, privateBroadcastingIds);
            List<ErpPrivateBroadcastingDO> privateBroadcastings = privateBroadcastingMapper.selectList(pbWrapper);
            privateBroadcastingMap = privateBroadcastings.stream()
                    .collect(Collectors.toMap(ErpPrivateBroadcastingDO::getId, pb -> pb));
        } else {
            privateBroadcastingMap = new HashMap<>();
        }

        // 按产品名称分组统计
        Map<String, List<ErpPrivateBroadcastingReviewDO>> productGroups = reviews.stream()
                .collect(Collectors.groupingBy(review -> {
                    ErpPrivateBroadcastingDO privateBroadcasting = privateBroadcastingMap.get(review.getPrivateBroadcastingId());
                    return privateBroadcasting != null ? privateBroadcasting.getProductName() : "未知产品";
                }));

        List<ErpReviewStatisticsRespVO.PrivateBroadcastingReviewStat> stats = productGroups.entrySet().stream()
                .map(entry -> {
                    String productName = entry.getKey();
                    List<ErpPrivateBroadcastingReviewDO> productReviews = entry.getValue();

                    ErpReviewStatisticsRespVO.PrivateBroadcastingReviewStat stat = new ErpReviewStatisticsRespVO.PrivateBroadcastingReviewStat();
                    stat.setProductName(productName);

                    // 寄样统计
                    long sampleSendCount = productReviews.stream()
                            .filter(review -> review.getSampleSendDate() != null)
                            .count();
                    stat.setSampleSendCount((int) sampleSendCount);
                    stat.setNotSampleSendCount(productReviews.size() - (int) sampleSendCount);

                    // 获取寄样日期（取最早的）
                    Optional<ErpPrivateBroadcastingReviewDO> sampleSendReview = productReviews.stream()
                            .filter(review -> review.getSampleSendDate() != null)
                            .min(Comparator.comparing(ErpPrivateBroadcastingReviewDO::getSampleSendDate));
                    sampleSendReview.ifPresent(review -> stat.setSampleSendDate(review.getSampleSendDate()));

                    // 开团统计
                    long groupStartCount = productReviews.stream()
                            .filter(review -> review.getGroupStartDate() != null)
                            .count();
                    stat.setGroupStartCount((int) groupStartCount);
                    stat.setNotGroupStartCount(productReviews.size() - (int) groupStartCount);

                    // 获取开团日期（取最早的）
                    Optional<ErpPrivateBroadcastingReviewDO> groupStartReview = productReviews.stream()
                            .filter(review -> review.getGroupStartDate() != null)
                            .min(Comparator.comparing(ErpPrivateBroadcastingReviewDO::getGroupStartDate));
                    groupStartReview.ifPresent(review -> stat.setGroupStartDate(review.getGroupStartDate()));

                    // 复团统计
                    long repeatGroupCount = productReviews.stream()
                            .filter(review -> review.getRepeatGroupDate() != null)
                            .count();
                    stat.setRepeatGroupCount((int) repeatGroupCount);
                    stat.setNotRepeatGroupCount(productReviews.size() - (int) repeatGroupCount);

                    // 获取复团日期（取最早的）
                    Optional<ErpPrivateBroadcastingReviewDO> repeatGroupReview = productReviews.stream()
                            .filter(review -> review.getRepeatGroupDate() != null)
                            .min(Comparator.comparing(ErpPrivateBroadcastingReviewDO::getRepeatGroupDate));
                    repeatGroupReview.ifPresent(review -> stat.setRepeatGroupDate(review.getRepeatGroupDate()));

                    return stat;
                })
                .collect(Collectors.toList());

        // 产品名称过滤
        if (reqVO.getProductName() != null && !reqVO.getProductName().trim().isEmpty()) {
            stats = stats.stream()
                    .filter(stat -> stat.getProductName().contains(reqVO.getProductName().trim()))
                    .collect(Collectors.toList());
        }

        // 分页处理
        int startIndex = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        int endIndex = Math.min(startIndex + reqVO.getPageSize(), stats.size());
        
        if (startIndex < stats.size()) {
            return stats.subList(startIndex, endIndex);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 获取直播复盘统计
     */
    private List<ErpReviewStatisticsRespVO.LiveBroadcastingReviewStat> getLiveBroadcastingReviewStats(ErpReviewStatisticsReqVO reqVO) {
        LambdaQueryWrapper<ErpLiveBroadcastingReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(ErpLiveBroadcastingReviewDO::getCreateTime, LocalDate.parse(reqVO.getBeginDate()).atStartOfDay(), LocalDate.parse(reqVO.getEndDate()).atTime(23, 59, 59));
        List<ErpLiveBroadcastingReviewDO> reviews = liveBroadcastingReviewMapper.selectList(wrapper);

        // 获取相关的直播货盘数据
        Set<Long> liveBroadcastingIds = reviews.stream()
                .map(ErpLiveBroadcastingReviewDO::getLiveBroadcastingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 修复：直接初始化Map，避免重新赋值
        final Map<Long, ErpLiveBroadcastingDO> liveBroadcastingMap;
        if (!liveBroadcastingIds.isEmpty()) {
            LambdaQueryWrapper<ErpLiveBroadcastingDO> lbWrapper = new LambdaQueryWrapper<>();
            lbWrapper.in(ErpLiveBroadcastingDO::getId, liveBroadcastingIds);
            List<ErpLiveBroadcastingDO> liveBroadcastings = liveBroadcastingMapper.selectList(lbWrapper);
            liveBroadcastingMap = liveBroadcastings.stream()
                    .collect(Collectors.toMap(ErpLiveBroadcastingDO::getId, lb -> lb));
        } else {
            liveBroadcastingMap = new HashMap<>();
        }

        // 按产品名称分组统计
        Map<String, List<ErpLiveBroadcastingReviewDO>> productGroups = reviews.stream()
                .collect(Collectors.groupingBy(review -> {
                    ErpLiveBroadcastingDO liveBroadcasting = liveBroadcastingMap.get(review.getLiveBroadcastingId());
                    return liveBroadcasting != null ? liveBroadcasting.getProductName() : "未知产品";
                }));

        List<ErpReviewStatisticsRespVO.LiveBroadcastingReviewStat> stats = productGroups.entrySet().stream()
                .map(entry -> {
                    String productName = entry.getKey();
                    List<ErpLiveBroadcastingReviewDO> productReviews = entry.getValue();

                    ErpReviewStatisticsRespVO.LiveBroadcastingReviewStat stat = new ErpReviewStatisticsRespVO.LiveBroadcastingReviewStat();
                    stat.setProductName(productName);

                    // 寄样统计
                    long sampleSendCount = productReviews.stream()
                            .filter(review -> review.getSampleSendDate() != null)
                            .count();
                    stat.setSampleSendCount((int) sampleSendCount);
                    stat.setNotSampleSendCount(productReviews.size() - (int) sampleSendCount);

                    // 获取寄样日期（取最早的）
                    Optional<ErpLiveBroadcastingReviewDO> sampleSendReview = productReviews.stream()
                            .filter(review -> review.getSampleSendDate() != null)
                            .min(Comparator.comparing(ErpLiveBroadcastingReviewDO::getSampleSendDate));
                    sampleSendReview.ifPresent(review -> stat.setSampleSendDate(review.getSampleSendDate()));

                    // 开团统计（直播复盘中是开播）
                    long groupStartCount = productReviews.stream()
                            .filter(review -> review.getLiveStartDate() != null)
                            .count();
                    stat.setGroupStartCount((int) groupStartCount);
                    stat.setNotGroupStartCount(productReviews.size() - (int) groupStartCount);

                    // 获取开团日期（取最早的）
                    Optional<ErpLiveBroadcastingReviewDO> groupStartReview = productReviews.stream()
                            .filter(review -> review.getLiveStartDate() != null)
                            .min(Comparator.comparing(ErpLiveBroadcastingReviewDO::getLiveStartDate));
                    groupStartReview.ifPresent(review -> stat.setGroupStartDate(review.getLiveStartDate()));

                    // 复团统计（直播复盘中是复播）
                    long repeatGroupCount = productReviews.stream()
                            .filter(review -> review.getRepeatLiveDate() != null)
                            .count();
                    stat.setRepeatGroupCount((int) repeatGroupCount);
                    stat.setNotRepeatGroupCount(productReviews.size() - (int) repeatGroupCount);

                    // 获取复团日期（取最早的）
                    Optional<ErpLiveBroadcastingReviewDO> repeatGroupReview = productReviews.stream()
                            .filter(review -> review.getRepeatLiveDate() != null)
                            .min(Comparator.comparing(ErpLiveBroadcastingReviewDO::getRepeatLiveDate));
                    repeatGroupReview.ifPresent(review -> stat.setRepeatGroupDate(review.getRepeatLiveDate()));

                    return stat;
                })
                .collect(Collectors.toList());

        // 产品名称过滤
        if (reqVO.getProductName() != null && !reqVO.getProductName().trim().isEmpty()) {
            stats = stats.stream()
                    .filter(stat -> stat.getProductName().contains(reqVO.getProductName().trim()))
                    .collect(Collectors.toList());
        }

        // 分页处理
        int startIndex = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        int endIndex = Math.min(startIndex + reqVO.getPageSize(), stats.size());
        
        if (startIndex < stats.size()) {
            return stats.subList(startIndex, endIndex);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 获取产品选项
     */
    private List<ErpReviewStatisticsRespVO.ProductOption> getProductOptions(ErpReviewStatisticsReqVO reqVO) {
        Set<String> productNames = new HashSet<>();

        // 获取团购复盘中的产品名称
        LambdaQueryWrapper<ErpGroupBuyingReviewDO> gbWrapper = new LambdaQueryWrapper<>();
        gbWrapper.between(ErpGroupBuyingReviewDO::getCreateTime, LocalDate.parse(reqVO.getBeginDate()).atStartOfDay(), LocalDate.parse(reqVO.getEndDate()).atTime(23, 59, 59));
        List<ErpGroupBuyingReviewDO> groupBuyingReviews = groupBuyingReviewMapper.selectList(gbWrapper);
        
        Set<String> groupBuyingIds = groupBuyingReviews.stream()
                .map(ErpGroupBuyingReviewDO::getGroupBuyingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if (!groupBuyingIds.isEmpty()) {
            LambdaQueryWrapper<ErpGroupBuyingDO> gbProductWrapper = new LambdaQueryWrapper<>();
            gbProductWrapper.in(ErpGroupBuyingDO::getNo, groupBuyingIds);
            List<ErpGroupBuyingDO> groupBuyings = groupBuyingMapper.selectList(gbProductWrapper);
            groupBuyings.stream()
                    .map(ErpGroupBuyingDO::getProductName)
                    .filter(Objects::nonNull)
                    .forEach(productNames::add);
        }

        // 获取私播复盘中的产品名称
        LambdaQueryWrapper<ErpPrivateBroadcastingReviewDO> pbWrapper = new LambdaQueryWrapper<>();
        pbWrapper.between(ErpPrivateBroadcastingReviewDO::getCreateTime, LocalDate.parse(reqVO.getBeginDate()).atStartOfDay(), LocalDate.parse(reqVO.getEndDate()).atTime(23, 59, 59));
        List<ErpPrivateBroadcastingReviewDO> privateBroadcastingReviews = privateBroadcastingReviewMapper.selectList(pbWrapper);
        
        Set<Long> privateBroadcastingIds = privateBroadcastingReviews.stream()
                .map(ErpPrivateBroadcastingReviewDO::getPrivateBroadcastingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if (!privateBroadcastingIds.isEmpty()) {
            LambdaQueryWrapper<ErpPrivateBroadcastingDO> pbProductWrapper = new LambdaQueryWrapper<>();
            pbProductWrapper.in(ErpPrivateBroadcastingDO::getId, privateBroadcastingIds);
            List<ErpPrivateBroadcastingDO> privateBroadcastings = privateBroadcastingMapper.selectList(pbProductWrapper);
            privateBroadcastings.stream()
                    .map(ErpPrivateBroadcastingDO::getProductName)
                    .filter(Objects::nonNull)
                    .forEach(productNames::add);
        }

        // 获取直播复盘中的产品名称
        LambdaQueryWrapper<ErpLiveBroadcastingReviewDO> lbWrapper = new LambdaQueryWrapper<>();
        lbWrapper.between(ErpLiveBroadcastingReviewDO::getCreateTime, LocalDate.parse(reqVO.getBeginDate()).atStartOfDay(), LocalDate.parse(reqVO.getEndDate()).atTime(23, 59, 59));
        List<ErpLiveBroadcastingReviewDO> liveBroadcastingReviews = liveBroadcastingReviewMapper.selectList(lbWrapper);
        
        Set<Long> liveBroadcastingIds = liveBroadcastingReviews.stream()
                .map(ErpLiveBroadcastingReviewDO::getLiveBroadcastingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if (!liveBroadcastingIds.isEmpty()) {
            LambdaQueryWrapper<ErpLiveBroadcastingDO> lbProductWrapper = new LambdaQueryWrapper<>();
            lbProductWrapper.in(ErpLiveBroadcastingDO::getId, liveBroadcastingIds);
            List<ErpLiveBroadcastingDO> liveBroadcastings = liveBroadcastingMapper.selectList(lbProductWrapper);
            liveBroadcastings.stream()
                    .map(ErpLiveBroadcastingDO::getProductName)
                    .filter(Objects::nonNull)
                    .forEach(productNames::add);
        }

        return productNames.stream()
                .map(productName -> {
                    ErpReviewStatisticsRespVO.ProductOption option = new ErpReviewStatisticsRespVO.ProductOption();
                    option.setProductName(productName);
                    
                    // 计算该产品的复盘数量
                    long groupBuyingCount = groupBuyingReviews.stream()
                            .filter(review -> {
                                ErpGroupBuyingDO groupBuying = groupBuyingMapper.selectOne(
                                    new LambdaQueryWrapper<ErpGroupBuyingDO>()
                                        .eq(ErpGroupBuyingDO::getNo, review.getGroupBuyingId())
                                );
                                return groupBuying != null && Objects.equals(groupBuying.getProductName(), productName);
                            })
                            .count();
                    
                    long privateBroadcastingCount = privateBroadcastingReviews.stream()
                            .filter(review -> {
                                ErpPrivateBroadcastingDO privateBroadcasting = privateBroadcastingMapper.selectById(review.getPrivateBroadcastingId());
                                return privateBroadcasting != null && Objects.equals(privateBroadcasting.getProductName(), productName);
                            })
                            .count();
                    
                    long liveBroadcastingCount = liveBroadcastingReviews.stream()
                            .filter(review -> {
                                ErpLiveBroadcastingDO liveBroadcasting = liveBroadcastingMapper.selectById(review.getLiveBroadcastingId());
                                return liveBroadcasting != null && Objects.equals(liveBroadcasting.getProductName(), productName);
                            })
                            .count();
                    
                    option.setReviewCount((int) (groupBuyingCount + privateBroadcastingCount + liveBroadcastingCount));
                    return option;
                })
                .filter(option -> option.getReviewCount() > 0)
                .sorted(Comparator.comparing(ErpReviewStatisticsRespVO.ProductOption::getReviewCount).reversed())
                .collect(Collectors.toList());
    }
}
