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

 Date: 22/04/2025 23:32:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_distribution_sale
-- ----------------------------
DROP TABLE IF EXISTS `erp_distribution_sale`;
CREATE TABLE `erp_distribution_sale`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `base_id` bigint(20) NOT NULL COMMENT '关联代发基础表',
  `sale_price_id` bigint(20) NULL DEFAULT NULL COMMENT '关联销售价格表',
  `salesperson` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '销售人员',
  `customer_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户名称',
  `sale_price` decimal(20, 2) NULL DEFAULT NULL COMMENT '出货单价',
  `shipping_fee` decimal(20, 2) NULL DEFAULT NULL COMMENT '出货运费',
  `other_fees` decimal(20, 2) NULL DEFAULT NULL COMMENT '其他费用',
  `total_sale_amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '出货总额',
  `tenant_id` bigint(20) NULL DEFAULT NULL COMMENT '租户编号',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `sale_price_id`(`sale_price_id` ASC) USING BTREE,
  CONSTRAINT `erp_distribution_sale_ibfk_1` FOREIGN KEY (`sale_price_id`) REFERENCES `erp_sale_price` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代发销售表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of erp_distribution_sale
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
