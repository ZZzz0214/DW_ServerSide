

package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 私播信息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpPrivateBroadcastingInfoPageReqVO extends PageParam {

    @Schema(description = "编号", example = "SBXX001")
    private String no;

    @Schema(description = "客户姓名", example = "张三")
    private String customerName;

    @Schema(description = "平台名称", example = "抖音")
    private String platformName;

    @Schema(description = "客户城市", example = "北京")
    private String customerCity;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}