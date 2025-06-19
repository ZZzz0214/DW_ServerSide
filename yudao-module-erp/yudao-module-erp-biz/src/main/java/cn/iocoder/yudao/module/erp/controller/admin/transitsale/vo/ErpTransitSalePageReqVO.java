package cn.iocoder.yudao.module.erp.controller.admin.transitsale.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 中转销售分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpTransitSalePageReqVO extends PageParam {

    @Schema(description = "编号", example = "TS001")
    private String no;

    @Schema(description = "组品编号", example = "1")
    private Long groupProductId;

    @Schema(description = "产品名称", example = "测试产品名称")
    private String productName;

    @Schema(description = "产品简称", example = "测试简称")
    private String productShortName;

    @Schema(description = "中转人员", example = "张三")
    private String transitPerson;

    @Schema(description = "代发单价", example = "10.50")
    private BigDecimal distributionPrice;

    @Schema(description = "批发单价", example = "8.50")
    private BigDecimal wholesalePrice;

    @Schema(description = "创建人员", example = "admin")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
