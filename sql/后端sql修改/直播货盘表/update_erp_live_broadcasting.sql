-- 为直播货盘表添加直播货盘状态字段
ALTER TABLE `erp_live_broadcasting`
ADD COLUMN `live_status` VARCHAR(50) NOT NULL DEFAULT '未设置' COMMENT '直播货盘状态'; 