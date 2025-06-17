-- 财务金额表重构 - 数据库迁移脚本
-- 执行时间：请在系统维护时间执行
-- 说明：为 erp_finance_amount 表添加新字段，支持单条记录的充值/消费模式

-- 1. 添加新字段
ALTER TABLE erp_finance_amount 
ADD COLUMN channel_type VARCHAR(20) COMMENT '充值渠道（微信、支付宝、银行卡）' AFTER carousel_images,
ADD COLUMN amount DECIMAL(10,2) COMMENT '充值金额' AFTER channel_type,
ADD COLUMN operation_type TINYINT(1) COMMENT '操作类型（1：充值，2：消费）' AFTER amount,
ADD COLUMN before_balance DECIMAL(10,2) COMMENT '充值前余额' AFTER operation_type,
ADD COLUMN after_balance DECIMAL(10,2) COMMENT '充值后余额' AFTER before_balance;

-- 2. 为新字段添加索引（提升查询性能）
ALTER TABLE erp_finance_amount 
ADD INDEX idx_creator_channel_type (creator, channel_type),
ADD INDEX idx_creator_operation_type (creator, operation_type),
ADD INDEX idx_creator_create_time (creator, create_time);

-- 3. 数据迁移（可选）
-- 如果需要将现有的余额数据迁移到新结构，可以执行以下语句
-- 注意：这会为每个用户的每个渠道创建一条初始化记录

-- 3.1 迁移微信数据
INSERT INTO erp_finance_amount (
    no, channel_type, amount, operation_type, before_balance, after_balance, 
    remark, creator, create_time, update_time, deleted, tenant_id,
    wechat_recharge, wechat_balance
)
SELECT 
    CONCAT('INIT_WX_', id) as no,
    '微信' as channel_type,
    wechat_recharge as amount,
    1 as operation_type,
    0 as before_balance,
    wechat_balance as after_balance,
    '数据迁移-微信初始余额' as remark,
    creator,
    create_time,
    update_time,
    deleted,
    tenant_id,
    wechat_recharge,
    wechat_balance
FROM erp_finance_amount 
WHERE wechat_recharge > 0 OR wechat_balance > 0;

-- 3.2 迁移支付宝数据
INSERT INTO erp_finance_amount (
    no, channel_type, amount, operation_type, before_balance, after_balance, 
    remark, creator, create_time, update_time, deleted, tenant_id,
    alipay_recharge, alipay_balance
)
SELECT 
    CONCAT('INIT_ALI_', id) as no,
    '支付宝' as channel_type,
    alipay_recharge as amount,
    1 as operation_type,
    0 as before_balance,
    alipay_balance as after_balance,
    '数据迁移-支付宝初始余额' as remark,
    creator,
    create_time,
    update_time,
    deleted,
    tenant_id,
    alipay_recharge,
    alipay_balance
FROM erp_finance_amount 
WHERE alipay_recharge > 0 OR alipay_balance > 0;

-- 3.3 迁移银行卡数据
INSERT INTO erp_finance_amount (
    no, channel_type, amount, operation_type, before_balance, after_balance, 
    remark, creator, create_time, update_time, deleted, tenant_id,
    bank_card_recharge, bank_card_balance
)
SELECT 
    CONCAT('INIT_BANK_', id) as no,
    '银行卡' as channel_type,
    bank_card_recharge as amount,
    1 as operation_type,
    0 as before_balance,
    bank_card_balance as after_balance,
    '数据迁移-银行卡初始余额' as remark,
    creator,
    create_time,
    update_time,
    deleted,
    tenant_id,
    bank_card_recharge,
    bank_card_balance
FROM erp_finance_amount 
WHERE bank_card_recharge > 0 OR bank_card_balance > 0;

-- 4. 验证数据迁移结果
-- 执行以下查询来验证迁移是否成功
SELECT 
    creator,
    channel_type,
    COUNT(*) as record_count,
    SUM(CASE WHEN operation_type = 1 THEN amount ELSE 0 END) as total_recharge,
    MAX(after_balance) as current_balance
FROM erp_finance_amount 
WHERE channel_type IS NOT NULL
GROUP BY creator, channel_type
ORDER BY creator, channel_type;

-- 5. 清理旧数据（谨慎操作）
-- 数据迁移完成并验证无误后，可以考虑删除原有的汇总记录
-- 建议先备份数据，确认新系统运行正常后再执行
-- DELETE FROM erp_finance_amount WHERE channel_type IS NULL;

-- 6. 添加字段注释
ALTER TABLE erp_finance_amount 
MODIFY COLUMN channel_type VARCHAR(20) COMMENT '充值渠道（微信、支付宝、银行卡）',
MODIFY COLUMN amount DECIMAL(10,2) COMMENT '操作金额',
MODIFY COLUMN operation_type TINYINT(1) COMMENT '操作类型（1：充值，2：消费）',
MODIFY COLUMN before_balance DECIMAL(10,2) COMMENT '操作前余额',
MODIFY COLUMN after_balance DECIMAL(10,2) COMMENT '操作后余额';

-- 执行完成后的表结构示例：
/*
CREATE TABLE `erp_finance_amount` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '充值记录ID（主键，自增）',
  `no` varchar(255) NOT NULL COMMENT '编号',
  `carousel_images` varchar(1000) DEFAULT NULL COMMENT '轮播图片（可存储多张图片路径，用逗号分隔）',
  `channel_type` varchar(20) DEFAULT NULL COMMENT '充值渠道（微信、支付宝、银行卡）',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '操作金额',
  `operation_type` tinyint(1) DEFAULT NULL COMMENT '操作类型（1：充值，2：消费）',
  `before_balance` decimal(10,2) DEFAULT NULL COMMENT '操作前余额',
  `after_balance` decimal(10,2) DEFAULT NULL COMMENT '操作后余额',
  `wechat_recharge` decimal(10,2) DEFAULT NULL COMMENT '微信充值金额（已废弃）',
  `alipay_recharge` decimal(10,2) DEFAULT NULL COMMENT '支付宝充值金额（已废弃）',
  `bank_card_recharge` decimal(10,2) DEFAULT NULL COMMENT '银行卡充值金额（已废弃）',
  `wechat_balance` decimal(10,2) DEFAULT NULL COMMENT '微信当前余额（已废弃）',
  `alipay_balance` decimal(10,2) DEFAULT NULL COMMENT '支付宝当前余额（已废弃）',
  `bank_card_balance` decimal(10,2) DEFAULT NULL COMMENT '银行卡当前余额（已废弃）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注信息',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_creator_channel_type` (`creator`,`channel_type`),
  KEY `idx_creator_operation_type` (`creator`,`operation_type`),
  KEY `idx_creator_create_time` (`creator`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务充值记录表';
*/ 