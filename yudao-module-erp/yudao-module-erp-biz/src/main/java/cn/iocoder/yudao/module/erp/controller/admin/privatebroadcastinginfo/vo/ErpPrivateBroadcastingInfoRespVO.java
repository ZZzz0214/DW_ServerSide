package cn.iocoder.yudao.module.erp.controller.admin.privatebroadcastinginfo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 私播信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPrivateBroadcastingInfoRespVO {

    @Schema(description = "私播信息编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("私播信息编号")
    private Long id;

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SBXX001")
    @ExcelProperty("编号")
    private String no;

    @Schema(description = "客户姓名", example = "张三")
    @ExcelProperty("客户姓名")
    private String customerName;

    @Schema(description = "客户职位", example = "主播")
    @ExcelProperty("客户职位")
    private String customerPosition;

    @Schema(description = "客户微信", example = "zhangsan")
    @ExcelProperty("客户微信")
    private String customerWechat;

    @Schema(description = "平台名称", example = "抖音")
    @ExcelProperty("平台名称")
    private String platformName;

    @Schema(description = "客户属性", example = "KOL")
    @ExcelProperty("客户属性")
    private String customerAttribute;

    @Schema(description = "客户城市", example = "北京")
    @ExcelProperty("客户城市")
    private String customerCity;

    @Schema(description = "客户区县", example = "朝阳区")
    @ExcelProperty("客户区县")
    private String customerDistrict;

    @Schema(description = "用户画像", example = "时尚达人")
    @ExcelProperty("用户画像")
    private String userPortrait;

    @Schema(description = "招商类目", example = "美妆")
    @ExcelProperty("招商类目")
    private String recruitmentCategory;

    @Schema(description = "选品标准", example = "国货品牌")
    @ExcelProperty("选品标准")
    private String selectionCriteria;

    @Schema(description = "备注信息", example = "备注内容")
    @ExcelProperty("备注信息")
    private String remark;

    @Schema(description = "创建者")
    @ExcelProperty("创建人员")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
