package cn.iocoder.yudao.module.erp.controller.admin.distribution.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 代发分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpDistributionPageReqVO extends PageParam {

    @Schema(description = "订单编号", example = "DF202403250001")
    private String no;

    @Schema(description = "订单号", example = "ORD-001")
    private String orderNumber;

    @Schema(description = "物流公司", example = "顺丰速运")
    private String logisticsCompany;

    @Schema(description = "物流公司为空筛选", example = "true")
    private Boolean logisticsCompanyEmpty;

    @Schema(description = "物流单号", example = "SF1234567890")
    private String trackingNumber;

    @Schema(description = "物流单号为空筛选", example = "true")
    private Boolean trackingNumberEmpty;

    @Schema(description = "收件人姓名", example = "张三")
    private String receiverName;

    @Schema(description = "联系电话", example = "13800138000")
    private String receiverPhone;

    @Schema(description = "详细地址", example = "广东省深圳市南山区")
    private String receiverAddress;

    @Schema(description = "原表商品")
    private String originalProduct;

    @Schema(description = "原表规格")
    private String originalSpecification;

    @Schema(description = "组品编号", example = "ZP202403250001")
    private String comboProductNo;

    @Schema(description = "发货编码", example = "FH202403250001")
    private String shippingCode;

    @Schema(description = "产品名称", example = "苹果手机")
    private String productName;

    @Schema(description = "产品规格", example = "128GB")
    private String productSpecification;

    @Schema(description = "售后状况", example = "正常")
    private String afterSalesStatus;

    @Schema(description = "售后时间")
    private String[] afterSalesTime;

    @Schema(description = "采购人员", example = "张三")
    private String purchaser;

    @Schema(description = "供应商名", example = "华为科技")
    private String supplier;

    @Schema(description = "销售人员", example = "李四")
    private String salesperson;

    @Schema(description = "客户名称", example = "小米科技")
    private String customerName;

    @Schema(description = "中转人员", example = "王五")
    private String transferPerson;

    @Schema(description = "创建人员", example = "admin")
    private String creator;

    @Schema(description = "创建时间")
    private String[] createTime;

    @Schema(description = "租户编号", example = "1")
    private Long tenantId;

    @Schema(description = "销售状态", example = "2")
    private Integer status;

    @Schema(description = "采购审核状态", example = "1")
    private Integer purchaseAuditStatus;
    
    @Schema(description = "销售审核状态", example = "1")
    private Integer saleAuditStatus;

    @Schema(description = "采购备注", example = "采购订单备注")
    private String purchaseRemark;

    @Schema(description = "销售备注", example = "销售订单备注")
    private String saleRemark;
}
