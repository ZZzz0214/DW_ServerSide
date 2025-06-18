package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 产品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductPageReqVO extends PageParam {
    private Long lastId; // 上一页最后一条记录的ID

    @Schema(description = "产品名称", example = "李四")
    private String name;

    @Schema(description = "产品简称", example = "简称")
    private String productShortName;

    @Schema(description = "发货编码", example = "SH001")
    private String shippingCode;

    @Schema(description = "品牌名称", example = "品牌A")
    private String brand;

    @Schema(description = "产品分类编号", example = "11161")
    private Long categoryId;

    @Schema(description = "产品状态", example = "1")
    private Integer status;

    @Schema(description = "采购人员", example = "张三")
    private String purchaser;

    @Schema(description = "供应商名", example = "供应商A")
    private String supplier;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "全文搜索关键词", example = "搜索关键词")
    private String keyword;

}
