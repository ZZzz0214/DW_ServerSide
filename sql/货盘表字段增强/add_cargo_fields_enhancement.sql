-- ================================================================
-- 货盘表字段增强 SQL 脚本
-- 功能：为三个货盘表添加 13 个新字段（富文本+文件上传）
-- 创建日期：2025-01-02
-- 执行说明：请在目标数据库中依次执行以下 SQL 语句
-- ================================================================

-- ================================================================
-- 1. 团购货盘表（erp_group_buying）新增字段
-- ================================================================
ALTER TABLE erp_group_buying 
ADD COLUMN main_image LONGTEXT NULL COMMENT '主图（富文本+文件，JSON格式）',
ADD COLUMN detail_info LONGTEXT NULL COMMENT '详情（富文本+文件，JSON格式）',
ADD COLUMN sku_image LONGTEXT NULL COMMENT 'SKU图（富文本+文件，JSON格式）',
ADD COLUMN basic_notes LONGTEXT NULL COMMENT '基础笔记（富文本+文件，JSON格式）',
ADD COLUMN upgrade_notes LONGTEXT NULL COMMENT '升级笔记（富文本+文件，JSON格式）',
ADD COLUMN community_promotion LONGTEXT NULL COMMENT '社群推广（富文本+文件，JSON格式）',
ADD COLUMN detailed_info LONGTEXT NULL COMMENT '详细信息（富文本+文件，JSON格式）',
ADD COLUMN qualification LONGTEXT NULL COMMENT '资质（富文本+文件，JSON格式）',
ADD COLUMN selling_points_ingredients LONGTEXT NULL COMMENT '卖点成分（富文本+文件，JSON格式）',
ADD COLUMN endorsement LONGTEXT NULL COMMENT '背书（富文本+文件，JSON格式）',
ADD COLUMN actual_photos LONGTEXT NULL COMMENT '实拍（富文本+文件，JSON格式）',
ADD COLUMN six_side_images LONGTEXT NULL COMMENT '六面图（富文本+文件，JSON格式）',
ADD COLUMN packaging_images LONGTEXT NULL COMMENT '打包图（富文本+文件，JSON格式）';

-- ================================================================
-- 2. 私播货盘表（erp_private_broadcasting）新增字段
-- ================================================================
ALTER TABLE erp_private_broadcasting 
ADD COLUMN main_image LONGTEXT NULL COMMENT '主图（富文本+文件，JSON格式）',
ADD COLUMN detail_info LONGTEXT NULL COMMENT '详情（富文本+文件，JSON格式）',
ADD COLUMN sku_image LONGTEXT NULL COMMENT 'SKU图（富文本+文件，JSON格式）',
ADD COLUMN basic_notes LONGTEXT NULL COMMENT '基础笔记（富文本+文件，JSON格式）',
ADD COLUMN upgrade_notes LONGTEXT NULL COMMENT '升级笔记（富文本+文件，JSON格式）',
ADD COLUMN community_promotion LONGTEXT NULL COMMENT '社群推广（富文本+文件，JSON格式）',
ADD COLUMN detailed_info LONGTEXT NULL COMMENT '详细信息（富文本+文件，JSON格式）',
ADD COLUMN qualification LONGTEXT NULL COMMENT '资质（富文本+文件，JSON格式）',
ADD COLUMN selling_points_ingredients LONGTEXT NULL COMMENT '卖点成分（富文本+文件，JSON格式）',
ADD COLUMN endorsement LONGTEXT NULL COMMENT '背书（富文本+文件，JSON格式）',
ADD COLUMN actual_photos LONGTEXT NULL COMMENT '实拍（富文本+文件，JSON格式）',
ADD COLUMN six_side_images LONGTEXT NULL COMMENT '六面图（富文本+文件，JSON格式）',
ADD COLUMN packaging_images LONGTEXT NULL COMMENT '打包图（富文本+文件，JSON格式）';

-- ================================================================
-- 3. 直播货盘表（erp_live_broadcasting）新增字段
-- ================================================================
ALTER TABLE erp_live_broadcasting 
ADD COLUMN main_image LONGTEXT NULL COMMENT '主图（富文本+文件，JSON格式）',
ADD COLUMN detail_info LONGTEXT NULL COMMENT '详情（富文本+文件，JSON格式）',
ADD COLUMN sku_image LONGTEXT NULL COMMENT 'SKU图（富文本+文件，JSON格式）',
ADD COLUMN basic_notes LONGTEXT NULL COMMENT '基础笔记（富文本+文件，JSON格式）',
ADD COLUMN upgrade_notes LONGTEXT NULL COMMENT '升级笔记（富文本+文件，JSON格式）',
ADD COLUMN community_promotion LONGTEXT NULL COMMENT '社群推广（富文本+文件，JSON格式）',
ADD COLUMN detailed_info LONGTEXT NULL COMMENT '详细信息（富文本+文件，JSON格式）',
ADD COLUMN qualification LONGTEXT NULL COMMENT '资质（富文本+文件，JSON格式）',
ADD COLUMN selling_points_ingredients LONGTEXT NULL COMMENT '卖点成分（富文本+文件，JSON格式）',
ADD COLUMN endorsement LONGTEXT NULL COMMENT '背书（富文本+文件，JSON格式）',
ADD COLUMN actual_photos LONGTEXT NULL COMMENT '实拍（富文本+文件，JSON格式）',
ADD COLUMN six_side_images LONGTEXT NULL COMMENT '六面图（富文本+文件，JSON格式）',
ADD COLUMN packaging_images LONGTEXT NULL COMMENT '打包图（富文本+文件，JSON格式）';

-- ================================================================
-- 验证脚本执行结果
-- ================================================================
-- 查看团购货盘表结构
-- DESCRIBE erp_group_buying;

-- 查看私播货盘表结构
-- DESCRIBE erp_private_broadcasting;

-- 查看直播货盘表结构
-- DESCRIBE erp_live_broadcasting;

-- ================================================================
-- 数据示例（字段值格式）
-- ================================================================
/*
字段值采用 JSON 格式存储，示例：
{
  "richText": "<p>这是富文本内容...</p>",
  "fileUrls": [
    "https://example.com/files/document1.pdf",
    "https://example.com/files/excel1.xlsx",
    "https://example.com/files/image1.jpg"
  ]
}
*/

-- ================================================================
-- 执行说明
-- ================================================================
/*
1. 备份数据库（重要！）
2. 在目标数据库中执行上述 SQL 语句
3. 执行完成后，使用验证脚本检查表结构
4. 确认字段添加成功后，通知后端开发人员继续代码改动

注意事项：
- 所有字段都是 LONGTEXT 类型，允许 NULL
- 字段使用蛇形命名法（snake_case）
- 所有字段都包含中文注释说明用途
- 不影响现有数据，向后兼容
*/

