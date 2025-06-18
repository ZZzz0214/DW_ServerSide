-- 为采购人员表添加 no 字段
ALTER TABLE `erp_purchaser` ADD COLUMN `no` varchar(50) NOT NULL COMMENT '采购人员编号' AFTER `id`;

-- 为供应商表添加 no 字段  
ALTER TABLE `erp_supplier` ADD COLUMN `no` varchar(50) NOT NULL COMMENT '供应商编号' AFTER `id`;

-- 为采购人员表的 no 字段添加唯一索引
ALTER TABLE `erp_purchaser` ADD UNIQUE INDEX `uk_no` (`no`);

-- 为供应商表的 no 字段添加唯一索引
ALTER TABLE `erp_supplier` ADD UNIQUE INDEX `uk_no` (`no`);

-- 初始化现有数据的 no 字段（如果有现有数据的话）
-- 采购人员
UPDATE `erp_purchaser` SET `no` = CONCAT('P', LPAD(id, 3, '0')) WHERE `no` = '' OR `no` IS NULL;

-- 供应商
UPDATE `erp_supplier` SET `no` = CONCAT('S', LPAD(id, 3, '0')) WHERE `no` = '' OR `no` IS NULL; 