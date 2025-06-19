-- 数据库迁移脚本：将brandId字段改为brandName字段
-- 执行时间：2024年

-- 1. 修改私播货盘表
ALTER TABLE erp_private_broadcasting 
CHANGE COLUMN brand_id brand_name VARCHAR(255) COMMENT '品牌名称';

-- 2. 修改直播货盘表
ALTER TABLE erp_live_broadcasting 
CHANGE COLUMN brand_id brand_name VARCHAR(255) COMMENT '品牌名称';

-- 注意：
-- 1. 执行前请备份数据库
-- 2. 如果原来存储的是品牌ID（数字），需要先将其转换为品牌名称
-- 3. 建议在测试环境先执行并验证 