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

 Date: 09/06/2025 21:29:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_transit_sale
-- ----------------------------
DROP TABLE IF EXISTS `erp_transit_sale`;
CREATE TABLE `erp_transit_sale`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '中转销售表id（主键，自增）',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编号',
  `group_product_id` bigint(20) NOT NULL COMMENT '组品编号',
  `transit_person` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '中转人员',
  `distribution_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '代发单价（单位：元）',
  `wholesale_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '批发单价（单位：元）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `shipping_fee_type` tinyint(4) NULL DEFAULT NULL COMMENT '运费类型（0：固定运费，1：按件计费，2：按重计费）',
  `fixed_shipping_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '固定运费（单位：元）',
  `first_item_quantity` int(11) NULL DEFAULT NULL COMMENT '首件数量',
  `first_item_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '首件价格（单位：元）',
  `additional_item_quantity` int(11) NULL DEFAULT NULL COMMENT '续件数量',
  `additional_item_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '续件价格（单位：元）',
  `first_weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '首重重量（单位：kg）',
  `first_weight_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '首重价格（单位：元）',
  `additional_weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '续重重量（单位：kg）',
  `additional_weight_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '续重价格（单位：元）',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除（0：未删除，1：已删除）',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_transit_person_group_product` (`transit_person`, `group_product_id`) USING BTREE COMMENT '中转人员和组品ID唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 中转销售表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_transit_sale
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
