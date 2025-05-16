package cn.iocoder.yudao.module.erp.controller.admin.groupbuyinginfo.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 团购信息新增/修改 Request VO")
@Data
public class ErpGroupBuyingInfoSaveReqVO {

    @Schema(description = "团购信息编号", example = "1")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "GBI001")
    private String no;

    @Schema(description = "客户姓名", example = "张三")
    private String customerName;

    @Schema(description = "客户职位", example = "采购经理")
    private String customerPosition;

    @Schema(description = "客户微信", example = "zhangsan123")
    private String customerWechat;

    @Schema(description = "平台名称", example = "美团")
    private String platformName;

    @Schema(description = "客户属性", example = "KA客户")
    private String customerAttribute;

    @Schema(description = "客户城市", example = "北京市")
    private String customerCity;

    @Schema(description = "客户区县", example = "朝阳区")
    private String customerDistrict;

    @Schema(description = "用户画像", example = "高端用户")
    private String userPortrait;

    @Schema(description = "招商类目", example = "食品饮料")
    private String recruitmentCategory;

    @Schema(description = "选品标准", example = "高毛利")
    private String selectionCriteria;

    @Schema(description = "备注信息", example = "备注内容")
    private String remark;
}