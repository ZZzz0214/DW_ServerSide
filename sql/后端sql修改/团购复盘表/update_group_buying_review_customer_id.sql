-- 团购复盘表 customer_id 字段迁移脚本
-- 将 customer_id 从数字ID更新为客户名称

-- 1. 首先备份现有数据（可选）
-- CREATE TABLE erp_group_buying_review_backup AS SELECT * FROM erp_group_buying_review;

-- 2. 更新 customer_id 字段，将数字ID替换为对应的客户名称
UPDATE erp_group_buying_review r 
INNER JOIN erp_customer c ON r.customer_id = c.id 
SET r.customer_id = c.name 
WHERE r.customer_id IS NOT NULL 
  AND r.customer_id != '' 
  AND c.name IS NOT NULL;

-- 3. 验证更新结果
-- SELECT r.id, r.no, r.customer_id, c.name as customer_name 
-- FROM erp_group_buying_review r 
-- LEFT JOIN erp_customer c ON r.customer_id = c.name 
-- WHERE r.customer_id IS NOT NULL;

-- 4. 如果验证无误，可以删除备份表
-- DROP TABLE erp_group_buying_review_backup; 