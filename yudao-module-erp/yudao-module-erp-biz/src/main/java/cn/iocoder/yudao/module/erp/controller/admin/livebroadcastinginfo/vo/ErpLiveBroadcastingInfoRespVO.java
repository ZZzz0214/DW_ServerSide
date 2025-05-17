package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 直播信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpLiveBroadcastingInfoRespVO {

    @Schema(description = "直播信息编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("直播信息编号")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ZBXX001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "客户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("客户ID")
    private Long customerId;

    @Schema(description = "客户职位", example = "运营总监")
    @ExcelProperty("客户职位")
    private String customerPosition;

    @Schema(description = "客户微信", example = "wechat123")
    @ExcelProperty("客户微信")
    private String customerWechat;

    @Schema(description = "平台名称", example = "抖音")
    @ExcelProperty("平台名称")
    private String platformName;

    @Schema(description = "客户属性", example = "KA客户")
    @ExcelProperty("客户属性")
    private String customerAttribute;

    @Schema(description = "客户城市", example = "北京")
    @ExcelProperty("客户城市")
    private String customerCity;

    @Schema(description = "客户区县", example = "朝阳区")
    @ExcelProperty("客户区县")
    private String customerDistrict;

    @Schema(description = "用户画像", example = "25-35岁女性")
    @ExcelProperty("用户画像")
    private String userPortrait;

    @Schema(description = "招商类目", example = "美妆个护")
    @ExcelProperty("招商类目")
    private String recruitmentCategory;

    @Schema(description = "选品标准", example = "客单价100-300元")
    @ExcelProperty("选品标准")
    private String selectionCriteria;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}