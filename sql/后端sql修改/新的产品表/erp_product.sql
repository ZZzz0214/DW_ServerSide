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

 Date: 02/05/2025 05:07:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_product
-- ----------------------------
DROP TABLE IF EXISTS `erp_product`;
CREATE TABLE `erp_product`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '产品编号（主键，自增）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品图片',
  `product_short_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品简称',
  `shipping_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发货编码',
  `standard` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品规格',
  `weight` decimal(24, 6) NULL DEFAULT NULL COMMENT '产品重量（单位：kg）',
  `production_date` datetime NULL DEFAULT NULL COMMENT '产品日期',
  `expiry_day` int(11) NULL DEFAULT NULL COMMENT '保质期天数',
  `brand` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品牌名称',
  `category_id` bigint(20) NOT NULL COMMENT '产品品类编号',
  `status` tinyint(4) NOT NULL COMMENT '产品状态',
  `product_selling_points` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品卖点',
  `bar_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '条形编号',
  `product_record` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备案编号',
  `execution_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '执行编号',
  `trademark_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商标编号',
  `total_quantity` int(255) NULL DEFAULT NULL COMMENT '现货数量',
  `packaging_material_quantity` int(255) NULL DEFAULT NULL COMMENT '包材数量',
  `order_replenishment_lead_time` datetime NULL DEFAULT NULL COMMENT '返单时效',
  `product_dimensions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品长宽高',
  `carton_specifications` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品箱规',
  `carton_dimensions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '箱长宽高',
  `carton_weight` double NULL DEFAULT NULL COMMENT '箱规重量',
  `shipping_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发货地址',
  `return_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退货地址',
  `logistics_company` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '快递公司',
  `nonshipping_area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '不发货区',
  `addon_shipping_area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '加邮地区',
  `after_sales_standard` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '售后标准',
  `after_sales_script` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '售后话术',
  `public_domain_event_minimum_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '公域活动最低价',
  `live_streaming_event_minimun_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '直播活动最低价',
  `pinduoduo_event_minimum_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '多多活动最低价',
  `alibaba_event_minimun_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '阿里活动最低价',
  `group_buy_event_minimun_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '团购活动最低价',
  `purchaser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '采购人员',
  `supplier` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '供应商名',
  `purchase_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '采购单价（单位：元）',
  `wholesale_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '批发单价（单位：元）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注信息',
  `min_purchase_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '对外最低采购单价（单位：元）',
  `min_wholesale_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '对外最低批发单价（单位：元）',
  `shipping_fee_type` tinyint(4) NULL DEFAULT NULL COMMENT '运费类型（0：固定运费，1：按件计费，2：按重计费）',
  `fixed_shipping_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '固定运费（单位：元）',
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
  `unit_id` bigint(20) NULL DEFAULT NULL COMMENT 'unit_id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 产品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_product
-- ----------------------------
INSERT INTO `erp_product` VALUES (3, '123123', 'http://test.yudao.iocoder.cn/5864d8c1a8c2d490f4b7bc48659b1b43248adfa799e01767b1eeecafd6e48419.jpg', '123123', '123123', '123123', 1.000000, '1970-01-01 08:00:00', 1, '123123', 87, 1, '123123', '123', '123', '123', '123', 1, 1, '1970-01-01 08:00:00', '123', '123', '123', 1, '123', '123', '123', '123', '213', '123', '123', 1.000000, 0.000000, 1.000000, 0.000000, 0.000000, '123', '123', 1.000000, 1.000000, '123', 1.000000, 1.000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-03-25 01:00:44', '1', '2025-03-25 01:00:44', b'0', 1, NULL);
INSERT INTO `erp_product` VALUES (4, '一加Ace3Pro', 'http://test.yudao.iocoder.cn/5864d8c1a8c2d490f4b7bc48659b1b43248adfa799e01767b1eeecafd6e48419.jpg', '66', '66', '66', 1.000000, '1970-01-01 08:00:00', 1, '66', 87, 0, '66', '5656', '5656', '5656', '5656', 1, 1, '1970-01-01 08:00:06', '5665', '5656', '6556', 1, '5656', '6556', '6556', '6565', '5665', '6556', '6556', 1.000000, 0.000000, 1.000000, 0.000000, 0.000000, '5656', '6565', 3.000000, 3.000000, '5665', 1.000000, 1.000000, 0, 666.00, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-03-25 02:20:34', '1', '2025-05-01 00:09:27', b'0', 1, NULL);
INSERT INTO `erp_product` VALUES (5, '图书馆', 'http://test.yudao.iocoder.cn/fd7485e5d69ad7b8c7f4ec2e31cec89bd6e99b838b34ad121069ff6d2735bb29.jpg', '座位', '123123', '123123', 4.000000, '2025-04-14 00:00:00', 3, '123213', 87, 0, '12312', '321321', '312312', '321321', '321312', 1, 1, '1970-01-01 08:00:00', '321', '312', '312', 1, '312', '312', '312', '1321', '123', '1213', '132', 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, '32113', '123123', 1.000000, 1.000000, '123123', 1.000000, 1.000000, 0, 312.00, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-01 17:14:47', '1', '2025-04-03 19:06:51', b'0', 1, NULL);
INSERT INTO `erp_product` VALUES (6, '张5', 'http://test.yudao.iocoder.cn/fd7485e5d69ad7b8c7f4ec2e31cec89bd6e99b838b34ad121069ff6d2735bb29.jpg', '123', '123', '123', 5.000000, '2025-04-18 00:00:00', 1, '123', 87, 0, '123', '123', '123', '123', '123', 1, 1, '1970-01-01 08:00:00', '2', '3', '3', 1, '1', '2', '3', '4', '5', '6', '7', 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, '123', '123', 10.000000, 3.000000, '123', 1.000000, 1.000000, 2, 123.00, NULL, NULL, 10.00, 14.00, 6.00, 8.00, '1', '2025-04-05 23:50:59', '1', '2025-04-14 09:40:40', b'0', 1, NULL);
INSERT INTO `erp_product` VALUES (7, '李6', 'http://test.yudao.iocoder.cn/fd7485e5d69ad7b8c7f4ec2e31cec89bd6e99b838b34ad121069ff6d2735bb29.jpg', '123', '123', '1', 1.000000, '2025-04-24 00:00:00', 1, '1', 87, 0, '1', '1', '2', '3', '4', 1, 1, '1970-01-01 08:00:00', '2', '3', '4', 1, '1', '2', '3', '4', '5', '6', '7', 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, '1', '2', 4.000000, 8.000000, '1', 1.000000, 1.000000, 1, 123.00, 4, 10.00, NULL, NULL, NULL, NULL, '1', '2025-04-05 23:58:45', '1', '2025-04-14 08:46:55', b'0', 1, NULL);
INSERT INTO `erp_product` VALUES (8, '笔记本', 'http://test.yudao.iocoder.cn/fd7485e5d69ad7b8c7f4ec2e31cec89bd6e99b838b34ad121069ff6d2735bb29.jpg', '神州', '302010', '3021', 2.000000, '2025-04-22 00:00:00', 7, '神州', 87, 0, '很好用', '123123', '123123', '456456', '456456', 3, 3, '1970-01-01 08:00:00', '23', '45', '54', 2, '6745', '456', '435', '234', '4567', '324', '345', 3.000000, 2.000000, 2.000000, 2.000000, 2.000000, '76234', '张三', 4.000000, 7.000000, '便宜', 2.000000, 4.000000, 0, 30.00, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-04-12 20:21:00', '1', '2025-04-12 20:21:00', b'0', 1, NULL);
INSERT INTO `erp_product` VALUES (9, '称王', 'http://test.yudao.iocoder.cn/fd7485e5d69ad7b8c7f4ec2e31cec89bd6e99b838b34ad121069ff6d2735bb29.jpg', '123', '3333', '33334', 3.000000, '2025-04-16 00:00:00', 3, '2121', 87, 0, '2312312', '213123', '123123', '123123', '123123', 3, 4, '1970-01-01 08:02:04', '4324321', '123123', '23423', 4, '21312', '213123', '123123', '123123', '12312', '123123', '123123', 3.000000, 2.000000, 2.000000, 4.000000, 5.000000, '21321', '嗡嗡嗡', 5.000000, 4.000000, '2332', 3.000000, 2.000000, 1, NULL, 4, 15.00, NULL, NULL, NULL, NULL, '1', '2025-04-15 00:41:39', '1', '2025-04-15 00:41:39', b'0', 1, NULL);

SET FOREIGN_KEY_CHECKS = 1;
