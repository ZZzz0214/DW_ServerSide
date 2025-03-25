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

 Date: 25/03/2025 22:54:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_purchase_order
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_order`;
CREATE TABLE `erp_purchase_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购单编号',
  `status` tinyint(4) NOT NULL COMMENT '采购状态',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商编号',
  `account_id` bigint(20) NULL DEFAULT NULL COMMENT '结算账户编号',
  `order_time` datetime NOT NULL COMMENT '采购时间',
  `total_count` decimal(24, 6) NOT NULL COMMENT '合计数量',
  `total_price` decimal(24, 6) NOT NULL COMMENT '合计价格，单位：元',
  `total_product_price` decimal(24, 6) NOT NULL COMMENT '合计产品价格，单位：元',
  `total_tax_price` decimal(24, 6) NOT NULL COMMENT '合计税额，单位：元',
  `discount_percent` decimal(24, 6) NOT NULL COMMENT '优惠率，百分比',
  `discount_price` decimal(24, 6) NOT NULL COMMENT '优惠金额，单位：元',
  `deposit_price` decimal(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '定金金额，单位：元',
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '附件地址',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `in_count` decimal(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '采购入库数量',
  `return_count` decimal(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '采购退货数量',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `no`(`no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 采购订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_purchase_order
-- ----------------------------
INSERT INTO `erp_purchase_order` VALUES (1, 'PO202402280001', 10, 1, 1, '2025-02-05 21:50:34', 100.000000, 5000.000000, 5000.000000, 650.000000, 0.000000, 0.000000, 0.000000, NULL, '测试采购订单', 0.000000, 0.000000, 'admin', '2025-02-05 21:50:34', 'admin', '2025-02-05 21:50:34', b'0', 1);

SET FOREIGN_KEY_CHECKS = 1;
