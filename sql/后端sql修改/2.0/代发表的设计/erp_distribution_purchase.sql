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

 Date: 25/05/2025 17:51:43
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_distribution_purchase
-- ----------------------------
DROP TABLE IF EXISTS `erp_distribution_purchase`;
CREATE TABLE `erp_distribution_purchase`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `base_id` bigint(20) NOT NULL COMMENT '关联代发基础表',
  `combo_product_id` bigint(20) NULL DEFAULT NULL COMMENT '关联组品表',
  `purchaser` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '采购人员',
  `supplier` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商名',
  `purchase_price` decimal(20, 2) NULL DEFAULT NULL COMMENT '采购单价',
  `shipping_fee` decimal(20, 2) NULL DEFAULT NULL COMMENT '采购运费',
  `other_fees` decimal(20, 2) NULL DEFAULT NULL COMMENT '其他费用',
  `total_purchase_amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '采购总额',
  `tenant_id` bigint(20) NULL DEFAULT NULL COMMENT '租户编号',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `purchase_after_sales_status` int(11) NULL DEFAULT NULL COMMENT '采购售后状态',
  `purchase_after_sales_situation` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '采购售后情况',
  `purchase_after_sales_amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '采购售后金额',
  `purchase_approval_time` datetime NULL DEFAULT NULL COMMENT '采购审批时间',
  `purchase_after_sales_time` datetime NULL DEFAULT NULL COMMENT '采购售后时间',
  `purchase_audit_status` int(11) NULL DEFAULT NULL COMMENT '采购审核状态',
  `purchase_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '采购备注信息',
  `purchase_unapprove_time` datetime NULL DEFAULT NULL COMMENT '采购反审批时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `combo_product_id`(`combo_product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代发采购表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_distribution_purchase
-- ----------------------------
INSERT INTO `erp_distribution_purchase` VALUES (23, 20, 35, '123', '123', 12.00, 15.00, 1.00, 28.00, 1, b'0', '1', '2025-05-22 02:12:11', '1', '2025-05-22 02:12:11', 30, NULL, NULL, NULL, NULL, 10, NULL, NULL);
INSERT INTO `erp_distribution_purchase` VALUES (24, 21, 35, '123', '123', 12.00, 45.00, 1.00, 82.00, 1, b'0', '1', '2025-05-22 15:05:15', '1', '2025-05-22 16:07:44', 30, NULL, NULL, NULL, NULL, 10, NULL, NULL);
INSERT INTO `erp_distribution_purchase` VALUES (25, 22, 35, '123', '123', 12.00, 35.00, 2.00, 61.00, 1, b'0', '1', '2025-05-22 16:48:09', '1', '2025-05-22 22:32:09', 30, NULL, 4.00, NULL, '2025-05-30 17:30:35', 20, NULL, NULL);
INSERT INTO `erp_distribution_purchase` VALUES (26, 23, 35, '123', '123', 12.00, 15.00, 1.00, 28.00, 1, b'0', '1', '2025-05-22 23:24:20', '1', '2025-05-22 23:24:20', 30, NULL, NULL, NULL, NULL, 10, NULL, NULL);
INSERT INTO `erp_distribution_purchase` VALUES (27, 24, 35, '123', '123', 12.00, 45.00, 1.00, 82.00, 1, b'0', '1', '2025-05-22 23:25:10', '1', '2025-05-22 23:25:10', 30, NULL, NULL, NULL, NULL, 10, NULL, NULL);
INSERT INTO `erp_distribution_purchase` VALUES (28, 25, 35, '123', '123', 12.00, 15.00, 3.00, 30.00, 1, b'0', '1', '2025-05-22 23:31:07', '1', '2025-05-22 23:31:07', 30, NULL, NULL, NULL, NULL, 10, '采购备注', NULL);
INSERT INTO `erp_distribution_purchase` VALUES (29, 26, 35, '123', '123', 12.00, 15.00, 1.00, 28.00, 1, b'0', '1', '2025-05-22 23:38:50', '1', '2025-05-22 23:38:50', 30, NULL, NULL, NULL, NULL, 10, '采购备注', NULL);

SET FOREIGN_KEY_CHECKS = 1;
