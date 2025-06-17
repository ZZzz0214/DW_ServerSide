-- 财务金额表完整结构
-- 使用方法：
-- 1. 备份现有数据：mysqldump -u用户名 -p 数据库名 erp_finance_amount > backup_erp_finance_amount.sql
-- 2. 删除原表：DROP TABLE IF EXISTS erp_finance_amount;
-- 3. 执行此脚本创建新表

DROP TABLE IF EXISTS `erp_finance_amount`;

CREATE TABLE `erp_finance_amount` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '充值记录ID（主键，自增）',
  `no` varchar(255) NOT NULL COMMENT '编号',
  `carousel_images` varchar(1000) DEFAULT NULL COMMENT '轮播图片（可存储多张图片路径，用逗号分隔）',
  `channel_type` varchar(20) DEFAULT NULL COMMENT '充值渠道（微信、支付宝、银行卡）',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '操作金额',
  `operation_type` tinyint(1) DEFAULT NULL COMMENT '操作类型（1：充值，2：消费）',
  `before_balance` decimal(10,2) DEFAULT NULL COMMENT '操作前余额',
  `after_balance` decimal(10,2) DEFAULT NULL COMMENT '操作后余额',
  `wechat_recharge` decimal(10,2) DEFAULT NULL COMMENT '微信充值金额（已废弃，保留兼容）',
  `alipay_recharge` decimal(10,2) DEFAULT NULL COMMENT '支付宝充值金额（已废弃，保留兼容）',
  `bank_card_recharge` decimal(10,2) DEFAULT NULL COMMENT '银行卡充值金额（已废弃，保留兼容）',
  `wechat_balance` decimal(10,2) DEFAULT NULL COMMENT '微信当前余额（已废弃，保留兼容）',
  `alipay_balance` decimal(10,2) DEFAULT NULL COMMENT '支付宝当前余额（已废弃，保留兼容）',
  `bank_card_balance` decimal(10,2) DEFAULT NULL COMMENT '银行卡当前余额（已废弃，保留兼容）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注信息',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_no` (`no`),
  KEY `idx_creator_channel_type` (`creator`,`channel_type`),
  KEY `idx_creator_operation_type` (`creator`,`operation_type`),
  KEY `idx_creator_create_time` (`creator`,`create_time`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 财务充值记录表';

-- 插入示例数据（可选）
-- INSERT INTO `erp_finance_amount` (`no`, `channel_type`, `amount`, `operation_type`, `before_balance`, `after_balance`, `remark`, `creator`) VALUES
-- ('CWJE202412010001', '微信', 100.00, 1, 0.00, 100.00, '初始充值', 'admin'),
-- ('CWJE202412010002', '支付宝', 200.00, 1, 0.00, 200.00, '初始充值', 'admin'),
-- ('CWJE202412010003', '银行卡', 300.00, 1, 0.00, 300.00, '初始充值', 'admin');

-- 验证表结构
-- DESCRIBE erp_finance_amount; 