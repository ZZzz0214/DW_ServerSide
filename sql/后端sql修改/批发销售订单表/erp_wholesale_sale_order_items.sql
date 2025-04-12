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

 Date: 12/04/2025 17:41:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_wholesale_sale_order_items
-- ----------------------------
DROP TABLE IF EXISTS `erp_wholesale_sale_order_items`;
CREATE TABLE `erp_wholesale_sale_order_items`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT '批发销售订单编号',
  `group_product_id` bigint(20) NULL DEFAULT NULL COMMENT '组品编号',
  `product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品名称',
  `customer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户名称',
  `sales_person` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '销售人员',
  `original_product` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原表商品',
  `original_specification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原表规格',
  `original_quantity` int(11) NULL DEFAULT NULL COMMENT '原表数量',
  `product_quantity` int(11) NULL DEFAULT NULL COMMENT '产品数量',
  `logistics_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '物流费用（原出货运费）',
  `other_fees` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '其他费用',
  `shipping_total` decimal(24, 6) NULL DEFAULT 0.000000 COMMENT '出货总额',
  `product_id` bigint(20) NULL DEFAULT NULL COMMENT '产品编号',
  `product_unit_id` bigint(20) NULL DEFAULT NULL COMMENT '产品单位编号',
  `wholesale_product_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '批发出货单价（原产品单价）',
  `count` decimal(24, 6) NULL DEFAULT NULL COMMENT '数量',
  `total_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '总价',
  `tax_percent` decimal(24, 6) NULL DEFAULT NULL COMMENT '税率，百分比',
  `tax_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '税额，单位：元',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `out_count` decimal(24, 6) NULL DEFAULT 0.000000 COMMENT '销售出库数量',
  `return_count` decimal(24, 6) NULL DEFAULT 0.000000 COMMENT '销售退货数量',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  `hulala_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '货拉拉费用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 批发销售订单项表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_wholesale_sale_order_items
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
