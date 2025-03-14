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

 Date: 14/03/2025 20:33:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_combo_product
-- ----------------------------
DROP TABLE IF EXISTS `erp_combo_product`;
CREATE TABLE `erp_combo_product`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '组品编号（主键，自增）',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组合产品名称',
  `short_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品简称',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品图片',
  `shipping_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发货编码',
  `weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '产品重量（单位：kg）',
  `purchaser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '采购人员',
  `supplier` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '供应商名',
  `purchase_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '采购单价（单位：元）',
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
  `total_quantity` int(11) NULL DEFAULT NULL COMMENT '产品数量（组合产品中包含的单品总数）',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除（0：未删除，1：已删除）',
  `tenant_id` bigint(20) NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '组合产品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_combo_product
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
