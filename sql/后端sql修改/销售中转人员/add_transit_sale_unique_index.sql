-- 为中转销售表添加中转人员和组品ID的唯一索引
-- 执行前请确保没有重复数据

-- 检查是否有重复数据
SELECT transit_person, group_product_id, COUNT(*) as count
FROM erp_transit_sale 
WHERE deleted = 0 
GROUP BY transit_person, group_product_id 
HAVING COUNT(*) > 1;

-- 如果有重复数据，请先处理重复数据后再执行以下语句

-- 添加唯一索引
ALTER TABLE `erp_transit_sale` 
ADD UNIQUE KEY `uk_transit_person_group_product` (`transit_person`, `group_product_id`) 
USING BTREE COMMENT '中转人员和组品ID唯一索引'; 