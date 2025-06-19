-- 代发辅助表添加备注信息和状态信息字段
ALTER TABLE erp_dropship_assist 
ADD COLUMN remark VARCHAR(500) COMMENT '备注信息',
ADD COLUMN status VARCHAR(50) COMMENT '状态信息';

-- 更新现有数据的默认状态
UPDATE erp_dropship_assist SET status = 'pending' WHERE status IS NULL; 