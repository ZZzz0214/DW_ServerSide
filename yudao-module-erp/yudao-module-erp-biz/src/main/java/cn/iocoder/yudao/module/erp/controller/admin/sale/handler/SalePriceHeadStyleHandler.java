package cn.iocoder.yudao.module.erp.controller.admin.sale.handler;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.AbstractCellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.*;

/**
 * 销售价格Excel导出表头样式处理器
 */
public class SalePriceHeadStyleHandler extends AbstractCellWriteHandler {

    @Override
    public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, 
                               Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        // 只处理表头
        if (!Boolean.TRUE.equals(isHead) || head == null) {
            return;
        }
        
        // 获取列索引
        int columnIndex = cell.getColumnIndex();
        
        // 根据列索引判断（产品名称通常是第3列，产品简称是第4列）
        // 或者根据head获取列名
        if (head.getHeadNameList() != null && !head.getHeadNameList().isEmpty()) {
            String columnName = head.getHeadNameList().get(0);
            if ("产品名称".equals(columnName) || "产品简称".equals(columnName)) {
                Workbook workbook = writeSheetHolder.getSheet().getWorkbook();
                CellStyle redHeaderStyle = workbook.createCellStyle();
                
                // 设置红色字体
                Font redFont = workbook.createFont();
                redFont.setColor(IndexedColors.RED.getIndex());
                redFont.setBold(true);
                redHeaderStyle.setFont(redFont);
                
                // 设置其他样式（边框、对齐等）
                redHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
                redHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                redHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                redHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                
                // 设置边框
                redHeaderStyle.setBorderBottom(BorderStyle.THIN);
                redHeaderStyle.setBorderLeft(BorderStyle.THIN);
                redHeaderStyle.setBorderRight(BorderStyle.THIN);
                redHeaderStyle.setBorderTop(BorderStyle.THIN);
                
                cell.setCellStyle(redHeaderStyle);
            }
        }
    }
} 