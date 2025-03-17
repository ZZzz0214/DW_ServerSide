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

 Date: 17/03/2025 23:07:56
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_product
-- ----------------------------
DROP TABLE IF EXISTS `erp_product`;
CREATE TABLE `erp_product`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '产品编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称',
  `bar_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品条码',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品图片',
  `product_short_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品简称',
  `purchase_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '进货编码',
  `product_record` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品备案',
  `category_id` bigint(20) NOT NULL COMMENT '产品分类编号',
  `unit_id` int(11) NOT NULL COMMENT '单位编号',
  `brand` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品牌',
  `status` tinyint(4) NOT NULL COMMENT '产品状态',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品备注',
  `product_selling_points` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品卖点',
  `standard` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品规格',
  `product_dimensions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品长宽高',
  `carton_specifications` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '箱规',
  `carton_dimensions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '箱规长宽高',
  `carton_weight` double NULL DEFAULT NULL COMMENT '箱规重量',
  `available_stock_quantity` int(255) NULL DEFAULT NULL COMMENT '现货数量',
  `packaging_material_quantity` int(255) NULL DEFAULT NULL COMMENT '包材数量',
  `order_replenishment_lead_time` datetime NULL DEFAULT NULL COMMENT '返单时效',
  `shipping_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发货地址',
  `return_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退货地址',
  `logistics_company` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物流公司',
  `nonshipping_area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '不发货地',
  `addon_shipping_area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '加邮区域',
  `after_sales_standard` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '售后标准',
  `after_sales_script` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '售后话术',
  `public_domain_event_minimum_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '公域活动最低价',
  `live_streaming_event_minimun_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '直播活动最低价',
  `pinduoduo_event_minimum_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '拼多多活动最低价',
  `alibaba_event_minimun_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '阿里巴巴活动最低价',
  `group_buy_event_minimun_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '团购活动最低价',
  `supplier` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '供应商',
  `dropshipping_unit_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '代发单价',
  `wholesale_unit_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '批发单价',
  `base_shipping_fee` decimal(24, 6) NULL DEFAULT NULL COMMENT '基础运费',
  `purchase_details` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '采购详情',
  `purchase_note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '采购备注',
  `sales_details` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '销售详情',
  `sales_note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '销售备注',
  `level_one_dropshipping_price` decimal(24, 2) NULL DEFAULT NULL COMMENT '一级代发单价',
  `level_two_dropshipping_price` decimal(24, 2) NULL DEFAULT NULL COMMENT '二级代发单价',
  `level_one_wholesale_price` decimal(24, 2) NULL DEFAULT NULL COMMENT '一级批发单价',
  `level_two_wholesale_price` decimal(24, 2) NULL DEFAULT NULL COMMENT '二级批发单价',
  `dropshipping_shipping_fee_type` bit(1) NULL DEFAULT NULL COMMENT '代发运费类型',
  `fixed_shipping_fee` decimal(24, 2) NULL DEFAULT NULL COMMENT '固定运费',
  `first_item_quantity` int(255) NULL DEFAULT NULL COMMENT '首件数量',
  `first_item_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '首件价格',
  `additonal_item_quantity` int(255) NULL DEFAULT NULL COMMENT '续件数量',
  `additonal_item_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '续件价格',
  `first_weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '首重重量',
  `first_weight_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '首重价格',
  `first_batch_production_date` datetime NULL DEFAULT NULL COMMENT '首批生产日期',
  `additional_weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '续重重量',
  `additional_weight_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '续重价格',
  `expiry_day` int(11) NULL DEFAULT NULL COMMENT '保质期天数',
  `weight` decimal(24, 6) NULL DEFAULT NULL COMMENT '基础重量（kg）',
  `purchase_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '采购价格，单位：元',
  `sale_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '销售价格，单位：元',
  `min_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '最低价格，单位：元',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 产品表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
