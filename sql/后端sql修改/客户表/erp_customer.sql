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

 Date: 19/06/2025 02:48:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_customer
-- ----------------------------
DROP TABLE IF EXISTS `erp_customer`;
CREATE TABLE `erp_customer`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '客户编号',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户名称',
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
  `mobile` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号码',
  `telephone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电子邮箱',
  `fax` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '传真',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(4) NOT NULL COMMENT '开启状态',
  `sort` int(11) NOT NULL COMMENT '排序',
  `tax_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '纳税人识别号',
  `tax_percent` decimal(24, 6) NULL DEFAULT NULL COMMENT '税率',
  `bank_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '开户行',
  `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '开户账号',
  `bank_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '开户地址',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 客户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_customer
-- ----------------------------
INSERT INTO `erp_customer` VALUES (3, '123123', NULL, NULL, NULL, NULL, NULL, NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-11 01:20:52', '1', '2025-04-11 01:20:52', b'0', 1);
INSERT INTO `erp_customer` VALUES (4, '123', NULL, NULL, NULL, NULL, NULL, NULL, 0, 2, NULL, NULL, NULL, NULL, NULL, '1', '2025-05-14 17:35:06', '1', '2025-05-14 17:35:06', b'0', 1);
INSERT INTO `erp_customer` VALUES (5, '帅哥', NULL, NULL, NULL, NULL, NULL, NULL, 0, 3, NULL, NULL, NULL, NULL, NULL, '1', '2025-05-20 12:59:51', '1', '2025-05-20 12:59:51', b'0', 1);
INSERT INTO `erp_customer` VALUES (6, '大哥', NULL, NULL, NULL, NULL, NULL, NULL, 0, 3, NULL, NULL, NULL, NULL, NULL, '1', '2025-05-20 22:55:13', '1', '2025-05-20 22:55:13', b'0', 1);
INSERT INTO `erp_customer` VALUES (7, '天才', NULL, NULL, NULL, NULL, NULL, NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, '1', '2025-05-22 14:59:05', '1', '2025-05-22 14:59:05', b'0', 1);

SET FOREIGN_KEY_CHECKS = 1;
