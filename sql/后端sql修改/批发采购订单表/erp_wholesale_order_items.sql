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

 Date: 26/03/2025 14:02:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_wholesale_order_items
-- ----------------------------
DROP TABLE IF EXISTS `erp_wholesale_order_items`;
CREATE TABLE `erp_wholesale_order_items`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `order_id` bigint(20) NOT NULL COMMENT '批发订单编号',
  `type` tinyint(4) NOT NULL COMMENT '产品类型：0-单品，1-组品',
  `product_id` bigint(20) NULL DEFAULT NULL COMMENT '产品编号（指向单品）',
  `combo_product_id` bigint(20) NULL DEFAULT NULL COMMENT '组品编号（指向组品）',
  `product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '产品名称（冗余字段）',
  `shipping_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发货编码',
  `product_quantity` int(11) NULL DEFAULT NULL COMMENT '产品数量',
  `purchaser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '采购人员',
  `supplier` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商名',
  `purchase_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '采购单价（单位：元）',
  `cargo_fee` decimal(24, 6) NULL DEFAULT NULL COMMENT '货拉拉费',
  `logistics_fee` decimal(24, 6) NULL DEFAULT NULL COMMENT '物流费用',
  `other_fees` decimal(24, 6) NULL DEFAULT NULL COMMENT '其他费用',
  `total_purchase_amount` decimal(24, 6) NULL DEFAULT NULL COMMENT '采购总额',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除（0：未删除，1：已删除）',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_combo_product_id`(`combo_product_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'ERP 批发订单项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of erp_wholesale_order_items
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
