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

 Date: 14/03/2025 20:33:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_combo_product_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_combo_product_item`;
CREATE TABLE `erp_combo_product_item`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `combo_product_id` bigint(20) NOT NULL COMMENT '组合产品编号',
  `item_product_id` bigint(20) NOT NULL COMMENT '单品产品编号',
  `item_quantity` int(11) NOT NULL COMMENT '单品数量',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除（0：未删除，1：已删除）',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_combo_product_id`(`combo_product_id` ASC) USING BTREE COMMENT '组合产品编号索引',
  INDEX `idx_item_product_id`(`item_product_id` ASC) USING BTREE COMMENT '单品产品编号索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '组合产品与单品关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_combo_product_item
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
