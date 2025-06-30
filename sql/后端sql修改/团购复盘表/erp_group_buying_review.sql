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

 Date: 16/05/2025 15:51:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_group_buying_review
-- ----------------------------
DROP TABLE IF EXISTS `erp_group_buying_review`;
CREATE TABLE `erp_group_buying_review`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '团购复盘编号',
  `no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '编号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `customer_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户名称',
  `group_buying_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '团购货盘表编号',
  `supply_group_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '供团价格',
  `sample_send_date` date NULL DEFAULT NULL COMMENT '寄样日期',
  `group_start_date` date NULL DEFAULT NULL COMMENT '开团日期',
  `group_sales` int(11) NULL DEFAULT NULL COMMENT '开团销量',
  `repeat_group_date` date NULL DEFAULT NULL COMMENT '复团日期',
  `repeat_group_sales` int(11) NULL DEFAULT NULL COMMENT '复团销量',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_customer_id`(`customer_id` ASC) USING BTREE,
  INDEX `idx_group_buying_id`(`group_buying_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 团购复盘表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_group_buying_review
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
