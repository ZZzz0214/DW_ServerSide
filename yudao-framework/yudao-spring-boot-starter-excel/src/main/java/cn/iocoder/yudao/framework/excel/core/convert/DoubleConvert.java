package cn.iocoder.yudao.framework.excel.core.convert;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel Double 转换器
 * 
 * 处理Excel中的Double类型转换，能够处理空值、文本等异常情况
 * 当数据不符合Double类型时记录错误并返回null，用于数据校验
 * 
 * @author 芋道源码
 */
@Slf4j
public class DoubleConvert implements Converter<Double> {

    public DoubleConvert() {
        log.info("[DoubleConvert] 转换器被实例化");
    }

    @Override
    public Class<?> supportJavaTypeKey() {
        log.info("[DoubleConvert] supportJavaTypeKey被调用，返回Double.class");
        return Double.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        log.info("[DoubleConvert] supportExcelTypeKey被调用，返回NUMBER");
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public Double convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        // 添加调试日志，确认转换器是否被调用
        String fieldName = contentProperty.getField().getName();
        log.info("[DoubleConvert] 转换器被调用 - 字段: {}, 类型: {}", fieldName, readCellData.getType());
        
        // 如果单元格为空，返回null（空值允许）
        if (readCellData.getType() == CellDataTypeEnum.EMPTY) {
            log.info("[DoubleConvert] 单元格为空，返回null");
            return null;
        }
        
        // 处理NUMBER类型的单元格
        if (readCellData.getType() == CellDataTypeEnum.NUMBER) {
            Number numberValue = readCellData.getNumberValue();
            if (numberValue == null) {
                log.info("[DoubleConvert] NUMBER类型单元格值为null，返回null");
                return null;
            }
            Double result = numberValue.doubleValue();
            log.info("[DoubleConvert] NUMBER类型转换成功: {} -> {}", numberValue, result);
            return result;
        }
        
        // 处理STRING类型的单元格
        if (readCellData.getType() == CellDataTypeEnum.STRING) {
            String stringValue = readCellData.getStringValue();
            log.info("[DoubleConvert] STRING类型单元格值: '{}'", stringValue);
            
            // 如果字符串为空或只包含空白字符，返回null（空值允许）
            if (StrUtil.isBlank(stringValue)) {
                log.info("[DoubleConvert] 字符串为空，返回null");
                return null;
            }
            
            try {
                // 尝试转换为Double
                Double result = Double.parseDouble(stringValue.trim());
                log.info("[DoubleConvert] STRING类型转换成功: '{}' -> {}", stringValue, result);
                return result;
            } catch (NumberFormatException e) {
                // 记录错误信息，但不抛出异常，返回null让后续处理
                String errorMsg = String.format("字段[%s]的值[%s]不是有效的数字格式", fieldName, stringValue);
                log.warn("[convertToJavaData]{}", errorMsg);
                
                // 尝试从ReadCellData中获取行号信息
                int rowIndex = getRowIndexFromReadCellData(readCellData);
                
                // 将错误信息存储到ThreadLocal中，供后续处理使用
                ConversionErrorHolder.addError(fieldName, stringValue, errorMsg, rowIndex);
                
                // 返回null，不抛出异常
                return null;
            }
        }

        // 其他类型，尝试转换为字符串再处理
        String stringValue = readCellData.getStringValue();
        log.info("[DoubleConvert] 其他类型单元格值: '{}'", stringValue);
        
        if (StrUtil.isBlank(stringValue)) {
            log.info("[DoubleConvert] 其他类型字符串为空，返回null");
            return null;
        }
        
        try {
            Double result = Double.parseDouble(stringValue.trim());
            log.info("[DoubleConvert] 其他类型转换成功: '{}' -> {}", stringValue, result);
            return result;
        } catch (NumberFormatException e) {
            // 记录错误信息，但不抛出异常，返回null让后续处理
            String errorMsg = String.format("字段[%s]的值[%s]不是有效的数字格式", fieldName, stringValue);
            log.warn("[convertToJavaData]{}", errorMsg);
            
            // 尝试从ReadCellData中获取行号信息
            int rowIndex = getRowIndexFromReadCellData(readCellData);
            
            // 将错误信息存储到ThreadLocal中，供后续处理使用
            ConversionErrorHolder.addError(fieldName, stringValue, errorMsg, rowIndex);
            
            // 返回null，不抛出异常
            return null;
        }
    }

    @Override
    public WriteCellData<String> convertToExcelData(Double value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(value.toString());
    }

    /**
     * 尝试从ReadCellData中获取行号信息
     */
    private int getRowIndexFromReadCellData(ReadCellData readCellData) {
        // 优先使用ConversionErrorHolder中的当前行号，这是最可靠的
        int currentRowIndex = ConversionErrorHolder.getCurrentRowIndex();
        log.debug("[DoubleConvert] 使用ConversionErrorHolder中的当前行号: {}", currentRowIndex);
        
        // 如果当前行号有效（大于0），直接使用
        if (currentRowIndex > 0) {
            return currentRowIndex;
        }
        
        // 如果当前行号无效，尝试从ReadCellData中获取
        try {
            // 尝试通过反射获取行号信息
            java.lang.reflect.Field rowIndexField = readCellData.getClass().getDeclaredField("rowIndex");
            rowIndexField.setAccessible(true);
            Object rowIndexObj = rowIndexField.get(readCellData);
            if (rowIndexObj instanceof Integer) {
                int rowIndex = (Integer) rowIndexObj;
                // EasyExcel的行号从0开始，转换为从1开始
                int convertedRowIndex = rowIndex + 1;
                log.debug("[DoubleConvert] 从ReadCellData获取行号: {} -> {}", rowIndex, convertedRowIndex);
                return convertedRowIndex;
            }
        } catch (Exception e) {
            // 如果无法获取行号，记录日志
            log.debug("[DoubleConvert] 无法从ReadCellData获取行号: {}", e.getMessage());
        }

        // 如果都无法获取，返回1作为默认值（避免返回0）
        log.warn("[DoubleConvert] 无法获取行号，使用默认值1");
        return 1;
    }

} 