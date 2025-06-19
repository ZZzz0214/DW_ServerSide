package cn.iocoder.yudao.module.erp.controller.admin.sample.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 样品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpSamplePageReqVO extends PageParam {

    @Schema(description = "编号", example = "SP001")
    private String no;

    @Schema(description = "物流公司", example = "顺丰快递")
    private String logisticsCompany;

    @Schema(description = "物流单号", example = "SF123456789")
    private String logisticsNo;

    @Schema(description = "收件姓名", example = "张三")
    private String receiverName;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "组品编号", example = "CB001")
    private String comboProductId;

    @Schema(description = "发货编码", example = "SH001")
    private String shippingCode;

    @Schema(description = "产品名称", example = "组合产品A")
    private String productName;

    @Schema(description = "产品规格", example = "规格A")
    private String productSpec;

    @Schema(description = "客户名称", example = "张三")
    private String customerName;

    @Schema(description = "样品状态", example = "1")
    private Integer sampleStatus;

    @Schema(description = "创建人员", example = "admin")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}