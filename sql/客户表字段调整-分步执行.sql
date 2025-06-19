-- ===================================
-- 客户表字段调整SQL - 分步执行版本
-- 请按步骤逐个执行，遇到错误可以跳过继续下一步
-- ===================================

-- 步骤1：备份原表数据
CREATE TABLE erp_customer_backup AS SELECT * FROM erp_customer;

-- 步骤2：删除不需要的字段（逐个执行，如果报错请跳过）
-- 删除联系人字段
ALTER TABLE erp_customer DROP COLUMN contact;

-- 删除手机号码字段  
ALTER TABLE erp_customer DROP COLUMN mobile;

-- 删除电子邮箱字段
ALTER TABLE erp_customer DROP COLUMN email;

-- 删除传真字段
ALTER TABLE erp_customer DROP COLUMN fax;

-- 删除开启状态字段
ALTER TABLE erp_customer DROP COLUMN status;

-- 删除排序字段
ALTER TABLE erp_customer DROP COLUMN sort;

-- 删除纳税人识别号字段
ALTER TABLE erp_customer DROP COLUMN tax_no;

-- 删除税率字段
ALTER TABLE erp_customer DROP COLUMN tax_percent;

-- 删除开户行字段
ALTER TABLE erp_customer DROP COLUMN bank_name;

-- 删除开户地址字段
ALTER TABLE erp_customer DROP COLUMN bank_address;

-- 步骤3：添加新字段
-- 添加客户业务编号字段
ALTER TABLE erp_customer ADD COLUMN no varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户业务编号' AFTER id;

-- 添加收件姓名字段
ALTER TABLE erp_customer ADD COLUMN receiver_name varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收件姓名' AFTER name;

-- 添加详细地址字段
ALTER TABLE erp_customer ADD COLUMN address varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址' AFTER telephone;

-- 添加微信账号字段
ALTER TABLE erp_customer ADD COLUMN wechat_account varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信账号' AFTER address;

-- 添加支付宝号字段
ALTER TABLE erp_customer ADD COLUMN alipay_account varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付宝号' AFTER wechat_account;

-- 步骤4：修改现有字段
-- 修改客户名称字段长度
ALTER TABLE erp_customer MODIFY COLUMN name varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户名称';

-- 修改联系电话字段长度
ALTER TABLE erp_customer MODIFY COLUMN telephone varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话';

-- 修改银行账号字段注释
ALTER TABLE erp_customer MODIFY COLUMN bank_account varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '银行账号';

-- 修改备注字段长度
ALTER TABLE erp_customer MODIFY COLUMN remark varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息';

-- 步骤5：添加索引
-- 添加客户业务编号索引
ALTER TABLE erp_customer ADD INDEX idx_no (no);

-- 步骤6：验证表结构
DESC erp_customer;

-- ===================================
-- 执行完成！
-- =================================== 