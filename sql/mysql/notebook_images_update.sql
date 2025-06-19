-- 记事本表字段修改：将carousel_image字段改为images
-- 用于支持多图片上传功能
-- 执行日期：2024年

-- 检查表是否存在
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = DATABASE() AND table_name = 'erp_notebook';

-- 检查原字段是否存在
SELECT COUNT(*) FROM information_schema.columns 
WHERE table_schema = DATABASE() 
  AND table_name = 'erp_notebook' 
  AND column_name = 'carousel_image';

-- 修改字段名称（如果原字段存在）
ALTER TABLE erp_notebook 
CHANGE COLUMN carousel_image images VARCHAR(2000) 
COMMENT '图片列表，多个图片用逗号分隔';

-- 如果原字段不存在，直接添加新字段
-- ALTER TABLE erp_notebook 
-- ADD COLUMN images VARCHAR(2000) 
-- COMMENT '图片列表，多个图片用逗号分隔'; 