package cn.iocoder.yudao.module.erp.controller.admin.livebroadcastinginfo.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 直播信息新增/修改 Request VO")
@Data
public class ErpLiveBroadcastingInfoSaveReqVO {

    @Schema(description = "直播信息编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ZBXX001")
    private String no;

    @Schema(description = "客户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @Schema(description = "客户职位", example = "运营总监")
    private String customerPosition;

    @Schema(description = "客户微信", example = "wechat123")
    private String customerWechat;

    @Schema(description = "平台名称", example = "抖音")
    private String platformName;

    @Schema(description = "客户属性", example = "KA客户")
    private String customerAttribute;

    @Schema(description = "客户城市", example = "北京")
    private String customerCity;

    @Schema(description = "客户区县", example = "朝阳区")
    private String customerDistrict;

    @Schema(description = "用户画像", example = "25-35岁女性")
    private String userPortrait;

    @Schema(description = "招商类目", example = "美妆个护")
    private String recruitmentCategory;

    @Schema(description = "选品标准", example = "客单价100-300元")
    private String selectionCriteria;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;
}