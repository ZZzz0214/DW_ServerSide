/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80013
 Source Host           : localhost:3306
 Source Schema         : dw-erp

 Target Server Type    : MySQL
 Target Server Version : 80013
 File Encoding         : 65001

 Date: 17/06/2025 16:54:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_finance
-- ----------------------------
DROP TABLE IF EXISTS `erp_finance`;
CREATE TABLE `erp_finance`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '财务记录ID（主键，自增）',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编号',
  `carousel_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '轮播图片（可存储多张图片路径，用逗号分隔）',
  `bill_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账单名称',
  `amount` decimal(15, 2) NULL DEFAULT NULL COMMENT '收付金额',
  `income_expense` tinyint(1) NULL DEFAULT NULL COMMENT '收入支出（1：收入，2：支出）',
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收付类目',
  `account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收付账号',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '账单状态（1：待处理，2：已完成，3：已取消等）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `order_date` date NULL DEFAULT NULL COMMENT '下单日期',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除（0：未删除，1：已删除）',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_date`(`order_date` ASC) USING BTREE COMMENT '下单日期索引',
  INDEX `idx_status`(`status` ASC) USING BTREE COMMENT '账单状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '财务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_finance
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
