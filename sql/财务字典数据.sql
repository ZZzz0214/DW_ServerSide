-- 财务收入支出字典类型
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (1001, '财务收入支出', 'erp_finance_income_expense', 0, '财务记录的收入支出类型', '1', '2024-03-25 10:00:00', '1', '2024-03-25 10:00:00', b'0', '1970-01-01 00:00:00');

-- 财务收入支出字典数据
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (10001, 1, '收入', '1', 'erp_finance_income_expense', 0, 'success', '', '收入类型', '1', '2024-03-25 10:00:00', '1', '2024-03-25 10:00:00', b'0');
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (10002, 2, '支出', '2', 'erp_finance_income_expense', 0, 'danger', '', '支出类型', '1', '2024-03-25 10:00:00', '1', '2024-03-25 10:00:00', b'0');

-- 财务账单状态字典类型
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (1002, '财务账单状态', 'erp_finance_bill_status', 0, '财务记录的账单状态', '1', '2024-03-25 10:00:00', '1', '2024-03-25 10:00:00', b'0', '1970-01-01 00:00:00');

-- 财务账单状态字典数据
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (10003, 1, '待处理', '1', 'erp_finance_bill_status', 0, 'warning', '', '待处理状态', '1', '2024-03-25 10:00:00', '1', '2024-03-25 10:00:00', b'0');
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (10004, 2, '已完成', '2', 'erp_finance_bill_status', 0, 'success', '', '已完成状态', '1', '2024-03-25 10:00:00', '1', '2024-03-25 10:00:00', b'0'); 