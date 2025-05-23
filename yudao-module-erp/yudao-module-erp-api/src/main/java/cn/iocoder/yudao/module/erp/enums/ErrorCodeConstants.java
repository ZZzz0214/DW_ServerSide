package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * ERP 错误码枚举类
 * <p>
 * erp 系统，使用 1-030-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== ERP 供应商（1-030-100-000） ==========
    ErrorCode SUPPLIER_NOT_EXISTS = new ErrorCode(1_030_100_000, "供应商不存在");
    ErrorCode SUPPLIER_NOT_ENABLE = new ErrorCode(1_030_100_000, "供应商({})未启用");

    // ========== ERP 采购订单（1-030-101-000） ==========
    ErrorCode PURCHASE_ORDER_NOT_EXISTS = new ErrorCode(1_030_101_000, "采购订单不存在");
    ErrorCode PURCHASE_ORDER_DELETE_FAIL_APPROVE = new ErrorCode(1_030_101_001, "采购订单({})已审核，无法删除");
    ErrorCode PURCHASE_ORDER_PROCESS_FAIL = new ErrorCode(1_030_101_002, "反审核失败，只有已审核的采购订单才能反审核");
    ErrorCode PURCHASE_ORDER_APPROVE_FAIL = new ErrorCode(1_030_101_003, "审核失败，只有未审核的采购订单才能审核");
    ErrorCode PURCHASE_ORDER_NO_EXISTS = new ErrorCode(1_030_101_004, "生成采购单号失败，请重新提交");
    ErrorCode PURCHASE_ORDER_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_101_005, "采购订单({})已审核，无法修改");
    ErrorCode PURCHASE_ORDER_NOT_APPROVE = new ErrorCode(1_030_101_006, "采购订单未审核，无法操作");
    ErrorCode PURCHASE_ORDER_ITEM_IN_FAIL_PRODUCT_EXCEED = new ErrorCode(1_030_101_007, "采购订单项({})超过最大允许入库数量({})");
    ErrorCode PURCHASE_ORDER_PROCESS_FAIL_EXISTS_IN = new ErrorCode(1_030_101_008, "反审核失败，已存在对应的采购入库单");
ErrorCode PURCHASE_ORDER_ITEM_RETURN_FAIL_IN_EXCEED = new ErrorCode(1_030_101_009, "采购订单项({})超过最大允许退货数量({})");
    ErrorCode PURCHASE_ORDER_PROCESS_FAIL_EXISTS_RETURN = new ErrorCode(1_030_101_010, "反审核失败，已存在对应的采购退货单");

    // ========== ERP 采购入库（1-030-102-000） ==========
    ErrorCode PURCHASE_IN_NOT_EXISTS = new ErrorCode(1_030_102_000, "采购入库单不存在");
    ErrorCode PURCHASE_IN_DELETE_FAIL_APPROVE = new ErrorCode(1_030_102_001, "采购入库单({})已审核，无法删除");
    ErrorCode PURCHASE_IN_PROCESS_FAIL = new ErrorCode(1_030_102_002, "反审核失败，只有已审核的入库单才能反审核");
    ErrorCode PURCHASE_IN_APPROVE_FAIL = new ErrorCode(1_030_102_003, "审核失败，只有未审核的入库单才能审核");
    ErrorCode PURCHASE_IN_NO_EXISTS = new ErrorCode(1_030_102_004, "生成入库单失败，请重新提交");
    ErrorCode PURCHASE_IN_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_102_005, "采购入库单({})已审核，无法修改");
    ErrorCode PURCHASE_IN_NOT_APPROVE = new ErrorCode(1_030_102_006, "采购入库单未审核，无法操作");
    ErrorCode PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED = new ErrorCode(1_030_102_007, "付款金额({})超过采购入库单总金额({})");
    ErrorCode PURCHASE_IN_PROCESS_FAIL_EXISTS_PAYMENT = new ErrorCode(1_030_102_008, "反审核失败，已存在对应的付款单");

    // ========== ERP 采购退货（1-030-103-000） ==========
    ErrorCode PURCHASE_RETURN_NOT_EXISTS = new ErrorCode(1_030_103_000, "采购退货单不存在");
    ErrorCode PURCHASE_RETURN_DELETE_FAIL_APPROVE = new ErrorCode(1_030_103_001, "采购退货单({})已审核，无法删除");
    ErrorCode PURCHASE_RETURN_PROCESS_FAIL = new ErrorCode(1_030_103_002, "反审核失败，只有已审核的退货单才能反审核");
    ErrorCode PURCHASE_RETURN_APPROVE_FAIL = new ErrorCode(1_030_103_003, "审核失败，只有未审核的退货单才能审核");
    ErrorCode PURCHASE_RETURN_NO_EXISTS = new ErrorCode(1_030_103_004, "生成退货单失败，请重新提交");
    ErrorCode PURCHASE_RETURN_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_103_005, "采购退货单({})已审核，无法修改");
    ErrorCode PURCHASE_RETURN_NOT_APPROVE = new ErrorCode(1_030_103_006, "采购退货单未审核，无法操作");
    ErrorCode PURCHASE_RETURN_FAIL_REFUND_PRICE_EXCEED = new ErrorCode(1_030_103_007, "退款金额({})超过采购退货单总金额({})");
    ErrorCode PURCHASE_RETURN_PROCESS_FAIL_EXISTS_REFUND = new ErrorCode(1_030_103_008, "反审核失败，已存在对应的退款单");

    // ========== ERP 客户（1-030-200-000）==========
    ErrorCode CUSTOMER_NOT_EXISTS = new ErrorCode(1_020_200_000, "客户不存在");
    ErrorCode CUSTOMER_NOT_ENABLE = new ErrorCode(1_020_200_001, "客户({})未启用");

    // ========== ERP 销售订单（1-030-201-000） ==========
    ErrorCode SALE_ORDER_NOT_EXISTS = new ErrorCode(1_020_201_000, "销售订单不存在");
    ErrorCode SALE_ORDER_DELETE_FAIL_APPROVE = new ErrorCode(1_020_201_001, "销售订单({})已审核，无法删除");
    ErrorCode SALE_ORDER_PROCESS_FAIL = new ErrorCode(1_020_201_002, "反审核失败，只有已审核的销售订单才能反审核");
    ErrorCode SALE_ORDER_APPROVE_FAIL = new ErrorCode(1_020_201_003, "审核失败，只有未审核的销售订单才能审核");
    ErrorCode SALE_ORDER_NO_EXISTS = new ErrorCode(1_020_201_004, "生成销售单号失败，请重新提交");
    ErrorCode SALE_ORDER_UPDATE_FAIL_APPROVE = new ErrorCode(1_020_201_005, "销售订单({})已审核，无法修改");
    ErrorCode SALE_ORDER_NOT_APPROVE = new ErrorCode(1_020_201_006, "销售订单未审核，无法操作");
    ErrorCode SALE_ORDER_ITEM_OUT_FAIL_PRODUCT_EXCEED = new ErrorCode(1_020_201_007, "销售订单项({})超过最大允许出库数量({})");
    ErrorCode SALE_ORDER_PROCESS_FAIL_EXISTS_OUT = new ErrorCode(1_020_201_008, "反审核失败，已存在对应的销售出库单");
    ErrorCode SALE_ORDER_ITEM_RETURN_FAIL_OUT_EXCEED = new ErrorCode(1_020_201_009, "销售订单项({})超过最大允许退货数量({})");
    ErrorCode SALE_ORDER_PROCESS_FAIL_EXISTS_RETURN = new ErrorCode(1_020_201_010, "反审核失败，已存在对应的销售退货单");
    // ========== ERP 销售价格 (1-030-300-000) ==========
    ErrorCode SALE_PRICE_NOT_EXISTS = new ErrorCode(1_030_300_000, "销售价格记录不存在");
    ErrorCode SALE_PRICE_GROUP_PRODUCT_ID_REQUIRED = new ErrorCode(1_030_300_001, "组品编号不能为空");
    ErrorCode SALE_PRICE_CUSTOMER_NAME_REQUIRED = new ErrorCode(1_030_300_002, "客户名称不能为空");
    ErrorCode SALE_PRICE_DISTRIBUTION_PRICE_REQUIRED = new ErrorCode(1_030_300_003, "代发单价不能为空");
    ErrorCode SALE_PRICE_WHOLESALE_PRICE_REQUIRED = new ErrorCode(1_030_300_004, "批发单价不能为空");
    ErrorCode SALE_PRICE_SHIPPING_FEE_TYPE_REQUIRED = new ErrorCode(1_030_300_005, "运费类型不能为空");

    // ========== ERP 销售出库（1-030-202-000） ==========
    ErrorCode SALE_OUT_NOT_EXISTS = new ErrorCode(1_020_202_000, "销售出库单不存在");
    ErrorCode SALE_OUT_DELETE_FAIL_APPROVE = new ErrorCode(1_020_202_001, "销售出库单({})已审核，无法删除");
    ErrorCode SALE_OUT_PROCESS_FAIL = new ErrorCode(1_020_202_002, "反审核失败，只有已审核的出库单才能反审核");
    ErrorCode SALE_OUT_APPROVE_FAIL = new ErrorCode(1_020_202_003, "审核失败，只有未审核的出库单才能审核");
    ErrorCode SALE_OUT_NO_EXISTS = new ErrorCode(1_020_202_004, "生成出库单失败，请重新提交");
    ErrorCode SALE_OUT_UPDATE_FAIL_APPROVE = new ErrorCode(1_020_202_005, "销售出库单({})已审核，无法修改");
    ErrorCode SALE_OUT_NOT_APPROVE = new ErrorCode(1_020_202_006, "销售出库单未审核，无法操作");
    ErrorCode SALE_OUT_FAIL_RECEIPT_PRICE_EXCEED = new ErrorCode(1_020_202_007, "收款金额({})超过销售出库单总金额({})");
    ErrorCode SALE_OUT_PROCESS_FAIL_EXISTS_RECEIPT = new ErrorCode(1_020_202_008, "反审核失败，已存在对应的收款单");

    // ========== ERP 销售退货（1-030-203-000） ==========
    ErrorCode SALE_RETURN_NOT_EXISTS = new ErrorCode(1_020_203_000, "销售退货单不存在");
    ErrorCode SALE_RETURN_DELETE_FAIL_APPROVE = new ErrorCode(1_020_203_001, "销售退货单({})已审核，无法删除");
    ErrorCode SALE_RETURN_PROCESS_FAIL = new ErrorCode(1_020_203_002, "反审核失败，只有已审核的退货单才能反审核");
    ErrorCode SALE_RETURN_APPROVE_FAIL = new ErrorCode(1_020_203_003, "审核失败，只有未审核的退货单才能审核");
    ErrorCode SALE_RETURN_NO_EXISTS = new ErrorCode(1_020_203_004, "生成退货单失败，请重新提交");
    ErrorCode SALE_RETURN_UPDATE_FAIL_APPROVE = new ErrorCode(1_020_203_005, "销售退货单({})已审核，无法修改");
    ErrorCode SALE_RETURN_NOT_APPROVE = new ErrorCode(1_020_203_006, "销售退货单未审核，无法操作");
    ErrorCode SALE_RETURN_FAIL_REFUND_PRICE_EXCEED = new ErrorCode(1_020_203_007, "退款金额({})超过销售退货单总金额({})");
    ErrorCode SALE_RETURN_PROCESS_FAIL_EXISTS_REFUND = new ErrorCode(1_020_203_008, "反审核失败，已存在对应的退款单");

    // ========== ERP 仓库 1-030-400-000 ==========
    ErrorCode WAREHOUSE_NOT_EXISTS = new ErrorCode(1_030_400_000, "仓库不存在");
    ErrorCode WAREHOUSE_NOT_ENABLE = new ErrorCode(1_030_400_001, "仓库({})未启用");

    // ========== ERP 其它入库单 1-030-401-000 ==========
    ErrorCode STOCK_IN_NOT_EXISTS = new ErrorCode(1_030_401_000, "其它入库单不存在");
    ErrorCode STOCK_IN_DELETE_FAIL_APPROVE = new ErrorCode(1_030_401_001, "其它入库单({})已审核，无法删除");
    ErrorCode STOCK_IN_PROCESS_FAIL = new ErrorCode(1_030_401_002, "反审核失败，只有已审核的入库单才能反审核");
    ErrorCode STOCK_IN_APPROVE_FAIL = new ErrorCode(1_030_401_003, "审核失败，只有未审核的入库单才能审核");
    ErrorCode STOCK_IN_NO_EXISTS = new ErrorCode(1_030_401_004, "生成入库单失败，请重新提交");
    ErrorCode STOCK_IN_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_401_005, "其它入库单({})已审核，无法修改");

    // ========== ERP 其它出库单 1-030-402-000 ==========
    ErrorCode STOCK_OUT_NOT_EXISTS = new ErrorCode(1_030_402_000, "其它出库单不存在");
    ErrorCode STOCK_OUT_DELETE_FAIL_APPROVE = new ErrorCode(1_030_402_001, "其它出库单({})已审核，无法删除");
    ErrorCode STOCK_OUT_PROCESS_FAIL = new ErrorCode(1_030_402_002, "反审核失败，只有已审核的出库单才能反审核");
    ErrorCode STOCK_OUT_APPROVE_FAIL = new ErrorCode(1_030_402_003, "审核失败，只有未审核的出库单才能审核");
    ErrorCode STOCK_OUT_NO_EXISTS = new ErrorCode(1_030_402_004, "生成出库单失败，请重新提交");
    ErrorCode STOCK_OUT_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_402_005, "其它出库单({})已审核，无法修改");

    // ========== ERP 库存调拨单 1-030-403-000 ==========
    ErrorCode STOCK_MOVE_NOT_EXISTS = new ErrorCode(1_030_402_000, "库存调拨单不存在");
    ErrorCode STOCK_MOVE_DELETE_FAIL_APPROVE = new ErrorCode(1_030_402_001, "库存调拨单({})已审核，无法删除");
    ErrorCode STOCK_MOVE_PROCESS_FAIL = new ErrorCode(1_030_402_002, "反审核失败，只有已审核的调拨单才能反审核");
    ErrorCode STOCK_MOVE_APPROVE_FAIL = new ErrorCode(1_030_402_003, "审核失败，只有未审核的调拨单才能审核");
    ErrorCode STOCK_MOVE_NO_EXISTS = new ErrorCode(1_030_402_004, "生成调拨号失败，请重新提交");
    ErrorCode STOCK_MOVE_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_402_005, "库存调拨单({})已审核，无法修改");

    // ========== ERP 库存盘点单 1-030-403-000 ==========
    ErrorCode STOCK_CHECK_NOT_EXISTS = new ErrorCode(1_030_403_000, "库存盘点单不存在");
    ErrorCode STOCK_CHECK_DELETE_FAIL_APPROVE = new ErrorCode(1_030_403_001, "库存盘点单({})已审核，无法删除");
    ErrorCode STOCK_CHECK_PROCESS_FAIL = new ErrorCode(1_030_403_002, "反审核失败，只有已审核的盘点单才能反审核");
    ErrorCode STOCK_CHECK_APPROVE_FAIL = new ErrorCode(1_030_403_003, "审核失败，只有未审核的盘点单才能审核");
    ErrorCode STOCK_CHECK_NO_EXISTS = new ErrorCode(1_030_403_004, "生成盘点号失败，请重新提交");
    ErrorCode STOCK_CHECK_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_403_005, "库存盘点单({})已审核，无法修改");

    // ========== ERP 产品库存 1-030-404-000 ==========
    ErrorCode STOCK_COUNT_NEGATIVE = new ErrorCode(1_030_404_000, "操作失败，产品({})所在仓库({})的库存：{}，小于变更数量：{}");
    ErrorCode STOCK_COUNT_NEGATIVE2 = new ErrorCode(1_030_404_001, "操作失败，产品({})所在仓库({})的库存不足");

    // ========== ERP 产品 1-030-500-000 ==========
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_030_500_000, "产品不存在");
    ErrorCode PRODUCT_NOT_ENABLE = new ErrorCode(1_030_500_001, "产品({})未启用");
    ErrorCode COMBO_PRODUCT_NOT_EXISTS = new ErrorCode(1_030_500_002, "组品不存在");
    ErrorCode COMBO_PRODUCT_ALREADY_EXISTS = new ErrorCode(1_030_500_003, "组品已存在");
    ErrorCode COMBO_PRODUCT_UPDATE_FAIL = new ErrorCode(1_030_500_004, "组品更新失败");
    ErrorCode COMBO_PRODUCT_DELETE_FAIL = new ErrorCode(1_030_500_005, "组品删除失败");

    // ========== ERP 产品分类 1-030-501-000 ==========
    ErrorCode PRODUCT_CATEGORY_NOT_EXISTS = new ErrorCode(1_030_501_000, "产品分类不存在");
    ErrorCode PRODUCT_CATEGORY_EXITS_CHILDREN = new ErrorCode(1_030_501_001, "存在存在子产品分类，无法删除");
    ErrorCode PRODUCT_CATEGORY_PARENT_NOT_EXITS = new ErrorCode(1_030_501_002,"父级产品分类不存在");
    ErrorCode PRODUCT_CATEGORY_PARENT_ERROR = new ErrorCode(1_030_501_003, "不能设置自己为父产品分类");
    ErrorCode PRODUCT_CATEGORY_NAME_DUPLICATE = new ErrorCode(1_030_501_004, "已经存在该分类名称的产品分类");
    ErrorCode PRODUCT_CATEGORY_PARENT_IS_CHILD = new ErrorCode(1_030_501_005, "不能设置自己的子分类为父分类");
    ErrorCode PRODUCT_CATEGORY_EXITS_PRODUCT = new ErrorCode(1_030_502_002, "存在产品使用该分类，无法删除");

    // ========== ERP 产品单位 1-030-502-000 ==========
    ErrorCode PRODUCT_UNIT_NOT_EXISTS = new ErrorCode(1_030_502_000, "产品单位不存在");
    ErrorCode PRODUCT_UNIT_NAME_DUPLICATE = new ErrorCode(1_030_502_001, "已存在该名字的产品单位");
    ErrorCode PRODUCT_UNIT_EXITS_PRODUCT = new ErrorCode(1_030_502_002, "存在产品使用该单位，无法删除");

    // ========== ERP 结算账户 1-030-600-000 ==========
    ErrorCode ACCOUNT_NOT_EXISTS = new ErrorCode(1_030_600_000, "结算账户不存在");
    ErrorCode ACCOUNT_NOT_ENABLE = new ErrorCode(1_030_600_001, "结算账户({})未启用");

    // ========== ERP 付款单 1-030-601-000 ==========
    ErrorCode FINANCE_PAYMENT_NOT_EXISTS = new ErrorCode(1_030_601_000, "付款单不存在");
    ErrorCode FINANCE_PAYMENT_DELETE_FAIL_APPROVE = new ErrorCode(1_030_601_001, "付款单({})已审核，无法删除");
    ErrorCode FINANCE_PAYMENT_PROCESS_FAIL = new ErrorCode(1_030_601_002, "反审核失败，只有已审核的付款单才能反审核");
    ErrorCode FINANCE_PAYMENT_APPROVE_FAIL = new ErrorCode(1_030_601_003, "审核失败，只有未审核的付款单才能审核");
    ErrorCode FINANCE_PAYMENT_NO_EXISTS = new ErrorCode(1_030_601_004, "生成付款单号失败，请重新提交");
    ErrorCode FINANCE_PAYMENT_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_601_005, "付款单({})已审核，无法修改");

    // ========== ERP 收款单 1-030-602-000 ==========
    ErrorCode FINANCE_RECEIPT_NOT_EXISTS = new ErrorCode(1_030_602_000, "收款单不存在");
    ErrorCode FINANCE_RECEIPT_DELETE_FAIL_APPROVE = new ErrorCode(1_030_602_001, "收款单({})已审核，无法删除");
    ErrorCode FINANCE_RECEIPT_PROCESS_FAIL = new ErrorCode(1_030_602_002, "反审核失败，只有已审核的收款单才能反审核");
    ErrorCode FINANCE_RECEIPT_APPROVE_FAIL = new ErrorCode(1_030_602_003, "审核失败，只有未审核的收款单才能审核");
    ErrorCode FINANCE_RECEIPT_NO_EXISTS = new ErrorCode(1_030_602_004, "生成收款单号失败，请重新提交");
    ErrorCode FINANCE_RECEIPT_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_602_005, "收款单({})已审核，无法修改");

    // ========== ERP 代发（1-030-104-000） ==========
    ErrorCode DISTRIBUTION_NOT_EXISTS = new ErrorCode(1_030_104_000, "代发记录不存在");
    ErrorCode DISTRIBUTION_NO_EXISTS = new ErrorCode(1_030_104_001, "代发订单号已存在");
    ErrorCode DISTRIBUTION_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_104_002, "代发记录({})已审核，无法修改");
    ErrorCode DISTRIBUTION_DELETE_FAIL_APPROVE = new ErrorCode(1_030_104_003, "代发记录({})已审核，无法删除");
    ErrorCode DISTRIBUTION_NOT_APPROVE = new ErrorCode(1_030_104_004, "代发记录未审核，无法操作");

    // ========== ERP 批发（1-030-105-000） ==========
    // ========== ERP 批发（1-030-105-000） ==========
    ErrorCode WHOLESALE_NOT_EXISTS = new ErrorCode(1_030_105_000, "批发记录不存在");
    ErrorCode WHOLESALE_NO_EXISTS = new ErrorCode(1_030_105_001, "批发订单号已存在");
    ErrorCode WHOLESALE_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_105_002, "批发记录({})已审核，无法修改");
    ErrorCode WHOLESALE_NOT_APPROVE = new ErrorCode(1_030_105_003, "批发记录未审核，无法操作");
    ErrorCode WHOLESALE_PROCESS_FAIL = new ErrorCode(1_030_105_004, "反审核失败，只有已审核的批发记录才能反审核");
    ErrorCode WHOLESALE_APPROVE_FAIL = new ErrorCode(1_030_105_005, "审核失败，只有未审核的批发记录才能审核");
    // ========== ERP 采购人员（1-030-106-000） ==========
    ErrorCode PURCHASER_NOT_EXISTS = new ErrorCode(1_030_106_000, "采购人员不存在");
    ErrorCode PURCHASER_NAME_DUPLICATE = new ErrorCode(1_030_106_001, "采购人员姓名已存在");
    ErrorCode PURCHASER_PHONE_DUPLICATE = new ErrorCode(1_030_106_002, "采购人员联系电话已存在");
    ErrorCode PURCHASER_NOT_ENABLE = new ErrorCode(1_030_106_003, "采购人员({})未启用");

    // ========== ERP 销售人员（1-030-107-000） ==========
    ErrorCode SALESPERSON_NOT_EXISTS = new ErrorCode(1_030_107_000, "销售人员不存在");
    ErrorCode SALESPERSON_NAME_DUPLICATE = new ErrorCode(1_030_107_001, "销售人员姓名已存在");
    ErrorCode SALESPERSON_PHONE_DUPLICATE = new ErrorCode(1_030_107_002, "销售人员联系电话已存在");
    ErrorCode SALESPERSON_NOT_ENABLE = new ErrorCode(1_030_107_003, "销售人员({})未启用");
    // ========== ERP 库存（1-030-108-000） ==========
    ErrorCode INVENTORY_NOT_EXISTS = new ErrorCode(1_030_108_000, "库存记录不存在");
    ErrorCode INVENTORY_NO_EXISTS = new ErrorCode(1_030_108_001, "库存编号已存在");
    ErrorCode INVENTORY_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_108_002, "库存记录({})已审核，无法修改");
    ErrorCode INVENTORY_DELETE_FAIL_APPROVE = new ErrorCode(1_030_108_003, "库存记录({})已审核，无法删除");
    ErrorCode INVENTORY_NOT_APPROVE = new ErrorCode(1_030_108_004, "库存记录未审核，无法操作");
        // ========== ERP 代发（1-030-104-000） ==========
    // ... existing error codes ...
    ErrorCode DISTRIBUTION_APPROVE_FAIL = new ErrorCode(1_030_104_005, "审核失败，只有未审核的代发记录才能审核");
    ErrorCode DISTRIBUTION_PROCESS_FAIL = new ErrorCode(1_030_104_006, "反审核失败，只有已审核的代发记录才能反审核");
// ========== ERP 记事本（1-030-109-000） ==========
    ErrorCode NOTEBOOK_NOT_EXISTS = new ErrorCode(1_030_109_000, "记事本不存在");
    ErrorCode NOTEBOOK_NO_EXISTS = new ErrorCode(1_030_109_001, "记事本编号已存在");
    // ========== ERP 样品（1-030-110-000） ==========
    ErrorCode SAMPLE_NOT_EXISTS = new ErrorCode(1_030_110_000, "样品不存在");
    ErrorCode SAMPLE_NO_EXISTS = new ErrorCode(1_030_110_001, "样品编号已存在");
    // ========== ERP 团购货盘（1-030-111-000） ==========
    ErrorCode GROUP_BUYING_NOT_EXISTS = new ErrorCode(1_030_111_000, "团购货盘不存在");
    ErrorCode GROUP_BUYING_NO_EXISTS = new ErrorCode(1_030_111_001, "团购货盘编号已存在");
    ErrorCode GROUP_BUYING_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_111_002, "团购货盘({})已审核，无法修改");
    ErrorCode GROUP_BUYING_DELETE_FAIL_APPROVE = new ErrorCode(1_030_111_003, "团购货盘({})已审核，无法删除");
    ErrorCode GROUP_BUYING_NOT_APPROVE = new ErrorCode(1_030_111_004, "团购货盘未审核，无法操作");
    ErrorCode GROUP_BUYING_APPROVE_FAIL = new ErrorCode(1_030_111_005, "审核失败，只有未审核的团购货盘才能审核");
    ErrorCode GROUP_BUYING_PROCESS_FAIL = new ErrorCode(1_030_111_006, "反审核失败，只有已审核的团购货盘才能反审核");

    // ========== ERP 团购复盘（1-030-112-000） ==========
    ErrorCode GROUP_BUYING_REVIEW_NOT_EXISTS = new ErrorCode(1_030_112_000, "团购复盘不存在");
    ErrorCode GROUP_BUYING_REVIEW_NO_EXISTS = new ErrorCode(1_030_112_001, "团购复盘编号已存在");
    ErrorCode GROUP_BUYING_REVIEW_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_112_002, "团购复盘({})已审核，无法修改");
    ErrorCode GROUP_BUYING_REVIEW_DELETE_FAIL_APPROVE = new ErrorCode(1_030_112_003, "团购复盘({})已审核，无法删除");
    ErrorCode GROUP_BUYING_REVIEW_NOT_APPROVE = new ErrorCode(1_030_112_004, "团购复盘未审核，无法操作");
    ErrorCode GROUP_BUYING_REVIEW_APPROVE_FAIL = new ErrorCode(1_030_112_005, "审核失败，只有未审核的团购复盘才能审核");
    ErrorCode GROUP_BUYING_REVIEW_PROCESS_FAIL = new ErrorCode(1_030_112_006, "反审核失败，只有已审核的团购复盘才能反审核");

    // ========== ERP 团购信息（1-030-113-000） ==========
    ErrorCode GROUP_BUYING_INFO_NOT_EXISTS = new ErrorCode(1_030_113_000, "团购信息不存在");
    ErrorCode GROUP_BUYING_INFO_NO_EXISTS = new ErrorCode(1_030_113_001, "团购信息编号已存在");
    ErrorCode GROUP_BUYING_INFO_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_113_002, "团购信息({})已审核，无法修改");
    ErrorCode GROUP_BUYING_INFO_DELETE_FAIL_APPROVE = new ErrorCode(1_030_113_003, "团购信息({})已审核，无法删除");
    ErrorCode GROUP_BUYING_INFO_NOT_APPROVE = new ErrorCode(1_030_113_004, "团购信息未审核，无法操作");
    ErrorCode GROUP_BUYING_INFO_APPROVE_FAIL = new ErrorCode(1_030_113_005, "审核失败，只有未审核的团购信息才能审核");
    ErrorCode GROUP_BUYING_INFO_PROCESS_FAIL = new ErrorCode(1_030_113_006, "反审核失败，只有已审核的团购信息才能反审核");

    // ========== ERP 私播货盘（1-030-114-000） ==========
    ErrorCode PRIVATE_BROADCASTING_NOT_EXISTS = new ErrorCode(1_030_114_000, "私播货盘不存在");
    ErrorCode PRIVATE_BROADCASTING_NO_EXISTS = new ErrorCode(1_030_114_001, "私播货盘编号已存在");
    ErrorCode PRIVATE_BROADCASTING_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_114_002, "私播货盘({})已审核，无法修改");
    ErrorCode PRIVATE_BROADCASTING_DELETE_FAIL_APPROVE = new ErrorCode(1_030_114_003, "私播货盘({})已审核，无法删除");
    ErrorCode PRIVATE_BROADCASTING_NOT_APPROVE = new ErrorCode(1_030_114_004, "私播货盘未审核，无法操作");
    ErrorCode PRIVATE_BROADCASTING_APPROVE_FAIL = new ErrorCode(1_030_114_005, "审核失败，只有未审核的私播货盘才能审核");
    ErrorCode PRIVATE_BROADCASTING_PROCESS_FAIL = new ErrorCode(1_030_114_006, "反审核失败，只有已审核的私播货盘才能反审核");


    // ========== ERP 私播复盘（1-030-115-000） ==========
    ErrorCode PRIVATE_BROADCASTING_REVIEW_NOT_EXISTS = new ErrorCode(1_030_115_000, "私播复盘不存在");
    ErrorCode PRIVATE_BROADCASTING_REVIEW_NO_EXISTS = new ErrorCode(1_030_115_001, "私播复盘编号已存在");
    ErrorCode PRIVATE_BROADCASTING_REVIEW_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_115_002, "私播复盘({})已审核，无法修改");
    ErrorCode PRIVATE_BROADCASTING_REVIEW_DELETE_FAIL_APPROVE = new ErrorCode(1_030_115_003, "私播复盘({})已审核，无法删除");
    ErrorCode PRIVATE_BROADCASTING_REVIEW_NOT_APPROVE = new ErrorCode(1_030_115_004, "私播复盘未审核，无法操作");
    ErrorCode PRIVATE_BROADCASTING_REVIEW_APPROVE_FAIL = new ErrorCode(1_030_115_005, "审核失败，只有未审核的私播复盘才能审核");
    ErrorCode PRIVATE_BROADCASTING_REVIEW_PROCESS_FAIL = new ErrorCode(1_030_115_006, "反审核失败，只有已审核的私播复盘才能反审核");
    // ========== ERP 私播信息（1-030-116-000） ==========
    ErrorCode PRIVATE_BROADCASTING_INFO_NOT_EXISTS = new ErrorCode(1_030_116_000, "私播信息不存在");
    ErrorCode PRIVATE_BROADCASTING_INFO_NO_EXISTS = new ErrorCode(1_030_116_001, "私播信息编号已存在");
    ErrorCode PRIVATE_BROADCASTING_INFO_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_116_002, "私播信息({})已审核，无法修改");
    ErrorCode PRIVATE_BROADCASTING_INFO_DELETE_FAIL_APPROVE = new ErrorCode(1_030_116_003, "私播信息({})已审核，无法删除");
    ErrorCode PRIVATE_BROADCASTING_INFO_NOT_APPROVE = new ErrorCode(1_030_116_004, "私播信息未审核，无法操作");
    ErrorCode PRIVATE_BROADCASTING_INFO_APPROVE_FAIL = new ErrorCode(1_030_116_005, "审核失败，只有未审核的私播信息才能审核");
    ErrorCode PRIVATE_BROADCASTING_INFO_PROCESS_FAIL = new ErrorCode(1_030_116_006, "反审核失败，只有已审核的私播信息才能反审核");


    // ========== ERP 直播货盘（1-030-117-000） ==========
    ErrorCode LIVE_BROADCASTING_NOT_EXISTS = new ErrorCode(1_030_117_000, "直播货盘不存在");
    ErrorCode LIVE_BROADCASTING_NO_EXISTS = new ErrorCode(1_030_117_001, "直播货盘编号已存在");
    ErrorCode LIVE_BROADCASTING_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_117_002, "直播货盘({})已审核，无法修改");
    ErrorCode LIVE_BROADCASTING_DELETE_FAIL_APPROVE = new ErrorCode(1_030_117_003, "直播货盘({})已审核，无法删除");
    ErrorCode LIVE_BROADCASTING_NOT_APPROVE = new ErrorCode(1_030_117_004, "直播货盘未审核，无法操作");
    ErrorCode LIVE_BROADCASTING_APPROVE_FAIL = new ErrorCode(1_030_117_005, "审核失败，只有未审核的直播货盘才能审核");
    ErrorCode LIVE_BROADCASTING_PROCESS_FAIL = new ErrorCode(1_030_117_006, "反审核失败，只有已审核的直播货盘才能反审核");
    // ========== ERP 直播复盘（1-030-118-000） ==========
    ErrorCode LIVE_BROADCASTING_REVIEW_NOT_EXISTS = new ErrorCode(1_030_118_000, "直播复盘不存在");
    ErrorCode LIVE_BROADCASTING_REVIEW_NO_EXISTS = new ErrorCode(1_030_118_001, "直播复盘编号已存在");
    ErrorCode LIVE_BROADCASTING_REVIEW_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_118_002, "直播复盘({})已审核，无法修改");
    ErrorCode LIVE_BROADCASTING_REVIEW_DELETE_FAIL_APPROVE = new ErrorCode(1_030_118_003, "直播复盘({})已审核，无法删除");
    ErrorCode LIVE_BROADCASTING_REVIEW_NOT_APPROVE = new ErrorCode(1_030_118_004, "直播复盘未审核，无法操作");
    ErrorCode LIVE_BROADCASTING_REVIEW_APPROVE_FAIL = new ErrorCode(1_030_118_005, "审核失败，只有未审核的直播复盘才能审核");
    ErrorCode LIVE_BROADCASTING_REVIEW_PROCESS_FAIL = new ErrorCode(1_030_118_006, "反审核失败，只有已审核的直播复盘才能反审核");

    // ========== ERP 直播信息（1-030-119-000） ==========
    ErrorCode LIVE_BROADCASTING_INFO_NOT_EXISTS = new ErrorCode(1_030_119_000, "直播信息不存在");
    ErrorCode LIVE_BROADCASTING_INFO_NO_EXISTS = new ErrorCode(1_030_119_001, "直播信息编号已存在");
    ErrorCode LIVE_BROADCASTING_INFO_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_119_002, "直播信息({})已审核，无法修改");
    ErrorCode LIVE_BROADCASTING_INFO_DELETE_FAIL_APPROVE = new ErrorCode(1_030_119_003, "直播信息({})已审核，无法删除");
    ErrorCode LIVE_BROADCASTING_INFO_NOT_APPROVE = new ErrorCode(1_030_119_004, "直播信息未审核，无法操作");
    ErrorCode LIVE_BROADCASTING_INFO_APPROVE_FAIL = new ErrorCode(1_030_119_005, "审核失败，只有未审核的直播信息才能审核");
    ErrorCode LIVE_BROADCASTING_INFO_PROCESS_FAIL = new ErrorCode(1_030_119_006, "反审核失败，只有已审核的直播信息才能反审核");
}
