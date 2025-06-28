package cn.iocoder.yudao.framework.excel.core.listener;

import cn.iocoder.yudao.framework.excel.core.convert.ConversionErrorHolder;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;

/**
 * EasyExcel行号监听器
 * 
 * 用于在读取Excel时设置当前处理的行号，供转换器使用
 * 
 * @author 芋道源码
 */
@Slf4j
public class RowIndexListener<T> implements ReadListener<T> {

    @Override
    public void invoke(T data, AnalysisContext context) {
        // 获取当前行号（EasyExcel的行号从0开始，我们转换为从1开始）
        int rowIndex = context.readRowHolder().getRowIndex() + 1;
        
        // 设置当前处理的行号
        ConversionErrorHolder.setCurrentRowIndex(rowIndex);
        
        log.debug("处理第{}行数据", rowIndex);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 读取完成后的处理
        log.debug("Excel读取完成，总共处理{}行数据", context.readRowHolder().getRowIndex());
    }
} 