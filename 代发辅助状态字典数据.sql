-- 创建代发辅助状态字典类型
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) 
VALUES ('代发辅助状态', 'erp_dropship_status', 0, '代发辅助状态信息', '1', NOW(), '1', NOW(), b'0', NULL);

-- 获取刚插入的字典类型ID
SET @dict_type_id = LAST_INSERT_ID();

-- 创建代发辅助状态字典数据
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES
(1, '待处理', 'pending', 'erp_dropship_status', 0, 'warning', '', '待处理状态', '1', NOW(), '1', NOW(), b'0'),
(2, '处理中', 'processing', 'erp_dropship_status', 0, 'primary', '', '处理中状态', '1', NOW(), '1', NOW(), b'0'),
(3, '已完成', 'completed', 'erp_dropship_status', 0, 'success', '', '已完成状态', '1', NOW(), '1', NOW(), b'0'),
(4, '已取消', 'cancelled', 'erp_dropship_status', 0, 'danger', '', '已取消状态', '1', NOW(), '1', NOW(), b'0'),
(5, '暂停', 'paused', 'erp_dropship_status', 0, 'info', '', '暂停状态', '1', NOW(), '1', NOW(), b'0'); 