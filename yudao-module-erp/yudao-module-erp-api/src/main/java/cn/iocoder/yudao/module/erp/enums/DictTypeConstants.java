package cn.iocoder.yudao.module.erp.enums;

/**
 * ERP 字典类型的枚举类
 *
 * @author 芋道源码
 */
public interface DictTypeConstants {

    String AUDIT_STATUS = "erp_audit_status"; // 审核状态
    String STOCK_RECORD_BIZ_TYPE = "erp_stock_record_biz_type"; // 库存明细的业务类型
    
    // 财务相关字典
    String FINANCE_CATEGORY = "erp_finance_category"; // 财务收付类目
    String FINANCE_INCOME_EXPENSE = "erp_finance_income_expense"; // 财务收入支出
    String FINANCE_BILL_STATUS = "erp_finance_bill_status"; // 财务账单状态

    // ========== 代发辅助模块 ==========
    String ERP_DROPSHIP_STATUS = "erp_dropship_status"; // 代发辅助状态

    // ========== 产品模块 ==========
    String ERP_PRODUCT_BRAND = "erp_product_brand"; // 产品品牌
    String ERP_PRODUCT_CATEGORY = "erp_product_category"; // 产品品类

}
