-- ===================================
-- 客户表字段调整SQL - 兼容版本
-- 执行前请先备份数据库！
-- 注意：如果某个字段已经被删除，请跳过对应的DROP语句
-- ===================================

-- 1. 备份原表数据
CREATE TABLE erp_customer_backup AS SELECT * FROM erp_customer;

-- 2. 删除不需要的字段（如果字段不存在会报错，请忽略错误继续执行）
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

-- 3. 添加新字段
ALTER TABLE erp_customer ADD COLUMN receiver_name varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收件姓名' AFTER name;
ALTER TABLE erp_customer ADD COLUMN address varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址' AFTER telephone;
ALTER TABLE erp_customer ADD COLUMN wechat_account varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信账号' AFTER address;
ALTER TABLE erp_customer ADD COLUMN alipay_account varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付宝号' AFTER wechat_account;

-- 4. 修改现有字段的长度和注释
ALTER TABLE erp_customer MODIFY COLUMN name varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户名称';
ALTER TABLE erp_customer MODIFY COLUMN telephone varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话';
ALTER TABLE erp_customer MODIFY COLUMN bank_account varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '银行账号';
ALTER TABLE erp_customer MODIFY COLUMN remark varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息';

-- 5. 验证表结构（可选，用于检查修改结果）
-- DESC erp_customer;

-- ===================================
-- 执行完成后的最终表结构应该是：
-- id              bigint(20)    客户编号 (主键)
-- name            varchar(100)  客户名称 (必填)
-- receiver_name   varchar(100)  收件姓名
-- telephone       varchar(20)   联系电话  
-- address         varchar(500)  详细地址
-- wechat_account  varchar(100)  微信账号
-- alipay_account  varchar(100)  支付宝号
-- bank_account    varchar(50)   银行账号
-- remark          varchar(500)  备注信息
-- creator         varchar(64)   创建者
-- create_time     datetime      创建时间
-- updater         varchar(64)   更新者
-- update_time     datetime      更新时间
-- deleted         bit(1)        是否删除
-- tenant_id       bigint(20)    租户编号
-- =================================== 