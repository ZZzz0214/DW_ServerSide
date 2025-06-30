-- 团购复盘表 group_buying_id 字段迁移脚本
-- 将 group_buying_id 从数字ID更新为团购货盘的编号

-- 1. 首先备份现有数据（可选）
-- CREATE TABLE erp_group_buying_review_backup AS SELECT * FROM erp_group_buying_review;

-- 2. 更新 group_buying_id 字段，将数字ID替换为对应的团购货盘编号
UPDATE erp_group_buying_review r 
INNER JOIN erp_group_buying g ON r.group_buying_id = g.id 
SET r.group_buying_id = g.no 
WHERE r.group_buying_id IS NOT NULL 
  AND r.group_buying_id != '' 
  AND g.no IS NOT NULL;

-- 3. 验证更新结果
-- SELECT r.id, r.no, r.group_buying_id, g.no as group_buying_no 
-- FROM erp_group_buying_review r 
-- LEFT JOIN erp_group_buying g ON r.group_buying_id = g.no 
-- WHERE r.group_buying_id IS NOT NULL;

-- 4. 如果验证无误，可以删除备份表
-- DROP TABLE erp_group_buying_review_backup; 