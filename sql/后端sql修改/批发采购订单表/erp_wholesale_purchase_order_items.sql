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

 Date: 11/04/2025 23:44:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_wholesale_purchase_order_items
-- ----------------------------
DROP TABLE IF EXISTS `erp_wholesale_purchase_order_items`;
CREATE TABLE `erp_wholesale_purchase_order_items`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT '采购订单编号',
  `type` tinyint(4) NOT NULL COMMENT '产品类型：0-单品，1-组合产品',
  `product_id` bigint(20) NULL DEFAULT NULL COMMENT '产品编号（指向单品或组合产品）',
  `combo_product_id` bigint(20) NULL DEFAULT NULL COMMENT '组品编号',
  `original_product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原表商品名称',
  `original_standard` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原表规格',
  `original_quantity` int(11) NULL DEFAULT NULL COMMENT '原表数量',
  `after_sales_status` tinyint(4) NULL DEFAULT NULL COMMENT '售后状况',
  `shipping_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发货编码',
  `product_quantity` int(11) NULL DEFAULT NULL COMMENT '产品数量',
  `purchase_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '采购单价(批发)',
  `logistics_fee` decimal(24, 6) NULL DEFAULT NULL COMMENT '物流费用',
  `hulala_fee` decimal(24, 6) NULL DEFAULT NULL COMMENT '货拉拉费用',
  `other_fees` decimal(24, 6) NULL DEFAULT NULL COMMENT '其他费用',
  `total_purchase_amount` decimal(24, 6) NULL DEFAULT NULL COMMENT '采购总额',
  `total_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '总价',
  `product_unit_id` bigint(20) NULL DEFAULT NULL COMMENT '产品单位编号',
  `count` decimal(24, 6) NULL DEFAULT NULL COMMENT '数量',
  `tax_percent` decimal(24, 6) NULL DEFAULT NULL COMMENT '税率，百分比',
  `tax_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '税额，单位：元',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `purchaser` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '采购人员',
  `in_count` decimal(24, 6) NULL DEFAULT 0.000000 COMMENT '采购入库数量',
  `return_count` decimal(24, 6) NULL DEFAULT 0.000000 COMMENT '采购退货数量',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 批发采购订单项表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_wholesale_purchase_order_items
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
