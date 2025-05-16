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

 Date: 16/05/2025 15:22:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_group_buying
-- ----------------------------
DROP TABLE IF EXISTS `erp_group_buying`;
CREATE TABLE `erp_group_buying`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '团购货盘编号',
  `no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '编号',
  `product_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品图片',
  `brand_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品牌名称',
  `product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品名称',
  `product_spec` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品规格',
  `product_sku` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品SKU',
  `market_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '市场价格',
  `shelf_life` date NULL DEFAULT NULL COMMENT '保质日期',
  `product_stock` int(11) NULL DEFAULT NULL COMMENT '产品库存',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `core_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '核心价格',
  `distribution_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '分发价格',
  `supply_group_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '供团价格',
  `selling_commission` decimal(10, 2) NULL DEFAULT NULL COMMENT '帮卖佣金',
  `group_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '开团价格',
  `channel_profit` decimal(10, 2) NULL DEFAULT NULL COMMENT '渠道毛利',
  `group_mechanism` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '开团机制',
  `express_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '快递费用',
  `tmall_jd` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '天猫京东',
  `public_data` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '公域数据',
  `private_data` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '私域数据',
  `brand_endorsement` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品牌背书',
  `competitive_analysis` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '竞品分析',
  `express_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '快递公司',
  `shipping_time` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发货时效',
  `shipping_area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发货地区',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 团购货盘表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_group_buying
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
