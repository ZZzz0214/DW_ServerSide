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

 Date: 06/04/2025 23:59:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_sale_order_items
-- ----------------------------
DROP TABLE IF EXISTS `erp_sale_order_items`;
CREATE TABLE `erp_sale_order_items`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `order_id` bigint(20) NOT NULL COMMENT '销售订单编号',
  `product_id` bigint(20) NOT NULL COMMENT '产品编号',
  `product_unit_id` bigint(20) NOT NULL COMMENT '产品单位单位',
  `product_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '产品单价',
  `count` decimal(24, 6) NOT NULL COMMENT '数量',
  `total_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '总价',
  `tax_percent` decimal(24, 6) NULL DEFAULT NULL COMMENT '税率，百分比',
  `tax_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '税额，单位：元',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `out_count` decimal(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '销售出库数量',
  `return_count` decimal(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '销售退货数量',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 销售订单项表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_sale_order_items
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
