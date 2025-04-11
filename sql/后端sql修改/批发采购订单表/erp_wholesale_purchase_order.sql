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

 Date: 11/04/2025 23:44:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_wholesale_purchase_order
-- ----------------------------
DROP TABLE IF EXISTS `erp_wholesale_purchase_order`;
CREATE TABLE `erp_wholesale_purchase_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单编号（主键，自增）',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `status` tinyint(4) NOT NULL COMMENT '采购状态',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商编号',
  `account_id` bigint(20) NULL DEFAULT NULL COMMENT '结算账户编号',
  `order_time` datetime NULL DEFAULT NULL COMMENT '采购时间',
  `logistics_company` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物流公司',
  `tracking_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物流单号',
  `receiver_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收件姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `receiver_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `total_count` decimal(24, 6) NULL DEFAULT NULL COMMENT '合计数量',
  `total_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '合计价格，单位：元',
  `total_product_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '合计产品价格，单位：元',
  `total_tax_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '合计税额，单位：元',
  `discount_percent` decimal(24, 6) NULL DEFAULT NULL COMMENT '优惠率，百分比',
  `discount_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '优惠金额，单位：元',
  `deposit_price` decimal(24, 6) NULL DEFAULT 0.000000 COMMENT '定金金额，单位：元',
  `logistics_fee` decimal(24, 6) NULL DEFAULT NULL COMMENT '物流费用（合计）',
  `hulala_fee` decimal(24, 6) NULL DEFAULT NULL COMMENT '货拉拉费用（合计）',
  `other_fees` decimal(24, 6) NULL DEFAULT NULL COMMENT '其他费用（合计）',
  `total_purchase_amount` decimal(24, 6) NULL DEFAULT NULL COMMENT '采购总额（合计）',
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '附件地址',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `in_count` decimal(24, 6) NULL DEFAULT 0.000000 COMMENT '采购入库数量',
  `return_count` decimal(24, 6) NULL DEFAULT 0.000000 COMMENT '采购退货数量',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `no`(`no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 批发采购订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_wholesale_purchase_order
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
