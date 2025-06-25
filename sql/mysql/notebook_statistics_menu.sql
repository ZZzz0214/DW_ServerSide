-- 记事本统计菜单SQL脚本

-- 插入记事本统计菜单
INSERT INTO `system_menu` VALUES (3010, '记事本统计', '', 2, 10, 2995, 'erp/statistics/notebook', 'ep:data-analysis', 'erp/statistics/notebook', 'ErpNotebookStatistics', 0, b'1', b'1', b'1', '1', '2025-01-10 10:00:00', '1', '2025-01-10 10:00:00', b'0');

-- 插入记事本统计查询权限
INSERT INTO `system_menu` VALUES (3011, '记事本统计查询', 'erp:statistics:notebook:query', 3, 1, 3010, '', '', '', '', 0, b'1', b'1', b'1', '1', '2025-01-10 10:00:00', '1', '2025-01-10 10:00:00', b'0');

-- 插入记事本统计导出权限
INSERT INTO `system_menu` VALUES (3012, '记事本统计导出', 'erp:statistics:notebook:export', 3, 2, 3010, '', '', '', '', 0, b'1', b'1', b'1', '1', '2025-01-10 10:00:00', '1', '2025-01-10 10:00:00', b'0'); 