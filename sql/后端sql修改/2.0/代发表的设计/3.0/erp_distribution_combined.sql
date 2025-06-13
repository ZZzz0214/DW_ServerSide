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

 Date: 12/06/2025 17:04:35
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_distribution_combined
-- ----------------------------
DROP TABLE IF EXISTS `erp_distribution_combined`;
CREATE TABLE `erp_distribution_combined`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单编号',
  `no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单编号',
  `order_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单号',
  `logistics_company` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流公司',
  `tracking_number` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流单号',
  `receiver_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收件姓名',
  `receiver_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `receiver_address` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `original_product_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原表商品',
  `original_standard` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原表规格',
  `original_quantity` int(11) NULL DEFAULT NULL COMMENT '原表数量',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `combo_product_id` bigint(20) NULL DEFAULT NULL COMMENT '组品编号',
  `product_specification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '产品规格',
  `product_quantity` int(11) NULL DEFAULT NULL COMMENT '产品数量',
  `after_sales_status` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '售后状态',
  `after_sales_time` datetime NULL DEFAULT NULL COMMENT '售后时间',
  `purchase_other_fees` decimal(20, 2) NULL DEFAULT NULL COMMENT '采购杂费',
  `purchase_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '采购备注',
  `purchase_after_sales_status` int(11) NULL DEFAULT NULL COMMENT '采购售后状态',
  `purchase_after_sales_amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '采购售后金额',
  `purchase_approval_time` datetime NULL DEFAULT NULL COMMENT '采购审批时间',
  `purchase_after_sales_time` datetime NULL DEFAULT NULL COMMENT '采购售后时间',
  `purchase_audit_status` int(11) NULL DEFAULT NULL COMMENT '采购审核状态',
  `purchase_unapprove_time` datetime NULL DEFAULT NULL COMMENT '采购反审批时间',
  `salesperson` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '销售人员',
  `customer_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户名称',
  `sale_other_fees` decimal(20, 2) NULL DEFAULT NULL COMMENT '出货杂费',
  `sale_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出货备注',
  `transfer_person` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '中转人员',
  `sale_after_sales_status` int(11) NULL DEFAULT NULL COMMENT '销售售后状态',
  `sale_after_sales_amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '销售售后金额',
  `sale_after_sales_time` datetime NULL DEFAULT NULL COMMENT '销售售后时间',
  `sale_audit_status` int(11) NULL DEFAULT NULL COMMENT '销售审核状态',
  `sale_approval_time` datetime NULL DEFAULT NULL COMMENT '销售审批时间',
  `sale_unapprove_time` datetime NULL DEFAULT NULL COMMENT '销售反审批时间',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建人员',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人员',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `tenant_id` bigint(20) NULL DEFAULT NULL COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代发合并表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_distribution_combined
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
