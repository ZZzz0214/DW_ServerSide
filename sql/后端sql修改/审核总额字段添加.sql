-- 为代发表和批发表添加审核总额字段
-- 执行时间：2025-01-27

-- 1. 为代发表 erp_distribution_combined 添加审核总额字段
ALTER TABLE `erp_distribution_combined` 
ADD COLUMN `purchase_audit_total_amount` DECIMAL(10,2) NULL COMMENT '代发采购审核总额' AFTER `sale_unapprove_time`,
ADD COLUMN `sale_audit_total_amount` DECIMAL(10,2) NULL COMMENT '代发销售审核总额' AFTER `purchase_audit_total_amount`;

-- 2. 为批发表 erp_wholesale_combined 添加审核总额字段
ALTER TABLE `erp_wholesale_combined` 
ADD COLUMN `purchase_audit_total_amount` DECIMAL(10,2) NULL COMMENT '批发采购审核总额' AFTER `sale_unapprove_time`,
ADD COLUMN `sale_audit_total_amount` DECIMAL(10,2) NULL COMMENT '批发销售审核总额' AFTER `purchase_audit_total_amount`;

-- 3. 验证字段添加成功
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME IN ('erp_distribution_combined', 'erp_wholesale_combined')
    AND COLUMN_NAME LIKE '%audit_total_amount%'
ORDER BY TABLE_NAME, COLUMN_NAME; 