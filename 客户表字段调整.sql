-- 客户表字段调整SQL
-- 注意：执行前请备份数据库

-- 1. 删除不需要的字段
ALTER TABLE erp_customer DROP COLUMN contact;
ALTER TABLE erp_customer DROP COLUMN mobile;
ALTER TABLE erp_customer DROP COLUMN email;
ALTER TABLE erp_customer DROP COLUMN fax;
ALTER TABLE erp_customer DROP COLUMN status;
ALTER TABLE erp_customer DROP COLUMN sort;
ALTER TABLE erp_customer DROP COLUMN tax_no;
ALTER TABLE erp_customer DROP COLUMN tax_percent;
ALTER TABLE erp_customer DROP COLUMN bank_name;
ALTER TABLE erp_customer DROP COLUMN bank_address;

-- 2. 添加新字段
ALTER TABLE erp_customer ADD COLUMN no VARCHAR(50) COMMENT '客户业务编号' AFTER id;
ALTER TABLE erp_customer ADD COLUMN receiver_name VARCHAR(100) COMMENT '收件姓名';
ALTER TABLE erp_customer ADD COLUMN address VARCHAR(500) COMMENT '详细地址';
ALTER TABLE erp_customer ADD COLUMN wechat_account VARCHAR(100) COMMENT '微信账号';
ALTER TABLE erp_customer ADD COLUMN alipay_account VARCHAR(100) COMMENT '支付宝号';

-- 3. 修改现有字段
ALTER TABLE erp_customer MODIFY COLUMN name VARCHAR(100) NOT NULL COMMENT '客户名称';
ALTER TABLE erp_customer MODIFY COLUMN telephone VARCHAR(50) COMMENT '联系电话';
ALTER TABLE erp_customer MODIFY COLUMN bank_account VARCHAR(100) COMMENT '银行账号';
ALTER TABLE erp_customer MODIFY COLUMN remark VARCHAR(1000) COMMENT '备注信息';

-- 4. 添加索引
ALTER TABLE erp_customer ADD INDEX idx_no (no);

COMMIT; 