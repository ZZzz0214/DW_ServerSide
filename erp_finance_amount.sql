CREATE TABLE `erp_finance_amount`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '财务金额记录ID（主键，自增）',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编号',
  `carousel_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '轮播图片（可存储多张图片路径，用逗号分隔）',
  `wechat_recharge` decimal(15, 2) NULL DEFAULT 0.00 COMMENT '微信充值金额',
  `alipay_recharge` decimal(15, 2) NULL DEFAULT 0.00 COMMENT '支付宝充值金额',
  `bank_card_recharge` decimal(15, 2) NULL DEFAULT 0.00 COMMENT '银行卡充值金额',
  `wechat_balance` decimal(15, 2) NULL DEFAULT 0.00 COMMENT '微信当前余额',
  `alipay_balance` decimal(15, 2) NULL DEFAULT 0.00 COMMENT '支付宝当前余额',
  `bank_card_balance` decimal(15, 2) NULL DEFAULT 0.00 COMMENT '银行卡当前余额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID（关联当前登录用户）',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除（0：未删除，1：已删除）',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE COMMENT '租户ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '财务金额表' ROW_FORMAT = DYNAMIC; 