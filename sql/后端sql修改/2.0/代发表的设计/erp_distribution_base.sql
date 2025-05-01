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

 Date: 01/05/2025 17:55:15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_distribution_base
-- ----------------------------
DROP TABLE IF EXISTS `erp_distribution_base`;
CREATE TABLE `erp_distribution_base`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '代发主键',
  `no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单编号',
  `logistics_company` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流公司',
  `tracking_number` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流单号',
  `receiver_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收件姓名',
  `receiver_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `receiver_address` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `original_product_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原表商品',
  `original_standard` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原表规格',
  `original_quantity` int(11) NULL DEFAULT NULL COMMENT '原表数量',
  `after_sales_status` int(11) NULL DEFAULT NULL COMMENT '售后状况',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `combo_product_id` bigint(20) NULL DEFAULT NULL COMMENT '组品编号',
  `product_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '产品名称（->组品编号）',
  `shipping_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发货编码（->组品编号）',
  `product_quantity` int(11) NULL DEFAULT NULL COMMENT '产品数量',
  `tenant_id` bigint(20) NULL DEFAULT NULL COMMENT '租户编号',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint(4) NULL DEFAULT NULL COMMENT '代发状态',
  `order_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单号',
  `product_specification` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '产品规格',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代发基础表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_distribution_base
-- ----------------------------
INSERT INTO `erp_distribution_base` VALUES (17, 'DFJL20250430000004', '物流公司1', '物流单号', '收件姓名', '联系电话', '收件地址', '原表山坡', '原表规格', 1, 123, '备注信息', NULL, '称王*1 + 笔记本*1', '123', 8, 1, b'0', '1', '2025-04-30 20:12:52', '1', '2025-04-30 20:12:52', 10, NULL, NULL);
INSERT INTO `erp_distribution_base` VALUES (18, 'DFJL20250430000005', '物流公司1', '物流单号', '收件姓名', '联系电话', '收件地址', '原表山坡', '原表规格', 1, 123, '123', NULL, '称王*1 + 笔记本*1', '123', 2, 1, b'0', '1', '2025-04-30 23:13:24', '1', '2025-04-30 23:13:24', 10, NULL, NULL);
INSERT INTO `erp_distribution_base` VALUES (19, 'DFJL20250430000006', '', '123', '123', '123', '123', '123', '123', 1, 123, '123', NULL, '123123*5 + 一加Ace2Pro*3', '123', 1, 1, b'0', '1', '2025-04-30 23:17:48', '1', '2025-04-30 23:17:48', 10, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
