package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 代发批发统计请求 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 代发批发统计请求 VO")
@Data
public class ErpDistributionWholesaleStatisticsReqVO {

    @Schema(description = "统计类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "purchaser")
    @NotNull(message = "统计类型不能为空")
    private String statisticsType; // purchaser(采购人员), supplier(供应商), salesperson(销售人员), customer(客户)

    @Schema(description = "搜索关键词", example = "张三")
    private String searchKeyword; // 用于搜索具体的人员或客户名称

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始时间不能为空")
    private String beginTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    private String endTime;

    @Override
    public String toString() {
        return "ErpDistributionWholesaleStatisticsReqVO{" +
                "statisticsType='" + statisticsType + '\'' +
                ", searchKeyword='" + searchKeyword + '\'' +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                '}';
    }
} 