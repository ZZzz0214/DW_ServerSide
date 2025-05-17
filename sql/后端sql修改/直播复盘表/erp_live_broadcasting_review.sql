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

 Date: 17/05/2025 17:52:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_live_broadcasting_review
-- ----------------------------
DROP TABLE IF EXISTS `erp_live_broadcasting_review`;
CREATE TABLE `erp_live_broadcasting_review`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '直播复盘编号',
  `no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编号',
  `live_broadcasting_id` bigint(20) NULL DEFAULT NULL COMMENT '直播货盘表ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `customer_id` bigint(20) NULL DEFAULT NULL COMMENT '客户ID',
  `live_platform` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '直播平台',
  `live_commission` decimal(10, 2) NULL DEFAULT NULL COMMENT '直播佣金',
  `public_commission` decimal(10, 2) NULL DEFAULT NULL COMMENT '公开佣金',
  `rebate_commission` decimal(10, 2) NULL DEFAULT NULL COMMENT '返点佣金',
  `sample_send_date` date NULL DEFAULT NULL COMMENT '寄样日期',
  `live_start_date` date NULL DEFAULT NULL COMMENT '开播日期',
  `live_sales` int(11) NULL DEFAULT NULL COMMENT '开播销量',
  `repeat_live_date` date NULL DEFAULT NULL COMMENT '复播日期',
  `repeat_live_sales` int(11) NULL DEFAULT NULL COMMENT '复播销量',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_live_broadcasting_id`(`live_broadcasting_id` ASC) USING BTREE,
  INDEX `idx_customer_id`(`customer_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 直播复盘表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_live_broadcasting_review
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
