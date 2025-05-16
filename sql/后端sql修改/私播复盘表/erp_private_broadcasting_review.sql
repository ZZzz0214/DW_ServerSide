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

 Date: 16/05/2025 19:49:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_private_broadcasting_review
-- ----------------------------
DROP TABLE IF EXISTS `erp_private_broadcasting_review`;
CREATE TABLE `erp_private_broadcasting_review`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '私播复盘编号',
  `no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编号',
  `private_broadcasting_id` bigint(20) NULL DEFAULT NULL COMMENT '私播货盘表ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `customer_id` bigint(20) NULL DEFAULT NULL COMMENT '客户ID',
  `product_naked_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '产品裸价',
  `express_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '快递费用',
  `dropship_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '代发价格',
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
  INDEX `idx_private_broadcasting_id`(`private_broadcasting_id` ASC) USING BTREE,
  INDEX `idx_customer_id`(`customer_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 私播复盘表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_private_broadcasting_review
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
