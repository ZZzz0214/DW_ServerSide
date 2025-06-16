package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - ERP 私播信息新增/修改 Request VO")
@Data
public class ErpPrivateBroadcastingInfoSaveReqVO {

    @Schema(description = "私播信息编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SBXX001")
    private String no;

    @Schema(description = "客户姓名", example = "张三")
    private String customerName;

    @Schema(description = "客户职位", example = "主播")
    private String customerPosition;

    @Schema(description = "客户微信", example = "zhangsan")
    private String customerWechat;

    @Schema(description = "平台名称", example = "抖音")
    private String platformName;

    @Schema(description = "客户属性", example = "KOL")
    private String customerAttribute;

    @Schema(description = "客户城市", example = "北京")
    private String customerCity;

    @Schema(description = "客户区县", example = "朝阳区")
    private String customerDistrict;

    @Schema(description = "用户画像", example = "时尚达人")
    private String userPortrait;

    @Schema(description = "招商类目", example = "美妆")
    private String recruitmentCategory;

    @Schema(description = "选品标准", example = "国货品牌")
    private String selectionCriteria;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;
}
