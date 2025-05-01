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

 Date: 02/05/2025 05:49:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_product
-- ----------------------------
DROP TABLE IF EXISTS `erp_product`;
CREATE TABLE `erp_product`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '产品编号（主键，自增）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品名称',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品图片',
  `product_short_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品简称',
  `shipping_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发货编码',
  `unit_id` bigint(20) NULL DEFAULT NULL COMMENT '单位编号',
  `standard` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品规格',
  `weight` decimal(24, 6) NULL DEFAULT NULL COMMENT '产品重量（单位：kg）',
  `production_date` datetime NULL DEFAULT NULL COMMENT '产品日期',
  `expiry_day` int(11) NULL DEFAULT NULL COMMENT '保质期天数',
  `brand` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品牌名称',
  `category_id` bigint(20) NULL DEFAULT NULL COMMENT '产品品类编号',
  `status` tinyint(4) NULL DEFAULT NULL COMMENT '产品状态',
  `product_selling_points` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品卖点',
  `bar_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '条形编号',
  `product_record` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备案编号',
  `execution_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '执行编号',
  `trademark_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商标编号',
  `total_quantity` int(255) NULL DEFAULT NULL COMMENT '现货数量',
  `packaging_material_quantity` int(255) NULL DEFAULT NULL COMMENT '包材数量',
  `order_replenishment_lead_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '返单时效',
  `order_replenishment_lead_time_unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '返单时效单位',
  `product_length` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品长',
  `product_width` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品宽',
  `product_height` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品高',
  `product_dimensions_unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品长宽高单位',
  `carton_length` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '箱长',
  `carton_width` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '箱宽',
  `carton_height` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '箱高',
  `carton_dimensions_unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '箱长宽高单位',
  `carton_weight` double NULL DEFAULT NULL COMMENT '箱规重量',
  `carton_weight_unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '箱规重量单位',
  `shipping_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发货地址',
  `return_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退货地址',
  `logistics_company` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '快递公司',
  `nonshipping_area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '不发货区',
  `addon_shipping_area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '加邮地区',
  `after_sales_standard` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '售后标准',
  `after_sales_script` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '售后话术',
  `public_domain_event_minimum_price` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '公域活动最低价',
  `live_streaming_event_minimun_price` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '直播活动最低价',
  `pinduoduo_event_minimum_price` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '多多活动最低价',
  `alibaba_event_minimun_price` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '阿里活动最低价',
  `group_buy_event_minimun_price` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '团购活动最低价',
  `purchaser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '采购人员',
  `supplier` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '供应商名',
  `purchase_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '采购单价（单位：元）',
  `wholesale_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '批发单价（单位：元）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `min_wholesale_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '对外最低批发单价（单位：元）',
  `min_purchase_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '对外最低采购单价（单位：元）',
  `shipping_fee_type` tinyint(4) NULL DEFAULT NULL COMMENT '运费类型（0：固定运费，1：按件计费，2：按重计费）',
  `fixed_shipping_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '固定运费（单位：元）',
  `additional_item_quantity` int(11) NULL DEFAULT NULL COMMENT '按件数量',
  `additional_item_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '按件价格（单位：元）',
  `first_weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '首重重量（单位：kg）',
  `first_weight_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '首重价格（单位：元）',
  `additional_weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '续重重量（单位：kg）',
  `additional_weight_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '续重价格（单位：元）',
  `expiry_unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '天' COMMENT '保质日期单位，默认值为“天”',
  `weight_unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'g' COMMENT '产品重量单位，默认值为“g”',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否删除（0：未删除，1：已删除）',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 产品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_product
-- ----------------------------
INSERT INTO `erp_product` VALUES (10, '123', 'http://test.yudao.iocoder.cn/38d814c02acd0c31042156be3649a1a37644475ee2a354b74a0a1932757bb41c.png', '123', '123', NULL, '123', 1.000000, '2025-05-01 00:00:00', 1, '123', 87, 0, '123', '123', '123', '123', '123', 1, 1, '1970-01-01 08:00:00.123', '天', '1', '2', '3', 'cm', '1', '2', '3', 'm', 1, 'g', '123', '123', '123', '123', '123', '123', '123', '123', '123', '123', '123', '123', '123', '123', 1.000000, 1.000000, '123', NULL, 1.000000, 1, NULL, 123, 123.00, NULL, NULL, NULL, NULL, '年', 'kg', '1', '2025-05-02 05:42:36', '1', '2025-05-02 05:42:36', b'0', 1);
INSERT INTO `erp_product` VALUES (11, '产品名称', 'http://test.yudao.iocoder.cn/38d814c02acd0c31042156be3649a1a37644475ee2a354b74a0a1932757bb41c.png', '产品简称', '发货编码', NULL, '产品规格', 1.000000, '2025-05-01 00:00:00', 1, '品牌名称', 87, 0, '产品卖点', '条形编号', '备案编号', '执行编号', '商标编号', 1, 1, '1970-01-01 08:00:00.001', '天', '1', '2', '3', 'cm', '1', '2', '3', 'm', 1, 'g', '发货地址', '退货地址', '快递公司', '不发货区', '加邮地区', '售后标准', '售后话术', '公域活动最低价', '直播活动最低价', '多多活动最低价', '阿里活动最低价', '团购活动最低价', '123', '供应商名', 1.000000, 1.000000, '123', NULL, 1.000000, 1, NULL, 250, 600.00, NULL, NULL, NULL, NULL, '年', 'kg', '1', '2025-05-02 05:44:47', '1', '2025-05-02 05:44:47', b'0', 1);

SET FOREIGN_KEY_CHECKS = 1;
