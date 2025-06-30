-- 添加直播客户名称字典类型
INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
('直播客户名称', 'erp_live_customer_name', 0, '直播信息客户名称字典', 'admin', NOW(), 'admin', NOW(), 0);

-- 添加直播客户名称字典数据
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(1, '张三', '张三', 'erp_live_customer_name', 0, 'default', '', '客户张三', 'admin', NOW(), 'admin', NOW(), 0),
(2, '李四', '李四', 'erp_live_customer_name', 0, 'default', '', '客户李四', 'admin', NOW(), 'admin', NOW(), 0),
(3, '王五', '王五', 'erp_live_customer_name', 0, 'default', '', '客户王五', 'admin', NOW(), 'admin', NOW(), 0),
(4, '赵六', '赵六', 'erp_live_customer_name', 0, 'default', '', '客户赵六', 'admin', NOW(), 'admin', NOW(), 0),
(5, '钱七', '钱七', 'erp_live_customer_name', 0, 'default', '', '客户钱七', 'admin', NOW(), 'admin', NOW(), 0),
(6, '孙八', '孙八', 'erp_live_customer_name', 0, 'default', '', '客户孙八', 'admin', NOW(), 'admin', NOW(), 0),
(7, '周九', '周九', 'erp_live_customer_name', 0, 'default', '', '客户周九', 'admin', NOW(), 'admin', NOW(), 0),
(8, '吴十', '吴十', 'erp_live_customer_name', 0, 'default', '', '客户吴十', 'admin', NOW(), 'admin', NOW(), 0); 