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

 Date: 22/04/2025 23:32:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_wholesale_purchase
-- ----------------------------
DROP TABLE IF EXISTS `erp_wholesale_purchase`;
CREATE TABLE `erp_wholesale_purchase`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `base_id` bigint(20) NOT NULL COMMENT '关联批发基础表',
  `combo_product_id` bigint(20) NULL DEFAULT NULL COMMENT '关联组品表',
  `purchaser` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '采购人员（->组品编号）',
  `supplier` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商名（->组品编号）',
  `purchase_price` decimal(20, 2) NULL DEFAULT NULL COMMENT '采购单价（->组品编号）',
  `truck_fee` decimal(20, 2) NULL DEFAULT NULL COMMENT '货拉拉费',
  `logistics_fee` decimal(20, 2) NULL DEFAULT NULL COMMENT '物流费用',
  `other_fees` decimal(20, 2) NULL DEFAULT NULL COMMENT '其他费用',
  `total_purchase_amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '采购总额',
  `tenant_id` bigint(20) NULL DEFAULT NULL COMMENT '租户编号',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `combo_product_id`(`combo_product_id` ASC) USING BTREE,
  CONSTRAINT `erp_wholesale_purchase_ibfk_1` FOREIGN KEY (`combo_product_id`) REFERENCES `erp_combo_product` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '批发采购表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of erp_wholesale_purchase
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
