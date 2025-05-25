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

 Date: 25/05/2025 17:51:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_distribution_sale
-- ----------------------------
DROP TABLE IF EXISTS `erp_distribution_sale`;
CREATE TABLE `erp_distribution_sale`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `base_id` bigint(20) NOT NULL COMMENT '关联代发基础表',
  `sale_price_id` bigint(20) NULL DEFAULT NULL COMMENT '关联销售价格表',
  `salesperson` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '销售人员',
  `customer_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户名称',
  `sale_price` decimal(20, 2) NULL DEFAULT NULL COMMENT '出货单价',
  `shipping_fee` decimal(20, 2) NULL DEFAULT NULL COMMENT '出货运费',
  `other_fees` decimal(20, 2) NULL DEFAULT NULL COMMENT '其他费用',
  `total_sale_amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '出货总额',
  `tenant_id` bigint(20) NULL DEFAULT NULL COMMENT '租户编号',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `sale_after_sales_status` int(11) NULL DEFAULT NULL COMMENT '销售售后状态',
  `sale_after_sales_situation` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '销售售后情况',
  `sale_after_sales_amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '销售售后金额',
  `sale_after_sales_time` datetime NULL DEFAULT NULL COMMENT '销售售后时间',
  `sale_audit_status` int(11) NULL DEFAULT NULL COMMENT '销售审核状态',
  `sale_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出货备注信息',
  `transfer_person` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '中转人员',
  `sale_approval_time` datetime NULL DEFAULT NULL COMMENT '销售审批时间',
  `sale_unapprove_time` datetime NULL DEFAULT NULL COMMENT '销售反审批时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `sale_price_id`(`sale_price_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '代发销售表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_distribution_sale
-- ----------------------------
INSERT INTO `erp_distribution_sale` VALUES (19, 20, NULL, '帅哥', '帅哥', 1.00, 15.00, 1.00, 12.00, 1, b'0', '1', '2025-05-22 02:12:11', '1', '2025-05-22 02:12:11', 30, NULL, NULL, NULL, 10, NULL, NULL, NULL, NULL);
INSERT INTO `erp_distribution_sale` VALUES (20, 21, NULL, '0', '天才', 0.00, 45.00, 1.00, 1.00, 1, b'0', '1', '2025-05-22 15:05:15', '1', '2025-05-22 15:05:15', 30, NULL, NULL, NULL, 10, NULL, NULL, NULL, NULL);
INSERT INTO `erp_distribution_sale` VALUES (21, 22, NULL, '帅哥', '天才', 1.00, 35.00, 6.00, 18.00, 1, b'0', '1', '2025-05-22 16:48:09', '1', '2025-05-22 22:32:48', 30, NULL, 1.00, '2025-05-23 17:55:12', 10, NULL, NULL, NULL, NULL);
INSERT INTO `erp_distribution_sale` VALUES (22, 23, NULL, '帅哥', '天才', 1.00, 15.00, 1.00, 12.00, 1, b'0', '1', '2025-05-22 23:24:20', '1', '2025-05-22 23:24:20', 30, NULL, NULL, NULL, 10, NULL, NULL, NULL, NULL);
INSERT INTO `erp_distribution_sale` VALUES (23, 24, NULL, '帅哥', '天才', 1.00, 45.00, 1.00, 14.00, 1, b'0', '1', '2025-05-22 23:25:10', '1', '2025-05-22 23:25:10', 30, NULL, NULL, NULL, 10, NULL, NULL, NULL, NULL);
INSERT INTO `erp_distribution_sale` VALUES (24, 25, NULL, '帅哥', '天才', 1.00, 15.00, 1.00, 12.00, 1, b'0', '1', '2025-05-22 23:31:07', '1', '2025-05-22 23:31:07', 30, NULL, NULL, NULL, 10, '出货备注', NULL, NULL, NULL);
INSERT INTO `erp_distribution_sale` VALUES (25, 26, NULL, '帅哥', '天才', 1.00, 15.00, 1.00, 12.00, 1, b'0', '1', '2025-05-22 23:38:50', '1', '2025-05-23 00:14:32', 30, NULL, NULL, NULL, 10, '额\n出货备注', '	\n中转人员', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
