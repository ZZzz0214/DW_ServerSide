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

 Date: 08/05/2025 13:43:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_sale_price
-- ----------------------------
DROP TABLE IF EXISTS `erp_sale_price`;
CREATE TABLE `erp_sale_price`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '销售价格表id（主键，自增）',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '销售价格表的编号',
  `group_product_id` bigint(20) NOT NULL COMMENT '组品编号',
  `product_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品图片（->组品编号）',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称（->组品编号）',
  `product_short_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品简称（->组品编号）',
  `customer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户名称',
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
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 62 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 销售价格表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_sale_price
-- ----------------------------
INSERT INTO `erp_sale_price` VALUES (43, NULL, 25, '', '图书馆*3 + 66*6 + 123123*9', '', '张', 0.000000, 0.000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-10 17:43:04', '1', '2025-04-15 01:58:23', b'1', 1);
INSERT INTO `erp_sale_price` VALUES (44, NULL, 25, '', '图书馆*3 + 66*6 + 123123*9', '', '张', 30.000000, 29.000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-10 22:09:10', '1', '2025-04-15 01:58:23', b'1', 1);
INSERT INTO `erp_sale_price` VALUES (45, NULL, 26, '', '图书馆*1 + 张三*1', '', '陈', 45.000000, 41.000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-10 22:09:26', '1', '2025-04-15 01:58:23', b'1', 1);
INSERT INTO `erp_sale_price` VALUES (46, NULL, 26, '', '图书馆*1 + 张三*1', '', '张', 0.000000, 0.000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-11 14:23:25', '1', '2025-04-11 14:40:25', b'1', 1);
INSERT INTO `erp_sale_price` VALUES (47, NULL, 25, '', '图书馆*3 + 66*6 + 123123*9', '', '王', 43.000000, 22.000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-11 23:06:13', '1', '2025-04-15 01:58:23', b'1', 1);
INSERT INTO `erp_sale_price` VALUES (48, NULL, 26, '', '图书馆*1 + 张三*1', '', '李', 123.000000, 34200.000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-12 00:13:04', '1', '2025-04-15 01:58:23', b'1', 1);
INSERT INTO `erp_sale_price` VALUES (49, NULL, 28, '', '图书馆*5 + 张三*3', '张_图', '尼', 34.000000, 2130.000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-12 00:17:20', '1', '2025-04-15 01:58:23', b'1', 1);
INSERT INTO `erp_sale_price` VALUES (50, NULL, 27, '', '123123*4 + 66*2', '简称321', '张', 43.000000, 23.000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-12 00:20:51', '1', '2025-04-15 01:58:23', b'1', 1);
INSERT INTO `erp_sale_price` VALUES (51, NULL, 29, '', '66*3 + 图书馆*4', '123123', '31', 120.000000, 210.000000, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-12 00:24:17', '1', '2025-04-15 01:58:23', b'1', 1);
INSERT INTO `erp_sale_price` VALUES (52, NULL, 30, '', '图书馆*3 + 张三*2', '图书三', 'hao', 26.000000, 25.000000, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-12 00:27:55', '1', '2025-04-15 01:58:23', b'1', 1);
INSERT INTO `erp_sale_price` VALUES (53, NULL, 28, '', '图书馆*7 + 张5*4', '张_图', '123', 0.000000, 0.000000, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-25 01:31:29', '1', '2025-04-25 01:31:29', b'0', 1);
INSERT INTO `erp_sale_price` VALUES (54, NULL, 29, '', '66*3 + 图书馆*5 + 66*2', '123123', '123', 0.000000, 0.000000, NULL, 0, 21312.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-30 14:58:59', '1', '2025-04-30 14:58:59', b'0', 1);
INSERT INTO `erp_sale_price` VALUES (55, NULL, 32, '', '称王*1 + 笔记本*1', '产品a', '123', 1.000000, 2.000000, NULL, 1, NULL, NULL, NULL, 5, 10.00, NULL, NULL, NULL, NULL, '1', '2025-04-30 15:02:03', '1', '2025-04-30 15:02:03', b'0', 1);
INSERT INTO `erp_sale_price` VALUES (56, NULL, 33, '', '一加Ace2Pro*1 + 图书馆*1 + 张5*1 + 李6*1 + 笔记本*1 + 称王*1 + 123123*1', '123', '123', 1.000000, 1.000000, NULL, 1, NULL, NULL, NULL, 123, 123.00, NULL, NULL, NULL, NULL, '1', '2025-05-02 00:56:33', '1', '2025-05-02 00:56:33', b'0', 1);
INSERT INTO `erp_sale_price` VALUES (57, NULL, 33, '', '一加Ace2Pro*1 + 图书馆*1 + 张5*1 + 李6*1 + 笔记本*1 + 称王*1 + 123123*1', '123', '123', 1.000000, 1.000000, NULL, 1, NULL, NULL, NULL, 11, 11.00, NULL, NULL, NULL, NULL, '1', '2025-05-02 00:59:21', '1', '2025-05-02 00:59:21', b'0', 1);
INSERT INTO `erp_sale_price` VALUES (58, NULL, 33, '', '一加Ace2Pro*1 + 图书馆*1 + 张5*1 + 李6*1 + 笔记本*1 + 称王*1 + 123123*1', '123', '123', 2.000000, 2.000000, NULL, NULL, 0.00, NULL, NULL, 0, 0.00, 0.00, 0.00, 0.00, 0.00, '1', '2025-05-02 01:19:19', '1', '2025-05-02 01:19:19', b'0', 1);
INSERT INTO `erp_sale_price` VALUES (59, NULL, 33, '', '一加Ace2Pro*1 + 图书馆*1 + 张5*1 + 李6*1 + 笔记本*1 + 称王*1 + 123123*1', '123', '123', 123.000000, 123.000000, NULL, 1, 0.00, NULL, NULL, 123, 321.00, 0.00, 0.00, 0.00, 0.00, '1', '2025-05-02 01:30:20', '1', '2025-05-02 01:30:20', b'0', 1);
INSERT INTO `erp_sale_price` VALUES (60, NULL, 33, '', '一加Ace2Pro*1 + 图书馆*1 + 张5*1 + 李6*1 + 笔记本*1 + 称王*1 + 123123*1', '123', '123', 1.000000, 2.000000, NULL, 1, 0.00, NULL, NULL, 123, 321.00, 0.00, 0.00, 0.00, 0.00, '1', '2025-05-02 01:40:49', '1', '2025-05-02 01:40:49', b'0', 1);
INSERT INTO `erp_sale_price` VALUES (61, NULL, 33, '', '一加Ace2Pro*1 + 图书馆*1 + 张5*1 + 李6*1 + 笔记本*1 + 称王*1 + 123123*1', '123', '123123', 1.000000, 1.000000, NULL, 1, NULL, NULL, NULL, 123, 123.00, NULL, NULL, NULL, NULL, '1', '2025-05-02 11:32:03', '1', '2025-05-02 11:32:03', b'0', 1);

SET FOREIGN_KEY_CHECKS = 1;
