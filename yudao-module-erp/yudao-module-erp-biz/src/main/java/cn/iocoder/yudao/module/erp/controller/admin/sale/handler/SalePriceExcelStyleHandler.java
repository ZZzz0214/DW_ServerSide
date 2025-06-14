package cn.iocoder.yudao.module.erp.controller.admin.sale.handler;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.*;

import java.util.List;

/**
 * 销售价格Excel导出样式处理器
 * 用于设置产品名称和产品简称列的表头为红色
 */
public class SalePriceExcelStyleHandler implements CellWriteHandler {

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        // 只处理表头
        if (!Boolean.TRUE.equals(isHead)) {
            return;
        }
        
        // 获取列标题
        if (head == null || head.getHeadNameList() == null || head.getHeadNameList().isEmpty()) {
            return;
        }
        
        String columnName = head.getHeadNameList().get(0);
        
        // 只为产品名称和产品简称设置红色
        if ("产品名称".equals(columnName) || "产品简称".equals(columnName)) {
            Workbook workbook = writeSheetHolder.getSheet().getWorkbook();
            CellStyle cellStyle = workbook.createCellStyle();
            
            // 复制原有样式
            CellStyle originalStyle = cell.getCellStyle();
            if (originalStyle != null) {
                cellStyle.cloneStyleFrom(originalStyle);
            }
            
            // 创建红色字体
            Font font = workbook.createFont();
            // 复制原有字体属性
            if (originalStyle != null && originalStyle.getFontIndex() > 0) {
                Font originalFont = workbook.getFontAt(originalStyle.getFontIndex());
                font.setBold(originalFont.getBold());
                font.setFontHeightInPoints(originalFont.getFontHeightInPoints());
                font.setFontName(originalFont.getFontName());
            } else {
                font.setBold(true);
            }
            
            // 设置红色
            font.setColor(IndexedColors.RED.getIndex());
            cellStyle.setFont(font);
            
            // 应用样式
            cell.setCellStyle(cellStyle);
        }
    }
} 