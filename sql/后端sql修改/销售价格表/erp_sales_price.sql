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

 Date: 26/03/2025 18:21:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_sales_price
-- ----------------------------
DROP TABLE IF EXISTS `erp_sales_price`;
CREATE TABLE `erp_sales_price`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '销售价格表编号（主键，自增）',
  `type` tinyint(4) NOT NULL COMMENT '产品类型：0-单品，1-组品',
  `product_id` bigint(20) NULL DEFAULT NULL COMMENT '单品编号（指向 erp_product 表）',
  `combo_product_id` bigint(20) NULL DEFAULT NULL COMMENT '组品编号（指向 erp_combo_product 表）',
  `customer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户名称',
  `agent_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '代发单价（单位：元）',
  `wholesale_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '批发单价（单位：元）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `shipping_fee_type` tinyint(4) NULL DEFAULT NULL COMMENT '运费类型：0-固定运费，1-按件计费，2-按重计费',
  `fixed_shipping_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '固定运费（单位：元）',
  `first_item_quantity` int(11) NULL DEFAULT NULL COMMENT '首件数量',
  `first_item_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '首件价格（单位：元）',
  `additional_item_quantity` int(11) NULL DEFAULT NULL COMMENT '续件数量',
  `additional_item_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '续件价格（单位：元）',
  `first_weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '首重重量（单位：kg）',
  `first_weight_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '首重价格（单位：元）',
  `additional_weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '续重重量（单位：kg）',
  `additional_weight_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '续重价格（单位：元）',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除（0：未删除，1：已删除）',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_combo_product_id`(`combo_product_id` ASC) USING BTREE,
  INDEX `idx_customer_name`(`customer_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'ERP 销售价格表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of erp_sales_price
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
